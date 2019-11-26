package org.craftercms.studio.api.v2.dal;

public interface BaseDAO {

    /** Parameter Names */
    String PARAM_NAME_SITE_ID = "siteId";
    String PARAM_NAME_PATH = "path";

    /** Pagination parameter names */
    String PARAM_NAME_OFFSET = "offset";
    String PARAM_NAME_LIMIT = "limit";

    /** Publish Request */
    String PARAM_NAME_ENVIRONMENT = "environment";
    String PARAM_NAME_STATE = "state";
    String PARAM_NAME_PACKAGE_ID = "packageId";
    String PARAM_NAME_PACKAGE_IDS = "packageIds";
    String PARAM_NAME_CANCELLED_STATE = "cancelledState";
}
