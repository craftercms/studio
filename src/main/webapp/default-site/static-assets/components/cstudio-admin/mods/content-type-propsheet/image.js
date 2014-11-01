CStudioAdminConsole.Tool.ContentTypes.PropertyType.Image = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Image ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Image, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		// note THIS IS NOT DONE - CURRENTLY SAME AS STRING BUT
		// SHOULD SHOW LIST OF DATASOURCE THAT CAN PROVIDE TYPE IMAGE
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
		YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");		
		containerEl.appendChild(valueEl);
		valueEl.value = value;
		valueEl.fieldName = this.fieldName;

		if(updateFn) {
			var updateFieldFn = function(event, el) {
				updateFn(event, el);
				CStudioAdminConsole.Tool.ContentTypes.visualization.render();
			};
			
			YAHOO.util.Event.on(valueEl, 'keyup', updateFieldFn, valueEl);
		}
		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return this.valueEl.value;	
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-image", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Image);