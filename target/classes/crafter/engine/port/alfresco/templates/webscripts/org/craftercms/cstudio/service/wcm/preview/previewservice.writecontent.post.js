
var site = args.site;
  var path = args.path;
  var fileName = args.fileName;
  var contentType = args.contentType;
  
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
	   } else {
			model.result = dmPreviewService.writeContent(site, path, fileName, contentType, requestbody);
	   }

	}
    
