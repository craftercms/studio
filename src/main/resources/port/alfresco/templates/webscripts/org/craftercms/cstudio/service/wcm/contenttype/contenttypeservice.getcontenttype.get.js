
var site = args.site;
  var type = args.type;
  
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (type == undefined || type == '')
	   {
	     status.code = 400;
	     status.message = "type must be provided.";
	     status.redirect = true;
	   } 
	   else 
	   {
			model.result = dmContentTypeService.getContentType(site, type);
	   }

	}
    
