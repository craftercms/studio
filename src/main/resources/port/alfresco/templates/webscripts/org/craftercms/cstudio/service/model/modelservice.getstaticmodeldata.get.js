 var site = args.site;
 var key = args.key;
 
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
   }
   else
   {
	   if (key == undefined || key == "")
	   {
	     status.code = 400;
	     status.message = "Key must be provided.";
	     status.redirect = true;
	   } 
	   else 
	   {
		 model.result = modelService.getStaticModelData(site, key);
	   }
   }  
