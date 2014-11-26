var site = args.site;
var channel = args.channel;
var path = args.path;
var size = args.size;
var valid = true;

if (site == undefined || site == "") {
     status.message = "Site must be provided.";
     valid = false;
} 
if (path == undefined || path == "") {
     status.message = "Start path must be provided.";
     valid = false;
} 
if (channel == undefined || channel == "") {
     status.message = "Publishing Channel must be provided.";
     valid = false;
} 

if (valid) {
	dmImport.publishSite(site, channel, path, size);
} else {
     status.code = 400;
     status.redirect = true;
}
