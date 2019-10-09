CREATE DATABASE IF NOT EXISTS @crafter_schema_name
  DEFAULT CHARACTER SET utf8 ;

FLUSH PRIVILEGES ;

CREATE USER 'crafter'@'localhost'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON @crafter_schema_name.* TO 'crafter'@'localhost'
WITH GRANT OPTION ;

CREATE USER 'crafter'@'%'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON @crafter_schema_name.* TO 'crafter'@'%'
WITH GRANT OPTION ;