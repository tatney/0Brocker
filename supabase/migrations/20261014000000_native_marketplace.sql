-- Native marketplace integration. Identity and evidence remain server-bound.
create or replace function public.ensure_my_profile() returns jsonb language plpgsql security definer set search_path=public,pg_temp as $$
declare p public.users;begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 insert into users(auth_user_id,full_name,phone,email) select id,'','',email from auth.users where id=auth.uid() on conflict(auth_user_id) do nothing;
 select * into p from users where auth_user_id=auth.uid();
 return to_jsonb(p)-'auth_user_id';
end $$;
create or replace function public.update_my_profile(p_name text,p_avatar text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if auth.uid() is null or length(trim(p_name)) not between 1 and 80 or length(p_avatar)>40 then raise exception 'Valid profile required';end if;
 perform ensure_my_profile();update users set full_name=trim(p_name),avatar_emoji=p_avatar where auth_user_id=auth.uid();
end $$;

-- Return only active evidence; never promote sample flags into approvals.
create or replace function public.provider_catalog(p_lat double precision default null,p_lng double precision default null) returns setof jsonb language sql stable security definer set search_path=public,pg_temp as $$
 select (to_jsonb(p)-'lat'-'lng') || jsonb_build_object('lat',round(p.lat::numeric,2),'lng',round(p.lng::numeric,2),'match_lat',p_lat,'match_lng',p_lng,'distance_km',case when p_lat is not null and p_lng is not null then verification_distance_km(p_lat,p_lng,p.lat,p.lng) end,'arrival_minutes',case when p_lat is not null and p_lng is not null then greatest(5,ceil(verification_distance_km(p_lat,p_lng,p.lat,p.lng)*2)::integer) end,'technician_identity',a.applicant_id,'qualification_valid_until',extract(epoch from least(now()+interval '30 seconds',a.expires_on::timestamptz,(select min(e.expires_at) from verification_evidence e where e.application_id=a.id and e.verified))), 'jobs_completed',(select count(*) from marketplace_bookings b join booking_workflow w on w.booking_id=b.id where b.provider_id=p.id and w.completion_confirmed_at is not null), 'rating',coalesce(r.score,0),'reviews_count',r.n)
 from service_providers p join verification_applications a on a.id=p.verification_application_id
 cross join lateral(select avg(v.overall) score,count(*) n from reviews v join booking_workflow w on w.booking_id=v.booking_id join marketplace_bookings b on b.id=v.booking_id where v.provider_id=p.id and v.is_verified_booking and w.completion_confirmed_at is not null and b.service_type=p.category) r
 where p.status='AVAILABLE' and verification_active(a.id,'provider',p.id,p.category) and provider_fee_eligible(p.id) and (p_lat is null or p_lng is null or verification_distance_km(p_lat,p_lng,p.lat,p.lng)<=p.coverage_radius_km)
 order by case when p_lat is not null and p_lng is not null then verification_distance_km(p_lat,p_lng,p.lat,p.lng) else 0 end,p.id;
$$;
create or replace function public.property_catalog() returns setof jsonb language sql stable security definer set search_path=public,pg_temp as $$
 select to_jsonb(p)||jsonb_build_object('owner_id',coalesce(u.id,0),'owner_name',coalesce(u.full_name,''),'ownership_valid_until',extract(epoch from least(now()+interval '30 seconds',(select min(e.expires_at) from verification_evidence e where e.application_id=a.id and e.verified))))
 from properties p join verification_applications a on a.id=p.verification_application_id left join users u on u.auth_user_id=a.applicant_id
 where verification_active(a.id,'property',p.id);
$$;

create table public.property_favorites(user_id uuid not null references auth.users(id),property_id bigint not null references properties(id),primary key(user_id,property_id));
alter table property_favorites enable row level security;
create policy favorite_own on property_favorites for all to authenticated using(user_id=auth.uid()) with check(user_id=auth.uid());
grant select,insert,delete on property_favorites to authenticated;

alter table marketplace_bookings add column request_key uuid unique;
alter table marketplace_bookings add column work_type text not null default 'General';
create or replace function public.request_service_v2(p_provider bigint,p_service text,p_lat real,p_lng real,p_description text,p_fee bigint,p_ack boolean,p_address text,p_work_type text,p_scheduled_at timestamptz,p_request_key uuid)
returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare n bigint;begin
 if auth.uid() is null or p_request_key is null then raise exception 'Sign in and request key required';end if;
 perform pg_advisory_xact_lock(hashtextextended(auth.uid()::text||p_request_key::text,0));
 select id into n from marketplace_bookings where request_key=p_request_key and customer_id=auth.uid();if n is not null then return n;end if;
 if length(trim(p_description)) not between 5 and 2000 or length(trim(p_address)) not between 3 and 300 or p_work_type not in('Inspection','Repair','Installation','Maintenance','General') or (p_scheduled_at is not null and p_scheduled_at<=now()) then raise exception 'Describe the work, location and valid future schedule';end if;
 n:=request_qualified_service(p_provider,p_service,p_lat,p_lng,p_description,p_fee,p_ack);
 update marketplace_bookings set request_key=p_request_key,location_text=p_address,work_type=p_work_type,scheduled_at=p_scheduled_at,provider_name=(select name from service_providers where id=p_provider),provider_emoji=(select emoji from service_providers where id=p_provider) where id=n;
 return n;
end $$;
create or replace function public.set_booking_payment_method(p_booking bigint,p_method text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if p_method not in('Mobile Money','Cash','Card','Wallet') then raise exception 'Invalid payment preference';end if;
 update marketplace_bookings set payment_method=p_method where id=p_booking and customer_id=auth.uid();if not found then raise exception 'Booking access denied';end if;
end $$;

create table public.booking_photos(id uuid primary key default gen_random_uuid(),booking_id bigint not null references marketplace_bookings(id),uploader_id uuid not null references auth.users(id),object_path text not null unique,client_key uuid not null unique,mime text not null check(mime in('image/jpeg','image/png','image/webp')),uploaded boolean not null default false,created_at timestamptz not null default now());
alter table booking_photos enable row level security;
create policy booking_photo_read on booking_photos for select to authenticated using(marketplace_participant(booking_id));
grant select on booking_photos to authenticated;
create or replace function public.prepare_booking_photo(p_booking bigint,p_mime text,p_client_key uuid) returns text language plpgsql security definer set search_path=public,pg_temp as $$
declare object_name text;begin
 perform 1 from marketplace_bookings where id=p_booking and customer_id=auth.uid() and status in('SEARCHING','ASSIGNED') for update;
 if not found then raise exception 'Only the requesting customer can attach photos before arrival';end if;
 select object_path into object_name from booking_photos where client_key=p_client_key and booking_id=p_booking and uploader_id=auth.uid();if object_name is not null then return object_name;end if;
 if p_client_key is null then raise exception 'Photo request key required';end if;
 if p_mime not in('image/jpeg','image/png','image/webp') or (select count(*) from booking_photos where booking_id=p_booking)>=5 then raise exception 'Maximum five JPEG, PNG or WebP photos';end if;
 object_name:=auth.uid()::text||'/'||p_booking::text||'/'||gen_random_uuid()::text||case p_mime when 'image/png' then '.png' when 'image/webp' then '.webp' else '.jpg' end;
 insert into booking_photos(booking_id,uploader_id,object_path,mime,client_key) values(p_booking,auth.uid(),object_name,p_mime,p_client_key);return object_name;
end $$;
create or replace function public.can_read_booking_photo(p_path text) returns boolean language sql stable security definer set search_path=public,pg_temp as $$
 select exists(select 1 from booking_photos where object_path=p_path and marketplace_participant(booking_id));
$$;
-- Storage existence is checked by the storage migration's replacement function.
create or replace function public.confirm_booking_photo(p_path text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin update booking_photos set uploaded=true where object_path=p_path and uploader_id=auth.uid();if not found then raise exception 'Photo access denied';end if;end $$;

create or replace function public.set_provider_availability(p_provider bigint,p_available boolean,p_lat real,p_lng real) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if p_lat not between -90 and 90 or p_lng not between -180 and 180 then raise exception 'Valid location required';end if;
 update service_providers p set status=case when p_available then 'AVAILABLE' else 'OFFLINE' end,lat=p_lat,lng=p_lng where p.id=p_provider and exists(select 1 from verification_applications a where a.id=p.verification_application_id and a.applicant_id=auth.uid()) and (not p_available or verification_active(p.verification_application_id,'provider',p.id,p.category));
 if not found then raise exception 'Provider ownership and active qualification required';end if;
end $$;
create or replace function public.my_provider_profiles() returns setof service_providers language sql stable security definer set search_path=public,pg_temp as $$
 select p.* from service_providers p join verification_applications a on a.id=p.verification_application_id where a.applicant_id=auth.uid();
$$;
create or replace function public.my_properties() returns setof properties language sql stable security definer set search_path=public,pg_temp as $$
 select p.* from properties p join verification_applications a on a.id=p.verification_application_id where a.applicant_id=auth.uid();
$$;
create or replace function public.edit_my_property(p_id bigint,p_title text,p_price bigint,p_location text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if length(trim(p_title)) not between 1 and 160 or p_price<1 or length(trim(p_location)) not between 1 and 300 then raise exception 'Valid listing details required';end if;
 update properties p set title=p_title,price_ugx=p_price,location=p_location where p.id=p_id and exists(select 1 from verification_applications a where a.id=p.verification_application_id and a.applicant_id=auth.uid()) and verification_active(p.verification_application_id,'property',p.id);
 if not found then raise exception 'Verified owner required';end if;
end $$;
create or replace function public.withdraw_my_property(p_id bigint) returns void language plpgsql security definer set search_path=public,pg_temp as $$
declare app uuid;begin
 select a.id into app from properties p join verification_applications a on a.id=p.verification_application_id where p.id=p_id and a.applicant_id=auth.uid() for update of a;
 if app is null then raise exception 'Owner access denied';end if;
 update verification_applications set status='suspended',review_reason='Withdrawn by owner' where id=app;
 insert into verification_audit(actor_id,application_id,action) values(auth.uid(),app,'owner_withdrawn');
end $$;

-- Owner and customer inbox. The sender comes from auth.uid(), never a client string.
alter table conversations add column owner_id uuid references auth.users(id);
alter table conversations add column property_id bigint references properties(id);
create unique index owner_conversation_unique on conversations(customer_id,owner_id,property_id);
alter table messages add column sender_id uuid references auth.users(id);
create or replace function public.conversation_participant(p_id bigint) returns boolean language sql stable security definer set search_path=public,pg_temp as $$
 select exists(select 1 from conversations where id=p_id and auth.uid() in(customer_id,owner_id));
$$;
create policy owner_conversation_read on conversations for select to authenticated using(owner_id=auth.uid());
create policy owner_messages_read on messages for select to authenticated using(conversation_participant(conversation_id));
revoke insert,update,delete on conversations,messages from authenticated;
create or replace function public.open_property_conversation(p_property bigint) returns bigint language plpgsql security definer set search_path=public,pg_temp as $$
declare owner_uid uuid; n bigint; display_name text;begin
 select a.applicant_id,p.title into owner_uid,display_name from properties p join verification_applications a on a.id=p.verification_application_id where p.id=p_property and verification_active(a.id,'property',p.id);
 if auth.uid() is null or owner_uid is null or owner_uid=auth.uid() then raise exception 'Verified property and different customer required';end if;
 insert into conversations(customer_id,owner_id,property_id,name) values(auth.uid(),owner_uid,p_property,display_name) on conflict(customer_id,owner_id,property_id) do update set name=excluded.name returning id into n;return n;
end $$;
create or replace function public.send_conversation_message(p_conversation bigint,p_text text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 perform 1 from conversations where id=p_conversation for update;
 if not conversation_participant(p_conversation) or length(trim(p_text)) not between 1 and 2000 then raise exception 'Participant and valid message required';end if;
 insert into messages(conversation_id,sender,sender_id,text) values(p_conversation,auth.uid()::text,auth.uid(),trim(p_text));
 update conversations set last_message=trim(p_text),last_message_at=now() where id=p_conversation;
end $$;

create table public.workspace_records(id bigint generated always as identity primary key,user_id uuid not null references auth.users(id),workspace text not null check(workspace in('OWNER','PROVIDER')),kind text not null check(kind in('CLIENT','VIEWING','REMINDER','INVOICE')),title text not null check(length(title) between 1 and 160),detail text not null default '' check(length(detail)<=1000),amount_ugx bigint not null default 0 check(amount_ugx between 0 and 1000000000),done boolean not null default false,created_at timestamptz not null default now(),check((workspace='OWNER' and kind<>'CLIENT') or (workspace='PROVIDER' and kind<>'VIEWING')));
alter table workspace_records enable row level security;
create policy workspace_record_own on workspace_records for all to authenticated using(user_id=auth.uid()) with check(user_id=auth.uid());
grant select,insert,update,delete on workspace_records to authenticated;
grant usage,select on sequence workspace_records_id_seq to authenticated;
create table public.service_suggestions(id uuid primary key default gen_random_uuid(),user_id uuid not null references auth.users(id),name text not null check(length(name) between 3 and 80),description text not null check(length(description) between 5 and 1000),status text not null default 'SUBMITTED' check(status='SUBMITTED'),created_at timestamptz not null default now());
alter table service_suggestions enable row level security;
create policy suggestion_read on service_suggestions for select to authenticated using(user_id=auth.uid() or is_admin());
create policy suggestion_insert on service_suggestions for insert to authenticated with check(user_id=auth.uid());
grant select,insert on service_suggestions to authenticated;

-- Restrict every exposed RPC explicitly.
revoke all on function ensure_my_profile(),update_my_profile(text,text),provider_catalog(double precision,double precision),property_catalog(),request_service_v2(bigint,text,real,real,text,bigint,boolean,text,text,timestamptz,uuid),set_booking_payment_method(bigint,text),prepare_booking_photo(bigint,text,uuid),confirm_booking_photo(text),can_read_booking_photo(text),set_provider_availability(bigint,boolean,real,real),my_provider_profiles(),my_properties(),edit_my_property(bigint,text,bigint,text),withdraw_my_property(bigint),conversation_participant(bigint),open_property_conversation(bigint),send_conversation_message(bigint,text) from public;
grant execute on function ensure_my_profile(),update_my_profile(text,text),provider_catalog(double precision,double precision),property_catalog(),request_service_v2(bigint,text,real,real,text,bigint,boolean,text,text,timestamptz,uuid),set_booking_payment_method(bigint,text),prepare_booking_photo(bigint,text,uuid),confirm_booking_photo(text),can_read_booking_photo(text),set_provider_availability(bigint,boolean,real,real),my_provider_profiles(),my_properties(),edit_my_property(bigint,text,bigint,text),withdraw_my_property(bigint),conversation_participant(bigint),open_property_conversation(bigint),send_conversation_message(bigint,text) to authenticated;

-- Durable in-app notifications; push/email delivery remains a separate adapter.
create table public.marketplace_notifications(id bigint generated always as identity primary key,user_id uuid not null references auth.users(id),kind text not null,message text not null,booking_id bigint references marketplace_bookings(id),conversation_id bigint references conversations(id),source_id text not null,read_at timestamptz,created_at timestamptz not null default now(),unique(user_id,kind,source_id));
alter table marketplace_notifications enable row level security;
create policy notification_read on marketplace_notifications for select to authenticated using(user_id=auth.uid());
create policy notification_update on marketplace_notifications for update to authenticated using(user_id=auth.uid()) with check(user_id=auth.uid());
grant select on marketplace_notifications to authenticated;
grant update(read_at) on marketplace_notifications to authenticated;
create or replace function public.notify_booking_event() returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
declare customer uuid;technician uuid; recipient uuid;begin
 select b.customer_id,a.applicant_id into customer,technician from marketplace_bookings b left join service_providers p on p.id=b.provider_id left join verification_applications a on a.id=p.verification_application_id where b.id=new.booking_id;
 foreach recipient in array array[customer,technician] loop
  if recipient is not null and recipient is distinct from new.actor_id then
   insert into marketplace_notifications(user_id,kind,message,booking_id,source_id) values(recipient,'BOOKING','Booking #'||new.booking_id||': '||replace(new.action,'_',' '),new.booking_id,new.id::text) on conflict do nothing;
  end if;
 end loop;return new;
end $$;
create trigger booking_event_notification after insert on booking_events for each row execute function notify_booking_event();
create or replace function public.notify_message() returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
declare recipient uuid;begin
 select case when new.sender_id=customer_id then owner_id else customer_id end into recipient from conversations where id=new.conversation_id;
 if recipient is not null and new.sender_id is not null then insert into marketplace_notifications(user_id,kind,message,conversation_id,source_id) values(recipient,'MESSAGE','New property enquiry message',new.conversation_id,new.id::text) on conflict do nothing;end if;return new;
end $$;
create trigger message_notification after insert on messages for each row execute function notify_message();
create or replace function public.notify_verification() returns trigger language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if new.status is distinct from old.status then insert into marketplace_notifications(user_id,kind,message,source_id) values(new.applicant_id,'VERIFICATION','Your '||new.kind||' application is '||replace(new.status,'_',' '),new.id::text||':'||new.status||':'||clock_timestamp()::text);end if;return new;
end $$;
create trigger verification_notification after update of status on verification_applications for each row execute function notify_verification();
revoke all on function notify_booking_event(),notify_message(),notify_verification() from public;
