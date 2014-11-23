
CREATE TABLE cstudio_objectstate (
        object_id varchar(255) NOT NULL PRIMARY KEY,
        site varchar(50) NOT NULL,
        path varchar(2000) NOT NULL,
        state varchar(255) NOT NULL,
        system_processing NUMBER(1,0) NOT NULL
);

