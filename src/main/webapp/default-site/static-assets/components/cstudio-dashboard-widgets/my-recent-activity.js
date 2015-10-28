var YDom = YAHOO.util.Dom;
var contextPath = location.protocol+"//"+location.hostname+":"+location.port; 

if(typeof CStudioAuthoringWidgets == "undefined" || !CStudioAuthoringWidgets) {
   var CStudioAuthoringWidgets = {};
}

/**
 * my recent activity
 */
CStudioAuthoringWidgets.MyRecentActivityDashboard = CStudioAuthoringWidgets.MyRecentActivityDashboard || function(widgetId, pageId) {

	this.widgetId = widgetId;
	this.pageId = pageId;
	this._self =  this;
	this.excludeLiveItems = false;
	this.expanded = true;
	this.hideEmptyRow = false;
	this.defaultSortBy='eventDate';
	this.defaultSearchNumber=10;
	this.tooltipLabels=null;
	WcmDashboardWidgetCommon.init(this);

	/**
	 * get table data
	 */
	this.retrieveTableData = function(sortBy, sortAscDesc, callback, retrieveTableData, filterByNumber,filterBy) {

		sortAscDesc = CStudioAuthoring.Utils.sortByAsc.init(sortBy, widgetId);

		CStudioAuthoring.Service.getUserActivitiesServices(
			CStudioAuthoringContext.site,
			CStudioAuthoringContext.user,
			sortBy,
			sortAscDesc,
			filterByNumber,
			filterBy,
			this.excludeLiveItems,
			callback);
	};

	/**
	 * render widget specific controls
	 */
	this.renderAuxControls = function(containerEl) {

		var listItemEl = document.createElement("li");
		var itemFilterEl = document.createElement("a");

		/**
		 * adding loading image to go-live0queue widget
		 */
		
		var liLoadingEl = document.createElement("li");
		liLoadingEl.id = "loading-" + widgetId;		
		var imgEl = document.createElement("img");
		imgEl.src = contextPath + CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";
		liLoadingEl.appendChild(imgEl);

		itemFilterEl.innerHTML = CMgs.format(langBundle, "dashletMyRecentActivityHideLiveItems");
		itemFilterEl.href = "javascript:void(0);";
		itemFilterEl.id = "widget-expand-state-" + widgetId;
        listItemEl.appendChild(itemFilterEl);
        YDom.addClass(itemFilterEl, "widget-expand-state btn btn-default btn-sm");

		var filterBydiv = document.createElement("li");
        // YDom.addClass(filterBydiv, "widget-FilterBy");

        var widgetFilterBy = CStudioAuthoring.Service.getWindowState(
            CStudioAuthoringContext.user,
            pageId,
            widgetId,
            "widgetFilterBy");

        var filterByEl = WcmDashboardWidgetCommon.initFilterToWidget(widgetId, widgetFilterBy);
        filterBydiv.appendChild(filterByEl);

        containerEl.appendChild(liLoadingEl);
        containerEl.appendChild(listItemEl);
        containerEl.appendChild(filterBydiv);

		itemFilterEl._self = this;
		
		filterByEl._self = this;
		
		filterByEl.onchange = function() {
			
			var _self = this._self;
			var selectedItems = filterByEl.selectedIndex;
			
			filterByEl.options[0]=new Option("Pages", "pages", true, false);
			filterByEl.options[1]=new Option("Components", "components", true, false);
			filterByEl.options[2]=new Option("Documents", "documents", true, false);
			filterByEl.options[3]=new Option("All", "all", true, false);
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

		
		itemFilterEl.onclick = function() {
			var _self = this._self;

			_self.excludeLiveItems = !_self.excludeLiveItems;

			if(!_self.excludeLiveItems) {
				this.innerHTML = CMgs.format(langBundle, "dashletMyRecentActivityHideLiveItems");
			}
			else {
				this.innerHTML = CMgs.format(langBundle, "dashletMyRecentActivityShowLiveItems");
			}
			var sortBy=_self.currentSortBy? _self.currentSortBy:_self.defaultSortBy;
			var searchNumber=_self.searchNumber? _self.searchNumber:_self.defaultSearchNumber;
            
			WcmDashboardWidgetCommon.loadFilterTableData(
				sortBy,
				YDom.get(_self.widgetId),
				_self.widgetId,searchNumber,filterByEl.value);
		};
	};

	/**
	 * callback to render the table headings
	 */
	this.renderItemsHeading = function() {

		var widgetId = this._self.widgetId,
            Common = WcmDashboardWidgetCommon;

        var header = [
            Common.getSimpleRow("checkAll", widgetId, '<input title="Select all" class="dashlet-item-check" id="' + widgetId + 'CheckAll" name="check-all" type="checkbox"/>', "minimize"),
            Common.getSortableRow("internalName", widgetId,  CMgs.format(langBundle, "dashletMyRecentActivityColPageName"), "minimize"),
            Common.getSimpleRow("edit", widgetId, CMgs.format(langBundle, "dashletMyRecentActivityColEdit"), "minimize"),
            Common.getSortableRow("browserUri", widgetId, CMgs.format(langBundle, "dashletMyRecentActivityColURL"), "maximize"),
            '<th id="fullUri" class="hide"></th>',
            Common.getSimpleRow("scheduledDate", widgetId, CMgs.format(langBundle, "dashletMyRecentActivityColPublishDate"), ""),
            Common.getSortableRow("userLastName", widgetId, CMgs.format(langBundle, "dashletMyRecentActivityColLastEditedBy"), "alignRight minimize"),
            Common.getSortableRow("eventDate",widgetId,CMgs.format(langBundle, "dashletMyRecentActivityColMyLastEdit"),"ttThColLast alignRight minimize")
        ].join('');

		return header;
	};

	/**
	 * Call back to render each line item of the table
	 */
	this.renderLineItem = function(item) {

			var itemName = item.internalName;
			if (!itemName || itemName == "") {
				itemName = item.title;
			}
			if (!itemName || itemName == "") {
				itemName = item.name;
			}
            var browserUri = item.browserUri,
                fullUri = item.uri,
                editLinkId = 'editLink_' + this.widgetId + '_' + WcmDashboardWidgetCommon.encodePathToNumbers(item.uri),

                fmt = CStudioAuthoring.Utils.formatDateFromString;

            //reducing max character length to support 1024 screen resolution
            var removeCharCount = ((window.innerWidth <= 1024)?5:0);
            var displayBrowserUri = WcmDashboardWidgetCommon.getFormattedString(browserUri, (80 - removeCharCount));
            var itemNameForDisplay = WcmDashboardWidgetCommon.getFormattedString(itemName, (40 - removeCharCount), item.newFile);
            var ttSpanId =  "tt_" + this.widgetId + "_" + item.uri + "_" + (this.tooltipLabels.length + 1);
            var itemTitle = CStudioAuthoring.Utils.getTooltipContent(item);
            this.tooltipLabels.push(ttSpanId);

            if (item.component && item.internalName != "crafter-level-descriptor.level.xml") {
                browserUri = "";
                displayBrowserUri = "";
            }
/*
            if (item.internalName == "crafter-level-descriptor.level.xml") {
                itemNameForDisplay = "Section Defaults" + (item.newFile?"*":"");
            }
*/

        var itemIconStatus = CStudioAuthoring.Utils.getIconFWClasses(item);
        itemIconStatus += ((item.disabled && !item.previewable) ? ' non-previewable-disabled' : '');

        // this API will replace double quotes with ASCII character
        // to resolve page display issue
        itemNameForDisplay = CStudioAuthoring.Utils.replaceWithASCIICharacter(itemNameForDisplay);

        WcmDashboardWidgetCommon.insertEditLink(item, editLinkId);
       	
        var itemRow = [
			'<td style="padding-right:0px">',
				'<div class="dashlet-ident">',
                    '<input type="checkbox" class="dashlet-item-check" id="', this.widgetId, '-', item.uri, '"', ((item.deleted || item.inFlight) ? ' disabled' : ''), '  />',
                '</div>',
			'</td>',
            '<td style="padding-left:0px">' +
                '<span class="', itemIconStatus, (item.disabled == true ? ' disabled' : ''), '" id="' + ttSpanId + '" title="' + itemTitle + '">',
                    '<a ', (item.previewable == true) ? 'href="/studio/preview/#/?page='+browserUri+'/&site='+CStudioAuthoringContext.site+'"' : '', ' class="', (item.previewable == true ? 'previewLink' : 'non-previewable-link'), '">',
                    itemNameForDisplay,  (item.isNew == true) ? ' <span style="font-size:16px;">*</span>' : '',
                    '</a>',
                '</span>',
            '</td>',
			'<td id="' + editLinkId + '"></td>',
			'<td title="', browserUri, '">', displayBrowserUri, '</td>',
			'<td title="fullUri" class="width0">', fullUri, '</td>',
            '<td class="">', item.scheduled ? fmt(item.scheduledDate, 'tooltipformat') : '', '</td>',
			'<td class="alignRight">', WcmDashboardWidgetCommon.getDisplayName(item), '</td>',
			'<td class="ttThColLast alignRight">', CStudioAuthoring.Utils.formatDateFromString(item.eventDate), '</td>'
        ];

		return itemRow.join('');
	};
};
