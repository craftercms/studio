CStudioAuthoring.Utils.addJavascript("http://www.google.com/jsapi");
CStudioAuthoring.Utils.addJavascript("http://www.google.com/uds/api/visualization/1.0/c664139ef53452718b5ceefb1d10fdfd/format+en,default,geomap,corechart.I.js");

CStudioAuthoring.CampaignsPanel = CStudioAuthoring.CampaignsPanel || {

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

		var realContainerEl = containerEl;

		var containerEl = document.createElement("div");
		containerEl.style = "height: 125px; display: inline; float: left; position: relative; width: 250px; border-bottom: medium none; overflow-y: scroll;"
        realContainerEl.appendChild(containerEl);

		var spacerEl = document.createElement("div");
		spacerEl.style = "display: inline; float: right; position: relative; width: 80%; background: none repeat scroll 0px 0px lightgray; border-width: 1px; border-style: solid; height: 125px; border-color: black white white black;"
		realContainerEl.appendChild(spacerEl);

		CStudioAuthoring.Service.lookupConfigurtion(
				CStudioAuthoringContext.site, 
				"/salesforce/campaign-reports-config.xml", 
				{
					success: function(config) {
						var reportSelectEl = document.createElement("select");
						reportSelectEl.style.height = "20px";
						YAHOO.util.Dom.addClass(reportSelectEl, "acn-panel-dropdown");

						containerEl.appendChild(reportSelectEl);
						reportSelectEl.options[0] = new Option("Reports Off", "0", true, false);
						
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
                                    "<div style='line-height: 111px; text-align: center;'><img src='"+CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/wait.gif'/></div>";

								
					            this.context.renderReport(
					                    CStudioAuthoringContext.site,
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
    renderReport:  function(site, reportId, containerEl) {

      var reportCb = {
        moduleLoaded: function(name, objClass, config) {
            try {
				objClass.render(config.containerEl);
            }
            catch(err) {
                this.containerEl.innerHTML = "<div style='color: white; line-height: 111px; text-align: center;'><b>Unable to render report due to error</b><br/>" + err + "</div>";                
            };
                
        },

        failure: function() {
        },

        containerEl: containerEl
      };

      var moduleConfig = {
          containerEl: containerEl
      };

      CStudioAuthoring.Module.requireModule(
          "salesforce-report-"+reportId,
          "/proxy/alfresco/cstudio/services/content/content-at-path" +
          "?path=/cstudio/config/sites/" + site  + "/salesforce/reports/" + reportId + ".js",
          moduleConfig, reportCb);

	}
}

CStudioAuthoring.Module.moduleLoaded("campaigns-panel", CStudioAuthoring.CampaignsPanel);