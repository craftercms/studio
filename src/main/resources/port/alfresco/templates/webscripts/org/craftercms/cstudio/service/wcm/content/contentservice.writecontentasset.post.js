var content = requestbody.content;
var path = args.path;
var site = args.site;

// ensure mandatory file attributes have been located
if (site == undefined) {
    status.code = 400;
    status.message = "Site is required parameter";
    status.redirect = true;
} else if (path == undefined) {
    status.code = 400;
    status.message = "Path is required parameter";
    status.redirect = true;
} else {
    var result = dmContentService.updateContentAsset(site, path, content);
    model.result = result;
}
