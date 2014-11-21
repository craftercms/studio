 var site = args.site;
 var modelName = args.modelName;
 var currentOnly = args.currentOnly;
 var elementName = args.elementName;
 var format = args.format;
 var startLevel = args.startLevel;
 var endLevel = args.endLevel;
 
   if (site == undefined || site == "")
   {
     status.code = 400;
     status.message = "site must be provided.";
     status.redirect = true;
   }
   else
   {
	   if (modelName == undefined || modelName == "")
	   {
	     status.code = 400;
	     status.message = "Model name must be provided.";
	     status.redirect = true;
	   } 
	   else 
	   {
	   	 if (currentOnly == undefined) 
	   	 {
	   	 	currentOnly = false;
	   	 }
	   	 if (format == undefined || format == "") 
	   	 {
	   	 	format = "xml"; // set the default format
	   	 }
		 model.result = modelService.getModelData(site, modelName, currentOnly, format, elementName, startLevel, endLevel);
	   }
   }  
