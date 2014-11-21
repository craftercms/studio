CREATE  TABLE [dbo].[cstudio_copytoenvironment] (
  [id] bigint IDENTITY(1,1) NOT NULL,
  [site] NVARCHAR(50) NOT NULL ,
  [environment] NVARCHAR(20) NOT NULL ,
  [path] NVARCHAR(4000) NOT NULL ,
  [oldpath] NVARCHAR(4000) ,
  [username] NVARCHAR(255) ,
  [scheduleddate] DATETIME NOT NULL ,
  [state] NVARCHAR(50) NOT NULL ,
  [action] NVARCHAR(20) NOT NULL ,
  [contenttypeclass] NVARCHAR(20) ,
  CONSTRAINT [PK_cstudio_copytoenvironment] PRIMARY KEY CLUSTERED (id));

CREATE NONCLUSTERED INDEX [cstudio_cte_site_idx] ON [dbo].[cstudio_copytoenvironment] ( [site] );

CREATE NONCLUSTERED INDEX [cstudio_cte_environment_idx] ON [dbo].[cstudio_copytoenvironment] ( [environment] );

CREATE NONCLUSTERED INDEX [cstudio_cte_sitepath_idx] ON [dbo].[cstudio_copytoenvironment] ( [site], [path] );

CREATE NONCLUSTERED INDEX [cstudio_cte_state_idx] ON [dbo].[cstudio_copytoenvironment] ( [state] );


CREATE  TABLE [dbo].[cstudio_publishtotarget] (
  [id] bigint IDENTITY(1,1) NOT NULL,
  [site] NVARCHAR(50) NOT NULL ,
  [environment] NVARCHAR(20) NOT NULL ,
  [path] NVARCHAR(4000) NOT NULL ,
  [oldpath] NVARCHAR(4000) ,
  [username] NVARCHAR(255) NOT NULL ,
  [version] BIGINT NOT NULL ,
  [action] VARCHAR(20) NOT NULL ,
  [contenttypeclass] VARCHAR(20) NULL,
  CONSTRAINT [PK_cstudio_publishtotarget] PRIMARY KEY CLUSTERED (id));

CREATE NONCLUSTERED INDEX [cstudio_ptt_site_idx] ON [dbo].[cstudio_publishtotarget] ( [site] );

CREATE NONCLUSTERED INDEX [cstudio_ptt_environment_idx] ON [dbo].[cstudio_publishtotarget] ( [environment] );

CREATE NONCLUSTERED INDEX [cstudio_ptt_sitepath_idx] ON [dbo].[cstudio_publishtotarget] ( [site], [path] );
