def result = [:]
def imageLocations = []
def age = params["age"]
def queryStatement = "content-type:\"/component/game\" AND maxAge_i:[" + age  + " TO *]"

def query = searchService.createQuery()
query = query.setQuery(queryStatement)
query = query.addParam("sort", "maxAge_i asc")

results = searchService.search(query)

def index = 0
if(results !=null && results.response.documents[0] != null) {
	def game = searchService.search(query).response.documents[0]
    result.gameId = game.localId
	
	game = siteItemService.getSiteItem(game.localId)
        def gameCards = game.cards.elements("item")

        gameCards.each { 
        	card ->
    		def imageLocation = card.element("image").text
			
        imageLocations[index++] = imageLocation
	}
}

result.imageLocations = imageLocations


return result
return result
