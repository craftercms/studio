import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

def segment = ProfileUtils.getSegment(profile)
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def articles = searchHelper.searchArticles(false, null, segment, 0, 3)

templateModel.sidebarArticles = articles
