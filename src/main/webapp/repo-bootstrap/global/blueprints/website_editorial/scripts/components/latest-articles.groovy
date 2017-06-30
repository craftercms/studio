import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

def segment = ProfileUtils.getSegment(profile, siteItemService)
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def articles = searchHelper.searchArticles(false, null, segment, 0, 3)

templateModel.articles = articles
