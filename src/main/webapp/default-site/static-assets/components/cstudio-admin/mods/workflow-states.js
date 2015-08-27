CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/workflow-states.css");
CStudioAdminConsole.Tool.WorkflowStates = CStudioAdminConsole.Tool.WorkflowStates ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}
var list = [];
var wfStates = [];
/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.WorkflowStates, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='state-list'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderJobsList();
	},
	
	renderJobsList: function() {
		
		var actions = [
				{ name: CMgs.format(formsLangBundle, "setStatedDialogSetStates"), context: this, method: this.setStates }
		];
		CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
		this.renderStatesTable();

	},
	
	renderStatesTable: function () {
		var stateLisEl = document.getElementById("state-list");
		stateLisEl.innerHTML = 
		"<table id='statesTable' class='cs-statelist'>" +
			 	"<tr>" +
				 	"<th class='cs-statelist-heading'><a href='#' onclick='CStudioAdminConsole.Tool.WorkflowStates.selectAll(); return false;'>"+CMgs.format(langBundle, "setStatedTabSelectAll")+"</a></th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setStatedTabID")+"</th>" +
    			 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setStatedTabState")+"</th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setStatedTabSystemProcessing")+"</th>" +
				 "</tr>" + 
			"</table>";
	
			cb = {
				success: function(response) {
					var states = eval("(" + response.responseText + ")");
					wfStates = states.items;
					
					var statesTableEl = document.getElementById("statesTable");
					for(var i=0; i<states.items.length; i++) {
						var state = states.items[i];
						var trEl = document.createElement("tr");
						     
						var rowHTML = 				 	
							"<td class='cs-statelist-detail'><input class='act'  type='checkbox' value='"+state.path+"' /></td>" +
				 			"<td class='cs-statelist-detail-id'>" + state.path + "</td>" +
				 			"<td class='cs-statelist-detail'>" + state.state + "</td>" +
				 			"<td class='cs-statelist-detail'>" + CMgs.format(langBundle, (state.systemProcessing=="1").toString()) + "</td>";
				 		trEl.innerHTML = rowHTML;
				 		statesTableEl.appendChild(trEl);
					}
				},
				failure: function(response) {
				},
				self: this
			};
			
			var serviceUri = "/api/1/services/api/1/content/get-item-states.json?site="+CStudioAuthoringContext.site+"&state=ALL";

			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
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
CStudioAdminConsole.Tool.WorkflowStates.selectAll = function() {
	var items = document.getElementsByClassName('act');
 
	for(var i=0; i<items.length; i++) {
		items[i].checked = true; 
	}
}
		
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-workflow-states",CStudioAdminConsole.Tool.WorkflowStates);