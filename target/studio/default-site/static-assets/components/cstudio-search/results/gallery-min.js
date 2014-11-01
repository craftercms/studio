CStudioSearch.ResultRenderer.Gallery={render:function(g){var f="";
var e=g.item.uri;
f="<img src='"+CStudioAuthoringContext.baseUri+"/proxy/alfresco/cstudio/wcm/content/get-content?site="+CStudioAuthoringContext.site+"&path="+e+"&edit=false&createFolders=false' alt='"+g.item.internalName+"' class='cstudio-search-banner-image' onload='CStudioSearch.ResultRenderer.Gallery.hideMagnifyIcon(this);'/> <img src='"+CStudioAuthoringContext.baseUri+"/themes/cstudioTheme/images/magnify.jpg' style='margin-left:-20px;'onclick ='CStudioSearch.magnifyBannerImage(\""+CStudioAuthoringContext.baseUri+"/proxy/alfresco/cstudio/wcm/content/get-content?site="+CStudioAuthoringContext.site+"&path="+e+"&edit=false&createFolders=false\");' />";
var b=g.item.uri;
var a=true;
var d="";
if(b!="null"){var c=(a=="null"||a=="false")?"_self":"_blank";
d="<div class='cstudio-search-description'><a href='"+CStudioAuthoringContext.previewAppBaseUri+b+"' target='"+c+"'>"+b+"</a></div>"
}return CStudioSearch.renderCommonResultWrapper(g,"<span class='cstudio-search-component-title-nopreview cstudio-gallery-component-title-nopreview'>"+g.item.internalName+(g.item.newFile?"*":"")+"</span><span class='cstudio-search-download-additional' style='margin-left:0px;'> | Image</span><span class='cstudio-search-download-additional' style='margin-left:0px;'> | <a href='javascript:CStudioSearch.viewItemDetails(\""+g.item.internalName+'","'+g.item.title+'","'+g.item.uri+"\");'>View Details</a></span><div class='cstudio-search-description'><div class='cstudio-search-description'>"+f+"</div>")
},_self:this,hideMagnifyIcon:function(b,c){if(b.height<=150){b.nextElementSibling.style.display="none";
if(c){b.previousSibling.previousSibling.style.display="none";
b.previousSibling.previousSibling.previousSibling.firstChild.height=b.height;
b.previousSibling.previousSibling.previousSibling.firstChild.width=b.width
}}else{var d=b.height;
var a=b.width;
b.height=150;
if(c){b.nextSibling.nextSibling.value=d+"|"+a;
b.previousSibling.previousSibling.previousSibling.firstChild.height=150;
b.previousSibling.previousSibling.previousSibling.firstChild.width=b.width
}}if(b.width>280){b.width=280
}}};
CStudioSearch.resultRenderers.jpg=CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers.gif=CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers.png=CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers.ico=CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers.jpeg=CStudioSearch.ResultRenderer.Gallery;