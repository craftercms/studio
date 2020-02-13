package org.craftercms.studio.permissions;

public interface StudioPermissions {

    // TODO: find better way
    //  All values are lower case (configuration has mixed case)
    String ACTION_ADD_REMOTE = "add_remote";
    String ACTION_AUDIT_LOG = "audit_log";
    String ACTION_CANCEL_FAILED_PULL = "cancel_failed_pull";
    String ACTION_CANCEL_PUBLISH = "cancel_publish";
    String ACTION_CHANGE_CONTENT_TYPE = "change content type";
    String ACTION_CLONE_CONTENT_CMIS = "clone_content_cmis";
    String ACTION_COMMIT_RESOLUTION = "commit_resolution";
    String ACTION_CREATE_CONTENT = "create content";
    String ACTION_CREATE_FOLDER = "create folder";
    String ACTION_CREATE_CLUSTER = "create_cluster";
    String ACTION_CREATE_GROUPS = "create_groups";
    String ACTION_CREATE_USERS = "create_users";
    String ACTION_CREATE_SITE = "create-site";
    String ACTION_DELETE = "delete";
    String ACTION_DELETE_CLUSTER = "delete_cluster";
    String ACTION_DELETE_CONTENT = "delete_content";
    String ACTION_DELETE_GROUPS = "delete_groups";
    String ACTION_DELETE_USERS = "delete_users";
    String ACTION_EDIT_SITE = "edit_site";
    String ACTION_ENCRYPTION_TOOL = "encryption_tool";
    String ACTION_GET_CHILDREN = "get_children";
    String ACTION_GET_PUBLISHING_QUEUE = "get_publishing_queue";
    String ACTION_LIST_CMIS = "list_cmis";
    String ACTION_LIST_REMOTES = "list_remotes";
    String ACTION_PUBLISH = "publish";
    String ACTION_PULL_FROM_REMOTE = "pull_from_remote";
    String ACTION_PUSH_TO_REMOTE = "push_to_remote";
    String ACTION_READ = "read";
    String ACTION_READ_CLUSTER = "read_cluster";
    String ACTION_READ_GROUPS = "read_groups";
    String ACTION_READ_LOGS = "read_logs";
    String ACTION_READ_USERS = "read_users";
    String ACTION_REBUILD_DATABASE = "rebuild_database";
    String ACTION_REMOVE_REMOTE = "remove_remote";
    String ACTION_RESOLVE_CONFLICT = "resolve_conflict";
    String ACTION_S3_READ = "s3 read";
    String ACTION_S3_WRITE = "s3 write";
    String ACTION_SEARCH_CMIS = "search_cmis";
    String ACTION_SITE_DIFF_CONFLICTED_FILE = "site_diff_conflicted_file";
    String ACTION_SITE_STATUS = "site_status";
    String ACTION_UPDATE_CLUSTER = "update_cluster";
    String ACTION_UPDATE_GROUPS = "update_groups";
    String ACTION_UPDATE_USERS = "update_users";
    String ACTION_UPLOAD_CONTENT_CMIS = "upload_content_cmis";
    String ACTION_WEBDAV_READ = "webdav_read";
    String ACTION_WEBDAV_WRITE = "webdav_write";
    String ACTION_WRITE = "write";
    String ACTION_WRITE_CONFIGURATION = "write_configuration";
    String ACTION_WRITE_GLOBAL_CONFIGURATION = "write_global_configuration";
    String ACTION_LIST_PLUGINS = "list_plugins";
    String ACTION_INSTALL_PLUGINS = "install_plugins";
}
