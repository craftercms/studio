
var site = args.site;
   var path = args.path;
   
   var valid = true;
      
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (path == undefined || path == '')
   {
     status.code = 400;
     status.message = "path must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid)
   {
   		model.items = dmContentTypeService.getAllowedContentTypes(site, path);
   }
