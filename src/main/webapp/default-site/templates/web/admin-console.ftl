<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "/templates/web/common/page-fragments/head.ftl" />

    <title>Crafter Studio</title>

    <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/console.css?version=${UIBuildId!''}" />
         <script src="/studio/static-assets/modules/editors/tiny_mce/tiny_mce.js?version=${UIBuildId!''}"></script>


    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/en/base.js?version=${UIBuildId!''}"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/kr/base.js?version=${UIBuildId!''}"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/es/base.js?version=${UIBuildId!''}"></script>

    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/amplify-core.js?version=${UIBuildId!''}"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-admin/base.js?version=${UIBuildId!''}"></script>

    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <#include "/templates/web/common/page-fragments/context-nav.ftl" />

    <script src="/studio/static-assets/scripts/crafter.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/scripts/animator.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/scripts/communicator.js?version=${UIBuildId!''}" ></script>

    <script>
        CMgs = CStudioAuthoring.Messages;
        langBundle = CMgs.getBundle("contentTypes", CStudioAuthoringContext.lang);
        formsLangBundle = CMgs.getBundle("forms", CStudioAuthoringContext.lang);
    </script>

</head>

<body class="yui-skin-cstudioTheme">
<div id="admin-console" class="categories-panel-active"></div>
</body>
</html>