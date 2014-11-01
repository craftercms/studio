<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <#include "/templates/web/common/page-fragments/head.ftl" />

   <title>Crafter Studioc</title>

     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/rte.css" /> 
     <script type="text/javascript" src="/studio/static-assets/modules/editors/tiny_mce/tiny_mce.js"></script>

     <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/amplify-core.js"></script>
     <script type="text/javascript" src="/studio/static-assets/components/cstudio-forms/forms-engine.js"></script> 


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
    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
</head>

<body class="yui-skin-cstudioTheme">

    <script>
     YAHOO.util.Event.onDOMReady(function() {
      var formId = CStudioAuthoring.Utils.getQueryVariable(location.search, "form");
      CStudioForms.engine.render(formId, "default", "formContainer");
     });
  </script>

    <div id="formContainer"></div>

</body>
</html>
