CStudioAdminConsole.Tool.ContentTypes.PropertyType.String = CStudioAdminConsole.Tool.ContentTypes.PropertyType.String ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.String, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
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
                        idDatasource.value = this.value.replace(/ /g,"-").toLowerCase();
                        updateFn(event, idDatasource);
                    }
                }
                CStudioAdminConsole.Tool.ContentTypes.visualization.render();
            };

            /*var testFn = function(event, el) {
                console.log('test');
                updateFn(event, el);
                CStudioAdminConsole.Tool.ContentTypes.visualization.render();
            };*/
			
			YAHOO.util.Event.on(valueEl, 'keyup', updateFieldFn, valueEl);
            //YAHOO.util.Event.on(valueEl, 'change', testFn, valueEl);
		}
		
		this.valueEl = valueEl;
	},
	
	getValue: function() {
		return this.valueEl.value;	
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-string", CStudioAdminConsole.Tool.ContentTypes.PropertyType.String);