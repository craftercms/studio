import scripts.api.ClipboardServices

def result = [:]
def site = params.site

def context = ClipboardServices.createContext(applicationContext, request)
def clipboardOp = ClipboardServices.getItems(site, context)

result.count = 0
result.site = site

if (clipboardOp != null) {
    result.count = 1 + clipboardOp.children.size()

    result.item = []

	def item = [:]
	item.uri = clipboardOp.path
	result.item.add(item)

}

return result