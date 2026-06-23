alter table users
add column created_at datetime not null default current_timestamp; 