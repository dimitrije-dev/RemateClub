alter table clubs
  add column address varchar(255),
  add column latitude numeric(9, 6),
  add column longitude numeric(9, 6),
  add column average_rating numeric(2, 1) not null default 0.0,
  add column review_count integer not null default 0;

alter table clubs
  add constraint ck_clubs_latitude check (latitude is null or latitude between -90 and 90),
  add constraint ck_clubs_longitude check (longitude is null or longitude between -180 and 180),
  add constraint ck_clubs_average_rating check (average_rating between 0 and 5),
  add constraint ck_clubs_review_count check (review_count >= 0);

alter table courts
  add column environment varchar(20) not null default 'INDOOR';

alter table courts
  alter column environment drop default,
  add constraint ck_courts_environment check (environment in ('INDOOR', 'OUTDOOR'));

create index idx_courts_environment on courts (environment);
