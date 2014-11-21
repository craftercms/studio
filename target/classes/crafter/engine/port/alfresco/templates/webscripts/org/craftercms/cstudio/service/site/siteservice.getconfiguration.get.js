  var site = args.site;
  var path = args.path;
  var applyEnv = args.applyEnv;
  
  var valid = true; 
  
   if (path == undefined || path == '')
   {
     status.message = "Path must be provided.";
     valid = false;
   }
   if (valid)
   {
   		var applyEnvironment = false;
   		if (applyEnv != null && applyEnv == 'true') {
   			applyEnvironment = true;
   		}
		model.result = authoringSiteService.getConfiguration(site, path, applyEnvironment);
   }
   else
   {
     status.code = 400;
     status.redirect = true;
   }

    
