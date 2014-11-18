
var site = args.site;
  var path = args.path;
  var oldPath = args.oldContentPath;
  var fileName = args.fileName;
  var contentType = args.contentType;
  var createFolders = args.createFolders;
  var edit = args.edit;
  var draft = args.draft;
  var unlock = args.unlock;
  
   if (site == undefined || site == '')
   {
     status.code = 400;
     status.message = "Site must be provided.";
     status.redirect = true;
   }
   else 
   {
	   if (path == undefined || path == '')
	   {
	     status.code = 400;
	     status.message = "Path must be provided.";
	     status.redirect = true;
	   } else {
	   		// if old path is provided, it's a rename case
	   		if (oldPath != undefined && oldPath != "" && (draft==undefined || draft!=true)) {
	   			// first write content to the same location and then move the file to new location
		   		// defaulted to create missing folders in content path
		   		//if (fileName == undefined || fileName == "") {
		   			// set the file name for move cause
		   			fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length);
		   		//}
		   		// defaulted to create missing folders in content path
                dmContentService.writeContentAndRename(site, oldPath, path, fileName, contentType, requestbody, "true", edit, unlock, true);
				
			} else {
				// straight write case

				model.result = dmContentService.writeContent(site, path, fileName, contentType, requestbody, "true", edit, unlock);
			}
	   }

	}
    
