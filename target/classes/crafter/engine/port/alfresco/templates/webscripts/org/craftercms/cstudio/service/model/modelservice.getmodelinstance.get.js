 var site = args.site;
 var id = args.id;
 
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (id == undefined || id == "")
	   {
	     status.code = 400;
	     status.message = "Id name must be provided.";
	     status.redirect = true;
	   }
	   else
	   {
	   	  model.result = modelService.getModelInstance(site, id);
	   }	  
   }  
