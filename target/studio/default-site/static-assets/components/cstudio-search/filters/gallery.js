/**
 * Gallery Search
 */
CStudioSearch.FilterRenderer.Gallery = function() {

	CStudioSearch.FilterRenderer.Gallery.prototype = new CStudioSearch.FilterRenderer.Common();

	var filter = new CStudioSearch.FilterRenderer.Common();

	filter.self = filter;
	filter.title = "Gallery Image Search";
	filter.contentTypes = [];
	filter.includeAspects = [];
	filter.excludeAspects = [];
	filter.keywords = " ";
	filter.filterCols = [
				    // new CStudioSearch.FilterRenderer.Common.GalleryTypeCol("image/*"),
				    new CStudioSearch.FilterRenderer.Common.MimeTypeReadOnlyCol("image/*"),
			    ];

	return filter;
}

// register this filter renderer
CStudioSearch.filterRenderers["gallery"] = new CStudioSearch.FilterRenderer.Gallery();