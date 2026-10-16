import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {PGlite} from '@electric-sql/pglite';
test('PostgreSQL verification RPCs and RLS block forged evidence, brokers and stale approvals',async()=>{
  const pg=new PGlite();
  const owner='11111111-1111-4111-8111-111111111111',reviewer='22222222-2222-4222-8222-222222222222',other='33333333-3333-4333-8333-333333333333';
  const q=(sql,args=[])=>pg.query(sql,args);
  async function user(id,admin=false){await pg.exec('reset role;set role authenticated');await q("select set_config('request.jwt.claim.sub',$1,false),set_config('request.jwt.claims',$2,false)",[id,JSON.stringify({app_metadata:{role:admin?'admin':'user'}})]);}
  async function privileged(){await pg.exec('reset role');}
  async function anonymous(){await pg.exec('reset role;set role anon');await q("select set_config('request.jwt.claim.sub','',false),set_config('request.jwt.claims','{}',false)");}
  try{
    await pg.exec(`create role anon;create role authenticated;create role service_role bypassrls;
      create schema auth;create table auth.users(id uuid primary key);
      create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
      create function auth.jwt() returns jsonb language sql stable as $$select coalesce(nullif(current_setting('request.jwt.claims',true),''),'{}')::jsonb$$;
      grant usage on schema auth to anon,authenticated,service_role;`);
    const initial=(await readFile(new URL('../../supabase/migrations/20260101000000_initial_schema.sql',import.meta.url),'utf8')).replace('create extension if not exists "pgcrypto";','');
    await pg.exec(initial);
    await pg.exec('grant select,insert,update,delete on all tables in schema public to anon,authenticated;');
    await pg.exec(await readFile(new URL('../../supabase/migrations/20261012000000_verification_workflow.sql',import.meta.url),'utf8'));
    for(const id of [owner,reviewer,other])await q('insert into auth.users values($1)',[id]);
    await user(owner);
    const app=(await q("select public.submit_verification('property','TITLE','UG',$1) id",[JSON.stringify({unit:'1',relationship:'individual_owner',title:'Home',location:'Main Road',city:'Kampala',price_ugx:500})])).rows[0].id;
    await assert.rejects(q("update public.verification_applications set status='approved' where id=$1",[app]),/permission denied/);
    await assert.rejects(q("insert into public.verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at) values($1,$2,'identity',true,'FORGED',now()+interval '1 year')",[app,owner]),/permission denied/);
    await q('select public.save_verification_extraction($1,$2)',[app,JSON.stringify({approved:true,identityVerified:true})]);
    assert.equal((await q('select public.verification_ready($1) ready',[app])).rows[0].ready,false);
    await assert.rejects(q("select public.submit_verification('property','TITLE','UG',$1)",[JSON.stringify({unit:'1',relationship:'broker'})]),/brokers excluded/);
    await user(other);assert.equal((await q('select * from public.verification_applications')).rows.length,0);
    await assert.rejects(q('select public.save_verification_extraction($1,$2)',[app,'{}']),/access denied/);
    await user(reviewer,true);await assert.rejects(q("select public.review_verification($1,'approve','Try without evidence')",[app]),/Approval blocked/);
    await privileged();await q("insert into public.verification_requirements values('UG','property','*',true)");
    for(const kind of ['identity','liveness','ownership'])await q("insert into public.verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at,details) values($1,$2,$3,true,'SIGNED-registry-reference',now()+interval '1 year',$4)",[app,owner,kind,JSON.stringify({property_id:'TITLE',unit:'1',jurisdiction:'UG',owner_id:owner})]);
    await user(reviewer,true);await q("select public.review_verification($1,'approve','Registry title and unit matched')",[app]);
    await assert.rejects(q('select public.publish_verified_property($1)',[app]),/Only the independently verified owner/);
    await user(owner);const record=(await q('select public.publish_verified_property($1) id',[app])).rows[0].id;
    await assert.rejects(q('select public.publish_verified_property($1)',[app]),/Already published/);
    await assert.rejects(q("select public.submit_verification('property',' title ',' ug ',$1)",[JSON.stringify({unit:'1',relationship:'individual_owner'})]),/duplicate key/);
    await privileged();await anonymous();assert.equal((await q('select id from public.properties')).rows.length,1);
    await privileged();await q("update public.verification_evidence set details=jsonb_set(details,'{transferred}','true'::jsonb) where application_id=$1 and kind='ownership'",[app]);
    assert.equal((await q('select status from public.verification_applications where id=$1',[app])).rows[0].status,'suspended');
    await user(reviewer,true);await assert.rejects(q("select public.review_verification($1,'approve','Ignore transfer')",[app]),/Approval blocked/);
    await privileged();await anonymous();assert.equal((await q('select id from public.properties')).rows.length,0);
    await privileged();await assert.rejects(q('delete from public.verification_audit'),/append-only/);
    await user(owner);assert.equal((await q('update public.properties set is_verified=true where id=$1',[record])).affectedRows,0);
    // A representative must independently prove authority for the actual owning company.
    await user(owner);
    const company=(await q("select public.submit_verification('property','COMPANY-TITLE','UG',$1) id",[JSON.stringify({unit:'9',relationship:'owning_company_representative'})])).rows[0].id;
    await privileged();
    for(const kind of ['identity','liveness','ownership','company_authority'])await q("insert into public.verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at,details) values($1,$2,$3,true,'SIGNED-company-registry',now()+interval '1 year',$4)",[company,owner,kind,JSON.stringify({property_id:'COMPANY-TITLE',unit:'9',jurisdiction:'UG',owner_id:'COMPANY-1',company_id:'COMPANY-1',representative_id:owner})]);
    assert.equal((await q('select public.verification_ready($1) ready',[company])).rows[0].ready,false);
    await q("update public.verification_evidence set details=jsonb_set(details,'{company_ownership_verified}','true'::jsonb) where application_id=$1 and kind='company_authority'",[company]);
    assert.equal((await q('select public.verification_ready($1) ready',[company])).rows[0].ready,true);
    await q("update public.verification_evidence set details=jsonb_set(details,'{representative_id}',to_jsonb($2::text)) where application_id=$1 and kind='company_authority'",[company,other]);
    assert.equal((await q('select public.verification_ready($1) ready',[company])).rows[0].ready,false);
    // Provider checks include exact technician, trade, jurisdiction and live expiry.
    const provider=(await q("select public.submit_verification('provider','PLUMBING','UG',$1) id",[JSON.stringify({credential_number:'PL-1',issuer:'Trade authority',expires_on:'2028-01-01'})])).rows[0].id;
    await privileged();await q("insert into public.verification_requirements values('UG','provider','PLUMBING',true)");
    for(const kind of ['identity','liveness','credential'])await q("insert into public.verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at,details) values($1,$2,$3,true,'SIGNED-issuer-reference',now()+interval '1 year',$4)",[provider,owner,kind,JSON.stringify({service:'PLUMBING',jurisdiction:'UG',credential_number:'PL-1',issuer:'Trade authority',expires_on:'2028-01-01',technician_id:owner})]);
    await privileged();await q("update public.verification_evidence set details=jsonb_set(details,'{issuer}',to_jsonb('Wrong issuer'::text)) where application_id=$1 and kind='credential'",[provider]);
    assert.equal((await q('select public.verification_ready($1) ready',[provider])).rows[0].ready,false);
    await q("update public.verification_evidence set details=jsonb_set(details,'{issuer}',to_jsonb('Trade authority'::text)) where application_id=$1 and kind='credential'",[provider]);
    await user(reviewer,true);await q("select public.review_verification($1,'approve','Issuer matched technician')",[provider]);
    await user(owner);const providerId=(await q('select public.register_verified_provider($1,$2) id',[provider,JSON.stringify({name:'Qualified plumber',diagnostic_fee:500})])).rows[0].id;
    await privileged();await anonymous();assert.equal((await q('select id from public.service_providers')).rows.length,1);
    await privileged();
    const nearby=(await q("select * from public.nearby_qualified_providers(0,0,'PLUMBING')")).rows;
    assert.equal(nearby.length,1);assert.equal(nearby[0].diagnostic_fee,500);
    await assert.rejects(q('insert into public.marketplace_bookings(customer_id,provider_id,service_type) values($1,$2,$3)',[owner,providerId,'ELECTRICAL']),/wrong trade/);
    await assert.rejects(q('insert into public.marketplace_bookings(customer_id,provider_id,service_type,lat,lng) values($1,$2,$3,45,90)',[owner,providerId,'PLUMBING']),/outside service area/);
    await assert.rejects(q('insert into public.marketplace_bookings(customer_id,provider_id,service_type) values($1,$2,$3)',[owner,providerId,'PLUMBING']),/must be acknowledged/);
    await q('insert into public.marketplace_bookings(customer_id,provider_id,service_type,base_cost,diagnostic_fee_acknowledged) values($1,$2,$3,500,true)',[owner,providerId,'PLUMBING']);
    await q("update public.service_providers set status='BUSY' where id=$1",[providerId]);
    assert.equal((await q("select * from public.nearby_qualified_providers(0,0,'PLUMBING')")).rows.length,0);
    await assert.rejects(q('insert into public.marketplace_bookings(customer_id,provider_id,service_type) values($1,$2,$3)',[owner,providerId,'PLUMBING']),/unavailable/);
    await q("update public.service_providers set status='AVAILABLE' where id=$1",[providerId]);
    await q("update public.verification_evidence set expires_at=now()-interval '1 day' where application_id=$1 and kind='credential'",[provider]);
    await anonymous();assert.equal((await q('select id from public.service_providers')).rows.length,0);
    await privileged();await assert.rejects(q('insert into public.marketplace_bookings(customer_id,provider_id,service_type) values($1,$2,$3)',[owner,providerId,'Repair']),/not currently qualified/);
  }finally{await pg.close();}
});

