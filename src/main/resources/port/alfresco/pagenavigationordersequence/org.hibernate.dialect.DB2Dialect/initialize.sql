CREATE TABLE "cstudio_pagenavigationordersequence" (
  "folder_id" varchar(100) NOT NULL,
  "site" varchar(50) NOT NULL,
  "path" varchar(255) NOT NULL,
  "max_count" float NOT NULL,
  CONSTRAINT "cstudio_pagenavigationordersequence_pk" PRIMARY KEY("folder_id")
);

CREATE INDEX "cstudio_pagenavigationorder_folder_idx" ON "cstudio_pagenavigationordersequence" ("folder_id");