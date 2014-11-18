var site = args.site;
var path = args.path;
var valid = true;

if (site == undefined) {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (valid) {
    model.result = dmPublishService.getAvailablePublishingChannelGroups(site, path);
}
