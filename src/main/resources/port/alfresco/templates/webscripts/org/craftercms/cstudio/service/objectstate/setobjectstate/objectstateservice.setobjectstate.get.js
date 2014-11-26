var site = args.site;
var path = args.path;
var state = args.state;
var systemprocessing = args.systemprocessing;

if (site == undefined) {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (path == undefined) {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
    valid = false;
}
if (state == undefined) {
    status.code = 400;
    status.message = "State must be provided.";
    status.redirect = true;
    valid = false;
}

if (systemprocessing == undefined) {
    systemprocessing = false;
} else {
    if ("true" == systemprocessing) {
        systemprocessing = true;
    } else {
        systemprocessing = false;
    }

}

model.result = objectStateService.setObjectState(site, path, state, systemprocessing);

