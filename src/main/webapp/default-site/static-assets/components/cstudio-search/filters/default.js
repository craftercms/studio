/**
 * Override the OTTB default
 * Default Search Filter, includes all options
 */
CStudioSearch.FilterRenderer.Default = function() {

	CStudioSearch.FilterRenderer.Default.prototype = new CStudioSearch.FilterRenderer.Common();

    var CMgs = CStudioAuthoring.Messages;
    var langBundle = CMgs.getBundle("search", CStudioAuthoringContext.lang);
    var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = CMgs.format(langBundle, "defaultTitle");
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [ ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["default"] = new CStudioSearch.FilterRenderer.Default();
CStudioAuthoring.Module.moduleLoaded("search-filter-default", CStudioSearch.filterRenderers["default"]);
