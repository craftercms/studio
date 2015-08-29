<!doctype html>
<html class="no-js" lang="" ng-app="studio">
<head>
    <meta charset="utf-8">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, minimal-ui">
    <title>Crafter Studio</title>
    <#-- Place favicon.ico and apple-touch-icon.png in the root directory -->

    <#if userEmail??>
    <script type="application/json" id="user">{"name":"${userFirstName}","surname":"${userLastName}","email":"${userEmail}","username":"${username}" }</script>
    </#if>

    <script>var CStudioAuthoring = { cookieDomain: "${cookieDomain}" };</script>

    <link rel="stylesheet" href="/studio/static-assets/styles/main.css">

    <script src="/studio/static-assets/js/modernizr.js"></script>
    <script src="/studio/static-assets/libs/angular/angular.js"></script>
    <script src="/studio/static-assets/libs/angular-ui-router/release/angular-ui-router.js"></script>
    <script src="/studio/static-assets/libs/angular-ui-bootstrap-bower/ui-bootstrap-tpls.js"></script>
    <script src="/studio/static-assets/libs/angular-cookies/angular-cookies.js"></script>
    <script src="/studio/static-assets/libs/angular-ui-utils/ui-utils.js"></script>
    <script src="/studio/static-assets/scripts/main.js"></script>

</head>
<body class="{{$state.current.cssClass}}">

<ui-view></ui-view>

</body>
</html>
