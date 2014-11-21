
CREATE TABLE cstudio_activity (
  id NUMBER(19,0) PRIMARY KEY,
  modified_date DATE NOT NULL,
  creation_date DATE NOT NULL,
  summary CLOB NOT NULL,
  summary_format varchar(255) NOT NULL,
  content_id varchar(2000) NOT NULL,
  site_network varchar(255) NOT NULL,
  activity_type varchar(255) NOT NULL,
  content_type varchar(255) NOT NULL,
  post_user_id varchar(255) NOT NULL
);


CREATE SEQUENCE CSTUDIO_TABLE_ACTIVITY
START WITH 1
INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cstduio_activity_trigger BEFORE INSERT ON cstudio_activity REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_TABLE_ACTIVITY.nextval INTO :NEW.ID FROM dual;END;;


CREATE INDEX cstudio_activity_user_idx ON cstudio_activity (post_user_id);

CREATE INDEX cstudio_activity_site_idx ON cstudio_activity (site_network);

CREATE INDEX cstudio_activity_content_idx ON cstudio_activity (content_id);

