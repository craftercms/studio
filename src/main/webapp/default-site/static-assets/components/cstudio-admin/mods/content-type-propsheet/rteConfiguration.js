CStudioAdminConsole.Tool.ContentTypes.PropertyType.RteConfiguration = CStudioAdminConsole.Tool.ContentTypes.PropertyType.RteConfiguration ||  function(fieldName, containerEl, form, type)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	this.form = form;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.RteConfiguration, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {

	render: function(value, updateFn) {
		var _self = this;
		var form = this.form;
		var type = this["interface"];
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
		YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");		
		containerEl.appendChild(valueEl);
		valueEl.value = value;
		valueEl.fieldName = this.fieldName;
		this.valueEl = valueEl;

		var pickEl = document.createElement("select");
		YAHOO.util.Dom.addClass(pickEl, "content-type-property-sheet-property-value");
		pickEl.style.display = "none";
		containerEl.appendChild(pickEl);
		pickEl.context = this;			

		// don't let the user type anything
		YAHOO.util.Event.on(valueEl, 'keydown', function(evt) { YAHOO.util.Event.stopEvent(evt); }, valueEl);
		YAHOO.util.Event.on(valueEl, 'click', function(evt) { 
			valueEl.style.display = "none";
			pickEl.style.display = "inline";

			var configCb = {
				success: function(config) {
					pickEl.options.length=0;

					for(var j=0; j<config.setup.length; j++) {
						setupId = config.setup[j].id;
						var option = new Option(setupId, setupId);
						pickEl.options[pickEl.options.length] = option;
						if(setupId== valueEl.value) {
							option.selected = true;
						}
					}
				},
				failure: function() {
				}
			};
			
			CStudioAuthoring.Service.lookupConfigurtion(
								CStudioAuthoringContext.site, 
								"/form-control-config/rte/rte-setup.xml",
								configCb);			
		}, pickEl);

		YAHOO.util.Event.on(pickEl, 'change', function(evt) { 
			valueEl.style.display = "inline";
			pickEl.style.display = "none";
			var value = pickEl.options[pickEl.selectedIndex].value;
			valueEl.value = value;
			updateFn(evt, { fieldName: valueEl.fieldName, value: value });
			CStudioAdminConsole.Tool.ContentTypes.visualization.render();
		}, pickEl);
	},
	
	getValue: function() {
		return this.valueEl.value;	
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-rteConfiguration", CStudioAdminConsole.Tool.ContentTypes.PropertyType.RteConfiguration);