CStudioSearch.ResultRenderer.Default={render:function(e){var b=CStudioAuthoringContext.liveAppBaseUri+e.item.browserUri;
if(!CStudioAuthoring.Utils.isEmpty(e.item.contentType)){var c=e.item.contentType
}var d="CStudioAuthoring.Service.lookupContentItem('"+CStudioAuthoringContext.site+"', '"+e.item.uri+"', { success:function(to) { CStudioAuthoring.Operations.openPreview(to.item, '"+window.id+"', false, false); }, failure: function() {} }, false); return false;";
var a="<a href='#' onclick=\""+d+"\" class='"+((e.item&&e.item.component)?"cstudio-search-no-preview":"cstudio-search-download-link")+"'>"+e.item.internalName+(e.item.newFile?"*":"")+"</a><span class='cstudio-search-download-link-additional'> | "+CStudioSearch.getContentTypeName(e.item.contentType)+"</span><br /><div class='cstudio-search-result-detail'><span class='cstudio-search-description'>"+e.item.metaDescription+"</span>";
if(e.item.previewable&&e.item.previewable==true){a+="<div>"+b+"</div>"
}a+="</div><span class='cstudio-search-download-additional'>Edited "+CStudioAuthoring.Utils.formatDateFromString(e.item.eventDate,"simpleformat")+" by "+CStudioAuthoring.Utils.getAuthorFullNameFromContentTOItem(e.item)+"</span>";
return CStudioSearch.renderCommonResultWrapper(e,a)
}};
CStudioSearch.resultRenderers["default"]=CStudioSearch.ResultRenderer.Default;