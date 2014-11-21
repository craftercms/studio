   // extract namespace and keyword
   var fullpath = url.extension.split("/");
   var page = args.page;
   var num = args.num;
   var sort = args.sort;
   var asc = args.asc;
   
	if (page == undefined) 
	{
   		page = 1;
	}
	if (num == undefined) {
   		num = 0; // use the default number
	}
	
   	if (fullpath.length < 2)
   	{
		status.code = 400;
		status.message = "site and keyword must be provided.";
		status.redirect = true;
   	}
   	else
   	{
   		var isAscending = true;
   		if (asc  != undefined && asc == 'false') {
   			isAscending = false;
   		} 
   		var site = fullpath[0];
   		var keyword = (fullpath.length == 2 ? fullpath[1] : "/" + fullpath.slice(1).join("/"));
   		model.result = searchService.search(site, keyword, page, num, sort, isAscending);
                
	}
