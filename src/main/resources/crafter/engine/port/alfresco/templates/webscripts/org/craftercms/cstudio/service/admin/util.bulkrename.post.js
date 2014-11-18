var site = args.site;
var srcPath = args.srcPath;
var targetPath = args.targetPath;
if (site == undefined || site == "") {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
} else if (srcPath == undefined || srcPath == "") {
    status.code = 400;
    status.message = "Source path must be provided.";
    status.redirect = true;
} else if (targetPath == undefined || targetPath == "") {
    status.code = 400;
    status.message = "Target path must be provided.";
    status.redirect = true;
} else {
    dmContentService.renameBulk(site, null, srcPath, targetPath,true);
}

