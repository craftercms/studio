
var filename = null;
	var content = null;
	var site = "";
	var path = "";
	var isImage = false;
	var allowedWidth = "";
	var allowedHeight = "";
	var allowLessSize = "";
	var changeCase = "";
	var draft = args.draft;
	var unlock = args.unlock;
	var systemAsset = args.systemAsset;
	
	// locate file attributes
	for each (field in formdata.fields)
	{
		if (field.name == "site")
		{
			site = field.value;
		}
		if (field.name == "path")
		{
			path = field.value;
		}
		if (field.name == "isImage")
		{
			isImage = field.value;
		}
		if (field.name == "allowedWidth")
		{
			allowedWidth = field.value;
		}
		if (field.name == "allowedHeight")
		{
			allowedHeight = field.value;
		}
		if (field.name == "allowLessSize")
		{
			allowLessSize = field.value;
		}
		if (field.name == "changeCase")
		{
			changeCase = field.value;
		}
		else if (field.isFile)
		{
			filename = field.filename;
			content = field.content;
		}
	}
	// ensure mandatory file attributes have been located
	if (filename == undefined || content == undefined)
	{
		status.code = 400;
		status.message = "Uploaded file cannot be located in request";
		status.redirect = true;
	}
	else
	{
        if(changeCase == 'true') {
            filename = filename.toLowerCase();
        }
		var result = dmContentService.writeContentAsset(site, path, filename, content,
				isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
		var siteConfigStr = authoringSiteService.getSiteConfig(site, null);
		var siteConfig = eval("(" + siteConfigStr + ")");
        model.cookieDomain = siteConfig.site.cookieDomain;
		model.result = result;
	}
