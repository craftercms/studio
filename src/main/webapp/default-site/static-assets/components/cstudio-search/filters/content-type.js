/**
 * Override the OTTB default
 * Content Type Search Filter, searches for the items with given content-types
 */
CStudioSearch.FilterRenderer.ContentType = function() {

	CStudioSearch.FilterRenderer.ContentType.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Content Type Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.ContentTypeCol(),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["content-type"] = new CStudioSearch.FilterRenderer.ContentType();
CStudioAuthoring.Module.moduleLoaded("search-filter-content-type", CStudioSearch.filterRenderers["content-type"]);