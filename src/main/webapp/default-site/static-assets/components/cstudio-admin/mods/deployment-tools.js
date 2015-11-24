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

CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/deployment-tools.css");
CStudioAdminConsole.Tool.DeploymentTools = CStudioAdminConsole.Tool.DeploymentTools ||  function(config, el)  {
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
YAHOO.extend(CStudioAdminConsole.Tool.DeploymentTools, CStudioAdminConsole.Tool, {
    renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='deployment-tool-area'>" +
			"</div>";
			
			var actions = [];

			CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
			this.renderDeploymentTools();
	},
	
	renderQueueList: function() {
		
		var actions = [
				{ name: CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment"), context: this, method: this.cancelDeployment }
		];
		CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
			
		this.renderQueueTable();

	},

    renderDeploymentQueue: function () {
        var queueListEl = document.getElementById("deployment-tool-area");
        var me = this;
        queueListEl.innerHTML =
            "<div class='cs-statelist'><button id='btnCancelTop' type='button' >" + CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment") + "</button>" +
            "<table id='queueTable'>" +
            "<tr>" +
            "<th class='cs-statelist-heading'><a href='#' onclick='CStudioAdminConsole.Tool.DeploymentTools.selectAll(); return false;'>"+CMgs.format(langBundle, "setQueueTabSelectAll")+"</a></th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabID")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabPath")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabEnvironment")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabUser")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabState")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabAction")+"</th>" +
            "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabScheduledDate")+"</th>" +
            "</tr>" +
            "</table><button type='button' id='btnCancelBot'>" + CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment") + "</button></div>" ;

        var btnTopEl = document.getElementById("btnCancelTop");
        var btnBotEl = document.getElementById("btnCancelBot");

        btnTopEl.addEventListener("click", function(){
            me.cancelDeployment();
        });
        btnBotEl.addEventListener("click", function(){
            me.cancelDeployment();
        });


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

    renderDeploymentEndpoints: function () {
        var containerEl = document.getElementById("deployment-tool-area");
        containerEl.innerHTML =
            "<div class='deployment-endpoint-window'>" +
            "<div id='endpoint-select'>" +
            "<select id='endpoint-list' class='cs-endpoint-list'>" +
            " <option value='' >"+CMgs.format(formsLangBundle, "deploymentTabSelectEndpoint")+"</option>" +
            "</select></div>" +
            "<div id='endpoint-details'></div>" +
            "<div id='endpoint-sync-queue'></div>" +
            "</div>";

        var endpointSelEl = document.getElementById("endpoint-list");
        this.context.loadDeploymentEndpoints(endpointSelEl);
    },

    /*
     * populate the list of configuration files
     */
    loadDeploymentEndpoints: function (itemSelectEl) {
        // load configuration to get the configuration files list

        var loadEndpointsCb = {
            success: function(response) {
                var endpointsResponse = eval("(" + response.responseText + ")");
                endpoints = endpointsResponse.endpoints;
                if (endpoints.length) {
                    //var index = 1;
                    for (var index = 0; index < endpoints.length; index++) {
                        var endpoint = endpoints[index];
                        var option = new Option(endpoint.name, endpoint.name, false, false);
                        option.setAttribute("json", JSON.stringify(endpoint));
                        //option.setAttribute("sample", fileConfig.samplePath);
                        itemSelectEl.options[index+1] = option;
                    }
                } else {
                    var endpoint = endpoints[0];
                    var option = new Option(endpoint.name, endpoint.name, false, false);
                    option.setAttribute("json", JSON.stringify(endpoint));
                    itemSelectEl.options[1] = option;
                }
            },
            failure: function() {
                alert("Failed to load endpoints");
            },
            self: this
        };

        var serviceUri = "/api/1/services/api/1/deployment/get-deployment-endpoints.json?site="+CStudioAuthoringContext.site;

        YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), loadEndpointsCb);

        // add onchange behavior to display selected
        itemSelectEl.onchange = function() {
            var endpointDetailsEl = document.getElementById("endpoint-details");
            endpointDetailsEl.innerHTML = "";

            var endpointSyncQueuEl = document.getElementById("endpoint-sync-queue");
            endpointSyncQueuEl.innerHTML = "";

            var selectedIndex = itemSelectEl.selectedIndex;
            if(selectedIndex != 0) {
                endpointDetailsEl.innerHTML =
                    "<table id='endpointTable' class='cs-statelist'>" +
                    "<tr id='endpointTableHeader'>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(formsLangBundle, "deploymentTabEndpointProperty")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(formsLangBundle, "deploymentTabEndpointValue")+"</th>" +
                    "</tr>" +
                    "</table>";

                endpointSyncQueuEl.innerHTML =
                    "<table id='syncQueueTable' class='cs-statelist'>" +
                    "<tr>" +
                    "<th class='cs-statelist-heading'><a href='#' onclick='CStudioAdminConsole.Tool.DeploymentQueues.selectAll(); return false;'>"+CMgs.format(langBundle, "setQueueTabSelectAll")+"</a></th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabID")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabPath")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabOldPath")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabEnvironment")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabUser")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabVersion")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabAction")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabContentTypeClass")+"</th>" +
                    "</tr>" +
                    "</table>";
                var endpointJson = itemSelectEl[selectedIndex].getAttribute("json");
                var endpoint = eval("(" + endpointJson + ")");



                var endpointTableEl = document.getElementById("endpointTable");

                var trEl = document.createElement("tr");

                var rowHTML =
                    "<td class='cs-statelist-detail'>Name</td>" +
                    "<td class='cs-statelist-detail'>" + endpoint.name + "</td>";
                trEl.innerHTML = rowHTML;
                endpointTableEl.appendChild(trEl);
                trEl = document.createElement("tr");
                rowHTML =
                    "<td class='cs-statelist-detail'>Target</td>" +
                    "<td class='cs-statelist-detail'>" + endpoint.target + "</td>";
                trEl.innerHTML = rowHTML;
                endpointTableEl.appendChild(trEl);
                trEl = document.createElement("tr");
                rowHTML =
                    "<td class='cs-statelist-detail'>Status</td>" +
                    "<td class='cs-statelist-detail' id='endpointStatus'></td>";
                trEl.innerHTML = rowHTML;
                endpointTableEl.appendChild(trEl);
                trEl = document.createElement("tr");
                rowHTML =
                    "<td class='cs-statelist-detail'>Version</td>" +
                    "<td class='cs-statelist-detail' id='endpointVersion'></td>";
                trEl.innerHTML = rowHTML;
                endpointTableEl.appendChild(trEl);

                // load configuration into editor
                var getStatusCb = {
                    success: function(response) {
                        var statusResponse = eval("(" + response.responseText + ")");
                        var statusEl = document.getElementById("endpointStatus");
                        statusEl.innerHTML = statusResponse.status;
                    },
                    failure: function(error) {
                        var statusEl = document.getElementById("endpointStatus");
                        statusEl.innerHTML = "FAILED"
                    }
                };
                YAHOO.util.Connect.asyncRequest('GET', endpoint.statusUrl, getStatusCb);

                var getVersionCb = {
                    success: function(response) {
                        var versionResponse = response.responseText;
                        var versionEl = document.getElementById("endpointVersion");
                        versionEl.innerHTML = versionResponse;

                        var getSyncQueueCb = {
                            success: function(response) {
                                var syncQueue = eval("(" + response.responseText + ")");
                                var syncQueueItems = syncQueue.queue;

                                var syncQueueTableEl = document.getElementById("syncQueueTable");
                                for(var i=0; i<syncQueueItems.length; i++) {
                                    var item = syncQueueItems[i];
                                    var trEl = document.createElement("tr");

                                    var rowHTML =
                                        "<td class='cs-statelist-detail'><input class='act'  type='checkbox' value='"+item.id+"' /></td>" +
                                        "<td class='cs-statelist-detail-id'>" + item.id + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.path + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.oldPath + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.environment + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.user + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.version + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.action + "</td>" +
                                        "<td class='cs-statelist-detail'>" + item.contentTypeClass + "</td>";
                                    trEl.innerHTML = rowHTML;
                                    syncQueueTableEl.appendChild(trEl);
                                }
                            },
                            failure: function(error) {
                            }
                        };

                        var serviceUri = "/api/1/services/api/1/deployment/get-sync-target-queue.json?site="+CStudioAuthoringContext.site +
                            "&endpoint=" + endpoint.name + "&targetVersion=1";//+versionResponse;
                        YAHOO.util.Connect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(serviceUri), getSyncQueueCb);
                    },
                    failure: function(error) {
                        var versionEl = document.getElementById("endpointVersion");
                        versionEl.innerHTML = "FAILED"
                    }
                };
                YAHOO.util.Connect.asyncRequest('GET', endpoint.versionUrl+"?target="+endpoint.target, getVersionCb);



            }
        };
    },
	
	renderQueueTable: function () {
		var queueListEl = document.getElementById("queue-list");
		queueListEl.innerHTML =
            "<div class='cs-statelist'><button id='btnCancelTop' type='button'>" + CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment") + "</button>" +
		    "<table id='queueTable' >" +
			 	"<tr>" +
				 	"<th class='cs-statelist-heading'><a href='#' onclick='CStudioAdminConsole.Tool.DeploymentTools.selectAll(); return false;'>"+CMgs.format(langBundle, "setQueueTabSelectAll")+"</a></th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabID")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabPath")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabEnvironment")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabUser")+"</th>" +
    			 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabState")+"</th>" +
                    "<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabAction")+"</th>" +
				 	"<th class='cs-statelist-heading'>"+CMgs.format(langBundle, "setQueueTabScheduledDate")+"</th>" +
				 "</tr>" + 
			"</table><button type='button' id=''btnCancelBot>" + CMgs.format(formsLangBundle, "setQueueDialogCancelDeployment") + "</button></div>";

        var btnTopEl = document.getElementById("btnCancelTop");
        var btnBotEl = document.getElementById("btnCancelBot");

        btnTopEl.addEventListener("click", function(){
            me.cancelDeployment();
        });
        btnBotEl.addEventListener("click", function(){
            me.cancelDeployment();
        });

	
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
        alert("BRLE!");
        var items = document.getElementsByClassName('act');

        for(var idx=0; idx<items.length; idx++) {
            if(items[idx].checked == true) {
                list[list.length] = queueItems[idx];
            }
        }

        for(var i=0;  i< list.length; i++) {
            var item = list[i];
            var path = item.path;

            var serviceUri = "/api/1/services/api/1/deployment/cancel-deployment.json?site="+CStudioAuthoringContext.site+"&path="+path+"&deploymentId="+item.id;

            cb = {
                success:function() {
                    alert("Deployment items canceled");
                    CStudioAdminConsole.Tool.DeploymentTools.prototype.renderQueueTable();
                },
                failure: function() {
                    alert("Failed to cancel deployment");
                    CStudioAdminConsole.Tool.DeploymentTools.prototype.renderQueueTable();
                }
            };

            YConnect.asyncRequest("POST", CStudioAuthoring.Service.createServiceUri(serviceUri), cb);
        }
    },

    renderDeploymentJobs: function () {
        var containerEl = document.getElementById("deployment-tool-area");
        containerEl.innerHTML =
            "<div id='jobsDetails' class='deployment-jobs-window'>" +
            "</div>";
        this.context.loadDeploymentJobs();
    },

    /*
     * populate the list of deployment jobs
     */
    loadDeploymentJobs: function () {

        var loadJobsCb = {
            success: function(response) {
                var jobsResponse = eval("(" + response.responseText + ")");
                jobs = jobsResponse.jobs;
                var containerEl = document.getElementById("jobsDetails");
                if (jobs.length) {
                    //var index = 1;
                    for (var index = 0; index < jobs.length; index++) {
                        var job = jobs[index];
                        var jobHTML =
                            "<span>ID:&nbsp;</span>" + job.id + "<br/>" +
                            "<span>Name:&nbsp;</span>" + job.name + "<br/>" +
                            "<span>Host:&nbsp;</span>" + job.host + "<br/>" +
                            "<span>Enabled:&nbsp;</span>" + job.enabled + "<br/>" +
                            "<span>Running:&nbsp;</span>" + job.running + "<br/><hr/>";
                        containerEl.innerHTML = containerEl.innerHTML + jobHTML;
                    }
                } else {
                    var job = jobs[0];
                    var jobHTML =
                        "<span>ID:&nbsp;</span>" + job.id + "<br/>" +
                        "<span>Name:&nbsp;</span>" + job.name + "<br/>" +
                        "<span>Host:&nbsp;</span>" + job.host + "<br/>" +
                        "<span>Enabled:&nbsp;</span>" + job.enabled + "<br/>" +
                        "<span>Running:&nbsp;</span>" + job.running + "<br/>";
                    containerEl.innerHTML = jobHTML;
                }
            },
            failure: function() {
                alert("Failed to load jobs");
            },
            self: this
        };

        var serviceUri = "/api/1/services/api/1/deployment/get-deployment-jobs.json?site="+CStudioAuthoringContext.site;

        YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), loadJobsCb);

    },

    renderDeploymentTools: function() {

        var actions = [
            { name: CMgs.format(formsLangBundle, "setDeploymentQueue"), context: this, method: this.renderDeploymentQueue },
            { name: CMgs.format(formsLangBundle, "setDeploymentEndpoints"), context: this, method: this.renderDeploymentEndpoints },
            { name: CMgs.format(formsLangBundle, "setDeploymentJobs"), context: this, method: this.renderDeploymentJobs }
        ];
        CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);

        this.renderDeploymentQueue();
    }

});

// add static function
CStudioAdminConsole.Tool.DeploymentTools.selectAll = function() {
	var items = document.getElementsByClassName('act');
 
	for(var i=0; i<items.length; i++) {
		items[i].checked = true; 
	}
};
		
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-deployment-tools",CStudioAdminConsole.Tool.DeploymentTools);