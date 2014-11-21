
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
     status.message = "Path must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid)
   {
   		model.result = dmPreviewService.deleteContent(site, path);
   }
