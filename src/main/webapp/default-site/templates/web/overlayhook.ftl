(function() {
        
        var YEvent = YAHOO.util.Event;

        CStudioAuthoringContext = {
            user: "${envConfig.user}",
            role: "${envConfig.role}", 
            site: "${envConfig.site}",
            collabSandbox: "",
            baseUri: "/studio",
            authoringAppBaseUri: "${envConfig.authoringServerUrl}",
            formServerUri: "${envConfig.formServerUrl}",
            previewAppBaseUri: "${envConfig.previewServerUrl}",
            contextMenuOffsetPage: true,
            brandedLogoUri: "/proxy/authoring/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
            homeUri: "/site-dashboard?site=${envConfig.site}",
            navContext: "default",
            cookieDomain: "${envConfig.cookieDomain}",
            openSiteDropdown: ${envConfig.openSiteDropdown},
            isPreview: true,
            previewCurrentPath: "/site/website${RequestParameters['page']}/index.xml"
        };

        CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/resources/en/base.js");

      roleCb = {
          success: function(result) {
              CStudioAuthoringContext.role = result.role;
          },
          failure: function(response) {}
      };

      CStudioAuthoring.Service.lookupAuthoringRole(CStudioAuthoringContext.site, CStudioAuthoringContext.user, roleCb);
      CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
      CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();

      CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
      CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
      CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
});   

        CStudioAuthoring.Events.moduleActiveContentReady.subscribe(function() {
            var currentPage = "/site/website${RequestParameters['page']}/index.xml";
            currentPage = currentPage.replace(".html", ".xml");
            
            if(currentPage.indexOf(".xml") == -1) {
              if(currentPage.substring(currentPage.length-1) != "/") {
                currentPage += "/";             
              }
              
              currentPage += "index.xml";
            }
            
            var callback = {
                success: function(content) {
                    CStudioAuthoring.SelectedContent.selectContent(content.item);
                    // TODO this logic needs to move in to a specialization of root folder for pages
                    //CStudioAuthoring.ContextualNav.WcmSiteDropdown.showPage(currentPage);
                },
                failure: function() {}
            };
            CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, currentPage, callback);
        });

})();
