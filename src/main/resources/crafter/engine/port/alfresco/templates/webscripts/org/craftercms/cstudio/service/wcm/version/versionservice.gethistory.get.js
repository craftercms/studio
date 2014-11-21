
/**
* Get all arguments.
* Validate.
* Execute.
*/


var path = args.path;
var site = args.site;
var maxhistory = args.maxhistory;
var showMinor = args.showMinor;
var showMinorBool = false;

var valid = true;

if (site == undefined || site == '')
{
 status.code = 400;
 status.message = "Site must be provided.";
 status.redirect = true;
 valid = false;
}

if (path == undefined || path == '')
{
 status.code = 400;
 status.message = "Path must be provided.";
 status.redirect = true;
 valid = false;
}

if (maxhistory == undefined || maxhistory == '')
{
 status.code = 400;
 status.message = "maxhistory must be provided.";
 status.redirect = true;
 valid = false;
}

if (showMinor != undefined && showMinor != '') {
    showMinorBool = showMinor == 'true';
}

if (valid)
{
    model.result = dmVersionService.getVersionHistory(site, path,maxhistory, showMinorBool);
}
