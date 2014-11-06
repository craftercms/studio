var YDom = YAHOO.util.Dom;
var contextPath = location.protocol + "//" + location.hostname + ":" + location.port;  

if (typeof CStudioAuthoringWidgets == "undefined" || !CStudioAuthoringWidgets) {
    var CStudioAuthoringWidgets = {};
}

/**
 * golive queue
 */
CStudioAuthoringWidgets.GoLiveQueueDashboard = CStudioAuthoringWidgets.GoLiveQueueDashboard || function(widgetId, pageId) {

    this.widgetId = widgetId;
    this.pageId = pageId;
    this._self = this;
    this.showInprogressItems = false;
    this.expanded = true;
    this.hideEmptyRow = true;
    this.defaultSortBy = 'eventDate';
    this.skipComponentSort = true;
    this.tooltipLabels=null;
    WcmDashboardWidgetCommon.init(this);

    /**
     * get table data
     */
    this.retrieveTableData = function(sortBy, sortAscDesc, callback) {
        sortAscDesc = CStudioAuthoring.Utils.sortByAsc.init(sortBy, widgetId);
        CStudioAuthoring.Service.getGoLiveQueueItems(
                CStudioAuthoringContext.site,
                this.showInprogressItems,
                sortBy,
                sortAscDesc,
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

        
        itemFilterEl.innerHTML = CMgs.format(langBundle, "dashletGoLiveShowInProgress");  
        itemFilterEl.hfref = "javascript:void(0);";
        itemFilterEl.id = "widget-expand-state-" + widgetId;
        YDom.addClass(itemFilterEl, "widget-expand-state");

        
        containerEl.appendChild(listItemEl);
        containerEl.appendChild(liLoadingEl);
        listItemEl.appendChild(itemFilterEl);
        itemFilterEl._self = this;

        itemFilterEl.onclick = function() {
            var _self = this._self;

            _self.showInprogressItems = !_self.showInprogressItems;

            if (_self.showInprogressItems) {
                this.innerHTML = CMgs.format(langBundle, "dashletGoLiveHideInProgress"); 
            }
            else {
                this.innerHTML = CMgs.format(langBundle, "dashletGoLiveShowInProgress");  
            }

            /* 
             * As we are using same loading function for sorting and Show/Hide In-Progress Items
             * Along with Show/Hide In-Progress Items, sorting also executing.
             * While calling function for Hide/Show In-Progress Items, We need to set reverse sort order
             * so that Hide/Show function will execute with current sort order.
            */
            var previousSortType = YDom.get('sort-type-' + _self.widgetId).innerHTML;
            if (previousSortType == "true") {
                YDom.get('sort-type-' + _self.widgetId).innerHTML = "false";
            } else if (previousSortType == "false") {
                YDom.get('sort-type-' + _self.widgetId).innerHTML = "true";
            }

            var sortBy = _self.currentSortBy ? _self.currentSortBy : _self.defaultSortBy;
            WcmDashboardWidgetCommon.loadTableData(
                    sortBy,
                    YDom.get(_self.widgetId),
                    _self.widgetId);
        };
    };

    /**
     * callback to render the table headings
     */
    this.renderItemsHeading = function() {

        var widgetId = this._self.widgetId,
            Common = WcmDashboardWidgetCommon;

        var header = [
            Common.getSortableRow("internalName", widgetId, CMgs.format(langBundle, "dashletGoLiveColPageName"), "minimize"),
            Common.getSimpleRow("edit", widgetId, CMgs.format(langBundle, "dashletGoLiveColEdit"), "minimize"),
            Common.getSortableRow("browserUri", widgetId, CMgs.format(langBundle, "dashletGoLiveColURL"), "maximize"),
            '<th id="fullUri" class="width0"></th>',
            Common.getSimpleRow("scheduledDate", widgetId, CMgs.format(langBundle, "dashletGoLiveColPublishDate"), ""),
            Common.getSortableRow("userLastName", widgetId, CMgs.format(langBundle, "dashletGoLiveColLastEditedBy"), "alignRight minimize"),
            Common.getSortableRow("eventDate", widgetId, CMgs.format(langBundle, "dashletGoLiveColLastEditedDate"), "ttThColLast alignRight minimize")
        ].join('');

        return header;
    };

    /**
     * optional on click handler
     */
    this.onCheckedClickHandler = function(event, matchedEl) {
        this.handleTopDownPathDependenciesItemsClick(event, matchedEl);
    }

    /**
     * Call back to render each line item of the table
     */
    this.renderLineItem = function(item, isFirst, count, depth) {

        var html = [],
            name = item.internalName,
            editLinkId;

        //reducing max character length to support 1024 screen resolution
        var removeCharCount = ((window.innerWidth <= 1024)?5:0);
        var displayName = WcmDashboardWidgetCommon.getFormattedString(name, (80 - removeCharCount), item.newFile);

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
                displayBrowserUri = WcmDashboardWidgetCommon.getFormattedString(browserUri, (50 - removeCharCount)),
                uri = item.uri,
                fmt = CStudioAuthoring.Utils.formatDateFromString;

            editLinkId = 'editLink_' + this.widgetId + '_' + WcmDashboardWidgetCommon.encodePathToNumbers(item.uri);

            var ttSpanId =  "tt_" + this.widgetId + "_" + item.uri + "_" + (this.tooltipLabels.length + 1);
            var itemTitle = CStudioAuthoring.Utils.getTooltipContent(item);
            this.tooltipLabels.push(ttSpanId);

            if (item.component && item.internalName != "crafter-level-descriptor.level.xml") {
                browserUri = "";
                displayBrowserUri = "";
            }
/*
            if (item.internalName == "crafter-level-descriptor.level.xml") {
                displayName = "Section Defaults" + (item.newFile?"*":"");
            }
*/

            var itemIconStatus = CStudioAuthoring.Utils.getIconFWClasses(item);
            itemIconStatus += ((item.disabled && !item.previewable) ? ' non-previewable-disabled' : '');

            // this API will replace double quotes with ASCII character
            // to resolve page display issue
            displayName = CStudioAuthoring.Utils.replaceWithASCIICharacter(displayName);
            
            if(item.uri.indexOf(".xml") != -1) {
                WcmDashboardWidgetCommon.insertEditLink(item, editLinkId);
            }
            
            html = html.concat([
                '<td>',
                    '<div class="dashlet-cell-wrp">', depth ?
                        '<div class="dashlet-ident">' : '',
                            '<div class="dashlet-ident">',
                                '<input type="checkbox" class="dashlet-item-check" id="', uri, '"', ((item.deleted || item.inFlight) ? ' disabled' : ''), ' />',
                                '<span class="', itemIconStatus, '" id="' + ttSpanId + '" title="' + itemTitle + '">',
                                    '<a href="#" class="', (item.previewable == true) ? "previewLink" : "non-previewable-link", '">',
                                        displayName,
                                    '</a>',
                                '</span>',
                            '</div>', depth ?
                        '</div>' : '',
                    '</div>',
                '</td>',
                '<td id="' + editLinkId + '"></td>',
                "<td title='",browserUri,"'>", displayBrowserUri, "</td>",
                "<td title='fullUri' class='width0'>", uri, "</td>",
                '<td class="">', item.scheduled ? fmt(item.scheduledDate, 'tooltipformat') : '', '</td>',
                "<td class='alignRight'>", WcmDashboardWidgetCommon.getDisplayName(item), "</td>",
                "<td class='alignRight ttThColLast'>", CStudioAuthoring.Utils.formatDateFromString(item.eventDate), "</td>"
            ]);

        }

        return html.join('');
    };

    /**
     * internal method to help determine what items are parents
     * We need to improve this - performance is not great and there must be an easier way
     */
    this.handleTopDownPathDependenciesItemsClick = function(event, matchedEl) {

        var isChecked = matchedEl.checked;
        var selectedElementURI = matchedEl.id;
        var item = CStudioAuthoringWidgets.GoLiveQueueDashboard.resultMap[selectedElementURI];
        WcmDashboardWidgetCommon.selectItem(matchedEl, matchedEl.checked);
        //CStudioAuthoring.SelectedContent.selectContent({"item":item});

        if (isChecked) {//check all parents
            var parentURI = item.mandatoryParent;
            if (parentURI) {
                var parentInputElement = YDom.get(parentURI);
                if (parentInputElement) {                    
                    parentInputElement.checked = true;
                    this.handleTopDownPathDependenciesItemsClick(null, parentInputElement);
                }
            }
        } else {//deselect all children
            var children = CStudioAuthoring.Service.getChildren(item, CStudioAuthoringWidgets.GoLiveQueueDashboard.resultMap);
            if (children.length > 0) {
                for (var i = 0; i < children.length; i++) {
                    var child = children[i];
                    var childInputElement = YDom.get(child.uri);
                    if (childInputElement) {
                        childInputElement.checked = false;
                        this.handleTopDownPathDependenciesItemsClick(null, childInputElement);
                    }
                }
            }

        }
    };
};
