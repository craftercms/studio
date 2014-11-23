 var site = args.site;
 jsonString = requestbody.content;
 
 model.result = searchService.search(site, jsonString);
