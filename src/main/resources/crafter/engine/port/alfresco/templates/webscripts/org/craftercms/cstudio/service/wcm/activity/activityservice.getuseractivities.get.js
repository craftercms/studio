var site = args.site;
   var user = args.user;
   var num = args.num;
   var excludeLive = args.excludeLive;
   var valid = true;
   var filterType = args.filterType;
   
   if (site == undefined) {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (user == undefined) {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid) {
   	    if (num == undefined || num == "") {
   	    	num = "10";
   	    }
   		if (excludeLive != undefined && excludeLive == "true") {
	   		model.result = dmActivityService.getActivities(site, user, num, "eventDate", false, true,filterType);
   		} else {
	   		model.result = dmActivityService.getActivities(site, user, num, "eventDate", false, false,filterType);
	   	}
   }
