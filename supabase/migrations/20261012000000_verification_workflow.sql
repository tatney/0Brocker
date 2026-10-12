-- Deployment required. No browser/LLM can write independent evidence or approve itself.
create table public.verification_applications (
  id uuid primary key default gen_random_uuid(),
  applicant_id uuid not null references auth.users(id),
  kind text not null check(kind in ('provider','property')),
  subject text not null check(length(subject) between 1 and 160),
  jurisdiction text not null check(length(jurisdiction) between 1 and 160),
  unit_number text not null default '',
  relationship text not null default 'individual_owner',
  credential_number text not null default '',
  issuer text not null default '',
  expires_on date,
  status text not null default 'pending' check(status in ('pending','approved','rejected','needs_information','suspended')),
  record_id bigint,
  draft jsonb not null default '{}',
  ai_summary jsonb,
  review_reason text,
  created_at timestamptz not null default now()
);
create table public.verification_evidence (
  id uuid primary key default gen_random_uuid(),
  application_id uuid not null references public.verification_applications(id),
  subject_id uuid not null references auth.users(id),
  kind text not null check(kind in ('identity','liveness','credential','ownership','company_authority')),
  verified boolean not null default false,
  source_reference text not null check(length(source_reference)>0),
  expires_at timestamptz not null,
  details jsonb not null default '{}',
  created_at timestamptz not null default now()
);
-- Intentionally empty: activate a jurisdiction/trade only after authoritative adapters exist.
create table public.verification_requirements (
  jurisdiction text not null,
  kind text not null check(kind in ('provider','property')),
  subject text not null default '*',
  enabled boolean not null default false,
  primary key(jurisdiction,kind,subject)
);
create table public.verification_audit (
  id bigint generated always as identity primary key,
  actor_id uuid,
  application_id uuid not null references public.verification_applications(id),
  action text not null,
  detail jsonb not null default '{}',
  created_at timestamptz not null default now()
);
alter table public.verification_applications enable row level security;
alter table public.verification_evidence enable row level security;
alter table public.verification_requirements enable row level security;
alter table public.verification_audit enable row level security;
revoke all on public.verification_applications, public.verification_evidence, public.verification_requirements, public.verification_audit from anon, authenticated;
grant select on public.verification_applications, public.verification_evidence, public.verification_requirements, public.verification_audit to authenticated;
grant all on public.verification_applications, public.verification_evidence, public.verification_requirements, public.verification_audit to service_role;
grant usage, select on sequence public.verification_audit_id_seq to service_role;
create policy verification_app_read on public.verification_applications for select to authenticated using(applicant_id=auth.uid() or public.is_admin());
create policy verification_evidence_read on public.verification_evidence for select to authenticated using(exists(select 1 from public.verification_applications a where a.id=application_id and (a.applicant_id=auth.uid() or public.is_admin())));
create policy verification_requirements_read on public.verification_requirements for select to authenticated using(true);
create policy verification_audit_read on public.verification_audit for select to authenticated using(public.is_admin());

create or replace function public.verification_ready(p_id uuid)
returns boolean language plpgsql stable security definer set search_path=public,pg_temp as $$
declare a public.verification_applications; e public.verification_evidence;
begin
  select * into a from public.verification_applications where id=p_id;
  if not found or not exists(select 1 from public.verification_requirements r where r.enabled and r.jurisdiction=a.jurisdiction and r.kind=a.kind and r.subject=case when a.kind='provider' then a.subject else '*' end) then return false; end if;
  if not exists(select 1 from public.verification_evidence where application_id=a.id and subject_id=a.applicant_id and kind='identity' and verified and expires_at>now())
    or not exists(select 1 from public.verification_evidence where application_id=a.id and subject_id=a.applicant_id and kind='liveness' and verified and expires_at>now()) then return false; end if;
  if exists(select 1 from public.verification_evidence where application_id=a.id and subject_id=a.applicant_id and (not verified or coalesce(details->>'revoked','false')<>'false' or coalesce(details->>'suspended','false')<>'false' or coalesce(details->>'disputed','false')<>'false' or coalesce(details->>'transferred','false')<>'false')) then return false; end if;
  if a.kind='provider' then
    if a.expires_on is null or a.expires_on <= current_date then return false; end if;
    return exists(select 1 from public.verification_evidence where application_id=a.id and subject_id=a.applicant_id and kind='credential' and verified and expires_at>now()
      and details->>'issuer'=a.issuer and details->>'expires_on'=a.expires_on::text
      and coalesce(details->>'suspended','false')='false' and coalesce(details->>'revoked','false')='false'
      and details->>'service'=a.subject and details->>'jurisdiction'=a.jurisdiction and details->>'credential_number'=a.credential_number and details->>'technician_id'=a.applicant_id::text);
  end if;
  if a.relationship not in ('individual_owner','owning_company_representative') then return false; end if;
  select * into e from public.verification_evidence where application_id=a.id and subject_id=a.applicant_id and kind='ownership' and verified and expires_at>now()
    and details->>'property_id'=a.subject and details->>'unit'=a.unit_number and details->>'jurisdiction'=a.jurisdiction order by created_at desc,id desc limit 1;
  if not found or coalesce(e.details->>'disputed','false')<>'false' or coalesce(e.details->>'transferred','false')<>'false' then return false; end if;
  if a.relationship='individual_owner' then return coalesce(e.details->>'owner_id'=a.applicant_id::text,false); end if;
  return exists(select 1 from public.verification_evidence c where c.application_id=a.id and c.subject_id=a.applicant_id and c.kind='company_authority' and c.verified and c.expires_at>now() and c.details->>'company_id'=e.details->>'owner_id' and c.details->>'representative_id'=a.applicant_id::text and c.details->>'company_ownership_verified'='true');
end $$;
revoke all on function public.verification_ready(uuid) from public;
grant execute on function public.verification_ready(uuid) to anon,authenticated,service_role;

create or replace function public.submit_verification(p_kind text,p_subject text,p_jurisdiction text,p_details jsonb default '{}')
returns uuid language plpgsql security definer set search_path=public,pg_temp as $$
declare v_id uuid;
begin
  if auth.uid() is null then raise exception 'Sign in required'; end if;
  if p_kind is null or p_subject is null or p_jurisdiction is null or length(trim(p_subject))=0 or length(trim(p_jurisdiction))=0 or p_kind not in ('provider','property') or length(p_subject) not between 1 and 160 or length(p_jurisdiction) not between 1 and 160 or length(p_details::text)>20000 then raise exception 'Invalid application'; end if;
  if p_kind='property' and (trim(coalesce(p_details->>'unit',''))='' or coalesce(p_details->>'relationship','') not in ('individual_owner','owning_company_representative')) then raise exception 'Owner relationship and exact unit required; brokers excluded'; end if;
  if p_kind='provider' and (coalesce(p_details->>'credential_number','')='' or coalesce(p_details->>'issuer','')='' or coalesce(p_details->>'expires_on','')='') then raise exception 'Credential, issuer and expiry required'; end if;
  insert into public.verification_applications(applicant_id,kind,subject,jurisdiction,unit_number,relationship,credential_number,issuer,expires_on,draft)
    values(auth.uid(),p_kind,upper(trim(p_subject)),upper(trim(p_jurisdiction)),upper(trim(coalesce(p_details->>'unit',''))),coalesce(p_details->>'relationship','individual_owner'),coalesce(p_details->>'credential_number',''),coalesce(p_details->>'issuer',''),nullif(p_details->>'expires_on','')::date,p_details)
    returning id into v_id;
  insert into public.verification_audit(actor_id,application_id,action) values(auth.uid(),v_id,'submitted');
  return v_id;
end $$;

create or replace function public.review_verification(p_id uuid,p_decision text,p_reason text)
returns public.verification_applications language plpgsql security definer set search_path=public,pg_temp as $$
declare a public.verification_applications;
begin
  if not public.is_admin() then raise exception 'Reviewer access required'; end if;
  if p_decision is null or p_reason is null or p_decision not in ('approve','reject','request_more','suspend') or length(trim(p_reason)) not between 1 and 1000 then raise exception 'Valid decision and reason required'; end if;
  select * into a from public.verification_applications where id=p_id for update;
  if not found then raise exception 'Application not found'; end if;
  if p_decision='approve' and (a.status in ('rejected','suspended') or not public.verification_ready(p_id)) then raise exception 'Approval blocked: independent evidence or jurisdiction configuration is missing, expired or mismatched'; end if;
  update public.verification_applications set status=case p_decision when 'approve' then 'approved' when 'reject' then 'rejected' when 'request_more' then 'needs_information' else 'suspended' end,review_reason=p_reason where id=p_id returning * into a;
  insert into public.verification_audit(actor_id,application_id,action,detail) values(auth.uid(),p_id,p_decision,jsonb_build_object('reason',p_reason));
  return a;
end $$;

create or replace function public.save_verification_extraction(p_id uuid,p_summary jsonb)
returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
  if auth.uid() is null or not exists(select 1 from public.verification_applications where id=p_id and (applicant_id=auth.uid() or public.is_admin())) then raise exception 'Application access denied'; end if;
  if length(p_summary::text)>20000 then raise exception 'Extraction too large'; end if;
  update public.verification_applications set ai_summary=p_summary where id=p_id;
  insert into public.verification_audit(actor_id,application_id,action) values(auth.uid(),p_id,'advisory_extraction');
end $$;
revoke all on function public.submit_verification(text,text,text,jsonb),public.review_verification(uuid,text,text),public.save_verification_extraction(uuid,jsonb) from public;
grant execute on function public.submit_verification(text,text,text,jsonb),public.review_verification(uuid,text,text),public.save_verification_extraction(uuid,jsonb) to authenticated;

alter table public.properties alter column is_verified set default false;
alter table public.properties add column verification_application_id uuid unique references public.verification_applications(id);
alter table public.service_providers add column diagnostic_fee bigint check(diagnostic_fee>=0);
alter table public.marketplace_bookings add column diagnostic_fee_acknowledged boolean not null default false;
alter table public.service_providers add column verification_application_id uuid unique references public.verification_applications(id);
-- Old boolean flags are insufficient. Public discovery requires approved, current evidence.
create or replace function public.verification_active(p_id uuid,p_kind text,p_record bigint,p_service text default null)
returns boolean language sql stable security definer set search_path=public,pg_temp as $$
  select exists(select 1 from public.verification_applications a where a.id=p_id and a.status='approved' and a.kind=p_kind and a.record_id=p_record and (p_service is null or a.subject=p_service) and public.verification_ready(a.id));
$$;
revoke all on function public.verification_active(uuid,text,bigint,text) from public;
grant execute on function public.verification_active(uuid,text,bigint,text) to anon,authenticated,service_role;
drop policy "properties are public readable" on public.properties;
drop policy "providers are public readable" on public.service_providers;
create policy approved_property_read on public.properties for select using(public.verification_active(verification_application_id,'property',id));
create policy approved_provider_read on public.service_providers for select using(public.verification_active(verification_application_id,'provider',id,category));

create or replace function public.guard_catalog_verification()
returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
declare a public.verification_applications;
begin
  if tg_op='UPDATE' and old.verification_application_id is not null and (new.verification_application_id is distinct from old.verification_application_id) then raise exception 'Approved application binding is immutable'; end if;
  if new.is_verified or new.verification_application_id is not null then
    select * into a from public.verification_applications where id=new.verification_application_id;
    if not found or a.status<>'approved' or not public.verification_ready(a.id) 
      or (a.record_id is not null and a.record_id is distinct from new.id)
      or a.kind<>(case when tg_table_name='properties' then 'property' else 'provider' end) then raise exception 'Independent approval and exact record binding required'; end if;
    if tg_table_name='service_providers' then
      if new.category<>a.subject then raise exception 'Service category does not match approved credential'; end if;
    end if;
  end if;
  return new;
end $$;
create trigger guard_property_verification before insert or update on public.properties for each row execute function public.guard_catalog_verification();
create trigger guard_provider_verification before insert or update on public.service_providers for each row execute function public.guard_catalog_verification();

create table public.verified_property_claims (
  jurisdiction text not null, property_id text not null, unit_number text not null,
  application_id uuid not null unique references public.verification_applications(id),
  primary key(jurisdiction,property_id,unit_number)
);
alter table public.verified_property_claims enable row level security;
revoke all on public.verified_property_claims from anon,authenticated;
create or replace function public.publish_verified_property(p_id uuid)
returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare a public.verification_applications; v_id bigint;
begin
  select * into a from public.verification_applications where id=p_id for update;
  if not found or a.applicant_id is distinct from auth.uid() or a.kind<>'property' or a.status<>'approved' or not public.verification_ready(a.id) then raise exception 'Only the independently verified owner may publish'; end if;
  if a.record_id is not null then raise exception 'Already published'; end if;
  insert into public.verified_property_claims values(a.jurisdiction,a.subject,a.unit_number,a.id);
  insert into public.properties(title,location,city,listing_type,price_ugx,is_verified,verification_application_id)
    values(a.draft->>'title',a.draft->>'location',a.draft->>'city',coalesce(a.draft->>'listing_type','RENT'),coalesce((a.draft->>'price_ugx')::bigint,0),false,a.id) returning id into v_id;
  update public.verification_applications set record_id=v_id where id=a.id;
  update public.properties set is_verified=true where id=v_id;
  insert into public.verification_audit(actor_id,application_id,action) values(auth.uid(),a.id,'published');
  return v_id;
end $$;
revoke all on function public.publish_verified_property(uuid) from public;
grant execute on function public.publish_verified_property(uuid) to authenticated;

create or replace function public.register_verified_provider(p_id uuid,p_profile jsonb)
returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare a public.verification_applications; v_id bigint;
begin
  select * into a from public.verification_applications where id=p_id for update;
  if not found or a.applicant_id is distinct from auth.uid() or a.kind<>'provider' or a.status<>'approved' or not public.verification_ready(a.id) then raise exception 'Verified technician approval required'; end if;
  if a.record_id is not null then raise exception 'Already registered'; end if;
  if p_profile->>'diagnostic_fee' is null then raise exception 'Explicit diagnostic fee required; repair labour and materials excluded'; end if;
  insert into public.service_providers(name,category,headline,is_verified,verification_application_id,diagnostic_fee,price_from,lat,lng)
    values(p_profile->>'name',a.subject,coalesce(p_profile->>'headline',''),false,a.id,(p_profile->>'diagnostic_fee')::bigint,(p_profile->>'diagnostic_fee')::bigint,coalesce((p_profile->>'lat')::real,0),coalesce((p_profile->>'lng')::real,0)) returning id into v_id;
  update public.verification_applications set record_id=v_id where id=a.id;
  update public.service_providers set is_verified=true where id=v_id;
  insert into public.verification_audit(actor_id,application_id,action) values(auth.uid(),a.id,'provider_registered');
  return v_id;
end $$;
revoke all on function public.register_verified_provider(uuid,jsonb) from public;
grant execute on function public.register_verified_provider(uuid,jsonb) to authenticated;


create or replace function public.verification_distance_km(a double precision,b double precision,c double precision,d double precision)
returns double precision language sql immutable set search_path=public,pg_temp as $$
 select 6371*2*asin(sqrt(least(1.0,power(sin(radians(c-a)/2),2)+cos(radians(a))*cos(radians(c))*power(sin(radians(d-b)/2),2))));
$$;

create or replace function public.guard_verified_booking()
returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
begin
  if new.provider_id is null and new.status not in ('SEARCHING','CANCELLED') then raise exception 'A qualified dispatched technician is required'; end if;
  if new.provider_id is not null then
    if not exists(select 1 from public.service_providers p where p.id=new.provider_id and public.verification_active(p.verification_application_id,'provider',p.id,p.category)) then raise exception 'Provider is not currently qualified'; end if;
    if tg_op='INSERT' or new.provider_id is distinct from old.provider_id or new.service_type is distinct from old.service_type or new.lat is distinct from old.lat or new.lng is distinct from old.lng then
      if not exists(select 1 from public.service_providers p where p.id=new.provider_id and p.status='AVAILABLE' and upper(replace(p.category,' ','_'))=upper(replace(new.service_type,' ','_')) and public.verification_distance_km(new.lat,new.lng,p.lat,p.lng)<=p.coverage_radius_km) then raise exception 'Technician unavailable, wrong trade or outside service area'; end if;
      if not new.diagnostic_fee_acknowledged or not exists(select 1 from public.service_providers p where p.id=new.provider_id and p.diagnostic_fee is not null and new.base_cost=p.diagnostic_fee) then raise exception 'Diagnostic fee and repair exclusions must be acknowledged'; end if;
    end if;
  end if;
  return new;
end $$;
create trigger guard_verified_assignment before insert or update on public.marketplace_bookings for each row execute function public.guard_verified_booking();

create or replace function public.prevent_verification_audit_change()
returns trigger language plpgsql as $$ begin raise exception 'Verification audit is append-only'; end $$;
create trigger verification_audit_immutable before update or delete on public.verification_audit for each row execute function public.prevent_verification_audit_change();

-- Concurrent applications cannot reserve the same official title/unit twice.
create unique index verification_property_submission_unique on public.verification_applications(jurisdiction,subject,unit_number)
 where kind='property' and status in ('pending','needs_information','approved','suspended');

-- New authoritative revocation overrides every earlier successful document check.
create or replace function public.verification_invalidate_evidence()
returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if not new.verified or new.expires_at<=now() or coalesce(new.details->>'disputed','false')<>'false'
   or coalesce(new.details->>'transferred','false')<>'false' or coalesce(new.details->>'suspended','false')<>'false'
   or coalesce(new.details->>'revoked','false')<>'false' then
   update public.verification_applications set status='suspended',review_reason='Authoritative evidence failed, expired, disputed, transferred or revoked' where id=new.application_id and status='approved';
 end if;
 insert into public.verification_audit(application_id,action,detail) values(new.application_id,'authoritative_evidence',jsonb_build_object('evidence_id',new.id,'kind',new.kind,'verified',new.verified,'source',new.source_reference));
 return new;
end $$;
create trigger evidence_invalidation after insert or update on public.verification_evidence for each row execute function public.verification_invalidate_evidence();

-- Public nearby search never ranks unavailable or unqualified technicians.
create or replace function public.nearby_qualified_providers(p_lat double precision,p_lng double precision,p_service text)
returns table(id bigint,name text,rating real,reviews_count integer,approx_lat double precision,approx_lng double precision,distance_km double precision,arrival_minutes integer,diagnostic_fee bigint)
language plpgsql stable security definer set search_path=public,pg_temp as $$
begin
 if p_lat is null or p_lng is null or p_lat not between -90 and 90 or p_lng not between -180 and 180 then raise exception 'Valid user location required'; end if;
 return query select p.id,p.name,p.rating,p.reviews_count,round(p.lat::numeric,2)::double precision,round(p.lng::numeric,2)::double precision,
 public.verification_distance_km(p_lat,p_lng,p.lat,p.lng),greatest(5,ceil(public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)*2)::integer),p.diagnostic_fee
 from public.service_providers p where p.status='AVAILABLE' and p.category=p_service and p.diagnostic_fee is not null
 and public.verification_active(p.verification_application_id,'provider',p.id,p.category)
 and public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)<=p.coverage_radius_km
 order by (p.rating*p.reviews_count+3.5*10)/(p.reviews_count+10) desc, greatest(5,ceil(public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)*2)::integer),p.id;
end $$;
revoke all on function public.nearby_qualified_providers(double precision,double precision,text) from public;
grant execute on function public.nearby_qualified_providers(double precision,double precision,text) to anon,authenticated;

-- Close legacy catalogue/booking bypasses: these records have no technician evidence binding.
drop policy "professionals are public readable" on public.service_professionals;
create or replace function public.guard_legacy_service_booking() returns trigger language plpgsql as $$
begin raise exception 'Use the independently qualified technician marketplace'; end $$;
create trigger qualified_legacy_service_booking before insert or update on public.service_bookings for each row execute function public.guard_legacy_service_booking();

-- Offers cannot establish qualification or substitute a different technician.
create or replace function public.guard_qualified_quote() returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if not exists(select 1 from public.service_providers p join public.marketplace_bookings b on b.id=new.booking_id where p.id=new.provider_id and b.provider_id=p.id and public.verification_active(p.verification_application_id,'provider',p.id,p.category)) then raise exception 'Quote requires the qualified dispatched technician'; end if;
 return new;
end $$;
create trigger qualified_quote before insert or update on public.quotes for each row execute function public.guard_qualified_quote();
