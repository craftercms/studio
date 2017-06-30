import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

def segment = ProfileUtils.getSegment(profile, siteItemService)
def category = contentModel.category.text
def maxArticles = contentModel.max_articles.text as Integer
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def articles = searchHelper.searchArticles(false, category, segment, 0, maxArticles)

templateModel.articles = articles
