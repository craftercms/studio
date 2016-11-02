<!DOCTYPE html>
<html>
<head>

    <title>Crafter Studio</title>

    <#include "/templates/web/common/page-fragments/head.ftl" />
    <script src="/studio/static-assets/components/cstudio-common/resources/en/base.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/components/cstudio-common/resources/kr/base.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/components/cstudio-common/resources/es/base.js?version=${UIBuildId!''}"></script>
    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <script>CStudioAuthoringContext.isPreview = true</script>
    <#include "/templates/web/common/page-fragments/context-nav.ftl" />

    <script>
        CMgs = CStudioAuthoring.Messages;
        langBundle = CMgs.getBundle("siteDashboard", CStudioAuthoringContext.lang);
        formsLangBundle = CMgs.getBundle("forms", CStudioAuthoringContext.lang);
        previewLangBundle = CMgs.getBundle("previewTools", CStudioAuthoringContext.lang);
        siteDropdownLangBundle = CMgs.getBundle("siteDropdown", CStudioAuthoringContext.lang);
    </script>

    <script src="/studio/static-assets/scripts/crafter.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/libs/amplify/lib/amplify.core.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/scripts/communicator.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/scripts/animator.js?version=${UIBuildId!''}"></script>
    <script src="/studio/static-assets/scripts/host.js?version=${UIBuildId!''}"></script>

</head>
<body>

<div class="studio-preview">
    <iframe id="engineWindow"></iframe>
</div>

</body>
</html>