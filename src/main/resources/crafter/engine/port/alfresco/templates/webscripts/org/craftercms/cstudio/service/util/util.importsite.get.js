var config = args.config;
if (config == undefined || config == "") {
     status.code = 400;
     status.message = "config file path must be provided.";
     status.redirect = true;
} else {
	dmImport.importSite(config);
}

