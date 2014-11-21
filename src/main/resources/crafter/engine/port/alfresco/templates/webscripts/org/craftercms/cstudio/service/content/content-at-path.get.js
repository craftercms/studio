//"proxyDownloadUrl: \"/proxy/alfresco/api/node/content/workspace/SpacesStore/"+ item.id + "/" + pathParts[pathParts.length-1] + "?a=true"
var contentPath = args.path;

if(!contentPath || contentPath == '') {
	status.code = 400;
	status.message = "path parameter is required";
	status.redirect = true;
}
else {
	var repoPath = "";
	var pathParts = contentPath.split("/");
	
	for(var i=0; i<pathParts.length; i++) {
		if(pathParts[i] != '') {
			repoPath += "/cm:" + pathParts[i];	
		}
	}
	
	var query = "PATH:\"/app:company_home" + repoPath + "\"";
	
	var results = search.luceneSearch(query);

	if(results.length == 0) {
		status.code = 400;
		status.message = "no results found at path: " + repoPath;
		status.redirect = true;
	}
	else if(results.length > 1) {
		status.code = 400;
		status.message = "expected 1 result but multiple (" + results.length + ") results found at path: " + repoPath;
		status.redirect = true;
	}
	else {
			
		var item = results[0];
		
		model.contentNode = item;
	}
}
