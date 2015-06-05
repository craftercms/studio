CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/log-view.css");
CStudioAdminConsole.Tool.LogView = CStudioAdminConsole.Tool.LogView ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}

/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.LogView, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='log-view'>" +
				"<table id='loggerTable' class='cs-loggerlist'>" +
					"<tr>" +
					 	"<th class='cs-loggerlist-heading'>Date</th>" +
					 	"<th class='cs-loggerlist-heading'>Message</th>" +
					"</tr>" +
				 "</table>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderLogView();
	},
	
	renderLogView: function() {
		this.appendLogs();
		window.setTimeout(function(console) { 
			console.renderLogView();
			var viewEl = document.getElementById("log-view")
			viewEl.scrollTop = viewEl.scrollHeight;
		 }, 1000, this);
	},
	
	appendLogs: function() {

			var tailEl = document.getElementById('loggerTable');

			if(tailEl) {
				tailEl.innerHTML += "<tr>"+
										"<td>"+new Date()+"</td>"+
										"<td>YAHYAHYAHYAHYAHYAHYAHYAHYAH</td>"+
									"</tr>";
			}
			else {
				if(CStudioAdminConsole.Tool.LogView.refreshFn) {
					window.clearTimeout(CStudioAdminConsole.Tool.LogView.refreshFn);
				}
			}
/*			var serviceUri = "/api/1/services/api/1/server/set-logger-state.json";
/			var cb = {
				success:function() {
					CStudioAdminConsole.Tool.Logging.prototype.refreshLoggingLevels();
				},
				failure: function() {
				}
			}
			
			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
*/
		}
	});


	
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-log-view",CStudioAdminConsole.Tool.LogView);