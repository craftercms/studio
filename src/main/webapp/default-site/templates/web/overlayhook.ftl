
// TODO
document.domain = "${cookieDomain}";
 
requirejs.config({
    baseUrl: '/studio/static-assets/scripts',
    paths: {
        'libs': '/studio/static-assets/libs/',
        'jquery': '/studio/static-assets/libs/jquery/dist/jquery',
        'jquery-ui': '/studio/static-assets/libs/jquery-ui/jquery-ui',
        'amplify': '/studio/static-assets/libs/amplify/lib/amplify.core',
        'noty': '/studio/static-assets/libs/notify/notify.min'
    }
});

require(['guest'], function () {

    /*CStudioAuthoringContext = {
        user: "${envConfig.user!'admin'}",
        role: "${envConfig.role!'UNSET2'}",
        site: "${envConfig.site!'UNSET3'}",
        siteId: "${envConfig.site!'UNSET3'}", // same as site
        collabSandbox: "",
        baseUri: "/studio",
        authoringAppBaseUri: "${envConfig.authoringServerUrl!'UNSET4'}",
        formServerUri: "${envConfig.formServerUrl!'UNSET5'}",
        previewAppBaseUri: "${envConfig.previewServerUrl!'UNSET6'}",
        contextMenuOffsetPage: true,
        brandedLogoUri: "/proxy/authoring/api/1/services/api/1/content/get-content-at-path.bin?path=/configuration/app-logo.png",
        homeUri: "/site-dashboard?site=${envConfig.site!'UNSET7'}",
        navContext: "default",
        cookieDomain: "${envConfig.cookieDomain!'UNSET8'}",
        openSiteDropdown: ${envConfig.openSiteDropdown!'UNSET9'},
        isPreview: true,
        previewCurrentPath: "/site/website${RequestParameters['page']}/index.xml"
    };

    CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/resources/en/base.js");

    var roleCb = {
        success: function(result) { CStudioAuthoringContext.role = result.role; },
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

        CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, currentPage, {
            success: function(content) {
                CStudioAuthoring.SelectedContent.selectContent(content.item);
                // TODO this logic needs to move in to a specialization of root folder for pages
                // CStudioAuthoring.ContextualNav.WcmSiteDropdown.showPage(currentPage);
            },
            failure: function() {}
        });

    });*/

});