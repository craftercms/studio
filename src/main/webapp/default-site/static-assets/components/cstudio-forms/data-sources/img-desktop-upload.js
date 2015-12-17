CStudioForms.Datasources.ImgDesktopUpload = CStudioForms.Datasources.ImgDesktopUpload ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.ImgDesktopUpload, CStudioForms.CStudioFormDatasource, {

    getLabel: function() {
        return CMgs.format(langBundle, "imageUploadedDesktop");
    },
    
	/**
	 * action called when user clicks insert image
	 */
	insertImageAction: function(insertCb) {
		this._self = this;
		var site = CStudioAuthoringContext.site;
		var path = "/static-assets/images"; // default
		var isUploadOverwrite = true;
		
		for(var i=0; i<this.properties.length; i++) {
			if(this.properties[i].name == "repoPath") {
				path = this.properties[i].value;
			
				path = this.processPathsForMacros(path);
			}
		}
		
		var callback = { 
			success: function(imageData) {
				var url = this.context.createPreviewUrl(path + "/" + imageData.fileName);
				imageData.previewUrl = url + "?" + new Date().getTime();
				imageData.relativeUrl = path + "/" + imageData.fileName
				insertCb.success(imageData);
			}, 
			failure: function() {
				insertCb.failure("An error occurred while uploading the image."); 
			},
			context: this 
		};
	
		CStudioAuthoring.Operations.uploadAsset(site, path, isUploadOverwrite, callback);
	},
	
	/**
	 * create preview URL
	 */
	createPreviewUrl: function(imagePath) {
		return CStudioAuthoringContext.previewAppBaseUri + imagePath + "";
	},
	
	/**
	 * clean up preview URL so that URL is canonical
	 */
	cleanPreviewUrl: function(previewUrl) {
		var url = previewUrl;
		
		if(previewUrl.indexOf(CStudioAuthoringContext.previewAppBaseUri) != -1) {
			url =  previewUrl.substring(CStudioAuthoringContext.previewAppBaseUri.length);
			
			if(url.substring(0,1) != "/") {
				url = "/" + url;
			}
		}
		
		return url;	
	},

	deleteImage : function(path) {

	},

   	getInterface: function() {
   		return "image";
   	},

	getName: function() {
		return "img-desktop-upload";
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


CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-img-desktop-upload", CStudioForms.Datasources.ImgDesktopUpload);