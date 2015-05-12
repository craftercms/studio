/**
 * Override the OTTB default
 * View Template Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.XHTML = function() {

	CStudioSearch.FilterRenderer.XHTML.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "View Template Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("application/xhtml+xml"),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["xhtml"] = new CStudioSearch.FilterRenderer.XHTML();
CStudioAuthoring.Module.moduleLoaded("search-filter-xhtml", CStudioSearch.filterRenderers["xhtml"]);

