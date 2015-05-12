/**
 * Override the OTTB default
 * Default Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.JS = function() {

	CStudioSearch.FilterRenderer.JS.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "JavaScript Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("application/x-javascript"),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["js"] = new CStudioSearch.FilterRenderer.JS();
CStudioAuthoring.Module.moduleLoaded("search-filter-javascript", CStudioSearch.filterRenderers["js"]);

