var path = args.path;
var site = args.site;
var isSubmit = args.isSubmit;
if (site == undefined || site == "" || path == undefined || path == "")
{
     status.code = 400;
     status.message = "Site and path must be provided.";
     status.redirect = true;
}
if(isSubmit == undefined || isSubmit = "") {
    isSubmit = "false";
}
importService.importWCM(site,path,isSubmit);

