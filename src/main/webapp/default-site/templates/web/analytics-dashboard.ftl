<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <#include "/templates/web/common/page-fragments/head.ftl" />

   <title>Crafter Studio</title>

    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/analytics-dashboard.js"></script>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />  

    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart", "geomap"]});
    </script>

    <#include "/templates/web/common/page-fragments/studio-context.ftl" />

    <#include "/templates/web/common/page-fragments/context-nav.ftl" />
</head>

<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
      <div id="doc3">
    <div id="bd"> 

<div id="global_x002e_cstudio-dashboard-title">
    <div id="global_x002e_cstudio-dashboard-title_x0023_default">
      <!-- dashboard title -->
    <div id="pageTitle">
      <div class="dashHeader"><h1><span>${envConfig.siteTitle} Dashboard</span></h1></div>
      <!--
       <ul id="pageNav">
        <li>  |  </li>
        <li><a href="#">Change Site</a>  <span class="ttSortDn"></span></li>
      </ul> -->
    </div>
    <!-- end of dashboard title -->

    </div>

</div>
   <div class=" grid columnSize1">
         <div class="yui-u first column1 dcolumn">
<div id="page_x002e_component-1-1_x002e_cstudio-site-analytics">
    <div id="page_x002e_component-1-1_x002e_cstudio-site-analytics_x0023_default">
  <div id="component-1-1">
  </div>
  
 


    </div>

</div><div id="unbound-region-component-1-2">
</div><div id="unbound-region-component-1-3">
</div><div id="unbound-region-component-1-4">
</div><div id="unbound-region-component-1-5">
</div><div id="unbound-region-component-1-6">
</div><div id="unbound-region-component-1-7">
</div><div id="unbound-region-component-1-8">
</div><div id="unbound-region-component-1-9">
</div><div id="unbound-region-component-1-10">
</div>         </div>
   </div>
    </div>
      </div>
      <div class="sticky-push"></div>
   </div>

       <#include "/templates/web/common/page-fragments/footer.ftl" />

   </div>

    <script language="javascript">
    YAHOO.util.Event.onDOMReady(function(){
    var dashboard = new CStudioAuthoringWidgets.AnalyticsDashboard('component-1-1','cstudio-site-analytics');
    dashboard.render();
    });
  </script>
</body>
</html>