   var namespace = args.namespace;
   var create = args.create;
   
    try {
		// get the default sequence if no namespace is provided
		if (namespace == null || namespace.length == 0) {
			model.result = sequenceService.next();
		} else {
			if (create != null && create == "true") {
				model.result = sequenceService.next(namespace, true);
			} else {
				model.result = sequenceService.next(namespace, false);
			}
		}
	} catch (e) {
	     status.code = 500;
	     status.message = "An error occured while retrieving the next id. " + e.message;
	     status.redirect = true;
	}
