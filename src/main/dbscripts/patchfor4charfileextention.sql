MS-SQL database
alter table avm_nodes alter column mime_type varchar(100);

MySQL database
alter table avm_nodes change column mime_type varchar(100);
