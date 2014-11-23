  var siteType = args.siteType;    
  var formId = args.form;    
  var widgetId = args.widget;    

  if (siteType == undefined)
  {
     status.code = 400;
     status.message = "Site Type must be provided.";
     status.redirect = true;
  }
  else {
	  if (widgetId == undefined)
	  {
	     status.code = 400;
	     status.message = "Widget Id must be provided.";
	     status.redirect = true;
	  } else {
	  		model.result = cstudioFormService.getWidgetConfiguration(siteType, formId, widgetId);
	  }
  }
