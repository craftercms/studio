CStudioForms.Controls.Textarea = CStudioForms.Controls.Textarea ||  
function(id, form, owner, properties, constraints, readonly)  {
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
	this.readonly = readonly;
	
	return this;
}

YAHOO.extend(CStudioForms.Controls.Textarea, CStudioForms.CStudioFormField, {
    getLabel: function() {
        return CMgs.format(langBundle, "textArea");
    },
    
	_onChange: function(evt, obj) {
		obj.value = obj.inputEl.value;
		
		if(obj.required) {
			if(obj.inputEl.value == "") {
				obj.setError("required", "Field is Required");
				obj.renderValidation(true, false);
			}
			else {
				obj.clearError("required");
				obj.renderValidation(true, true);
			}
		}
		else {
			obj.renderValidation(false, true);
		}			

		obj.owner.notifyValidation();
		obj.form.updateModel(obj.id, obj.getValue());
	},
    _onChangeVal: function(evt, obj) {
        obj.edited = true;
        this._onChange(evt,obj);
    },

	/**
	 * perform count calculation on keypress
	 * @param evt event
	 * @param el element
	 */
	count: function(evt, countEl, el) {
		// 'this' is the input box
	    el = (el) ? el : this;
	    var text = el.value;
	    
	    var charCount = ((text.length) ? text.length : ((el.textLength) ? el.textLength : 0));
	    var maxlength = (el.maxlength && el.maxlength != '') ? el.maxlength : -1;
	    
	    if(maxlength != -1) {
		    if (charCount > el.maxlength) {
				// truncate if exceeds max chars
				if (charCount > el.maxlength) {
				  this.value = text.substr (0, el.maxlength);
				  charCount = el.maxlength;
			    }
	      
	      		
				if (evt && evt != null
				&& evt.keyCode!=8 && evt.keyCode!=46 && evt.keyCode!=37
				&& evt.keyCode!=38 && evt.keyCode!=39 && evt.keyCode!=40	// arrow keys
				&& evt.keyCode!=88 && evt.keyCode !=86) {					// allow backspace and
																			// delete key and arrow keys (37-40)
																			// 86 -ctrl-v, 90-ctrl-z,
	          		if(evt)
	          			YAHOO.util.Event.stopEvent(evt);
	       		}
			}
	    }
	    
        if (maxlength != -1) {
        	countEl.innerHTML = charCount + ' / ' + el.maxlength;
        } 
        else {
        	countEl.innerHTML = charCount;
        }
    },
    	
	render: function(config, containerEl) {
		// we need to make the general layout of a control inherit from common
		// you should be able to override it -- but most of the time it wil be the same
		containerEl.id = this.id;
		
		var titleEl = document.createElement("span");

  		    YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
			titleEl.innerHTML = config.title;
		
		var controlWidgetContainerEl = document.createElement("div");
		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-input-container');

		var validEl = document.createElement("span");
			YAHOO.util.Dom.addClass(validEl, 'validation-hint');
			YAHOO.util.Dom.addClass(validEl, 'cstudio-form-control-validation');
			controlWidgetContainerEl.appendChild(validEl);

		var inputEl = document.createElement("textarea");
			this.inputEl = inputEl;
			YAHOO.util.Dom.addClass(inputEl, 'datum');
			YAHOO.util.Dom.addClass(inputEl, 'cstudio-form-control-input');
			inputEl.value = (this.value = "_not-set") ? config.defaultValue : this.value;

			YAHOO.util.Event.on(inputEl, 'change', this._onChangeVal, this);
			YAHOO.util.Event.on(inputEl, 'blur', this._onChange, this);
			YAHOO.util.Event.on(inputEl, 'focus', function(evt, context) { context.form.setFocusedField(context) }, this);

		var dataWrapperEl = document.createElement("div");
			YAHOO.util.Dom.addClass(dataWrapperEl, 'cstudio-form-control-input-data');

		var controlsWrapperEl = document.createElement("div");
			YAHOO.util.Dom.addClass(controlsWrapperEl, 'cstudio-form-control-input-controls');

		var countEl = document.createElement("div");
			this.countEl = countEl;
			YAHOO.util.Dom.addClass(countEl, 'char-count');
			YAHOO.util.Dom.addClass(countEl, 'cstudio-form-control-input-count');
			controlsWrapperEl.appendChild(countEl);

			YAHOO.util.Event.on(inputEl, 'keyup', this.count, countEl);
			YAHOO.util.Event.on(inputEl, 'keypress', this.count, countEl);
			YAHOO.util.Event.on(inputEl, 'mouseup', this.count, countEl);

			this.renderHelp(config, controlsWrapperEl);

			dataWrapperEl.appendChild(controlsWrapperEl);
			dataWrapperEl.appendChild(inputEl);

			controlWidgetContainerEl.appendChild(dataWrapperEl);

		for(var i=0; i<config.properties.length; i++) {
			var prop = config.properties[i];

			if(prop.name == "cols") {
				inputEl.cols = prop.value;
			}

			if(prop.name == "rows") {
				inputEl.rows = prop.value;
			}

			if(prop.name == "maxlength") {
				inputEl.maxlength = prop.value;
			}
			
			if(prop.name == "readonly" && prop.value == "true"){
				this.readonly = true;
			}
		}
		
			if(this.readonly == true){
				inputEl.disabled = true;
			}

		var descriptionEl = document.createElement("span");
			YAHOO.util.Dom.addClass(descriptionEl, 'description');
			YAHOO.util.Dom.addClass(descriptionEl, 'cstudio-form-field-description');
			descriptionEl.innerHTML = config.description;

		containerEl.appendChild(titleEl);
		containerEl.appendChild(controlWidgetContainerEl);
		containerEl.appendChild(descriptionEl);
	},

	getValue: function() {
		return this.value;
	},
	
	setValue: function(value) {
		this.value = value;
		this.inputEl.value = value;
		this.count(null, this.countEl, this.inputEl);
		this._onChange(null, this);
        this.edited = false;
	},
		
	getName: function() {
		return "textarea";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: CMgs.format(langBundle, "columns"), name: "cols", type: "int", defaultValue: "50" },
			{ label: CMgs.format(langBundle, "rows"), name: "rows", type: "int", defaultValue: "5" },
			{ label: CMgs.format(langBundle, "maxLength"), name: "maxlength", type: "int", defaultValue: "100000" },
			{ label: CMgs.format(langBundle, "allowResize"), name: "allowResize", type: "boolean", defaultValue: "true" },
			{ label: CMgs.format(langBundle, "readonly"), name: "readonly", type: "boolean" },
			];
	},

	getSupportedConstraints: function() {
		return [
			{ label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" },
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-textarea", CStudioForms.Controls.Textarea);

