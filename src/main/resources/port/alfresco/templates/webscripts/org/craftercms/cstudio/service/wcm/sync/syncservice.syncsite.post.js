var site = args.site;

if (site) {
    synchronizationService.synchronizeSite(site);
} else {
    status.code = 400;
    status.message = "Site must be provided";
    status.redirect = true;
}