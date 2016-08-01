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
		CStudioAdminConsole.Tool.LogView.pause = false;
		CStudioAdminConsole.Tool.LogView.history = 
		 	"<th class='cs-loglist-heading'>Timestamp</th>" +
		 	"<th class='cs-loglist-heading'>Message</th>" +
		 	"<th class='cs-loglist-heading'>Details</th>";

		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='log-view'>" +
				"<table id='logTable' class='cs-loglist'>" +
					"<tr>" +
					"</tr>" +
				 "</table>" +
			"</div>";
			
			var actions = [
				{ name: "Play/Pause", context: this, method: this.playPauseToggleClick }
			];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderLogView();
	},
	
	playPauseToggleClick: function() {
		CStudioAdminConsole.Tool.LogView.pause = (!CStudioAdminConsole.Tool.LogView.pause);
	},

	renderLogView: function() {
		this.appendLogs();
		window.setTimeout(function(console) { 
			console.renderLogView();
		 }, 1000, this);
	},
	
	appendLogs: function() {
		var tailEl = document.getElementById('logTable');

		var cb = {
			success:function(response) {
				var entries = eval("(" + response.responseText + ")");

				for(var i=0; i<entries.length; i++) {
					var entry = entries[i];

					CStudioAdminConsole.Tool.LogView.history += "<tr class='entry "+entry.level+"' >"+
							"<td class='timestamp'>"+entry.timestamp+"</td>"+
							"<td class='message'>"+entry.message+"</td>"+
							"<td class='exception'>"+entry.exception+"</td>"+
						"</tr>";

					if(CStudioAdminConsole.Tool.LogView.pause == false) {
						this.el.innerHTML = CStudioAdminConsole.Tool.LogView.history;
						var viewEl = document.getElementById("log-view")
						viewEl.scrollTop = viewEl.scrollHeight;
					}
						
				}

			},
			failure: function() {
			},

			el: tailEl
		}

		if(tailEl) {
			var d = new Date();
			var n = d.getTime();
			var since = n;
			var serviceUri = "/api/1/monitoring/log?since="+since+"&siteId="+CStudioAuthoringContext.site;
			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
		}
		else {
			if(CStudioAdminConsole.Tool.LogView.refreshFn) {
				window.clearTimeout(CStudioAdminConsole.Tool.LogView.refreshFn);
			}
		}
	}
});


	
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-log-view",CStudioAdminConsole.Tool.LogView);