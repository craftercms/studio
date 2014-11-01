/**
 * editor tools
 */
CStudioAuthoring.MediumPanel.Browser = CStudioAuthoring.MediumPanel.Browser || {

	initialized : false,

	initialize : function(config) {
		if (this.initialized == false) {

			this.initialized = true;
		}
	},

	render : function(containerEl, config) {
		var emulateEl = document.getElementById("cstudio-emulate");

		if (emulateEl) {
			emulateEl.parentNode.removeChild(emulateEl);
		}
	}
}

CStudioAuthoring.Module.moduleLoaded("medium-panel-browser", CStudioAuthoring.MediumPanel.Browser);
