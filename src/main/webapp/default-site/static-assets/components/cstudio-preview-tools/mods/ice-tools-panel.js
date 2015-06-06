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

        var container = document.createElement('div'),
            wrapper;

        containerEl.appendChild(container);
        container.className = 'studio-view';

	    var buttonEl, imageEl, labelEl, iceOn;

        wrapper = document.createElement('div');
		buttonEl = document.createElement("button");
		imageEl = document.createElement("img");
        labelEl = document.createElement("span");
        YDom.addClass(wrapper, 'form-group');
        YDom.addClass(buttonEl, 'btn btn-default btn-block');
        YDom.addClass(labelEl, 'acn-ptools-ice-label');

		iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean

		if (iceOn) {
    	    imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit.png";
    	    labelEl.innerHTML = "In-Context Edit On";
		} else {
            imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit_off.png";
            labelEl.innerHTML = "In-Context Edit Off";
		}

		buttonEl.appendChild(imageEl);
        buttonEl.appendChild(labelEl);
        wrapper.appendChild(buttonEl);
		container.appendChild(wrapper);

		buttonEl.onclick = function() {
		    var iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean
			if(!iceOn) {
				CStudioAuthoring.IceTools.turnEditOn();
			} else {
				CStudioAuthoring.IceTools.turnEditOff();
			}
		};

        wrapper = document.createElement('div');
		var regionSelectEl = document.createElement("select");

        YDom.addClass(wrapper, "form-group");
        YDom.addClass(regionSelectEl, "form-control");

        wrapper.appendChild(regionSelectEl);
        container.appendChild(wrapper);

        wrapper = document.createElement('div');
		var templateButtonEl = document.createElement("button");
		var templateImageEl = document.createElement("img");
		var templateLabelEl = document.createElement("span");

        templateImageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/icons/code-edit.gif";
    	templateLabelEl.innerHTML = "Edit Template";

        // YDom.addClass(wrapper, 'form-group');
        YDom.addClass(templateButtonEl, 'btn btn-default btn-block');
        YDom.addClass(templateLabelEl, 'acn-ptools-ice-label');

		templateButtonEl.appendChild(templateImageEl);
        templateButtonEl.appendChild(templateLabelEl);
        wrapper.appendChild(templateButtonEl);
        container.appendChild(wrapper);

        regionSelectEl.options[0] = new Option("Jump to Region", "0", true, false);
        for(var i=0; i<CStudioAuthoring.InContextEdit.regions.length; i++) {
            var label = (CStudioAuthoring.InContextEdit.regions[i].label)
                ? CStudioAuthoring.InContextEdit.regions[i].label
                : CStudioAuthoring.InContextEdit.regions[i].id;
            regionSelectEl.options[i+1] = new Option(label, '' + (i+1), false, false);
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
                } else {
                    var label = (region.label) ? region.label : region.id;
                    alert("Region " + label + " could not be found");
                }
            }
        };

		templateButtonEl.onclick = function() {
			var contentType = CStudioAuthoring.SelectedContent.getSelectedContent()[0].renderingTemplates[0].uri;
			
			// if(CStudioAuthoringContext.channel && CStudioAuthoringContext.channel != "web") {
			// 		contentType = contentType.substring(0, contentType.lastIndexOf(".ftl")) +
			// 			"-" + CStudioAuthoringContext.channel + ".ftl"; 
			// }

			CStudioAuthoring.Operations.openTemplateEditor(contentType, "default", {
                success: function() {
                    CStudioAuthoring.Operations.refreshPreview();     
                },
                failure: function() {
                }
            });
		};
        var contextNavImg = YDom.get("acn-ice-tools-image");
        var cstopic = crafter.studio.preview.cstopic;

        CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(function() {
            imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit_off.png";
            contextNavImg.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit_off.png";
            labelEl.innerHTML = "In-Context Edit Off";

            amplify.publish(cstopic('ICE_TOOLS_OFF'));

        });

        CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(function() {
            imageEl.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit.png";
            contextNavImg.src = CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/edit.png";
            YDom.replaceClass(containerEl.parentNode, 'contracted', 'expanded');
            labelEl.innerHTML = "In-Context Edit On";

            amplify.publish(cstopic('ICE_TOOLS_ON'));

        });
		
		if(iceOn) {
			CStudioAuthoring.IceTools.turnEditOn();
		}
	}
}

CStudioAuthoring.Module.moduleLoaded("ice-tools-panel", CStudioAuthoring.IceToolsPanel);