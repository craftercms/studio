  var site = args.site;
  var path = args.path;
  var order = args.order;
  var sub = args.sub;
  
  var valid = true;
  
   if (site == undefined || site == '')
   {
   	 valid = false;
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   if (path == undefined || path == '')
   {
   	 valid = false;
     status.code = 400;
     status.message = "path must be provided.";
     status.redirect = true;
   }
   if (order == undefined || order == '')
   {
   	 valid = false;
     status.code = 400;
     status.message = "order must be provided.";
     status.redirect = true;
   }
   
   if (valid)
   {
   		model.orderName = order;
		model.order = dmContentService.getOrders(site, path, sub, order);
   }

