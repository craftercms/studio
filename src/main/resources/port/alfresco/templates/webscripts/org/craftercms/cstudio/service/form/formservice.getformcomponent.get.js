  var formId = args.formId;    
  var componentName = args.componentName;    

  if (formId == undefined)
  {
     status.code = 400;
     status.message = "Form ID must be provided.";
     status.redirect = true;
  }

  if (componentName == undefined)
  {
     status.code = 400;
     status.message = "componentName must be provided.";
     status.redirect = true;
  }
  
  try {
	  model.result = cstudioFormService.loadComponentAsString(formId, componentName);
  }
  catch(err) {
     status.code = 400;
     status.message = "repository could not retrieve form component:" + err;
     status.redirect = true;
  }
