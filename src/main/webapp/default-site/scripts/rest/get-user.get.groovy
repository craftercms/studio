// get alfresco sites and convert to our service response format
// also call secondary services per site to embelish each record

import groovy.json.JsonSlurper;
def crafterUser = [:];
def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco

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

  def sitesurl = alfrescoUrl + "/service/api/people?filter="+ccu+"&maxResults=1&alf_ticket="+ticket;
  
  def response = (sitesurl).toURL().getText();
  def users = new JsonSlurper().parseText( response );
  
  for(int j = 0; j < 1; j++) {
     def alfUser = users[j];
 
     crafterUser.name = alfUser.firstName;;
     crafterUser.surname = alfUser.lastName;
     crafterUser.email = ccu;
  }
  
}
catch(err) {
  crafterUser.err = err;
}

return crafterUser;
