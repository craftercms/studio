CREATE TABLE [dbo].[cstudio_activity](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[modified_date] [datetime] NOT NULL,
	[creation_date] [datetime] NOT NULL,
	[summary] [nvarchar](4000) NOT NULL,
	[summary_format] [nvarchar](255) NOT NULL,
	[content_id] [nvarchar](4000) NOT NULL,
	[site_network] [nvarchar](255) NOT NULL,
	[activity_type] [nvarchar](255) NOT NULL,
	[content_type] [nvarchar](255) NOT NULL,
	[post_user_id] [nvarchar](255) NOT NULL,
 CONSTRAINT [PK_cstudio_activity] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];

CREATE NONCLUSTERED INDEX [cstudio_activity_user_idx] ON [dbo].[cstudio_activity] ( [post_user_id] ASC )WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];

CREATE NONCLUSTERED INDEX [cstudio_activity_site_idx] ON [dbo].[cstudio_activity] ( [site_network] ASC )WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];

CREATE NONCLUSTERED INDEX [cstudio_activity_content_idx] ON [dbo].[cstudio_activity] ( [content_id] ASC )WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];