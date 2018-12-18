import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.log.LoggerFactory
import scripts.api.SecurityServices
import org.apache.commons.configuration2.BaseHierarchicalConfiguration
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER

import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_HEADERS
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG

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
    def chainConfig =
            studioConfigurationSB.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG)
    def authenticationHeadersEnabled = false
    if (chainConfig != null) {
        chainConfig.stream().anyMatch { providerConfig ->
            providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE).toUpperCase().equals(AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS) && providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED)
        }
    }
    if (authenticationHeadersEnabled) {
        currentUser = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER))
        email = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER))
        firstname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER))
        lastname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER))
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
model.cookieDomain = request.getServerName()
