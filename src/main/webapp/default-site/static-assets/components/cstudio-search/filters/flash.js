/**
 * Flash Search
 */
CStudioSearch.FilterRenderer.Flash = function() {

	CStudioSearch.FilterRenderer.Flash.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Flash Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
					//new CStudioSearch.FilterRenderer.Common.PathSelect("/static-assets/flash","/static-assets/flash"),
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("application/x-shockwave-flash"),
                    ];                                                         
                                                                            
	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["flash"] = new CStudioSearch.FilterRenderer.Flash();
CStudioAuthoring.Module.moduleLoaded("search-filter-flash", CStudioSearch.filterRenderers["flash"]);

