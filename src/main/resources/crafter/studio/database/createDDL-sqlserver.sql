USE [crafter] ;

/****** Object:  Table [dbo].[cstudio_activity] ******/
SET ANSI_NULLS ON ;

SET QUOTED_IDENTIFIER ON ;

CREATE TABLE [dbo].[cstudio_activity](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [modified_date] [datetime] NOT NULL,
  [creation_date] [datetime] NOT NULL,
  [summary] [ntext] NOT NULL,
  [summary_format] [nvarchar](255) NOT NULL,
  [content_id] [nvarchar](2000) NOT NULL,
  [site_network] [nvarchar](255) NOT NULL,
  [activity_type] [nvarchar](255) NOT NULL,
  [content_type] [nvarchar](255) NOT NULL,
  [post_user_id] [nvarchar](255) NOT NULL,
  CONSTRAINT [PK_cstudio_activity] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [cstudio_activity_user_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_activity_user_idx] ON [dbo].[cstudio_activity]
(
  [post_user_id] ASC
) ;

/****** Object:  Index [cstudio_activity_site_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_activity_site_idx] ON [dbo].[cstudio_activity]
(
  [site_network] ASC
) ;

/****** Object:  Index [cstudio_activity_content_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_activity_content_idx] ON [dbo].[cstudio_activity]
(
  [content_id] ASC
) ;


/****** Object:  Table [dbo].[cstudio_dependency] ******/
CREATE TABLE [dbo].[cstudio_dependency](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [site] [nvarchar](255) NOT NULL,
  [source_path] [nvarchar](2000) NOT NULL,
  [target_path] [nvarchar](2000) NOT NULL,
  [type] [nvarchar](15) NOT NULL,
  CONSTRAINT [PK_cstudio_dependency] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [cstudio_dependency_site_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_dependency_site_idx] ON [dbo].[cstudio_dependency]
(
  [site] ASC
) ;

/****** Object:  Index [cstudio_dependency_sourcepath_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_dependency_sourcepath_idx] ON [dbo].[cstudio_dependency]
(
  [source_path] ASC
) ;


/****** Object:  Table [dbo].[cstudio_objectstate] ******/
CREATE TABLE [dbo].[cstudio_objectstate](
  [object_id] [nvarchar](255) NOT NULL,
  [site] [nvarchar](255) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [state] [nvarchar](255) NOT NULL,
  [system_processing] [bit] NOT NULL,
  CONSTRAINT [PK_cstudio_objectstate] PRIMARY KEY CLUSTERED
    (
      [object_id] ASC
    ),
  CONSTRAINT [uq_os_site_path] UNIQUE NONCLUSTERED
    (
      [site] ASC,
      [path] ASC
    )) ;


/****** Object:  Table [dbo].[cstudio_pagenavigationordersequence] ******/
CREATE TABLE [dbo].[cstudio_pagenavigationordersequence](
  [folder_id] [nvarchar](100) NOT NULL,
  [site] [nvarchar](50) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [max_count] [float] NOT NULL
  CONSTRAINT [PK_cstudio_pagenavigationordersequence] PRIMARY KEY CLUSTERED
    (
      [folder_id] ASC
    )
) ;

/****** Object:  Index [cstudio_pagenavigationorder_folder_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_pagenavigationorder_folder_idx] ON [dbo].[cstudio_pagenavigationordersequence]
(
  [folder_id] ASC
) ;


/****** Object:  Table [dbo].[cstudio_copytoenvironment] ******/
CREATE TABLE [dbo].[cstudio_copytoenvironment](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [site] [nvarchar](50) NOT NULL,
  [environment] [nvarchar](20) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [oldpath] [nvarchar](2000) NULL,
  [username] [nvarchar](255) NOT NULL,
  [scheduleddate] [datetime] NOT NULL,
  [state] [nvarchar](50) NOT NULL,
  [action] [nvarchar](20) NOT NULL,
  [contenttypeclass] [nvarchar](20) NULL,
  [submissioncomment] [ntext] NULL,
  CONSTRAINT [PK_cstudio_copytoenvironment] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [cstudio_cte_environment_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_cte_environment_idx] ON [dbo].[cstudio_copytoenvironment]
(
  [environment] ASC
) ;

/****** Object:  Index [cstudio_cte_path_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_cte_path_idx] ON [dbo].[cstudio_copytoenvironment]
(
  [path] ASC
) ;

/****** Object:  Index [cstudio_cte_site_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_cte_site_idx] ON [dbo].[cstudio_copytoenvironment]
(
  [site] ASC
) ;

/****** Object:  Index [cstudio_cte_sitepath_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_cte_sitepath_idx] ON [dbo].[cstudio_copytoenvironment]
(
  [site] ASC,
  [path] ASC
) ;

/****** Object:  Index [cstudio_cte_state_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_cte_state_idx] ON [dbo].[cstudio_copytoenvironment]
(
  [state] ASC
) ;


/****** Object:  Table [dbo].[cstudio_publishtotarget] ******/
CREATE TABLE [dbo].[cstudio_publishtotarget](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [site] [nvarchar](50) NOT NULL,
  [environment] [nvarchar](20) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [oldpath] [nvarchar](2000) NULL,
  [username] [nvarchar](255) NOT NULL,
  [version] [bigint] NOT NULL,
  [action] [nvarchar](20) NOT NULL,
  [contenttypeclass] [nvarchar](20) NULL,
  CONSTRAINT [PK_cstudio_publishtotarget] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [cstudio_ptt_environment_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_ptt_environment_idx] ON [dbo].[cstudio_publishtotarget]
(
  [environment] ASC
) ;

/****** Object:  Index [cstudio_ptt_path] ******/
CREATE NONCLUSTERED INDEX [cstudio_ptt_path] ON [dbo].[cstudio_publishtotarget]
(
  [path] ASC
) ;

/****** Object:  Index [cstudio_ptt_site_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_ptt_site_idx] ON [dbo].[cstudio_publishtotarget]
(
  [site] ASC
) ;

/****** Object:  Index [cstudio_ptt_sitepath_idx] ******/
CREATE NONCLUSTERED INDEX [cstudio_ptt_sitepath_idx] ON [dbo].[cstudio_publishtotarget]
(
  [site] ASC,
  [path] ASC
) ;


/****** Object:  Table [dbo].[cstudio_deploymentsynchistory] ******/
CREATE TABLE [dbo].[cstudio_deploymentsynchistory](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [syncdate] [datetime] NOT NULL,
  [site] [nvarchar](50) NOT NULL,
  [environment] [nvarchar](20) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [target] [nvarchar](50) NOT NULL,
  [username] [nvarchar](255) NOT NULL,
  [contenttypeclass] [nvarchar](20) NULL,
  CONSTRAINT [PK_cstudio_deploymentsynchistory] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [cs_depsynchist_site_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_site_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [site] ASC
) ;

/****** Object:  Index [cs_depsynchist_env_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_env_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [environment] ASC
) ;

/****** Object:  Index [cs_depsynchist_path_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_path_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [path] ASC
) ;

/****** Object:  Index [cs_depsynchist_sitepath_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_sitepath_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [site] ASC,
  [path] ASC
) ;

/****** Object:  Index [cs_depsynchist_target_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_target_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [target] ASC
) ;

/****** Object:  Index [cs_depsynchist_user_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_user_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [username] ASC
) ;

/****** Object:  Index [cs_depsynchist_ctc_idx] ******/
CREATE NONCLUSTERED INDEX [cs_depsynchist_ctc_idx] ON [dbo].[cstudio_deploymentsynchistory]
(
  [contenttypeclass] ASC
) ;


/****** Object:  Table [dbo].[cstudio_site] ******/
CREATE TABLE [dbo].[cstudio_site](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [site_id] [nvarchar](255) NOT NULL,
  [name] [nvarchar](255) NOT NULL,
  [description] [ntext] NULL,
  [status] [nvarchar](255) NULL,
  CONSTRAINT [PK_cstudio_site] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    )) ;

/****** Object:  Index [site_id_UNIQUE] ******/
CREATE UNIQUE NONCLUSTERED INDEX [site_id_UNIQUE] ON [dbo].[cstudio_site]
(
  [site_id] ASC
) ;


/****** Object:  Table [dbo].[cstudio_objectmetadata] ******/
CREATE TABLE [dbo].[cstudio_objectmetadata](
  [id] [bigint] IDENTITY(1,1) NOT NULL,
  [site] [nvarchar](50) NOT NULL,
  [path] [nvarchar](2000) NOT NULL,
  [name] [nvarchar](255) NULL,
  [modified] [datetime] NULL,
  [modifier] [nvarchar](255) NULL,
  [owner] [nvarchar](255) NULL,
  [creator] [nvarchar](255) NULL,
  [firstname] [nvarchar](255) NULL,
  [lastname] [nvarchar](255) NULL,
  [lockowner] [nvarchar](255) NULL,
  [email] [nvarchar](255) NULL,
  [renamed] [int] NULL,
  [oldurl] [ntext] NULL,
  [deleteurl] [ntext] NULL,
  [imagewidth] [int] NULL,
  [imageheight] [int] NULL,
  [approvedby] [nvarchar](255) NULL,
  [submittedby] [nvarchar](255) NULL,
  [submittedfordeletion] [int] NULL,
  [sendemail] [int] NULL,
  [submissioncomment] [ntext] NULL,
  [launchdate] [datetime] NULL,
  [commit_id] [nvarchar](50) NULL,
  CONSTRAINT [PK_cstudio_objectmetadata] PRIMARY KEY CLUSTERED
    (
      [id] ASC
    ));

/****** Object:  Index [uq__om_site_path] ******/
CREATE UNIQUE NONCLUSTERED INDEX [uq__om_site_path] ON [dbo].[cstudio_objectmetadata]
(
  [site] ASC,
  [path] ASC
) ;