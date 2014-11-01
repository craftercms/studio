CStudioAdminConsole.Tool.ContentTypes.PropertyType.Date = CStudioAdminConsole.Tool.ContentTypes.PropertyType.Date || function(fieldName, containerEl)  {
	this.fieldName = fieldName;
	this.containerEl = containerEl;
	this.defaulValue = {
		show : true,
		option : []		// an array with the names of the options enabled
	};
	this.value;
	this.options = {
		"control" : { "id" : "link",
					  "group" : "link",	// important for radio buttons; otherwise, it should have the same value as id
					  "type" : "checkbox",
					  "label" :	"Add link to set field to current date" }
	};
	this.dom;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.Date, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
	render: function(value, updateFn) {
		var _self = this,
			wrapperEl = document.createElement("div"),
			optionsEl = document.createElement("ul"),
			showEl = document.createElement("input"),
			liArr;

		/* @param el : The field container */
		var updateValue = function (el) {
			var showEl = YAHOO.util.Selector.filter(el.children, "[type='checkbox']")[0],
				options = YAHOO.util.Selector.query("[type='checkbox']", optionsEl),
				selected = [];

			for (var i = options.length - 1; i >= 0; i--) {
				if (options[i].checked) {
					selected.push(options[i].value);
				}
			};
			// Update value
			_self.setValue({ "show" : showEl.checked, 
						     "option" : selected });
		};

		var updateField = function (evt, el) {
			updateValue(el);
			updateFn(evt, _self);
			CStudioAdminConsole.Tool.ContentTypes.visualization.render();
		};

		var toggleUpdateField = function (evt, el) {
			var target = YAHOO.util.Event.getTarget(evt),
				options,
				defaultOptions = this.defaulValue.option;

			if (target) {
				if (target.checked) {
					YAHOO.util.Dom.removeClass(optionsEl, "hidden");
				} else {
					YAHOO.util.Dom.addClass(optionsEl, "hidden");
					// When the options are hidden, reset the options to the default values
					options = YAHOO.util.Selector.query("[type='checkbox']", optionsEl);
					options.forEach( function(el) {
						if (defaultOptions.indexOf(el.id) >= 0) {
							options[0].checked = true;
						} else {
							options[0].checked = false;
						}
					});
				}
			}
			updateField(evt, el);
		};

		var populateOptions = function (container, opts, selected, show) {
			var html = "";
			for (var option in opts) {
				if (opts[option].type == "checkbox") {
					html += "<li>" +
							"<input type='" + opts[option].type + "' name='" + _self.fieldName + "-" + opts[option].group + "' value='" + opts[option].id + 
							"' " + ((selected.indexOf(opts[option].id) >= 0) ? "checked='true'" : "") + " id='" + _self.fieldName + "-" + opts[option].id + "' />" +
							"<label for='" + _self.fieldName + "-" + opts[option].id + "'>" + opts[option].label + "</label>" + 
						"</li>";	
				}
			}
			if (!show) {
				YAHOO.util.Dom.addClass(container, "hidden");
			}
			container.innerHTML = html;
			return container;
		};

		// Set the instance's value:
		// If the value hasn't been set it should come as an empty string, in which case use the type's default value
		// The value may also come as a string -true/false- instead of a JSON object so this check is also for backwards compatibility
		if (typeof value == "object") {
			this.setValue(value);
		} else if (typeof value == "string") {
			try {
				this.setValue(JSON.parse(value));
			} catch (e) {
				this.setValue(this.defaulValue);
			}
		} else {
			this.setValue(this.defaulValue);
		}	

		optionsEl.className = "date-options";
		showEl.type = "checkbox";
		showEl.checked = this.isShown();
		showEl.fieldName = this.fieldName;

		wrapperEl.appendChild(showEl);
		wrapperEl.appendChild(populateOptions(optionsEl, this.options, this.getValue().option, this.isShown()));
		this.containerEl.appendChild(wrapperEl);

		this.dom = wrapperEl;
		
		// Subscriptions
		if (updateFn) {
			YAHOO.util.Event.on(showEl, 'change', toggleUpdateField, wrapperEl, this);

			liArr = YAHOO.util.Selector.query("[type='checkbox']", wrapperEl);
			liArr.forEach( function(el) {
				YAHOO.util.Event.on(el, 'change', updateField, wrapperEl);
			});
		}
	},

	isShown: function () {
		return this.value.show;
	},
	
	getValue: function() {
		return this.value;	
	},

	setValue: function (val) {
		if (typeof val == "object") {
			this.value = val;
		} else if (typeof val == "boolean") {
			// for backwards compatibility
			this.value = {};
			this.value.show = val;
			this.value.option = this.defaulValue.option;
		}
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-date", CStudioAdminConsole.Tool.ContentTypes.PropertyType.Date);