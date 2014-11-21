
var site = args.site;
   var user = args.user;
   var sub = args.sub;
   var path = args.path;
   var contentType = args.contentType;
   
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
   if (contentType == undefined || contentType == '')
   {
     status.code = 400;
     status.message = "Content type must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid)
   {
   		model.items = dmContentTypeService.changeContentType(site, sub, path, contentType);
   }
