CREATE TABLE cstudio_SEQUENCE (
        id NUMBER (19, 0) NOT NULL PRIMARY KEY,
        namespace varchar(20) NOT NULL,
        sql_generator NUMBER(19, 0) NOT NULL,
        step NUMBER(19, 0) NOT NULL
        );

        CREATE SEQUENCE CSTUDIO_SEQUENCE_SEQ
        START WITH 1
        INCREMENT BY 1;

CREATE OR REPLACE TRIGGER cstudio_SEQUENCE_trigger BEFORE INSERT ON cstudio_SEQUENCE REFERENCING NEW AS NEW FOR EACH ROW BEGIN SELECT CSTUDIO_SEQUENCE_SEQ.nextval INTO :NEW.ID FROM dual; END;;
        
CREATE INDEX cstudio_sequence_namespace_idx ON cstudio_SEQUENCE (namespace);