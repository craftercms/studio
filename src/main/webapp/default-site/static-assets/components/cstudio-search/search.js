/**
 * CStudio Search root namespace.
 * 
 * @namespace CStudio
 */
if (typeof CStudioSearch == "undefined" || !CStudioSearch) {
   var CStudioSearch= {};
}

CStudioSearch.DETAIL_OPEN = "open";
CStudioSearch.DETAIL_CLOSED = "closed";
CStudioSearch.DETAIL_TOGGLE = "toggle";

CStudioSearch.searchInProgress = false;

/* availabe search filters */
CStudioSearch.filterRenderers = new Array();

/* search renderers */
CStudioSearch.resultRenderers = new Array();

/* active search filter */
CStudioSearch.activeFilter = null;

/* default search context */
CStudioSearch.searchContext = {
	searchId: null,
	contextName: "default",
	contentTypes: new Array(),
	selectMode: "none",
	interactMode: "none",
	selectLimit: 0,
	itemsPerPage: 20,
	keywords: "",
	contentType: "all",
    filters: null,
	nonFilters: null, 
    sortBy: "relevance",
    sortAsc: true,
    currentPage: 1,	
    searchInProgress: false, 
	presearch: false
};

/**
 * determin the search context
 * (encapsulates looking at the query string)
 */
CStudioSearch.determineSearchContextFromUrl = function() {
	var searchContext = CStudioSearch.searchContext;
	
	var queryString = document.location.search;
	
	var paramMode = CStudioAuthoring.Utils.getQueryVariable(queryString, "mode");
	var paramContext = CStudioAuthoring.Utils.getQueryVariable(queryString, "context");
	var paramSelection = CStudioAuthoring.Utils.getQueryVariable(queryString, "selection");
	var searchId = CStudioAuthoring.Utils.getQueryVariable(queryString, "searchId");
	var itemsPerPage = CStudioAuthoring.Utils.getQueryVariable(queryString, "ipp");
	var page = CStudioAuthoring.Utils.getQueryVariable(queryString, "page");
	var sortBy = CStudioAuthoring.Utils.getQueryVariable(queryString, "sortBy");
	var presearch = CStudioAuthoring.Utils.getQueryVariable(queryString, "presearch");

	/* configure search context */
	searchContext.contextName = (paramContext) ? paramContext : "default";
	searchContext.searchId = (searchId) ? searchId : null;
	searchContext.interactMode = paramMode;
	searchContext.currentPage = (page) ? page : 1;
	searchContext.sortBy = (sortBy) ? sortBy : '';
	searchContext.presearch = (presearch == 'true' || presearch == 'false') ? presearch : 'false';
		
	if(!CStudioAuthoring.Utils.isEmpty(itemsPerPage)) {
		searchContext.itemsPerPage = itemsPerPage;
		YDom.get("cstudio-wcm-search-item-per-page-textbox").value = itemsPerPage;		
	}
	else {		
		searchContext.itemsPerPage = 20;
		YDom.get("cstudio-wcm-search-item-per-page-textbox").value = 20;		
	}
	
	/* configure selection control settings */	
	if(paramSelection) {
		if(paramSelection < -1) {
			searchContext.selectMode = "none";
			searchContext.selectLimit = 0;
		}
		else if(paramSelection == -1) {
			searchContext.selectMode = "many";
			searchContext.selectLimit = -1;
		}
		else if(paramSelection == 0) {
			searchContext.selectMode = "none";
			searchContext.selectLimit = 0;
		}
		else if(paramSelection == 1) {
			searchContext.selectMode = "one";
			searchContext.selectLimit = 1;
		}
		else {
			searchContext.selectMode = "many";
			searchContext.selectLimit = paramSelection;
		}
	}
	else {
		searchContext.selectMode = "none";
		searchContext.selectLimit = 0;
	}
	
	return searchContext;
},

/**
 * This method will put the filter and result values gotten from the filter template into 
 * a 2 hidden input divs , which will be used by the search webscript to pass on to the
 * alfresco service for search purpose, as alfresco services need these info.  
 * It will also initialise the sort dropdown getting data from the filter pagelet
 */
CStudioSearch.init = function(){
	
	// initialize seach context	
	CStudioSearch.searchContext = CStudioSearch.determineSearchContextFromUrl();

	// initialize filter
	CStudioSearch.initializeSearchFilter(); // moved to content-type-map-load
	//CStudioSearch.loadContentTypeMap();

    var searchCallback = function() {
		
		CStudioSearch.updateSearchContextWithBaseOptions();
		CStudioSearch.searchContext = CStudioSearch.augmentContextWithActiveFilter(CStudioSearch.searchContext);
		CStudioSearch.searchContext.page = null;
		
 		CStudioAuthoring.Operations.openSearch(
 			CStudioSearch.searchContext.contextName,
 			CStudioSearch.searchContext, 
 			CStudioSearch.searchContext.selectLimit, 
 			CStudioSearch.searchContext.interactMode,
 			false,
			null,
			CStudioSearch.searchContext.searchId);
    };

    var keywordTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-keyword-textbox");
    var keywordEnterKeyListener = new YAHOO.util.KeyListener(keywordTextBox , { keys:13 }, searchCallback , "keydown"  ); 
    keywordEnterKeyListener.enable();
    
    var paginationTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-item-per-page-textbox");    
    var paginationEnterKeyListener = new YAHOO.util.KeyListener(paginationTextBox , { keys:13 }, searchCallback , "keydown"  ); 
    paginationEnterKeyListener.enable();
    paginationTextBox.onkeyup = function() {
    	CStudioSearch.checkIfNumeric();
	};
	
    YAHOO.util.Event.addListener("cstudio-wcm-search-button", "click", searchCallback);
    YAHOO.util.Event.addListener("cstudio-wcm-search-sort-dropdown", "change", searchCallback);

}

CStudioSearch.checkIfNumeric = function() {
	var paginationTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-item-per-page-textbox");
	var ipp = paginationTextBox.value;
	if (ipp && ipp != '') { 
		if(isNaN(parseInt(ipp))) {
			paginationTextBox.value = "";
		} else {
			paginationTextBox.value = parseInt(ipp);
		} 
	}
}

/**
 * determine the search filter based on the url
 */
CStudioSearch.determineFilterRendererFromUrl = function() {
	var queryString = document.location.search;
	
	var paramContext = unescape(CStudioAuthoring.Utils.getQueryVariable(queryString, "context"));
	var paramSearchId = unescape(CStudioAuthoring.Utils.getQueryVariable(queryString, "searchId"));

	CStudioSearch.searchContext.contextName = paramContext;

	var filterRenderer = CStudioSearch.filterRenderers[paramContext];
	
	if(!filterRenderer) {
		//CStudioSearch.searchContext.contextName = "default";
		//filterRenderer = CStudioSearch.filterRenderers["default"];

		var moduleCb = {
        	moduleLoaded: function(moduleName, clazz, config) {
        		CStudioSearch.initializeSearchFilter(clazz);
            	//var controller = clazz.getController(config.controller);
        	}
        };

        var moduleConfig = { };

		var modulePath = "/proxy/alfresco/cstudio/services/content/content-at-path" +
            "?path=/cstudio/config/sites/" + CStudioAuthoringContext.site + "/search/filters/" + paramContext + ".js";
            
		CStudioAuthoring.Module.requireModule(
        	paramContext,
            modulePath,
            moduleConfig, 
            moduleCb);
	}

	return filterRenderer;
}

/**
 * build the appropriate search filter
 */
CStudioSearch.initializeSearchFilter = function(filter) {
	
	// determine active filter
	if(!filter) {
		CStudioSearch.activeFilter = CStudioSearch.determineFilterRendererFromUrl();
	}
	else {
		CStudioSearch.activeFilter = filter;
	}
	
	if(!CStudioSearch.activeFilter ) return;
	
	// render filter sort options
	var sortOptions = CStudioSearch.activeFilter.getSortOptions();
	var sortBySelectEl = YAHOO.util.Dom.get('cstudio-wcm-search-sort-dropdown');
	if(sortOptions.length > 0) {
		for(var i=0; i<sortOptions.length; i++) {
			var curOption = sortOptions[i];
			sortBySelectEl.options[i] = new Option(curOption.label, curOption.value);
			if(curOption.value == CStudioSearch.searchContext.sortBy)
				sortBySelectEl.options[i].selected = true;
		}
	}
	else {
		sortBySelectEl.options[0] = new Option("Relevance", "relevance");
	}
	
	// render filter body 
	var FilterControlsEl = YDom.get('cstudio-wcm-search-filter-controls');

	FilterControlsEl.innerHTML = "";

	var renderCb = {
		success: function() {

			YDom.get('cstudio-wcm-search-search-title').innerHTML = CStudioSearch.activeFilter.getFilterTitle();

			CStudioSearch.activeFilter.prepareSearchFilterFromUrl();
			
			var presearchElement = new YAHOO.util.Element('cstudio-wcm-search-presearch');  
		    var	presearch = presearchElement.get('value');    
		
		    /* following two methods are called only to check if any pre-filled value is there in the (form/filter) */
		    CStudioSearch.updateSearchContextWithBaseOptions();
			CStudioSearch.searchContext = CStudioSearch.augmentContextWithActiveFilter(CStudioSearch.searchContext);
		   
		    if(CStudioSearch.searchContext.filters != null && CStudioSearch.searchContext.filters.length > 0) {
				for(var i=0; i < CStudioSearch.searchContext.filters.length; i++) { 
					var filterArgs = CStudioSearch.searchContext.filters[0];
					if(filterArgs && filterArgs.value && filterArgs.value != "all") {
						presearch = "true";
						break;
					}
				}
			}
		    
		    if ((presearch == 'true') || (CStudioSearch.searchContext.presearch == 'true')) {    	
		    	CStudioSearch.executeSearch();
		    }
		    
		},
		failure: function() {
		}
	}
		
	CStudioSearch.activeFilter.renderFilterBody(FilterControlsEl, renderCb); 
}

/** 
 * call active filter back and allow it to augment the search context prior
 * to executing the search
 */
CStudioSearch.augmentContextWithActiveFilter = function(context) {
	context.contentTypes = CStudioSearch.activeFilter.getAppliesToContentTypes();
	return CStudioSearch.activeFilter.augmentContextWithActiveFilter(context)
}

/**
 * get the basic search values
 */
CStudioSearch.updateSearchContextWithBaseOptions = function() {

	CStudioSearch.searchContext.contentTypes = new Array();
	CStudioSearch.searchContext.includeAspects = new Array();
	CStudioSearch.searchContext.excludeAspects = new Array();
	CStudioSearch.searchContext.filters = new Array();
	CStudioSearch.searchContext.nonFilters = new Array();
	CStudioSearch.searchContext.keywords = encodeURIComponent(document.getElementById('cstudio-wcm-search-keyword-textbox').value);
	CStudioSearch.searchContext.sortBy = document.getElementById('cstudio-wcm-search-sort-dropdown').value;
	CStudioSearch.searchContext.itemsPerPage = document.getElementById('cstudio-wcm-search-item-per-page-textbox').value;
}

/**
 * This method is called when the search button is pressed
 * Does some initial cleaning like restarting the patination and then calls submitcallback
 */
CStudioSearch.executeSearch = function() {	
	
	var ipp = document.getElementById('cstudio-wcm-search-item-per-page-textbox').value;
	if (ipp && ipp != '' && isNaN(parseInt(ipp))) {
		alert("Enter a number in show item per page");
		return;
	}
	
	CStudioSearch.updateSearchContextWithBaseOptions();
	CStudioSearch.searchContext = CStudioSearch.augmentContextWithActiveFilter(CStudioSearch.searchContext);

	CStudioSearch.fireSearchRequest(CStudioSearch.searchContext);
	//CStudioSearch.toggleResultDetail(CStudioSearch.DETAIL_OPEN);
}

/**
 * convert a path in to an ID
 */
CStudioSearch.pathToId = function(path) {
	var id = "";
	
	if(path) {
		id = path.replace(/\//g, "-");
		id = id.replace(/\./g, "-");
	}
	
	return id;
},

 /**
 * render stock result wrapper
 */
CStudioSearch.renderCommonResultWrapper = function(contentTO, resultBody) {

	return "<div class='cstudio-search-result'>" +
				"<div id='result-select-" + contentTO.resultId + "' class='cstudio-search-select-container'></div>" +
				"<div id='result-status" + contentTO.resultId + "' style='float: left !important; margin-left: 32px;'></div>" +
				"<div style='margin-top: -16px'>"+
					"<div class='cstudio-search-result-body'>" +
						resultBody +
					"</div>" +
				"</div>" +
				"<div style='clear:both;'></div>" +
				"<div class='cstudio-wcm-search-vertical-spacer'></div>" +
			"</div>";
}

/**
 * empty message from search context.  used when result set is empty
 */
CStudioSearch.emptyResultMessage = function(searchContext, searchFailed, failCause) {
	var msg = "<br />";
	
	if (searchFailed) {
		msg += "Search Failed.  Failure reason "+ failCause +".  You may try adjusting your filters in less Search space or contact Administrator";
	} else {
		msg += "<p align='center'><strong>Your search returned no results.</strong></p>";
	}
	return msg;
}

/**
 * render the pagination 
 */
CStudioSearch.renderPagination = function(resultCount, pageCount, searchContext, searchFailed, failCause) {
	var paginationControlsEl = YDom.get("cstudio-wcm-search-pagination-controls");
	var currentPage = CStudioSearch.searchContext.currentPage;
	var previousDisabled = (pageCount <= 1 || currentPage == 1);
	var nextDisabled = (pageCount <= 1 || currentPage == pageCount);
	
	// clear current controls
	paginationControlsEl.innerHTML = "";
	
	if(pageCount == 0) {
		var emptyMsg = document.createElement("div");
		emptyMsg.innerHTML = CStudioSearch.emptyResultMessage(searchContext, searchFailed, failCause)+ "";
		YDom.get('cstudio-wcm-search-result').appendChild(emptyMsg);
		//paginationControlsEl.appendChild(emptyMsg);
		YDom.get("cstudio-wcm-search-result-header-count").innerHTML = "(0 results)";
		YDom.get('cstudio-wcm-search-description-toggle-link').style.display = "none";
		return;
	} else {
		var resultHeaderCount = YDom.get("cstudio-wcm-search-result-header-count");
		var startItem = ((searchContext.currentPage-1)*(searchContext.itemsPerPage))+1;
		var endItem   = (parseInt(startItem) + parseInt(searchContext.itemsPerPage) -1);
		if( endItem > resultCount) 
			endItem  = resultCount;  
		resultHeaderCount.innerHTML = "(Showing "+startItem+"-"+endItem+" of "+resultCount+")";
	}
	
	// construct previous control
	var previousEl = null;

	if(previousDisabled) { // this can be done by disbaling the link+ccs right?
		previousEl = document.createElement("span");
  	    previousEl.innerHTML = "&laquo; Previous &nbsp;";
		YDom.addClass(previousEl, "disabled");
	}
	else {
	    previousEl = document.createElement("a");
		previousEl.innerHTML = "&laquo; Previous &nbsp;";
		previousEl.className = "cstudio-wcm-search-pagination-number";

		previousEl.onclick = function() {
			CStudioSearch.searchContext.page = parseInt(CStudioSearch.searchContext.currentPage) - 1;

	 		CStudioAuthoring.Operations.openSearch(
	 		 	CStudioSearch.searchContext.contextName,
	 			CStudioSearch.searchContext, 
	 			CStudioSearch.searchContext.selectLimit, 
	 			CStudioSearch.searchContext.interactMode,
	 			false,
				null,
				CStudioSearch.searchContext.searchId);
		};
	}

	// construct next control
	var nextEl =  null;
		
	if(nextDisabled) { 
		nextEl = document.createElement("span");
		nextEl.innerHTML = "&nbsp; Next &raquo;";
		YDom.addClass(nextEl, "disabled");
	}
	else {
		nextEl = document.createElement("a");
		nextEl.innerHTML = "Next &raquo;";
		nextEl.className = "cstudio-wcm-search-pagination-number";

		nextEl.onclick = function() {
			CStudioSearch.searchContext.page = parseInt(CStudioSearch.searchContext.currentPage) + 1;

	 		CStudioAuthoring.Operations.openSearch(
	 			CStudioSearch.searchContext.contextName,
	 			CStudioSearch.searchContext,
	 			CStudioSearch.searchContext.selectLimit, 
	 			CStudioSearch.searchContext.interactMode,
	 			false,
				null,
				CStudioSearch.searchContext.searchId);

		};
	}
	
	//add previous link element
	paginationControlsEl.appendChild(previousEl);

	//construct pagination numbers
	var startPage = parseInt(CStudioSearch.searchContext.currentPage) >= 5 ? (parseInt(CStudioSearch.searchContext.currentPage) - 5) : 0;
	var endPage   = parseInt(startPage) + 8 > pageCount ? pageCount : (parseInt(startPage) + 8);
	if(parseInt(pageCount - CStudioSearch.searchContext.currentPage) < 4) {
		endPage   = pageCount
		startPage = parseInt(endPage) - 8 > 0 ? parseInt(endPage) - 8 : 0;
	}
	for(var i=startPage; i < endPage; i++) {
		
		if(parseInt(i+1) == parseInt(CStudioSearch.searchContext.currentPage)) {
			var pageEl = document.createElement("span");
			pageEl.innerHTML = "&nbsp;" + (i+1) + "&nbsp;&nbsp;&nbsp;";
			pageEl.pageNumber = i+1;
			pageEl.className = "disabled";
		} else {
			var pageEl = document.createElement("a");
			pageEl.innerHTML = "&nbsp;" + (i+1);
			pageEl.pageNumber = i+1;
			
			pageEl.className = "cstudio-wcm-search-pagination-number";

			pageEl.onclick = function() {
				CStudioSearch.searchContext.page = this.pageNumber;
				
				CStudioAuthoring.Operations.openSearch(
						CStudioSearch.searchContext.contextName,
						CStudioSearch.searchContext, 
						CStudioSearch.searchContext.selectLimit, 
						CStudioSearch.searchContext.interactMode,
						false,
						null,
						CStudioSearch.searchContext.searchId);

			}
		}

		paginationControlsEl.appendChild(pageEl);
	}

	// add next link element
	paginationControlsEl.appendChild(nextEl);
}

/**
 * called right before firing the search request
 * used to get basic values in to search context and clean up previous searches
 */
CStudioSearch.preFireSearch = function(searchContext) {

	// clear out current results
	//YDom.get('cstudio-wcm-search-result').innerHTML = "";
	// disabling the search button until the callback
	var searchSubmitButton = YDom.get("cstudio-wcm-search-button");
	var keywordTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-keyword-textbox");
	var paginationTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-item-per-page-textbox");
	var sortDropDown = YAHOO.util.Dom.get("cstudio-wcm-search-sort-dropdown");
	
	searchSubmitButton.disabled = "disabled";
	keywordTextBox.disabled = "disabled";
    paginationTextBox.disabled = "disabled";
    sortDropDown.disabled = "disabled";
    
	CStudioSearch.toggleResultDetail("hide-link");
	
	var resultHeaderInProgress = YDom.get("cstudio-wcm-search-result-in-progress");
	var contextPath = document.location.protocol + "//" + document.location.hostname + ":" + document.location.port + CStudioAuthoringContext.baseUri+"/";  
	var imgEl = document.createElement("img");
	imgEl.src = contextPath + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";
	resultHeaderInProgress.appendChild(imgEl);
	
	// clear out pagination
	YDom.get('cstudio-wcm-search-pagination-controls').innerHTML = "";
}

/**
 * Method for submitting the the form to the webscript
 * And to return the result and put the result in the result div
 */
CStudioSearch.fireSearchRequest = function(searchContext) {
	
	// call preFire event
	CStudioSearch.preFireSearch(searchContext);
	
	// define callback
	var callback = {
			success: function(results)  {
				var searchSubmitButton = YDom.get("cstudio-wcm-search-button");
				var keywordTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-keyword-textbox");
				var paginationTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-item-per-page-textbox");
				var sortDropDown = YAHOO.util.Dom.get("cstudio-wcm-search-sort-dropdown");
				var searchResultsLabels = new Array();
				
				searchSubmitButton.disabled = "";
				keywordTextBox.disabled = "";
			    paginationTextBox.disabled = "";
			    sortDropDown.disabled = "";
			    
				CStudioSearch.toggleResultDetail(CStudioSearch.DETAIL_OPEN);
				
				//var resultHeaderInProgress = YDom.get("cstudio-wcm-search-result-in-progress");  
				//resultHeaderInProgress.innerHTML = "";
				
				var resultTableEl = document.getElementById('cstudio-wcm-search-result');
				resultTableEl.innerHTML = "";
				
				if(resultTableEl) {
					
					var resultCount = results.resultCount;
					var pageTotal = results.pageTotal;
			        
			        /* render pagination */
			        CStudioSearch.renderPagination(resultCount, pageTotal, searchContext, results.searchFailed, results.failCause);
			        
			        var previousResultIds = [];
			        var exists = function(elementId) {
			        	for(var i = 0;i < previousResultIds.length; i++) {
			        		if(elementId == previousResultIds[i]) return true;
			        	}
			        	return false;
			        }
					
					/* render results */
			        for(var i=0; i<results.objectList.length; i++) {
			   			var resultsHTML = "";
			        	var contentItem = results.objectList[i],
                            _item = contentItem.item;
			        	contentItem.resultId = CStudioSearch.pathToId(_item.uri);
			        	
			        	/********************************************************************************
			        	 * handling duplicate item issue due to indexing.
			        	 * Starts
			        	 *******************************************************************************/
			        	if( exists(contentItem.resultId) ) contentItem.resultId += '-----' + i;   
			        	previousResultIds.push(contentItem.resultId.split("-----")[0]);
			        	/********************************************************************************
			        	 * handling duplicate item issue due to indexing.
			        	 * Ends
			        	 ********************************************************************************/
			        	
				        /* render result */
			        	var resultEl = document.createElement("div");
			        	resultTableEl.appendChild(resultEl);
			        	
			        	var resultTemplate = CStudioSearch.resultRenderers[_item.contentType];
			      
			        	if(resultTemplate) {
			        	 	resultsHTML = resultTemplate.render(contentItem);
			        	}
			        	else {
			        		var extension = _item.name.substring(_item.name.lastIndexOf(".")+1);
			        		resultTemplate = CStudioSearch.resultRenderers[extension];
			        		
			        		if(resultTemplate) {
				        	 	resultsHTML = resultTemplate.render(contentItem);			        			
			        		}
			        		else {
				        	 	resultsHTML = CStudioSearch.ResultRenderer.Default.render(contentItem);
			        		}
			        	}
				      	
				      	resultEl.innerHTML = resultsHTML;
	
						var resultStatusIconStyle = CStudioAuthoring.Utils.getIconFWClasses(_item),
                            resultStatusEl = YDom.get("result-status"+contentItem.resultId),
				      	    nextElement = resultStatusEl.nextElementSibling;
				      	
				      	//Adding Stike To next Element of the Icon (Title) EMO-9406
				      	if(_item.disabled) {
							var titleEl = nextElement.firstChild.firstChild;
				      		YDom.addClass(titleEl, 'strike-dashboard-item');
				      		titleEl.style.color = "#0176B1";
				      	}

                        YDom.addClass(resultStatusEl, resultStatusIconStyle);

					//Add Tool tip information for item
					var searReultItem = resultStatusEl.nextSibling;
					if (searReultItem) {
						var itemTitle = CStudioAuthoring.Utils.getTooltipContent(contentItem.item);
						var oSpan = document.createElement("div");
						oSpan.setAttribute("id", "search-item-tt-" + contentItem.resultId);
						oSpan.setAttribute("title", itemTitle);
						searReultItem.parentNode.insertBefore(oSpan, searReultItem);
						oSpan.appendChild(resultStatusEl);
						oSpan.appendChild(searReultItem);
						searchResultsLabels.push("search-item-tt-" + contentItem.resultId);

						//Adjusting left padding for a long internal title
						/*
						if (contentItem.item.internalName.length > 60) {
							oSpan.setAttribute("class", "search-page-title");
							var oDiv = document.createElement("div");
							oDiv.setAttribute("class", "search-item-title");
							oSpan.parentNode.insertBefore(oDiv, oSpan);
							oDiv.appendChild(oSpan);

							//add next sibling also inside div so that all will appear in-line.
							if (oDiv.nextSibling && oDiv.nextSibling.tagName && oDiv.nextSibling.tagName.toLowerCase() == "span") {
								oDiv.appendChild(oDiv.nextSibling);
							}

							//remove br tag next to div
							if (oDiv.nextSibling && oDiv.nextSibling.tagName && oDiv.nextSibling.tagName.toLowerCase() == "br") {
								oDiv.nextSibling.parentNode.removeChild(oDiv.nextSibling);
							}
						}
						*/
					}

				      	/* instrument result */
				      	if(this.searchContext.selectMode == "many") {
				      		
				      		var selectResultEl = YDom.get("result-select-"+contentItem.resultId);
				      		var controlEl = document.createElement("input");
				      		controlEl.type = "checkbox";
				      		controlEl.name = "result-select";
							controlEl.contentTO = contentItem;							
				      		selectResultEl.appendChild(controlEl);				      		 
							if (contentItem != null && contentItem.item != null && contentItem.item.inFlight) {
								controlEl.disabled = true;
							}
				      		controlEl.onchange = function() { 
				      			
				      			if(this.checked) {
				      				if(searchContext.selectLimit != -1){
				      					if(CStudioAuthoring.SelectedContent.getSelectedContentCount() < searchContext.selectLimit) {
							      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO.item);
				      					}
				      					else {
				      						this.checked = false;
				      					}
				      				}
				      				else {
						      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO.item);
				      				}
				      			}
				      			else {
				      				CStudioAuthoring.SelectedContent.unselectContent(this.contentTO.item);
				      			}
				      			//enable disable Add Item button based on selection
				      			if(document.getElementById("submission-controls")) {
					      			if(CStudioAuthoring.SelectedContent.getSelectedContentCount() > 0){
					      				document.getElementById("submission-controls").firstChild.disabled = "";
					      			}else {
					      				document.getElementById("submission-controls").firstChild.disabled = "disabled";
					      			}
				      			}
				      		};
				      	}
				      	else if(this.searchContext.selectMode == "one") {
				      		var selectResultEl = YDom.get("result-select-"+contentItem.resultId);
				      		var controlEl = document.createElement("input");
				      		controlEl.type = "radio";
				      		controlEl.name = "result-select";
							controlEl.contentTO = contentItem;							
				      		selectResultEl.appendChild(controlEl);
				      		

				      		controlEl.onchange = function() {
				      			
				      			if(CStudioSearch.selectedContentTO) {
					      			CStudioAuthoring.SelectedContent.unselectContent(CStudioSearch.selectedContentTO.item);
				      			}
				      			
				      			CStudioSearch.selectedContentTO = this.contentTO;
				      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO.item);
				      			
				      			//enable disable Add Item button based on selection
				      			if(document.getElementById("submission-controls")){				      				
					      			if(CStudioAuthoring.SelectedContent.getSelectedContentCount() > 0){
					      				document.getElementById("submission-controls").firstChild.disabled = "";
					      			}else {
					      				document.getElementById("submission-controls").firstChild.disabled = "disabled";
					      			}
				      			}
				      		} 
				      	}				      	
			        }

					var toolTipContainer = document.createElement("div");
					toolTipContainer.setAttribute("id", "acn-context-tooltip-search");
					toolTipContainer.className = "acn-context-tooltip";
					toolTipContainer.innerHTML = "<div style=\"z-index: 2; left: 73px; top: 144px; visibility: hidden;\"" +
									" class=\"yui-module yui-overlay yui-tt\"" +
								        "id=\"acn-context-tooltipWrapper-search\"><div class=\"bd\"></div>" +
									"<div class=\"yui-tt-shadow\"></div>" +
									"<div class=\"yui-tt-shadow\"></div>" +
									"<div class=\"yui-tt-shadow\"></div>" +	"</div>";
					
					resultTableEl.appendChild(toolTipContainer);


					new YAHOO.widget.Tooltip("acn-context-tooltipWrapper-search", {
							context: searchResultsLabels,
							hidedelay:0,
							showdelay:500,
							container: "acn-context-tooltip-search"
					});

					//disable links for "cstudio-search-no-preview" items.
					var noPreviewLinks = YDom.getElementsByClassName("cstudio-search-no-preview");
					if (noPreviewLinks && noPreviewLinks.length >= 1) {
						for (var linkIdx = 0; linkIdx < noPreviewLinks.length; linkIdx++) {
							noPreviewLinks[linkIdx].href = "JavaScript:void(0)";
							noPreviewLinks[linkIdx].removeAttribute("onclick");
							noPreviewLinks[linkIdx].setAttribute("onclick", "return false")
						}
					}

					var searchFinishDiv = document.createElement("div");
					searchFinishDiv.id = "cstudio-wcm-search-render-finish";
					resultTableEl.appendChild(searchFinishDiv);
					
					// initialize flash view starts
					var els = document.getElementsByClassName("flash-banner-wrapper");
					for(var idx = 0; idx < els.length; idx++) {
						var container = els[idx];
						var flashSrc = container.lastChild.value; 
						var flashObject = new FlashObject(flashSrc, "swf-" + idx, "auto", "auto", 6, "");
						flashObject.addParam("wmode","transparent");
						flashObject.addParam("border","1px solid");
						flashObject.write(container);
					}
					// initialize flash view ends					
				} 
			},
	        failure: function(o) {
				var searchSubmitButton = YDom.get("cstudio-wcm-search-button");
				var keywordTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-keyword-textbox");
				var paginationTextBox = YAHOO.util.Dom.get("cstudio-wcm-search-item-per-page-textbox");
				var sortDropDown = YAHOO.util.Dom.get("cstudio-wcm-search-sort-dropdown");
				
				searchSubmitButton.disabled = "";
				keywordTextBox.disabled = "";
			    paginationTextBox.disabled = "";
			    sortDropDown.disabled = "";

				//	var searchFailedFlag = ,"searchFailed":false,"failCause":"",		        
			    YDom.get('cstudio-wcm-search-result').innerHTML =  "<p align='center'><strong>Unable to Retrieve Search Result. Please Try again.</strong><br><br><br><i>If this is consistent, please contact Crafter Studio Administrator.</i></p>";//+"<br><br><p align='center'>For Developers only, will be removed later.  <br></p><br><br><br>" + o.responseText + "<br>";
			},
			        
			searchContext: searchContext		
	};

	// perform search
	CStudioAuthoring.Service.search(CStudioAuthoringContext.site, searchContext, callback);
}	


/**
 * result line items that identify detail areas can be collapsed to allow for a tighter
 * search result
 */
CStudioSearch.toggleResultDetail = function(state){
	var cssClass = 'cstudio-wcm-search-invisible';
	var elements = YAHOO.util.Dom.getElementsByClassName('cstudio-search-description');
	var link = new YAHOO.util.Element('cstudio-wcm-search-description-toggle-link');
	
	if(state == "hide-link") {
		link.set("innerHTML", "");
		return;
	}
	if(state == CStudioSearch.DETAIL_OPEN) {
		YAHOO.util.Dom.removeClass(elements, cssClass);		
		link.set("innerHTML", "Hide Descriptions");
	}
	else if(state == CStudioSearch.DETAIL_CLOSE) {
		YAHOO.util.Dom.addClass(elements, cssClass);
		link.set("innerHTML", "Show Descriptions");		
	}
	else {
		
		if(YAHOO.util.Dom.hasClass(elements[0], cssClass)){		
			YAHOO.util.Dom.removeClass(elements, cssClass);		
			CStudioSearch.toggleDescriptionLink();
		}
		else{
			YAHOO.util.Dom.addClass(elements, cssClass);
			CStudioSearch.toggleDescriptionLink();
		}
	}
}

/**
 * following method returns a div with a separation bar, if private.  Otherwise, nothing.  
 */
CStudioSearch.publicPrivateIcon = function(pnpText) {
	if ((!CStudioAuthoring.Utils.isEmpty(pnpText)) && (pnpText.toLowerCase() == "private")) 
		return " | <span class='status-icon private new component'></span>";
	return "";
}

/**
 * The following method creates a image pop up if the src is given
 * through parameter
 * */
CStudioSearch.magnifyBannerImage = function(imgSrc) {
	try {
		var width = 0;
		var height = 0;
		
		if (document.documentElement && document.documentElement.scrollWidth ) {
			width = document.documentElement.scrollWidth;
		} else if (document.body.scrollWidth > document.body.offsetWidth) {
			width = document.body.scrollWidth;
		} else {
			width = document.body.offsetWidth;
		}

		if (document.documentElement && document.documentElement.scrollHeight ) {
			height = document.documentElement.scrollHeight;
		} else if (document.body.scrollHeight > document.body.offsetHeight) {
			height = document.body.scrollHeight;
		} else {
			height = document.body.offsetHeight;
		}

		var maskingDiv = document.createElement("div");
		maskingDiv.style.backgroundColor = "#000000";
		maskingDiv.style.opacity = "0.25";
		maskingDiv.style.position = "absolute";
		maskingDiv.style.display = "block";
		maskingDiv.style.width = width + "px";
		maskingDiv.style.height = height + "px";
		maskingDiv.style.top = "0";
		maskingDiv.style.right = "0";
		maskingDiv.style.bottom = "0";
		maskingDiv.style.left = "0";
		maskingDiv.style.zIndex = "31";
		
		var containerDiv = document.createElement("div");
		containerDiv.id = "cstudio-wcm-search-banner-image-pop-up";
		containerDiv.style.position = "absolute";
		containerDiv.style.right = "0";
		containerDiv.style.bottom = "0";
		containerDiv.style.zIndex = "32";
		
		var imageContent = document.createElement("img");
		imageContent.src = imgSrc;
		imageContent.style.borderWidth = "10px 10px 50px";
		imageContent.style.borderColor = "#DADADA";
		imageContent.style.borderStyle = "solid";
		containerDiv.style.width = (imageContent.width + 20) + "px";
		containerDiv.style.height = (imageContent.height + 60) + "px";
		containerDiv.style.borderWidth = "5px";
		containerDiv.style.borderColor = "black";
		containerDiv.style.borderStyle = "solid";
		
		var buttonHolderDiv = document.createElement("div");
		buttonHolderDiv.style.textAlign = "center";
		buttonHolderDiv.style.marginTop = "-35px";
		
		var closeButton = document.createElement("input");
		closeButton.setAttribute("type","button");
		closeButton.value = "Close";
		closeButton.onclick = function () {
			document.body.removeChild(containerDiv);
			document.body.removeChild(maskingDiv);
			
			//disable scroll bars for the window.
			document.getElementsByTagName("body")[0].style.overflow = "auto";
		};
		
		buttonHolderDiv.appendChild(closeButton);
		
		containerDiv.appendChild(imageContent);
		containerDiv.appendChild(buttonHolderDiv);
		
		document.body.appendChild(containerDiv);
		document.body.appendChild(maskingDiv);
		
		/**
		** render pop up in the center of the screen.
		**/
		var imagePopUp = new YAHOO.widget.Overlay("cstudio-wcm-search-banner-image-pop-up");
		imagePopUp.center();
		imagePopUp.render();
		
		//disable scroll bars for the window.
		document.getElementsByTagName("body")[0].style.overflow = "hidden";
	} catch (err) {
		//alert("["+err+"]");
	}
}

CStudioSearch.magnifyFlashBanner = function(magnifyer) {
	try {
		var width = 0;
		var height = 0;
		
		if (document.documentElement && document.documentElement.scrollWidth ) {
			width = document.documentElement.scrollWidth;
		} else if (document.body.scrollWidth > document.body.offsetWidth) {
			width = document.body.scrollWidth;
		} else {
			width = document.body.offsetWidth;
		}

		if (document.documentElement && document.documentElement.scrollHeight ) {
			height = document.documentElement.scrollHeight;
		} else if (document.body.scrollHeight > document.body.offsetHeight) {
			height = document.body.scrollHeight;
		} else {
			height = document.body.offsetHeight;
		}

		var maskingDiv = document.createElement("div");
		maskingDiv.style.backgroundColor = "#000000";
		maskingDiv.style.opacity = "0.25";
		maskingDiv.style.position = "absolute";
		maskingDiv.style.display = "block";
		maskingDiv.style.width = width + "px";
		maskingDiv.style.height = height + "px";
		maskingDiv.style.top = "0";
		maskingDiv.style.right = "0";
		maskingDiv.style.bottom = "0";
		maskingDiv.style.left = "0";
		maskingDiv.style.zIndex = "31";
		
		var containerDiv = document.createElement("div");
		containerDiv.id = "cstudio-wcm-search-banner-image-pop-up";
		containerDiv.style.position = "absolute";
		containerDiv.style.right = "0";
		containerDiv.style.bottom = "0";
		containerDiv.style.zIndex = "32";
		
		var flashContent = magnifyer.previousSibling.firstChild.cloneNode(true);
		flashContent.id += "-popup"; 
		flashContent.wmode = "";
		flashContent.style.background = "white";
		flashContent.style.borderWidth = "10px 10px 50px";
		flashContent.style.borderColor = "#DADADA";
		flashContent.style.borderStyle = "solid";
		var orgHeightWidth = magnifyer.nextSibling.nextSibling.nextSibling.nextSibling.value.split("|");
		var orgHeight = orgHeightWidth[0];
		var orgWidth = orgHeightWidth[1];
		flashContent.width = parseInt(orgWidth, 10) + 20;
		containerDiv.style.width = flashContent.width + "px";
		flashContent.height = parseInt(orgHeight, 10) + 60;
		containerDiv.style.height = flashContent.height + "px";		
		containerDiv.style.borderWidth = "5px";
		containerDiv.style.borderColor = "black";
		containerDiv.style.borderStyle = "solid";
		
		var buttonHolderDiv = document.createElement("div");
		buttonHolderDiv.style.textAlign = "center";
		buttonHolderDiv.style.marginTop = "-35px";
		
		var closeButton = document.createElement("input");
		closeButton.setAttribute("type","button");
		closeButton.value = "Close";
		closeButton.onclick = function () {
			document.body.removeChild(containerDiv);
			document.body.removeChild(maskingDiv);
			
			//disable scroll bars for the window.
			document.getElementsByTagName("body")[0].style.overflow = "auto";
		};
		
		buttonHolderDiv.appendChild(closeButton);
		
		containerDiv.appendChild(flashContent);
		containerDiv.appendChild(buttonHolderDiv);
		
		document.body.appendChild(containerDiv);
		document.body.appendChild(maskingDiv);
		
		/**
		** render pop up in the center of the screen.
		**/
		var imagePopUp = new YAHOO.widget.Overlay("cstudio-wcm-search-banner-image-pop-up");
		imagePopUp.center();
		imagePopUp.render();
		
		//disable scroll bars for the window.
		document.getElementsByTagName("body")[0].style.overflow = "hidden";
	} catch (err) {
		//alert("["+err+"]");
	}
}

/**
 * Loads the big content-type map to serve different result-template.   
 */
CStudioSearch.loadContentTypeMap = function() {
	// not loaded yet.  Load service is expensive
	if (!(CStudioSearch.ContentTypeConfigMap.length > 0)) {
		var contentTypeCallback =  {
			success: function(typeContainer) {			
				if(typeContainer && typeContainer.types) {
					for(var i = 0; i < typeContainer.types.length; i++)  {
						var ctype = typeContainer.types[i];
						CStudioSearch.ContentTypeConfigMap[ctype.name] = ctype.label;
					}
				}
				CStudioSearch.initializeSearchFilter();
			},
			
			failure: function() {
				CStudioSearch.initializeSearchFilter();
			},
		};
		try {
			CStudioAuthoring.Service.getAllContentTypesForSite(CStudioAuthoringContext.site, contentTypeCallback);
		} catch (e) {
			//alert(e);
		}
	} else { // already loaded.  
		CStudioSearch.initializeSearchFilter();
	}
}

CStudioSearch.getContentTypeName = function(ctype) {
	if (CStudioAuthoring.Utils.isEmpty(ctype)) 
		return "Unknown Type";
	var ctypeName = CStudioSearch.ContentTypeConfigMap[ctype]; 
	return (CStudioAuthoring.Utils.isEmpty(ctypeName)) ? ("["+ctype+"] Template") : ctypeName;
}

/**
 * The following line calls init function 
 */
CStudioSearch.inited = false;
YAHOO.util.Event.onDOMReady(function() {
	if(!CStudioSearch.inited) {
		//TODO this fires twice for some reason?
		// two includes?
		CStudioSearch.inited = true;
		CStudioSearch.init();
	}
});

//when search form is open Add Item button is set disabled
YAHOO.util.Event.onAvailable("submission-controls", function() {
	document.getElementById("submission-controls").firstChild.disabled = "disabled";				
}, this);

/**
 * Declare Filter Renderer Namepsce
 */
CStudioSearch.FilterRenderer = {};

/**
 * Declare Result Renderer Namespace
 */ 
CStudioSearch.ResultRenderer = {};

/**
 * Content type name map
 */
CStudioSearch.ContentTypeConfigMap = [];

/**
 * toggle the show/hide description link
 */ 
CStudioSearch.toggleDescriptionLink =  function(){	
	var link = YDom.get('cstudio-wcm-search-description-toggle-link');
	if(link){
		if(link.innerHTML == 'Show Descriptions'){		
			link.innerHTML =  "Hide Descriptions";			
		}else{		
			link.innerHTML = "Show Descriptions";			
		}	
	}	
}