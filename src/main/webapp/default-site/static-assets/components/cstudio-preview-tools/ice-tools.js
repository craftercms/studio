CStudioAuthoring.Utils.addJavascript("/modules/editors/tiny_mce/tiny_mce.js");
CStudioAuthoring.Utils.addJavascript("/components/cstudio-forms/forms-engine.js");
/**
 * In-Context Editing (ICE)
 */
CStudioAuthoring.IceTools = CStudioAuthoring.IceTools || {
	IceToolsOffEvent: new YAHOO.util.CustomEvent("cstudio-ice-tools-off", CStudioAuthoring),
	IceToolsOnEvent: new YAHOO.util.CustomEvent("cstudio-ice-tools-on", CStudioAuthoring),
	
	initialized: false,

	/**
	 * initialize module
	 */
	initialize: function(config) {
	    var iceOn;

		if(this.initialized == false) {
			iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean
			
			if(iceOn) {
				this.IceToolsOnEvent.fire();
			}
			
			this.initialized = true;
		}
	},
	
	turnEditOn: function() {
		if(!!(sessionStorage.getItem('pto-on') == false)) {   // cast string value to a boolean
			CStudioAuthoring.PreviewTools.turnToolsOn();
		}
		sessionStorage.setItem('ice-on', "on");
		this.IceToolsOnEvent.fire();
	},
	
	turnEditOff: function() {
		sessionStorage.setItem('ice-on', "");
		this.IceToolsOffEvent.fire();
	}
}

CStudioAuthoring.Module.moduleLoaded("ice-tools-controller", CStudioAuthoring.IceTools);