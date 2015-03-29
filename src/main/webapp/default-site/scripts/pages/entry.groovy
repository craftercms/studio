import scripts.libs.EnvironmentOverrides
import scripts.api.SecurityServices

def result = [:]
def email = params.username;
def ticket = "";

def context = SecurityServices.createContext(applicationContext, request);
//def profile = [:]
//profile.email="x";
//profile.firstName="x"
//profile.lastName="x"
def profile = SecurityServices.getUserProfile(context, "admin");

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request)
model.userEmail = profile.email
model.userFirstName = profile.firstName
model.userLastName =  profile.lastName

try {

  def cookies = request.getCookies();

  for (int i = 0; i < cookies.length; i++) {
    def name = cookies[i].getName();
    def value = cookies[i].getValue();

    if (name == "ccticket") {
      ticket = value;
      break;
    }
  }

  model.ticket = ticket;
 // model.cookieDomain = "127.0.0.1"

} catch (err) {
  model.err = err;
}