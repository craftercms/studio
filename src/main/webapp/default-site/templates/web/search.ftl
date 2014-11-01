

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
      Alfresco.constants.URL_CONTEXT = "/studio/static-assets/";
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
<script type="text/javascript" src="/studio/static-assets/components/cstudio-common/common-api.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-search/search.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/default.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script> 

<link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/assets/skins/sam/calendar.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/search.css" />


<!-- Template Assets -->

   
   <script type="text/javascript" src="/studio/static-assets/yui/animation/animation-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-form/swfobject.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/search.js"></script>

  <!-- filter templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/common.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/default.js"></script>
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/javascript.js"></script>  
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/css.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/image.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/xhtml.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/flash.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/content-type.js"></script>  

  <!-- result templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/default.js"></script>
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/image.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/flash.js"></script>   
   <link href="/studio/static-assets/themes/cstudioTheme/css/icons.css" type="text/css" rel="stylesheet">
   <link href="/studio/static-assets/yui/container/assets/container.css" type="text/css" rel="stylesheet">

<!-- MSIE CSS fix overrides -->
   <!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie6.css" /><![endif]-->
   <!--[if IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie7.css" /><![endif]-->
</head>

<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
<div id="global_x002e_cstudio-search">
    <div id="global_x002e_cstudio-search_x0023_default">

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
CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();

CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
  CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
  CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
});   


  </script>

  <div id="cstudio-wcm-search-wrapper"> 
    <div id="cstudio-wcm-search-main">        
      
      <div id="cstudio-wcm-search-search-title" class="cstudio-wcm-searchResult-header"></div>
      <div id="cstudio-wcm-search-filter-controls"></div>   
      <div style="clear:both;"></div>
      <br />
      <span>Keywords (optional):</span>
      <br />
      <input type="text" name="keywords" id="cstudio-wcm-search-keyword-textbox"  value="m"/>

      <input type="hidden" id="cstudio-wcm-search-presearch"  value="true" />
            
      <input type="button" id="cstudio-wcm-search-button" value="Search">   
      <div id="cstudio-wcm-search-result-header">
        <div id="cstudio-wcm-search-result-header-container">       
          <span class="cstudio-wcm-search-result-header">Search Results</span>
          <span id="cstudio-wcm-search-message-span"></span>      
          <span id="cstudio-wcm-search-result-header-count"></span>
          <a id="cstudio-wcm-search-description-toggle-link" href="javascript:void(0)" onClick="CStudioSearch.toggleResultDetail(CStudioSearch.DETAIL_TOGGLE);"></a>
          
          <div class="filters">
            <div class="cstudio-wcm-search-result-header-pagination"> 
              Show:<input type="text" 
                    class="cstudio-wcm-search-result-header-pagination-textbox" 
                    maxlength="3" 
                    value="20"
                    id="cstudio-wcm-search-item-per-page-textbox"
                    name="total"/>
            </div>
            <div class="cstudio-wcm-search-result-header-sort">
              Sort:<select id="cstudio-wcm-search-sort-dropdown" name="sortBy">
              <!-- items added via ajax -->
              </select>
            </div>
          </div>
        </div>
      </div>      
      <div id="cstudio-wcm-search-result">
         <div id="cstudio-wcm-search-result-in-progress" class="cstudio-wcm-search-result-in-progress-img"></div>
        &nbsp;  
      </div>

      <div class="cstudio-wcm-search-pagination">
        <div id="cstudio-wcm-search-pagination-controls"></div>
      </div>
    

    </div>
  </div>  
    </div>

</div>  <div id="cstudio-command-controls"></div>
   </div>
</body>
</html>
