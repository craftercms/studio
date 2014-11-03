CStudioAdminConsole.Tool.ContentTypes.PropertyType.Range = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Range ||  function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	this.fieldValue =  { exact: "", min:"", max:""};
	
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Range, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var _self = this;
		var isExact = false;
			if(value){
                try{
				    var obj =  (typeof value == "string") ? eval("(" + value + ")") : value;
				
				    if(typeof obj == 'number'){
					    isExact = true;
					    this.fieldValue["exact"] = value;
				    }else{
                        if( !Array.isArray(obj) ){
                            if(obj["exact"] && obj["exact"] != "") {
                                isExact = true;
                                this.fieldValue["exact"] = obj["exact"];
                            }
                            else {
                                this.fieldValue["min"] = obj["min"];
                                this.fieldValue["max"] = obj["max"];
                            }
                        } else{
                            isExact = true;
                        }
                    }
                }catch(err){

                }
			}
			
		var containerEl = this.containerEl;
			YDom.addClass(containerEl, "range");
			
		var labelEl = YDom.getFirstChild(containerEl);
			
		var switchCtrl = document.createElement("a");
			YDom.addClass(switchCtrl, "switch-icon");
			switchCtrl.innerHTML = "&nbsp;";
			labelEl.appendChild(switchCtrl);
			
		var ctrlsContainerEl = document.createElement("div");
		    YDom.addClass(ctrlsContainerEl, "type-range");
	
		var exactContainerEl = 	document.createElement("div");
			YDom.addClass(exactContainerEl, "exact-value");
			if(!isExact){
				YDom.addClass(exactContainerEl, "hide");
			}
		var exactValEl = this.createControl("exact", updateFn);
			exactContainerEl.appendChild(exactValEl);
		
		var rangeContainerEl = 	document.createElement("div");
			YDom.addClass(rangeContainerEl, "range-value");
			if(isExact){
				YDom.addClass(rangeContainerEl, "hide");
			}
		var minValEl = this.createControl("min", updateFn);
		var maxValEl = this.createControl("max", updateFn);
			YDom.addClass(maxValEl, "last");
			rangeContainerEl.appendChild(minValEl);
			rangeContainerEl.appendChild(maxValEl);
			
			ctrlsContainerEl.appendChild(exactContainerEl);
			ctrlsContainerEl.appendChild(rangeContainerEl);
			containerEl.appendChild(ctrlsContainerEl);
			
		var switchFn = function(evt, el){
				if( YDom.hasClass(exactContainerEl, "hide") ){
					minValEl.resetValue();
					maxValEl.resetValue();
					YDom.removeClass(exactContainerEl, "hide");
					YDom.addClass(rangeContainerEl,"hide");
				}
				else{
					exactValEl.resetValue();
					YDom.removeClass(rangeContainerEl, "hide");
					YDom.addClass(exactContainerEl,"hide");
				}

                updateFn(null, { fieldName: _self.fieldName, value: _self.valueToJsonString(_self.fieldValue) });
			};
		
			YAHOO.util.Event.on(switchCtrl, "click", switchFn, switchCtrl);

            //Update the model with the same value but with the correct format ( see valueToJsonString )
            updateFn(null, { fieldName: this.fieldName, value: this.valueToJsonString(this.fieldValue) });
	},
	
	createControl: function(label, updateFn){
		var _self = this;
		
		var valueEl = document.createElement("div");
			YDom.addClass(valueEl, "value");
			
		var valEl = document.createElement("input");
		
		var spanEl = document.createElement("span");
			spanEl.innerHTML  = label;
			
			valueEl.appendChild(valEl);
			valueEl.appendChild(spanEl);
			
			valEl.value = this.fieldValue[label];
			
		var validFn = function(evt, el) {
				if (evt && evt != null) {
					var charCode = (evt.which) ? evt.which : event.keyCode

					if(!_self.isNumberKey(charCode)) {
			          	if(evt)
			          		YAHOO.util.Event.stopEvent(evt);			
						}
					}
			};

		var hideFn = function(evt, el){
				var spanEl = YDom.getNextSibling(el);
					YDom.addClass(spanEl,"hide");
			};

		var showFn = function(evt, el){
				var spanEl = YDom.getNextSibling(el);
					YDom.removeClass(spanEl,"hide");
					
					_self.fieldValue[label] = el.value;
					updateFn(null, { fieldName: _self.fieldName, value: _self.valueToJsonString(_self.fieldValue) });
			};
			
			YAHOO.util.Event.on(valEl, 'keydown', validFn, valEl);
			YAHOO.util.Event.on(valEl, 'focus', hideFn, valEl);
			YAHOO.util.Event.on(valEl, 'blur', showFn, valEl);
			
			valueEl.resetValue =  function(){
				_self.fieldValue[label] = "";
				valEl.value = "";
			}
			
		return valueEl;
	},
	
	valueToJsonString: function(value) {
		var strValue = "";
		
		strValue = "{ \"exact\":\"" + value["exact"] + "\", ";
		strValue += "\"min\":\"" + value["min"] + "\", ";	
		strValue += "\"max\":\"" + value["max"] + "\" }";
		
		return strValue;
	},
	
	getValue: function() {
		return this.valueEl.value;	
	},

	isNumberKey: function(charCode) {
		return !(charCode != 43 && charCode > 31 && (charCode < 48 || charCode > 57));
	}
	
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-range", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Range);
