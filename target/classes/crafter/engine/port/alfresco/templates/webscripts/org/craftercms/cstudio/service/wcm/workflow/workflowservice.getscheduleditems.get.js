   // extract parameters
   var sort = args.sort;
   var site = args.site;
   var sub = args.sub;
   var ascending = args.ascending;
   var filterType = args.filterType;
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } else {
   	   var ascending = false;
	   if (ascending != undefined && ascending == "1") {
	   		ascending = true; // 0 is descending, 1 is ascending
	   }
	   if (sort == undefined) 
	   {
	   		sort = "eventDate";
	   } 
   		 // TODO: read the sub sorting from request parameters
   		model.result = dmWorkflowService.getScheduledItems(site, sub, sort, ascending, "internalName", true,filterType);
   }
