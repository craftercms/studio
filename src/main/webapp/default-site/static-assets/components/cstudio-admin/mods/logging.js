CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/logging.css");
CStudioAdminConsole.Tool.Logging = CStudioAdminConsole.Tool.Logging ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}

/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.Logging, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='logger-list'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderJobsList();
	},
	
	renderJobsList: function() {
		
		this.refreshLoggingLevels();
	
	},
	
	/*
	* set individual logger's logging level and refresh the table
	*/
	setLoggingLevel: function (index, level) {
		var logger = CStudioAdminConsole.Tool.Logging.loggers[index];
		if(logger) {
			var serviceUri = "/api/1/services/api/1/server/set-logger-state.json?logger="+logger.name+"&level="+level;
			var cb = {
				success:function() {
					CStudioAdminConsole.Tool.Logging.prototype.refreshLoggingLevels();
				},
				failure: function() {
				}
			}
			
			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
		}
	},
	
	/*
	* display all loggers and their logging levels
	*/
	refreshLoggingLevels: function () {
		cb = {
			success: function(response) {
				var loggerLisEl = document.getElementById("logger-list");
		
				var loggers = eval("(" + response.responseText + ")");
				CStudioAdminConsole.Tool.Logging.loggers = loggers;
				// create the entire table HTML due to IE9 support
				var jobsTableEl = document.getElementById("loggerTable");
				var tableHTML = 
					"<table id='loggerTable' class='cs-loggerlist'>" +
						"<tr>" +
						 	"<th class='cs-loggerlist-heading'>"+CMgs.format(langBundle, "logginLevelsTabLogger")+"</th>" +
						 	"<th class='cs-loggerlist-heading'>"+CMgs.format(langBundle, "logginLevelsTabCurrentLevel")+"</th>" +
    					 	"<th class='cs-loggerlist-heading'>"+CMgs.format(langBundle, "logginLevelsTabChangeLevel")+"</th>" +
						"</tr>";

				for(var i=0; i<loggers.length; i++) {
					var logger = loggers[i];
					var rowHTML =
						"<tr>" +  				 	
			 				"<td class='cs-loggerlist-detail'>" + logger.name + "</td>" +
			 				"<td class='cs-loggerlist-detail'>" + CMgs.format(langBundle, logger.level) + "</td>" +
			 				"<td class='cs-loggerlist-detail'>"+
				 			  "<a onclick=\"CStudioAdminConsole.Tool.Logging.prototype.setLoggingLevel("+i+ ",\'debug\'); return false;\">"+CMgs.format(langBundle, "debug")+"</a>&nbsp;&nbsp;"+
				 			  "<a onclick=\"CStudioAdminConsole.Tool.Logging.prototype.setLoggingLevel("+i+ ",\'warn\'); return false;\">"+CMgs.format(langBundle, "warn")+"</a>&nbsp;&nbsp;"+
				 			  "<a onclick=\"CStudioAdminConsole.Tool.Logging.prototype.setLoggingLevel("+i+ ",\'info\'); return false;\">"+CMgs.format(langBundle, "info")+"</a>&nbsp;&nbsp;"+
				 			  "<a onclick=\"CStudioAdminConsole.Tool.Logging.prototype.setLoggingLevel("+i+ ",\'error\'); return false;\">"+CMgs.format(langBundle, "error")+"</a>"+
			 				"</td>" +
			 			"</tr>";
			 		tableHTML = tableHTML + rowHTML;
				}
				tableHTML = tableHTML + "</table>";
				loggerLisEl.innerHTML = tableHTML;
			},
			failure: function(response) {
			},
				
			self: this
		};
			
		var serviceUri = "/api/1/services/api/1/server/get-loggers.json";

		YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
	}
	
});
	
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-logging",CStudioAdminConsole.Tool.Logging);