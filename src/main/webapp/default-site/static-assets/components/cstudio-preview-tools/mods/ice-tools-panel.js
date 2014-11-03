CStudioAuthoring.Utils.addJavascript("/static-assets/modules/editors/tiny_mce/tiny_mce.js");
CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-forms/forms-engine.js");


/**
 * editor tools
 */
CStudioAuthoring.IceToolsPanel = CStudioAuthoring.IceToolsPanel || {

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

	    var buttonEl, imageEl, labelEl, iceOn;

		buttonEl = document.createElement("div");
		YAHOO.util.Dom.addClass(buttonEl, "acn-ptools-button");

		imageEl = document.createElement("img");
		labelEl = document.createElement("span");
        YDom.addClass(labelEl, "acn-ptools-ice-label");

		iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean

		if (iceOn) {
    	    imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit.png";
    	    labelEl.innerHTML = "In-Context Edit On";
		} else {
            imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit_off.png";
            labelEl.innerHTML = "In-Context Edit Off";
		}

		buttonEl.appendChild(imageEl);
		containerEl.appendChild(buttonEl);
		containerEl.appendChild(labelEl);

		buttonEl.onclick = function() {
		    var iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean

			if(!iceOn) {
				CStudioAuthoring.IceTools.turnEditOn();
			}
			else {
				CStudioAuthoring.IceTools.turnEditOff();
			}
		}

		var regionSelectEl = document.createElement("select");
		YAHOO.util.Dom.addClass(regionSelectEl, "acn-panel-dropdown");
		regionSelectEl.style.height = "20px";
		
		containerEl.appendChild(regionSelectEl);
		regionSelectEl.options[0] = new Option("Jump to Region", "0", true, false);
		
		for(var i=0; i<CStudioAuthoring.InContextEdit.regions.length; i++) {
			var label = (CStudioAuthoring.InContextEdit.regions[i].label) ? 
					CStudioAuthoring.InContextEdit.regions[i].label : CStudioAuthoring.InContextEdit.regions[i].id;
			regionSelectEl.options[i+1] = new Option(label, ""+(i+1), false, false);
		}

		regionSelectEl.onchange = function() {
			var selectedIndex = this.selectedIndex;
			if(selectedIndex != 0) {
				var region = CStudioAuthoring.InContextEdit.regions[selectedIndex-1];
				var regionEl = document.getElementById(region.id);
				
				if(regionEl) {
					regionEl.scrollIntoView();
					window.scrollBy(0,-150);
					
					window.setTimeout(function() {
						regionEl.style.border = "1px solid blue";
							window.setTimeout(function() {
								regionEl.style.border = "";
							}, 1000);
					}, 1000);
				}
				else {
					var label = (region.label) ? region.label : region.id;
					alert("Region " + label + " could not be found");
				}
			}
		};

		var brEl = document.createElement("br");
		containerEl.appendChild(brEl);
		
		var templateButtonEl = document.createElement("div");
		YAHOO.util.Dom.addClass(templateButtonEl, "acn-ptools-button");

		var templateImageEl = document.createElement("img");
		var templateLabelEl = document.createElement("span");
		
        templateImageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/icons/code-edit.gif";
    	templateLabelEl.innerHTML = "Edit Template";
    	    
        YDom.addClass(templateLabelEl, "acn-ptools-ice-label");

		templateButtonEl.appendChild(templateImageEl);
		containerEl.appendChild(templateButtonEl);
		containerEl.appendChild(templateLabelEl);

		var onSaveCb = {
			success: function() {
				if(!CStudioAuthoringContext.channel || CStudioAuthoringContext.channel == "web") {
					document.location = document.location;
				}
				else {

					var cb = {
						moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
				   			try {
				   				moduleClass.render();
				   			} 
					   		catch (e) {
							}	
						},
					
						self: this
					};
				
					CStudioAuthoring.Module.requireModule(
                    	"medium-panel-"+CStudioAuthoringContext.channel,
                    	'/static-assets/components/cstudio-preview-tools/mods/agent-plugins/'+channel.value+'/'+CStudioAuthoringContext.channel+'.js',
                    	0,
                   	 	cb);

				}
			},
			failure: function() {
			}
		};
		
		templateButtonEl.onclick = function() {
			var contentType = CStudioAuthoring.SelectedContent.getSelectedContent()[0].renderingTemplates[0].uri;
			
			if(CStudioAuthoringContext.channel && CStudioAuthoringContext.channel != "web") {
					contentType = contentType.substring(0, contentType.lastIndexOf(".ftl")) +
						"-" + CStudioAuthoringContext.channel + ".ftl"; 
			}
			
			
			
			CStudioAuthoring.Operations.openTemplateEditor(contentType, "default", onSaveCb);
		};

       	CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(
       			function() {
       				imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit_off.png";
    				labelEl.innerHTML = "In-Context Edit Off";
       			});

       	CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(
       			function() {
       				imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit.png";
       				YDom.replaceClass(containerEl.parentNode, 'contracted', 'expanded');
       				labelEl.innerHTML = "In-Context Edit On";
       			});
		
		if(iceOn) {
			CStudioAuthoring.IceTools.turnEditOn();
		}
	}
}

CStudioAuthoring.Module.moduleLoaded("ice-tools-panel", CStudioAuthoring.IceToolsPanel);