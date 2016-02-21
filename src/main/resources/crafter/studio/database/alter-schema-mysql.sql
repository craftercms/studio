-- cstudio_activity
alter table cstudio_activity engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_activity drop KEY `cstudio_activity_content_idx`;
alter table cstudio_activity add KEY `cstudio_activity_content_idx` (`content_id`(1000));

-- cstudio_DEPENDENCY
alter table cstudio_DEPENDENCY engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_DEPENDENCY drop KEY `cstudio_dependency_sourcepath_idx`;
alter table cstudio_DEPENDENCY add KEY `cstudio_dependency_sourcepath_idx` (`source_path`(1000));

-- cstudio_copytoenvironment
alter table cstudio_copytoenvironment engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_copytoenvironment drop INDEX `cstudio_cte_path_idx`;
alter table cstudio_copytoenvironment add INDEX `cstudio_cte_path_idx` (`path`(1000) ASC);
alter table cstudio_copytoenvironment drop INDEX `cstudio_cte_sitepath_idx`;
alter table cstudio_copytoenvironment add INDEX `cstudio_cte_sitepath_idx` (`site` ASC, `path`(900) ASC);

-- cstudio_deploymentsynchistory
alter table cstudio_deploymentsynchistory engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_deploymentsynchistory drop INDEX `cs_depsynchist_path_idx`;
alter table cstudio_deploymentsynchistory add INDEX `cs_depsynchist_path_idx` (`path`(1000) ASC);
alter table cstudio_deploymentsynchistory drop INDEX `cs_depsynchist_sitepath_idx`;
alter table cstudio_deploymentsynchistory add INDEX `cs_depsynchist_sitepath_idx` (`site` ASC, `path`(900) ASC);

-- cstudio_objectmetadata
alter table cstudio_objectmetadata engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_objectmetadata drop INDEX `uq__om_site_path`;
alter table cstudio_objectmetadata add UNIQUE `uq__om_site_path` (`site`, `path`(900));

-- cstudio_objectstate
alter table cstudio_objectstate engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_objectstate drop INDEX `uq_os_site_path`;
alter table cstudio_objectstate add UNIQUE `uq_os_site_path` (`site`, `path`(900));

-- cstudio_pagenavigationordersequence
alter table cstudio_pagenavigationordersequence engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_pagenavigationordersequence modify column `path` text not null;

-- cstudio_publishtotarget
alter table cstudio_publishtotarget engine InnoDB ROW_FORMAT=DYNAMIC;
alter table cstudio_publishtotarget drop INDEX `cstudio_ptt_path`;
alter table cstudio_publishtotarget add INDEX `cstudio_ptt_path` (`path`(1000) ASC);
alter table cstudio_publishtotarget drop INDEX `cstudio_ptt_sitepath_idx`;
alter table cstudio_publishtotarget add INDEX `cstudio_ptt_sitepath_idx` (`site` ASC, `path`(900) ASC);

-- cstudio_site
alter table cstudio_site engine InnoDB ROW_FORMAT=DYNAMIC;