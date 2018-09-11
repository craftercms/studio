import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH

def resourceName = params.resource

if(resourceName == null) {
	throw new Exception("resource is a required parameter")
}

def studioConfiguration = applicationContext.get("studioConfiguration")
def classloader = this.getClass().getClassLoader().getParent().getParent().getParent()
def resource = classloader.getResourceAsStream(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH) + "/" + resourceName)
if(resource != null) {
	if( resourceName.contains(".css")
	||  resourceName.contains(".js")) {
		return resource.text 	
	}
	else{
		response.getOutputStream().write(resource.bytes) 
	}
}
else {
	response.setStatus(404)
    def instance = applicationContext.get("instanceService")

	return instance.getInstanceId() //"NOT FOUND"
}
