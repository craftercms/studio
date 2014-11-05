/**
 * Namespace is intentially kept as Search so that we can re-use the response filters.  The long term plan is to
 * merge search and browse together.  The only reason they are different today is due to limitations in Alresco AVM itself (path queries in Sandboxes do are not supported)
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
	var path = CStudioAuthoring.Utils.getQueryVariable(queryString, "PATH");
	
	/* configre search context */
	searchContext.contextName = (paramContext) ? paramContext : "default";
	searchContext.searchId = (searchId) ? searchId : null;
	searchContext.interactMode = paramMode;
	searchContext.presearch = true;
	searchContext.path = path;
			
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
	CStudioSearch.initializeSearchFilter(); 
}


/**
 * build the appropriate search filter
 */
CStudioSearch.initializeSearchFilter = function() {
	
	// determine active filter
	CStudioSearch.activeFilter = CStudioSearch.filterRenderers["default"];

	// render filter sort options

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
		    	
		    CStudioSearch.executeSearch();
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
}

/**
 * This method is called when the search button is pressed
 * Does some initial cleaning like restarting the patination and then calls submitcallback
 */
CStudioSearch.executeSearch = function() {	
	

	CStudioSearch.updateSearchContextWithBaseOptions();
	CStudioSearch.searchContext = CStudioSearch.augmentContextWithActiveFilter(CStudioSearch.searchContext);

	CStudioSearch.fireSearchRequest(CStudioSearch.searchContext);
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
				"<div id='result-status" + contentTO.resultId + "' style='float: left; margin-left: 32px;'></div>" +
				"<div>"+
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
 * called right before firing the search request
 * used to get basic values in to search context and clean up previous searches
 */
CStudioSearch.preFireSearch = function(searchContext) {
	CStudioSearch.toggleResultDetail("hide-link");
	
	var resultHeaderInProgress = YDom.get("cstudio-wcm-search-result");
	var contextPath = document.location.protocol + "//" + document.location.hostname + ":" + document.location.port + CStudioAuthoringContext.baseUri+"/";  
	var imgEl = document.createElement("img");
	imgEl.src = contextPath + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";
	resultHeaderInProgress.appendChild(imgEl);
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
				results = results.item.children;
			 
				CStudioSearch.toggleResultDetail(CStudioSearch.DETAIL_OPEN);
				var resultTableEl = document.getElementById('cstudio-wcm-search-result');
				resultTableEl.innerHTML = "";
				var searchResultsLabels = [];
				if(resultTableEl) {
			       var previousResultIds = [];
			        var exists = function(elementId) {
			        	for(var i = 0;i < previousResultIds.length; i++) {
			        		if(elementId == previousResultIds[i]) return true;
			        	}
			        	return false;
			        }
			        
					var fileResultCount = 0;
					
					/* render results */
			        for(var i=0; i<results.length; i++) {
			   			var resultsHTML = "";
			        	var contentItem = results[i],
			        	
                        _item = contentItem;
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
				     	if(!contentItem.container || contentItem.name.indexOf(".xml") != -1) {
							fileResultCount++;
				        	var resultEl = document.createElement("div");
				        	resultTableEl.appendChild(resultEl);
				        	
				        	var resultTemplate = CStudioSearch.resultRenderers[_item.contentType];
				      
				        	if(resultTemplate) {
				        	 	resultsHTML = resultTemplate.render(contentItem);
				        	}
				        	else {
				        		var extension = _item.name.substring(_item.name.lastIndexOf(".")+1);
				        		resultTemplate = CStudioSearch.resultRenderers[extension];

                                if(resultTemplate && (typeof resultTemplate !== "function")) {
					        	 	resultsHTML = resultTemplate.render({ item: contentItem, resultId: contentItem.resultId });			        			
				        		}
				        		else {
					        	 	resultsHTML = CStudioSearch.ResultRenderer.Default.render(
					        	 		{ item: contentItem, resultId: contentItem.resultId });
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
	                        YDom.addClass(resultStatusEl, "result-item");

							//Add Tool tip information for item
							var searReultItem = (resultStatusEl) ? resultStatusEl.nextSibling: undefined;
							
//							if (searReultItem) {
//								var itemTitle = CStudioAuthoring.Utils.getTooltipContent(contentItem);
//								var oSpan = document.createElement("div");
//								oSpan.setAttribute("id", "search-item-tt-" + contentItem.resultId);
//								oSpan.setAttribute("title", itemTitle);
//								searReultItem.parentNode.insertBefore(oSpan, searReultItem);
//								oSpan.appendChild(resultStatusEl);
//								oSpan.appendChild(searReultItem);
//								searchResultsLabels.push("search-item-tt-" + contentItem.resultId);
//							}

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
								      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO);
					      					}
					      					else {
					      						this.checked = false;
					      					}
					      				}
					      				else {
							      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO);
					      				}
					      			}
					      			else {
					      				CStudioAuthoring.SelectedContent.unselectContent(this.contentTO);
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
						      			CStudioAuthoring.SelectedContent.unselectContent(CStudioSearch.selectedContentTO);
					      			}
					      			
					      			CStudioSearch.selectedContentTO = this.contentTO;
					      			CStudioAuthoring.SelectedContent.selectContent(this.contentTO);
					      			
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
			        }


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
				
				if(fileResultCount==0) {
				YDom.get('cstudio-wcm-search-result').innerHTML =  "<p align='center'><strong>There are no files at this path.</i></p>";
				} 
			},
	        failure: function(o) {
			    YDom.get('cstudio-wcm-search-result').innerHTML =  "<p align='center'><strong>Unable to Retrieve Search Result. Please Try again.</strong><br><br><br><i>If this is consistent, please contact System Administrator.</i></p>";
			},
			        
			searchContext: searchContext		
	};

	// perform search
	CStudioAuthoring.Service.lookupSiteContent(CStudioAuthoringContext.site, searchContext.path, 1, "default", callback);
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