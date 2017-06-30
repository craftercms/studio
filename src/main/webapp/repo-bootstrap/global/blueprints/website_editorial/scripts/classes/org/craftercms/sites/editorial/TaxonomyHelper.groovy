package org.craftercms.sites.editorial

class TaxonomyHelper {

    private static String TAXONOMY_PATH = "/site/taxonomy/NAME.xml"

    protected def siteItemService

    TaxonomyHelper(siteItemService) {
        this.siteItemService = siteItemService
    }

    def getValues(String name) {
        def path = TAXONOMY_PATH.replaceFirst(/NAME/, name)
        def siteItem = siteItemService.getSiteItem(path)
        if(siteItem) {
            return siteItem.items.item.collect { it.key.text }
        }
        return []
    }

}