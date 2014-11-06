/**
 * WCM Search Plugin
 */
CStudioAuthoring.ContextualNav.WcmSearchMod = CStudioAuthoring.ContextualNav.WcmSearchMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		var el = YDom.get("acn-search");
		el.innerHTML = '<span class="acn-search-container">' +
	                     '<input type="text" class="search-input" id="acn-searchtext" value="" maxlength="256" />' +
	  				   '</span>';


		this.definePlugin();
		CStudioAuthoring.ContextualNav.WcmSearch.init();
	},
	
	definePlugin: function() {
		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event;
		/**
		 * WCM Search Contextual Nav Widget
		 */
		CStudioAuthoring.register({
			"ContextualNav.WcmSearch": {
				init: function() { 
					var e = YDom.get("acn-searchtext");
					YAHOO.util.Event.addListener(e, "focus", this.focusSearchText);
					YAHOO.util.Event.addListener(e, "blur", this.blurSearchText);
					new YAHOO.util.KeyListener(e,{keys: 13},{fn:this.doSearch},"keydown").enable();
					this.setDefaultSearchText();
					this.blurSearchText();
				},
				/**
				 * user has focused on search text box
				 */
				focusSearchText: function(e) {
					var e = YDom.get("acn-searchtext");
					YDom.setStyle(e, "color", "");
					e.value="";
					e.select();
				},
				/**
				 * handle on blur event
				 */
				blurSearchText: function(e) {
					var e = YDom.get("acn-searchtext");
					var searchVal = e.value;
					YDom.setStyle(e, "color", "#999999");
					CStudioAuthoring.ContextualNav.WcmSearch.setDefaultSearchText();
				},
				/**
				 * set the search box to it's default search text value
				 */
				setDefaultSearchText: function() {
					var CMgs = CStudioAuthoring.Messages;
        			var contextNavLangBundle = CMgs.getBundle("contextnav", CStudioAuthoringContext.lang);

					YDom.get("acn-searchtext").value=CMgs.format(contextNavLangBundle, "search");
				},
				/**
				 * perform the search
				 */
				doSearch: function() {
					var searchContext = CStudioAuthoring.Service.createSearchContext();
					searchContext.keywords = encodeURIComponent(YDom.get("acn-searchtext").value);
					searchContext.includeAspects = new Array();
					searchContext.includeAspects.push("cstudio-core:pageMetadata");
					searchContext.includeAspects.push("cstudio-core:documentMetadata");
					CStudioAuthoring.Operations.openSearch("default", searchContext, -1, "act", false, null);
				}
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("search", CStudioAuthoring.ContextualNav.WcmSearchMod);
