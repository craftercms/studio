CREATE  TABLE cstudio_copytoenvironment (
  id NUMBER (19, 0) PRIMARY KEY,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path VARCHAR(1000) NOT NULL ,
  oldpath VARCHAR(1000) ,
  username VARCHAR(255) ,
  scheduleddate DATE NOT NULL ,
  state VARCHAR(50) NOT NULL ,
  action VARCHAR(20) NOT NULL ,
  contenttypeclass VARCHAR(20)
);

CREATE SEQUENCE CSTUDIO_COPYTOENVIRONMENT_SEQ
START WITH 1
INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cs_copytoenv_trigger BEFORE INSERT ON cstudio_copytoenvironment REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_COPYTOENVIRONMENT_SEQ.nextval INTO :NEW.ID FROM dual;END;;

CREATE INDEX cstudio_cte_site_idx ON cstudio_copytoenvironment (site);
CREATE INDEX cstudio_cte_environment_idx ON cstudio_copytoenvironment (environment);
CREATE INDEX cstudio_cte_sitepath_idx ON cstudio_copytoenvironment (site, path);
CREATE INDEX cstudio_cte_state_idx ON cstudio_copytoenvironment (state);

CREATE  TABLE cstudio_publishtotarget (
  id NUMBER (19, 0) PRIMARY KEY,
  site VARCHAR(50) NOT NULL ,
  environment VARCHAR(20) NOT NULL ,
  path VARCHAR(1000) NOT NULL ,
  oldpath VARCHAR(1000) ,
  username VARCHAR(255) NOT NULL ,
  version NUMBER(19, 0) NOT NULL ,
  action VARCHAR(20) NOT NULL ,
  contenttypeclass VARCHAR(20)
);

CREATE SEQUENCE CSTUDIO_PUBLISHTOTARGET_SEQ
START WITH 1
INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cs_pubtotarget_trigger BEFORE INSERT ON cstudio_publishtotarget REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_PUBLISHTOTARGET_SEQ.nextval INTO :NEW.ID FROM dual;END;;

CREATE INDEX cstudio_ptt_site_idx ON cstudio_publishtotarget (site);
CREATE INDEX cstudio_ptt_environment_idx ON cstudio_publishtotarget (environment);
CREATE INDEX cstudio_ptt_sitepath_idx ON cstudio_publishtotarget (site, path);