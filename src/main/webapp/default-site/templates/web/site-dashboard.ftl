<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Crafter Studio</title>

    <#include "/templates/web/common/page-fragments/head.ftl" />


    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/en/base.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/kr/base.js"></script>

    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/lib/wcm-dashboardwidget-common.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/go-live-queue.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/recently-made-live.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/my-recent-activity.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/my-notifications.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/icon-guide.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-dashboard-widgets/approved-scheduled-items.js"></script>

    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <#include "/templates/web/common/page-fragments/context-nav.ftl" />

    <script>
      CMgs = CStudioAuthoring.Messages;
      langBundle = CMgs.getBundle("siteDashboard", CStudioAuthoringContext.lang);
    </script>
</head>

<body class="yui-skin-cstudioTheme">
   <div class="sticky-wrapper">
      <div id="doc3">
    <div id="bd"> 

<div id="page_x002e_title_x002e_site_x007e_rosie_x007e_dashboard">
    <div id="page_x002e_title_x002e_site_x007e_rosie_x007e_dashboard_x0023_default">
      <!-- dashboard title -->
		<div id="pageTitle">
			<div class="dashHeader"><h1><span><script>CMgs.display(langBundle, "dashboardTitle","${envConfig.siteTitle}")</script></span></h1></div>
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
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('GoLiveQueue','site/rosie/dashboard');"><script>CMgs.display(langBundle, "dashletGoLiveQueueTitle")</script></span>
            (<span class='cstudio-dash-totalcount' id='GoLiveQueue-total-count'></span>)
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-GoLiveQueue" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('GoLiveQueue');"><script>CMgs.display(langBundle, "dashletGoLiveCollapseAll")</script></a>
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
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('approvedScheduledItems','site/rosie/dashboard');"><script>CMgs.display(langBundle, "dashletApprovedSchedTitle")</script></span>
            (<span class='cstudio-dash-totalcount' id='approvedScheduledItems-total-count'></span>)
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-approvedScheduledItems" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('approvedScheduledItems');"><script>CMgs.display(langBundle, "approvedScheduledCollapseAll")</script></a>
            </li>
        </ul>
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
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('recentlyMadeLive','site/rosie/dashboard');"><script>CMgs.display(langBundle, "dashletRecentDeployTitle")</script></span>
        </div>

        <ul id="ttNav" class='cstudio-widget-controls'>
            <li>
                <a id="expand-all-recentlyMadeLive" class="widget-expand-state" href="#"
                   OnClick="return WcmDashboardWidgetCommon.toggleAllItems('recentlyMadeLive');"><script>CMgs.display(langBundle, "dashletRecentDeployCollapseAll")</script></a>
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
        	<span class="dashboard-widget-title" onclick="return WcmDashboardWidgetCommon.toggleWidget('MyRecentActivity','site/rosie/dashboard');"><script>CMgs.display(langBundle, "dashletMyRecentActivityTitle")</script></span>
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
			<script>CMgs.display(langBundle, "dashletIconGuideTitle")</script>
		</div>
    </div>			

	<div id="icon-guide-body" style="display:none">

        <div id="icon-guide-widget" class="headerIcon clearfix" style="width:290px;">
          <div class="iconLeft">
      		<div class="iconPaper"></div>
      		<div class="iconName"><script>CMgs.display(langBundle, "dashletIconGuideNavigationPage")</script></div>
            <div class="iconPlainPaper"></div>
      		<div class="iconName"><script>CMgs.display(langBundle, "dashletIconGuideFloatingPage")</script></div>

            <div class="iconPuzzle"></div>
      		<div class="iconName"><script>CMgs.display(langBundle, "dashletIconGuideComponent")</script></div>
            <div class="iconDoc"></div>
      		<div class="iconName"><script>CMgs.display(langBundle, "dashletIconGuideDocument")</script></div>
            <div class="iconSpace">*</div>
      		<div class="iconName"><script>CMgs.display(langBundle, "dashletIconGuideStateNew")</script></div>
            <div class="iconText">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<script>CMgs.display(langBundle, "dashletIconGuideStateDisabled")</script></div>

      </div>
      <div class="iconRight">
      		<div class="iconPen"></div>
      		<div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateInProgress")</script></div>
            <div class="iconFlag"></div>
      		<div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateInWorkflow")</script></div>
            <div class="iconSchedule"></div>
      		<div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateScheduled")</script></div>				
            <div class="iconDelete"></div>
            <div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateDeleted")</script></div>
            <div class="iconInFlight"></div>
            <div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateSystemProcessing")</script></div>
            <div class="iconLocked"></div>
            <div class="iconNameR"><script>CMgs.display(langBundle, "dashletIconGuideStateLocked")</script></div>				
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

    <#include "/templates/web/common/page-fragments/footer.ftl" />

   <div id="alfresco-yuiloader"></div>
   <script type="text/javascript">//<![CDATA[
      //Alfresco.util.YUILoaderHelper.loadComponents();
   //]]></script>
</body>
</html>


