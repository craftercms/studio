(function() {

        var YEvent = YAHOO.util.Event;
        /**
         * contextual variables
         * note: these are all fixed at the moment but will be dynamic
         * //http://admin.cstudio.www--sandbox.127-0-0-1.ip.alfrescodemo.net:8180/downloads
         */
        CStudioAuthoringContext = {
            user: CStudioAuthoring.Utils.Cookies.readCookie("username").replace("\"","").replace("\"",""),
            site: "bestmoviesbyfarr",
      collabSandbox: "",
            baseUri: "/proxy/authoring",
            authoringAppBaseUri: "http://127.0.0.1:8080/studio",
            formServerUri: "http://127.0.0.1:8080/form-server",
            previewAppBaseUri: "http://127.0.0.1:8080",
            contextMenuOffsetPage: true,
            brandedLogoUri: "/proxy/alfresco-noauth/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
            homeUri: "/page/site/" + "rosie" + "/dashboard",
            navContext: "default",
            cookieDomain: "127.0.0.1",
            openSiteDropdown: false,
            isPreview: true,
            previewCurrentPath: "/site/website/"
        };
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
            var currentPage = "/site/website/";
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
