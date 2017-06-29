import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

def segment = ProfileUtils.getSegment(profile, siteItemService)
def searchHelper = new SearchHelper(searchService, urlTransformationService)
// articleCategories and articlePath should be provided as additionalModel of the component and
// should be the categories of the current article
def articles = searchHelper.searchArticles(false, articleCategories, segment, 0, 3, "-localId:\"${articlePath}\"")

templateModel.articles = articles
