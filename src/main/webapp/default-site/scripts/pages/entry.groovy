import scripts.api.SecurityServices

def context = SecurityServices.createContext(applicationContext, request)

def result = [:]
def currentUser = SecurityServices.getCurrentUser(context)
def profile = SecurityServices.getUserProfile(context, currentUser)

model.username = currentUser
model.userEmail = profile.email 
model.userFirstName = profile.firstName
model.userLastName =  profile.lastName
model.cookieDomain = request.getServerName();
