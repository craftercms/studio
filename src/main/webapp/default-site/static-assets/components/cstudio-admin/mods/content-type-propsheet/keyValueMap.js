CStudioAdminConsole.Tool.ContentTypes.PropertyType.KeyValueMap = CStudioAdminConsole.Tool.ContentTypes.PropertyType.KeyValueMap ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.KeyValueMap, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		this.setValue(value);
		this.updateFn = updateFn;
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
		YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");		
		containerEl.appendChild(valueEl);
		valueEl.value = this.valueToString();
		valueEl.context = this;
		
		valueEl.fieldName = this.fieldName;

		// don't let the user type anything
		YAHOO.util.Event.on(valueEl, 'keydown', function(evt) { YAHOO.util.Event.stopEvent(evt); }, valueEl);
			
		YAHOO.util.Event.on(valueEl, 'focus', this.showEdit, this);

		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return this.value;  	
	},

    setValue: function(value){
        if(Array.isArray(value)){
            this.value = value;
        }else if(typeof value == "string"){
            if(value == ""){
                this.value = [];
            }else{
                try{
                    this.value = JSON.parse(value);
                }catch(err){
                    this.value = eval("(" + value + ")");
                }
            }
        }else {
            this.value = [];
        }
    },
	
	valueToString: function() {
		var strValue = "";
		var values = this.value;

		for(var i=0; i < values.length; i++) {
			if(i!=0) strValue += ",";
			strValue += values[i].value;
		}

		return strValue;
	},

	valueToJsonString: function(value) {
        return JSON.stringify(value);
	},
		
	showEdit: function() {
		var _self = this;
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		if(!keyValueDialogEl) {
            var maskEl = document.createElement("div");
            maskEl.id = 'keyValueDialogMask';
            maskEl.style.display = 'block';
			keyValueDialogEl = document.createElement("div");
			keyValueDialogEl.id = 'keyValueDialog';
			YAHOO.util.Dom.addClass(keyValueDialogEl, "property-dialog");
			YAHOO.util.Dom.addClass(keyValueDialogEl, "seethrough");

			document.body.appendChild(maskEl);
            document.body.appendChild(keyValueDialogEl);
			
			// copy the values structure
			keyValueDialogEl.values = [];

			if(_self.context.value.length == 0) {
				_self.context.value = [ { key: "", value: ""} ];				
			}

			for(var i=0; i<_self.context.value.length; i++) {
				var item = _self.context.value[i];
				keyValueDialogEl.values[i] = { key: item.key, value: item.value };	
			}		
		}

		keyValueDialogEl.style.display = "block";
		keyValueDialogEl.innerHTML = "";
		
		var titleEl = document.createElement("div");
		YAHOO.util.Dom.addClass(titleEl, "property-dialog-title");
		titleEl.innerHTML = "Values";
		keyValueDialogEl.appendChild(titleEl);

		var keyValueDialogContainerEl = document.createElement("div");
		keyValueDialogContainerEl.id = "keyValueDialogContainer";
		keyValueDialogEl.appendChild(keyValueDialogContainerEl);

		this.context.renderListItems();
	
		var buttonContainerEl = document.createElement("div");
		YAHOO.util.Dom.addClass(buttonContainerEl, "property-dialog-button-container");
		keyValueDialogEl.appendChild(buttonContainerEl);

		var saveEl = document.createElement("div");
		YAHOO.util.Dom.addClass(saveEl, "btn btn-primary");
		saveEl.innerHTML = "Save";
		buttonContainerEl.appendChild(saveEl);
		YAHOO.util.Event.on(saveEl, 'click', function(evt) {
			_self.context.save();
		}, saveEl);

        var cancelEl = document.createElement("div");
        cancelEl.style.marginLeft = "6px";
        YAHOO.util.Dom.addClass(cancelEl, "btn btn-default");
        cancelEl.innerHTML = "Cancel";
        buttonContainerEl.appendChild(cancelEl);

        YAHOO.util.Event.on(cancelEl, 'click', function(evt) {
            _self.context.cancel();
        }, cancelEl);
	
	},
	
	renderListItems: function() {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		var tableContainerEl = document.getElementById("keyValueDialogContainer");
		var values = keyValueDialogEl.values;
		var _self = this;
		
		tableContainerEl.innerHTML = "";
		
		for(var i=0; i<values.length; i++) {

			var rowEl = document.createElement("div");
			YAHOO.util.Dom.addClass(rowEl, "property-dialog-row");

			var keyTitleEl = document.createElement("div");
			YAHOO.util.Dom.addClass(keyTitleEl, "property-dialog-col-title");
			keyTitleEl.innerHTML = "Key";
			keyValueDialogEl.appendChild(keyTitleEl);
			
			var valueTitleEl = document.createElement("div");
			YAHOO.util.Dom.addClass(valueTitleEl, "property-dialog-col-title");
			valueTitleEl.innerHTML = "Value";
			keyValueDialogEl.appendChild(valueTitleEl);

			var addEl = document.createElement("div");
			YAHOO.util.Dom.addClass(addEl, "property-dialog-add-link btn btn-default");
			addEl.innerHTML = "Add Another";
			addEl.index = i;
			
			YAHOO.util.Event.on(addEl, 'click', function(evt) {
				_self.insertItem(this.index);
				_self.renderListItems();
			}, addEl);			

			var delEl = null; 
			
			if(i!=0) {
				delEl = document.createElement("img");
				delEl.src = CStudioAuthoringContext.authoringAppBaseUri 
						  + "/static-assets/themes/cstudioTheme/images/icons/delete.png";
				YAHOO.util.Dom.addClass(delEl, "deleteControl");
				delEl.index = i;
				YAHOO.util.Event.on(delEl, 'click', function(evt) {
					_self.removeItem(this.index);
					_self.renderListItems();
				}, delEl);			
			}
			else {
				delEl = document.createElement("div");
				YAHOO.util.Dom.addClass(delEl, "deleteControl");
			}
 
			var keyEl = document.createElement("input");
			keyEl.index = i;
			YAHOO.util.Event.on(keyEl, 'keyup', function(evt) {
				_self.updateKey(this.index, this.value);
			}, keyEl);			
						
			var valEl = document.createElement("input");
			valEl.index = i;
			YAHOO.util.Event.on(valEl, 'keyup', function(evt) {
				_self.updateValue(this.index, this.value);
			}, valEl);			

			tableContainerEl.appendChild(rowEl);
			
			rowEl.appendChild(keyTitleEl);
			rowEl.appendChild(keyEl);

			rowEl.appendChild(valueTitleEl);
			rowEl.appendChild(valEl);

			rowEl.appendChild(addEl);
			rowEl.appendChild(delEl);
					
			keyEl.value = values[i].key;
			valEl.value = values[i].value;
		}
	},
	
	insertItem: function(index) {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		keyValueDialogEl.values.splice(index+1, 0, { key: "", value: "" });
	},

	removeItem: function(index) {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		keyValueDialogEl.values.splice(index,1);
	},

	updateKey: function(index, value) {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		keyValueDialogEl.values[index].key = value;
	},
	
	updateValue: function(index, value) {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
		keyValueDialogEl.values[index].value = value;
	},

	cancel: function() {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
        var keyValueDialogMaskEl = document.getElementById("keyValueDialogMask");
        keyValueDialogEl.parentNode.removeChild(keyValueDialogMaskEl);
		keyValueDialogEl.parentNode.removeChild(keyValueDialogEl);
	},

	save: function() {
		var keyValueDialogEl = document.getElementById("keyValueDialog");
        var keyValueDialogMaskEl = document.getElementById("keyValueDialogMask");
		this.value = keyValueDialogEl.values;
		this.valueEl.value = this.valueToString();
        keyValueDialogEl.parentNode.removeChild(keyValueDialogMaskEl);
		keyValueDialogEl.parentNode.removeChild(keyValueDialogEl);
		this.updateFn(null, { fieldName: this.fieldName, value: this.valueToJsonString(this.value)});
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-keyValueMap", CStudioAdminConsole.Tool.ContentTypes.PropertyType.KeyValueMap);