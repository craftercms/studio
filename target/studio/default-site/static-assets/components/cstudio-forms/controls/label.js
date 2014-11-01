CStudioForms.Controls.Label = CStudioForms.Controls.Label ||  
function(id, form, owner, properties, constraints)  {
	this.owner = owner;
	this.owner.registerField(this);
	this.errors = []; 
	this.properties = properties;
	this.constraints = constraints;
	this.inputEl = null;
	this.countEl = null;
	this.required = false;
	this.value = "_not-set";
	this.form = form;
	this.id = id;
	
	return this;
}

YAHOO.extend(CStudioForms.Controls.Label, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return "Label";
    },
    

	render: function(config, containerEl) {
		// we need to make the general layout of a control inherit from common
		// you should be able to override it -- but most of the time it wil be the same
		containerEl.id = this.id;

		var titleEl = document.createElement("span");
			YAHOO.util.Dom.addClass(titleEl, 'label');
  		    YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
			titleEl.innerHTML = "&nbsp;";
			containerEl.appendChild(titleEl);
		
		var controlWidgetContainerEl = document.createElement("div");
		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'datum');
		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-input-container');
        containerEl.appendChild(controlWidgetContainerEl);

		for(var i=0; i<config.properties.length; i++) {
			var prop = config.properties[i];

			if(prop.name == "text") {
				controlWidgetContainerEl.innerHTML = prop.value;
			}
		}
	},

	getValue: function() {
		return this.value;
	},
	
	setValue: function(value) {
		this.value = value;
	},
		
	getName: function() {
		return "label";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: "Text", name: "text", type: "string" }
        ];
	},

	getSupportedConstraints: function() {
		return [
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-label", CStudioForms.Controls.Label);

