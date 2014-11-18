
   var site = args.site;
   var path = args.path;
   var sub = args.path;
   var populateDependencies = args.populateDependencies;
   var popDeps = false;
   
   var valid = true;
   
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
   if (populateDependencies == undefined) {
       popDeps = true;
   } else if (populateDependencies == "true") {
       popDeps = true;
   }
   if (valid)
   {
   		model.result = dmContentService.getItem(site, sub, path, popDeps);
   }
