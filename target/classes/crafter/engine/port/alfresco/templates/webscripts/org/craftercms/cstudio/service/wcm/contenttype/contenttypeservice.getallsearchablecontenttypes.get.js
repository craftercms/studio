
var site = args.site;
   var user = args.user;
      
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   } else {
   		model.items = dmContentTypeService.getAllSearchableContentTypes(site, user);
   }
