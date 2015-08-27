CStudioForms.Datasources.ImgRepoUpload = CStudioForms.Datasources.ImgRepoUpload ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;

   	for(var i=0; i<properties.length; i++) {
   		if(properties[i].name == "repoPath") {
 			this.repoPath = properties[i].value;
   		}
   	} 
   		
	return this;
}

YAHOO.extend(CStudioForms.Datasources.ImgRepoUpload, CStudioForms.CStudioFormDatasource, {

    getLabel: function() {
        return CMgs.format(langBundle, "imageFromRepository");
    },
    
	/**
	 * action called when user clicks insert image
	 */
	insertImageAction: function(insertCb) {
		var _self = this;

		CStudioAuthoring.Operations.openBrowse("", _self.repoPath, "1", "select", true, { 
			success: function(searchId, selectedTOs) {
				var imageData = {};
				var path = selectedTOs[0].uri;
				var url = this.context.createPreviewUrl(path);
				imageData.previewUrl = url;
				imageData.relativeUrl = path;
				imageData.fileExtension = path.substring(path.indexOf(".")+1);

				insertCb.success(imageData);		
			}, 
			failure: function() {

			},
			context: _self
		});
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
		return "img-repository-upload";
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


CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-img-repository-upload", CStudioForms.Datasources.ImgRepoUpload);