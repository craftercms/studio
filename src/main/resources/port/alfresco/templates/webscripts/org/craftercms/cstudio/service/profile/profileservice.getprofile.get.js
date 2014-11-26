 var user = args.user;
 var site = args.site;
 var valid = true;

   if (user == undefined)
   {
     status.code = 400;
     status.message = "user must be provided.";
     status.redirect = true;
     valid = false;
   }
   
   if (site == undefined)
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true
     valid = false;
   }

   if (valid) 
   {
	 model.result = profileService.getUserProfile(user, site);
   }
   
