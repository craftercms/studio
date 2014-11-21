var site = args.site;
var path = args.path;
var environment = args.environment;
if (site == undefined || site == "") {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
} else if (path == undefined || path == "") {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
} else if (environment == undefined || environment == "") {
    status.code = 400;
    status.message = "Publishing environment must be provided.";
    status.redirect = true;
} else {
    dmPublishService.bulkGoLive(site, environment, path);
}

