import org.craftercms.sites.editorial.SearchHelper

def segment = null

if (profile) {
    segment = profile.attributes.segment
    if (segment == "unknown") {
      segment = null
    }
}

def category = contentModel.categoryToDisplay.text
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def articles = searchHelper.searchArticles(false, category, segment)

templateModel.articles = articles
