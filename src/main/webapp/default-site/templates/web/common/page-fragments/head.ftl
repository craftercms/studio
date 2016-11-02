<#include "/templates/system/common/versionInfo.ftl" />
<#if envConfig.site?? == false || envConfig.site == "">
<meta http-equiv="refresh" content="0;URL='/studio/user-dashboard/#/sites/all'" />
<#else>

<script>
    window.UIBuildId = "${UIBuildId!''}";
</script>

<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/yui/assets/skin.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/container/assets/skins/sam/container.css?version=${UIBuildId!''}"/>
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/base.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/presentation.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/contextNav.css?version=${UIBuildId!''}"/>
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css?version=${UIBuildId!''}" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/styleicon.css?version=${UIBuildId!''}" />
<!--[if IE 9]>
	<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/ie9.css" />
<![endif]-->

<script src="/studio/static-assets/yui/utilities/utilities.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/button/button-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/container/container-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/menu/menu-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/json/json-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/selector/selector-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/connection/connection-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/element/element-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/dragdrop/dragdrop-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/yahoo-dom-event/yahoo-dom-event.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/yahoo/yahoo-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/utilities/utilities.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/calendar/calendar-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/event-delegate/event-delegate-min.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/resize/resize-min.js?version=${UIBuildId!''}"></script>

<script src="/studio/static-assets/components/cstudio-common/common-api.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/jquery/dist/jquery.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/jquery-ui/jquery-ui.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/yui/bubbling.v1.5.0-min.js?version=${UIBuildId!''}" type="text/javascript"></script>


<script>document.domain = "${cookieDomain}";</script>

</#if>

<link rel="shortcut icon" href="/studio/static-assets/img/favicon.png">