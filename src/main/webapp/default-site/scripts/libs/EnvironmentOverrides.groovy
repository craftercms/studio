package scripts.libs

import scripts.api.SiteServices;
import scripts.api.SecurityServices;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_TYPE;

class EnvironmentOverrides {
    static SITE_SERVICES_BEAN = "cstudioSiteServiceSimple"

	static getValuesForSite(appContext, request, response) {
		 
		def result = [:]
		def serverProperties = appContext.get("studio.crafter.properties")
		def cookies = request.getCookies();

    	def context = SiteServices.createContext(appContext, request)
		result.environment = serverProperties["environment"]
        try {
            def siteServiceSB = context.applicationContext.get(SITE_SERVICES_BEAN)
		    result.previewServerUrl = siteServiceSB.getPreviewServerUrl(Cookies.getCookieValue("crafterSite", request))

			result.user = SecurityServices.getCurrentUser(context)
			result.site = Cookies.getCookieValue("crafterSite", request)

            def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
            def authenticationType = studioConfigurationSB.getProperty(SECURITY_TYPE)
            result.authenticationType = authenticationType

   			def language = Cookies.getCookieValue("crafterStudioLanguage", request)
   			if(language == null || language == "" || language == "UNSET") {
   				language = "en"
   			}
			result.language = language

			if(result.user == null){
				response.sendRedirect("/studio/#/login")
			}else{
				try{
	                def roles = SecurityServices.getUserRoles(context, result.site, result.user)

	                if(roles!=null && roles.size() > 0) {
	                    if (roles.contains("admin")) {
	                        result.role = "admin"
	                    } else {
	                        result.role = roles[0]
	                    }
	                }
	                else {
	                    response.sendRedirect("/studio/#/sites?siteValidation="+result.site)
	                }
	            }catch(error){
	                response.sendRedirect("/studio/#/sites?siteValidation="+result.site)
	            }
	            def sites = SiteServices.getSitesPerUser(context, result.user, 0, 25)

				result.siteTitle = result.site +sites.size;

				for(int j = 0; j < sites.size; j++) {
			        def site = sites[j];
			        if(site.siteId == result.site) {
			     		result.siteTitle = site.name;
			     		break;
			     	}
			    }
			}
		  }
		  catch(err) {
		     result.err = err
		     throw new Exception(err)
		  }

	      return result;
	}
}
