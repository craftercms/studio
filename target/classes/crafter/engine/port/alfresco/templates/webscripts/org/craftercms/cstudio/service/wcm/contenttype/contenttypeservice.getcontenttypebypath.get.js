
var site = args.site;
  var sub = args.sub;
  var path = args.path;
  var valid = true;
  
   if (site == undefined || site == '')
   {
   	 valid = false;
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   if (path == undefined || path == '')
   {
   	 valid = false;
     status.code = 400;
     status.message = "Path must be provided.";
     status.redirect = true;
   }
   if (valid) { 
		model.result = dmContentTypeService.getContentTypeByPath(site, sub, path);
   } 
