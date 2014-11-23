CREATE  TABLE cstudio_deploymentsynchistory (
  id NUMBER (19, 0) PRIMARY KEY ,
  syncdate DATE NOT NULL ,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path VARCHAR(1000) NOT NULL ,
  target VARCHAR(50) NOT NULL ,
  username VARCHAR(255) NOT NULL ,
  contenttypeclass VARCHAR(25)
);

CREATE SEQUENCE CSTUDIO_DEPSYNCHISTORY_SEQ
START WITH 1
INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cs_depsynchistory_trigger BEFORE INSERT ON cstudio_deploymentsynchistory REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_DEPSYNCHISTORY_SEQ.nextval INTO :NEW.ID FROM dual;END;;

CREATE INDEX cstudio_dsh_site_idx ON cstudio_deploymentsynchistory (site);
CREATE INDEX cstudio_dsh_environment_idx ON cstudio_deploymentsynchistory (environment);
CREATE INDEX cstudio_dsh_sitepath_idx ON cstudio_deploymentsynchistory (site, path);
CREATE INDEX cstudio_dsh_target_idx ON cstudio_deploymentsynchistory (target);
CREATE INDEX cstudio_dsh_username_idx ON cstudio_deploymentsynchistory (username);
CREATE INDEX cs_dsh_contenttypecls_idx ON cstudio_deploymentsynchistory (contenttypeclass);
