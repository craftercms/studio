CStudioAuthoring.Utils.addJavascript("/static-assets/yui/datasource/datasource-min.js");
CStudioAuthoring.Utils.addJavascript("/static-assets/yui/autocomplete/autocomplete-min.js");

CStudioForms.Datasources.ImgFlickrUpload = CStudioForms.Datasources.ImgFlickrUpload ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.ImgFlickrUpload, CStudioForms.CStudioFormDatasource, {

    getLabel: function() {
        return CMgs.format(langBundle, "DAMImage");
    },
    
	/**
	 * action called when user clicks insert image
	 */
	insertImageAction: function(insertCb) {
		this._self = this;
		var site = CStudioAuthoringContext.site;
		var path = "/static-assets/images";
		var isUploadOverwrite = true;

	    for(var i=0; i<this.properties.length; i++) {
			if(this.properties[i].name == "repoPath") {
				path = this.properties[i].value;
			
				path = this.processPathsForMacros(path);
			}
		}

		var callback = { 
			success: function(imageData) {
//				var url = this.context.createPreviewUrl('/static-assets/images/' + imageData.fileName);
//				imageData.previewUrl = url
//				imageData.relativeUrl = '/static-assets/images/' + imageData.fileName
				imageData.previewUrl = imageData.relativeUrl;
				insertCb.success(imageData);
			}, 
			failure: function() {
				insertCb.failure("An error occurred while uploading the image."); 
			},
			context: this 
		};
	
		var openFlickrDialogCb = {
			moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
				dialogClass.showDialog(moduleConfig.site, moduleConfig.path, moduleConfig.callback, moduleConfig.isUploadOverwrite);	
			}
		};
		
		var moduleConfig = {
			path: path,
			site: site,
			callback: callback,
			isUploadOverwrite: isUploadOverwrite
		}
		
		CStudioAuthoring.Module.requireModule("flickr-dialog", "/static-assets/components/cstudio-dialogs/upload-flickr-dialog.js", moduleConfig, openFlickrDialogCb);

	},
	
	/**
	 * create preview URL
	 */
	createPreviewUrl: function(imagePath) {
		return CStudioAuthoringContext.previewAppBaseUri + imagePath;
	},
	
	/**
	 * clean up preview URL so that URL is canonical
	 */
	cleanPreviewUrl: function(previewUrl) {
		var url = previewUrl;
		
		if(previewUrl.indexOf(CStudioAuthoringContext.previewAppBaseUri) != -1) {
			url =  previewUrl.substring(CStudioAuthoringContext.previewAppBaseUri.length);
		}
		
		return url;	
	},

	deleteImage : function(path) {

//		var deleteUrl = CStudioAuthoringContext.authoringAppBaseUri 
//				+ "/proxy/alfresco/cstudio/wcm/content/delete-content"
//				+ "?site=" + CStudioAuthoringContext.site
//				+ "&path=" + path;
//		var callback = {
//			success : function(response) {
//				//alert("success");
//			},
//			failure : function(response) {
//				alert("Failed to delete");
//			}
//		};
//		
//		try {
//            var url = deleteUrl;
//    		if(url.indexOf("?") != -1) {
//    			url += "&draft=true";
//    		} else {
//    			url += "?draft=true";
//    		}
//			YAHOO.util.Connect.asyncRequest('GET', url, callback);
//		} catch (error) {
//			alert("failed get request," + error);
//		}
	},

   	getInterface: function() {
   		return "image";
   	},

	getName: function() {
		return "img-flickr-upload";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: CMgs.format(langBundle, "repositoryPath"), name: "repoPath", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
			{ label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" },
		];
	}

});


CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-img-flickr-upload", CStudioForms.Datasources.ImgFlickrUpload);