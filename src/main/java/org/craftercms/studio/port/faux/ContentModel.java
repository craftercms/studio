package org.alfresco.model;
import org.alfresco.service.namespace.QName;

/**
 * Content Model Constants
 */
public interface ContentModel
{
    //
    // System Model Definitions
    //
    
    // type for deleted nodes
    static final QName TYPE_DELETED = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "deleted");
    static final QName PROP_ORIGINAL_ID = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "originalId");
    
    // base type constants
    static final QName TYPE_BASE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "base");
    static final QName ASPECT_REFERENCEABLE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "referenceable");
    static final QName PROP_STORE_PROTOCOL = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store-protocol");
    static final QName PROP_STORE_IDENTIFIER = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store-identifier");
    static final QName PROP_NODE_UUID = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-uuid");
    static final QName PROP_NODE_DBID = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
    
    // tag for incomplete nodes
    static final QName ASPECT_INCOMPLETE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "incomplete");
    
    // tag for temporary nodes
    static final QName ASPECT_TEMPORARY = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "temporary");
    
    // tag for nodes being formed (CIFS)
    static final QName ASPECT_NO_CONTENT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "noContent");

    // tag for nodes being formed (WebDAV)
    static final QName ASPECT_WEBDAV_NO_CONTENT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "webdavNoContent");
    
    static final QName ASPECT_WEBDAV_OBJECT = null; // PORT.createQName(NamespaceService.WEBDAV_MODEL_1_0_URI, "object");
    static final QName PROP_DEAD_PROPERTIES = null; // PORT.createQName(NamespaceService.WEBDAV_MODEL_1_0_URI, "deadproperties");
    
    // tag for localized nodes
    static final QName ASPECT_LOCALIZED = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "localized");
    static final QName PROP_LOCALE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "locale");
    
    // tag for hidden nodes
    static final QName ASPECT_HIDDEN = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "hidden");
    static final QName PROP_VISIBILITY_MASK = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "clientVisibilityMask");
    static final QName PROP_HIDDEN_FLAG = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "hiddenFlag");
    static final QName PROP_CASCADE_HIDDEN = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeHidden");
    static final QName PROP_CASCADE_INDEX_CONTROL = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeIndexControl");
    static final QName PROP_CLIENT_CONTROLLED = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "clientControlled");

    // tag for soft delete (CIFS rename shuffle)
    static final QName ASPECT_SOFT_DELETE  = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "softDelete");
    
    // archived nodes aspect constants
    static final QName ASPECT_ARCHIVE_ROOT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveRoot");
    static final QName ASSOC_ARCHIVE_USER_LINK = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveUserLink");
    static final QName TYPE_ARCHIVE_USER = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveUser");
    static final QName ASSOC_ARCHIVED_LINK = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLink");
    static final QName ASPECT_ARCHIVED = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archived");
    static final QName PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedOriginalParentAssoc");
    static final QName PROP_ARCHIVED_BY = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedBy");
    static final QName PROP_ARCHIVED_DATE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedDate");
    static final QName PROP_ARCHIVED_ORIGINAL_OWNER = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedOriginalOwner");
    static final QName ASPECT_ARCHIVED_ASSOCS = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archived-assocs");
    static final QName PROP_ARCHIVED_PARENT_ASSOCS = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedParentAssocs");
    static final QName PROP_ARCHIVED_CHILD_ASSOCS = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedChildAssocs");
    static final QName PROP_ARCHIVED_SOURCE_ASSOCS = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedSourceAssocs");
    static final QName PROP_ARCHIVED_TARGET_ASSOCS = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedTargetAssocs");
    
    // referenceable aspect constants
    static final QName TYPE_REFERENCE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "reference");
    static final QName PROP_REFERENCE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "reference");

    // container type constants
    static final QName TYPE_CONTAINER = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "container");
    /** child association type supported by {@link #TYPE_CONTAINER} */
    static final QName ASSOC_CHILDREN =QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "children");

    // roots
    static final QName ASPECT_ROOT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "aspect_root");
    static final QName TYPE_STOREROOT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store_root");

    // for internal use only: see ALF-13066 / ALF-12358
    static final QName TYPE_LOST_AND_FOUND = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "lost_found");
    static final QName ASSOC_LOST_AND_FOUND = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "lost_found");
    
    // descriptor properties
    static final QName PROP_SYS_NAME = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "name");
    static final QName PROP_SYS_VERSION_MAJOR = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMajor");
    static final QName PROP_SYS_VERSION_MINOR = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMinor");
    static final QName PROP_SYS_VERSION_REVISION = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionRevision");
    static final QName PROP_SYS_VERSION_LABEL = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_SYS_VERSION_BUILD = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionBuild");
    static final QName PROP_SYS_VERSION_SCHEMA = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionSchema");
    static final QName PROP_SYS_VERSION_EDITION = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionEdition"); 
    static final QName PROP_SYS_VERSION_PROPERTIES = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionProperties"); 
    static final QName PROP_SYS_LICENSE_MODE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "licenseMode");
    
    /**
     * Aspect for nodes which are by default not deletable.
     * @since 3.5.0
     */
    static final QName ASPECT_UNDELETABLE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "undeletable");

    /**
     * Aspects marking nodes that are pending deletion.
     * This aspect is applied to all nodes that are about to be deleted within a transaction.
     * The aspect survives only for the duration of calls to delete nodes and their children.
     */
    static final QName ASPECT_PENDING_DELETE = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "pendingDelete");
    
    //
    // Content Model Definitions
    //
    
    // content management type constants
    static final QName TYPE_CMOBJECT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
    static final QName PROP_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name");
    
    // copy aspect constants
    static final QName ASPECT_COPIEDFROM = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copiedfrom");
    static final QName ASSOC_ORIGINAL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "original");
    
    // working copy aspect contants
    static final QName ASPECT_CHECKED_OUT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "checkedOut");
    static final QName ASSOC_WORKING_COPY_LINK = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingcopylink");
    static final QName ASPECT_WORKING_COPY = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingcopy");
    static final QName PROP_WORKING_COPY_OWNER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyOwner");
    static final QName PROP_WORKING_COPY_MODE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyMode");
    static final QName PROP_WORKING_COPY_LABEL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyLabel");
    
    // content type and aspect constants
    static final QName TYPE_CONTENT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    static final QName PROP_CONTENT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    
    // title aspect
    static final QName ASPECT_TITLED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "titled");
    static final QName PROP_TITLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "title");
    static final QName PROP_DESCRIPTION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "description");
    
    // auditable aspect
    static final QName ASPECT_AUDITABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "auditable");
    static final QName PROP_CREATED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "created");
    static final QName PROP_CREATOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "creator");
    static final QName PROP_MODIFIED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modified");
    static final QName PROP_MODIFIER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modifier");
    static final QName PROP_ACCESSED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "accessed");
    
    // author aspect
    static final QName ASPECT_AUTHOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "author");
    static final QName PROP_AUTHOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "author");
    
    // categories
    static final QName TYPE_CATEGORYROOT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "category_root");  
    static final QName ASPECT_CLASSIFIABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "classifiable");
    //static final QName ASPECT_CATEGORISATION = null; // PORT.createQName(NamespaceService.ALFRESCO_URI, "aspect_categorisation");
    static final QName ASPECT_GEN_CLASSIFIABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "generalclassifiable");
    static final QName TYPE_CATEGORY = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "category");
    static final QName PROP_CATEGORIES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categories");
    static final QName ASSOC_CATEGORIES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categories");
    static final QName ASSOC_SUBCATEGORIES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subcategories");

    // tags - a subsection of categories
    static final QName ASPECT_TAGGABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "taggable");
    static final QName PROP_TAGS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "taggable");
    
    // tagscope aspect
    static final QName ASPECT_TAGSCOPE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagscope");
    static final QName PROP_TAGSCOPE_CACHE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagScopeCache");
    static final QName PROP_TAGSCOPE_SUMMARY = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagScopeSummary");
    
    // ratings
    static final QName ASPECT_RATEABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "rateable");
    static final QName ASPECT_LIKES_RATING_SCHEME_ROLLUPS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "likesRatingSchemeRollups");
    static final QName ASPECT_FIVESTAR_RATING_SCHEME_ROLLUPS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "fiveStarRatingSchemeRollups");
    static final QName ASSOC_RATINGS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratings");
    static final QName TYPE_RATING = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "rating");
    static final QName PROP_RATING_SCORE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratingScore");
    static final QName PROP_RATING_SCHEME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratingScheme");
    static final QName PROP_RATED_AT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratedAt");
    
    // lock aspect
    public final static QName ASPECT_LOCKABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockable");
    public final static QName PROP_LOCK_OWNER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockOwner");
    public final static QName PROP_LOCK_TYPE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockType");
    public final static QName PROP_LOCK_LIFETIME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockLifetime");
    public final static QName PROP_EXPIRY_DATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "expiryDate");
    
    // version aspect
    static final QName ASPECT_VERSIONABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionable");
    static final QName PROP_VERSION_LABEL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_INITIAL_VERSION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "initialVersion");
    static final QName PROP_AUTO_VERSION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "autoVersion");
    static final QName PROP_AUTO_VERSION_PROPS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "autoVersionOnUpdateProps");
    static final QName PROP_VERSION_TYPE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionType"); 
    
    // folders
    static final QName TYPE_SYSTEM_FOLDER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "systemfolder");
    static final QName TYPE_FOLDER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "folder");
    /** child association type supported by {@link #TYPE_FOLDER} */
    static final QName ASSOC_CONTAINS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "contains");
    
    // person
    static final QName TYPE_PERSON = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "person");
    static final QName PROP_USERNAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userName");
    static final QName PROP_HOMEFOLDER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "homeFolder");
    static final QName PROP_FIRSTNAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "firstName");
    static final QName PROP_LASTNAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lastName");
    static final QName PROP_EMAIL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "email");
    static final QName PROP_ORGID = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "organizationId");
    static final QName PROP_HOME_FOLDER_PROVIDER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "homeFolderProvider");
    static final QName PROP_DEFAULT_HOME_FOLDER_PATH = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "defaultHomeFolderPath");
    static final QName PROP_PRESENCEPROVIDER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "presenceProvider");
    static final QName PROP_PRESENCEUSERNAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "presenceUsername");
    static final QName PROP_ORGANIZATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "organization");
    static final QName PROP_JOBTITLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "jobtitle");
    static final QName PROP_LOCATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "location");
    static final QName PROP_PERSONDESC = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "persondescription");
    static final QName PROP_TELEPHONE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "telephone");
    static final QName PROP_MOBILE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mobile");
    static final QName PROP_COMPANYADDRESS1 = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress1");
    static final QName PROP_COMPANYADDRESS2 = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress2");
    static final QName PROP_COMPANYADDRESS3 = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress3");
    static final QName PROP_COMPANYPOSTCODE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companypostcode");
    static final QName PROP_COMPANYTELEPHONE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companytelephone");
    static final QName PROP_COMPANYFAX = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyfax");
    static final QName PROP_COMPANYEMAIL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyemail");
    static final QName PROP_SKYPE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "skype");
    static final QName PROP_GOOGLEUSERNAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "googleusername");
    static final QName PROP_INSTANTMSG = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "instantmsg");
    static final QName PROP_USER_STATUS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userStatus");
    static final QName PROP_USER_STATUS_TIME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userStatusTime");
    
    static final QName PROP_SIZE_CURRENT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sizeCurrent"); // system-maintained
    static final QName PROP_SIZE_QUOTA = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sizeQuota");
    
    static final QName PROP_EMAIL_FEED_ID = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailFeedId"); // system-maintained
    static final QName PROP_EMAIL_FEED_DISABLED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailFeedDisabled");
    
    static final QName PROP_SUBSCRIPTIONS_PRIVATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subscriptionsPrivate");
    
    static final QName ASPECT_PERSON_DISABLED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "personDisabled");
    
    static final QName ASPECT_AnullABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "annullable");
        
    static final QName ASSOC_AVATAR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "avatar");
    
    // Authority
    static final QName TYPE_AUTHORITY = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authority");
    
    static final QName TYPE_AUTHORITY_CONTAINER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityContainer");
    static final QName PROP_AUTHORITY_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityName");
    static final QName PROP_AUTHORITY_DISPLAY_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityDisplayName");
    
    static final QName ASSOC_MEMBER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "member");

    // Zone
    static final QName TYPE_ZONE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "zone");
    static final QName ASSOC_IN_ZONE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "inZone");

    // Ownable aspect  
    static final QName ASPECT_OWNABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ownable");
    static final QName PROP_OWNER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "owner");
    
    // Templatable aspect
    static final QName ASPECT_TEMPLATABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "templatable");
    static final QName PROP_TEMPLATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "template");
    
    // Webscriptable aspect
    static final QName ASPECT_WEBSCRIPTABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "webscriptable");
    static final QName PROP_WEBSCRIPT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "webscript");
    
    // Dictionary model
    static final QName TYPE_DICTIONARY_MODEL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dictionaryModel");
    static final QName PROP_MODEL_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelName");
    static final QName PROP_MODEL_DESCRIPTION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelDescription");
    static final QName PROP_MODEL_AUTHOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelAuthor");
    static final QName PROP_MODEL_PUBLISHED_DATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelPublishedDate");
    static final QName PROP_MODEL_VERSION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelVersion");
    static final QName PROP_MODEL_ACTIVE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelActive");
    
    // referencing aspect
    static final QName ASPECT_REFERENCING = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "referencing");
    static final QName ASSOC_REFERENCES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "references");
    
    // link object
    static final QName TYPE_LINK = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "link");
    static final QName PROP_LINK_DESTINATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "destination");
    
    // attachable aspect
    static final QName ASPECT_ATTACHABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachable");
    static final QName ASSOC_ATTACHMENTS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachments");

    // emailed aspect
    static final QName ASPECT_EMAILED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailed");
    static final QName PROP_SENTDATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sentdate");
    static final QName PROP_ORIGINATOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "originator");
    static final QName PROP_ADDRESSEE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressee");
    static final QName PROP_ADDRESSEES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressees");
    static final QName PROP_SUBJECT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subjectline");
    
    // countable aspect
    static final QName ASPECT_COUNTABLE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "countable");
    static final QName PROP_HITS = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "hits");
    static final QName PROP_COUNTER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "counter");
    
    // References Node Aspect.
    static final QName ASPECT_REFERENCES_NODE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "referencesnode");
    static final QName PROP_NODE_REF = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "noderef");
    
    // Geographic Aspect.
    static final QName ASPECT_GEOGRAPHIC = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "geographic");
    static final QName PROP_LATITUDE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "latitude");
    static final QName PROP_LONGITUDE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "longitude");
    
    // Multilingual Type
    static final QName TYPE_MULTILINGUAL_CONTAINER = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlContainer");
    static final QName ASSOC_MULTILINGUAL_CHILD = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlChild");
    static final QName ASPECT_MULTILINGUAL_DOCUMENT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlDocument");
    static final QName ASPECT_MULTILINGUAL_EMPTY_TRANSLATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlEmptyTranslation");

    // Thumbnail Type
    static final QName TYPE_THUMBNAIL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnail");
    static final QName PROP_THUMBNAIL_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailName");
    static final QName PROP_CONTENT_PROPERTY_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "contentPropertyName");
    static final QName PROP_AUTOMATIC_UPDATE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "automaticUpdate");
    
    // Thumbnail modification handling
    public static final QName ASPECT_THUMBNAIL_MODIFICATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailModification");
    public static final QName PROP_LAST_THUMBNAIL_MODIFICATION_DATA = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lastThumbnailModification"); 
    
    // The below content entities can be used to manage 'failed' thumbnails. These are thumbnails that execute and fail with an
    // exception that likely means a reattempt will fail. The failedThumbnailSource aspect can be used to mark a node as
    // having tried and failed to use a particular thumbnail definition. This can then be checked and reattempts at that thumbnail
    // can be prevented or throttled.
    static final QName ASPECT_FAILED_THUMBNAIL_SOURCE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnailSource");
    static final QName ASSOC_FAILED_THUMBNAIL= null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnail");
    static final QName TYPE_FAILED_THUMBNAIL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnail");
    static final QName PROP_FAILED_THUMBNAIL_TIME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnailTime");
    static final QName PROP_FAILURE_COUNT = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failureCount");

    // Thumbnailed Aspect
    /**
     * This aspect type has been deprecated.
     * From Alfresco 3.3 the {@link RenditionModel#ASPECT_RENDITIONED rn:renditioned}
     * (which is a child of cm:thumbnailed) should be used instead.
     */
    @Deprecated
    static final QName ASPECT_THUMBNAILED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailed");
    /**
     * This association type has been deprecated.
     * From Alfresco 3.3 this association no longer exists and has been replaced with
     * {@link RenditionModel#ASSOC_RENDITION rn:rendition} association. From Alfresco
     * 3.3 onwards {@link QNamePatch a patch} is executed at startup which renames
     * the cm:thumbnails QName to rn:rendition in the database.
     * <P/>
     * This field has been updated to point to that association and references to this
     * field should be updated to use the new field.
     */
    @Deprecated
    static final QName ASSOC_THUMBNAILS = RenditionModel.ASSOC_RENDITION;
    
    // StoreSelector Aspect
    static final QName ASPECT_STORE_SELECTOR = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "storeSelector");
    static final QName PROP_STORE_NAME = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "storeName");
    
    // Preference Aspect
    static final QName ASPECT_PREFERENCES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferences");
    static final QName PROP_PREFERENCE_VALUES = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferenceValues");
    static final QName ASSOC_PREFERENCE_IMAGE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferenceImage");
    
    // Syndication Aspect
    static final QName ASPECT_SYNDICATION = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "syndication");
    static final QName PROP_PUBLISHED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "published");
    static final QName PROP_UPDATED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "updated");
    
    // Dublin core aspect
    static final QName ASPECT_DUBLINCORE = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dublincore");
    
    //
    // User Model Definitions
    //
    
    static final String USER_MODEL_URI = "http://www.alfresco.org/model/user/1.0";
    static final String USER_MODEL_PREFIX = "usr";
    
    static final QName TYPE_USER = null; // PORT.createQName(USER_MODEL_URI, "user");
    static final QName PROP_USER_USERNAME = null; // PORT.createQName(USER_MODEL_URI, "username");
    static final QName PROP_PASSWORD = null; // PORT.createQName(USER_MODEL_URI, "password");
    static final QName PROP_PASSWORD_SHA256 = null; // PORT.createQName(USER_MODEL_URI, "password2");
    static final QName PROP_ENABLED = null; // PORT.createQName(USER_MODEL_URI, "enabled");
    static final QName PROP_ACCOUNT_EXPIRES = null; // PORT.createQName(USER_MODEL_URI, "accountExpires");
    static final QName PROP_ACCOUNT_EXPIRY_DATE = null; // PORT.createQName(USER_MODEL_URI, "accountExpiryDate");
    static final QName PROP_CREDENTIALS_EXPIRE = null; // PORT.createQName(USER_MODEL_URI, "credentialsExpire");
    static final QName PROP_CREDENTIALS_EXPIRY_DATE = null; // PORT.createQName(USER_MODEL_URI, "credentialsExpiryDate");
    static final QName PROP_ACCOUNT_LOCKED = null; // PORT.createQName(USER_MODEL_URI, "accountLocked");
    static final QName PROP_SALT = null; // PORT.createQName(USER_MODEL_URI, "salt");    
    
    // 
    // Indexing control
    //
    
    static final QName ASPECT_INDEX_CONTROL = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "indexControl");
    static final QName PROP_IS_INDEXED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "isIndexed");
    static final QName PROP_IS_CONTENT_INDEXED = null; // PORT.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "isContentIndexed");
    
    // CMIS aspects
    static final QName ASPECT_CMIS_UPDATE_CONTEXT = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "CMISUpdateContext");
    static final QName PROP_GOT_FIRST_CHUNK = null; // PORT.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "gotFirstChunk");
}
