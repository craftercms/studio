var site= args.site;
  var path = args.path;    
  var draft = args.draft;

   if (site == undefined)
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } 
   else 
   {
	   if (path == undefined)
	   {
	     status.code = 400;
	     status.message = "Path must be provided.";
	     status.redirect = true;
	   }
	   else{
           if(draft) {
               dmPreviewService.cleanContent(site,path)
           } else{
               dmContentService.cancelEditing(site, path);
           }
	   }
   }
   
