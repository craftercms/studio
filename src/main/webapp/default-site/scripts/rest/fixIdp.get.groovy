import groovy.xml.XmlUtil

println("Getting All IDP's")
def contentService = applicationContext.getApplicationContext().getBean("cstudioContentService")//get("cstudioContentService");
def siteItem = contentService.getContentItemTree(params.site, "/site/website", 5)

class Maker {
    static checkFiles(contentService, item, site) {
        try {
            if (item.children && !item.children.isEmpty()) {
                item.children.each { child ->
                    if (child.page) {
                        def page = new XmlSlurper().parse(contentService.getContent(site, child.uri))
                        if (page.'content-type' == '/page/psc20-p-idp') {
                            def currentAuthorNotes = new String(page.description.text());
                            def currentDescription = new String(page.shortDescription.text());
                            page.appendNode {
                                authorNotes(currentAuthorNotes)
                            }
                            page.description.replaceBody(currentDescription)
                            println("Writting ${child.uri}")
                            def fileOut = new ByteArrayOutputStream()
                            XmlUtil.serialize(page, fileOut)
                            def contentStream = new ByteArrayInputStream()
                            contentService.writeContent(site, child.uri, contentStream)
                        }
                    }
                    checkFiles(contentService, contentService.getContentItem(site, child.uri), site)
                }
            }
        } catch (Throwable ex) {
            println(ex.toString())
        }
    }
}

    Maker.checkFiles ( contentService, siteItem, params.site )
    return null;
