/**
 * Override the OTTB default
 * Default Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.CSS = function() {

	CStudioSearch.FilterRenderer.CSS.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "CSS Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("text/css"),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["css"] = new CStudioSearch.FilterRenderer.CSS();
