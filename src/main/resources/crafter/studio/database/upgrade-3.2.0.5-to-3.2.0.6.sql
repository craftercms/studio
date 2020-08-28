ALTER TABLE `item` DROP FOREIGN KEY `item_ix_parent`  ;

ALTER TABLE `item` ADD FOREIGN KEY item_ix_parent(`parent_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ;

UPDATE _meta SET version = '3.2.0.6' ;