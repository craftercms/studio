<!DOCTYPE html>
<html>
<head>

    <title>Crafter Studio</title>

    <#include "/templates/web/common/page-fragments/head.ftl" />
    <script src="/studio/static-assets/components/cstudio-common/resources/en/base.js"></script>
    <script src="/studio/static-assets/components/cstudio-common/resources/kr/base.js"></script>
    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <script>CStudioAuthoringContext.isPreview = true</script>
    <#include "/templates/web/common/page-fragments/context-nav.ftl" />

    <script>
        CMgs = CStudioAuthoring.Messages;
        langBundle = CMgs.getBundle("siteDashboard", CStudioAuthoringContext.lang);
    </script>

    <script src="/studio/static-assets/scripts/crafter.js"></script>
    <script src="/studio/static-assets/libs/amplify.js"></script>
    <script src="/studio/static-assets/scripts/communicator.js"></script>
    <script src="/studio/static-assets/scripts/host.js"></script>

</head>
<body>

<div class="studio-preview">
    <iframe id="engineWindow"></iframe>
</div>

</body>
</html>