  var site = args.site;
  var key = args.key;
  
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (key == undefined || key == '')
	   {
	     status.code = 400;
	     status.message = "key must be provided.";
	     status.redirect = true;
	   }
	   else 
	   {
			model.message = notificationService.getGeneralMessage(site, key);
		}
	}
    
