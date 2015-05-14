CStudioSearch.ResultRenderer.Gallery = {
		render: function(contentTO) {
			var mediaTag = "";
			var path = contentTO.item.uri;
			
			// TODO: Fix Styles for maginify icon if image width > 300
			mediaTag = "<img src='"+CStudioAuthoringContext.baseUri+"/proxy/alfresco/cstudio/wcm/content/get-content?site="+ 
				CStudioAuthoringContext.site+"&path="+path +
				"&edit=false&createFolders=false' alt='"+contentTO.item.internalName+"' "+
				"class='cstudio-search-banner-image' onload='CStudioSearch.ResultRenderer.Gallery.hideMagnifyIcon(this);'/> " +
				"<img src='"+CStudioAuthoringContext.baseUri+"/themes/cstudioTheme/images/magnify.jpg' style='margin-left:-20px;'" +
				"onclick ='CStudioSearch.magnifyBannerImage(\"" +
				CStudioAuthoringContext.baseUri+"/proxy/alfresco/cstudio/wcm/content/get-content?site=" +
				CStudioAuthoringContext.site+"&path="+path +
				"&edit=false&createFolders=false" +
				"\");' />";

			var url = contentTO.item.uri;
			var urlNewWindow =  true;
			var urlLinkElement = "";

			if(url != "null") {
				var targetWindow = (urlNewWindow == "null" || urlNewWindow == "false") ? "_self" : "_blank";			
				urlLinkElement = "<div class='cstudio-search-description'><a href='" + CStudioAuthoringContext.previewAppBaseUri + url + "' target='" +targetWindow+ "'>" + url + "</a></div>";	
			}

			return CStudioSearch.renderCommonResultWrapper(contentTO,  
				"<span class='cstudio-search-component-title-nopreview cstudio-gallery-component-title-nopreview'>"+
				contentTO.item.internalName+
				(contentTO.item.newFile?"*":"")+"</span>" +
				"<span class='cstudio-search-download-additional' style='margin-left:0px;'>"+
				" | Image" + 
				"</span>" + 
				"<span class='cstudio-search-download-additional' style='margin-left:0px;'>"+
				" | <a href='javascript:CStudioSearch.viewItemDetails(\"" + 
				contentTO.item.internalName + "\",\"" + contentTO.item.title + "\",\"" + contentTO.item.uri + 
				"\");'>View Details</a>" + 
				"</span>" + 
				"<div class='cstudio-search-description'>"+
				"<div class='cstudio-search-description'>"+mediaTag+"</div>"
				);
		}, 
		
		_self: this,
		
		hideMagnifyIcon : function(img,isFlash) {
			if( img.height <= 150 ){
				img.nextElementSibling.style.display = "none";
				if(isFlash){
					img.previousSibling.previousSibling.style.display = "none";
					img.previousSibling.previousSibling.previousSibling.firstChild.height = img.height;
					img.previousSibling.previousSibling.previousSibling.firstChild.width = img.width;
				}
				
			} else {
				var oldHeight = img.height;
				var oldWidth = img.width;
				img.height = 150;
				if(isFlash){
					img.nextSibling.nextSibling.value = oldHeight+"|"+oldWidth;
					img.previousSibling.previousSibling.previousSibling.firstChild.height = 150;
					img.previousSibling.previousSibling.previousSibling.firstChild.width = img.width;
				}			
			}
			
			if (img.width > 280) {
				img.width = 280;
			}			
		}
	};

// register renderer
CStudioSearch.resultRenderers["jpg"] = CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers["gif"] = CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers["png"] = CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers["ico"] = CStudioSearch.ResultRenderer.Gallery;
CStudioSearch.resultRenderers["jpeg"] = CStudioSearch.ResultRenderer.Gallery;
CStudioAuthoring.Module.moduleLoaded("search-result-gallery", CStudioSearch.resultRenderers["jpg"]);