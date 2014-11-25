// get alfresco sites and convert to our service response format
// also call secondary services per site to embelish each record

import groovy.json.JsonSlurper;
def crafterUser = [:];
def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
def user = ""
def ticket = ""
def sitesurl ="";
def response = ""

try {

  def cookies = request.getCookies();
  
  for (int i = 0; i < cookies.length; i++) {
    def name = cookies[i].getName(); 
    def value = cookies[i].getValue();

    
    if(name == "ccu") {
      user = value;
    }
    
    if(name == "ccticket") {
      ticket = value;
    }
  }

  sitesurl = alfrescoUrl + "/service/api/people?filter="+user+"&maxResults=1&alf_ticket="+ticket
  
  response = (sitesurl).toURL().getText()
  def result = new JsonSlurper().parseText( response )
  
  def alfUser = result.people[0];
  crafterUser.name = alfUser.firstName;;
  crafterUser.surname = alfUser.lastName;
  crafterUser.email = alfUser.email;

}
catch(err) {
  crafterUser.err = err;
}

return crafterUser;
