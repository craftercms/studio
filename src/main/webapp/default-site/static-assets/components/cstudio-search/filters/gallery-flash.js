/**
 * Gallery Flash Search
 */
CStudioSearch.FilterRenderer.GalleryFlash = function() {

	CStudioSearch.FilterRenderer.GalleryFlash.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Gallery Flash Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("application/x-shockwave-flash"),
                    ];                                                         
                                                                            
	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["gallery-flash"] = new CStudioSearch.FilterRenderer.GalleryFlash();
CStudioAuthoring.Module.moduleLoaded("search-filter-gallery-flash", CStudioSearch.filterRenderers["gallery-flash"]);
