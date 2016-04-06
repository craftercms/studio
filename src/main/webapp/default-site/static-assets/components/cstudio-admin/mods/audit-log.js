/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/audit-log.css");
CStudioAdminConsole.Tool.AuditLog = CStudioAdminConsole.Tool.AuditLog ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
};
var list = [];
var auditLog = [];
/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.AuditLog, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='auditlog-list'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderAuditLogList();
	},
	
	renderAuditLogList: function() {
			
		this.renderAuditLogTable();

	},
	
	renderAuditLogTable: function () {
		var auditLogListEl = document.getElementById("auditlog-list");
		auditLogListEl.innerHTML =
		"<table id='auditLogTable' class='cs-auditloglist'>" +
			 	"<tr>" +
				 	"<th class='cs-auditloglist-heading'>"+CMgs.format(langBundle, "setAuditLogTabID")+"</th>" +
    			 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setAuditLogTabUser")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setAuditLogTabPath")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setAuditLogTabContentType")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setAuditLogTabModifiedDate")+"</th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setAuditLogTabActivityType")+"</th>" +
				 "</tr>" + 
			"</table>";
	
			cb = {
				success: function(response) {
					var aLog = eval("(" + response.responseText + ")");
					auditLog = aLog.items;
					
					var auditLogTableEl = document.getElementById("auditLogTable");
					for(var i=0; i<aLog.items.length; i++) {
						var logItem = aLog.items[i];
						var trEl = document.createElement("tr");
						     
						var rowHTML =
				 			"<td class='cs-auditloglist-detail-id'>" + logItem.id + "</td>" +
				 			"<td class='cs-auditloglist-detail'>" + logItem.userId + "</td>" +
                            "<td class='cs-auditloglist-detail'>" + logItem.contentId + "</td>" +
                            "<td class='cs-auditloglist-detail'>" + logItem.contentType + "</td>" +
                            "<td class='cs-auditloglist-detail'>" + logItem.modifiedDate + "</td>" +
                            "<td class='cs-auditloglist-detail'>" + logItem.type + "</td>";
				 		trEl.innerHTML = rowHTML;
				 		auditLogTableEl.appendChild(trEl);
					}
				},
				failure: function(response) {
				},
				self: this
			};
			
			var serviceUri = "/api/1/services/api/1/activity/get-audit-log.json?site="+CStudioAuthoringContext.site+"&startpos=0&num=20";

			YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-audit-log",CStudioAdminConsole.Tool.AuditLog);