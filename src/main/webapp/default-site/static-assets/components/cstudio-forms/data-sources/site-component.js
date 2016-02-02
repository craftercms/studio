CStudioForms.Datasources.SiteComponent = CStudioForms.Datasources.SiteComponent ||
function(id, form, properties, constraints)  {

   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	this.callbacks = [];
	var _self = this;
	
	for(var i=0; i<properties.length; i++) {
		var property = properties[i]
		if(property.name == "componentPath") {
			// Load page model
			var getContextIdCallback = {
				getItemServiceUrl: this.getItemServiceUrl,
				success: function(context) {
					var idContent = JSON.parse(context.responseText);
					var url = property.value;
					var getItemServiceUrl = this.getItemServiceUrl + "?url=" + url + "&contextId=" + idContent.id;

					var getItemCallBack = {
						success: function(response) {
							// Some native JSON parsers (e.g. Chrome) don't like the empty string for input
							var res = response.responseText || "null";
							var config = YAHOO.lang.JSON.parse(res);
							if (config.component == undefined || config.component.items == undefined || config.component.items.item == undefined) {
								alert(property.value + " is not a component or does not have items.");
							} else {
								var items = config.component.items.item;
								if (items.length == undefined) {
									items = [ items ];
								}
								_self.list = items;

								for(var j=0; j<_self.callbacks.length; j++) {
									_self.callbacks[j].success(items);
								}
							}
						},
						failure: function() {
							alert("The system was unable to load " + property.value + ".");
						}
					}; // end of getItemCallBack

					YConnect.asyncRequest('GET', getItemServiceUrl, getItemCallBack);
				}, // end of success
				failure: function(err) {
				}
			}; // end of getContextIdCallback

			// get the context id from preview store
			YConnect.asyncRequest('GET', this.getContextIdUrl, getContextIdCallback);
		}
	}
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.SiteComponent, CStudioForms.CStudioFormDatasource, {

	getContextIdUrl: "/api/1/site/context/id.json",
	getItemServiceUrl: "/api/1/content_store/descriptor.json",

    getLabel: function() {
        return CMgs.format(langBundle, "siteComponent");
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
                var value = JSON.parse(prop.value); 
                value.forEach( function(opt) {
                    if (opt.selected) {
                        val = opt.value;
                    }
                });
            }
        });
        return val;
    },

	getName: function() {
		return "site-component";
	},
	
	getSupportedProperties: function() {
		return [{
			label: CMgs.format(langBundle, "dataType"),
			name: "dataType",
			type: "dropdown",
			defaultValue: [{ // Update this array if the dropdown options need to be updated
				value: "value",
				label: "",
				selected: true
			}, {
				value: "value_s",
				label: CMgs.format(langBundle, "string"),
				selected: false
			}, {
				value: "value_i",
				label: CMgs.format(langBundle, "integer"),
				selected: false
			}, {
				value: "value_f",
				label: CMgs.format(langBundle, "float"),
				selected: false
			}, {
				value: "value_dt",
				label: CMgs.format(langBundle, "date"),
				selected: false
			}, {
				value: "value_html",
				label: CMgs.format(langBundle, "HTML"),
				selected: false
			}]
		}, {
			label: CMgs.format(langBundle, "componentPath"),
			name: "componentPath",
			type: "string"
		}];
	},	

	getSupportedConstraints: function() {
		return [
			{ label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" }
		];
	},
	
	getList: function(cb) {
		if(!this.list) {
			this.callbacks[this.callbacks.length] = cb;
		}
		else {
			cb.success(this.list);
		}
	}
	

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-site-component", CStudioForms.Datasources.SiteComponent);