import org.apache.commons.lang3.StringUtils
import scripts.api.SecurityServices

import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_ENABLED
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_USERNAME

def context = SecurityServices.createContext(applicationContext, request)

def result = [:]
def currentUser = SecurityServices.getCurrentUser(context)

if (StringUtils.isEmpty(currentUser)) {
    def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
    def enabledString = studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_ENABLED)
    def enabled = Boolean.parseBoolean(enabledString)
    if (enabled) {
        currentUser = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_HEADERS_USERNAME))
    }
}

def profile = SecurityServices.getUserProfile(context, currentUser)

model.username = currentUser
model.userEmail = profile.email 
model.userFirstName = profile.first_name
model.userLastName =  profile.last_name
model.cookieDomain = request.getServerName();
