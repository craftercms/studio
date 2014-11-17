
/**
 * Most of CStudio's Filters are similar, this class encapsulates that simularity and
 * allows us to subclass it for specializaitons rather than duplicating code
 */
CStudioSearch.FilterRenderer.Common = function() { 
	var object = {
		title: "TITLE NOT SET",
		appliesToContentTypes: [ "" ],
		filterCols: [ ],

		/**
		 * return the title for the filter
		 */		
		getFilterTitle: function() {
			return this.self.title;
		},

		/*
		 * returns an array of content types to be target
		 */
		getAppliesToContentTypes: function() {
			return this.self.appliesToContentTypes;
		},

		/**
		 * returns an array of sort option objects
		 */
		getSortOptions: function() {
			// sort order will be changed, *only* when content type is *only* news-item
			if (this.contentTypes && (this.contentTypes.length > 0)) {
				return [ { "label" : CMgs.format(langBundle, "sortRelevance"), "value" : "relevance" },
						{ "label" : CMgs.format(langBundle, "sortAlphabetical"), "value" : "cstudio-core:internalName,cstudio-core:title" },
					];
			}
			
			return [ { "label" : CMgs.format(langBundle, "sortRelevance"), "value" : "relevance" },
					 { "label" : CMgs.format(langBundle, "sortAlphabetical"), "value" : "cstudio-core:internalName,cstudio-core:title" },
					 { "label" : CMgs.format(langBundle, "sortCreateDate"), "value": "cm:created"}
				   ];
		},

		/**
		 * render the filter body
		 * @returns HTML
		 */
		renderFilterBody: function(filterControlsEl, renderCb) {
		
			var filters = this.self.filterCols;
			
			var filterBodyRenderCb = {
				success: function() {
					this.callbackRecieved++;

					if(this.callbackRecieved >= this.filters.length) {
						this.renderCb.success();
					}
				},
				
				failure: function() {
				},
				
				renderCb: renderCb,
				filters: filters,
				callbackRecieved: 0
			}
			
			if(filters.length && filters.length > 0) {
				for(var i=0; i<filters.length; i++) {
					filters[i].render(filterControlsEl, filterBodyRenderCb);
				}
			}
			else {
				filterBodyRenderCb.success();
			}
		},

		/**
	 	 * populate search context with filter values from url
	 	 * returns context
	  	 */
		prepareSearchFilterFromUrl: function() {
			var queryString = document.location.search;

			var filters = this.self.filterCols;
			
			for(var i=0; i<filters.length; i++) {
				filters[i].prepareColFromUrl(queryString);
			}
   		},

		/**
		 * populate search context with filter values
		 * returns context
		 */
		augmentContextWithActiveFilter: function(searchContext) {

			if(searchContext.contentTypes == null){
			searchContext.contentTypes = this.self.contentTypes;
			}
			searchContext.includeAspects = this.self.includeAspects;
			searchContext.excludeAspects = this.self.excludeAspects;
			
			var filters = this.self.filterCols;
			
			for(var i=0; i<filters.length; i++) {
				searchContext = filters[i].augmentContextWithCol(searchContext);
			}

			return searchContext;
		}
		
	};
	
	object.self = object;
	return object;
}




/** 
 * common base for all colums
 */
CStudioSearch.FilterRenderer.Common.BaseCol = function() {
	return {

		/** 
		 * render col, base does nothing 
		 */
		render: function() {
		},
		
		/**
		 * translate url parms in to set col values
		 */
		prepareColFromUrl: function(queryString) {
		},
		
		/**
		 * add filters etc for the given column
		 */
		augmentContextWithCol: function(searchContext) {
			return searchContext;
		},
		
		/**
		 * create a column for controls in the filter
		 */
		createFilterColEl: function(label) {
			var colEl = document.createElement("div");
			YDom.addClass(colEl, "cstudio-wcm-searchfilter-col");
			
			var colLabelEl = document.createElement("div");
			if (label && label.trim() != "") 
				colLabelEl.innerHTML = label + ": ";
			else 
				colLabelEl.innerHTML = "&nbsp;";
			
			
			colEl.appendChild(colLabelEl);
			
			return colEl;
		},

		/**
		 * create a checkbox 
		 */
		createCheckboxEl: function(label, family, id, defaultChecked) {
			var containerEl  = document.createElement("span");
			var labelEl = document.createElement("span");
			YDom.addClass(labelEl, "cstudio-wcm-searchfilter-chkbox-label");

			var checkboxEl = document.createElement("input");
			checkboxEl.type = "checkbox";
			checkboxEl.id = id;
			checkboxEl.checked = defaultChecked;
			checkboxEl.name = family;
			
			labelEl.innerHTML = label;
			
			containerEl.inputEl = checkboxEl;
			containerEl.appendChild(checkboxEl);
			containerEl.appendChild(labelEl);
			
			return containerEl;
		}

	};
}

CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol = function(defaultMimeType) { 

	var column = new CStudioSearch.FilterRenderer.Common.BaseCol();
	
	column.self = column;
	
	/**
	 * render filter
	 */
	column.render = function(filtersContainerEl, renderCb) {
		var mimeTypeColEl = this.self.createFilterColEl("File Type");

		filtersContainerEl.appendChild(mimeTypeColEl);

		var buildLanguageFilterCallback = {
			success: function(controlEl) {
				this.containerEl.appendChild(controlEl);
				this.parent.mimeTypeEl = controlEl;
				this.renderCb.success();
			},
			
			failure: function() {
			},
			
			parent: this.self,
	
			containerEl: mimeTypeColEl,
			
			renderCb: renderCb
		}

		this.self._renderMimeTypeSpan(buildLanguageFilterCallback);	
	};

	/**
	 * prepare from url 
	 */
	column.prepareColFromUrl = function(queryString) {

		var value = CStudioAuthoring.Utils.getQueryVariable(queryString, "cm:content.mimetype");
	
		if((!value && ! defaultMimeType) || value == "all") {
			this.self.mimeTypeEl.innerHTML = "All";
		} else if (!value) {
			this.self.mimeTypeEl.innerHTML = defaultMimeType;
		} else {
			this.self.mimeTypeEl.innerHTML = new String(value);
		}
	}
	
	/**
	 * add filters etc for the given column
	 */
	column.augmentContextWithCol = function(searchContext) {
		
		var value = this.self.mimeTypeEl.innerHTML;
		
		if (value != null && value != "All") {
			searchContext.filters.push({ qname: "cm:content.mimetype",  value: value });
		}
		
		return searchContext;
	}

	/**
	 * build the language drop down
	 */
	column._renderMimeTypeSpan = function(buildCallback) {
		var spanEl = document.createElement("span");
		spanEl.innerHTML = "";
		buildCallback.success(spanEl);
	};
	
	return column;
}



/**Content-type filter start**/
CStudioSearch.FilterRenderer.Common.ContentTypeCol = function() {

	var column = new CStudioSearch.FilterRenderer.Common.BaseCol();

	column.self = column;

	/**
	 * render filter
	 */
	column.render = function(filtersContainerEl, renderCb) {
		var contentTypeColEl = this.self.createFilterColEl("Content Type");

		filtersContainerEl.appendChild(contentTypeColEl);

		var buildLanguageFilterCallback = {
			success: function(controlEl) {
				this.containerEl.appendChild(controlEl);
				this.parent.contentTypeEl = controlEl;
				this.renderCb.success();
			},

			failure: function() {
			},

			parent: this.self,

			containerEl: contentTypeColEl,

			renderCb: renderCb
		}

		this.self._renderContentTypeSpan(buildLanguageFilterCallback);
	};

	/**
	 * prepare from url
	 */
	column.prepareColFromUrl = function(queryString) {

		var value = CStudioAuthoring.Utils.getQueryVariable(queryString, "cstudio-core:contentType");

		if((!value) || value == "all") {
			this.self.contentTypeEl.innerHTML = "All";
		} else {
			this.self.contentTypeEl.innerHTML = new String(value);
		}
	}

	/**
	 * add filters etc for the given column
	 */
	column.augmentContextWithCol = function(searchContext) {

		var value = this.self.contentTypeEl.innerHTML;

		if (value != null && value != "All") {
			searchContext.filters.push({ qname: "cstudio-core:contentType",  value: value, useWildCard: true });
		}

		return searchContext;
	}

	/**
	 * build the language drop down
	 */
	column._renderContentTypeSpan = function(buildCallback) {
		var spanEl = document.createElement("span");
		spanEl.innerHTML = "";
		buildCallback.success(spanEl);
	};

	return column;
}/**Content-type filter end**/

CStudioSearch.FilterRenderer.Common.PathReadOnlyCol = function(defaultPath) {

	var column = new CStudioSearch.FilterRenderer.Common.BaseCol();
	column.self = column;
	
	/**
	 * render filter
	 */
	column.render = function(filtersContainerEl, renderCb) {
		var pathColEl = this.self.createFilterColEl("Path");

		filtersContainerEl.appendChild(pathColEl);

		var buildLanguageFilterCallback = {
			success: function(controlEl) {
				this.containerEl.appendChild(controlEl);
				this.parent.pathEl = controlEl;
				this.renderCb.success();
			},
			
			failure: function() {
			},
			
			parent: this.self,
	
			containerEl: pathColEl,
			
			renderCb: renderCb
		}

		this.self._renderPathSpan(buildLanguageFilterCallback);	
	};

	/**
	 * prepare from url 
	 */
	column.prepareColFromUrl = function(queryString) {

		var value = CStudioAuthoring.Utils.getQueryVariable(queryString, "path");
	
		if((!value && !defaultPath) || value == "any") {
			this.self.pathEl.innerHTML = "Any";
		} else if (!value) {
			this.self.pathEl.innerHTML = defaultPath;
		} else {
			this.self.pathEl.innerHTML = new String(value);
		}
	}
	
	/**
	 * add filters etc for the given column
	 */
	column.augmentContextWithCol = function(searchContext) {
		
		var value = this.self.pathEl.innerHTML;
		
		if (value != null && value != "Any") {
			searchContext.filters.push({ qname: "PATH",  value: value });
		}
		
		return searchContext;
	}

	/**
	 * build the language drop down
	 */
	column._renderPathSpan = function(buildCallback) {
		var spanEl = document.createElement("span");
		spanEl.innerHTML = "";
		buildCallback.success(spanEl);
	};
	
	return column;
}
