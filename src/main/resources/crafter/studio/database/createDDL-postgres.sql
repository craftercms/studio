-- Table: cstudio_activity
-- DROP TABLE cstudio_activity;
CREATE TABLE cstudio_activity
(
  id serial NOT NULL,
  modified_date timestamp without time zone NOT NULL,
  creation_date timestamp without time zone NOT NULL,
  summary text NOT NULL,
  summary_format character varying(255) NOT NULL,
  content_id text NOT NULL,
  site_network character varying(255) NOT NULL,
  activity_type character varying(255) NOT NULL,
  content_type character varying(255) NOT NULL,
  post_user_id character varying(255) NOT NULL,
  CONSTRAINT cstudio_activity_pkey PRIMARY KEY (id)
) ;

-- Index: cstudio_activity_content_idx
-- DROP INDEX cstudio_activity_content_idx;
CREATE INDEX cstudio_activity_content_idx ON cstudio_activity USING btree (content_id) ;

-- Index: cstudio_activity_site_idx
-- DROP INDEX cstudio_activity_site_idx;
CREATE INDEX cstudio_activity_site_idx ON cstudio_activity USING btree (site_network) ;

-- Index: cstudio_activity_user_idx
-- DROP INDEX cstudio_activity_user_idx;
CREATE INDEX cstudio_activity_user_idx ON cstudio_activity USING btree (post_user_id) ;


-- Table: cstudio_dependency
-- DROP TABLE cstudio_dependency;
CREATE TABLE cstudio_dependency
(
  id serial NOT NULL,
  site character varying(35) NOT NULL,
  source_path text NOT NULL,
  target_path text NOT NULL,
  type character varying(15) NOT NULL,
  CONSTRAINT cstudio_dependency_pkey PRIMARY KEY (id)
) ;

-- Index: cstudio_dependency_site_idx
-- DROP INDEX cstudio_dependency_site_idx;
CREATE INDEX cstudio_dependency_site_idx ON cstudio_dependency USING btree (site) ;

-- Index: cstudio_dependency_sourcepath_idx
-- DROP INDEX cstudio_dependency_sourcepath_idx;
CREATE INDEX cstudio_dependency_sourcepath_idx ON cstudio_dependency USING btree (source_path) ;


-- Table: cstudio_objectstate
-- DROP TABLE cstudio_objectstate;
CREATE TABLE cstudio_objectstate
(
  object_id character varying(255) NOT NULL,
  site character varying(50) NOT NULL,
  path text NOT NULL,
  state character varying(255) NOT NULL,
  system_processing bit(1) NOT NULL,
  CONSTRAINT cstudio_objectstate_pkey PRIMARY KEY (object_id),
  CONSTRAINT uq_os_site_path UNIQUE (site, path)
) ;

-- Index: cstudio_objectstate_object_idx
-- DROP INDEX cstudio_objectstate_object_idx;
CREATE INDEX cstudio_objectstate_object_idx ON cstudio_objectstate USING btree (object_id) ;


-- Table: cstudio_pagenavigationordersequence
-- DROP TABLE cstudio_pagenavigationordersequence;
CREATE TABLE cstudio_pagenavigationordersequence
(
  folder_id character varying(100) NOT NULL,
  site character varying(50) NOT NULL,
  path text NOT NULL,
  max_count double precision NOT NULL,
  CONSTRAINT cstudio_pagenavigationordersequence_pkey PRIMARY KEY (folder_id)
) ;

-- Index: cstudio_pagenavigationorder_folder_idx
-- DROP INDEX cstudio_pagenavigationorder_folder_idx;
CREATE INDEX cstudio_pagenavigationorder_folder_idx ON cstudio_pagenavigationordersequence USING btree (folder_id) ;


-- Table: cstudio_copytoenvironment
-- DROP TABLE cstudio_copytoenvironment;
CREATE TABLE cstudio_copytoenvironment
(
  id serial NOT NULL,
  site character varying(50) NOT NULL,
  environment character varying(20) NOT NULL,
  path text NOT NULL,
  oldpath text,
  username character varying(255),
  scheduleddate timestamp without time zone NOT NULL,
  state character varying(50) NOT NULL,
  action character varying(20) NOT NULL,
  contenttypeclass character varying(20),
  submissioncomment text,
  CONSTRAINT cstudio_copytoenvironment_pkey PRIMARY KEY (id)
) ;

-- Index: cstudio_cte_environment_idx
-- DROP INDEX cstudio_cte_environment_idx;
CREATE INDEX cstudio_cte_environment_idx ON cstudio_copytoenvironment USING btree (environment) ;

-- Index: cstudio_cte_path_idx
-- DROP INDEX cstudio_cte_path_idx;
CREATE INDEX cstudio_cte_path_idx ON cstudio_copytoenvironment USING btree (path) ;

-- Index: cstudio_cte_site_idx
-- DROP INDEX cstudio_cte_site_idx;
CREATE INDEX cstudio_cte_site_idx ON cstudio_copytoenvironment USING btree (site COLLATE pg_catalog."default") ;

-- Index: cstudio_cte_sitepath_idx
-- DROP INDEX cstudio_cte_sitepath_idx;
CREATE INDEX cstudio_cte_sitepath_idx ON cstudio_copytoenvironment USING btree (site, path COLLATE pg_catalog."default") ;

-- Index: cstudio_cte_state_idx
-- DROP INDEX cstudio_cte_state_idx;
CREATE INDEX cstudio_cte_state_idx ON cstudio_copytoenvironment USING btree (state) ;


-- Table: cstudio_publishtotarget
-- DROP TABLE cstudio_publishtotarget;
CREATE TABLE cstudio_publishtotarget
(
  id serial NOT NULL,
  site character varying(50) NOT NULL,
  environment character varying(20) NOT NULL,
  path text NOT NULL,
  oldpath text,
  username character varying(255) NOT NULL,
  version bigint NOT NULL,
  action character varying(20) NOT NULL,
  contenttypeclass character varying(20),
  CONSTRAINT cstudio_publishtotarget_pkey PRIMARY KEY (id)
) ;

-- Index: cstudio_ptt_environment_idx
-- DROP INDEX cstudio_ptt_environment_idx;
CREATE INDEX cstudio_ptt_environment_idx ON cstudio_publishtotarget USING btree (environment) ;

-- Index: cstudio_ptt_path
-- DROP INDEX cstudio_ptt_path;
CREATE INDEX cstudio_ptt_path ON cstudio_publishtotarget USING btree (path) ;

-- Index: cstudio_ptt_site_idx
-- DROP INDEX cstudio_ptt_site_idx;
CREATE INDEX cstudio_ptt_site_idx ON cstudio_publishtotarget USING btree (site) ;

-- Index: cstudio_ptt_sitepath_idx
-- DROP INDEX cstudio_ptt_sitepath_idx;
CREATE INDEX cstudio_ptt_sitepath_idx ON cstudio_publishtotarget USING btree (site, path) ;


-- Table: cstudio_deploymentsynchistory
-- DROP TABLE cstudio_deploymentsynchistory;
CREATE TABLE cstudio_deploymentsynchistory
(
  id serial NOT NULL,
  syncdate timestamp without time zone NOT NULL,
  site character varying(50) NOT NULL,
  environment character varying(20) NOT NULL,
  path text NOT NULL,
  target character varying(50) NOT NULL,
  username character varying(255) NOT NULL,
  contenttypeclass character varying(25),
  CONSTRAINT cstudio_deploymentsynchistory_pkey PRIMARY KEY (id)
) ;

-- Index: cs_depsynchist_ctc_idx
-- DROP INDEX cs_depsynchist_ctc_idx;
CREATE INDEX cs_depsynchist_ctc_idx ON cstudio_deploymentsynchistory USING btree (contenttypeclass) ;

-- Index: cs_depsynchist_env_idx
-- DROP INDEX cs_depsynchist_env_idx;
CREATE INDEX cs_depsynchist_env_idx ON cstudio_deploymentsynchistory USING btree (environment) ;

-- Index: cs_depsynchist_path_idx
-- DROP INDEX cs_depsynchist_path_idx;
CREATE INDEX cs_depsynchist_path_idx ON cstudio_deploymentsynchistory USING btree (path) ;

-- Index: cs_depsynchist_site_idx
-- DROP INDEX cs_depsynchist_site_idx;
CREATE INDEX cs_depsynchist_site_idx ON cstudio_deploymentsynchistory USING btree (site) ;

-- Index: cs_depsynchist_sitepath_idx
-- DROP INDEX cs_depsynchist_sitepath_idx;
CREATE INDEX cs_depsynchist_sitepath_idx ON cstudio_deploymentsynchistory USING btree (site, path) ;

-- Index: cs_depsynchist_target_idx
-- DROP INDEX cs_depsynchist_target_idx;
CREATE INDEX cs_depsynchist_target_idx ON cstudio_deploymentsynchistory USING btree (target) ;

-- Index: cs_depsynchist_user_idx
-- DROP INDEX cs_depsynchist_user_idx;
CREATE INDEX cs_depsynchist_user_idx ON cstudio_deploymentsynchistory USING btree (username) ;


-- Table: cstudio_site
-- DROP TABLE cstudio_site;
CREATE TABLE cstudio_site
(
  id serial NOT NULL,
  site_id character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  description text,
  status character varying(255),
  CONSTRAINT cstudio_site_pkey PRIMARY KEY (id),
  CONSTRAINT "id_UNIQUE" UNIQUE (id),
  CONSTRAINT "site_id_UNIQUE" UNIQUE (site_id)
) ;

-- Index: site_id_idx
-- DROP INDEX site_id_idx;
CREATE INDEX site_id_idx ON cstudio_site USING btree (id) ;


-- Table: cstudio_objectmetadata
-- DROP TABLE cstudio_objectmetadata;
CREATE TABLE cstudio_objectmetadata
(
  id serial NOT NULL,
  site character varying(50) NOT NULL,
  path text NOT NULL,
  name character varying(45),
  modified timestamp without time zone,
  modifier character varying(255),
  owner character varying(255),
  creator character varying(255),
  firstname character varying(255),
  lastname character varying(255),
  lockowner character varying(255),
  email character varying(255),
  renamed integer,
  oldurl text,
  deleteurl text,
  imagewidth integer,
  imageheight integer,
  approvedby character varying(255),
  submittedby character varying(255),
  submittedfordeletion integer,
  sendemail integer,
  submissioncomment text,
  launchdate timestamp without time zone,
  CONSTRAINT cstudio_objectmetadata_pkey PRIMARY KEY (id),
  CONSTRAINT uq__om_site_path UNIQUE (site, path)
) ;

