CREATE  TABLE cstudio_deploymentsynchistory (
  id bigserial NOT NULL ,
  syncdate timestamp NOT NULL ,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path TEXT NOT NULL ,
  target VARCHAR(50) NOT NULL ,
  username VARCHAR(255) NOT NULL ,
  contenttypeclass VARCHAR(25) NOT NULL ,
  CONSTRAINT cstudio_deploymentsynchistory_pkey PRIMARY KEY (id)
);

CREATE INDEX cs_depsynchist_site_idx
ON cstudio_deploymentsynchistory USING BTREE (site);

CREATE INDEX cs_depsynchist_env_idx
ON cstudio_deploymentsynchistory USING BTREE (environment);

CREATE INDEX cs_depsynchist_sitepath_idx
ON cstudio_deploymentsynchistory USING BTREE (site, path);

CREATE INDEX cs_depsynchist_target_idx
ON cstudio_deploymentsynchistory USING BTREE (target);

CREATE INDEX cs_depsynchist_user_idx
ON cstudio_deploymentsynchistory USING BTREE (username);

CREATE INDEX cs_depsynchist_ctc_idx
ON cstudio_deploymentsynchistory USING BTREE (contenttypeclass);
