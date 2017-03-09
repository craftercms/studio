import org.craftercms.sites.editorial.SearchHelper

def searchHelper = new SearchHelper(searchService)
def documents = searchHelper.searchArticlesByFeatured()
def articles = []

if (documents) {
  documents.each {doc ->
    def article = [:]
    	article.title = doc.title
        article.headline = doc.headline
        article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)
        article.image = doc.image
        
	articles << article
  }
}

templateModel.articles = articles
