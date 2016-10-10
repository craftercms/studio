CStudioAuthoring.Analytics =CStudioAuthoring.Analytics || { };
CStudioAuthoring.Analytics.CommonReports =CStudioAuthoring.Analytics.CommonReports || { 
	
  controllerDailySiteVisitsTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Visits');


      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:visits")
            data.setValue(i, 1, parseInt(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerDailyPageviewsTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Pageviews');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:pageviews")
            data.setValue(i, 1, parseInt(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerDailyPagesPerVisitTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Pages/Visit');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:pageviewsPerVisit")
            data.setValue(i, 1, parseFloat(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerDailyBounceRateVisits: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Bounce Rate');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:visitBounceRate")
            data.setValue(i, 1, parseFloat(item.value));
        }
      }


      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerDailyNewVisitsPercentTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Visits');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:percentNewVisits")
            data.setValue(i, 1, parseFloat(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerDailyVisitorsTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Day');
      data.addColumn('number', 'Visitors');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:date")
            data.setValue(i, 0, item.value.substring(4,6) + "/" + item.value.substring(6,8));
          else if (item.key == "ga:visitors")
            data.setValue(i, 1, parseInt(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.AreaChart(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerTrafficSourcesTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Source');
      data.addColumn('number', 'Visits');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:source")
            data.setValue(i, 0, item.value);
          else if (item.key == "ga:visits")
            data.setValue(i, 1, parseInt(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.PieChart(containerEl).
        draw(data, {
        title : report.title
      });
    }
  },

  controllerMapOverlayTrend: {
    render: function(report, containerEl) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Country');
      data.addColumn('number', 'Visits');

      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
        var entry = report.queryResults[i];
        data.addRows(1);
        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
          var item = entry.dataItems[j];
          if (item.key == "ga:country")
            data.setValue(i, 0, item.value);
          else if (item.key == "ga:visits")
            data.setValue(i, 1, parseInt(item.value));
        }
      }

      // Create and draw the visualization.
      new google.visualization.GeoMap(containerEl).
        draw(data, {
        title : report.title,
        legend: "none"
      });
    }
  },

  controllerHeatMap: {
	    render: function(report, containerEl, filter) {
	    	containerEl.style.position = "";
	    	containerEl.style.top = "";

	    	var canvasContainerEl = document.createElement("div");
			canvasContainerEl.id = "heatmapContainer";
			containerEl.appendChild(canvasContainerEl);
			
			var canvasEl = document.createElement("canvas");
			canvasEl.id = "heatmapCanvas";
			canvasContainerEl.appendChild(canvasEl);		
			canvasEl.style.width = "100%";
			canvasEl.style.height = window.document.body.scrollHeight + "px";

			var cb = {
					moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
						var xx = h337.create({"element":document.getElementById("heatmapContainer"), "radius":25, "visible":true});
						var canvasEl = document.getElementById("heatmapContainer").children[1];
						canvasEl.style.zIndex = "3";

						var rows = report.queryResults;
						var msg = "";
						var dataset = [];
						
						for(var i=0; i<rows.length; i++) {
							dataset[rows[i].dataItems[1].value] = rows[i].dataItems[0].value;
						}

						var links = document.links;
						
						for(var j=0; j<links.length; j++) {
							var linkEl = links[j];
							var linkPos = YAHOO.util.Dom.getXY(linkEl);

							var location = linkEl.href.replace("//","");
							location = location.substring(location.indexOf("/"));
							
							if(location.indexOf("?") != -1) {
								location = location.substring(0, location.indexOf("?"));	
							}
							
							if(location.indexOf("#") != -1) {
								location = location.substring(0, location.indexOf("#"));	
							}

							if(location.substring(location.length-1) == "/") {
						    	  location = location.substring(0, location.length-1);
						      }

							
							var magnitude = dataset[location];
							
							if(magnitude) {
								for(var k=0; k<magnitude; k++) {
									xx.store.addDataPoint(linkPos[0],linkPos[1]);
								}
							}
						}								

					},
					
					context: this
				};
				
	            CStudioAuthoring.Module.requireModule(
	                'heatmap-support',
	                '/components/cstudio-common/heatmap-support.js',
	                { },
	                cb
	            );

	    }  
  },
  
  controllerHourlyPageviewsTrendOverlay: {
	    render: function(report, containerEl) {
	      // Create and populate the data table.
	      var data = new google.visualization.DataTable();
	      data.addColumn('string', 'Hour');
	      data.addColumn('number', 'Pageviews');

	      for (var i = 0, len1 = report.queryResults.length; i < len1; i++) {
	        var entry = report.queryResults[i];
	        data.addRows(1);
	        for (var j = 0, len2 = entry.dataItems.length; j < len2; j++) {
	          var item = entry.dataItems[j];

	          if(item.key == "ga:hour") {
	                var hour = "";
	                    if(item.value.substring(0,1) == "0") {
	                    	item.value = item.value.substring(1);
	                    }
	                    
                        var hourVal = parseInt(item.value);
                        
                        if(hourVal == 0 || hourVal == 24) {
                           hour = "12 am";
                        }
                        else if(hourVal < 12) {
                          hour = ""+ hourVal + " am";
                       }
                       else {
                          if(hourVal == 12) {
                               hour = "12 pm";
                          }
                          else {
                               hour = "" + (hourVal - 12) + " pm";
                          }
                     }
                     data.setValue(i, 0, hour);

	          }
	          else if (item.key == "ga:pageviews") {
	            data.setValue(i, 1, parseInt(item.value));
                  }
	        }
	      }

	      // Create and draw the visualization.
	      new google.visualization.AreaChart(containerEl).
	        draw(data, {
	        title : "Hourly Page Views",
	        legend: "none",
                hAxis: {textStyle: {color: 'white'}, slantedText: true},
                vAxis: {textStyle: {color: 'white'}},
                titleTextStyle:{color: 'white'},

	        backgroundColor: "none"
	      });
	    }
	  },
  
  controllers: {},

  getController: function(controllerName) {
    return this.controllers[controllerName];
  }
};


	
// register controllers
CStudioAuthoring.Analytics.CommonReports.controllers = {
  "dailySiteVisitsTrend": CStudioAuthoring.Analytics.CommonReports.controllerDailySiteVisitsTrend,
  "dailyPageviewsTrend": CStudioAuthoring.Analytics.CommonReports.controllerDailyPageviewsTrend,
  "dailyPagesPerVisitTrend": CStudioAuthoring.Analytics.CommonReports.controllerDailyPagesPerVisitTrend,
  "dailyBounceRateVisits": CStudioAuthoring.Analytics.CommonReports.controllerDailyBounceRateVisits,
  "dailyNewVisitsPercentTrend": CStudioAuthoring.Analytics.CommonReports.controllerDailyNewVisitsPercentTrend,

  "dailyVisitorsTrend": CStudioAuthoring.Analytics.CommonReports.controllerDailyVisitorsTrend,
  "trafficSourcesTrend": CStudioAuthoring.Analytics.CommonReports.controllerTrafficSourcesTrend,
  "mapOverlayTrend": CStudioAuthoring.Analytics.CommonReports.controllerMapOverlayTrend,
  "heatmapPageOverlay":CStudioAuthoring.Analytics.CommonReports.controllerHeatMap,
  "hourlyPageviewsTrendOverlay":CStudioAuthoring.Analytics.CommonReports.controllerHourlyPageviewsTrendOverlay
};
	
// notify that module is ready
CStudioAuthoring.Module.moduleLoaded("dashboard-analytics", CStudioAuthoring.Analytics.CommonReports);
