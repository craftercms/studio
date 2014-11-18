
var site = args.site;
   var days = args.days;
   var num = args.num;
   var filterType = args.filterType;
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   } 
   
   model.result = dmDeploymentService.getDeploymentHistory(site, days, num, "eventDate", false,filterType);
