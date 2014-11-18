
/**
    * Get all arguments.
    * Validate.
    * Execute. 
    */
	
	
   var path = args.path;
   var site = args.site;
   var version = args.version;
   
   var valid = true;
   
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (path == undefined || path == '')
   {
     status.code = 400;
     status.message = "Path must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (version == undefined || version == '')
   {
     status.code = 400;
     status.message = "version must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (valid)
   {
		model.result = dmVersionService.revert(site, path, version);
   }
