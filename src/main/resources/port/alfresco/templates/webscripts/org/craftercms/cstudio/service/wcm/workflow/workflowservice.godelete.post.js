   // extract parameters
   var site = args.site;
   var sub = args.sub;
   var body = requestbody.content;
   var user = args.user;
   if (site == undefined || site == "")
   {
     	status.code = 400;
     	status.message = "Site must be provided.";
     	status.redirect = true;
   } else {
   		model.result = dmWorkflowService.goDelete(site, sub, body,user);
   }
