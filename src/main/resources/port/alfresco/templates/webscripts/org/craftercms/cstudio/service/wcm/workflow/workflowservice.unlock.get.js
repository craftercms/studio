
var site = args.site;
var path = args.path;
 if (site == undefined || site == "" || path==undefined || path =="" )
   {
     status.code = 400;
     status.message = "Site,sandbox,path must be provided.";
     status.redirect = true;
   } else {
        dmWorkflowControlService.unlock(site,path);
   }
