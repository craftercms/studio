 var site = args.site;
 var contentType = args.contentType;
 var includeDataType = args.includeDataType;
 
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (contentType == undefined || contentType == "")
	   {
	     status.code = 400;
	     status.message = "contentType name must be provided.";
	     status.redirect = true;
	   }
	   else
	   {
	   	  if (includeDataType != undefined || includeDataType == "true") {
	   	  	model.result = modelService.getModelTemplate(site, contentType, true);
	   	  } else {
	   	  	model.result = modelService.getModelTemplate(site, contentType, false);
	   	  }
	   }	  
   }  
