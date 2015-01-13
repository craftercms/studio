import scripts.api.ContentServices;
/*
// get post body as input stream
def content = request.getInputStream()
def site = params.site
def path = params.path

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.writeContent(site, path, content, context);
return result
*/

def result = [:];
def site = params.site;
def path = params.path;
def oldPath = params.oldContentPath;
def fileName = params.fileName;
def contentType = params.contentType;
def createFolders = params.createFolders;
def edit = params.edit;
def draft = params.draft;
def unlock = params.unlock;
def content = request.getInputStream()

/* TODO: check params
if (site == undefined || site == '')
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
}
else
{
    if (path == undefined || path == '')
    {
        status.code = 400;
        status.message = "Path must be provided.";
        status.redirect = true;
    } else {
        // if old path is provided, it's a rename case
        if (oldPath != undefined && oldPath != "" && (draft==undefined || draft!=true)) {
            // first write content to the same location and then move the file to new location
            // defaulted to create missing folders in content path
            //if (fileName == undefined || fileName == "") {
            // set the file name for move cause
            fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length);
            //}
            // defaulted to create missing folders in content path
            dmContentService.writeContentAndRename(site, oldPath, path, fileName, contentType, requestbody, "true", edit, unlock, true);

        } else {
            // straight write case

            model.result = dmContentService.writeContent(site, path, fileName, contentType, requestbody, "true", edit, unlock);
        }
    }

}*/
def context = ContentServices.createContext(applicationContext, request)
if (oldPath != null && oldPath != "" && (draft==null || draft!=true)) {
    fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length);
    result.result = ContentServices.writeContentAndRename(context, site, oldPath, path, fileName, contentType, content, "true", edit, unlock, true);

} else {
    result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content, "true", edit, unlock);
}
return result