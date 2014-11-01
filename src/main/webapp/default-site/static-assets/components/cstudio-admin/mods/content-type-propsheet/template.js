CStudioAdminConsole.Tool.ContentTypes.PropertyType.Template = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Template||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Template, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var _self = this;
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
		YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");		
		containerEl.appendChild(valueEl);
		valueEl.value = value;
		valueEl.fieldName = this.fieldName;
		this.updateFn = updateFn;

		// don't let the user type anything
		YAHOO.util.Event.on(valueEl, 'keydown', function(evt) { YAHOO.util.Event.stopEvent(evt); }, valueEl);

		YAHOO.util.Event.on(valueEl, 'focus', function(evt) { _self.showTemplateEdit(); }, valueEl);
		//YAHOO.util.Event.on(valueEl, 'blur', function(evt) { _self.hideTemplateEdit();  }, valueEl);
						
		if(updateFn) {
			var updateFieldFn = function(event, el) {
				
			};
						
			YAHOO.util.Event.on(valueEl, 'change', updateFieldFn, valueEl);
		}
		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return this.valueEl.value;	
	},
	
	showTemplateEdit: function() {
		var _self = this;
		if(this.controlsContainerEl) {
			this.controlsContainerEl.style.display = "inline";
			this.valueEl.size
		}
		else {
			var controlsContainerEl = document.createElement("div");
			YAHOO.util.Dom.addClass(controlsContainerEl, "options");

			var editEl = document.createElement("div");
			YAHOO.util.Dom.addClass(editEl, "edit");
			
			var pickEl = document.createElement("div");
			YAHOO.util.Dom.addClass(pickEl, "pick");
			
			controlsContainerEl.appendChild(editEl);
			controlsContainerEl.appendChild(pickEl);			
			
			this.containerEl.appendChild(controlsContainerEl);
			
			this.controlsContainerEl = controlsContainerEl;
			
			editEl.onclick = function() {
				var contentType = _self.valueEl.value

				if(contentType == "") {
					CStudioAuthoring.Operations.createNewTemplate({ 
						success: function(templatePath) {
							_self.valueEl.value = templatePath;
							_self.value = templatePath;
							_self.updateFn(null, _self.valueEl);	 
						}, 
						failure: function() {}
					}); 
				}
				else {
					CStudioAuthoring.Operations.openTemplateEditor
						(contentType, "default", { success: function() {}, failure: function() {}});
				}
			}
			
			pickEl.onclick = function() {
				CStudioAuthoring.Operations.openBrowse("", "/templates/web", "1", "select", true, { 
					success: function(searchId, selectedTOs) {
						var item = selectedTOs[0];
						_self.valueEl.value = item.uri; 
						_self.value = item.uri;	 
						_self.updateFn(null, _self.valueEl);	 
					}, failure: function() {}}); 
			}
		}
	},
	
	hideTemplateEdit: function() {
		if(this.controlsContainerEl) {
			this.controlsContainerEl.style.display = "none";
		}
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-template", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Template);