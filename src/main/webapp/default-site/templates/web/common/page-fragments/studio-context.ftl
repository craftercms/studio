<#assign site = envConfig.site />
<script>

	/**
	 * contextual variables 
	 * note: these are all fixed at the moment but will be dynamic
	 */
	CStudioAuthoringContext = {
		user: "${envConfig.user!'UNSET'}",
		role: "${envConfig.role!'UNSET'}",
		site: "${envConfig.site!'UNSET'}",
		siteId: "${envConfig.site!'UNSET'}",
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "${envConfig.authoringServerUrl!'/studio'}",
		formServerUri: "${envConfig.formServerUrl!'UNSET'}",
		previewAppBaseUri: "${envConfig.previewServerUrl!'UNSET'}", 
		contextMenuOffsetPage: false,
		brandedLogoUri: "/api/1/services/api/1/content/get-content-at-path.bin?path=/configuration/app-logo.png",
		homeUri: "/site-dashboard?site=${envConfig.site!'UNSET'}",
		navContext: "default",
		cookieDomain: "${cookieDomain!'UNSET'}",
		openSiteDropdown: ${envConfig.openSiteDropdown!"false"},
		isPreview: false,
		liveAppBaseUri:"",
		lang: "${envConfig.language!'UNSET'}"
	};

   	if(CStudioAuthoringContext.role === "") {
   		document.location = CStudioAuthoringContext.baseUri;
   	}

</script>
