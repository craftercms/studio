var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;
var subChildren = null;

if (typeof WcmDashboardWidgetCommon == "undefined" || !WcmDashboardWidgetCommon) {
    var WcmDashboardWidgetCommon = {};
}

WcmDashboardWidgetCommon.dashboards = new Array();

WcmDashboardWidgetCommon.sortClick = function (event, matchedEl, el, params) {
    //var eventId='sorteventDate-component-1-1';
    var eventId = matchedEl.id;
    var sortBy = eventId.substring(4, eventId.indexOf('-'));
    var Widget=WcmDashboardWidgetCommon.dashboards[params.widgetId];
    WcmDashboardWidgetCommon.loadTableData(sortBy, YDom.get(params.widgetId), params.widgetId, null, true);
};

WcmDashboardWidgetCommon.encodePathToNumbers = function (path) {
    var re1 = new RegExp('/', 'g');

    var res = path.replace(re1, '00');    // substitute all forward slash with '00'
    res = res.replace(/\./g, '010');      // substitute all periods with '010'
    return res;
};

WcmDashboardWidgetCommon.insertEditLink = function (item, editLinkId) {
    CStudioAuthoring.Service.getUserPermissions(CStudioAuthoringContext.site, item.uri, { 
        success: function(results) {

            function addEditLink() {
                var editLink = document.getElementById(editLinkId);

                if (editLink) {
                    editLink.innerHTML = ''.concat('<a href="javascript:" class="editLink', ((item.deleted || item.inFlight ) ? ' non-previewable-edit' : ''), '">Edit</a>');
                } else {
                    // We cannot assume the DOM will be ready to insert the edit link
                    // that's why we'll poll until the element is available in the DOM
                    setTimeout(addEditLink, 200);
                }
            }

            var isUserAllowed = CStudioAuthoring.Service.isUserAllowed(results.permissions);

            if (isUserAllowed) {
                // If the user's role is allowed to edit the content then add an edit link
                addEditLink();
            }
        },
        failure: function() {
            throw new Error('Unable to retrieve user permissions');
        }
    });
};

WcmDashboardWidgetCommon.convertDate = function(dateString) {
    if (!dateString) return 0;
    //our eventDate are passing in the format "YYYY-MM-DDTHH:MM:SS;"
    var dateObj = null;
    var dateArray = dateString.split("T");
    if (dateArray && dateArray.length == 2) {
        var dtArray = dateArray[0].split("-");
        var tmArray = dateArray[1].split(":");
        if (dtArray && dtArray.length == 3 &&
            tmArray && tmArray.length == 3) {
            dateObj = new Date(parseInt(dtArray[0], 10),
                               parseInt(dtArray[1], 10),
                               parseInt(dtArray[2], 10),
                               parseInt(tmArray[0], 10),
                               parseInt(tmArray[1], 10),
                               parseInt(tmArray[2], 10));
        }
    }

    if (dateObj) return dateObj;
    return 0;
};

WcmDashboardWidgetCommon.sortItems = function(items, currentSortBy, currentSortType) {
    try {
        items.sort(function (firstItem, secondItem) {
                       if (currentSortBy == "userLastName") {
                           var firstItemVal = firstItem[currentSortBy];
                           var secondItemVal = secondItem[currentSortBy];
                           if (!firstItemVal) {
                               firstItemVal = firstItem["userFirstName"];
                           }
                           if (!secondItemVal) {
                               secondItemVal = secondItem["userFirstName"];
                           }

                           if (firstItemVal && secondItemVal) {
                               firstItemVal = firstItemVal.toLowerCase()
                               secondItemVal = secondItemVal.toLowerCase()

                               if (firstItemVal && secondItemVal) {
                                   if (currentSortType == "true") {
                                       return (firstItemVal == secondItemVal)?0:(firstItemVal < secondItemVal)?-1:1;
                                   } else {
                                       return (firstItemVal == secondItemVal)?0:(secondItemVal < firstItemVal)?-1:1;
                                   }
                               }
                           }
                       } else if (firstItem[currentSortBy]) {
                           if (currentSortBy == "eventDate") {
                               var firstDate = WcmDashboardWidgetCommon.convertDate(firstItem[currentSortBy]);
                               var secondDate = WcmDashboardWidgetCommon.convertDate(secondItem[currentSortBy]);
                               if (currentSortType == "true") {
                                   return (firstDate == secondDate)?0:(firstDate < secondDate)?-1:1;
                               } else {
                                   return (firstDate == secondDate)?0:(secondDate < firstDate)?-1:1;
                               }
                           } else if(!isNaN(firstItem[currentSortBy])  && !isNaN(secondItem[currentSortBy])) {
                               var firstValue = parseInt (firstItem[currentSortBy], 10);
                               var secondValue = parseInt (secondItem[currentSortBy], 10);
                               if (currentSortType == "true") {
                                   return (firstValue == secondValue)?0:(firstValue < secondValue)?-1:1;
                               } else {
                                   return (firstValue == secondValue)?0:(secondValue < firstValue)?-1:1;
                               }
                           } else if(typeof(firstItem[currentSortBy]) == "string") {
                               var firstString = firstItem[currentSortBy].toLowerCase();
                               var secondString = secondItem[currentSortBy].toLowerCase();
                               if (currentSortType == "true") {
                                   return (firstString == secondString)?0:(firstString < secondString)?-1:1;
                               } else {
                                   return (firstString == secondString)?0:(secondString < firstString)?-1:1;
                               }
                           }
                       }
                       return 0;
                   });
    } catch(err) { }
    return items;
};

/*
 * get level 2 and beyond children (becomes a flat stucture)
 */
WcmDashboardWidgetCommon.getChilderenRecursive = function(items) {
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        subChildren.push(item);
        // add further dependencies
        if (item.children && item.children.length > 0) {
              WcmDashboardWidgetCommon.getChilderenRecursive(item.children);
        }
    }
}

/*
 * build level 2 and beyond children (becomes a flat stucture)
 */
WcmDashboardWidgetCommon.getSubSubChilderen = function(table, parentClass, items, widgetId, depth) {
    var rowHtml = "";
    var instance = WcmDashboardWidgetCommon.dashboards[widgetId];

    for (var i = 0; i < items.length; i++) {

        var item = items[i];
        rowHtml += "<tr class='" + parentClass + "'><td colspan='5' class='ttBlankRow3'></td></tr>";

        var itemRowStart = "<tr class='" + parentClass + "'>";

        var itemRowEnd = "</tr>";

        //create table row for this item
        var itemRow = WcmDashboardWidgetCommon.buildItemTableRow(item, instance, false, i, depth);

        rowHtml += itemRowStart + itemRow + itemRowEnd;
    }

    return rowHtml;
};

WcmDashboardWidgetCommon.getDisplayName = function(item) {
    var displayName = '';
    var hasLastName = !CStudioAuthoring.Utils.isEmpty(item.userLastName);
    if (hasLastName) {
        displayName += item.userLastName;
    }

    var hasFirstName = !CStudioAuthoring.Utils.isEmpty(item.userFirstName);
    if (hasFirstName) {
        displayName += hasLastName ? ', ' + item.userFirstName : item.userFirstName;
    }
    return displayName;
};

WcmDashboardWidgetCommon.getFormattedString = function(str, maxLength, isNewFile) {
	var formattedString = "";
	if(str != undefined && str != null) {
		if(str.length > maxLength){
			formattedString = str.substring(0, maxLength) + "...";
		}else {
			formattedString = str;
		}
	}	
	if(isNewFile) formattedString = formattedString + "*";
		 
	return formattedString;
};

WcmDashboardWidgetCommon.Ajax = {
	container: null,
	loadingImage: null,
	disableDashboard: function(){
		if(WcmDashboardWidgetCommon.Ajax.container != null){
	    	document.body.removeChild(WcmDashboardWidgetCommon.Ajax.container);	    	
		}
		if(WcmDashboardWidgetCommon.Ajax.loadingImage != null) {
			document.body.removeChild(WcmDashboardWidgetCommon.Ajax.loadingImage);
		}
		var container = YDom.get();
		WcmDashboardWidgetCommon.Ajax.container = document.createElement("div");		
		with(WcmDashboardWidgetCommon.Ajax.container.style) {
			backgroundColor = "#FFFFFF";
			opacity = "0";
			position = "absolute";
			display = "block";
			width = YDom.getDocumentWidth() + "px";
			height = YDom.getDocumentHeight() + "px";
			top = "0";
			right = "0";
			bottom = "0";
			left = "0";
			zIndex = "1000";
		}
		
		WcmDashboardWidgetCommon.Ajax.loadingImage = document.createElement("img");		
		WcmDashboardWidgetCommon.Ajax.loadingImage.src = contextPath + CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";					
		
		document.body.appendChild(WcmDashboardWidgetCommon.Ajax.container);
		document.body.appendChild(WcmDashboardWidgetCommon.Ajax.loadingImage);
		
		var imagePopUp = new YAHOO.widget.Overlay(WcmDashboardWidgetCommon.Ajax.loadingImage);
		imagePopUp.center();
		imagePopUp.render();		
    },
    enableDashboard: function(){
    	if(WcmDashboardWidgetCommon.Ajax.container != null){
	    	document.body.removeChild(WcmDashboardWidgetCommon.Ajax.container);
	    	WcmDashboardWidgetCommon.Ajax.container = null;
		}
		if(WcmDashboardWidgetCommon.Ajax.loadingImage != null) {
			document.body.removeChild(WcmDashboardWidgetCommon.Ajax.loadingImage);
			WcmDashboardWidgetCommon.Ajax.loadingImage = null;
		}    	    	
    }
};

/**
 * init widget
 */
WcmDashboardWidgetCommon.init = function(instance) {
	
	

    var widgetId = instance.widgetId;
    var sortBy = instance.defaultSortBy;
    var pageId = instance.pageId;
    var hideEmptyRow = instance.hideEmptyRow;

    /////////////////////////////////////////////////////
    // added to protect un wanted values in text boxes //
    ////////////////////////////////////////////////////
    if( YDom.get("widget-showitems-" + widgetId) != null )
    	YDom.get("widget-showitems-" + widgetId).value = 10;
    
    YEvent.onAvailable(widgetId, function() {

        WcmDashboardWidgetCommon.dashboards[widgetId] = instance;
        dashboardEl = YDom.get(widgetId);
        dashboardEl.style.display = "none";
        
        var hasPermsForDashboardFn = function(perms, permission) {
        	var hasPerm = false;

        	for(var k=0; k < perms.permissions.length; k++) {
				if(permission == perms.permissions[k].permission) {
					hasPerm = true;	
					break;
				}
			}

        	return hasPerm;
        }

        var getPermsCb = {
        	widgetId: widgetId,
        	dashboardEl: dashboardEl,
        	
        	success: function(perms) {
				WcmDashboardWidgetCommon.cachedPerms = perms;
				var dashboardEl = this.dashboardEl;
				
				var permission = "none";
				if(this.widgetId == "GoLiveQueue"
				||this.widgetId == "recentlyMadeLive"
				||this.widgetId == "approvedScheduledItems") { 
					permission = "publish"; 
				}
				
				if(this.widgetId == "icon-guide" 
				|| this.widgetId == "MyRecentActivity"
				|| hasPermsForDashboardFn(perms, permission)) {
					dashboardEl.style.display = "block";

			        dashboardEl.instance = instance;
			
			        var state = WcmDashboardWidgetCommon.getCurrentWidgetTogglePreference(widgetId, pageId);
			
			        
			        WcmDashboardWidgetCommon.toggleWidget(widgetId, pageId, state);
			
			
			        var checkboxClick = function(event, matchedEl) {
			            if (instance.onCheckedClickHandler) {
			                instance.onCheckedClickHandler(event, matchedEl);
			            }
			            else {
			                WcmDashboardWidgetCommon.selectItem(matchedEl, matchedEl.checked);
			            }
			        };
			
			        var editClick = function(event, matchedEl) {
			            WcmDashboardWidgetCommon.editItem(matchedEl, matchedEl.checked);
			        };
			
			        var previewClick = function(event, matchedEl) {
			            WcmDashboardWidgetCommon.previewItem(matchedEl, matchedEl.checked);
			        };
			
			        var dispatchLinkClick = function(event, matchedEl) {
			            if (matchedEl.className.indexOf("previewLink") != -1) {
			                previewClick(event, matchedEl);
			            }
			            else if (matchedEl.className.indexOf("editLink") != -1
                            && matchedEl.className.indexOf("non-previewable-edit") == -1 ) {
			                editClick(event, matchedEl);
			            }
			        }
			
			
			        YEvent.delegate(widgetId, "click", checkboxClick, "input");
			        YEvent.delegate(widgetId, "click", dispatchLinkClick, "a");
			
			        var searchLimitInput = YDom.get("widget-showitems-" + widgetId);
			        var filterByCount = null;
			        if (searchLimitInput) {
			            var searchNumber = CStudioAuthoring.Service.getWindowState(CStudioAuthoringContext.user,
			                                                                      pageId,
			                                                                      widgetId,
			                                                                      "searchNumber");
			            if (searchNumber && !isNaN(searchNumber)) {
			                searchLimitInput.value = parseInt(searchNumber, 10);
			            } else {
			                searchLimitInput.value = instance.defaultSearchNumber;
			            }
			
			            filterByCount = (isNaN(searchLimitInput.value)?10:parseInt(searchLimitInput.value, 10));
			        }
			
			        var widgetFilterBy = CStudioAuthoring.Service.getWindowState(CStudioAuthoringContext.user,
			                                                                    pageId,
			                                                                    widgetId,
			                                                                    "widgetFilterBy");
			        var filterByEl = YDom.get("widget-filterBy-" + widgetId);
			        if (widgetFilterBy && widgetFilterBy != undefined && widgetFilterBy != "") {
			            WcmDashboardWidgetCommon.loadFilterTableData(sortBy, YDom.get(widgetId), widgetId, filterByCount, widgetFilterBy);
			        } else if(filterByCount != null) {
			            WcmDashboardWidgetCommon.loadTableData(sortBy, YDom.get(widgetId), widgetId, filterByCount);
			        } else {
			            WcmDashboardWidgetCommon.loadTableData(sortBy, YDom.get(widgetId), widgetId);
			        }
			
			        var controlsListEl = YDom.getElementsByClassName("cstudio-widget-controls", null, dashboardEl)[0];
			
			        if (controlsListEl) {
			            if (instance.renderAuxControls) {
			                instance.renderAuxControls(controlsListEl,widgetId);
			            }
			        }
			
			        if (state == 'closed') {
			            YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .widget-FilterBy")[0],"display","none");
			        }
			
			        //attach keydown event to search limit input       
			        if (searchLimitInput) {  
			        	
			        	var isInt = function(val) {
			    			var parsedVal = parseInt(val);
			    			if ( isNaN(parsedVal) || val == "0") return false;
			    			return ( val == parsedVal && val.toString() == parsedVal.toString() );
			        	};
			
			        	var searchLimitInputEvent = function(event) {
			            	var searchNumber = searchLimitInput.value;
			            	
			                //added to protect non numeric input.                
			                if (event.keyCode == "13") {
			                    if ( !isInt(searchNumber) ) { //execute the ajax only if its a number
			                        searchLimitInput.value = instance.defaultSearchNumber;
			                        searchNumber = searchLimitInput.value;
			                    }
			
			                    //var searchNumber=searchLimitInput.value;
			                    if ( isInt(searchNumber) ) { //execute the ajax only if its a integer number.
			                        searchNumber = searchNumber.replace(/\+/g, "").replace(/\-/g, "");
			                        searchLimitInput.value = searchNumber;
			                        CStudioAuthoring.Service.setWindowState(CStudioAuthoringContext.user,
			                                                               pageId,
			                                                               widgetId,
			                                                               "searchNumber",
			                                                               searchNumber);
			                        var sortBy=instance.currentSortBy? instance.currentSortBy:instance.defaultSortBy;
			                        var filterByEl = YDom.get("widget-filterBy-" + widgetId);
			                        if (filterByEl && filterByEl.value != undefined && filterByEl.value != "") {
			                            WcmDashboardWidgetCommon.loadFilterTableData(sortBy, YDom.get(widgetId), widgetId, searchNumber, filterByEl.value);
			                        } else {
			                            WcmDashboardWidgetCommon.loadTableData(sortBy, YDom.get(widgetId), widgetId, searchNumber);
			                        }
			                    }else{
			                    	searchLimitInput.value = instance.defaultSearchNumber;
			                    }
			
			                }
			
			            };
			
			            var validateSearchLimitInputValue = function(event) {
			                var searchNum = searchLimitInput.value;
			                //insert default value if invalid
			                if ( !isInt(searchNum) ) {
			                    searchLimitInput.value = instance.defaultSearchNumber;
			                }
			            };
			            
			            YEvent.addListener(searchLimitInput, "keyup", searchLimitInputEvent);
			            YEvent.addListener(searchLimitInput, "blur", validateSearchLimitInputValue);
			        }

				}				
        	},

        	failure: function() {
        	}
        };
        
        if(WcmDashboardWidgetCommon.cachedPerms) {
        	getPermsCb.success(WcmDashboardWidgetCommon.cachedPerms);
        }
        else {
	        CStudioAuthoring.Service.getUserPermissions(CStudioAuthoringContext.site, "/", getPermsCb);
        }					
						

    });

};

WcmDashboardWidgetCommon.getSimpleRow = function(prefix, widgetId, rowTitle, classes) {
    var row = '<th id=\"' + prefix + '-' + widgetId + '\" class=\"' + classes + '\">' +
              '<span>' +
              rowTitle +              
              '</span>' +
              '</span>' +
              '</th>';
    return row;
};

WcmDashboardWidgetCommon.getSortableRow = function(prefix, widgetId, rowTitle, classes) {
    var row = '<th id=\"' + prefix + '-' + widgetId + '\" class=\"' + classes + '\">' +
              '<span>' +
              '<a href="javascript:void(0);" id=\"sort' + prefix + '-' + widgetId + '\">' + rowTitle + '</a>' +
              '<span class="wcm-widget-margin"/>' +
              '</span>' +
              '<span id=\"sortIcon-' + prefix + '-' + widgetId + '\" class=\"ttSortDesc wcm-go-live-sort-columns-' + widgetId + '\" style=\"display:none\"></span>' +
              '</th>';
    return row;
};
WcmDashboardWidgetCommon.getDefaultSortRow = function(prefix, widgetId, rowTitle, classes) {
    var row = '<th id=\"' + prefix + '-' + widgetId + '\" class=\"' + classes + '\">' +
              '<span>' +
              rowTitle +
              '<span class=\"wcm-widget-margin\"/>' +
              '</span>' +
              '<span id=\"sortIcon-' + prefix + '-' + widgetId + '\" class=\"ttSortBlack\"></span>' +
              '</th>';
    return row;
};
/**
 * open and close a given dashboard widget
 */
WcmDashboardWidgetCommon.toggleWidget = function(widgetId, pageId, newState) {
    
    var widgetBodyEl = YDom.get(widgetId + "-body");
    var widgetToggleEl = YDom.get("widget-toggle-" + widgetId);
    var currentState = widgetToggleEl.className == 'ttOpen' ? 'closed' : 'open';
    
    if (YAHOO.lang.isUndefined(newState)) {
        newState = currentState == 'closed' ? 'open' : 'closed';
    }

    if (newState == 'closed') {
        widgetToggleEl.className = 'ttOpen';
        widgetBodyEl.style.display = "none";
        YDom.setStyle("expand-all-" + widgetId, "display", "none");
        YDom.setStyle("widget-expand-state-" + widgetId, "display", "none");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .recently-made-live")[0],"display","none");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .recently-made-live-right")[0],"display","none");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .widget-FilterBy")[0],"display","none");
    } else {
        widgetBodyEl.style.display = "block";
        widgetToggleEl.className = "ttClose";
        YDom.setStyle("expand-all-" + widgetId, "display", "block");
        YDom.setStyle("widget-expand-state-" + widgetId, "display", "block");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .recently-made-live")[0],"display","block");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .recently-made-live-right")[0],"display","block");
        YDom.setStyle(YAHOO.util.Selector.query("#" + widgetId +" .widget-FilterBy")[0],"display","block");
    }

    CStudioAuthoring.Service.setWindowState(
            CStudioAuthoringContext.user,
            pageId,
            widgetId,
            "widgetToggle",
            newState);

   
    return false;
};

/**
 * get user's preference on widget state
 */
WcmDashboardWidgetCommon.getCurrentWidgetTogglePreference = function(widgetId, pageId) {

    var widgetState = "";

    widgetState = CStudioAuthoring.Service.getWindowState(
            CStudioAuthoringContext.user,
            pageId,
            widgetId,
            "widgetToggle");

    return widgetState;
};

/**
 * toggle (expand or collapse) a given line item
 */
WcmDashboardWidgetCommon.toggleLineItem = function(id, ignoreParent) {
	
    var parentId = YDom.get(id),
    		childItems = CStudioAuthoring.Utils.getElementsByClassName(id),
    		length = childItems.length,
    		idx,
    		item;

    if (parentId.className == "ttClose parent-div-widget") {
    		for (idx = 0; idx < length; idx++) {
    				item = childItems[idx];
            if (item) {
                item.style.display = "none";
            }
    		}
        parentId.className = "ttOpen parent-div-widget";
    }
    else {
    		for (idx = 0; idx < length; idx++) {
    				item = childItems[idx];
            if (item) {
                item.style.display = "";
            }
    		}
        parentId.className = "ttClose parent-div-widget";
    }  

    // If all lines are collapsed, then the header link should change to "Expand All" and vice versa.
    var expandAll = false,
    		tableEl = YDom.getAncestorByClassName(id, "ttTable"),
    		rows = tableEl.rows,
    		arr = [],
    		widgetId = tableEl.id.split("-")[0],
    		widget = YDom.get(widgetId),
    		linkEl = YDom.get("expand-all-" + widgetId);

    for(idx = 0; idx < rows.length; idx++) {
    	if (rows[idx].className === "avoid" || rows[idx].className == "" ) {
    		continue;
       } else {
          arr.push(rows[idx]);
       }       
    }

    for(idx = 0; idx < arr.length; idx++) {
       if ( arr[idx].style.display === "none" ) {
            expandAll = true;
            break;
        }
    }

    if (!ignoreParent) {
    		this.toggleHeaderLink(widget, linkEl, expandAll);
    }

};

WcmDashboardWidgetCommon.toggleHeaderLink = function(widget, linkEl, showCollapsed) {

		if (showCollapsed) {
				linkEl.setAttribute("href","javascript:void(0);");
        linkEl.innerHTML = "Expand All";        
        linkEl.className = "widget-collapse-state";
        widget.instance.expanded = false;
    }
    else {
				linkEl.setAttribute("href","javascript:void(0);");
        linkEl.innerHTML = "Collapse All";
        linkEl.className = "widget-expand-state";
        widget.instance.expanded = true;
    }
}

/**
 * toggle All items
 */
WcmDashboardWidgetCommon.toggleAllItems = function(widgetId) {

    var widget = YDom.get(widgetId),
				instance = widget.instance,
				link = YDom.get("expand-all-" + widgetId),
				items = YDom.getElementsByClassName("parent-div-widget", null, widget),
				item,
				length = items.length;

    for (var count = 0; count < length; count++) {
    		item = items[count];
        if (item) {
						item.className = (instance.expanded) ? "ttClose parent-div-widget" : "ttOpen parent-div-widget";
            this.toggleLineItem(item.id, true);
        }
    }

    this.toggleHeaderLink(widget, link, instance.expanded);
};

/**
 * edit an item
 */
WcmDashboardWidgetCommon.editItem = function(matchedElement, isChecked) {

    var editCallback = {
        success: function() {
            this.callingWindow.location.reload(true);
        },
        failure: function() {
        },
        callingWindow : window
    };

    var getContentCallback = {
        success: function(contentTO) {
            WcmDashboardWidgetCommon.Ajax.enableDashboard();
            CStudioAuthoring.Operations.editContent(
                contentTO.form,
                CStudioAuthoringContext.siteId,
                contentTO.uri,
                contentTO.nodeRef,
                contentTO.uri,
                false,
                editCallback);
        },

        failure: function() {
            WcmDashboardWidgetCommon.Ajax.enableDashboard();
        }
    };
    WcmDashboardWidgetCommon.Ajax.disableDashboard();
    WcmDashboardWidgetCommon.getContentItemForMatchedElement(matchedElement, getContentCallback);
};

/**
 * User clicked on preview link, open preview
 */
WcmDashboardWidgetCommon.previewItem = function(matchedElement, isChecked) {

    var callback = {
        success: function(contentTO) {
			CStudioAuthoring.Storage.write(CStudioAuthoring.Service.menuParentPathKeyFromItemUrl(contentTO.path), contentTO.path);
            CStudioAuthoring.Operations.openPreview(contentTO);
        },

        failure: function() {
        }
    };

    WcmDashboardWidgetCommon.getContentItemForMatchedElement(matchedElement, callback);
};


/**
 * Select an item in the dashboard widget
 */
WcmDashboardWidgetCommon.selectItem = function(matchedElement, isChecked) {
	if(matchedElement.type == "checkbox") WcmDashboardWidgetCommon.Ajax.disableDashboard();
    var callback = {
        success: function(contentTO) {
            if (isChecked == true) {
                CStudioAuthoring.SelectedContent.selectContent(contentTO);
            }
            else {
                CStudioAuthoring.SelectedContent.unselectContent(contentTO);
            }
            WcmDashboardWidgetCommon.Ajax.enableDashboard();
        },

        failure: function() {
            WcmDashboardWidgetCommon.Ajax.enableDashboard();
        }
    };

    WcmDashboardWidgetCommon.getContentItemForMatchedElement(matchedElement, callback);
};


/**
 * return the transfer object for the matched item
 */
WcmDashboardWidgetCommon.getContentItemForMatchedElement = function(matchedElement, callback) {

    var itemUrl = "";

    // walk the DOM to get the path  get parent of current element
    var parentTD = YDom.getAncestorByTagName(matchedElement, "td");

    // get a sibling, that is <td>, that has attribute of title
    var urlEl = YDom.getNextSiblingBy(parentTD, function(el) {
        return el.getAttribute('title') == 'fullUri';
    });

    if (!urlEl) { // if url null return    	
        return;
    }
    else {
        itemUrl = urlEl.innerHTML;
    }

    var getContentItemsCb = {
        success: function(contentTO) {
            callback.success(contentTO.item);
        },

        failure: function() {
            callback.failure();
        }
    };

    CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, itemUrl, getContentItemsCb, false, false);
}

/**
 * load and render table data
 */
WcmDashboardWidgetCommon.loadTableData = function(sortBy, container, widgetId, filterByNumber, sortFromCachedData) {

    var instance = WcmDashboardWidgetCommon.dashboards[widgetId];
    var tableName = widgetId;
    var webscriptName = widgetId + "-table";
    var divTableContainer = widgetId + "-body";
    var currentSortBy = (sortBy) ? sortBy : null;
    var currentSortType = "";
    var hideEmptyRow = instance.hideEmptyRow;
    var pageId = instance.pageId;

    var callback = {
        success: function(results) {
            instance.dashBoardData = results;
            var sortDocuments = results.documents;
            instance.tooltipLabels = new Array();
            var newtable = "";
            var blankRow = "<tr class='avoid'><td class='ttBlankRow' colspan='5'>&nbsp;</td></tr>";
            var count = 0;
            var sortedByValue = results.sortedBy;
            var sortType = results.sortType;
            var previousSortedBy = YDom.get('sortedBy-' + widgetId).innerHTML;
            var previousSortType = YDom.get('sort-type-' + widgetId).innerHTML;

            if (previousSortedBy == currentSortBy) {
                if (previousSortType == "true") {
                    currentSortType = "false";
                }
                else {
                    currentSortType = "true";
                }
            }
            else {
                currentSortType = "false";
            }

            // update total count
            var totalCountEl = YDom.get(widgetId + "-total-count");
            if (totalCountEl != undefined) {
                totalCountEl.innerHTML = results.total;
            }

            if (sortFromCachedData && sortDocuments.length > 1) {
                if (instance.skipComponentSort) {
                    //Don't sort by components
                } else {
                   //if skipComponentSort flag not available
                    sortDocuments = WcmDashboardWidgetCommon.sortItems(sortDocuments, currentSortBy, currentSortType)
                }
            }

            // update custom header controls
            // create line items
            for (var j = 0; j < sortDocuments.length; j++) {

                var items = sortDocuments[j].children;
                var document = sortDocuments[j];

                count = count + 1;

                var name = (sortDocuments[j].internalName != undefined) ? sortDocuments[j].internalName : "error";
                var parentClass = "wcm-table-parent-" + name + "-" + count;

                if (!hideEmptyRow || sortDocuments[j].numOfChildren > 0) {
                    var table = "<tr>";
                    table += WcmDashboardWidgetCommon.buildItemTableRow(sortDocuments[j], instance, true, count, 0);
                    table += "</tr>";

                    if (sortFromCachedData) {
                        items = WcmDashboardWidgetCommon.sortItems(items, currentSortBy, currentSortType)
                    }

                    for (var i = 0; i < items.length; i++) {
                        var item = items[i];
                        table = table + "<tr class='" + parentClass + "'><td colspan='5' class='ttBlankRow3'></td></tr>";
                        var itemRowStart = "<tr class='" + parentClass + "'>";
                        var itemRowEnd = "</tr>";

                        var subItemRowStart = "<tr class='" + parentClass + "'><td><span class='wcm-widget-margin'></span><span class='ttFirstCol128'><input type='checkbox'/></span><span class='wcm-widget-margin'></span>";

                        //create table row for this item
                        var itemRow = WcmDashboardWidgetCommon.buildItemTableRow(item, instance, false, i, 0);
                        table += itemRowStart + itemRow + itemRowEnd;

                        var subItems = item.children;
                        if (subItems && subItems.length > 0) {
                            subChildren = new Array();
                            WcmDashboardWidgetCommon.getChilderenRecursive(subItems);
                            subChildren = WcmDashboardWidgetCommon.sortItems(subChildren, currentSortBy, currentSortType)
                            table += WcmDashboardWidgetCommon.getSubSubChilderen(table, parentClass, subChildren, widgetId, 1);
                            //table += WcmDashboardWidgetCommon.getSubSubChilderenRecursive(table, parentClass, subItems, widgetId, 1);
                        }
                    }
                    newtable += table;
                }

            }

            newtable = blankRow + newtable + blankRow;

            var tableContentStart = '<table id="' + tableName + '-table" class="ttTable" border="0">';
            var theadContent = '<thead class="ttThead" id="' + tableName + '-thead"><tr class="avoid">' + instance.renderItemsHeading() + '</tr></thead>';
            var tbodyContent = '<tbody class="ttTbody" id="' + tableName + '-tbody">' + newtable + '</tbody>';
            var tableContentEnd = '</table>';

            //Check for already checked items,
            //un-check then to remove those items from selected items list.
            var checkboxArray = YDom.getElementsBy(function(el) {
                                                       return ( el.type === 'checkbox' && el.checked === true);
                                                   },
                                                   'input',
                                                   divTableContainer);
            if (checkboxArray && checkboxArray.length >= 1) {
                for (var chkIdx=0; chkIdx < checkboxArray.length; chkIdx++) {
                    checkboxArray[chkIdx].checked = false;
                    WcmDashboardWidgetCommon.clearItem(checkboxArray[chkIdx], instance.dashBoardData);
                }
            }

            YDom.get(divTableContainer).innerHTML = tableContentStart + theadContent + tbodyContent + tableContentEnd;
            YEvent.delegate(widgetId + "-thead", "click", WcmDashboardWidgetCommon.sortClick, 'th a', {'widgetId':widgetId,'sortBy':currentSortBy }, true);
            WcmDashboardWidgetCommon.updateSortIconsInWidget(currentSortBy, currentSortType, widgetId);

            YDom.get('sortedBy-' + widgetId).innerHTML = currentSortBy;
            YDom.get('sort-type-' + widgetId).innerHTML = currentSortType;

            instance.currentSortBy=sortBy;
            instance.searchNumber=filterByNumber;
            
            /**
			 * remove loading image for recent current widget
			 */
            YDom.setStyle("loading-" + widgetId, "display", "none");
            /**
			 * ajax call link in dashboard widget will be showed/hide
			 * according to widget state..
			 */
            var widgetState = CStudioAuthoring.Service.getWindowState(CStudioAuthoringContext.user, pageId, widgetId, "widgetToggle");
        	if(widgetState == "closed") {        		
                YDom.setStyle("widget-expand-state-" + widgetId, "display", "none");
        	}else {        		
        		YDom.setStyle("widget-expand-state-" + widgetId, "display", "block");
        	}
        	
        	/*************************************************************
        	 * registering mouse over and mouse out events for row items.
        	 ************************************************************/
            var rowMouseover = function(event, matchedEl) {
                YDom.addClass(matchedEl, "over");
            };

            var rowMouseout = function(event, matchedEl) {
                YDom.removeClass(matchedEl, "over", "");
            };

            YEvent.delegate(webscriptName, "mouseover", rowMouseover, "tr");
            YEvent.delegate(webscriptName, "mouseout", rowMouseout, "tr");

            // adding tool tip display
            if (instance.tooltipLabels && instance.tooltipLabels.length >= 1) {
                var oTTContainer = YDom.get("acn-context-tooltip-widgets");
                if (!oTTContainer) {
                    var toolTipContainer = window.document.createElement("div");
                    toolTipContainer.setAttribute("id", "acn-context-tooltip-widgets");
                    toolTipContainer.className = "acn-context-tooltip";
                    toolTipContainer.innerHTML = "<div style=\"z-index: 2; left: 73px; top: 144px; visibility: hidden;\"" +
                                                 " class=\"yui-module yui-overlay yui-tt\"" +
                                                 "id=\"acn-context-tooltipWrapper-widgets\"><div class=\"bd\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +	"</div>";

                    window.document.body.appendChild(toolTipContainer);
                }

                new YAHOO.widget.Tooltip("acn-context-tooltipWrapper-widgets", {
                                          context: instance.tooltipLabels,
                                          hidedelay:0,
                                          showdelay:500,
                                          container: "acn-context-tooltip-widgets"
                                         });
            }

            if (!instance.expanded) {
                instance.expanded = true;
                WcmDashboardWidgetCommon.toggleAllItems(widgetId);
            }
        },

        failure: function() {
        	YDom.setStyle("loading-" + widgetId, "display", "none");
        },
        
        beforeServiceCall : function(){        	        	
			YDom.setStyle("loading-" + widgetId, "display", "block");		
        }
    };

    if (sortFromCachedData && instance.dashBoardData) {
        callback.success(instance.dashBoardData);
    } else {
        instance.retrieveTableData(currentSortBy, currentSortType, callback, null, filterByNumber);
    }
};

/////For filtering Widgets


WcmDashboardWidgetCommon.loadFilterTableData = function(sortBy, container, widgetId, filterByNumber,filterBy) {

    var instance = WcmDashboardWidgetCommon.dashboards[widgetId];
    var tableName = widgetId;
    var webscriptName = widgetId + "-table";
    var divTableContainer = widgetId + "-body";
    var currentSortBy = (sortBy) ? sortBy : null;
    var currentSortType = "";
    var hideEmptyRow = instance.hideEmptyRow;
    var pageId = instance.pageId;

    var callback = {
        success: function(results) {    		
            instance.dashBoardData = results;
            var sortDocuments = results.documents;
            instance.tooltipLabels = new Array();
            var newtable = "";
            var blankRow = "<tr class='avoid'><td class='ttBlankRow' colspan='5'>&nbsp;</td></tr>";
            var count = 0;
            var sortedByValue = results.sortedBy;
            var sortType = results.sortType;
            var previousSortedBy = YDom.get('sortedBy-' + widgetId).innerHTML;
            var previousSortType = YDom.get('sort-type-' + widgetId).innerHTML;

            if (previousSortedBy == currentSortBy) {
                if (previousSortType == "true") {
                    currentSortType = "false";
                }
                else {
                    currentSortType = "true";
                }
            }
            else {
                currentSortType = "false";
            }

            // update total count
            var totalCountEl = YDom.get(widgetId + "-total-count");
            if (totalCountEl != undefined) {
                totalCountEl.innerHTML = results.total;
            }

            // update custom header controls
            // create line items
            for (var j = 0; j < sortDocuments.length; j++) {

                var items = sortDocuments[j].children;
                var document = sortDocuments[j];

                count = count + 1;

                var name = (sortDocuments[j].internalName != undefined) ? sortDocuments[j].internalName : "error";
                var parentClass = "wcm-table-parent-" + name + "-" + count;

                if (!hideEmptyRow || sortDocuments[j].numOfChildren > 0) {
                    var table = "<tr>";
                    table += WcmDashboardWidgetCommon.buildItemTableRow(sortDocuments[j], instance, true, count, 0);
                    table += "</tr>";

                    for (var i = 0; i < items.length; i++) {
                        var item = items[i];
                        table = table + "<tr class='" + parentClass + "'><td colspan='5' class='ttBlankRow3'></td></tr>";
                        var itemRowStart = "<tr class='" + parentClass + "'>";
                        var itemRowEnd = "</tr>";

                        var subItemRowStart = "<tr class='" + parentClass + "'><td><span class='wcm-widget-margin'></span><span class='ttFirstCol128'><input type='checkbox'/></span><span class='wcm-widget-margin'></span>";

                        //create table row for this item
                        var itemRow = WcmDashboardWidgetCommon.buildItemTableRow(item, instance, false, i, 0);
                        table += itemRowStart + itemRow + itemRowEnd;

                        var subItems = item.children;
                        if (subItems && subItems.length > 0) {
                            table += WcmDashboardWidgetCommon.getSubSubChilderenRecursive(table, parentClass, subItems, widgetId, 1);
                        }
                    }
                    newtable += table;
                }

            }

            newtable = blankRow + newtable + blankRow;

            var tableContentStart = '<table id="' + tableName + '-table" class="ttTable" border="0">';
            var theadContent = '<thead class="ttThead" id="' + tableName + '-thead"><tr class="avoid">' + instance.renderItemsHeading() + '</tr></thead>';
            var tbodyContent = '<tbody class="ttTbody" id="' + tableName + '-tbody">' + newtable + '</tbody>';
            var tableContentEnd = '</table>';

            //Check for already checked items,
            //un-check then to remove those items from selected items list.
            var checkboxArray = YDom.getElementsBy(function(el) {
                                                       return ( el.type === 'checkbox' && el.checked === true);
                                                   },
                                                   'input',
                                                   divTableContainer);
            if (checkboxArray && checkboxArray.length >= 1) {
                for (var chkIdx=0; chkIdx < checkboxArray.length; chkIdx++) {
                    checkboxArray[chkIdx].checked = false;
                    WcmDashboardWidgetCommon.clearItem(checkboxArray[chkIdx], instance.dashBoardData);
                }
            }

            YDom.get(divTableContainer).innerHTML = tableContentStart + theadContent + tbodyContent + tableContentEnd;
            YEvent.delegate(widgetId + "-thead", "click", WcmDashboardWidgetCommon.sortClick, 'th a', {'widgetId':widgetId,'sortBy':currentSortBy }, true);
            WcmDashboardWidgetCommon.updateSortIconsInWidget(currentSortBy, currentSortType, widgetId);

            YDom.get('sortedBy-' + widgetId).innerHTML = currentSortBy;
            YDom.get('sort-type-' + widgetId).innerHTML = currentSortType;

            instance.currentSortBy=sortBy;
            instance.searchNumber=filterByNumber;
            
            /**
			 * remove loading image for recent current widget
			 */
            YDom.setStyle("loading-" + widgetId, "display", "none");
            /**
			 * ajax call link in dashboard widget will be showed/hide
			 * according to widget state..
			 */
            var widgetState = CStudioAuthoring.Service.getWindowState(CStudioAuthoringContext.user, pageId, widgetId, "widgetToggle");
        	if(widgetState == "closed") {        		
                YDom.setStyle("widget-expand-state-" + widgetId, "display", "none");
        	}else {        		
        		YDom.setStyle("widget-expand-state-" + widgetId, "display", "block");
        	}
        	
        	/*************************************************************
        	 * registering mouse over and mouse out events for row items.
        	 ************************************************************/
            var rowMouseover = function(event, matchedEl) {
                YDom.addClass(matchedEl, "over");
            };

            var rowMouseout = function(event, matchedEl) {
                YDom.removeClass(matchedEl, "over", "");
            };

            YEvent.delegate(webscriptName, "mouseover", rowMouseover, "tr");
            YEvent.delegate(webscriptName, "mouseout", rowMouseout, "tr");

            // adding tool tip display
            if (instance.tooltipLabels && instance.tooltipLabels.length >= 1) {
                var oTTContainer = YDom.get("acn-context-tooltip-widgets");
                if (!oTTContainer) {
                    var toolTipContainer = window.document.createElement("div");
                    toolTipContainer.setAttribute("id", "acn-context-tooltip-widgets");
                    toolTipContainer.className = "acn-context-tooltip";
                    toolTipContainer.innerHTML = "<div style=\"z-index: 2; left: 73px; top: 144px; visibility: hidden;\"" +
                                                 " class=\"yui-module yui-overlay yui-tt\"" +
                                                 "id=\"acn-context-tooltipWrapper-widgets\"><div class=\"bd\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +
                                                 "<div class=\"yui-tt-shadow\"></div>" +	"</div>";

                    window.document.body.appendChild(toolTipContainer);
                }

                new YAHOO.widget.Tooltip("acn-context-tooltipWrapper-widgets", {
                                          context: instance.tooltipLabels,
                                          hidedelay:0,
                                          showdelay:500,
                                          container: "acn-context-tooltip-widgets"
                                         });
            }

            if (!instance.expanded) {
                instance.expanded = true;
                WcmDashboardWidgetCommon.toggleAllItems(widgetId);
            }
        },

        failure: function() {
        	YDom.setStyle("loading-" + widgetId, "display", "none");
        },
        
        beforeServiceCall : function(){        	        	
			YDom.setStyle("loading-" + widgetId, "display", "block");		
        }
    };

    instance.retrieveTableData(currentSortBy, currentSortType, callback, null, filterByNumber,filterBy);
};


/**
 * Handle children level 2 and beyond (becomes a flat stucture)
 */
WcmDashboardWidgetCommon.getSubSubChilderenRecursive = function(table, parentClass, items, widgetId, depth) {
    var rowHtml = "";
    var instance = WcmDashboardWidgetCommon.dashboards[widgetId];

    for (var i = 0; i < items.length; i++) {

        var item = items[i];
        rowHtml += "<tr class='" + parentClass + "'><td colspan='5' class='ttBlankRow3'></td></tr>";

        var itemRowStart = "<tr class='" + parentClass + "'>";

        var itemRowEnd = "</tr>";

        //create table row for this item
        var itemRow = WcmDashboardWidgetCommon.buildItemTableRow(item, instance, false, i, depth);

        rowHtml += itemRowStart + itemRow + itemRowEnd;

        // add further dependencies
        if (item.children && item.children.length > 0) {
            rowHtml += WcmDashboardWidgetCommon.getSubSubChilderenRecursive(table, parentClass, item.children, widgetId, depth + 1);
        }
    }

    return rowHtml;
};

/**
 * call render line item for the particular kind of dashboard
 * @param item - content object
 * @param dashboardInstance instance of the dashboard
 */
WcmDashboardWidgetCommon.buildItemTableRow = function(item, dashboardInstance, firstRow, count, depth) {
    return dashboardInstance.renderLineItem(item, firstRow, count, depth);
};

/**
 * update sorting icons
 */
WcmDashboardWidgetCommon.updateSortIconsInWidget = function(currentSortBy, currentSortType, widgetId) {
    if (YAHOO.lang.isNull(currentSortBy)) {
        return;
    }
    //valid for sorting clicks

    var currentSortById = "sortIcon-" + currentSortBy + '-' + widgetId;

    if (currentSortType == "true") {
        currentSortType = "ttSortAsc";
    }
    else {
        currentSortType = "ttSortDesc";
    }

    var sortColumns = CStudioAuthoring.Utils.getElementsByClassName('wcm-go-live-sort-columns-' + widgetId);
    var count = 0;
    var length = sortColumns.length;
    while (length > count) {

        var item = sortColumns[count];
        if (item != undefined) {

            if (item.id == currentSortById) {

                item.style.display = "inline-block";
                item.className = currentSortType + " wcm-go-live-sort-columns-" + widgetId;
            }
            else {
                item.style.display = "none";
            }
        }
        count = count + 1;
    }
};

WcmDashboardWidgetCommon.initFilterToWidget= function(widgetId, widgetFilterBy) {
    var filterByEl =  document.createElement("select");
    if (widgetId) {
        filterByEl.setAttribute("id", "widget-filterBy-" + widgetId);
    }

	filterByEl.options[0]=new Option(CMgs.format(langBundle, "dashletFilterPages"), "pages", true, false);
	filterByEl.options[1]=new Option(CMgs.format(langBundle, "dashletFilterComponents"), "components", true, false);
	filterByEl.options[2]=new Option(CMgs.format(langBundle, "dashletFilterDocuments"), "documents", true, false);
	filterByEl.options[3]=new Option(CMgs.format(langBundle, "dashletFilterAll"), "all", true, false);

    //set default value from cookie
    if (widgetFilterBy) {
        for (var optIdx=0; optIdx < filterByEl.options.length; optIdx++) {
            if (filterByEl.options[optIdx].value == widgetFilterBy) {
                filterByEl.options[optIdx].selected = true;
                break;
            }
        }
    }

    return filterByEl;	
};

/**
 * get selected item from cache data
 */
WcmDashboardWidgetCommon.getContentRecursive = function(dashBoardData, itemUrl) {
    var sortDocuments = dashBoardData.documents;
    var result = null;
    for (var j = 0; j < sortDocuments.length; j++) {
        if (sortDocuments[j].uri == itemUrl) {
            return sortDocuments[j];
        }
        if (sortDocuments[j].children && sortDocuments[j].children >= 1) {
            result = WcmDashboardWidgetCommon.getContentRecursive(sortDocuments[j].children, itemUrl);
            if (result) break;
        }
    }
    return result;
};

/**
 * clear selected item in the dashboard widget
 */
WcmDashboardWidgetCommon.clearItem = function(matchedElement, dashBoardData) {
    if(matchedElement.type == "checkbox") {
        if (dashBoardData) {
            var itemUrl = "";

            // walk the DOM to get the path  get parent of current element
            var parentTD = YDom.getAncestorByTagName(matchedElement, "td");

            // get a sibling, that is <td>, that has attribute of title
            var urlEl = YDom.getNextSiblingBy(parentTD, function(el) {
                return el.getAttribute('title') == 'fullUri';
            });

            if (!urlEl) { // if url null return    	
                return;
            }
            else {
                itemUrl = urlEl.innerHTML;
            }

            //check for matched element from cache
            var contentTO = WcmDashboardWidgetCommon.getContentRecursive(dashBoardData, itemUrl);
            if (contentTO) {
                CStudioAuthoring.SelectedContent.unselectContent(contentTO);
                return;
            }
        }
        WcmDashboardWidgetCommon.selectItem(matchedElement, false);
    }
};
