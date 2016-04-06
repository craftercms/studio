CStudioAdminConsole.Tool.ContentTypes.PropertyType.Variable = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Variable ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Variable, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn, fName) {
		var containerEl = this.containerEl;
		var valueEl = document.createElement("input");
        YAHOO.util.Dom.addClass(valueEl, "property-input-"+fName);
		containerEl.appendChild(valueEl);
		valueEl.value = value;
		valueEl.fieldName = this.fieldName;

		if(updateFn) {
			var updateFieldFn = function(event, el) {
                updateFn(event, el);
                if(YDom.hasClass(this,"property-input-title")){
                    var idDatasource = YDom.getElementsByClassName("property-input-name")[0] ? YDom.getElementsByClassName("property-input-name")[0] : YDom.getElementsByClassName("property-input-id")[0];
                    if(idDatasource){
                        idDatasource.value = this.value.replace(/[^A-Za-z0-9-_]/g,"").toLowerCase();
                        updateFn(event, idDatasource);
                    }
                }
                CStudioAdminConsole.Tool.ContentTypes.visualization.render();
            };

            var checkVarState = function(event, el) {
            	var titleEl = YDom.getElementsByClassName("property-input-title");
            	YAHOO.util.Event.removeListener(titleEl, 'keyup');

            	if(this.value == ""){
            		YAHOO.util.Event.on(titleEl, 'keyup', updateFieldFn, titleEl);
            	}
            }

			YAHOO.util.Event.on(valueEl, 'keyup', updateFieldFn, valueEl);

			if( (fName == "id" || fName == "name") && value !== "" ) {
				var titleEl = YDom.getElementsByClassName("property-input-title");
				YAHOO.util.Event.removeListener(titleEl, 'keyup');
				YAHOO.util.Event.on(valueEl, 'keyup', checkVarState);
			}
		}
		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return this.valueEl.value;	
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-variable", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Variable);