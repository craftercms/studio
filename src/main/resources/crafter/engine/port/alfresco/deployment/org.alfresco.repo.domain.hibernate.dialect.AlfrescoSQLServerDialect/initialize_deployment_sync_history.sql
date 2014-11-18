CREATE  TABLE [dbo].[cstudio_deploymentsynchistory] (
  [id] bigint IDENTITY(1,1) NOT NULL ,
  [syncdate] DATETIME NOT NULL ,
  [site] NVARCHAR(50) NOT NULL ,
  [environment] NVARCHAR(20) NOT NULL ,
  [path] NVARCHAR(4000) NOT NULL ,
  [target] NVARCHAR(50) NOT NULL ,
  [username] NVARCHAR(255) NOT NULL ,
  [contenttypeclass] NVARCHAR(25) NOT NULL ,
  CONSTRAINT [PK_cstudio_deploymentsynchistory] PRIMARY KEY CLUSTERED (id));

CREATE NONCLUSTERED INDEX [cs_depsynchist_site_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [site] );

CREATE NONCLUSTERED INDEX [cs_depsynchist_env_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [environment] );

CREATE NONCLUSTERED INDEX [cs_depsynchist_sitepath_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [site], [path] );

CREATE NONCLUSTERED INDEX [cs_depsynchist_target_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [target] );

CREATE NONCLUSTERED INDEX [cs_depsynchist_user_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [username] );

CREATE NONCLUSTERED INDEX [cs_depsynchist_ctc_idx] ON [dbo].[cstudio_deploymentsynchistory] ( [contenttypeclass] );

