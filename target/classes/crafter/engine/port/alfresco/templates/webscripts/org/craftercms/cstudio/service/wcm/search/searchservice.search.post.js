var site = args.site;
 var sandbox = args.sandbox;
 jsonString = requestbody.content;
 
 var isIndexingRequired = dmSearchService.isIndexingRequired(site);
 if (isIndexingRequired) {
	   dmSearchService.indexUserSandbox(site, sandbox);
 }
 model.result = dmSearchService.search(site, jsonString);
