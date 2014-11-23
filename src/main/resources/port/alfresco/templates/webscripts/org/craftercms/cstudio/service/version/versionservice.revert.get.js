   /**
    * Get all arguments.
    * Validate.
    * Execute. 
    */
	
	
   var node = args.node;
   var site = args.site;
   var version = args.version;
   var deep = args.deep;
   
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
   
   if (version == undefined || version == '')
   {
     status.code = 400;
     status.message = "version must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (deep == undefined || deep == '')
   {
     status.code = 400;
     status.message = "Deep level(true/false) must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (valid)
   {
	   var deepBool=false;
	   if(deep == 'true')
		   deepBool=true;
	   
		model.versions = dmVersionService.revert(site,node,version,deepBool);
   }
