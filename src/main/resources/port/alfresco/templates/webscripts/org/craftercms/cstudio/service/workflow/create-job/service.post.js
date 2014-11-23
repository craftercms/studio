var createRequestStr = requestbody.content;
var createRequest = eval('(' + createRequestStr + ')');

																	
for(var j=0; j<createRequest.jobs.length; j++) {
	var job = createRequest.jobs[j];
	var paths = new java.util.ArrayList();
	var properties = new java.util.HashMap();
	
	for(var i=0; i<job.paths.length; i++){ 
		paths.add(job.paths[i]);
	}

	for(var n=0; n<job.properties.length; n++){ 
		var prop = job.properties[n];
		if(prop && prop.name) {
			var name = prop.name;
			var value = (prop.value) ? prop.value : "";
			
			properties[name] = value;
		}
	}

	var workflow = cstudioWorkflowService.createJob(createRequest.site, paths,  job.processName, properties);
}																																	