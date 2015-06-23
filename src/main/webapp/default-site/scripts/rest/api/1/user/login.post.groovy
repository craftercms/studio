import scripts.libs.Cookies
import scripts.api.SecurityServices

def result = [:]
def username = params.username
def password = params.password

def invalidpw = false

try {
    def context = SecurityServices.createContext(applicationContext, request)
    def ticket = SecurityServices.authenticate(context, username, password)

    if(ticket != null) {
       def profile = SecurityServices.getUserProfile(context, username)
       def user = ["name":profile.firstName,"surname":profile.lastName,"email":profile.email]
       result.user = user

        result.type = "success"
        result.message = "Login successful"
      }
      else {
        result.type = "failure"
        result.message = "Incorrect login or password"        
      }
} catch(err) {
    invalidpw = true
    result.exception = err
    result.type = "error"
    result.message = "Invalid user name or password"
}

return result