
   var site = args.site;
   var path = args.path;
   var sub = args.sub;
   var depth = args.depth;
   var order = args.order;
   
   var valid = true;
   
   if (depth == null) {
   		depth = 0;
   }
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (path == undefined || path == '')
   {
     status.code = 400;
     status.message = "Path must be provided.";
     status.redirect = true;
     valid = false;
   }
   if (valid)
   {
   		model.result = dmContentService.getPages(site, sub, path, depth, order, true);
   }
