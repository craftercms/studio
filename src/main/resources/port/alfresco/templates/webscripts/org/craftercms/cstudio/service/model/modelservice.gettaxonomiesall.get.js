 var site = args.site;
  
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
   }
   else
   {
	 model.taxonomies = modelService.getTaxonomies(site);
   }	  
