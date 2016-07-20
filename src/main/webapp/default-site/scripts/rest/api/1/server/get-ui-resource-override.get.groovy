def resourceName = params.resource

if(resourceName == null) {
	throw new Exception("resource is a required parameter")
}

def classloader = this.getClass().getClassLoader().getParent().getParent().getParent()
def resource = classloader.getResourceAsStream("crafter/cstudio/ui/"+resourceName)
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
	return "NOT FOUND"
}
