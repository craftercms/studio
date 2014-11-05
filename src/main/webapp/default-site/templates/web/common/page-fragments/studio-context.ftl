<#assign site = envConfig.site />
<script>
	/**
	 * contextual variables 
	 * note: these are all fixed at the moment but will be dynamic
	 */
	CStudioAuthoringContext = {
		user: "${envConfig.user}",
		role: "${envConfig.role}", 
		site: "${envConfig.site}",
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "${envConfig.authoringServerUrl}",
		formServerUri: "${envConfig.formServerUrl}",
		previewAppBaseUri: "${envConfig.previewServerUrl}",
		contextMenuOffsetPage: false,
		brandedLogoUri: "/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
		homeUri: "/site-dashboard?site=${envConfig.site}",
		navContext: "default",
		cookieDomain: "${envConfig.cookieDomain}",
		openSiteDropdown: ${envConfig.openSiteDropdown},
		isPreview: false,
		liveAppBaseUri:"",
		lang: "en"
	};

   	if(CStudioAuthoringContext.role === "") {
   		document.location = CStudioAuthoringContext.baseUri;
   	}
</script>
