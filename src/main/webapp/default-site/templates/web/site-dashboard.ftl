<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Crafter Studio</title>

    <#include "/templates/web/common/page-fragments/head.ftl" />

    <#assign path="/studio/static-assets/components/cstudio-common/resources/" />
    <script src="${path}en/base.js"></script>
    <script src="${path}kr/base.js"></script>

    <#assign path="/studio/static-assets/components/cstudio-dashboard-widgets/" />
    <script src="${path}lib/wcm-dashboardwidget-common.js"></script>
    <script src="${path}go-live-queue.js"></script>
    <script src="${path}recently-made-live.js"></script>
    <script src="${path}my-recent-activity.js"></script>
    <script src="${path}my-notifications.js"></script>
    <script src="${path}icon-guide.js"></script>
    <script src="${path}approved-scheduled-items.js"></script>

    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <#include "/templates/web/common/page-fragments/context-nav.ftl" />

    <script>
        var
                CMgs = CStudioAuthoring.Messages,
                langBundle = CMgs.getBundle("siteDashboard", CStudioAuthoringContext.lang);
    </script>

    <script src="/studio/static-assets/scripts/crafter.js"></script>
    <script src="/studio/static-assets/scripts/animator.js"></script>

</head>

<body class="yui-skin-cstudioTheme">

<section class="site-dashboard">
    <div class="container">

        <hgroup class="page-header">
            <h1></h1>
        </hgroup>

        <div id="GoLiveQueue" class="panel panel-default">
            <div class="panel-heading">
                <h2 class="panel-title">
                    <span></span> (<span class="cstudio-dash-totalcount" id="GoLiveQueue-total-count"></span>)
                </h2>
                <ul class="widget-controls">
                    <li>
                        <button id="expand-all-GoLiveQueue" class="btn btn-default btn-sm"
                                onclick="return WcmDashboardWidgetCommon.toggleAllItems('GoLiveQueue');"></button>
                    </li>
                </ul>
            </div>
            <div id="sortedBy-GoLiveQueue" style="display:none"></div>
            <div id="sort-type-GoLiveQueue" style="display:none"></div>
            <div id="GoLiveQueue-body" style="display:none"></div>
        </div>

        <div id="approvedScheduledItems" class="panel panel-default">
            <div class="panel-heading">
                <h2 class="panel-title">
                    <span></span> (<span class="cstudio-dash-totalcount" id="approvedScheduledItems-total-count"></span>)
                </h2>
                <ul class="widget-controls">
                    <li>
                        <button id="expand-all-approvedScheduledItems" class="btn btn-default btn-sm"
                                onclick="return WcmDashboardWidgetCommon.toggleAllItems('approvedScheduledItems');"></button>
                    </li>
                </ul>
            </div>
            <div id="sortedBy-approvedScheduledItems" style="display:none"></div>
            <div id="sort-type-approvedScheduledItems" style="display:none"></div>
            <div id="approvedScheduledItems-body" style="display:none"></div>
        </div>

        <div id="recentlyMadeLive" class="panel panel-default">
            <div class="panel-heading">
                <h2 class="panel-title">
                    <span></span>
                </h2>
                <ul class="widget-controls">
                    <li class="form-inline">
                        <div class="input-group">
                            <label for="widget-showitems-recentlyMadeLive" class="input-group-addon">Show</label>
                            <input id="widget-showitems-recentlyMadeLive" type="text" maxlength="3" value="10"
                                   class="form-control input-sm"/>
                        </div>
                    </li>
                    <li>
                        <button id="expand-all-recentlyMadeLive" class="btn btn-default btn-sm"
                                onclick="return WcmDashboardWidgetCommon.toggleAllItems('recentlyMadeLive');"></button>
                    </li>
                </ul>
            </div>
            <div id="sortedBy-recentlyMadeLive" style="display:none"></div>
            <div id="sort-type-recentlyMadeLive" style="display:none"></div>
            <div id="recentlyMadeLive-body" style="display:none"></div>
        </div>

        <div id="MyRecentActivity" class="panel panel-default">
            <div class="panel-heading">
                <h2 class="panel-title">
                    <span></span> (<span class="cstudio-dash-totalcount" id="MyRecentActivity-total-count"></span>)
                </h2>
                <ul class="widget-controls">
                    <li class="form-inline">
                        <div class="input-group">
                            <label for="widget-showitems-MyRecentActivity" class="input-group-addon">Show</label>
                            <input type="text" id="widget-showitems-MyRecentActivity" maxlength="3" value="10"
                                   class="form-control input-sm"/>
                        </div>
                    </li>
                </ul>
            </div>
            <div id="MyRecentActivity-body" class="table-responsive"></div>
            <div id="sortedBy-MyRecentActivity" style="display:none"></div>
            <div id="sort-type-MyRecentActivity" style="display:none"></div>
        </div>

        <div id="iconGuide" class="panel panel-default">
            <div class="panel-heading">
                <h2 class="panel-title">
                    <span></span>
                </h2>
            </div>
            <div class="panel-body">
            <#assign classes="col-xs-6 col-sm-3 col-md-2 mb10" />
                <div class="row">
                    <div class="${classes}">
                        <div class="iconPaper"></div>
                        <div class="iconName">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideNavigationPage")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconPlainPaper"></div>
                        <div class="iconName">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideFloatingPage")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconPuzzle"></div>
                        <div class="iconName">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideComponent")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconDoc"></div>
                        <div class="iconName">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideDocument")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconSpace">*</div>
                        <div class="iconName">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateNew")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconText" style="margin: 0 0 0 20px; padding: 2px 0 1px;">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateDisabled")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconPen"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateInProgress")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconFlag"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateInWorkflow")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconSchedule"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateScheduled")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconDelete"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateDeleted")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconInFlight"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateSystemProcessing")</script>
                        </div>
                    </div>
                    <div class="${classes}">
                        <div class="iconLocked"></div>
                        <div class="iconNameR">
                            <script>CStudioAuthoring.Messages.display(langBundle, "dashletIconGuideStateLocked")</script>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</section>
<script>
    (function (CStudioAuthoring) {

        var CMgs = CStudioAuthoring.Messages;
        var langBundle = CMgs.getBundle("siteDashboard", CStudioAuthoringContext.lang);
        var loc = CStudioAuthoring.Messages.format;

        document.querySelector('.page-header h1').innerHTML = loc(langBundle, "dashboardTitle", "${envConfig.siteTitle!"SITE"}");
        document.querySelector('#GoLiveQueue .panel-title span').innerHTML = loc(langBundle, "dashletGoLiveQueueTitle");
        document.querySelector('#approvedScheduledItems .panel-title span').innerHTML = loc(langBundle, "dashletApprovedSchedTitle");
        document.querySelector('#recentlyMadeLive .panel-title span').innerHTML = loc(langBundle, "dashletRecentDeployTitle");
        document.querySelector('#MyRecentActivity .panel-title span').innerHTML = loc(langBundle, "dashletMyRecentActivityTitle");
        document.querySelector('#iconGuide .panel-title span').innerHTML = loc(langBundle, "dashletIconGuideTitle");

        document.querySelector('#expand-all-GoLiveQueue').innerHTML = loc(langBundle, "dashletGoLiveCollapseAll");
        document.querySelector('#expand-all-recentlyMadeLive').innerHTML = loc(langBundle, "dashletRecentDeployCollapseAll");
        document.querySelector('#expand-all-approvedScheduledItems').innerHTML = loc(langBundle, "approvedScheduledCollapseAll");

        new CStudioAuthoringWidgets.GoLiveQueueDashboard('GoLiveQueue', 'site/rosie/dashboard');
        new CStudioAuthoringWidgets.ApprovedScheduledItemsDashboard('approvedScheduledItems', 'site/rosie/dashboard');
        new CStudioAuthoringWidgets.RecentlyMadeLiveDashboard('recentlyMadeLive', 'site/rosie/dashboard');
        new CStudioAuthoringWidgets.MyRecentActivityDashboard('MyRecentActivity', 'site/rosie/dashboard');
        new CStudioAuthoringWidgets.IconGuideDashboard('icon-guide', 'site/rosie/dashboard');

    })(CStudioAuthoring);
</script>

<#include "/templates/web/common/page-fragments/footer.ftl" />
<script>//<![CDATA[
//Alfresco.util.YUILoaderHelper.loadComponents();
//]]></script>
<div id="alfresco-yuiloader"></div>

</body>
</html>


