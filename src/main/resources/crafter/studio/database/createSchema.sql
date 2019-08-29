CREATE DATABASE IF NOT EXISTS crafter
  DEFAULT CHARACTER SET utf8 ;

FLUSH PRIVILEGES ;

CREATE USER 'crafter'@'localhost'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON crafter.* TO 'crafter'@'localhost'
WITH GRANT OPTION ;

CREATE USER 'crafter'@'%'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON crafter.* TO 'crafter'@'%'
WITH GRANT OPTION ;