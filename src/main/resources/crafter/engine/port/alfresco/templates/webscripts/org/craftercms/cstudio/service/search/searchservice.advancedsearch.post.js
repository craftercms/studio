   // extract namespace and keyword
   var site = args.site;
   var sort = args.sort;
   var asc = args.asc;
   var num = args.num;
   var page = args.page;
   
	if (page == undefined) 
	{
   		page = 1;
	}
	if (num == undefined) {
   		num = 0; // use the default number
	}

	if (site == undefined) 
	{
		status.code = 400;
		status.message = "site, path and name must be provided.";
		status.redirect = true;
   	}
   	else
   	{
   		var isAscending = true;
   		if (asc  != undefined && asc == 'false') {
   			isAscending = false;
   		} 
   		model.result = searchService.search(site, requestbody, page, num, sort, isAscending);
                
	}
