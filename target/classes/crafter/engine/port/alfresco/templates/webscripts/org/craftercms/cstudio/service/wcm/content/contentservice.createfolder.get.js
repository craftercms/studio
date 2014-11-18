var site = args.site;
  var path = args.path;
  var name = args.name;
  
  var valid = true;
  
   if (site == undefined || site == '')
   {
     status.message = "Site must be provided.";
     valid = false;
   }
   if (path == undefined || path == '')
   {
     status.message = "Path must be provided.";
     valid = false;
   } 
   if (name == undefined || name == '')
   {
     status.message = "Folder name must be provided.";
     valid = false;
   }
   
   if (valid) { 
		var result = dmContentService.createFolder(site, path, name);
   		if (result.success == false) {
		    status.code = result.status;
		    status.message = result.message;
		    status.redirect = true;
   		} else {
   			model.result = result.item;
   		}
   } else {
     status.code = 400;
     status.redirect = true;
   } 
    
