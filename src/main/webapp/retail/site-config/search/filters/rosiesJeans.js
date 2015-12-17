/**
 * Rosie Rivet Jean Search
 */
CStudioSearch.FilterRenderer.Rosies = CStudioSearch.FilterRenderer.Rosies || {};

CStudioSearch.FilterRenderer.Rosies.Jeans = function() {

	CStudioSearch.FilterRenderer.Rosies.Jeans.prototype = new CStudioSearch.FilterRenderer.Common();
	var queryString = document.location.search;
	var path = CStudioAuthoring.Utils.getQueryVariable(queryString, "path");

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Jeans Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [  new CStudioSearch.FilterRenderer.Common.PathReadOnlyCol(path), ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["rosiesJeans"] = new CStudioSearch.FilterRenderer.Rosies.Jeans();
CStudioAuthoring.Module.moduleLoaded("rosiesJeans", CStudioSearch.filterRenderers["rosiesJeans"]);