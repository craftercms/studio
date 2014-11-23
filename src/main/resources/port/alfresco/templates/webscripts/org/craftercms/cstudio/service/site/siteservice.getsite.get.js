  var key = args.key;
  var mappingKey = args.mappingKey;
  
   if (key == undefined || key == '')
   {
     status.code = 400;
     status.message = "key must be provided.";
     status.redirect = true;
   }
   else
   {
		model.result = authoringSiteService.getSiteConfig(key, mappingKey);
		
		if (model.result == null) {
		     status.code = 500;
		     status.message = "No site found by " + key + ", " + mappingKey;
		     status.redirect = true;
		}
   }

    
