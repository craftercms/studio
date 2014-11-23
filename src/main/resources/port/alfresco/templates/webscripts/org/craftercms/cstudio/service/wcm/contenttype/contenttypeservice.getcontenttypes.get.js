
  var site = args.site;
   var valid = true;
      
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid)
   {
   		model.items = dmContentTypeService.getAllContentTypes(site);
   }
