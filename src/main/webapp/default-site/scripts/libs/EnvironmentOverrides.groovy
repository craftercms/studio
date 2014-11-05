package scripts.libs

import groovy.json.JsonSlurper
import scripts.libs.Cookies

class EnvironmentOverrides {

	static getValuesForSite(appContext, request) {
		def result = [:]
		def serverProperties = appContext.get("studio.crafter.properties")
		def cookies = request.getCookies();

		result.environment = serverProperties["environment"] // local
		result.alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
		result.cookieDomain = serverProperties["cookieDomain"] // 127.0.0.1

		 
		result.role = "author" // default

		try {		
			result.user = Cookies.getCookieValue("ccu", request)
			result.ticket = Cookies.getCookieValue("ccticket", request)
			result.site = Cookies.getCookieValue("crafterSite", request)


  			def sitesurl = result.alfrescoUrl + "/service/api/people/"+result.user+"/sites?roles=user&size=100&alf_ticket="+result.ticket;
			def sitesResponse = (sitesurl).toURL().getText();
			def sites = new JsonSlurper().parseText( sitesResponse );
			
		     for(int j = 0; j < sites.size; j++) {
		        def alfSite = sites[j];
		        if(alfSite.shortName == result.site) {
		     		result.siteTitle = alfSite.title
		     	}
		     }

			  def overridesUrl = result.alfrescoUrl + 
			  	"/service/cstudio/site/get-configuration"+
			  		"?site="+result.site+
			  		"&path=/environment-overrides/"+result.environment+"/environment-config.xml&alf_ticket="+result.ticket;
			  
			  def response = (overridesUrl).toURL().getText();
			  def config = new JsonSlurper().parseText( response );


			 def puburl = result.alfrescoUrl + "/service/api/groups/site_"+result.site+"_SiteManager/children?sortBy=displayName&maxItems=50&skipCount=0&alf_ticket="+result.ticket;
			 def pubresponse = (puburl).toURL().getText();
			 def pubgroups = new JsonSlurper().parseText( pubresponse );

			 for(int k = 0; k < pubgroups.data.size; k++) {
			   if(pubgroups.data[k].shortName == result.user) {
			   		result.role = "admin"
			   }
			 }

		      result.previewServerUrl = config["preview-server-url"];
		      result.authoringServerUrl = config["authoring-server-url"]
		      result.formServerUrl = "NOT AVAILABLE"
		      result.liveServerUrl = config["live-server-url"]
		      result.publishingChannels = config["publishing-channels"]
		      result.openSiteDropdown = config["open-site-dropdown"]
		  }
		  catch(err) {
		     result.err = err
		  }
		  
	      return result;
	}
}