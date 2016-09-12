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
model.topNavItems = topNavItems;
