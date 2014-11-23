CREATE TABLE cstudio_DEPENDENCY (
        id NUMBER(19, 0) PRIMARY KEY,
        site varchar(35) NOT NULL,
        source_path varchar(1000) NOT NULL,
        target_path varchar(1000) NOT NULL,
        "type" varchar(15) NOT NULL
);

CREATE SEQUENCE CSTUDIO_DEPENDENCY_SEQUENCE
    START WITH 1
    INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cstudio_dependency_trigger BEFORE INSERT ON cstudio_DEPENDENCY REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_DEPENDENCY_SEQUENCE.nextval INTO :NEW.ID FROM dual;END;;

CREATE INDEX cstudio_dependency_site_idx ON cstudio_DEPENDENCY (SITE);
CREATE INDEX cs_dep_sourcepath_idx ON cstudio_DEPENDENCY (SOURCE_PATH);

