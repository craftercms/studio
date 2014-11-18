   var site = args.site;
   var parentpath = args.parentpath;
   
    try {
        if (site == undefined || site == '') {
            status.code = 400;
            status.message = "Site must be provided.";
            status.redirect = true;
            valid = false;
        }
        if (parentpath == undefined || parentpath == '') {
            status.code = 400;
            status.message = "Parent path must be provided.";
            status.redirect = true;
            valid = false;
        }

        model.result = pageNavOrderSequenceService.next(site, parentpath);

	} catch (e) {
	     status.code = 500;
	     status.message = "An error occured while retrieving the next id. " + e.message;
	     status.redirect = true;
	}
