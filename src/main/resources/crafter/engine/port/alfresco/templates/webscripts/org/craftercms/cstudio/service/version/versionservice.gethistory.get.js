   /**
    * Get all arguments.
    * Validate.
    * Execute. 
    */
	
	
   var node = args.node;
   var site = args.site;
   var maxhistory = args.maxhistory;
   
   var valid = true;
   
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (node == undefined || node == '')
   {
     status.code = 400;
     status.message = "NodeRef must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (maxhistory == undefined || maxhistory == '')
   {
     status.code = 400;
     status.message = "maxhistory must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (valid)
   {
		model.versions = dmVersionService.getVersionHistory(site,node,maxhistory);
   }
