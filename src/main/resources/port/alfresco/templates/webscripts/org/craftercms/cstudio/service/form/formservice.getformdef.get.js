  var formId = args.formId;    

  if (formId == undefined)
  {
     status.code = 400;
     status.message = "Form ID must be provided.";
     status.redirect = true;
  }

  try {
	  model.result = cstudioFormService.loadFormAsString(formId);
  }
  catch(err) {
     status.code = 400;
     status.message = "repository could not retrieve form:" + err;
     status.redirect = true;
  }	  
