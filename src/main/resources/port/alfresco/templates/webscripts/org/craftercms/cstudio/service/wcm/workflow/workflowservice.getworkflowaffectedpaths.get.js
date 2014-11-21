
// extract parameters
var site = args.site;
var path = args.path;
var valid = true
   
if (site == undefined || site == "") {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = true;
}
if (path == undefined || path == "") {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
    valid = true;
}
if (valid) {
    model.result = dmWorkflowService.getWorkflowAffectedPaths(site, path);
}
