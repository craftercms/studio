import scripts.api.ClipboardServices;

def result = [:]
def site = params.site
def session = request.session

def context = ClipboardServices.createContext(applicationContext, request)
def clipboardItem = ClipboardServices.getItem(site, session)

result.site = site
if (clipboardItem != null) {
    result.item = clipboardItem.item
    result.count = clipboardItem.item.size
}

return result