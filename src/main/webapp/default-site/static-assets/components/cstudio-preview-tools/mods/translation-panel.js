
CStudioAuthoring.TranslationPanel = CStudioAuthoring.TranslationPanel || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			
			this.initialized = true;
		}
	},

	render: function(containerEl, config) {

		var configCb = {
			success: function(config) {
				
				if (config.translation) {
					this.control.translationConfig = config;

					if(config.translation.targetSites) {
						var translationSelectEl = document.createElement("select");
						var translationButtonEl = document.createElement("input");
						translationButtonEl.type = "submit";

						translationButtonEl.value = "Translate";

						YAHOO.util.Dom.addClass(translationButtonEl, "cstudio-button");
						YAHOO.util.Dom.addClass(translationSelectEl, "acn-panel-dropdown");
						translationSelectEl.style.height = "20px";
						translationButtonEl.style.backgroundColor = "#C0C0C0";
						translationButtonEl.style.border = "1px solid #002185"; 
						translationButtonEl.style.borderRadius = "3px 3px 3px 3px"; 
						translationButtonEl.style.color ="#000000"; 
						translationButtonEl.style.font = "12px Arial,Helvetica"; 
						translationButtonEl.style.minWidth = "6em; padding: 5px 10px";
						translationButtonEl.style.height = "27px";
						translationButtonEl.style.left = "80px";
						translationButtonEl.style.position = "relative";
						translationButtonEl.style.right = "auto";

						containerEl.appendChild(translationSelectEl);
						containerEl.appendChild(translationButtonEl);

						if(!config.translation.targetSites.length) {
							config.translation.targetSites = [ config.translation.targetSites.targetSite ];
						}

						var targets = config.translation.targetSites;
						translationSelectEl.options[0] = new Option("All Target Languages", "0", false, false);

						for(var i=0; i<targets.length; i++) {
							var target = targets[i].targetLanguage;
							var label = targets[i].label;
							translationSelectEl.options[i+1] = new Option(label, ""+(i+1), false, false);
						}

						translationButtonEl.translationConfig = config.translation;
						translationButtonEl.select = translationSelectEl;

						translationButtonEl.onclick = function() {
							var value =  Number(this.select.value);
							var createWorkflowRequest = {
								site: CStudioAuthoringContext.site,
								jobs: []
							}

							if(value != 0) {
								var config = this.translationConfig.targetSites[value-1];
								createWorkflowRequest.jobs = [ {
									processName: "submit-for-translate",
									paths: [ CStudioAuthoring.SelectedContent.getSelectedContent()[0].uri ],
									properties: [
										{ name:"submitter", value: CStudioAuthoringContext.user},
										{ name:"sourceSite", value: CStudioAuthoringContext.site },
										{ name:"sourceLanguage", value: this.translationConfig.sourceLanguage },
										{ name:"targetSite", value: config.id },
										{ name:"basePath", value: config.basePath },
										{ name:"targetLanguage", value: config.targetLanguage }
									]
								}];
							}
							else {
								for(var p=0; p<this.translationConfig.targetSites.length; p++) {
									var config = this.translationConfig.targetSites[p];

									createWorkflowRequest.jobs[p] = {
										processName: "submit-for-translate",
										paths: [ CStudioAuthoring.SelectedContent.getSelectedContent()[0].uri ],
										properties: [
											{ name:"submitter", value: CStudioAuthoringContext.user},
											{ name:"sourceSite", value: CStudioAuthoringContext.site },
											{ name:"sourceLanguage", value: this.translationConfig.sourceLanguage },
											{ name:"targetSite", value: config.id },
											{ name:"basePath", value: config.basePath },
											{ name:"targetLanguage", value: config.targetLanguage }
										]
									};
								}
							}

							CStudioAuthoring.Service.createWorkflowJobs(createWorkflowRequest, {
								success: function() {
									alert("Translation submitted");
								},

								failure: function() {
								}
							});
						}
					}
				}
			},
			failure: function() {
			},
			control: this
		};

		CStudioAuthoring.Service.lookupConfigurtion(CStudioAuthoringContext.site, 
			"/site-config.xml", configCb);
	}
}

CStudioAuthoring.Module.moduleLoaded("translation-panel", CStudioAuthoring.TranslationPanel);