CREATE TABLE [dbo].[cstudio_pagenavigationordersequence](
	[folder_id] [nvarchar](100) NOT NULL,
	[site] [nvarchar](50) NOT NULL,
	[path] [nvarchar](255) NOT NULL,
	[max_count] [float] NOT NULL,
 CONSTRAINT [PK_cstudio_pagenavigationordersequence] PRIMARY KEY CLUSTERED 
(
	[folder_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY];

CREATE NONCLUSTERED INDEX [cstudio_pagenavigationorder_folder_idx] ON [dbo].[cstudio_pagenavigationordersequence] ( [folder_id] ASC )WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY];