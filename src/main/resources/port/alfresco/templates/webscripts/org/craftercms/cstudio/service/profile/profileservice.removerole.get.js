 var user = args.user;
 var role = args.role;
 var site = args.site;
 var valid = true;

   if (user == undefined)
   {
     status.code = 400;
     status.message = "user must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (role == undefined)
   {
     status.code = 400;
     status.message = "role must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (site == undefined)
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
     valid = false;
   }

   if (valid) 
   {
	 model.result = profileService.removeUserRole(user, role,site);
   }
