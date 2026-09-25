-- ============================================================
-- SecureFilePreview - Supabase schema (PRODUCTION)
-- Project ref : astwkwiowbucqkadrdom
-- Project URL : https://astwkwiowbucqkadrdom.supabase.co
--
-- Mobile clients use the publishable (anon) API key, so all access
-- is gated by Row Level Security on this table AND on the storage
-- bucket. Service-role keys MUST NEVER be embedded in the APK.
-- ============================================================

-- 1) Code -> object-path mapping ----------------------------------
create table if not exists public.shared_links (
    code        integer     primary key,            -- 4-digit secret code
    file_path   text        not null,               -- "<uuid>/<filename>" in the bucket
    file_name   text,                               -- original file name (for UX)
    created_at  timestamptz not null default now()
);

alter table public.shared_links enable row level security;

drop policy if exists "anon_insert_shared_links" on public.shared_links;
drop policy if exists "anon_select_shared_links" on public.shared_links;
drop policy if exists "anon_delete_shared_links" on public.shared_links;

create policy "anon_insert_shared_links"
    on public.shared_links for insert to anon with check (true);

create policy "anon_select_shared_links"
    on public.shared_links for select to anon using (true);

create policy "anon_delete_shared_links"
    on public.shared_links for delete to anon using (true);

-- 2) Storage bucket ---------------------------------------------
insert into storage.buckets (id, name, public)
values ('shared-files', 'shared-files', false)
on conflict (id) do nothing;

drop policy if exists "anon_upload_shared-files" on storage.objects;
drop policy if exists "anon_read_shared-files"   on storage.objects;
drop policy if exists "anon_delete_shared-files" on storage.objects;

create policy "anon_upload_shared-files"
    on storage.objects for insert to anon
    with check (bucket_id = 'shared-files');

create policy "anon_read_shared-files"
    on storage.objects for select to anon
    using (bucket_id = 'shared-files');

create policy "anon_delete_shared-files"
    on storage.objects for delete to anon
    using (bucket_id = 'shared-files');

-- Refresh PostgREST schema cache so new columns/policies are live.
notify pgrst, 'reload schema';
