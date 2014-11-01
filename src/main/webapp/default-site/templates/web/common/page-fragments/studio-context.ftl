<#assign user = cookies["username"] />
<#assign role = cookies["ccu"] />
<#assign site = RequestParameters["site"] />

<script>
	/**
	 * contextual variables 
	 * note: these are all fixed at the moment but will be dynamic
	 */
	CStudioAuthoringContext = {
		user: "${user}",
		role: "${role}", 
		site: "${site}",
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "http://127.0.0.1:8080/studio",
		formServerUri: "http://127.0.0.1:8080/form-server",
		previewAppBaseUri: "http://127.0.0.1:8080",
		contextMenuOffsetPage: false,
		brandedLogoUri: "/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
		homeUri: "/site-dashboard?site=${site}",
		navContext: "default",
		cookieDomain: "127.0.0.1",
		openSiteDropdown: false,
		isPreview: false
	};

   	if(CStudioAuthoringContext.role === "") {
   		document.location = CStudioAuthoringContext.baseUri;
   	}

CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();

CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
	CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
	CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
});		

</script>
