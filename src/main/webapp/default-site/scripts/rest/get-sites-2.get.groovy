import groovy.json.JsonSlurper;
def crafterSites = [];
def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
def urlbase = alfrescoUrl + "/service/api";

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

  def sitesurl = urlbase + "/people/"+user+"/sites?roles=user&size=100&alf_ticket="+ticket;

  def response = (sitesurl).toURL().getText();
  def sites = new JsonSlurper().parseText( response );

  for(int j = 0; j < sites.size; j++) {
     def alfSite = sites[j];
     def crafterSite = [:];
     crafterSite.name = alfSite.title;


     def diskuse = 1;

     crafterSite.id = j;
     crafterSite.status = "Running";
     crafterSite.url = "http://" + alfSite.shortName + ".com";
     
     /* send the user right to the website: */
     crafterSite.cstudioURL = "http://127.0.0.1:8080";
     
     crafterSite.siteId = alfSite.shortName;
     crafterSite.storage = [:];
     crafterSite.storage.used = diskuse*5;
     crafterSite.storage.total = 26214400;

     def puburl = urlbase + "/groups/site_"+alfSite.shortName+"_SiteManager/children?sortBy=displayName&maxItems=50&skipCount=0&alf_ticket="+ticket;
     def pubresponse = (puburl).toURL().getText();
     def pubgroups = new JsonSlurper().parseText( pubresponse );

     crafterSite.publishers = [];
     for(int k = 0; k < pubgroups.data.size; k++) {
       crafterSite.publishers[k] = [:];
       crafterSite.publishers[k].name = pubgroups.data[k].fullName;
     }

     def collaburl = urlbase + "/groups/site_"+alfSite.shortName+"_SiteCollaborator/children?sortBy=displayName&maxItems=50&skipCount=0&alf_ticket="+ticket;
     def collabresponse = (collaburl).toURL().getText();
     def collabgroups = new JsonSlurper().parseText( collabresponse );

     crafterSite.contributors = [];
     for(int l = 0; l < collabgroups.data.size; l++) {
       crafterSite.contributors[l] = [:];
       crafterSite.contributors[l].name = collabgroups.data[l].fullName;
     }

  
     crafterSites[j] = crafterSite;

   }

}
catch(err) {
  crafterSites[0] = err;
}

return crafterSites;
