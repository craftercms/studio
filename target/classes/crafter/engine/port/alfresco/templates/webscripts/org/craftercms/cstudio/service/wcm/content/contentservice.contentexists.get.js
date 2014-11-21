var site = args.site;
  var path = args.path;
  var originalPath=args.originalPath;
  
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (path == undefined || path == '')
	   {
	     status.code = 400;
	     status.message = "Path must be provided.";
	     status.redirect = true;
	   } 
	   else 
	   {
			if (originalPath==undefined || originalPath==null){
				originalPath="";
			}	
		   
		   model.result = dmContentService.contentExists(site, path,originalPath);
	   }

	}
    
