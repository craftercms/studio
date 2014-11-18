CREATE TABLE [dbo].[cstudio_objectstate](
	[object_id] [nvarchar](255) NOT NULL,
	[site] [nvarchar](50) NOT NULL,
	[path] [nvarchar](2000) NOT NULL,
	[state] [nvarchar](255) NOT NULL,
	[system_processing] [bit] NOT NULL,
 CONSTRAINT [PK_cstudio_objectstate] PRIMARY KEY CLUSTERED 
(
	[object_id] ASC
)
);

CREATE INDEX [cstudio_objectstate_object_idx] ON [dbo].[cstudio_objectstate] ( [object_id] ASC );