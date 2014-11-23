
// extract parameters
 var site = args.site;
 var sub = args.sub;
 var path = args.path;
 var scheduledDate = args.scheduledDate;
 var sendEmail = args.sendEmail;

 if (scheduledDate == undefined || scheduledDate == "") {
  var isNow = true;
 } else {
  var isNow = false;
 }
 if (sendEmail == undefined || sendEmail == "") {
  	sendEmail = false;
 }
 
 var body = '{"isNow":"' + isNow + '",'
	 + '"scheduledDate":"' + scheduledDate + '",'
	 + '"sendEmail":"' + sendEmail + '",'
	 + '"items":[' 
     + '    {"uri":"' + path + '",'
 	 + '      "components":[],'
 	 + '	  "assets":[],'
 	 + '	  "children":[]'
	 + 		' }'
	 +	 	']'
     + '}'

 model.result = dmWorkflowService.submitToGoLive(site, sub, body);
