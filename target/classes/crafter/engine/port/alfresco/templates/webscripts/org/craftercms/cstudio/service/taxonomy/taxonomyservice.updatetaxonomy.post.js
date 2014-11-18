   var body = requestbody.content;
   var site = args.site;
   
   if (site == undefined || site == "")
   {
   	 valid = false;
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else
   {
		taxonomyService.updateTaxonomyInstances(site, body);
   }
