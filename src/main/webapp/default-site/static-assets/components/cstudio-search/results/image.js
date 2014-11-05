CStudioSearch.ResultRenderer.Banner = {
	render: function(contentTO) {
		var mediaTag = "";
		var path = contentTO.item.uri;
		
				mediaTag = "<img src='"+CStudioAuthoringContext.baseUri+"/proxy/alfresco/cstudio/wcm/content/get-content?site="+ 
					CStudioAuthoringContext.site+"&path="+path +
					"&edit=false&createFolders=false' alt='"+contentTO.item.internalName+"' "+
					"class='cstudio-search-banner-image' onload='CStudioSearch.ResultRenderer.Banner.hideMagnifyIcon(this);'/> " +
					"<img src='"+CStudioAuthoringContext.baseUri+"/static-assets/themes/cstudioTheme/images/magnify.jpg' style='margin-left:-20px;'" +
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
			"<span class='cstudio-search-component-title-nopreview'>"+
			contentTO.item.internalName+
			(contentTO.item.newFile?"*":"")+"</span>" +
			"<span class='cstudio-search-download-additional' style='margin-left:0px;'>"+
			" | Image" + 
			"</span>" + 
			"<div class='cstudio-search-description'>"+
			urlLinkElement +
			"<div>"+
				"<span class='cstudio-search-download-additional'>"+
					"Created: " + CStudioAuthoring.Utils.formatDateFromStringNullToEmpty(contentTO.item.eventDate, "simpleformat")+			
				"</span><br />"+
				"<span class='cstudio-search-download-additional'>Edited "+ 
					CStudioAuthoring.Utils.formatDateFromString(contentTO.item.eventDate, "simpleformat") + " by " + 
					CStudioAuthoring.Utils.getAuthorFullNameFromContentTOItem(contentTO.item) +
				"</span>"+
			"</div>" +			
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
	}
};

// register renderer
CStudioSearch.resultRenderers["jpg"] = CStudioSearch.ResultRenderer.Banner;
CStudioSearch.resultRenderers["gif"] = CStudioSearch.ResultRenderer.Banner;
CStudioSearch.resultRenderers["png"] = CStudioSearch.ResultRenderer.Banner;
CStudioSearch.resultRenderers["ico"] = CStudioSearch.ResultRenderer.Banner;
CStudioSearch.resultRenderers["jpeg"] = CStudioSearch.ResultRenderer.Banner;
