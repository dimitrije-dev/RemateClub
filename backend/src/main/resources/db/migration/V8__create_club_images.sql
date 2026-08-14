create table club_images (
  id uuid primary key,
  club_id uuid not null,
  storage_key varchar(120) not null unique,
  original_filename varchar(255) not null,
  content_type varchar(40) not null,
  file_size bigint not null,
  width integer not null,
  height integer not null,
  alt_text varchar(180) not null,
  sort_order integer not null default 0,
  is_cover boolean not null default false,
  created_at timestamp(6) with time zone not null,
  constraint fk_club_images_club_id foreign key (club_id) references clubs (id) on delete cascade,
  constraint ck_club_images_file_size check (file_size > 0),
  constraint ck_club_images_dimensions check (width > 0 and height > 0)
);

create index idx_club_images_club_id on club_images (club_id);
create unique index uq_club_images_one_cover_per_club on club_images (club_id) where is_cover = true;
