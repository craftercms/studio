var YDom = YAHOO.util.Dom;
var contextPath = location.protocol+"//"+location.hostname+":"+location.port;  

if(typeof CStudioAuthoringWidgets == "undefined" || !CStudioAuthoringWidgets) {
   var CStudioAuthoringWidgets = {};
}

/**
 * recently made live
 */
CStudioAuthoringWidgets.RecentlyMadeLiveDashboard = CStudioAuthoringWidgets.RecentlyMadeLiveDashboard || function(widgetId, pageId) {
	
	this.widgetId = widgetId;
	this.pageId = pageId;
	this._self =  this;
	this.showInprogressItems = false;
	this.expanded = true;
	this.hideEmptyRow = false;
	this.defaultSortBy='eventDate';
	this.defaultSearchNumber=20;
	this.tooltipLabels=null;
	WcmDashboardWidgetCommon.init(this);
	
	/**
	 * get table data
	 */
	this.retrieveTableData = function(sortBy, sortAscDesc, callback, retrieveTableData, filterByNumber,filterBy) { 	

		sortAscDesc = CStudioAuthoring.Utils.sortByAsc.init(sortBy, widgetId);

		CStudioAuthoring.Service.getDeploymentHistory(
			CStudioAuthoringContext.site,
			sortBy,
			sortAscDesc,
			30, // what should this be 
			filterByNumber,
			filterBy,
			callback);
	};
	 
	/**
	 * callback to render the table headings
	 */
	this.renderItemsHeading = function() {
	
		var widgetId = this._self.widgetId;
		
		var header = WcmDashboardWidgetCommon.getDefaultSortRow("eventDate",widgetId,CMgs.format(langBundle, "dashletRecentDeployColMadeLiveDateDate"),"minimize")+
                     WcmDashboardWidgetCommon.getSimpleRow("edit",widgetId,CMgs.format(langBundle, "dashletRecentDeployColEdit"),"minimize")+
		             WcmDashboardWidgetCommon.getSimpleRow("browserUri",widgetId,CMgs.format(langBundle, "dashletRecentDeployColURL"),"maximize")+
                     WcmDashboardWidgetCommon.getSimpleRow("endpoint",widgetId,CMgs.format(langBundle, "dashletRecentDeployColEndpoint"),"minimize")+
		             "<th id='fullUri' class='width0'></th>"+		             
                     WcmDashboardWidgetCommon.getSimpleRow("madeliveDate",widgetId,CMgs.format(langBundle, "dashletRecentDeployColDeployBy"),"ttThColLast alignRight minimize");
		
		return header;	
	};

	this.renderAuxControls = function(containerEl) {
		var liLoadingEl = document.createElement("li");
		liLoadingEl.id = "loading-" + widgetId;		
		var imgEl = document.createElement("img");		
		imgEl.src = contextPath + CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";		
		liLoadingEl.appendChild(imgEl);
		containerEl.appendChild(liLoadingEl);
		var filterBydiv =  document.createElement("div");
		YDom.addClass(filterBydiv, "widget-FilterBy");

		var widgetFilterBy = CStudioAuthoring.Service.getWindowState(CStudioAuthoringContext.user,
										pageId,
										widgetId,
										"widgetFilterBy");
		var filterByEl = WcmDashboardWidgetCommon.initFilterToWidget(widgetId, widgetFilterBy);
		containerEl.appendChild(filterBydiv);
		filterBydiv.appendChild(filterByEl);
		
		filterByEl._self = this;
		
		filterByEl.onchange = function() {
			
			var _self = this._self;
			var selectedItems = filterByEl.selectedIndex;
			filterByEl.options[0]=new Option(CMgs.format(langBundle, "dashletFilterPages"), "pages", true, false);
			filterByEl.options[1]=new Option(CMgs.format(langBundle, "dashletFilterComponents"), "components", true, false);
			filterByEl.options[2]=new Option(CMgs.format(langBundle, "dashletFilterDocuments"), "documents", true, false);
			filterByEl.options[3]=new Option(CMgs.format(langBundle, "dashletFilterAll"), "all", true, false);
			filterByEl.options[selectedItems].selected =true;
			var newState = filterByEl.value;

			CStudioAuthoring.Service.setWindowState(CStudioAuthoringContext.user,
								pageId,
								widgetId,
								"widgetFilterBy",
								newState);

			var sortBy=_self.currentSortBy? _self.currentSortBy:_self.defaultSortBy;
			var searchNumber=_self.searchNumber? _self.searchNumber:_self.defaultSearchNumber;
			WcmDashboardWidgetCommon.loadFilterTableData(
									sortBy,
									YDom.get(_self.widgetId),
									_self.widgetId,searchNumber,filterByEl.value);

		};
	};
	
	/**
	 * Call back to render each line item of the table
	 */
	this.renderLineItem = function(item, isFirst, count) {

        var html = [],

            name = item.internalName,
            endpoint = item.endpoint,
            displayEndpoint = item.endpoint,
            displayName = WcmDashboardWidgetCommon.getFormattedString(name, 40),
            editLinkId;

        if (isFirst) {

            html.push('<td>');

            if (item.numOfChildren > 0) {
                var parentClass = ['wcm-table-parent-', name, '-', count].join("");
                html = html.concat([
                    '<span id="', parentClass, '"',
                        'class="', item.children.length ? 'ttClose parent-div-widget' : 'ttOpen parent-div-widget', '"',
                        'onclick="WcmDashboardWidgetCommon.toggleLineItem(\'' + parentClass + '\');" >',
                    '</span>'
                ]);
            }

            html = html.concat([
                '<span class="wcm-widget-margin-align" title="', name, '">',
                    displayName, ' (', item.numOfChildren, ')',
                '</span>',
                '</td>',
                '<td colspan="4">&nbsp;</td>'
            ]);

        } else {

            var browserUri = item.browserUri,
                displayBrowserUri = WcmDashboardWidgetCommon.getFormattedString(browserUri, 80),
                uri = item.uri;

            editLinkId = 'editLink_' + this.widgetId + '_' + WcmDashboardWidgetCommon.encodePathToNumbers(item.uri);

            if (item.component && item.internalName != "crafter-level-descriptor.level.xml") {
                browserUri = "";
                displayBrowserUri = "";
            }
/*
            if (item.internalName == "crafter-level-descriptor.level.xml") {
                displayName = "Section Defaults" + (item.newFile?"*":"");
            }
*/
            var ttSpanId =  "tt_" + this.widgetId + "_" + item.uri + "_" + (this.tooltipLabels.length + 1);
            var itemTitle = CStudioAuthoring.Utils.getTooltipContent(item);
            this.tooltipLabels.push(ttSpanId);

            var itemIconStatus = CStudioAuthoring.Utils.getIconFWClasses(item);
            itemIconStatus += ((item.disabled && !item.previewable) ? ' non-previewable-disabled' : '');

            // this API will replace double quotes with ASCII character
            // to resolve page display issue
            displayName = CStudioAuthoring.Utils.replaceWithASCIICharacter(displayName);

            if(!item.deleted && item.uri.indexOf(".xml") != -1) {
            	WcmDashboardWidgetCommon.insertEditLink(item, editLinkId);
            }
            
            html = html.concat([
                '<td>',
                    '<div class="dashlet-cell-wrp">',
                        '<div class="dashlet-ident">',
                            '<input type="checkbox" class="dashlet-item-check" id="', uri, '"', ((item.deleted || item.inFlight) ? ' disabled' : ''), '  />',
                            '<span class="', itemIconStatus, '" id="' + ttSpanId + '" title="' + itemTitle + '">',
                                '<a href="#" class="', (item.previewable == true) ? "previewLink" : "non-previewable-link", '">',
                                    displayName,
                                '</a>',
                            '</span>',
                        '</div>',
                    '</div>',
                '</td>',
                '<td id="' + editLinkId + '"></td>',
                "<td title='",browserUri,"'>", displayBrowserUri, "</td>",
                "<td title='fullUri' class='width0'>", uri, "</td>",
                "<td title='",endpoint,"'>", displayEndpoint, "</td>",
                "<td class='alignRight ttThColLast'>", CStudioAuthoring.Utils.formatDateFromString(item.eventDate), "</td>"
            ]);
        }

        return html.join("");
    };
	
	
};
