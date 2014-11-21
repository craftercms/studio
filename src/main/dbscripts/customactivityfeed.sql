CREATE TABLE cstudio_activity 
(
  id bigint NOT NULL IDENTITY,
  modified_date datetime NOT NULL,
  creation_date timestamp NOT NULL,
  summary varchar(4000) NOT NULL,
  summary_format varchar(255) NOT NULL,
  content_id varchar(255) NOT NULL,
  site_network varchar(255) default NULL,
  activity_type varchar(255) NOT NULL,
  content_type varchar(255) NOT NULL,
  post_user_id varchar(255) NOT NULL,
  primary key (id)

);
