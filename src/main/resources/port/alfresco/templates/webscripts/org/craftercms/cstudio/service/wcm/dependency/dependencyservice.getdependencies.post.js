
var site = args.site;
  var deletedep = args.deletedep;
  
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (deletedep != undefined && deletedep == "true") {
	   		model.result = dmDependencyService.getDependencies(site, requestbody,true);
  		} else {
	   		model.result = dmDependencyService.getDependencies(site, requestbody,false);
	   	}		
	}
    
