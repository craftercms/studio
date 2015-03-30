<#assign site = envConfig.site />
<script>

	/**
	 * contextual variables 
	 * note: these are all fixed at the moment but will be dynamic
	 */
	CStudioAuthoringContext = {
		user: "${envConfig.user!'UNSET1'}",
		role: "${envConfig.role!'UNSET2'}",
		site: "${envConfig.site!'UNSET3'}",
		siteId: "${envConfig.site!'UNSET3'}",
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "${envConfig.authoringServerUrl!'/studio'}",
		formServerUri: "${envConfig.formServerUrl!'UNSET5'}",
		previewAppBaseUri: "${envConfig.previewServerUrl!''}", <#-- TODO RETURNING UNSET. Changed to '' to be able to use preview. -->
		contextMenuOffsetPage: false,
		brandedLogoUri: "/api/1/services/api/1/content/get-content-at-path.bin?path=/cstudio/config/app-logo.png",
		homeUri: "/site-dashboard?site=${envConfig.site!'UNSET'}",
		navContext: "default",
		cookieDomain: "${envConfig.cookieDomain!'UNSET'}",
		openSiteDropdown: ${envConfig.openSiteDropdown!"false"},
		isPreview: false,
		liveAppBaseUri:"",
		lang: "en"
	};

   	if(CStudioAuthoringContext.role === "") {
   		document.location = CStudioAuthoringContext.baseUri;
   	}

</script>
