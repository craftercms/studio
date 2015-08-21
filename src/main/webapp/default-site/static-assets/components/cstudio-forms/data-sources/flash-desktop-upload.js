CStudioForms.Datasources.FlashDesktopUpload = CStudioForms.Datasources.FlashDesktopUpload ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.FlashDesktopUpload, CStudioForms.CStudioFormDatasource, {

    getLabel: function() {
        return CMgs.format(langBundle, "flashUploadedDesktop");
    },

	/**
	 * action called when user clicks insert flash
	 */
	insertFlashAction: function(insertCb) {
		this._self = this;
		var site = CStudioAuthoringContext.site;
		var path = "/static-assets/flash"; // default
		var isUploadOverwrite = true;

	    for(var i=0; i<this.properties.length; i++) {
			if(this.properties[i].name == "repoPath") {
				path = this.properties[i].value;
			
				path = this.processPathsForMacros(path);
			}
		}

		var callback = { 
			success: function(flashData) {
				var url = this.context.createPreviewUrl(path + '/' + flashData.fileName);
				flashData.previewUrl = url
				flashData.relativeUrl = path + '/' + flashData.fileName
				insertCb.success(flashData);
			}, 
			failure: function() {
				insertCb.failure("An error occurred while uploading the flash."); 
			},
			context: this 
		};

		CStudioAuthoring.Operations.uploadAsset(site, path, isUploadOverwrite, callback);
	},

	/**
	 * create preview URL
	 */
	createPreviewUrl: function(flashPath) {
		return CStudioAuthoringContext.previewAppBaseUri + flashPath + "";
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

	deleteFlash : function(path) {
	},

   	getInterface: function() {
   		return "flash";
   	},

	getName: function() {
		return "flash-desktop-upload";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: "Repository Path", name: "repoPath", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
			{ label: "Required", name: "required", type: "boolean" },
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-flash-desktop-upload", CStudioForms.Datasources.FlashDesktopUpload);