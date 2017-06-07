import org.craftercms.sites.editorial.SuggestionHelper

def term = params.term

def helper = new SuggestionHelper(searchService)

return helper.getSuggestions(term)
