CStudioAdminConsole.Tool.ContentTypes.PropertyType.String = CStudioAdminConsole.Tool.ContentTypes.PropertyType.String ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.String, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
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

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-string", CStudioAdminConsole.Tool.ContentTypes.PropertyType.String);