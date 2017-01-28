
CREATE TABLE CSTUDIO_ACTIVITY
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    MODIFIED_DATE TIMESTAMP NOT NULL,
    CREATION_DATE TIMESTAMP NOT NULL,
    SUMMARY VARCHAR(3000) NOT NULL,
    SUMMARY_FORMAT VARCHAR(255) NOT NULL,
    CONTENT_ID VARCHAR(3000) NOT NULL,
    SITE_NETWORK VARCHAR(255) NOT NULL,
    ACTIVITY_TYPE VARCHAR(255) NOT NULL,
    CONTENT_TYPE VARCHAR(255) NOT NULL,
    POST_USER_ID VARCHAR(255) NOT NULL,
  CONSTRAINT primary_key PRIMARY KEY (id)
) ;

CREATE TABLE CSTUDIO_DEPENDENCY (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(35) NOT NULL,
  SOURCE_PATH VARCHAR(3000) NOT NULL,
  TARGET_PATH VARCHAR(3000) NOT NULL,
  TYPE VARCHAR(15) NOT NULL
) ;

CREATE TABLE CSTUDIO_OBJECTSTATE (
  OBJECT_ID VARCHAR(255) PRIMARY KEY NOT NULL,
  SITE VARCHAR(50)   NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  STATE VARCHAR(255)  NOT NULL,
  SYSTEM_PROCESSING SMALLINT NOT NULL,
  UNIQUE (SITE, PATH)
) ;

CREATE TABLE CSTUDIO_PAGENAVIGATIONORDERSEQUENCE (
  FOLDER_ID VARCHAR(100) PRIMARY KEY NOT NULL,
  SITE VARCHAR(50)  NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  MAX_COUNT FLOAT NOT NULL
) ;

CREATE TABLE CSTUDIO_COPYTOENVIRONMENT (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50)  NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  OLDPATH VARCHAR(3000),
  USERNAME VARCHAR(255),
  SCHEDULEDDATE TIMESTAMP NOT NULL,
  STATE VARCHAR(50) NOT NULL,
  ACTION VARCHAR(20) NOT NULL,
  CONTENTTYPECLASS VARCHAR(20),
  SUBMISSIONCOMMENT VARCHAR(3000)
) ;

CREATE TABLE CSTUDIO_PUBLISHTOTARGET (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50) NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  OLDPATH VARCHAR(2000),
  USERNAME VARCHAR(255) NOT NULL,
  VERSION BIGINT NOT NULL,
  ACTION VARCHAR(20) NOT NULL,
  CONTENTTYPECLASS VARCHAR(20)
) ;

CREATE TABLE CSTUDIO_DEPLOYMENTSYNCHISTORY (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SYNCDATE TIMESTAMP NOT NULL,
  SITE VARCHAR(50) NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  TARGET VARCHAR(50) NOT NULL,
  USERNAME VARCHAR(255) NOT NULL,
  CONTENTTYPECLASS VARCHAR(25)
) ;

CREATE TABLE CSTUDIO_SITE (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE_ID VARCHAR(255) NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  DESCRIPTION VARCHAR(45),
  STATUS VARCHAR(255),
  LAST_COMMIT_ID VARCHAR(50),
  SYSTEM INT NOT NULL DEFAULT 0,
  UNIQUE (SITE_ID)
) ;

CREATE TABLE CSTUDIO_OBJECTMETADATA (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  NAME VARCHAR(255),
  MODIFIED TIMESTAMP,
  MODIFIER VARCHAR(255),
  OWNER VARCHAR(255),
  CREATOR VARCHAR(255),
  FIRSTNAME VARCHAR(255),
  LASTNAME VARCHAR(255),
  LOCKOWNER VARCHAR(255),
  EMAIL VARCHAR(255),
  RENAMED INT,
  OLDURL VARCHAR(2000),
  DELETEURL VARCHAR(2000),
  IMAGEWIDTH INT ,
  IMAGEHEIGHT INT,
  APPROVEDBY VARCHAR(255),
  SUBMITTEDBY VARCHAR(255),
  SUBMITTEDFORDELETION INT,
  SENDEMAIL INT,
  SUBMISSIONCOMMENT VARCHAR(2000),
  LAUNCHDATE TIMESTAMP,
  COMMIT_ID VARCHAR(50),
  UNIQUE (SITE, PATH)
) ;

CREATE TABLE CSTUDIO_USER
(
    USERNAME VARCHAR(255) PRIMARY KEY NOT NULL ,
    PASSWORD VARCHAR(255) NOT NULL,
    FIRSTNAME VARCHAR(255) NOT NULL,
    LASTNAME VARCHAR(255) NOT NULL,
    EMAIL VARCHAR(255) NOT NULL,
    ENABLED INT NOT NULL
) ;

INSERT INTO CSTUDIO_USER (USERNAME, PASSWORD, FIRSTNAME, LASTNAME, EMAIL, ENABLED)
    VALUES ('admin', 'vTwNOJ8GJdyrP7rrvQnpwsd2hCV1xRrJdTX2sb51i+w=|R68ms0Od3AngQMdEeKY6lA==', 'admin', 'admin', 'evaladmin@example.com', 1) ;


CREATE TABLE CSTUDIO_GROUP
(
    ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR(3000),
    SITE_ID BIGINT CONSTRAINT GROUP_SITE_FK REFERENCES CSTUDIO_SITE,
    UNIQUE (NAME)
) ;

CREATE TABLE CSTUDIO_USERGROUP
(
    ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    USERNAME VARCHAR(255) NOT NULL CONSTRAINT USER_UG_FOREIGN_KEY REFERENCES CSTUDIO_USER,
    GROUPID BIGINT NOT NULL CONSTRAINT GROUP_UG_FOREIGN_KEY REFERENCES CSTUDIO_GROUP
) ;

INSERT INTO CSTUDIO_SITE (SITE_ID, NAME, DESCRIPTION, SYSTEM) VALUES ('studio_root', 'Studio Root', 'Studio Root for global permissions', 1) ;

INSERT INTO CSTUDIO_GROUP (NAME, DESCRIPTION, SITE_ID) VALUES ('crafter-admin', 'crafter admin', 1) ;
INSERT INTO CSTUDIO_GROUP (NAME, DESCRIPTION, SITE_ID) VALUES ('crafter-create-sites', 'crafter-create-sites', 1) ;

INSERT INTO CSTUDIO_USERGROUP (USERNAME, GROUPID) VALUES ('admin', 1) ;
INSERT INTO CSTUDIO_USERGROUP (USERNAME, GROUPID) VALUES ('admin', 2) ;
