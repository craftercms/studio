CREATE TABLE cstudio_activity
(
  id bigserial NOT NULL,
  modified_date timestamp NOT NULL,
  creation_date timestamp NOT NULL,
  summary text NOT NULL,
  summary_format varchar(255) NOT NULL,
  content_id text NOT NULL,
  site_network varchar(255) NOT NULL,
  activity_type varchar(255) NOT NULL,
  content_type varchar(255) NOT NULL,
  post_user_id varchar(255) NOT NULL,
  CONSTRAINT cstudio_activity_pkey PRIMARY KEY (id)
);

CREATE INDEX cstudio_activity_content_idx
ON cstudio_activity USING btree (content_id);

CREATE INDEX cstudio_activity_site_idx
ON cstudio_activity USING btree (site_network);

CREATE INDEX cstudio_activity_user_idx
ON cstudio_activity USING btree (post_user_id);

