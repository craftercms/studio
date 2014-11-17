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
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "${envConfig.authoringServerUrl!'UNSET'}",
		formServerUri: "${envConfig.formServerUrl!'UNSET'}",
		previewAppBaseUri: "${envConfig.previewServerUrl!'UNSET'}",
		contextMenuOffsetPage: false,
		brandedLogoUri: "/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
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
