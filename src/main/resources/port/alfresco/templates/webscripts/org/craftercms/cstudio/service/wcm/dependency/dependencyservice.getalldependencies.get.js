   var site = args.site;
   var path = args.path;
   var resultstyle = args.resultstyle
   
    try {
        if (site == undefined || site == '') {
            status.code = 400;
            status.message = "Site must be provided.";
            status.redirect = true;
            valid = false;
        }
        if (path == undefined || path == '') {
            status.code = 400;
            status.message = "Path must be provided.";
            status.redirect = true;
            valid = false;
        }
        if (resultstyle == undefined || path == '') {
            resultstyle = "json"
        }

        model.result = dmDependencyService.getAllDependencies(site, path, resultstyle);

	} catch (e) {
	     status.code = 500;
	     status.message = "An error occured while retrieving the next id. " + e.message;
	     status.redirect = true;
	}
