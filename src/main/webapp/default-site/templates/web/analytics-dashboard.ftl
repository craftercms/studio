<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Crafter Studio</title>

<!-- Shortcut Icons -->
   <link rel="shortcut icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" /> 
   <link rel="icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" />

<!-- Site-wide YUI Assets -->
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/skin.css" />   
<!-- Common YUI components: RELEASE -->
   <script type="text/javascript" src="/studio/static-assets/yui/utilities/utilities.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/button/button-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/container/container-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/menu/menu-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/json/json-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script> 
   <script type="text/javascript" src="/studio/static-assets/yui/connection/connection-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/element/element-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/dragdrop/dragdrop-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui//container/assets/skins/sam/container.css"/>

<!-- Site-wide Common Assets -->
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/base.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/contextNav.css"/>
   <script type="text/javascript" src="/studio/static-assets/js/bubbling.v1.5.0.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/flash/AC_OETags.js"></script>
   <script type="text/javascript" src="/studio/static-assets/service/messages.js?locale=en_US"></script>
   <script type="text/javascript" src="/studio/static-assets/js/alfresco.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/forms-runtime.js"></script>
   <script type="text/javascript">//<![CDATA[
      Alfresco.constants.DEBUG = false;
      Alfresco.constants.PROXY_URI = window.location.protocol + "//" + window.location.host + "/studio/proxy/alfresco/";
      Alfresco.constants.PROXY_URI_RELATIVE = "/studio/proxy/alfresco/";
      Alfresco.constants.PROXY_FEED_URI = window.location.protocol + "//" + window.location.host + "/studio/proxy/alfresco-feed/";
      Alfresco.constants.THEME = "cstudioTheme";
      Alfresco.constants.URL_CONTEXT = "/studio";
      Alfresco.constants.URL_PAGECONTEXT = "/studio/page/";
      Alfresco.constants.URL_SERVICECONTEXT = "/studio/service/";
      Alfresco.constants.URL_FEEDSERVICECONTEXT = "/studio/feedservice/";
      Alfresco.constants.USERNAME = "admin";
   //]]></script>
   <script type="text/javascript">//<![CDATA[
      Alfresco.constants.URI_TEMPLATES =
      {
         remote-site-page: "/site/{site}/{pageid}/p/{pagename}",
         remote-page: "/{pageid}/p/{pagename}",
         share-site-page: "/site/{site}/{pageid}/ws/{webscript}",
         sitedashboardpage: "/site/{site}/dashboard",
         contextpage: "/context/{pagecontext}/{pageid}",
         sitepage: "/site/{site}/{pageid}",
         userdashboardpage: "/user/{userid}/dashboard",
         userpage: "/user/{userid}/{pageid}",
         userprofilepage: "/user/{userid}/profile",
         userdefaultpage: "/user/{pageid}",
         consoletoolpage: "/console/{pageid}/{toolid}",
         consolepage: "/console/{pageid}",
         share-page: "/{pageid}/ws/{webscript}"
      }
   //]]></script>
   <script type="text/javascript">//<![CDATA[
      Alfresco.constants.HTML_EDITOR = 'tinyMCE';
   //]]></script>


<!-- Component Assets -->
<script type="text/javascript" src="/studio/static-assets/yui/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-common/common-api.js"></script>


<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/analytics-dashboard.js"></script>
<script type="text/javascript" src="http://www.google.com/jsapi"></script>

<script type="text/javascript">
  google.load("visualization", "1", {packages:["corechart", "geomap"]});
</script>
 


<!-- Template Assets -->
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />  
   
   <div id="hd">
<div id="global_x002e_cstudio-header">
    <div id="global_x002e_cstudio-header_x0023_default">
<script>
  /**
   * contextual variables 
   * note: these are all fixed at the moment but will be dynamic
   */
  CStudioAuthoringContext = {
    user: "admin",
    role: "admin", 
    site: "rosie",
    collabSandbox: "",
    baseUri: "/studio",
    authoringAppBaseUri: "http://127.0.0.1:8080/studio",
    formServerUri: "http://127.0.0.1:8080/form-server",
    previewAppBaseUri: "http://127.0.0.1:8080",
    contextMenuOffsetPage: false,
    brandedLogoUri: "/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
    homeUri: "/page/site/rosie/dashboard",
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
    </div>

</div>   </div>    

<!-- MSIE CSS fix overrides -->
   <!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie6.css" /><![endif]-->
   <!--[if IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie7.css" /><![endif]-->
</head>

<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
      <div id="doc3">
    <div id="bd"> 

<div id="global_x002e_cstudio-dashboard-title">
    <div id="global_x002e_cstudio-dashboard-title_x0023_default">
      <!-- dashboard title -->
    <div id="pageTitle">
      <div class="dashHeader"><h1><span>Rosie's Rivets Dashboard</span></h1></div>
      <!--
       <ul id="pageNav">
        <li>  |  </li>
        <li><a href="#">Change Site</a>  <span class="ttSortDn"></span></li>
      </ul> -->
    </div>
    <!-- end of dashboard title -->

    </div>

</div>
   <div class=" grid columnSize1">
         <div class="yui-u first column1 dcolumn">
<div id="page_x002e_component-1-1_x002e_cstudio-site-analytics">
    <div id="page_x002e_component-1-1_x002e_cstudio-site-analytics_x0023_default">
  <div id="component-1-1">
  </div>
  
  <script language="javascript">
    YAHOO.util.Event.onDOMReady(function(){
    var dashboard = new CStudioAuthoringWidgets.AnalyticsDashboard('component-1-1','cstudio-site-analytics');
    dashboard.render();
    });
  </script>


    </div>

</div><div id="unbound-region-component-1-2">
</div><div id="unbound-region-component-1-3">
</div><div id="unbound-region-component-1-4">
</div><div id="unbound-region-component-1-5">
</div><div id="unbound-region-component-1-6">
</div><div id="unbound-region-component-1-7">
</div><div id="unbound-region-component-1-8">
</div><div id="unbound-region-component-1-9">
</div><div id="unbound-region-component-1-10">
</div>         </div>
   </div>
    </div>
      </div>
      <div class="sticky-push"></div>
   </div>

   <div class="sticky-footer">
  
    <div id="ft">
<div id="global_x002e_cstudio-footer">
    <div id="global_x002e_cstudio-footer_x0023_default">
 <div id="footer" >
    <div class="floatLeft"><a href="mailto:WCMadmins@craftercms.org?subject=Problem with authoring content">Problems? Email the Administrator.</a></div>
    <div class="floatRight">&copy; 2007-2014 Crafter Software Corp. All rights reserved.</div>
 </div>

    </div>

</div>    </div>
   </div>
   <div id="alfresco-yuiloader"></div>
   <script type="text/javascript">//<![CDATA[
      Alfresco.util.YUILoaderHelper.loadComponents();
   //]]></script>
</body>
</html>
