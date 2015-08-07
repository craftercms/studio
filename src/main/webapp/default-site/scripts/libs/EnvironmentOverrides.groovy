package scripts.libs

import scripts.api.SiteServices;
import scripts.api.SecurityServices;

class EnvironmentOverrides {

	static getValuesForSite(appContext, request, response) {
		 
		def result = [:]
		def serverProperties = appContext.get("studio.crafter.properties")
		def cookies = request.getCookies();

    	def context = SiteServices.createContext(appContext, request)
		result.environment = serverProperties["environment"] 
		result.previewServerUrl = serverProperties["previewUrl"]
		if(result.previewServerUrl.equals("\${previewUrl}")){
			result.previewServerUrl=request.scheme+"://"+request.serverName+":"+request.serverPort;
		}
		try {		
			result.user = SecurityServices.getCurrentUser(context)
			result.site = Cookies.getCookieValue("crafterSite", request)
   		
   			def language = Cookies.getCookieValue("crafterStudioLanguage", request)
   			if(language == null || language == "" || language == "UNSET") {
   				language = "en"
   			}
			result.language = language
			
			def roles = SecurityServices.getUserRoles(context, result.site, result.user)
			  
			if(roles!=null && roles.size() > 0) {
				result.role = roles[0]
			}
			else {
				response.sendRedirect("/studio/#/sites")
			}


    		def sites = SiteServices.getUserSites(context, result.user)

			result.siteTitle = result.site +sites.size;

 
			 for(int j = 0; j < sites.size; j++) {
		        def site = sites[j];
		        if(site.siteId == result.site) {
		     		result.siteTitle = site.name;
		     		break;
		     	}
		     }

//		      result.previewServerUrl = config["preview-server-url"];
//		      result.authoringServerUrl = config["authoring-server-url"]
//		      result.formServerUrl = "NOT AVAILABLE"
//		      result.liveServerUrl = config["live-server-url"]
//		      result.publishingChannels = config["publishing-channels"]
//		      result.openSiteDropdown = config["open-site-dropdown"]
		  }
		  catch(err) {
		     result.err = err
		     throw new Exception(err)
		  }
		  
	      return result;
	}
}