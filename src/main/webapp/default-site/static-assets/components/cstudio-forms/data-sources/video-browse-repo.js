CStudioForms.Datasources.VideoBrowseRepo= CStudioForms.Datasources.VideoBrowseRepo ||  
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

YAHOO.extend(CStudioForms.Datasources.VideoBrowseRepo, CStudioForms.CStudioFormDatasource, {

	insertVideoAction: function(callback) {
		var _self = this;

			    CStudioAuthoring.Operations.openBrowse("", _self.processPathsForMacros(_self.repoPath), "-1", "select", true, { 
					success: function(searchId, selectedTOs) {
						var item = selectedTOs[0];
						var url = CStudioAuthoringContext.previewAppBaseUri + item.uri;
						var videoData = {}
						videoData.previewUrl = url
						videoData.relativeUrl = item.uri
						videoData.fileExtension = url.substring(url.lastIndexOf(".")+1);
						callback.success(videoData);
					}, 
					failure: function() {
					}
				}); 
	},
	
	// remove edit because edit is not supported
	edit: function(key) {
	},
	
    getLabel: function() {
        return CMgs.format(langBundle, "videoBrowse");
    },

   	getInterface: function() {
   		return "video";
   	},

	getName: function() {
		return "video-browse-repo";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: CMgs.format(langBundle, "repositoryPath"), name: "repoPath", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-video-browse-repo", CStudioForms.Datasources.VideoBrowseRepo);