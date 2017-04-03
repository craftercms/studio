import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

logger.info(profile.toString())

def segment = ProfileUtils.getSegment(profile)
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def articles = searchHelper.searchArticles(true, null, segment)

templateModel.articles = articles
