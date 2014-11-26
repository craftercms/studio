   var namespace = args.namespace;
	if (namespace == null || namespace.length == 0) {
	     status.code = 400;
	     status.message = "A namespace must be provided.";
	     status.redirect = true;
	} else {
		model.result = sequenceService.sequenceExists(namespace);
	}
