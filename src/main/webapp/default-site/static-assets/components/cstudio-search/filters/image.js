/**
 * Image Search
 */
CStudioSearch.FilterRenderer.Image = function() {

	CStudioSearch.FilterRenderer.Image.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Image Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("image/*"),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["image"] = new CStudioSearch.FilterRenderer.Image();
