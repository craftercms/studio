// extract parameters
var site = args.site;
var sub = args.sub;
var body = requestbody.content;

model.result = dmWorkflowService.submitToGoLive(site, sub, body);
