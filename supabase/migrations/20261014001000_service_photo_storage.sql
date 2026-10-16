-- Private request photos, limited to actual booking participants.
insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types) values('service-photos','service-photos',false,5242880,array['image/jpeg','image/png','image/webp']) on conflict(id) do update set public=false,file_size_limit=excluded.file_size_limit,allowed_mime_types=excluded.allowed_mime_types;
create policy service_photo_upload on storage.objects for insert to authenticated with check(bucket_id='service-photos' and exists(select 1 from public.booking_photos p where p.object_path=name and p.uploader_id=auth.uid() and not p.uploaded));
create policy service_photo_read on storage.objects for select to authenticated using(bucket_id='service-photos' and public.can_read_booking_photo(name));
create or replace function public.confirm_booking_photo(p_path text) returns void language plpgsql security definer set search_path=public,pg_temp as $$
begin
 if not exists(select 1 from storage.objects where bucket_id='service-photos' and name=p_path) then raise exception 'Upload the photo before confirming it';end if;
 update public.booking_photos set uploaded=true where object_path=p_path and uploader_id=auth.uid();if not found then raise exception 'Photo access denied';end if;
end $$;
