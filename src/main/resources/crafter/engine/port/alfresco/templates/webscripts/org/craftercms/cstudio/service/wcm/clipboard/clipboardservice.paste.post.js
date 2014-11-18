var site = args.site;
var destination = args.destination;
var cut = args.cut;
var valid = true;

if (site == undefined || site == "") {
    status.message = "Site must be provided.";
    valid = false;
}

if (destination == undefined || destination == "") {
    status.message = "Destination must be provided.";
    valid = false;
}

if (valid) {
    model.items = dmClipboardService.paste(site, requestbody.content, destination, cut);
} else {
    status.code = 400;
    status.redirect = true;
}
