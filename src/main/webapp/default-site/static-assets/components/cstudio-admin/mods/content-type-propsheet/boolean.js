CStudioAdminConsole.Tool.ContentTypes.PropertyType.Bool = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Bool||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Bool, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var _self = this;
		var containerEl = this.containerEl,
            wrapperEl = document.createElement("div"),
            valueEl = document.createElement("input");

		valueEl.type = "checkbox";
		valueEl.checked = (value == "true");
		valueEl.fieldName = this.fieldName;

		wrapperEl.appendChild(valueEl);
		containerEl.appendChild(wrapperEl);

		if(updateFn) {
			var updateFieldFn = function(event, el) {
				_self.value = _self.getValue();
				updateFn(event, _self);
				CStudioAdminConsole.Tool.ContentTypes.visualization.render();
			};
						
			YAHOO.util.Event.on(valueEl, 'change', updateFieldFn, valueEl);
		}
		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return ""+(this.valueEl.checked == true);	
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-boolean", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Bool);