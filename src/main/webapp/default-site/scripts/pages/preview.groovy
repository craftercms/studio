import scripts.libs.EnvironmentOverrides
import scripts.api.SecurityServices

def result = [:]
def ticket = request.getSession().getValue("alf_ticket");
def username = request.getSession().getValue("username");

def context = SecurityServices.createContext(applicationContext, request, response);
def profile = SecurityServices.getUserProfile(context, username);

//model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request)
model.userEmail = profile.email
model.userFirstName = profile.firstName
model.userLastName =  profile.lastName
model.authenticationType =  profile.authentication_type
model.cookieDomain = request.getServerName();

model.username = username
model.ticket = ticket
