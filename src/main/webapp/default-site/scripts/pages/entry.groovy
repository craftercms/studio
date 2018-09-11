import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.log.LoggerFactory
import scripts.api.SecurityServices

import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_EMAIL
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_ENABLED
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_FIRST_NAME
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_LAST_NAME
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_USERNAME

import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_HEADERS;

def logger = LoggerFactory.getLogger(this.class)

def context = SecurityServices.createContext(applicationContext, request)

def result = [:]
def currentUser = SecurityServices.getCurrentUser(context)
def email = ""
def firstname = ""
def lastname = ""
def authenticationType = ""
def profile = null

if (StringUtils.isEmpty(currentUser)) {
    def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
    def enabledString = studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_ENABLED)
    def enabled = Boolean.parseBoolean(enabledString)
    if (enabled) {
        currentUser = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_USERNAME))
        email = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_EMAIL))
        firstname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_FIRST_NAME))
        lastname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_LAST_NAME))
        authenticationType = SECURITY_AUTHENTICATION_TYPE_HEADERS
    }
}

try {
    profile = SecurityServices.getUserProfile(context, currentUser)
} catch(e) {
    profile = [:]
    profile.email = email
    profile.first_name = firstname
    profile.last_name = lastname
    profile.authentication_type = authenticationType;
}

model.username = currentUser
model.userEmail = profile.email 
model.userFirstName = profile.first_name
model.userLastName =  profile.last_name
model.authenticationType =  profile.authentication_type
model.cookieDomain = request.getServerName();
