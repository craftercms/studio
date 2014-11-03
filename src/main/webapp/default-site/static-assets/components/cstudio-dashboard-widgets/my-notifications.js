var YDom = YAHOO.util.Dom;

if(typeof CStudioAuthoringWidgets == "undefined" || !CStudioAuthoringWidgets) {
   var CStudioAuthoringWidgets = {};
}

/**
 * my recent activity
 */
CStudioAuthoringWidgets.MyNotificationsDashboard = CStudioAuthoringWidgets.MyNotificationsDashboard || function(widgetId, pageId) {
	
	this.widgetId = widgetId;
	this.pageId = pageId;
	this._self =  this;
	this.showInprogressItems = false;
	this.expanded = true;
	this.hideEmptyRow = false;
	
	WcmDashboardWidgetCommon.init(this);
	
	/**
	 * get table data
	 */
	this.retrieveTableData = function(sortBy, sortAscDesc, callback, retrieveTableData, filterByNumber) { 
		
	sortAscDesc = CStudioAuthoring.Utils.sortByAsc.init(sortBy, widgetId);


	CStudioAuthoring.Service.getGoLiveQueueItems(
			CStudioAuthoringContext.site, 
			this.showInprogressItems, 
			sortBy, 
			sortAscDesc, 
			callback,
			filterByNumber);
	};
	
	 
	/**
	 * callback to render the table headings
	 */
	this.renderItemsHeading = function() {
	
		var widgetId = this._self.widgetId;
		
		var header = 
		"<th id='internalName-" + widgetId + "' class='minimize'>"+
			"<span>"+
				"<a href='#' id='sortInternalName-" + widgetId + "'>Page Name</a>"+
				"<span class='wcm-widget-margin'/>"+
			"</span>"+
			"<span id='sortIcon-internalName-" + widgetId + "' class='ttSortDesc wcm-go-live-sort-columns-" + widgetId + "' style='display:none'></span>"+
			"</span>"+
		"</th>"+
		
		"<th id='edit-" + widgetId + "' class='minimize'>"+
			"<a href='#' id='sortEdit-" + widgetId + "'>Edit</a>"+
		"</th>"+
		
		"<th id='browserUri-" + widgetId + "' class='maximize'>"+
			"<span>"+
				"<a href='#' id='sortBrowserUri-" + widgetId + "'>URL</a>"+
				"<span class='wcm-widget-margin' />"+
				"<span id='sortIcon-browserUri-" + widgetId + "' class='ttSortDesc wcm-go-live-sort-columns-" + widgetId + "' style='display:none'></span>"+
			"</span>" +
		"</th>"+
		
		"<th id='fullUri' class='width0'></th>"+
		
		"<th id='userLastName-" + widgetId + "' class='alignRight minimize'>"+
			"<span>"+
				"<a href='#' id='sortUserLastName-" + widgetId + "'>Last Edited By</a>"+
				"<span class='wcm-widget-margin' />" +
				"<span id='sortIcon-userLastName-" + widgetId + "' class='ttSortDesc wcm-go-live-sort-columns-" + widgetId + "' style='display:none'></span>" +
			"</span>" + 
		"</th>" +
		
		"<th id='eventDate-" + widgetId + "' class='ttThColLast alignRight minimize'>"+
			"<span>"+
				"<a href='#' id='sortEventDate-" + widgetId + "'>Last Edited</a>"+
				"<span class='wcm-widget-margin'/>"+
			"</span>"+
			"<span id='sortIcon-eventDate-" + widgetId + "' class='ttSortDesc wcm-go-live-sort-columns-" + widgetId + "' style='display:none'></span>"+
			"</span>" +
		"</th>";
		
		return header;	
	};

	/**
	 * Call back to render each line item of the table
	 */
	this.renderLineItem = function(item) {
	
		var itemStatusIconClass = CStudioAuthoring.Utils.getContentItemClassName(item);
		var itemNameClass = "";	
		var browserUri = browserUriValue = item.browserUri;
		var fullUri = item.uri;
		var userName = item.userLastName + ", " + item.userFirstName;
		var eventDate = item.eventDate; 
		var formattedDateTime =  CStudioAuthoring.Utils.formatDateFromString(item.eventDate); 	
		var itemName = item.internalName;
			 		
		if(item.newFile) {
			itemName += "*";
		}

                // this API will replace double quotes with ASCII character
                // to resolve page display issue
                itemName = CStudioAuthoring.Utils.replaceWithASCIICharacter(itemName);

		var itemRow =	 
			"<td>"+
				"<span class='ttFirstCol15'>"+
					"<input type='checkbox'/>"+
				"</span>" +
				"<span class='wcm-widget-margin'></span>"+
				"<span class='" +itemStatusIconClass+ "' />"+
				"<span class='" +itemNameClass+ "'>"+
					"<a href='#' class='previewLink'>" +itemName+ "</a>"+
				"</span>" +
			"</td>"+
			"<td>" +
				"<a href='#' class='editLink'>Edit</a>"+
			"</td>" +
			"<td>"+browserUri+"</td>"+
			"<td title='fullUri' class='width0'>" +fullUri+"</td>"+
			"<td class='alignRight'>" +userName+"</td>" +
			"<td class='ttThColLast alignRight'>"+formattedDateTime+"</td>";
		
		return itemRow;
	};
	
	
};
