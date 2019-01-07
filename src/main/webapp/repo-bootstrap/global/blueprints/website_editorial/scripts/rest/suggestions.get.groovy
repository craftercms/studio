import org.craftercms.sites.editorial.SuggestionHelper

def term = params.term

def helper = new SuggestionHelper(elasticSearch)

return helper.getSuggestions(term)
