/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/deployment-queues.css");
CStudioAdminConsole.Tool.DeploymentQueues = CStudioAdminConsole.Tool.DeploymentQueues ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}
var list = [];
var queueItems = [];
/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.DeploymentQueues, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='queue-list'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderQueueList();
	},
	
	renderQueueList: function() {
		
		var actions = [
				{ name: CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment"), context: this, method: this.cancelDeployment }
		];
		CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
		this.renderQueueTable();

	},
	
	renderQueueTable: function () {
		var queueListEl = document.getElementById("queue-list");
		queueListEl.innerHTML =
		"<table id='queueTable' class='cs-statelist'>" +
			 	"<tr>" +
				 	"<th class='cs-statelist-heading'><a href='#' onclick='CStudioAdminConsole.Tool.DeploymentQueues.selectAll(); return false;'>"+CMgs.format(langBundle, "setQueueTabSelectAll")+"</a></th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabID")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabPath")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabEnvironment")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabUser")+"</th>" +
    			 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabState")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabAction")+"</th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabScheduledDate")+"</th>" +
				 "</tr>" + 
			"</table>";
	
			cb = {
				success: function(response) {
					var queue = eval("(" + response.responseText + ")");
					queueItems = queue.items;
					
					var queueTableEl = document.getElementById("queueTable");
					for(var i=0; i<queue.items.length; i++) {
						var item = queue.items[i];
						var trEl = document.createElement("tr");
						     
						var rowHTML = 				 	
							"<td class='cs-statelist-detail'><input class='act'  type='checkbox' value='"+item.id+"' /></td>" +
				 			"<td class='cs-statelist-detail-id'>" + item.id + "</td>" +
                            "<td class='cs-statelist-detail'>" + item.path + "</td>" +
                            "<td class='cs-statelist-detail'>" + item.environment + "</td>" +
                            "<td class='cs-statelist-detail'>" + item.user + "</td>" +
				 			"<td class='cs-statelist-detail'>" + item.state + "</td>" +
                            "<td class='cs-statelist-detail'>" + item.action + "</td>" +
				 			"<td class='cs-statelist-detail'>" + item.scheduledDate + "</td>";
				 		trEl.innerHTML = rowHTML;
				 		queueTableEl.appendChild(trEl);
					}
				},
				failure: function(response) {
				},
				self: this
			};
			
			var serviceUri = "/api/1/services/api/1/deployment/get-deployment-queue.json?site="+CStudioAuthoringContext.site;

			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
	},

    cancelDeployment: function() {
        var items = document.getElementsByClassName('act');

        for(var i=0; i<items.length; i++) {
            if(items[i].checked == true) {
                list[list.length] = queueItems[i];
            }
        }

        for(var i=0;  i< list.length; i++) {
            var item = list[i];
            var path = item.path;

            var serviceUri = "/api/1/services/api/1/deployment/cancel-deployment.json?site="+CStudioAuthoringContext.site+"&path="+path+"&deploymentId="+item.id;

            cb = {
                success:function() {
                    alert("Deployment items canceled");
                    CStudioAdminConsole.Tool.DeploymentQueues.prototype.renderQueueTable();
                },
                failure: function() {
                    alert("Failed to cancel deployment");
                    CStudioAdminConsole.Tool.DeploymentQueues.prototype.renderQueueTable();
                }
            };

            YConnect.asyncRequest("POST", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
        }
    },
			
	setStates: function() {
		var items = document.getElementsByClassName('act');

		for(var i=0; i<items.length; i++) {
			if(items[i].checked == true) {
				list[list.length] = wfStates[i]; 
			}
		}

		var mySimpleDialog = new YAHOO.widget.SimpleDialog("dlg", { 
		    width: "20em", 
		    effect:{
		        effect: YAHOO.widget.ContainerEffect.FADE,
		        duration: 0.25
		    }, 
		    fixedcenter: true,
		    modal: true,
		    visible: false,
		    draggable: false
		});
		 
		var html = "";
		html = "<div width='300px'>"+
		    "<select id='setState'>"+
			    "<option value='NEW_UNPUBLISHED_LOCKED'>NEW_UNPUBLISHED_LOCKED</option>" +
				"<option value='NEW_UNPUBLISHED_UNLOCKED'>NEW_UNPUBLISHED_UNLOCKED</option>" +
	    		"<option value='NEW_SUBMITTED_WITH_WF_SCHEDULED'>NEW_SUBMITTED_WITH_WF_SCHEDULED</option>" +
	    		"<option value='NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED'>NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED</option>" +
	    		"<option value='NEW_SUBMITTED_WITH_WF_UNSCHEDULED'>NEW_SUBMITTED_WITH_WF_UNSCHEDULED</option>" +
	   		 	"<option value='NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED'>NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED</option>" +
	    		"<option value='NEW_SUBMITTED_NO_WF_SCHEDULED'>NEW_SUBMITTED_NO_WF_SCHEDULED</option>" +
	    		"<option value='NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED'>NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED</option>" +
	    		"<option value='NEW_SUBMITTED_NO_WF_UNSCHEDULED'>NEW_SUBMITTED_NO_WF_UNSCHEDULED</option>" +
	    		"<option value='NEW_PUBLISHING_FAILED'>NEW_PUBLISHING_FAILED</option>" +
	    		"<option value='NEW_DELETED'>NEW_DELETED</option>" +
	    		"<option value='EXISTING_UNEDITED_LOCKED'>EXISTING_UNEDITED_LOCKED</option>" +
	    		"<option value='EXISTING_UNEDITED_UNLOCKED'>EXISTING_UNEDITED_UNLOCKED</option>" +
	    		"<option value='EXISTING_EDITED_LOCKED'>EXISTING_EDITED_LOCKED</option>" +
	    		"<option value='EXISTING_EDITED_UNLOCKED'>EXISTING_EDITED_UNLOCKED</option>" +
	    		"<option value='EXISTING_SUBMITTED_WITH_WF_SCHEDULED'>EXISTING_SUBMITTED_WITH_WF_SCHEDULED</option>" +
	    		"<option value='EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED'>EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED</option>" +
	    		"<option value='EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED'>EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED</option>" +
	    		"<option value='EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED'>EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED</option>" +
	   			"<option value='EXISTING_SUBMITTED_NO_WF_SCHEDULED'>EXISTING_SUBMITTED_NO_WF_SCHEDULED</option>" +
	    		"<option value='EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED'>EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED</option>" +
	    		"<option value='EXISTING_SUBMITTED_NO_WF_UNSCHEDULED'>EXISTING_SUBMITTED_NO_WF_UNSCHEDULED</option>" +
	    		"<option value='EXISTING_PUBLISHING_FAILED'>EXISTING_PUBLISHING_FAILED</option>" +
	    		"<option value='EXISTING_DELETED'>EXISTING_DELETED</option>" +
	    	"</select><br/>" +
            CMgs.format(formsLangBundle, "setStatedDialogSystemProcessing")+": <input id='setProcessing' type='checkbox' value='false'/>" +
	    "</div>";
		
		var handleSet = function() {
			var state = document.getElementById('setState').value;
			var processing = document.getElementById('setProcessing').checked;
			
			for(var i=0;  i< list.length; i++) {
				var item = list[i];
				var path = item.path;
				var serviceUri = "/api/1/services/api/1/content/set-item-state.json?site="+CStudioAuthoringContext.site+"&path="+path+"&state="+state+"&systemprocessing="+processing;
				
				cb = { 
						success:function() {
							CStudioAdminConsole.Tool.WorkflowStates.prototype.renderStatesTable();
						}, 
						failure: function() {} 
				};

				YConnect.asyncRequest("POST", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
			}
			
			this.hide();
		};
		
		var handleCancel = function() {
		    this.hide();
		};

		var myButtons = [
    		{ text: CMgs.format(formsLangBundle, "setStatedDialogSetStates"), handler: handleSet },
    		{ text: CMgs.format(formsLangBundle, "cancel"), handler: handleCancel, isDefault:true}
		];

		mySimpleDialog.cfg.queueProperty("buttons", myButtons);
		mySimpleDialog.setHeader(CMgs.format(formsLangBundle, "setStatedDialogTitle"));
		mySimpleDialog.setBody(html);
		mySimpleDialog.render(document.body);
		mySimpleDialog.show();
		
	}
	

});

// add static function
CStudioAdminConsole.Tool.DeploymentQueues.selectAll = function() {
	var items = document.getElementsByClassName('act');
 
	for(var i=0; i<items.length; i++) {
		items[i].checked = true; 
	}
}
		
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-deployment-queues",CStudioAdminConsole.Tool.DeploymentQueues);