<#if envConfig.site?? == false || envConfig.site == "">
   <meta http-equiv="refresh" content="0;URL='/studio/user-dashboard#/sites/all'" />
<#else> 
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
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/container/assets/skins/sam/container.css"/>

   <!-- Site-wide Common Assets -->
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/base.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/contextNav.css"/>
 
   <!-- Component Assets -->
   <script type="text/javascript" src="/studio/static-assets/yui/yahoo/yahoo-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/utilities/utilities.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/common-api.js"></script>

   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/styleicon.css" />


   <script>
      <!-- make sure child window domain -->
      document.domain = "${envConfig.cookieDomain}";
   </script>
</#if>