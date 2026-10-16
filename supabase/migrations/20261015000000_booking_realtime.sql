-- Publish only booking-related tables; existing RLS and write restrictions remain in force.
DO $$
DECLARE relation_name text;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname='supabase_realtime') THEN
    CREATE PUBLICATION supabase_realtime;
  END IF;
  FOREACH relation_name IN ARRAY ARRAY['marketplace_bookings','quotes','booking_workflow','booking_events','booking_photos','reviews'] LOOP
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname='supabase_realtime' AND schemaname='public' AND tablename=relation_name) THEN
      EXECUTE format('ALTER PUBLICATION supabase_realtime ADD TABLE public.%I',relation_name);
    END IF;
  END LOOP;
END $$;
