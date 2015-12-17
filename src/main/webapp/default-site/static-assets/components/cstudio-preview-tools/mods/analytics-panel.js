CStudioAuthoring.Utils.addJavascript("http://www.google.com/jsapi");
CStudioAuthoring.Utils.addJavascript("http://www.google.com/uds/api/visualization/1.0/c664139ef53452718b5ceefb1d10fdfd/format+en,default,geomap,corechart.I.js");

/**
 * editor tools
 */
CStudioAuthoring.AnalyticsPanel = CStudioAuthoring.AnalyticsPanel || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			
			this.initialized = true;
		}
	},
	
	render: function(containerEl, config) {

		CStudioAuthoring.Service.lookupConfigurtion(
				CStudioAuthoringContext.site, 
				"/analytics/preview-config.xml", 
				{
					success: function(config) {
						var reportSelectEl = document.createElement("select");
						reportSelectEl.style.height = "20px";
						YAHOO.util.Dom.addClass(reportSelectEl, "acn-panel-dropdown");

						containerEl.appendChild(reportSelectEl);
						reportSelectEl.options[0] = new Option(CMgs.format(previewLangBundle, "reportsOff"), "0", true, false);
						
                        if(!config.reports) {
                            config.reports = [];
                        }
						else if(!config.reports.length) {
							config.reports = [config.reports.report];
						}
						
						for(var i=0; i<config.reports.length; i++) {
							var label = config.reports[i].title
							reportSelectEl.options[i+1] = new Option(label, ""+(i+1), false, false);
						}

						reportSelectEl.context = this.context;
						reportSelectEl.onchange = function() {

							// remove the existing container
							var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");
							
							if(reportContainerEl) {
								document.body.removeChild(reportContainerEl);
							}

							var selectedIndex = this.selectedIndex;
							if(selectedIndex != 0) {
								var report = config.reports[selectedIndex-1];
								
								var reportContainerEl = document.createElement("div");
								reportContainerEl.id = "cstudioPreviewAnalyticsOverlay";
								YAHOO.util.Dom.addClass(reportContainerEl, "cstudio-analytics-overlay");
								
								//if(!report.presentation.noFloat) {
									reportContainerEl.style.position = "fixed";
									reportContainerEl.style.width = "100%";
									reportContainerEl.style.top = "30px";
								//}
								
								document.body.appendChild(reportContainerEl);
                                reportContainerEl.innerHTML = 
                                    "<div style='line-height: 111px; text-align: center;'><img src='"+CStudioAuthoringContext.baseUri + "/themes/cstudioTheme/images/wait.gif'/></div>";

								
					            this.context.renderReport(
					                    CStudioAuthoringContext.site,
					                    report.webPropertyId,
					                    report.reportId,
					                    reportContainerEl);
							}
						}
					},
				
					failure: function() {
					},
				
					context: this
				});	
	},
	
    /**
	 * render report
	 */
    renderReport:  function(site, webPropertyId, reportId, containerEl) {

      var reportCb = {
        success: function(report) {
            try {
                var library = report.visualizationCode.library;
                var controller = report.visualizationCode.controller;

                var moduleCb = {
                    moduleLoaded: function(moduleName, clazz, config) {
                        var controller = clazz.getController(config.controller);
              
                        config.containerEl.innerHTML = "";
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
                    "/api/1/services/api/1/content/get-content-at-path.bin" +
                    "?path=/cstudio" + library,
                    moduleConfig, moduleCb);
            }
            catch(err) {
                this.containerEl.innerHTML = "<div style='color: white; line-height: 111px; text-align: center;'><b>"+CMgs.format(previewLangBundle, "reportsError")+"</b><br/>" + err + "</div>";
            };
                
        },

        failure: function() {
        },

        containerEl: containerEl
      };
      
      var location = document.location.pathname;
		
      if(location.indexOf("?") != -1) {
    	  location = location.substring(0, location.indexOf("?"));	
      }
		
      if(location.indexOf("#") != -1) {
    	  location = location.substring(0, location.indexOf("#"));	
      }

      if(location.substring(location.length-1) == "/") {
    	  location = location.substring(0, location.length-1);
      }
      
      CStudioAuthoring.Service.Analytics.getReport(site, webPropertyId, reportId, reportCb, "ga:pagePath.eq."+location);

	}
}

CStudioAuthoring.Module.moduleLoaded("analytics-panel", CStudioAuthoring.AnalyticsPanel);