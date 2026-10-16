-- Review before deployment. No payment adapter or native cloud sync is implied.
create table public.marketplace_policy (
 id boolean primary key default true check(id), free_launch boolean not null default true,
 provider_fee_ugx bigint not null default 2500 check(provider_fee_ugx between 2000 and 3000),
 fee_rule text not null default 'ATTENDED_REQUEST' check(fee_rule='ATTENDED_REQUEST'),
 paid_plans_enabled boolean not null default false check(not paid_plans_enabled)
);
insert into public.marketplace_policy(id) values(true);
create table public.provider_fee_accounts(provider_id bigint primary key references public.service_providers(id),balance_ugx bigint not null default 0 check(balance_ugx>=0));
create table public.booking_workflow (
 booking_id bigint primary key references public.marketplace_bookings(id),
 accepted_at timestamptz, arrival_confirmed_at timestamptz, completion_confirmed_at timestamptz,
 fee_ugx bigint not null default 0, fee_state text not null default 'NONE' check(fee_state in('NONE','WAIVED','RESERVED','SETTLED','RELEASED')),
 accepted_quote_id bigint references public.quotes(id), cancellation_reason text not null default ''
);
create table public.booking_events(id bigint generated always as identity primary key,booking_id bigint not null references public.marketplace_bookings(id),actor_id uuid,action text not null,reason text not null default '',created_at timestamptz not null default now());
create table public.provider_fee_ledger(id bigint generated always as identity primary key,booking_id bigint not null references public.marketplace_bookings(id),provider_id bigint not null references public.service_providers(id),action text not null check(action in('reserve','settle','release','refund')),amount_ugx bigint not null check(amount_ugx>=0),created_at timestamptz not null default now(),unique(booking_id,action));
create table public.marketplace_preferences(user_id uuid primary key references auth.users(id),workspace text not null default 'CUSTOMER' check(workspace in('CUSTOMER','PROVIDER','OWNER')),owner_contact_consent boolean not null default false);
create table public.marketplace_plan_interest(user_id uuid references auth.users(id),plan text check(plan in('PROVIDER_PRO','OWNER_PORTFOLIO','SEARCH_PASS')),created_at timestamptz default now(),primary key(user_id,plan));
create table public.rental_management_enquiries(id uuid primary key default gen_random_uuid(),applicant_id uuid not null references auth.users(id),title_identifier text not null check(length(title_identifier) between 1 and 160),unit_number text not null check(length(unit_number) between 1 and 160),scope text not null check(length(scope) between 1 and 1000),note text not null default '' check(length(note)<=1000),status text not null default 'SUBMITTED',created_at timestamptz not null default now());
-- Preference and interest are never an entitlement, qualification or consent to charge.
alter table public.marketplace_policy enable row level security;
alter table public.provider_fee_accounts enable row level security;
alter table public.booking_workflow enable row level security;
alter table public.booking_events enable row level security;
alter table public.provider_fee_ledger enable row level security;
alter table public.marketplace_preferences enable row level security;
alter table public.marketplace_plan_interest enable row level security;
alter table public.rental_management_enquiries enable row level security;
create or replace function public.marketplace_participant(p_booking bigint) returns boolean language sql stable security definer set search_path=public,pg_temp as $$
 select auth.uid() is not null and exists(select 1 from marketplace_bookings b left join service_providers p on p.id=b.provider_id left join verification_applications a on a.id=p.verification_application_id where b.id=p_booking and (b.customer_id=auth.uid() or a.applicant_id=auth.uid() or public.is_admin()));
$$;
create policy policy_read on marketplace_policy for select using(true);
create policy workflow_read on booking_workflow for select to authenticated using(public.marketplace_participant(booking_id));
create policy events_read on booking_events for select to authenticated using(public.marketplace_participant(booking_id));
create policy ledger_read on provider_fee_ledger for select to authenticated using(public.marketplace_participant(booking_id));
create policy fee_account_read on provider_fee_accounts for select to authenticated using(public.is_admin() or exists(select 1 from service_providers p join verification_applications a on a.id=p.verification_application_id where p.id=provider_id and a.applicant_id=auth.uid()));
create policy prefs_own on marketplace_preferences for all to authenticated using(user_id=auth.uid()) with check(user_id=auth.uid());
create policy interest_own_read on marketplace_plan_interest for select to authenticated using(user_id=auth.uid() or public.is_admin());
create policy interest_own_insert on marketplace_plan_interest for insert to authenticated with check(user_id=auth.uid());
create policy enquiry_read on rental_management_enquiries for select to authenticated using(applicant_id=auth.uid() or public.is_admin());
create policy enquiry_insert on rental_management_enquiries for insert to authenticated with check(applicant_id=auth.uid() and status='SUBMITTED');
revoke all on marketplace_policy,provider_fee_accounts,booking_workflow,booking_events,provider_fee_ledger,marketplace_preferences,marketplace_plan_interest,rental_management_enquiries from anon,authenticated;
grant select on marketplace_policy to anon,authenticated;
grant select on provider_fee_accounts,booking_workflow,booking_events,provider_fee_ledger to authenticated;
grant select,insert,update on marketplace_preferences to authenticated;
grant select,insert on marketplace_plan_interest to authenticated;
grant select on rental_management_enquiries to authenticated;
grant insert(applicant_id,title_identifier,unit_number,scope,note) on rental_management_enquiries to authenticated;
-- Only trusted payment reconciliation can fund balances; no public top-up function.
grant all on marketplace_policy,provider_fee_accounts,booking_workflow,booking_events,provider_fee_ledger to service_role;
create trigger booking_events_immutable before update or delete on booking_events for each row execute function public.prevent_verification_audit_change();
create trigger fee_ledger_immutable before update or delete on provider_fee_ledger for each row execute function public.prevent_verification_audit_change();
-- Old permissive writes cannot forge arrival, completion, fee state or quote acceptance.
revoke insert,update,delete on public.marketplace_bookings,public.quotes,public.reviews from anon,authenticated;
create policy bookings_technician_read on marketplace_bookings for select to authenticated using(public.marketplace_participant(id));
create policy quotes_technician_read on quotes for select to authenticated using(public.marketplace_participant(booking_id));
create or replace function public.provider_fee_eligible(p_provider bigint) returns boolean language sql stable security definer set search_path=public,pg_temp as $$
 select free_launch or coalesce((select balance_ugx from provider_fee_accounts where provider_id=p_provider),0)-coalesce((select sum(w.fee_ugx) from booking_workflow w join marketplace_bookings b on b.id=w.booking_id where b.provider_id=p_provider and w.fee_state='RESERVED'),0)>=provider_fee_ugx from marketplace_policy where id;
$$;
create or replace function public.request_qualified_service(p_provider bigint,p_service text,p_lat real,p_lng real,p_description text,p_fee bigint,p_ack boolean) returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare n bigint;begin
 if auth.uid() is null or p_ack is distinct from true or p_lat is null or p_lng is null or p_description is null or length(p_description)>2000 or p_lat not between -90 and 90 or p_lng not between -180 and 180 then raise exception 'Authenticated customer, valid location and fee acknowledgement required'; end if;
 if exists(select 1 from service_providers p join verification_applications a on a.id=p.verification_application_id where p.id=p_provider and a.applicant_id=auth.uid()) then raise exception 'Self-booking cannot generate marketplace jobs or reviews'; end if;
 if not public.provider_fee_eligible(p_provider) then raise exception 'No financially eligible match'; end if;
 insert into marketplace_bookings(customer_id,provider_id,service_type,lat,lng,description,base_cost,total_cost,platform_fee,diagnostic_fee_acknowledged,status) values(auth.uid(),p_provider,p_service,p_lat,p_lng,p_description,p_fee,p_fee,0,true,'SEARCHING') returning id into n;
 insert into booking_workflow(booking_id) values(n); insert into booking_events(booking_id,actor_id,action) values(n,auth.uid(),'requested');return n;
end $$;
create or replace function public.marketplace_action(p_booking bigint,p_action text,p_reason text default '') returns void language plpgsql security definer set search_path=public,pg_temp as $$
declare b marketplace_bookings; w booking_workflow; c marketplace_policy; technician uuid; available bigint; actor_customer boolean;
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 select * into b from marketplace_bookings where id=p_booking for update;
 if not found then raise exception 'Booking unavailable'; end if;
 select a.applicant_id into technician from service_providers p join verification_applications a on a.id=p.verification_application_id where p.id=b.provider_id;
 actor_customer:=b.customer_id=auth.uid();
 if not coalesce(actor_customer,false) and technician is distinct from auth.uid() and not public.is_admin() then raise exception 'Participant access denied'; end if;
 select * into w from booking_workflow where booking_id=p_booking for update;
 if not found then raise exception 'Legacy booking has no independent workflow evidence'; end if;
 select * into c from marketplace_policy where id;
 if p_action='accept' then
  if technician is distinct from auth.uid() or b.status<>'SEARCHING' or w.accepted_at is not null then raise exception 'Assigned technician acceptance required'; end if;
  if not exists(select 1 from service_providers p where p.id=b.provider_id and p.status='AVAILABLE' and p.category=b.service_type and public.verification_distance_km(b.lat,b.lng,p.lat,p.lng)<=p.coverage_radius_km and public.verification_active(p.verification_application_id,'provider',p.id,p.category)) then raise exception 'Technician no longer eligible for this request'; end if;
  insert into provider_fee_accounts(provider_id) values(b.provider_id) on conflict do nothing;
  perform 1 from provider_fee_accounts where provider_id=b.provider_id for update;
  if not public.provider_fee_eligible(b.provider_id) then raise exception 'Insufficient unreserved fee balance'; end if;
  update booking_workflow set accepted_at=now(),fee_ugx=case when c.free_launch then 0 else c.provider_fee_ugx end,fee_state=case when c.free_launch then 'WAIVED' else 'RESERVED' end where booking_id=p_booking;
  if not c.free_launch then insert into provider_fee_ledger(booking_id,provider_id,action,amount_ugx) values(p_booking,b.provider_id,'reserve',c.provider_fee_ugx); end if;
  update marketplace_bookings set status='ASSIGNED' where id=p_booking;
 elsif p_action='arrival' then
  if not coalesce(actor_customer,false) or b.status<>'ASSIGNED' or w.arrival_confirmed_at is not null then raise exception 'Customer-confirmed arrival required'; end if;
  if w.fee_state='RESERVED' then
   update provider_fee_accounts set balance_ugx=balance_ugx-w.fee_ugx where provider_id=b.provider_id;
   insert into provider_fee_ledger(booking_id,provider_id,action,amount_ugx) values(p_booking,b.provider_id,'settle',w.fee_ugx);
  end if;
  update booking_workflow set arrival_confirmed_at=now(),fee_state=case when fee_state='RESERVED' then 'SETTLED' else fee_state end where booking_id=p_booking;
  update marketplace_bookings set status='ARRIVED',started_at=now() where id=p_booking;
 elsif p_action='request_completion' then
  if technician is distinct from auth.uid() or b.status<>'IN_PROGRESS' or w.accepted_quote_id is null then raise exception 'Technician and accepted quote required'; end if;
  update marketplace_bookings set status='AWAITING_CONFIRMATION' where id=p_booking;
 elsif p_action in('confirm_repair','confirm_diagnostic') then
  if not coalesce(actor_customer,false) or w.arrival_confirmed_at is null or w.completion_confirmed_at is not null or b.status='DISPUTED' then raise exception 'Customer confirmation required'; end if;
  if p_action='confirm_repair' and w.accepted_quote_id is null then raise exception 'Accepted repair quote required'; end if;
  update booking_workflow set completion_confirmed_at=now() where booking_id=p_booking;
  update marketplace_bookings set status='COMPLETED',completed_at=now() where id=p_booking;
 elsif p_action='cancel' then
  if not coalesce(actor_customer,false) and technician is distinct from auth.uid() then raise exception 'Participant cancellation required'; end if;
  if w.completion_confirmed_at is not null or b.status in('CANCELLED','DISPUTED') then raise exception 'Completed or disputed record cannot be cancelled'; end if;
  if p_reason not in('QUOTE_DECLINED','CUSTOMER_NO_SHOW','PROVIDER_NO_SHOW','WRONG_SERVICE','DUPLICATE_REQUEST','CUSTOMER_CANCELLED','PROVIDER_UNABLE','INVALID_ADDRESS') then raise exception 'Cancellation reason required'; end if;
  if w.arrival_confirmed_at is null and w.fee_state='RESERVED' then
   insert into provider_fee_ledger(booking_id,provider_id,action,amount_ugx) values(p_booking,b.provider_id,'release',w.fee_ugx);
   update booking_workflow set fee_state='RELEASED' where booking_id=p_booking;
  end if;
  update booking_workflow set cancellation_reason=p_reason where booking_id=p_booking;
  update marketplace_bookings set status=case when w.arrival_confirmed_at is null then 'CANCELLED' else 'REVIEW_REQUIRED' end where id=p_booking;
 elsif p_action='dispute' then
  if not coalesce(actor_customer,false) and technician is distinct from auth.uid() then raise exception 'Participant dispute required'; end if;
  if length(trim(p_reason)) not between 5 and 1000 then raise exception 'Explain dispute'; end if;
  update marketplace_bookings set status='DISPUTED' where id=p_booking;
 elsif p_action in('release_fee','uphold_fee') then
  if not public.is_admin() or b.status not in('DISPUTED','REVIEW_REQUIRED') or length(trim(p_reason)) not between 5 and 1000 then raise exception 'Reasoned reviewer decision required'; end if;
  if p_action='uphold_fee' and w.arrival_confirmed_at is null then raise exception 'Cannot uphold an attended fee without customer-confirmed arrival'; end if;
  if p_action='release_fee' and w.fee_state in('RESERVED','SETTLED') then
   perform 1 from provider_fee_accounts where provider_id=b.provider_id for update;
   if w.fee_state='SETTLED' then update provider_fee_accounts set balance_ugx=balance_ugx+w.fee_ugx where provider_id=b.provider_id; end if;
   insert into provider_fee_ledger(booking_id,provider_id,action,amount_ugx) values(p_booking,b.provider_id,case when w.fee_state='SETTLED' then 'refund' else 'release' end,w.fee_ugx);
   update booking_workflow set fee_state='RELEASED' where booking_id=p_booking;
  end if;
  update marketplace_bookings set status=case when w.completion_confirmed_at is not null then 'COMPLETED' else 'CANCELLED' end where id=p_booking;
 else raise exception 'Unknown action'; end if;
 insert into booking_events(booking_id,actor_id,action,reason) values(p_booking,auth.uid(),p_action,p_reason);
end $$;
create or replace function public.offer_repair_quote(p_booking bigint,p_labour bigint,p_materials bigint) returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare b marketplace_bookings; n bigint;begin
 select * into b from marketplace_bookings where id=p_booking for update;
 if p_labour not between 0 and 1000000000 or p_materials not between 0 and 1000000000 or b.status not in('ARRIVED','QUOTED') or not exists(select 1 from service_providers p join verification_applications a on a.id=p.verification_application_id where p.id=b.provider_id and a.applicant_id=auth.uid()) then raise exception 'Assigned technician, arrival and valid itemised costs required'; end if;
 if not exists(select 1 from booking_workflow where booking_id=p_booking and arrival_confirmed_at is not null and accepted_quote_id is null) then raise exception 'Arrival evidence required'; end if;
 update quotes set status='SUPERSEDED' where booking_id=p_booking and status='PENDING';
 insert into quotes(booking_id,provider_id,labour_cost,materials_cost,service_fee,total) values(p_booking,b.provider_id,p_labour,p_materials,0,p_labour+p_materials) returning id into n;
 update marketplace_bookings set status='QUOTED' where id=p_booking;
 insert into booking_events(booking_id,actor_id,action) values(p_booking,auth.uid(),'quote_offered');return n;
end $$;
create or replace function public.respond_repair_quote(p_quote bigint,p_accept boolean) returns void language plpgsql security definer set search_path=public,pg_temp as $$
declare o quotes; b marketplace_bookings;begin
 select * into o from quotes where id=p_quote; select * into b from marketplace_bookings where id=o.booking_id for update;
 if b.customer_id is distinct from auth.uid() or o.provider_id is distinct from b.provider_id or o.status<>'PENDING' or b.status<>'QUOTED' or not exists(select 1 from booking_workflow where booking_id=b.id and arrival_confirmed_at is not null and accepted_quote_id is null) then raise exception 'Customer and pending dispatched quote required'; end if;
 update quotes set status=case when p_accept then 'ACCEPTED' else 'DECLINED' end where id=p_quote;
 if p_accept then update booking_workflow set accepted_quote_id=p_quote where booking_id=b.id; end if;
 update marketplace_bookings set status=case when p_accept then 'IN_PROGRESS' else 'REVIEW_REQUIRED' end,additional_cost=case when p_accept then o.total else additional_cost end,total_cost=case when p_accept then base_cost+o.total else total_cost end where id=b.id;
 insert into booking_events(booking_id,actor_id,action) values(b.id,auth.uid(),case when p_accept then 'quote_accepted' else 'quote_declined' end);
end $$;
alter table public.reviews add column is_verified_booking boolean not null default false;
create unique index verified_review_one_booking on public.reviews(booking_id) where is_verified_booking;
-- Historical sample reviews are retained, never promoted into trusted evidence.
create or replace function public.submit_booking_review(p_booking bigint,p_scores integer[],p_comment text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
declare b marketplace_bookings;begin
 select * into b from marketplace_bookings where id=p_booking for update;
 if exists(select 1 from service_providers p join verification_applications a on a.id=p.verification_application_id where p.id=b.provider_id and a.applicant_id=b.customer_id) or b.customer_id is distinct from auth.uid() or not exists(select 1 from booking_workflow where booking_id=p_booking and completion_confirmed_at is not null) or cardinality(p_scores)<>6 or array_position(p_scores,null) is not null or exists(select 1 from unnest(p_scores) n where n not between 1 and 5) or length(p_comment)>1000 then raise exception 'Actual customer, confirmed completion and valid review required'; end if;
 insert into reviews(booking_id,provider_id,customer_id,quality,professionalism,timeliness,communication,value_rating,overall,comment,is_verified_booking) values(p_booking,b.provider_id,auth.uid(),p_scores[1],p_scores[2],p_scores[3],p_scores[4],p_scores[5],p_scores[6],p_comment,true);
 insert into booking_events(booking_id,actor_id,action) values(p_booking,auth.uid(),'review_submitted');
end $$;
revoke all on function public.request_qualified_service(bigint,text,real,real,text,bigint,boolean),public.marketplace_action(bigint,text,text),public.offer_repair_quote(bigint,bigint,bigint),public.respond_repair_quote(bigint,boolean),public.submit_booking_review(bigint,integer[],text) from public;
grant execute on function public.request_qualified_service(bigint,text,real,real,text,bigint,boolean),public.marketplace_action(bigint,text,text),public.offer_repair_quote(bigint,bigint,bigint),public.respond_repair_quote(bigint,boolean),public.submit_booking_review(bigint,integer[],text) to authenticated;
-- Preserve historical completion, dispute and fee evidence even after qualification expires.
create or replace function public.guard_verified_booking() returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if new.provider_id is null and new.status not in('SEARCHING','CANCELLED') then raise exception 'A qualified dispatched technician is required'; end if;
 if new.provider_id is not null and (tg_op='INSERT' or new.provider_id is distinct from old.provider_id or new.service_type is distinct from old.service_type or new.lat is distinct from old.lat or new.lng is distinct from old.lng or new.status in('ASSIGNED','EN_ROUTE','QUOTED','IN_PROGRESS')) then
  if not exists(select 1 from service_providers p where p.id=new.provider_id and public.verification_active(p.verification_application_id,'provider',p.id,p.category)) then raise exception 'Provider is not currently qualified'; end if;
  if tg_op='INSERT' or new.provider_id is distinct from old.provider_id or new.service_type is distinct from old.service_type or new.lat is distinct from old.lat or new.lng is distinct from old.lng then
   if not exists(select 1 from service_providers p where p.id=new.provider_id and p.status='AVAILABLE' and upper(replace(p.category,' ','_'))=upper(replace(new.service_type,' ','_')) and public.verification_distance_km(new.lat,new.lng,p.lat,p.lng)<=p.coverage_radius_km) then raise exception 'Technician unavailable, wrong trade or outside service area'; end if;
   if not new.diagnostic_fee_acknowledged or not exists(select 1 from service_providers p where p.id=new.provider_id and p.diagnostic_fee is not null and new.base_cost=p.diagnostic_fee) then raise exception 'Diagnostic fee and repair exclusions must be acknowledged'; end if;
  end if;
 end if;return new;
end $$;
create or replace function public.nearby_qualified_providers(p_lat double precision,p_lng double precision,p_service text)
returns table(id bigint,name text,rating real,reviews_count integer,approx_lat double precision,approx_lng double precision,distance_km double precision,arrival_minutes integer,diagnostic_fee bigint)
language plpgsql stable security definer set search_path=public,pg_temp as $$
begin
 if p_lat is null or p_lng is null or p_lat not between -90 and 90 or p_lng not between -180 and 180 then raise exception 'Valid user location required'; end if;
 return query select p.id,p.name,coalesce(r.avg_rating,0)::real,r.count_reviews::integer,round(p.lat::numeric,2)::double precision,round(p.lng::numeric,2)::double precision,public.verification_distance_km(p_lat,p_lng,p.lat,p.lng),greatest(5,ceil(public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)*2)::integer),p.diagnostic_fee
 from service_providers p cross join lateral(select avg(v.overall) avg_rating,count(*) count_reviews from reviews v join marketplace_bookings b on b.id=v.booking_id join booking_workflow w on w.booking_id=b.id where v.provider_id=p.id and b.provider_id=p.id and b.customer_id=v.customer_id and b.service_type=p.category and v.is_verified_booking and w.completion_confirmed_at is not null) r
 where p.status='AVAILABLE' and p.category=p_service and p.diagnostic_fee is not null and public.verification_active(p.verification_application_id,'provider',p.id,p.category) and public.provider_fee_eligible(p.id) and public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)<=p.coverage_radius_km
 order by (coalesce(r.avg_rating,0)*r.count_reviews+3.5*10)/(r.count_reviews+10) desc,greatest(5,ceil(public.verification_distance_km(p_lat,p_lng,p.lat,p.lng)*2)::integer),p.id;
end $$;
