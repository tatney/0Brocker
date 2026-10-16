import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {PGlite} from '@electric-sql/pglite';

test('native cloud integration preserves roles, private photos, idempotency and owner/customer inbox',async()=>{
 const pg=new PGlite(); const customer='11111111-1111-4111-8111-111111111111',tech='22222222-2222-4222-8222-222222222222',owner='33333333-3333-4333-8333-333333333333',stranger='44444444-4444-4444-8444-444444444444';
 const q=(s,a=[])=>pg.query(s,a);
 const user=async(id,admin=false)=>{await pg.exec('reset role;set role authenticated');await q("select set_config('request.jwt.claim.sub',$1,false),set_config('request.jwt.claims',$2,false)",[id,JSON.stringify({app_metadata:{role:admin?'admin':'user'}})]);};
 try {
  await pg.exec(`create role anon;create role authenticated;create role service_role bypassrls;create schema auth;create table auth.users(id uuid primary key,email text);create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;create function auth.jwt() returns jsonb language sql stable as $$select coalesce(nullif(current_setting('request.jwt.claims',true),''),'{}')::jsonb$$;grant usage on schema auth to anon,authenticated,service_role;
  create schema storage;create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);create table storage.objects(id uuid default gen_random_uuid(),bucket_id text,name text,primary key(bucket_id,name));alter table storage.objects enable row level security;grant usage on schema storage to authenticated;grant select,insert on storage.objects to authenticated;`);
  for(const f of ['20260101000000_initial_schema.sql','20261012000000_verification_workflow.sql','20261013000000_marketplace_workflows.sql','20261014000000_native_marketplace.sql','20261014001000_service_photo_storage.sql','20261015000000_booking_realtime.sql']){
   await pg.exec((await readFile(new URL(`../../supabase/migrations/${f}`,import.meta.url),'utf8')).replace('create extension if not exists "pgcrypto";',''));
   if(f.startsWith('202601')) await pg.exec('grant select,insert,update,delete on all tables in schema public to anon,authenticated;');
  }
  await pg.exec(await readFile(new URL('../../supabase/migrations/20261015000000_booking_realtime.sql',import.meta.url),'utf8'));
  assert.equal((await q("select count(*)::int n from pg_publication_tables where pubname='supabase_realtime'")).rows[0].n,6);
  for(const [i,id] of [customer,tech,owner,stranger].entries())await q('insert into auth.users values($1,$2)',[id,`role${i}@example.test`]);
  await user(customer);const profile=(await q('select ensure_my_profile() p')).rows[0].p;assert.ok(profile.id);await q("select update_my_profile('Customer','TEST')");assert.equal((await q('select ensure_my_profile() p')).rows[0].p.full_name,'Customer');
  await user(tech);await q('select ensure_my_profile()');const app=(await q("select submit_verification('provider','PLUMBING','UG',$1) id",[JSON.stringify({credential_number:'PL-1',issuer:'Authority',expires_on:'2028-01-01'})])).rows[0].id;
  await pg.exec('reset role');await q("insert into verification_requirements values('UG','provider','PLUMBING',true)");
  for(const kind of ['identity','liveness','credential']) await q("insert into verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at,details) values($1,$2,$3,true,'TEST-ONLY',now()+interval '1 year',$4)",[app,tech,kind,JSON.stringify({service:'PLUMBING',jurisdiction:'UG',credential_number:'PL-1',issuer:'Authority',expires_on:'2028-01-01',technician_id:tech})]);
  await user(stranger,true);await q("select review_verification($1,'approve','Synthetic evidence in isolated test database')",[app]);await user(tech);const provider=(await q('select register_verified_provider($1,$2) id',[app,JSON.stringify({name:'Test technician',diagnostic_fee:500,lat:0,lng:0})])).rows[0].id;
  await user(customer);const providers=(await q('select provider_catalog() p')).rows;assert.equal(providers.length,1);assert.equal(providers[0].p.technician_identity,tech);
  const key='aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa';const args=[provider,'PLUMBING',0,0,'Leaking tap with photos',500,true,'Test address','Repair',null,key];
  const request=()=>q('select request_service_v2($1,$2,$3::real,$4::real,$5,$6,$7,$8,$9,$10,$11) id',args);
  const booking=(await request()).rows[0].id;assert.equal((await request()).rows[0].id,booking);assert.equal((await q('select count(*) n from marketplace_bookings')).rows[0].n,1);
  const photoKey='bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb';const photo=(await q("select prepare_booking_photo($1,'image/jpeg',$2) path",[booking,photoKey])).rows[0].path;assert.equal((await q("select prepare_booking_photo($1,'image/jpeg',$2) path",[booking,photoKey])).rows[0].path,photo);
  await assert.rejects(q('select confirm_booking_photo($1)',[photo]),/Upload/);await q("insert into storage.objects(bucket_id,name) values('service-photos',$1)",[photo]);await q('select confirm_booking_photo($1)',[photo]);
  await user(stranger);assert.equal((await q('select * from booking_photos')).rows.length,0);assert.equal((await q('select * from storage.objects')).rows.length,0);await assert.rejects(q("insert into storage.objects(bucket_id,name) values('service-photos','fake')"),/row-level/);await assert.rejects(q('select set_provider_availability($1,false,0::real,0::real)',[provider]),/ownership/);
  await user(tech);assert.equal((await q('select * from storage.objects')).rows.length,1);await q("select marketplace_action($1,'accept','')",[booking]);await assert.rejects(q("select marketplace_action($1,'arrival','')",[booking]),/Customer/);
  await user(customer);await q("select marketplace_action($1,'arrival','')",[booking]);await assert.rejects(q("select prepare_booking_photo($1,'image/jpeg',gen_random_uuid())",[booking]),/before arrival/);
  await user(owner);await q('select ensure_my_profile()');const propertyApp=(await q("select submit_verification('property','TITLE-1','UG',$1) id",[JSON.stringify({unit:'1',relationship:'individual_owner',title:'Test house',location:'Test address',city:'Kampala',listing_type:'RENT',price_ugx:1000})])).rows[0].id;
  await pg.exec('reset role');await q("insert into verification_requirements values('UG','property','*',true)");for(const kind of ['identity','liveness','ownership']) await q("insert into verification_evidence(application_id,subject_id,kind,verified,source_reference,expires_at,details) values($1,$2,$3,true,'TEST-ONLY',now()+interval '1 year',$4)",[propertyApp,owner,kind,JSON.stringify({property_id:'TITLE-1',unit:'1',jurisdiction:'UG',owner_id:owner})]);
  await user(stranger,true);await q("select review_verification($1,'approve','Isolated ownership fixture')",[propertyApp]);await user(owner);const property=(await q('select publish_verified_property($1) id',[propertyApp])).rows[0].id;
  await user(customer);const chat=(await q('select open_property_conversation($1) id',[property])).rows[0].id;assert.equal((await q('select open_property_conversation($1) id',[property])).rows[0].id,chat);await q("select send_conversation_message($1,'Can we view tomorrow?')",[chat]);
  await user(owner);assert.equal((await q('select * from messages')).rows.length,1);await q("select send_conversation_message($1,'Yes, at 10 AM')",[chat]);assert.equal((await q('select sender_id from messages order by id desc limit 1')).rows[0].sender_id,owner);await q("select edit_my_property($1,'Updated house',2000,'Updated address')",[property]);
  await user(stranger);assert.equal((await q('select * from messages')).rows.length,0);await assert.rejects(q("select send_conversation_message($1,'Unauthorized')",[chat]),/Participant/);await assert.rejects(q("select edit_my_property($1,'Fake',500,'Fake address')",[property]),/owner/);
  await user(owner);await q('select withdraw_my_property($1)',[property]);await user(customer);assert.equal((await q('select property_catalog()')).rows.length,0);assert.equal((await q('select * from messages')).rows.length,2);
 } finally { await pg.close(); }
});
