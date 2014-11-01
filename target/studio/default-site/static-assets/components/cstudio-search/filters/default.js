/**
 * Override the OTTB default
 * Default Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.Default = function() {

	CStudioSearch.FilterRenderer.Default.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "WCM Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [ ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["default"] = new CStudioSearch.FilterRenderer.Default();
