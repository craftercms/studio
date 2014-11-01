CStudioAdminConsole.Tool.ContentTypes.PropertyType.Int = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Int ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Int, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var _self = this;
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
		YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");		
		containerEl.appendChild(valueEl);
		valueEl.value = value;
		valueEl.fieldName = this.fieldName;
	
		var validFn = function(evt, el) {
			if (evt && evt != null) {
				var charCode = (evt.which) ? evt.which : event.keyCode
				
				if(!_self.isNumberKey(charCode)) {
	          		if(evt)
	          			YAHOO.util.Event.stopEvent(evt);			
				}
			}
		};

		YAHOO.util.Event.on(valueEl, 'keydown', validFn, valueEl);

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
	},

	isNumberKey: function(charCode) {
		return !(charCode != 43 && charCode > 31 && (charCode < 48 || charCode > 57));
	}
	
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-int", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Int);