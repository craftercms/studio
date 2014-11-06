<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Crafter Studio</title>

    <#include "/templates/web/common/page-fragments/head.ftl" />
    <#include "/templates/web/common/page-fragments/studio-context.ftl" />

    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/en/base.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/kr/base.js"></script>

     <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/rte.css" /> 
     <script type="text/javascript" src="/studio/static-assets/modules/editors/tiny_mce/tiny_mce.js"></script>

     <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/amplify-core.js"></script>
     <script type="text/javascript" src="/studio/static-assets/components/cstudio-forms/forms-engine.js"></script> 

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
