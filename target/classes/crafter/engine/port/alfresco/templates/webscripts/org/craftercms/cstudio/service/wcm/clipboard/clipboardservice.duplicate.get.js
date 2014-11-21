
   var site = args.site;
   var sub = args.sub;
   var path = args.path;
   
   var valid = true;
   
   if (site == undefined || site == "")
   {
     status.message = "Site must be provided.";
     valid = false;
   } 
   if (path == undefined || path == "")
   {
     status.message = "Path must be provided.";
     valid = false;
   }
   if (valid) 
   {	
   		model.result = dmClipboardService.duplicate(site, sub, path);
   } 
   else
   {
     status.code = 400;
     status.redirect = true;
   }
