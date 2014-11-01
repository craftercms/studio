

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--[if IE 9]><html xmlns="http://www.w3.org/1999/xhtml" class="ie9"><![endif]-->
<!--[if gt IE 9]><!--> <html xmlns="http://www.w3.org/1999/xhtml"> <!--<![endif]-->
<head>
   <title>Crafter Studio</title>

<!-- Shortcut Icons -->
   <link rel="shortcut icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" />
   <link rel="icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" />

<!-- Site-wide YUI Assets -->
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/reset-fonts-grids/reset-fonts-grids.css" />
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
   <script type="text/javascript" src="/studio/static-assets/yui/animation/animation-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/resize/resize-min.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui//container/assets/skins/sam/container.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/assets/skins/sam/resize.css" />

<!-- Site-wide Common Assets -->
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/base.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
   <script type="text/javascript" src="/studio/static-assets/js/bubbling.v1.5.0.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/flash/AC_OETags.js"></script>
   <script type="text/javascript" src="/studio/static-assets/service/messages.js?locale=en_US"></script>
   <script type="text/javascript" src="/studio/static-assets/js/alfresco.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/forms-runtime.js"></script>
   <script type="text/javascript">
    //<![CDATA[
      Alfresco.constants.DEBUG = false;
      Alfresco.constants.PROXY_URI = window.location.protocol + "//" + window.location.host + "/studio/proxy/alfresco/";
      Alfresco.constants.PROXY_URI_RELATIVE = "/studio/proxy/alfresco/";
      Alfresco.constants.PROXY_FEED_URI = window.location.protocol + "//" + window.location.host + "/studio/proxy/alfresco-feed/";
      Alfresco.constants.THEME = "cstudioTheme";
      Alfresco.constants.URL_CONTEXT = "/studio/";
      Alfresco.constants.URL_PAGECONTEXT = "/studio//page/";
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


<!-- Template Assets -->

    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/menu/assets/skins/sam/menu.css" />
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/button/assets/skins/sam/button.css" />
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/calendar/assets/skins/sam/calendar.css" />
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/fonts/fonts-min.css" />
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/container/assets/skins/sam/container.css" />
        <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/editor/assets/skins/sam/editor.css" />

     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/rte.css" /> 
     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/rte.css" /> 
     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/skin.css" />  
     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/contextNav.css"/>


     <script type="text/javascript" src="/studio/static-assets/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/animation/animation-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/element/element-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/container/container-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/menu/menu-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/button/button-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script>
       <script type="text/javascript" src="/studio/static-assets/yui/datasource/datasource-min.js"></script>
     <script type="text/javascript" src="/studio/static-assets/modules/editors/tiny_mce/tiny_mce.js"></script>

     <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
     <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/common-api.js"></script>
     <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/amplify-core.js"></script>
     <script type="text/javascript" src="/studio/static-assets/components/cstudio-forms/forms-engine.js"></script> 

<!-- MSIE CSS fix overrides -->
   <!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie6.css" /><![endif]-->
   <!--[if IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie7.css" /><![endif]-->
</head>
   
<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
<div id="global_x002e_cstudio-form">
    <div id="global_x002e_cstudio-form_x0023_default">

  <script>
    /**
     * contextual variables 
     * note: these are all fixed at the moment but will be dynamic
     */
    CStudioAuthoringContext = {
      user: "admin",
      role: "admin", 
      site: "rosie",
      baseUri: "/studio",
      authoringAppBaseUri: "http://127.0.0.1:8080/studio",
      formServerUri: "http://127.0.0.1:8080/form-server",
      previewAppBaseUri: "http://127.0.0.1:8080",
      liveAppBaseUri: "http://rosie",
      contextMenuOffsetPage: true,
      brandedLogoUri:"/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
      homeUri: "/page/site/rosie/dashboard",
      navContext: "default",
      cookieDomain: "127.0.0.1"
    };

     YAHOO.util.Event.onDOMReady(function() {
      var formId = CStudioAuthoring.Utils.getQueryVariable(location.search, "form");
      CStudioForms.engine.render(formId, "default", "formContainer");
     });
  </script>

    </div>

</div>    <div id="formContainer"></div>
   </div>
</body>
</html>
