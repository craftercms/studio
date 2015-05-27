/**
 * Quick Content
 */
CStudioAuthoring.ContextualNav.QuickContentMod = CStudioAuthoring.ContextualNav.QuickContentMod || {

	initialized: false,
	openState: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.render();
	},
				
	render: function() {
		var el, containerEl, imageEl, ptoOn;

		el = YDom.get("acn-quick-content");	
		containerEl = document.createElement("div");
		containerEl.id = "acn-quick-content-button";

					
		el.appendChild(containerEl);
		containerEl.innerHTML = "<div id='acn-qc-wrapper' class='acn-dropdown-wrapper'>" +
								"<a class='acn-qc-toggler acn-drop-arrow' href='#' id='acn-qc-toggler'>New</a>" +
		                        "<div id='acn-qc-dropdown' style='background: none repeat scroll 0 0 white; "+
		                                    "border: 1px solid black; color: #0176b1; display: none; "+
		                                    "font-weight: bold; list-style: none outside none; position: relative; "+
		                                    "text-align: start; top: 4px; width: 100px; z-index: 100; position: fixed; margin-top: 46px;'>"+
          "<ul id='quick-content-options' style='list-style:none;'>" +
             //"<li style='cursor:pointer; margin:10px; padding: 2px;' id='qc-article'>Article</li>"+
             //"<li style='cursor:pointer; margin:10px; padding: 2px;'  id='qc-movie'>Movie</li>" +
          "</ul>"+
        "</div>"+
        "</div>";

        var buttonEl = document.getElementById("acn-qc-wrapper");
        buttonEl.control = this;
        buttonEl.onclick = function() { this.control.toggle(); }

        CStudioAuthoring.Service.lookupConfigurtion(CStudioAuthoringContext.site, "/context-nav/quick-content.xml", {
            success: function (config) {
                this.context.buildModules(config);
            },
            failure: CStudioAuthoring.Utils.noop,
            context: this
        });


	},

	buildModules: function(config) {
		var listEl = document.getElementById("quick-content-options");

		if (config.quickContent && !config.quickContent.length) {
            config.quickContent= [config.quickContent];
        }

        if (config.quickContent.length) {
            var containersEls = [];

            for (var j = 0; j < config.quickContent.length; j++) {
                var optionEl = document.createElement("li");
                optionEl.style.cursor= "pointer";
                optionEl.style.margin = "10px"; 
                optionEl.style.padding = "2px";
                optionEl.id = config.quickContent[j].name;
                optionEl.innerHTML =  config.quickContent[j].name;
                optionEl.control = this;
                optionEl.contentData = config.quickContent[j]
				optionEl.onclick = function() { this.control.newContent(this.contentData.contentType, this.contentData.contentPath); }

                listEl.appendChild(optionEl);
            }
		}
	},

	newContent: function(contentType, path) {
		var formSaveCb = {
			success: function() {
				document.location = document.location;
			}
		};

		CStudioAuthoring.Operations.createNewContentForType(
                        CStudioAuthoringContext.site,
                        path,
                        contentType,
                        false,
                        formSaveCb);
	},

	toggle: function() {

		var dropdownEl = document.getElementById("acn-qc-dropdown");
		var buttonEl = document.getElementById("acn-qc-wrapper");

		if(this.openState == true) {
			this.openState = false;
			dropdownEl.style.display = "none";
			buttonEl.style.backgroundColor = "transparent";
		}
		else {
			{
			this.openState = true;
			dropdownEl.style.display = "inline-block";
			buttonEl.style.backgroundColor = "#f0f0f0";
		}
		}

	}
}

CStudioAuthoring.Module.moduleLoaded("quick-content", CStudioAuthoring.ContextualNav.QuickContentMod);
