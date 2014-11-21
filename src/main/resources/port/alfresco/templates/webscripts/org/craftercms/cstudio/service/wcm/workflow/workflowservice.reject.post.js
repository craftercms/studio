
// extract parameters
	var site = args.site;
	var sub = args.sub;
	var body = requestbody.content;

   if (site == undefined || site == "")
   {
     	status.code = 400;
     	status.message = "Site must be provided.";
     	status.redirect = true;
   } else {
		model.result = dmWorkflowService.reject(site, sub, body);
   }

