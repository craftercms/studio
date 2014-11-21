CREATE  TABLE cstudio_copytoenvironment (
  id bigserial NOT NULL ,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path TEXT NOT NULL ,
  oldpath TEXT NULL ,
  username VARCHAR(255) NULL ,
  scheduleddate timestamp NOT NULL ,
  state VARCHAR(50) NOT NULL ,
  action VARCHAR(20) NOT NULL ,
  contenttypeclass VARCHAR(20) NULL,
  CONSTRAINT cstudio_copytoenvironment_pkey PRIMARY KEY (id)
);

CREATE INDEX cstudio_cte_site_idx
ON cstudio_copytoenvironment USING BTREE (site);

CREATE INDEX cstudio_cte_environment_idx
ON cstudio_copytoenvironment USING BTREE (environment);

CREATE INDEX cstudio_cte_sitepath_idx
ON cstudio_copytoenvironment USING BTREE (site, path);

CREATE INDEX cstudio_cte_state_idx
ON cstudio_copytoenvironment USING BTREE (state);


CREATE  TABLE cstudio_publishtotarget (
  id bigserial NOT NULL ,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path TEXT NOT NULL ,
  oldpath TEXT NULL ,
  username VARCHAR(255) NOT NULL ,
  version bigint NOT NULL ,
  action VARCHAR(20) NOT NULL ,
  contenttypeclass VARCHAR(20) NULL,
  CONSTRAINT cstudio_publishtotarget_pkey PRIMARY KEY (id)
);

CREATE INDEX cstudio_ptt_site_idx
ON cstudio_publishtotarget USING BTREE (site);

CREATE INDEX cstudio_ptt_environment_idx
ON cstudio_publishtotarget USING BTREE (environment);

CREATE INDEX cstudio_ptt_sitepath_idx
ON cstudio_publishtotarget USING BTREE (site, path);
