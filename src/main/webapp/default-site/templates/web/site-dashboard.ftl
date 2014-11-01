









<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Crafter Studio</title>

<!-- Shortcut Icons -->
   <link rel="shortcut icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" /> 
   <link rel="icon" href="/studio/static-assets/favicon.ico" type="image/vnd.microsoft.icon" />

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
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui//container/assets/skins/sam/container.css"/>

<!-- Site-wide Common Assets -->
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/base.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/presentation.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/contextNav.css"/>
   <script type="text/javascript" src="/studio/static-assets/js/bubbling.v1.5.0.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/flash/AC_OETags.js"></script>
   <script type="text/javascript" src="/studio/static-assets/service/messages.js?locale=en_US"></script>
   <script type="text/javascript" src="/studio/static-assets/js/alfresco.js"></script>
   <script type="text/javascript" src="/studio/static-assets/js/forms-runtime.js"></script>
   <script type="text/javascript">//<![CDATA[
      Alfresco.constants.DEBUG = false;
      Alfresco.constants.PROXY_URI = window.location.protocol + "//" + window.location.host + "/studio/static-assets/proxy/alfresco/";
      Alfresco.constants.PROXY_URI_RELATIVE = "/studio/static-assets/proxy/alfresco/";
      Alfresco.constants.PROXY_FEED_URI = window.location.protocol + "//" + window.location.host + "/studio/static-assets/proxy/alfresco-feed/";
      Alfresco.constants.THEME = "cstudioTheme";
      Alfresco.constants.URL_CONTEXT = "/studio/static-assets/";
      Alfresco.constants.URL_PAGECONTEXT = "/studio/static-assets/page/";
      Alfresco.constants.URL_SERVICECONTEXT = "/studio/static-assets/service/";
      Alfresco.constants.URL_FEEDSERVICECONTEXT = "/studio/static-assets/feedservice/";
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
<script type="text/javascript" src="/studio/static-assets/yui/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-common/common-api.js"></script>

<script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>

<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/go-live-queue.js"></script>


<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />

<script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>

<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/approved-scheduled-items.js"></script>


<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />

<script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>

<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/recently-made-live.js"></script>


<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />

<script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>

<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/my-recent-activity.js"></script>


<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />

<script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/selector/selector-min.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/event-delegate/event-delegate-min.js"></script>

<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/my-notifications.js"></script>


<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/icon-guide.js"></script>

<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/styleicon.css" />


<!-- Template Assets -->
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard.css" />
   <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/dashboard-presentation.css" />  
   
   <div id="hd">
<div id="page_x002e_cstudioHeader_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_cstudioHeader_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<script>
	/**
	 * contextual variables 
	 * note: these are all fixed at the moment but will be dynamic
	 */
	CStudioAuthoringContext = {
		user: "admin",
		role: "admin", 
		site: "rosie",
		collabSandbox: "",
		baseUri: "/studio",
		authoringAppBaseUri: "http://127.0.0.1:8080/studio",
		formServerUri: "http://127.0.0.1:8080/form-server",
		previewAppBaseUri: "http://127.0.0.1:8080",
		contextMenuOffsetPage: false,
		brandedLogoUri: "/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png",
		homeUri: "/page/site/rosie/dashboard",
		navContext: "default",
		cookieDomain: "127.0.0.1",
		openSiteDropdown: false,
		isPreview: false
	};

   	if(CStudioAuthoringContext.role === "") {
   		document.location = CStudioAuthoringContext.baseUri;
   	}

CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();

CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
	CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
	CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
});		

</script>
    </div>

</div>   </div>    

<!-- MSIE CSS fix overrides -->
   <!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie6.css" /><![endif]-->
   <!--[if IE 7]><link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/ie7.css" /><![endif]-->
</head>

<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
      <div id="doc3">
    <div id="bd"> 

<div id="page_x002e_title_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_title_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
      <!-- dashboard title -->
		<div id="pageTitle">
			<div class="dashHeader"><h1><span>Rosie's Rivets Dashboard</span></h1></div>
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
<div id="page_x002e_component-1-1_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-1_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<div id="GoLiveQueue" class="ttTableGroup">
    <div class="ttHdr">
        <div class="ttWidgetHdr">
            <span class="ttClose" style="cursor:pointer;" id="widget-toggle-GoLiveQueue"
                  onclick="return WcmDashboardWidgetCommon.toggleWidget('GoLiveQueue','site/rosie/dashboard');"></span>
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('GoLiveQueue','site/rosie/dashboard');">Go Live Queue</span>
            (<span class='cstudio-dash-totalcount' id='GoLiveQueue-total-count'></span>)
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-GoLiveQueue" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('GoLiveQueue');">Collapse All</a>
            </li>
        </ul>

        <!-- TODO Sajan please change this to classes with generic names .. what wwere they thinking?! -->
    </div>


    <div id="sortedBy-GoLiveQueue" style="display:none"></div>
    <div id="sort-type-GoLiveQueue" style="display:none"></div>

    <div id="GoLiveQueue-body" style="display:none">

    </div>
</div>

<script language="javascript">
	new CStudioAuthoringWidgets.GoLiveQueueDashboard('GoLiveQueue','site/rosie/dashboard');
</script>
    </div>

</div><div id="page_x002e_component-1-2_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-2_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<div id="approvedScheduledItems" class="ttTableGroup">
    <div class="ttHdr">
        <div class="ttWidgetHdr">
            <span class="ttClose" style="cursor:pointer;" id="widget-toggle-approvedScheduledItems"
                  onclick="return WcmDashboardWidgetCommon.toggleWidget('approvedScheduledItems','site/rosie/dashboard');"></span>
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('approvedScheduledItems','site/rosie/dashboard');">Approved Scheduled Items</span>
            (<span class='cstudio-dash-totalcount' id='approvedScheduledItems-total-count'></span>)
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-approvedScheduledItems" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('approvedScheduledItems');">Collapse All</a>
            </li>
        </ul>

        <!-- TODO Sajan please change this to classes with generic names .. what wwere they thinking?! -->
    </div>


    <div id="sortedBy-approvedScheduledItems" style="display:none"></div>
    <div id="sort-type-approvedScheduledItems" style="display:none"></div>

    <div id="approvedScheduledItems-body" style="display:none">

    </div>
</div>

<script language="javascript">
	new CStudioAuthoringWidgets.ApprovedScheduledItemsDashboard('approvedScheduledItems','site/rosie/dashboard');
</script>
    </div>

</div><div id="page_x002e_component-1-3_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-3_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<div id="recentlyMadeLive" class="ttTableGroup">
    <div class="ttHdr">
        <div class="ttWidgetHdr">
            <span class="ttClose" style="cursor:pointer;" id="widget-toggle-recentlyMadeLive"
                  onclick="return WcmDashboardWidgetCommon.toggleWidget('recentlyMadeLive','site/rosie/dashboard');"></span>
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('recentlyMadeLive','site/rosie/dashboard');">Recently Made Live</span>
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-recentlyMadeLive" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('recentlyMadeLive');">Collapse All</a>
            </li>
        </ul>

        <!-- TODO Sajan please change this to classes with generic names .. what wwere they thinking?! -->
        <div class="recently-made-live-right">
            <div class="recently-made-live" style="margin-right:0px;">
                Show: <input id="widget-showitems-recentlyMadeLive" type="text" maxlength="3" value="10"
                             class="serchLimitInput"/>
            </div>
        </div>
    </div>


    <div id="sortedBy-recentlyMadeLive" style="display:none"></div>
    <div id="sort-type-recentlyMadeLive" style="display:none"></div>

    <div id="recentlyMadeLive-body" style="display:none">

    </div>
</div>

<script language="javascript">
	new CStudioAuthoringWidgets.RecentlyMadeLiveDashboard('recentlyMadeLive','site/rosie/dashboard');
</script>
    </div>

</div><div id="page_x002e_component-1-4_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-4_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<div id="MyRecentActivity" class="ttTableGroup">
    <div class="ttHdr">
        <div class="ttWidgetHdr">
            <span class="ttClose" style="cursor:pointer;" id="widget-toggle-MyRecentActivity"
                  onclick="return WcmDashboardWidgetCommon.toggleWidget('MyRecentActivity','site/rosie/dashboard');"></span>
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('MyRecentActivity','site/rosie/dashboard');">My Recent Activity</span>
            (<span class='cstudio-dash-totalcount' id='MyRecentActivity-total-count'></span>)
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
        </ul>

        <!-- TODO Sajan please change this to classes with generic names .. what wwere they thinking?! -->
        <div class="recently-made-live-right">
            <div class="recently-made-live" style="margin-right:0px;">
                Show: <input id="widget-showitems-MyRecentActivity" type="text" maxlength="3" value="10"
                             class="serchLimitInput"/>
            </div>
        </div>
    </div>


    <div id="sortedBy-MyRecentActivity" style="display:none"></div>
    <div id="sort-type-MyRecentActivity" style="display:none"></div>

    <div id="MyRecentActivity-body" style="display:none">

    </div>
</div>

<script language="javascript">
	new CStudioAuthoringWidgets.MyRecentActivityDashboard('MyRecentActivity','site/rosie/dashboard');
</script>
    </div>

</div><div id="page_x002e_component-1-5_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-5_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">

<script language="javascript">
	new CStudioAuthoringWidgets.MyNotificationsDashboard('component-1-5','site/rosie/dashboard');
</script>
    </div>

</div><div id="page_x002e_component-1-6_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_component-1-6_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
<div id="icon-guide" style="width:290px;">
	<div class="ttHdr" >
		<div class="ttWidgetHdr">		  	
			<span 	class="ttClose" 
					id="widget-toggle-icon-guide" 
					onclick="return WcmDashboardWidgetCommon.toggleWidget('icon-guide','site/rosie/dashboard');"> 
			</span>	
			Icon Guide 
		</div>
    </div>			

	<div id="icon-guide-body" style="display:none">

        <div id="icon-guide-widget" class="headerIcon clearfix" style="width:290px;">
          <div class="iconLeft">
      		<div class="iconPaper"></div>
      		<div class="iconName">Navigation Page</div>
            <div class="iconPlainPaper"></div>
      		<div class="iconName">Floating Page</div>

            <div class="iconPuzzle"></div>
      		<div class="iconName">Component</div>
            <div class="iconDoc"></div>
      		<div class="iconName">Document</div>
            <div class="iconSpace">*</div>
      		<div class="iconName">New Page</div>
            <div class="iconText">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Disabled Page</div>

      </div>
      <div class="iconRight">
      		<div class="iconPen"></div>
      		<div class="iconNameR">In Progress</div>
            <div class="iconFlag"></div>
      		<div class="iconNameR">Submitted</div>
            <div class="iconSchedule"></div>
      		<div class="iconNameR">Scheduled</div>				
            <div class="iconDelete"></div>
            <div class="iconNameR">Deletion</div>
            <div class="iconInFlight"></div>
            <div class="iconNameR">Processing</div>
            <div class="iconLocked"></div>
            <div class="iconNameR">In Edit</div>				
      </div>


	</div>
</div>
<br/>

<script language="javascript">
	new CStudioAuthoringWidgets.IconGuideDashboard('icon-guide','site/rosie/dashboard');
</script>
    </div>

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

   <div class="sticky-footer">
  
    <div id="ft">
<div id="global_x002e_cstudio-footer">
    <div id="global_x002e_cstudio-footer_x0023_default">
 <div id="footer" >
		<div class="floatLeft"><a href="mailto:WCMadmins@craftercms.org?subject=Problem with authoring content">Problems? Email the Administrator.</a></div>
		<div class="floatRight">&copy; 2007-2014 Crafter Software Corp. All rights reserved.</div>
 </div>

    </div>

</div>    </div>
   </div>
   <div id="alfresco-yuiloader"></div>
   <script type="text/javascript">//<![CDATA[
      Alfresco.util.YUILoaderHelper.loadComponents();
   //]]></script>
</body>
</html>


