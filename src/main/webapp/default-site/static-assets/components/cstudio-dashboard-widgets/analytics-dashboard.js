if (typeof CStudioAuthoringWidgets == "undefined" || !CStudioAuthoringWidgets) {
  var CStudioAuthoringWidgets = {};
}

/**
 * render widget dashboard
 */
CStudioAuthoringWidgets.AnalyticsDashboard =
  CStudioAuthoringWidgets.AnalyticsDashboard || function(widgetId, pageId) {
    this.widgetId = widgetId;
    this.pageId = pageId;
    this.containerEl = document.getElementById(widgetId);
    this._self = this;

    this.render = function() {
      var configCb = {
        success: function(dashConfig) {
          if (!dashConfig.reports.length) { // one object array
            dashConfig.reports = [ dashConfig.reports.report ];
          }

          // build layout
          var html = "<table width='100%' border='0'><tr>";
          for (var i = 0; i < dashConfig.reports.length; i++) {
            var report = dashConfig.reports[i];

            if (i != 0 && i % 2 == 0)
              html += "</tr><tr>";

            html +=
              ["<td style='vertical-align:top'>" +
                "<div style='border:1px solid #c0c0c0;margin:10px;width:600px'>" +
                "<div style='padding:5px;color:#ffffff;background-color:#505050;font-weight:bold'>" + report.webPropertyId + " | " + report.title + "</div>",
                "<div style='min-height:360px;margin: 0 auto;text-align:center' id='", report.webPropertyId, "-", report.reportId, "'>Loading...</div>",
                "<div style='border-top:1px solid #c0c0c0;padding:5px;color:#ffffff;background-color:#d0d0d0'> </div>",
                "</div></td>"].join('');
          }
          html += "</tr></table>";

          this._self.containerEl.innerHTML = html;

          // queue up report rendering
          for (var j = 0; j < dashConfig.reports.length; j++) {
            var report = dashConfig.reports[j];
            var containerEl = document.getElementById(report.webPropertyId + "-" + report.reportId);

            this._self.renderReport(
              CStudioAuthoringContext.site,
              report.webPropertyId,
              report.reportId,
              containerEl);
          }
        },

        failure: function() {
        },

        _self: this._self
      };

      CStudioAuthoring.Service.lookupConfigurtion(
        CStudioAuthoringContext.site,
        "/analytics/dashboard-config.xml",
        configCb);

    };

    /**
     * render report
     */
    this.renderReport = function(site, webPropertyId, reportId, containerEl) {
      var reportCb = {
        success: function(report) {
          var library = report.visualizationCode.library;
          var controller = report.visualizationCode.controller;

          var moduleCb = {
            moduleLoaded: function(moduleName, clazz, config) {
              var controller = clazz.getController(config.controller);
              controller.render(config.report, config.containerEl);
            }
          };

          var moduleConfig = {
            report: report,
            controller: controller,
            containerEl: containerEl
          };

          CStudioAuthoring.Module.requireModule(
            "dashboard-analytics",
            "/proxy/alfresco/cstudio/services/content/content-at-path" +
              "?path=/cstudio" + library,
            moduleConfig, moduleCb);


        },

        failure: function() {
        },

        containerEl: containerEl
      };

      CStudioAuthoring.Service.Analytics.getReport(site, webPropertyId, reportId, reportCb);
    }
  };
