Crafter Studio
==============

Crafter Studio is the authoring environment for Crafter CMS. For more information, please read the [docs.](http://docs.craftercms.org/en/3.1/developers/projects/studio/index.html)

We welcome community contributions, please read [CONTRIBUTING.md](https://github.com/craftercms/studio/blob/master/CONTRIBUTING.md) for guidelines.


## Using the Upgrade System

When a change is made to Crafter Studio's configuration or database, the PR that makes that change should include the required upgrades to make sure that sites created in previous versions will get upgraded automatically to work according with the changes introduced. The upgrade system provides four diferent pipelines to help with this.

All pipelines are configured in the same [file](https://github.com/craftercms/studio/blob/develop/src/main/resources/crafter/studio/upgrade/pipelines.yaml).

### Blueprints

In the current version of the upgrade system any change made to the blueprints in this [repository](https://github.com/craftercms/studio/tree/develop/src/main/webapp/repo-bootstrap/global/blueprints) will be automatically copied to the local global repository when Studio boots.

*In the future this will change to follow the same approach used for the other components.*

### Database & Configuration

When there is a change in the database (structure or content) there are two requirements:

1. Update the [SQL create script](https://github.com/craftercms/studio/blob/develop/src/main/resources/crafter/studio/database/createDDL.sql)
2. Add an SQL upgrade script in the [database folder](https://github.com/craftercms/studio/tree/develop/src/main/resources/crafter/studio/database)

The upgrade script can perform any change in the database such as adding/changing or deleting tables and columns. **Keep in mind this changes will be done on existing systems with real data**.

A simple SQL upgrade script could look like this:

```sql
CREATE TABLE IF NOT EXISTS new_feature_table (...) ;

ALTER TABLE `existing_table` DROP COLUMN IF EXISTS `unused_column` ;

UPDATE _meta SET version = '3.1.0.5' ;
```

After completing the upgrade script a new version needs to be added to the upgrade pipeline:

```yaml
pipelines:
  system:
    ...
      - currentVersion: 3.1.0.4
        nextVersion: 3.1.0.5
        operations:
          - type: dbScriptUpgrader
            filename: upgrade-3.1.0.4-to-3.1.0.5.sql
		...
```

**Note: Every SQL script in the system pipeline must update the version in the `_meta` table.**

Global configurations files can be added or updated from this [repository](https://github.com/craftercms/studio/tree/develop/src/main/webapp/repo-bootstrap/global/configuration) to the local global repository:

```yaml
        - type: globalRepoUpgrader
          files:
            - configuration/global-menu-config.xml
            - configuration/global-permission-mappings-config.xml
            - configuration/global-role-mappings-config.xml
```

### Site Structure

When there is a change in the structure of sites like adding, renaming, moving or deleting folders or files a new version needs to be added to the upgrade pipeline:

```yaml
pipelines:
  site:
    ...
    - currentVersion 3.1.0
      nextVersion: 3.1.0.1
      operations:
        - type: addFileUpgrader
          path: /config/engine/new-file.xml
          file: crafter/studio/upgrade/3.1.0.1/new-file.xml
    ...
```

**Note: Every version in the site pipeline must include the `versionFileUpgrader` operation.**

### Site Configuration

When the structure or content of a configuration file needs to be changed a new version needs to be added to the upgrade pipeline, unlike the previous examples configuration files have individual pipelines and the versioning schema doesn't follow the same of Crafter Studio.

If the file is not present in the configuration a new pipeline needs to be added:

```yaml
configurations:
  <name of the file>:
    path: <path of the file in the site repository>
    pipeline:
      <list of versions>
```

If the file is already present in the configuration only a new version needs to be added:

```yaml
configurations:
  role-mappings-config:
    path: &role-mappings-config '/config/studio/role-mappings-config.xml'
    pipeline:
      ...
      - currentVersion: 1.1
        nextVersion: 1.2
        operations:
          - type: xsltFileUpgrader
            path: *role-mappings-config
            template: crafter/studio/upgrade/role-mappings-config-1.2.xslt
          - type: xsltFileUpgrader
            path: *role-mappings-config
            template: crafter/studio/upgrade/update-version.xslt
      ...
```

**Note: Every version in the site pipeline must include the `xsltFileUpgrader` operation with the `update-version.xslt` template.**
