CStudioForms.Datasources.ConfiguredList = CStudioForms.Datasources.ConfiguredList ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	this.callbacks = [];
	var _self = this;
	
	for(var i=0; i<properties.length; i++) {
		var property = properties[i]
		if(property.name == "listName") {
			var cb = { 
				success: function(config) {
					var values = config.values;
					if(!values.length) {
						values = [ values.value ];
					}
					
					_self.list = values;
					
					for(var j=0; j<_self.callbacks.length; j++) {
						_self.callbacks[j].success(values);
					}
				},
				failure: function() {
				}
			};
			
			CStudioAuthoring.Service.lookupConfigurtion(
					CStudioAuthoringContext.site, 
					"/form-control-config/configured-lists/" + property.value + ".xml",
					cb);
				
		}
	}
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.ConfiguredList, CStudioForms.CStudioFormDatasource, {

    getLabel: function() {
        return "Configured List of Values";
    },

   	getInterface: function() {
   		return "item";
   	},

   	/*
     * Datasource controllers don't have direct access to the properties controls, only to their properties and their values.
     * Because the property control (dropdown) and the dataType property share the property value, the dataType value must stay
     * as an array of objects where each object corresponds to each one of the options of the control. In order to know exactly
     * which of the options in the control is currently selected, we loop through all of the objects in the dataType value 
     * and check their selected value.
     */
    getDataType : function getDataType () {
        var val = null;

        this.properties.forEach( function(prop) {
            if (prop.name == "dataType") {
                // return the value of the option currently selected
                prop.value.forEach( function(opt) {
                    if (opt.selected) {
                        val = opt.value;
                    }
                });
            }
        });
        return val;
    },

	getName: function() {
		return "configured-list";
	},
	
	getSupportedProperties: function() {
		return [{
			label: "Data Type",
			name: "dataType",
			type: "dropdown",
			defaultValue: [{ // Update this array if the dropdown options need to be updated
				value: "value",
				label: "",
				selected: true
			}, {
				value: "value_s",
				label: "String",
				selected: false
			}, {
				value: "value_i",
				label: "Integer",
				selected: false
			}, {
				value: "value_f",
				label: "Float",
				selected: false
			}, {
				value: "value_dt",
				label: "Date",
				selected: false
			}, {
				value: "value_html",
				label: "HTML",
				selected: false
			}]
		}, {
			label: "List Name",
			name: "listName",
			type: "string"
		}];
	},	

	getSupportedConstraints: function() {
		return [
			{ label: "Required", name: "required", type: "boolean" }
		];
	},
	
	getList: function(cb) {
		if(!this.list) {
			this.callbacks[this.callbacks.length] = cb;
		}
		else {
			cb.success(this.list);
		}
	},
	

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-configured-list", CStudioForms.Datasources.ConfiguredList);