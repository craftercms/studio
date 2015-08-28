CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/workflow-jobs.css");
CStudioAdminConsole.Tool.WorkflowJobs = CStudioAdminConsole.Tool.WorkflowJobs ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}

/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.WorkflowJobs, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='job-list'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderJobsList();
	},
	
	renderJobsList: function() {
		var jobLisEl = document.getElementById("job-list");
		
		jobLisEl.innerHTML = 
			"<table id='jobsTable' class='cs-joblist'>" +
			 	"<tr>" +
				 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabAct")+"</th>" +
				 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabJobID")+"</th>" +
    			 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabProcessName")+"</th>" +
				 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabCreatedDate")+"</th>" +
				 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabModifiedDate")+"</th>" +
				 	"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabSiteId")+"</th>" +
					"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabItemsInJob")+"</th>" +
					"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabProperties")+"</th>"+
					"<th class='cs-joblist-heading'>"+CMgs.format(langBundle, "jobWorkflowTabStatus")+"</th>"+
				 "</tr>" + 
			"</table>";
	
			cb = {
				success: function(jobs) {
					var jobsTableEl = document.getElementById("jobsTable");
					for(var i=0; i<jobs.length; i++) {
						var job = jobs[i];
						var trEl = document.createElement("tr");
						var itemDetail = 
						     "<div>";
						     
						for(var j=0; j<job.items.length; j++) {
							var item = job.items[j];
							
							if(item) {
								itemDetail += 
									 "<table cellpadding='5px'><tr>"+     	
								     "<td>" + item.path + "&nbsp;&nbsp;</td>" +
									 "</tr></table>";
							}
						}



						var propDetail = "<table cellpadding='5px'>";
						for(var l=0; l<job.properties.length; l++) {
							var prop = job.properties[l];
							
							if(prop) {
								propDetail += 
									 "<tr><td>" + prop.name + "</td><td>&nbsp;&nbsp;&nbsp;</td><td>" + prop.value + "</td></tr>";
							}
						}
						propDetail += "</table>"

						
						itemDetail +=      	
						     "</div>";
						     
						var rowHTML = 				 	
							"<td class='cs-joblist-detail'><input type='checkbox' /></td>" +
				 			"<td class='cs-joblist-detail'>" + job.id + itemDetail + "</td>" +
				 			"<td class='cs-joblist-detail'>" + job.processName + "</td>" +
				 			"<td class='cs-joblist-detail'>" + job.createDate + "</td>" +
				 			"<td class='cs-joblist-detail'>" + job.modifiedDate + "</td>" +
				 			"<td class='cs-joblist-detail'>" + job.site + "</td>" +
							"<td class='cs-joblist-detail'>" + job.items.length + "</td>" +
							"<td class='cs-joblist-detail'>" + propDetail + "</td>" +
							"<td class='cs-joblist-detail'>" + job.currentStatus + "</td>"
				 		trEl.innerHTML = rowHTML;
				 		jobsTableEl.appendChild(trEl);
					}
				},
				failure: function(response) {
				}
			};
			
			CStudioAuthoring.Service.getWorkflowJobs(CStudioAuthoringContext.site, cb);
	}
	

});
	
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-workflow-jobs",CStudioAdminConsole.Tool.WorkflowJobs);