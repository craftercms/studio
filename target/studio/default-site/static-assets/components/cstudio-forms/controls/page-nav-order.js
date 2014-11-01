CStudioForms.Controls.PageNavOrder = CStudioForms.Controls.PageNavOrder ||  
function(id, form, owner, properties, constraints, readonly)  {
	this.owner = owner;
	this.owner.registerField(this);
	this.errors = []; 
	this.properties = properties;
	this.constraints = constraints;
	this.dropdownEl = null; // Must Reference the select element
	this.required = false;
	this.value = "_not-set"; // Must Be True or False
	this.form = form;
	this.id = id;
	this.readonly = readonly;
	
	this.orderDefault = "orderDefault_f";
	this.orderValue = null;
	this.placeInNav = "placeInNav";
	
	return this;
}

YAHOO.extend(CStudioForms.Controls.PageNavOrder, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return "Page Order";
    },

	_onChange: function(evt, obj) {
		obj.value = obj.dropdownEl.value;//value from the dropdown
		
		obj.owner.notifyValidation();
		obj.form.updateModel(obj.id, obj.getValue());
		obj.form.updateModel(obj.placeInNav, obj.getValue());

		if (obj.getValue() === "true") {
			obj.form.updateModel(obj.orderDefault, obj.orderValue);
		}else{
			obj.form.updateModel(obj.orderDefault, -1);
		}
	},

	showEditPosition: function() {
		if (this.dropdownEl.value == 'true') {
			this.editPositionEl.style.display = "inline";
			if(!this.orderValue || this.orderValue === -1){
				this.setOrderValue();
			}else{
				this._onChange(null, this);
			}
		} else {
			this.editPositionEl.style.display = "none";
			this._onChange(null, this);
		}
	},

	showEditPositionDialog: function() {
	    //Disable Edit Position button to not allow double clicks
	    this.editPositionEl.disabled = true;

	    var query = location.search.substring(1); 
	    var thisPage = CStudioAuthoring.Utils.getQueryVariable(query, 'path');
	    var order = 'default';

	    var callback = {
		    success: function(contentTypes) {                    
				    var query = location.search.substring(1); 
				    var currentPath = CStudioAuthoring.Utils.getQueryVariable(query, 'path');
				    var contentTypeSize = contentTypes.order.length;

			    	var pageFound = 'false';
					for (var i = 0; i < contentTypeSize; i++) {
					    var orderId = contentTypes.order[i].id;

					    if (orderId == currentPath) {
							contentTypes.order[i].internalName = "This Page";
							contentTypes.order[i].order = this.parentControl.orderValue;
							pageFound = 'true';
							break;
					    }
					}

					if (pageFound == 'false') {
						contentTypes.order.push({id:currentPath, order:this.parentControl.orderValue, internalName:"This Page"});
					}

				    panelId = 'panel1';
				    CStudioAuthoring.Service.reorderServiceCreatePanel(panelId, contentTypes, CStudioAuthoringContext.site, this.parentControl);			
			    //Enable Edit Position button
			    this.parentControl.editPositionEl.disabled = false;
		    },

		    failure: function() {
				//Enable Edit Position button
				this.parentControl.editPositionEl.disabled = false;
			}
	    };
	    callback.parentControl = this;

	    CStudioAuthoring.Service.getOrderServiceRequest(CStudioAuthoringContext.site, thisPage, order, callback);
		this.editPositionEl.disabled = false;
	},

	render: function(config, containerEl) {
		containerEl.id = this.id;

		var	currentValue = (this.value == "_not-set")?this.defaultValue:this.value;

		for(var i=0; i<config.properties.length; i++){
			var prop =  config.properties[i];
			
			if(prop.name == "readonly" && prop.value == "true"){
				this.readonly = true;
			}
		}
		
		var titleEl = document.createElement("span");
  		    YAHOO.util.Dom.addClass(titleEl, 'label');
  		    YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
			titleEl.innerHTML = config.title;

		var controlWidgetContainerEl = document.createElement("div");
		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-page-nav-order-container');

		var validEl = document.createElement("span");
			YAHOO.util.Dom.addClass(validEl, 'validation-hint');
			YAHOO.util.Dom.addClass(validEl, 'cstudio-form-control-validation');
			controlWidgetContainerEl.appendChild(validEl);

		var dropdownEl = document.createElement("select");
			YAHOO.util.Dom.addClass(dropdownEl, 'datum');
			YAHOO.util.Dom.addClass(dropdownEl, 'cstudio-form-control-dropdown');
			this.dropdownEl = dropdownEl;

		    var option1El = document.createElement('option');
		    option1El.value = "true";
		    option1El.text = "Yes";
		    dropdownEl.add(option1El);

		    var option2El = document.createElement('option');
		    option2El.value = "false";
		    option2El.text = "No";
		    dropdownEl.add(option2El);

			controlWidgetContainerEl.appendChild(dropdownEl);

		var editPositionEl = document.createElement("input");
			this.editPositionEl = editPositionEl;
			YAHOO.util.Dom.addClass(editPositionEl, 'cstudio-button');
			editPositionEl.type = "button";
			editPositionEl.value = "Edit Position";
			editPositionEl.style.paddingLeft = "10px";
			editPositionEl.style.display = "none";
			controlWidgetContainerEl.appendChild(editPositionEl);

        this.renderHelp(config, controlWidgetContainerEl);

		var descriptionEl = document.createElement("span");
			YAHOO.util.Dom.addClass(descriptionEl, 'description');
			YAHOO.util.Dom.addClass(descriptionEl, 'cstudio-form-field-description');
			descriptionEl.innerHTML = config.description;

		containerEl.appendChild(titleEl);
		containerEl.appendChild(controlWidgetContainerEl);
		containerEl.appendChild(descriptionEl);

		if (this.required == "true") {
			this.dropdownEl.value = "true";
			this.dropdownEl.disabled = "true";
			this.editPositionEl.style.display = "inline";
			this._onChange(null,this);
		}
		
		if (currentValue === "true" || currentValue == true) {
			this.dropdownEl.value = "true";
		} else {
			this.dropdownEl.value = "false";
		}

		if(this.readonly == true) {
			dropdownEl.disabled =  true;
			editPositionEl.value = "View Order";
		}

		YAHOO.util.Event.addListener(dropdownEl, "change", this.showEditPosition, this, true);
		YAHOO.util.Event.addListener(editPositionEl, "click", this.showEditPositionDialog, this, true);
	},

	getValue: function() {
		return this.value;
	},
	
	setOrderValue: function(){
		var submitCallback = {
				success: function(orderValue) {
					this.parentControl.orderValue = orderValue;
					this.parentControl._onChange(null, this.parentControl);
				},

				failure: function() {
					this.parentControl.orderValue = -1;
					this.parentControl._onChange(null, this.parentControl);
				}
			}; // end of callback
			submitCallback.parentControl = this;
		var query = location.search.substring(1); 
		var thisPage = CStudioAuthoring.Utils.getQueryVariable(query, 'path');
		var parentPath = CStudioAuthoring.Utils.getParentPath(thisPage);
			CStudioAuthoring.Service.getNextOrderSequenceRequest(CStudioAuthoringContext.site, parentPath,submitCallback);
	},
	
	setValue: function(value) {
		this.value = value;
		
		if( value === "true" || value === true){
			this.dropdownEl.value = "true";
			this.editPositionEl.style.display = "inline";
			
			//Takes the value of the orderDefault if exists
			this.orderValue = this.form.getModelValue(this.orderDefault);
			if(!this.orderValue || this.orderValue === -1 || this.orderValue === 0){
				this.setOrderValue();
			}else{
				this._onChange(null, this);
			}
		}else{
			this.dropdownEl.value = "false";
			this._onChange(null, this);
		}

	
	},

	getName: function() {
		return "page-nav-order";
	},

	getSupportedProperties: function() {
		return [
		   { label: "Readonly", name: "readonly", type: "boolean"}
		];
	},

	getSupportedConstraints: function() {
		return [
			{ label: "Required", name: "required", type: "boolean" },
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-page-nav-order", CStudioForms.Controls.PageNavOrder);