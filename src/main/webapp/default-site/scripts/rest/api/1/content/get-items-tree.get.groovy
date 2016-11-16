import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path
def depth = params.depth ? params.depth.toInteger() : 666;

/**
 * After 11/15/16 depth was ignore and internal was
 * hardcoded to 2, this was fix ,but UI expect that min possible value is 2
 * **/
println depth
if(depth<=1){
    depth=2
}
println depth

def context = ContentServices.createContext(applicationContext, request)

result.item = ContentServices.getContentItemTree(site, path, depth, context)

return result
