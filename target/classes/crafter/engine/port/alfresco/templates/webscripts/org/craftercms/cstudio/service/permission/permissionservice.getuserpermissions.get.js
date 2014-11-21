   // extract parameters
   var user = args.user;
   var groups = argsM.groups;
   var path = args.path;
   var site = args.site;
   
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } else {   
  	  if (path == undefined || path == "")
      {
        status.code = 400;
        status.message = "Path must be provided.";
        status.redirect = true;
      } else {
      	if (groups != null) {
	    	 /* set the authorityDisplayName as the group ID */
    		 var groupList = new java.util.ArrayList();
	    	 for (i in groups)
    		 {
	    		logger.log("Getting permissions for group: " + groups[i]);
    		    groupList.add(groups[i]);
    		 }
	    	 groups = groupList;
	     }
	     model.result = permissionService.getUserPermissions(site, path, user, groups);
      }
  }
