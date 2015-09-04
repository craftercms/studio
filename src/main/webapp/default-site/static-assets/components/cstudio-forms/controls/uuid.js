
CStudioForms.Controls.UUID = CStudioForms.Controls.UUID  ||
	function(id, form, owner, properties, constraints, readonly, obj)  {
		this.owner = owner;
		this.owner.registerField(this);
		this.errors = [];
		this.properties = properties;
		this.constraints = constraints;
		this.inputEl = null;
		this.patternErrEl = null;
		this.countEl = null;
		this.required = false;
		this.value = "_not-set";
		this.form = form;
		this.id = id;
		this.readonly = readonly;

		return this;
	}

YAHOO.extend(CStudioForms.Controls.UUID, CStudioForms.CStudioFormField, {

	getLabel: function() {
		return "uuid";
	},

	_onChange: function(evt, obj) {
		obj.value = obj.inputEl.value;

		var validationExist = false;
		var validationResult = true;
		if(obj.required) {
			if(obj.inputEl.value == "") {
				obj.setError("required", "Field is Required");
				validationExist = true;
				validationResult = false;
			}
			else {
				obj.clearError("required");
				//validationExist = true;
			}
		}


		// actual validation is checked by # of errors
		// renderValidation does not require the result being passed
		obj.renderValidation(validationExist, validationResult);
		obj.owner.notifyValidation();
		obj.form.updateModel(obj.id, obj.getValue());
	},




	render: function(config, containerEl) {
		// we need to make the general layout of a control inherit from common
		// you should be able to override it -- but most of the time it wil be the same
		containerEl.id = this.id;

		var titleEl = document.createElement("span");

		YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
		titleEl.innerHTML = config.title;

		var controlWidgetContainerEl = document.createElement("div");
		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-uuid-container');



		var inputEl = document.createElement("input");
		this.inputEl = inputEl;
		YAHOO.util.Dom.addClass(inputEl, 'datum');
		YAHOO.util.Dom.addClass(inputEl, 'cstudio-form-control-input');
		inputEl.value = (this.value = "_not-set") ? config.defaultValue : this.value;
		controlWidgetContainerEl.appendChild(inputEl);

		YAHOO.util.Event.on(inputEl, 'focus', function(evt, context) { context.form.setFocusedField(context) }, this);

		YAHOO.util.Event.on(inputEl, 'change', this._onChange, this);
		YAHOO.util.Event.on(inputEl, 'blur', this._onChange, this);
		this.readonly = true;
		inputEl.disabled = true;
		inputEl.size = 40
		containerEl.appendChild(titleEl);
		containerEl.appendChild(controlWidgetContainerEl);

	},

	getValue: function() {
		return this.value;
	},

	setValue: function(value) {

		if (!value || value == "") {
			value = this.generateUUID();
		}
		this.value = value;
		this.inputEl.value = value;
		//this.count(null, this.countEl, this.inputEl);
		this._onChange(null, this);
	},

	getName: function() {
		return "uuid";
	},

	getSupportedProperties: function() {
		return [
			//{ label: "Display Size", name: "size", type: "int", defaultValue: "50" },
			//{ label: "Max Length", name: "maxlength", type: "int",  defaultValue: "50" },
			//{ label: "Readonly", name: "readonly", type: "boolean" },
		];
	},

	getSupportedConstraints: function() {
		return [
			{ label: "Required", name: "required", type: "boolean" },
			//{ label: "Match Pattern", name: "pattern", type: "string" },
		];
	},
   generateUUID : function () {
	   var d = new Date().getTime();
	   var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		   var r = (d + Math.random()*16)%16 | 0;
		   d = Math.floor(d/16);
		   return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	   });
	   return uuid;
   }
});


CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-uuid", CStudioForms.Controls.UUID);