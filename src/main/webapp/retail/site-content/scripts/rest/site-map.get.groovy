def result = [:]
def topNavItems = [:]

def siteDir = siteItemService.getSiteTree("/site/website", 2)

if(siteDir) {
    def dirs = siteDir.childItems

    dirs.each { dir ->
        	def dirName = dir.getStoreName()
			def dirItem = siteItemService.getSiteItem("/site/website/${dirName}/index.xml")
			if (dirItem != null) {
	        	def dirDisplayName = dirItem.queryValue('internal-name')
	    	   	topNavItems.put(dirName, dirDisplayName)
	    	}
   }
}

result.topNavItems = topNavItems;
result.BOP = "Google I/O Kicked Butt!";

result.colors = [];
result.colors[0] =  "blue";
result.colors[1] =   "red";
result.colors[2] =   "white";

result.demo = "mike"

return result;
