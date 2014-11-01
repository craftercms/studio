/**
 * Override the OTTB default
 * Default Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.Default = function() {

	CStudioSearch.FilterRenderer.Default.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "WCM Browse";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [ new CStudioSearch.FilterRenderer.Common.PathSelect("") ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["default"] = new CStudioSearch.FilterRenderer.Default();
