
// extract parameters
   var sort = args.sort;
   var site = args.site;
   var sub = args.sub;
   var ascending = args.ascending;
   var inProgressOnly = args.inProgressOnly;
   var includeInProgress = args.includeInProgress;
   
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } else {
	   if (ascending == undefined) {
	   		ascending = "false"; // 0 is descending, 1 is ascending
	   }
	   if (sort == undefined) 
	   {
	   		sort = "eventDate";
	   } 
   
       if ((inProgressOnly != undefined && inProgressOnly == 'true') ||
       		(includeInProgress != undefined && includeInProgress == 'true')) {
   			model.result = dmWorkflowService.getInProgressItems(site, sub, sort, ascending, inProgressOnly);
   	   } else {
   			model.result = dmWorkflowService.getGoLiveItems(site, sub, sort, ascending);
   	   }
   }
