   // extract parameters
   var user = args.user;
   var site = args.site;
   
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } else {   
	   if (user == undefined || user == "")
	   {
	     status.code = 400;
	     status.message = "User must be provided.";
	     status.redirect = true;
	   }  else {
		   model.result = permissionService.getUserRoles(site, user);
       }
  }
