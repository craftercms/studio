CStudioForms.Controls.DateTime = CStudioForms.Controls.DateTime ||  
function(id, form, owner, properties, constraints, readonly)  {
	this.owner = owner;
	this.owner.registerField(this);
	this.errors = []; 
	this.properties = properties;
	this.constraints = constraints;
	this.dateEl = null;
	this.countEl = null;
	this.required = false;
	this.value = "_not-set";
	this.form = form;
	this.id = id;
	this.readonly = readonly;
	this.showTime = false;
	this.showDate = false;
	this.showNowLink = false;
	this.populate = false;
	this.timezone = "";
	this.allowPastDate = false;
	this.startDateTimeObj;		// Object storing the time when the form was loaded; will be used to adjust startTzDateTimeStr before the form is saved
	this.startTzDateTimeStr;	// Time the form was loaded (adjusted to the site's timezone)
	
	return this;
}

YAHOO.extend(CStudioForms.Controls.DateTime, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return CMgs.format(langBundle, "dateTime");
    },

	validate: function(evt, obj, dateCheck) {
		var dateValue = (obj.showDate) ? obj.dateEl.value : "",
			timeValue = (obj.showTime) ? obj.timeEl.value : "",
			valid;

		if (obj.required) {
			if ((obj.showDate && dateValue == "") || 
				(obj.showTime && timeValue == "")) {
				obj.setError("required", "Field is Required");
				obj.renderValidation(true);
				valid = false;
			} else {
				obj.clearError("required");
				obj.renderValidation(true);
				valid = true;
			}
		} else {
			// Date check: if show and date fields are present, the time value is populated, 
			// but the date value isn't, then give an error.  
			if (dateCheck && obj.showDate && obj.showTime && timeValue != "" && dateValue == "") {
				obj.displayMessage("Date field must be filled in.", "date-required", "warning");
				obj.setError("required", "Field is Required");
				obj.renderValidation(true);
				valid = false;
			}
			// Date check: if show and date fields are present, and the date value is populated, 
			// OR if show and date fields are present, and the date and the time values are empty
			// then clear any previos errors
			else if (dateCheck && obj.showDate && obj.showTime && dateValue != "" || 
					  dateCheck && obj.showDate && obj.showTime && timeValue == "" && dateValue == "") {
				obj.removeMessage("date-required");
				obj.clearError("required");
				obj.renderValidation(false);
				valid = true;
			} else {
				valid = true;
			}
		}
		return valid;
	},

    _onChangeVal: function(evt, obj) {
        obj.edited = true;
    },

	// Get the UTC date representation for what is currently in the UI fields (date/time)
	// Returns a date/time value in a string (see getConvertFormat for value format)
	getFieldValue: function () {
		var dateValue = (this.showDate) ? this.dateEl.value : "",
			timeValue = (this.showTime) ? this.timeEl.value : "",
			now = new Date(),
			nowObj = this.getDateTimeObject(now),
			dateVal, timeVal, res, val;

		if (this.validate(null, this)) {

			// If dateValue == "", then it must be because only the date field is 
			// displayed; otherwise, validation should not have allowed the user get this far 
			dateVal = (dateValue != "") ? dateValue : nowObj.date;

			if (timeValue != "") {
				var timeVals = timeValue.split(' ');
				var isPm = (timeVals[1] == 'p.m.') ? true : false;
				var timeFields = timeVals[0].split(':');
				var hh = parseInt(timeFields[0], 10);
				var mi = timeFields[1];
				var ss = timeFields[2];
				hh = (isPm && hh != 12) ? hh + 12 : ((!isPm && hh == 12) ? 0 : hh);
                var hpad = (hh<10)?"0":"";
				timeVal = hpad + hh + ":" + mi + ":" + ss;
			} else {
				timeVal = "";	// Default time value if the user doesn't populate the time field
			}

			if (this.showDate && dateValue != "" ||
				this.showTime && !this.showDate && timeValue != "") {

				res = this.convertDateTimeSync(dateVal, timeVal, this.timezone, "GMT");
	            val = eval("(" + (res.responseText) + ")");

	            if (res.status == 200 && val.convertedTimezone) {
	            	res = val.convertedTimezone.split(" ");
	            	return res[0] + " " + res[1];

	            } else {
	            	return false;
	            }
			} else {
				return "";	// The date/time fields are empty
			}
		} 
		// If the form doesn't validate, it should trigger errors when the fields are blurred so 
		// in theory, it should never reach this point (because this function -getFieldValue- should be called on beforeSave).
		return false;
	},

	/**
	 * perform count calculation on keypress
	 * @param evt event
	 * @param el element
	 */
	count: function(evt, countEl, el) {
    },

    updateDate: function(type, args, calendarObj) {
    	var dates = args[0];
		var date = dates[0];
		var divPrefix = this.id + '-';

	    var calEl = document.getElementById(divPrefix + 'calendarContainer');

        var mm = (date[1] < 10)?"0" + date[1]:date[1];
        var dd = (date[2] < 10)?"0" + date[2]:date[2];
        var yyyy = date[0];

        this.setDateTime(mm + '/' + dd + '/' + yyyy, 'date');
        calendarObj.hide();
        this._onChangeVal(null, this)
	},
	
	createServiceUri: function(time, srcTimezone, destTimezone, dateFormat){
		var baseUrl = CStudioAuthoringContext.authoringAppBaseUri;
		var serviceUrl = "/api/1/services/util/time/convert-time.json?";
		var url = baseUrl;
		url += serviceUrl;
		url += "time=" + time;
		url += "&srcTimezone=" + srcTimezone;
		url += "&destTimezone="  + destTimezone;
		url += "&dateFormat=" + dateFormat;
		
		return url;
	},

	// Get the date/time formatting for the time converting service
	getConvertFormat: function (includeDate) {
		var format = (includeDate) ? "MM/dd/yyyy%20HH:mm:ss" : "HH:mm:ss";
		return format;
	},

	// Get a date/time string to use with the time converting service
	getDateTimeString: function (date, time) {
		// There should always be a time value or else, we risk calculating the date value incorrectly; but, just in case ...
		var dateTimeStr = (date) ? date + ((time) ? "%20" + time : "%2000:00:00") :
							       "" + time;
		return dateTimeStr;
	},
	
	// TO-DO: improvement
	// Currently this is making a synchronous call to get the UTC representation of a date. The size of the transfer of 
	// information made through this call is small so it shouldn't affect UX considerably. This call is synchronous because 
	// we want to store the UTC representation of a date before the form closes. The form engine offers the possibility to 
	// register "beforeSave" callbacks, but these are assumed to be synchronous (forms-engine.js, onBeforeSave method)
	convertDateTimeSync: function(date, time, srcTimezone, destTimezone, callback) {
		var xhrObj;

		var format = this.getConvertFormat(date),
			convertString = this.getDateTimeString(date, time);

		var service = this.createServiceUri(convertString, srcTimezone, destTimezone, format);
		YAHOO.util.Connect.initHeader("Content-Type", "application/json; charset=utf-8");
		// YAHOO.util.Connect.asyncRequest('GET',service, callback);

		var xhrObj = YAHOO.util.Connect.createXhrObject();
		xhrObj.conn.open("GET", service, false);
		xhrObj.conn.send(null);
		return xhrObj.conn;
	},

	convertDateTime: function(date, time, srcTimezone, destTimezone, callback){
		var format = this.getConvertFormat(date),
			convertString = this.getDateTimeString(date, time);
		
		var service = this.createServiceUri(convertString, srcTimezone, destTimezone, format);
		YAHOO.util.Connect.initHeader("Content-Type", "application/json; charset=utf-8");
		YAHOO.util.Connect.asyncRequest('GET', service, callback);
	},

	// set the timestamp and format for the output
	setTimeStamp : function (timeStamp, timeFormat) {
		return this._padAZero(timeStamp.getHours())
				+ ':'
				+ this._padAZero(timeStamp.getMinutes())
				+ ':'
				+ this._padAZero(timeStamp.getSeconds())
				+ ' '
				+ timeFormat;
	},

	updateTime : function(evt, param) {

		//patterns to match the time format
		var timeParsePatterns = [
			// Now
			{
				re: /^now/i,
				example: new Array('now'),
				handler: function() {
					return new Date();
				}
			},
			// p.m.
			{
				re: /(\d{1,2}):(\d{1,2}):(\d{1,2})(?:p| p)/,
				example: new Array('9:55:00 pm', '12:55:00 p.m.', '9:55:00 p', '11:5:10pm', '9:5:1p'),
				handler: function(bits) {
					var d = new Date();
					var h = parseInt(bits[1], 10);
					d.setHours(h);
					d.setMinutes(parseInt(bits[2], 10));
					d.setSeconds(parseInt(bits[3], 10));
					return d + "~p.m.";
				}
			},
			// p.m., no seconds
			{
				re: /(\d{1,2}):(\d{1,2})(?:p| p)/,
				example: new Array('9:55 pm', '12:55 p.m.', '9:55 p', '11:5pm', '9:5p'),
				handler: function(bits) {
					var d = new Date();
					var h = parseInt(bits[1], 10);
					d.setHours(h);
					d.setMinutes(parseInt(bits[2], 10));
					d.setSeconds(0);
					return d + "~p.m.";
				}
			},
			// p.m., hour only
			{
				re: /(\d{1,2})(?:p| p)/,
				example: new Array('9 pm', '12 p.m.', '9 p', '11pm', '9p'),
				handler: function(bits) {
					var d = new Date();
					var h = parseInt(bits[1], 10);
					d.setHours(h);
					d.setMinutes(0);
					d.setSeconds(0);
					return d + "~p.m.";
				}
			},
			// hh:mm:ss
			{
				re: /(\d{1,2}):(\d{1,2}):(\d{1,2})/,
				example: new Array('9:55:00', '19:55:00', '19:5:10', '9:5:1', '9:55:00 a.m.', '11:55:00a'),
				handler: function(bits) {
					var d = new Date();
					var h = parseInt(bits[1], 10);
					if (h == 12) {
						//h = 0;
					}
					d.setHours(h);
					d.setMinutes(parseInt(bits[2], 10));
					d.setSeconds(parseInt(bits[3], 10));
					return d + "~a.m.";
				}
			},
			// hh:mm
			{
				re: /(\d{1,2}):(\d{1,2})/,
				example: new Array('9:55', '19:55', '19:5', '9:55 a.m.', '11:55a'),
				handler: function(bits) {
					var d = new Date();
					var h = parseInt(bits[1], 10);
					if (h == 12) {
						//h = 0;
					}
					d.setHours(h);
					d.setMinutes(parseInt(bits[2], 10));
					d.setSeconds(0);
					return d + "~a.m.";
				}
			},
			// hhmmss
			{
				re: /(\d{1,6})/,
				example: new Array('9', '9a', '9am', '19', '1950', '195510', '0955'),
				handler: function(bits) {
					var d = new Date();
					var h = bits[1].substring(0, 2)
					var m = parseInt(bits[1].substring(2, 4), 10);
					var s = parseInt(bits[1].substring(4, 6), 10);
					if (isNaN(m)) {
						m = 0;
					}
					if (isNaN(s)) {
						s = 0;
					}
					if (h == 12) {
						//h = 0;
					}
					d.setHours(parseInt(h, 10));
					d.setMinutes(parseInt(m, 10));
					d.setSeconds(parseInt(s, 10));
					return d + "~a.m.";
				}
			}
		];
		
		//Parses a string to figure out the time it represents
		function parseTimeString(s) {
			for (var i = 0; i < timeParsePatterns.length; i++) {
				var re = timeParsePatterns[i].re;
				var handler = timeParsePatterns[i].handler;
				var bits = re.exec(s);
				if (bits) {
					return handler(bits);
				}
			}
		}

		//parse the value using patterns and retrive the date with format
		var inputTime = parseTimeString(this.timeEl.value);
		
		if(inputTime == undefined) {
			if(this.timeEl.value != ""){
			    alert('( '+this.timeEl.value+' ) is not a valid time format, please provide a valid time');
			}
			this.timeEl.value = "";
			this.setDateTime("", "time");
			return;
		} else {
			var finalTimeFormat = inputTime.split("~");
			var timeStamp = this.setTimeStamp.call(this, new Date(finalTimeFormat[0]), finalTimeFormat[1]);
			//Check for 12 hours format time
			var timeSplit = timeStamp.split(":");
			if (timeSplit.length == 3) {
				var hours = parseInt(timeSplit[0], 10);
				if (hours == 0 || hours > 12) {
					alert('( '+this.timeEl.value+' ) is not a valid time format, please provide a valid time');
					this.timeEl.focus();
					this.setDateTime("", "time");
					return;
				}
			}
			//set the value
			this.timeEl.value = timeStamp;
			this.setDateTime(timeStamp, "time");
		}
	},

	/**
	 * padd a zero if single digit found
	 */
	_padAZero : function (s) {
		s = s.toString();
		return (s.length == 1) ? '0' + s : s;
	},

	/**
	 * create timepicker increment and decrement helper
	 * that increse the input time
	 */
	
	textFieldTimeIncrementHelper : function(triggerEl, targetEl, event, keyCode) {

	    var incrementHandler = function(type, args) {

                  var timePicker = YDom.get(targetEl),
                      timeValue = timePicker.value,
                      cursorPosition;

                  if( timeValue != 'Time...' && timeValue != ''){
                      var timeValueArray = timeValue.split(/[: ]/),
                          hourValue = timeValueArray[0],
                          minuteValue = timeValueArray[1],
                          secondValue = timeValueArray[2],
                          amPmValue = timeValueArray[3];

                      cursorPosition = timePicker.getAttribute('data-cursor');

                      if( cursorPosition > -1 && cursorPosition < 3){

                          if(hourValue.charAt(0) == '0')
                              hourValue = hourValue.charAt(1);

                          hourValue = (parseInt(hourValue, 10)%12)+1;

                          if(hourValue.toString().length < 2)
                              hourValue =	"0"+hourValue;
                          else
                              hourValue = hourValue.toString();
                      }else if(cursorPosition > 2 && cursorPosition < 6){

                          if(minuteValue.charAt(0) == '0')
                              minuteValue = minuteValue.charAt(1);

                              if(parseInt(minuteValue, 10) == 59){
                                  minuteValue = (parseInt(minuteValue, 10)%59);
                              }else{
                                  minuteValue = (parseInt(minuteValue, 10)%59)+1;
                              }

                              if(minuteValue.toString().length < 2)
                                  minuteValue =	"0"+minuteValue;
                              else
                                  minuteValue = minuteValue.toString();

                      }else if(cursorPosition > 5 && cursorPosition < 9){
                          if(secondValue.charAt(0) == '0')
                              secondValue = secondValue.charAt(1);

                              if(parseInt(secondValue, 10) == 59){
                                  secondValue = (parseInt(secondValue, 10)%59);
                              }else{
                                  secondValue = (parseInt(secondValue, 10)%59)+1;
                              }

                              if(secondValue.toString().length < 2)
                                  secondValue =	"0"+secondValue;
                              else
                                  secondValue = secondValue.toString();
                      }else if(cursorPosition > 8){
                          amPmValue = (amPmValue == 'a.m.') ? 'p.m.' : 'a.m.';
                      }

                      timePicker.value = hourValue+":"+minuteValue+":"+secondValue+" "+amPmValue;
                  }
              };
						
		YEvent.addListener(triggerEl, event, incrementHandler);

		if (keyCode) {
		    // Add keyboard support, incomplete --CSTUDIO-401
		    klInc = new YAHOO.util.KeyListener(targetEl, { keys: keyCode}, incrementHandler);
		    klInc.enable();
		}
	},
	
	/**
	 * create timepicker decrement and decrement helper
	 * that decrese the input time
	 */
	textFieldTimeDecrementHelper : function(triggerEl, targetEl, event, keyCode) {

	    var decrementHandler = function(type, args) {

                  var timePicker = YDom.get(targetEl),
                         timeValue = timePicker.value,
                         cursorPosition;

                  if( timeValue != 'Time...' && timeValue != ''){
                      var timeValueArray = timeValue.split(/[: ]/),
                             hourValue = timeValueArray[0],
                             minuteValue = timeValueArray[1],
                             secondValue = timeValueArray[2],
                             amPmValue = timeValueArray[3];

                         cursorPosition = timePicker.getAttribute('data-cursor');

                      if( cursorPosition > -1 && cursorPosition < 3){

                          if(hourValue.charAt(0) == '0')
                              hourValue = hourValue.charAt(1);

                          if(parseInt(hourValue, 10) == 1){
                              hourValue = 12;
                          }else{
                              hourValue = (parseInt(hourValue, 10)-1)%12;
                          }

                          if(hourValue.toString().length < 2)
                              hourValue =	"0"+hourValue;
                          else
                              hourValue = hourValue.toString();
                      }else if(cursorPosition > 2 && cursorPosition < 6){

                          if(minuteValue.charAt(0) == '0')
                              minuteValue = minuteValue.charAt(1);

                              if(parseInt(minuteValue, 10) == 0){
                                  minuteValue = 59;
                              }else{
                                  minuteValue = (parseInt(minuteValue, 10)-1)%59;
                              }

                              if(minuteValue.toString().length < 2)
                                  minuteValue =	"0"+minuteValue;
                              else
                                  minuteValue = minuteValue.toString();

                      }else if(cursorPosition > 5 && cursorPosition < 9){
                          if(secondValue.charAt(0) == '0')
                              secondValue = secondValue.charAt(1);

                              if(parseInt(secondValue, 10) == 0){
                                  secondValue = 59;
                              }else{
                                  secondValue = (parseInt(secondValue, 10)-1)%59;
                              }

                              if(secondValue.toString().length < 2)
                                  secondValue =	"0"+secondValue;
                              else
                                  secondValue = secondValue.toString();
                      }else if(cursorPosition > 8){
                          if(amPmValue == 'a.m.')
                              amPmValue = 'p.m.';
                          else
                              amPmValue = 'a.m.';
                      }

                      timePicker.value = hourValue+":"+minuteValue+":"+secondValue+" "+amPmValue;
                  }
              };
						
		YEvent.addListener(triggerEl, event, decrementHandler);

              if (keyCode) {
                  // Add keyboard support, incomplete --CSTUDIO-401
                  klDec = new YAHOO.util.KeyListener(targetEl, { keys: keyCode}, decrementHandler);
                  klDec.enable();
              }
	},

	/*
	 * Renders a link that serves to populate an input element with a date value
	 * @param containerEl : DOM element that will contain the link
	 * @param label : Text value of the link
	 */
	_renderDateLink: function (containerEl, label) {
		var dl = document.createElement("a");

		dl.setAttribute("alt", "");
		dl.setAttribute("href", "#");
		dl.className = "date-link";
		dl.innerHTML = label;

		YAHOO.util.Event.on(dl, "click", function(e) {
			YAHOO.util.Event.preventDefault(e);

			var _self = this,
				nowObj = new Date(), cb;

			cb = {
				success: function (response) {
					var data = eval("(" + response.responseText + ")"),
						timezoneNow = data.convertedTimezone;

					timezoneNowObj = _self.getFormattedDateTimeObject(timezoneNow, true);
					_self.populateDateTime(timezoneNowObj, _self.dateEl, _self.timeEl, _self.showDate, _self.showTime);
					_self.validate(null, _self, true);
				},
				failure: function (response) {
					console.log("Unable to convert current date/time");
				}
			};
			this.getCurrentDateTime(nowObj, this.timezone, cb);
            this._onChangeVal(null, this);

		}, this, true);
		containerEl.appendChild(dl);
	},

	render: function(config, containerEl) {
		// we need to make the general layout of a control inherit from common
		// you should be able to override it -- but most of the time it wil be the same
		containerEl.id = this.id;

		var beforeSaveCb = {
			beforeSave: function(paramObj) {
				var _self = this.context;
				var val = _self.getFieldValue();
				if (typeof val == "string") {
					_self.value = val;
					_self.form.updateModel(_self.id, _self.value);
				} else {
					alert("Unable to save Date/Time field. Please contact your system administrator");
				}
			},
			context: this
		};
		this.form.registerBeforeSaveCallback(beforeSaveCb);

		for(var i=0; i<config.properties.length; i++) {
		    var prop = config.properties[i];
		
	        if(prop.name == "showTime" && prop.value == "true") {
		       this.showTime =  true;
	        }
	
	        if(prop.name == "showDate" && prop.value == "true") {
	        	this.showDate = true;
		    }

		     if(prop.name == "showNowLink" && prop.value == "true") {
	        	this.showNowLink = true;
		    }

		    if(prop.name == "populate" && prop.value == "true") {
		       this.populate = true;
		    }

	        if(prop.name == "allowPastDate" && prop.value == "true") {
		       this.allowPastDate = true;
		    }
		    				
		    if((prop.name == "readonly" && prop.value == "true") || 
		    	(prop.name == "readonlyEdit" && prop.value == "true" && window.location.search.indexOf("edit=true") >= 1)){
				this.readonly = true;
			}
	    }

		var today = new Date(),
		    dd = today.getDate(),
		    mm = today.getMonth()+1,
		    yyyy = today.getFullYear();

		var divPrefix = this.id + "-";

		var titleEl = document.createElement("span");

	        YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
		    titleEl.innerHTML = config.title;
		
		var controlWidgetContainerEl = document.createElement("div");
		    YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'date-time-container');
		    if (this.readonly) {
           		YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'read-only');
           	}

		var validEl = document.createElement("span");
			YAHOO.util.Dom.addClass(validEl, 'validation-hint');
		    YAHOO.util.Dom.addClass(validEl, 'cstudio-form-control-validation');
		    controlWidgetContainerEl.appendChild(validEl);
		
		    if(this.showDate) {
					var dateEl = document.createElement("input");

					dateEl.id = divPrefix + "cstudio-form-control-date-input";
					dateEl.className = "date-control";
					dateEl.readOnly = "readonly";
					this.dateEl = dateEl;
					YAHOO.util.Dom.addClass(dateEl, 'datum');
					YAHOO.util.Dom.addClass(dateEl, 'date');

					controlWidgetContainerEl.appendChild(dateEl);

				YAHOO.util.Event.on(dateEl, 'blur', function(e, _this) { 
					_this.validate(e, _this, true);
				}, this);

				if (this.readonly)	{
					dateEl.disabled = true;	
				}

                YAHOO.util.Event.on(dateEl, 'change',  this._onChangeVal, this);
            }
		
		    if (this.showTime) {
		    	var timeWrapper, timeEl, incrementControlEl, decrementControlEl, timezoneEl;

		    	timeWrapper = document.createElement("div");
		    	YAHOO.util.Dom.addClass(timeWrapper, 'time-container');

		      	timeEl = document.createElement("input");
		        timeEl.id = divPrefix + 'timepicker';
		        timeEl.className = "time-control";
		        timeEl.setAttribute("data-cursor", 0);
		        this.timeEl = timeEl;
		        YAHOO.util.Dom.addClass(timeEl, 'datum');
				YAHOO.util.Dom.addClass(timeEl, 'time');

	       		if (!this.readonly) {
	       			incrementControlEl = document.createElement("input");
		            incrementControlEl.type="button";
		            incrementControlEl.id=divPrefix + "timeIncrementButton";
		            incrementControlEl.className = "time-increment";
                    YAHOO.util.Event.on(incrementControlEl, 'click',  this._onChangeVal, this);

		       		decrementControlEl = document.createElement("input");
		            decrementControlEl.type="button";
		            decrementControlEl.id=divPrefix + "timeDecrementButton";
		            decrementControlEl.className = "time-decrement";
                    YAHOO.util.Event.on(decrementControlEl, 'click',  this._onChangeVal, this);

		            timeWrapper.appendChild(incrementControlEl);
		            timeWrapper.appendChild(decrementControlEl);
	       		}

	            timeWrapper.appendChild(timeEl);
	            controlWidgetContainerEl.appendChild(timeWrapper);		           
	           
	            if (this.readonly){
		           timeEl.disabled = true;
	            }

	            //Subscriptions
		       	YAHOO.util.Event.addListener(timeEl, 'blur', this.updateTime, this, true);
		     	
		     	YAHOO.util.Event.addListener(timeEl, 'click', function(e) {
		     		var caretPos = this.saveCaretPosition(timeEl);
		     		timeEl.setAttribute("data-cursor", caretPos);
				}, timeEl, this);

                YAHOO.util.Event.on(timeEl, 'change',  this._onChangeVal, this);

				YAHOO.util.Event.addListener(timeEl, 'keyup', function(e) {
					var caretPos = this.saveCaretPosition(timeEl);
		     		timeEl.setAttribute("data-cursor", caretPos);
				}, timeEl, this);

				if (!this.readonly) {
					this.textFieldTimeIncrementHelper(incrementControlEl.id, timeEl.id, 'click');
					this.textFieldTimeDecrementHelper(decrementControlEl.id, timeEl.id, 'click');
				}

				timezoneEl = document.createElement("span");
				timezoneEl.id = divPrefix + "timezoneCode";
				controlWidgetContainerEl.appendChild(timezoneEl);
		    }

		    if (this.showNowLink && !this.readonly) {
		    	// only show the link if the field is editable
				  this._renderDateLink(controlWidgetContainerEl, "Set Now");
		    }

		    if (!this.readonly) {
		    	// Render a link to clear the date and/or time values
		    	var clearDateEl = document.createElement("a"),
		            clearDateLabel = document.createTextNode("Clear");

      		clearDateEl.className = "clear-link";
      		clearDateEl.href = "#";
      		clearDateEl.appendChild(clearDateLabel);

      		YAHOO.util.Event.addListener(clearDateEl, 'click', function(e) {
      				YAHOO.util.Event.preventDefault(e);
                    this._onChangeVal(null, this);
			     		this.setDateTime('', 'date');
			     		this.setDateTime('', 'time');
					}, clearDateEl, this);
						
					controlWidgetContainerEl.appendChild(clearDateEl);
		    }

		this.renderHelp(config, controlWidgetContainerEl);

	    var descriptionEl = document.createElement("span");
	    	YAHOO.util.Dom.addClass(descriptionEl, 'description');
		    YAHOO.util.Dom.addClass(descriptionEl, 'cstudio-form-field-description');
		    descriptionEl.innerHTML = config.description;		
		
		var calEl = document.createElement("div");
		    calEl.id = divPrefix + "calendarContainer";
		    YAHOO.util.Dom.addClass(calEl, 'cstudio-form-field-calendar');
		    YAHOO.util.Dom.addClass(calEl, 'hidden');

		controlWidgetContainerEl.appendChild(calEl);
	    containerEl.appendChild(titleEl);
	    containerEl.appendChild(controlWidgetContainerEl);
	    containerEl.appendChild(descriptionEl);

	   	if (this.showDate) {
	      var calDivId = divPrefix + "date-time-id";
 	      var minDate = (this.allowPastDate == true) ? null : mm + '/' + dd + '/' + yyyy; // today's date
				var navConfig = {
					strings: {
						month: "Choose Month",
						year: "Enter Year",
						submit: "OK",
						cancel: "Cancel",
						invalidYear: "Please enter a valid year"
					},
					initialFocus: "year"
				};

	      var calendarComponent = new YAHOO.widget.Calendar(calDivId, calEl.id, {title: "Select Date", mindate: minDate, close:true, navigator: navConfig});
          calendarComponent.render();
          calendarComponent.hide();
          YAHOO.util.Dom.removeClass(calEl, "hidden");

          YAHOO.util.Event.addListener(dateEl, "click", function (e) {
          	this.show();
          }, calendarComponent, true);
          calendarComponent.selectEvent.subscribe(this.updateDate, calendarComponent, this);
       }
	},

	saveCaretPosition: function (inputEl) {		
		var iCaretPos = 0;

		// IE Support
		if (document.selection) {
			inputEl.focus();

			// To get cursor position, get empty selection range
			var oSel = document.selection.createRange();

			// Move selection start to 0 position
			oSel.moveStart ('character', -inputEl.value.length);

			// The caret position is selection length
			iCaretPos = oSel.text.length;
		}

		// Firefox/Chrome support
		else if (inputEl.selectionStart || inputEl.selectionStart == '0')
			iCaretPos = inputEl.selectionStart;

		return iCaretPos;
	},

	getValue: function() {
		return this.value;
	},

	getDateTimeObject: function (timeObj) {
		return {
			"date" : (timeObj.getUTCMonth() + 1) + "/" + timeObj.getUTCDate() + "/" + timeObj.getUTCFullYear(),
			"time" : timeObj.getUTCHours() + ":" + timeObj.getUTCMinutes() + ":" + timeObj.getUTCSeconds()
		}
	},

	// Parse a date/time string (of the form: "MM/dd/yyyy HH:mm:ss") and return an object with valid date and time values
	getFormattedDateTimeObject: function (datetimeStr, includeDate) {
		var values, timeVals, hh, mi, ss, a, h, hpad, dateObj;

		if (typeof datetimeStr == "string" && datetimeStr != "") {
			if (includeDate) {
				values = datetimeStr.split(' ');
				timeVals = values[1].split(":");
			} else {
				timeVals = datetimeStr.split(":");
			}
			hh = parseInt(timeVals[0], 10),
			mi = timeVals[1],
			ss = timeVals[2],
			a = (hh < 12) ? "a.m." : "p.m.",
			h = (hh > 12) ? hh - 12 : ((hh == 0) ? 12 : hh),
			hpad = (h < 10)?"0":"";

			dateObj = {
				"date" : (includeDate) ? values[0] : "",
				"time" : hpad + h + ":" + mi + ":" + ss + " " + a
			};
		} else {
			dateObj = {
				"date" : "",
				"time" : ""
			};
		}
		return dateObj;
	}, 

	displayMessage: function (msgStr, msgType, msgClass) {
		var msgContainer = YAHOO.util.Selector.query(".date-time-container", this.containerEl, true),
			msgExists = YAHOO.util.Selector.query("." + msgType, msgContainer, true),
			warningEl = document.createElement("div");

		if (msgContainer && !msgExists) {
			warningEl.className = msgClass + " " + msgType;
			warningEl.innerHTML = msgStr;
			msgContainer.appendChild(warningEl);
		}
	},

	removeMessage: function (msgType) {
		var msgContainer = YAHOO.util.Selector.query(".date-time-container", this.containerEl, true),
			msgExists = YAHOO.util.Selector.query("." + msgType, msgContainer, true);

		if (msgContainer && msgExists) {
			msgExists.parentNode.removeChild(msgExists);
		}
	},

	// Populates the date/time fields using the information from a dateObj
	populateDateTime: function (dateObj, dateEl, timeEl, showDate, showTime) {
		if(showDate && showTime){
			dateEl.value = dateObj.date;
			timeEl.value = dateObj.time;
		} else {
			if (showDate) {
				dateEl.value = dateObj.date;
		    } else if (showTime) {
				timeEl.value = dateObj.time;
			}
		}
	},

	routeDateTimePopulation: function(value){
		var _self = this,
			nowObj = this.getDateTimeObject(_self.startDateTimeObj),
			dateVal, timeVal, emptyDate, emptyTime, refDateVal, refTimeVal, dtValues, timezoneNowObj, cb;

		if (value != "" && value != "_not-set") {
			// If a value already exists for the date/time field, then convert this value (in UTC) to the site's timezone	
			cb = {
				success: function(response) {
					//Set date and time values in the UI
					var data = eval("(" + response.responseText + ")"),
						timezoneTime = data.convertedTimezone,
						tzDateTimeObj = _self.getFormattedDateTimeObject(timezoneTime, true),
						res, data, timeObj;

						if (_self.populate) {
							// Get the current date/time to fill the fields that are empty
							timezoneNowObj = _self.getFormattedDateTimeObject(_self.startTzDateTimeStr, true);
						}
						
						if (emptyDate) {
							// The time was calculated using refDateVal so the date field should really be 
							// blank or be populated with the current date; this is for backwards compatibility since
							// before it was possible to save only the date or only the time.
							tzDateTimeObj.date = (_self.populate) ? timezoneNowObj.date : "";
						}

						if (emptyTime) {
							// The date was calculated using refTimeVal so the time field should really be 
							// blank or be populated with the current date; this is for backwards compatibility since
							// before it was possible to save only the date or only the time.
							tzDateTimeObj.time = (_self.populate) ? timezoneNowObj.time : "";
						}

						_self.populateDateTime(tzDateTimeObj, _self.dateEl, _self.timeEl, _self.showDate, _self.showTime);
						// The date/time restored should be correct; however, the fields are validated so that the validation icon is rendered
						// in case the fields are required
						_self.validate(null, _self);
						// _self.displayTimezoneWarning(_self.startDateTimeObj, _self.startTzDateTimeStr);
				},
				failure: function(response) {
					console.log("Unable to convert stored date/time values");
					if(_self.dateEl){
						_self.dateEl.value = "";
					}
					if(_self.timeEl){
						_self.timeEl.value = "";
					}
				}
			};

			// All date values will be made up of 2 parts: date and time
			dtValues = value.split(" ");
			if (dtValues.length == 2) {
				// New method where value will always be made up of 2 parts
				dateVal = dtValues[0];
				timeVal = dtValues[1];
			} else {
				// Backwards compatibility
				// Previous method of storing values. This method allowed storing only the date or only the time. 
				// The problem with this is that if one of the fields was missing, the other one risked being 
				// calculated incorrectly.
				refDateVal = nowObj.date;
				refTimeVal = "00:00:00";

				if(this.showDate && this.showTime){
					dateVal = dtValues[0];
					timeVal = dtValues[1] ? dtValues[1] : refTimeVal;
					emptyDate = false;
					emptyTime = dtValues[1] ? false : true;
				} else if (this.showTime) {
					dateVal = refDateVal;
					timeVal = value;
					emptyDate = true;
					emptyTime = false;
				} else if (this.showDate) {
					dateVal = value;
					timeVal = refTimeVal;
					emptyDate = false;
					emptyTime = true;
				}
			}
		  	this.convertDateTime(dateVal, timeVal, "GMT", this.timezone, cb);
		} else {
			//No value exists yet
			if (this.populate) {
				// Populate it with the current time (see getCurrentDateTime)
				timezoneNowObj = this.getFormattedDateTimeObject(this.startTzDateTimeStr, true);
				this.populateDateTime(timezoneNowObj, this.dateEl, this.timeEl, this.showDate, this.showTime);
	    	}
     	   	this.validate(null, this);
     	   	// this.displayTimezoneWarning(this.startDateTimeObj, this.startTzDateTimeStr);
		}
	},

	getCurrentDateTime : function (now, configTimezone, callback) {
		var dtObj = this.getDateTimeObject(now);

		this.convertDateTime(dtObj.date, dtObj.time, "GMT", configTimezone, callback);
	},

	_setValue: function(value, configTimezone){
		var storedVal = value,
			nowObj = new Date(),
			cgTz = configTimezone,
			_self = this;

		var cb = {
			success: function (response) {
				var data = eval("(" + response.responseText + ")"),
					timezoneNow = data.convertedTimezone;
					_self.startTzDateTimeStr = timezoneNow;

				_self.routeDateTimePopulation(storedVal);
			},
			failure: function (response) {
				_self.startTzDateTimeStr = "";
				console.log("Unable to convert current date/time");
			}
		};
		this.startDateTimeObj = nowObj;
		this.getCurrentDateTime(nowObj, configTimezone, cb);
	},
	
	setValue: function(value) {
        this.edited = false;
		var timezoneCb = {
			context: this,
			
			success: function(config){
				var timezoneStr;
				this.context.timezone = config['default-timezone'];
				timezoneStr = this.context.timezone.substr(0, 3);
				if (this.context.showTime) {
					YDom.get(this.context.id + "-timezoneCode").innerHTML = timezoneStr;
				}
				this.context._setValue(value, this.context.timezone);
			},
				
			failure: function(){
			}
		}
		CStudioAuthoring.Service.lookupConfigurtion(CStudioAuthoringContext.site, "/site-config.xml", timezoneCb);	
	},

	setDateTime: function(value, type) {

		if (type == 'date' && this.dateEl) {
			this.dateEl.value = value;
		} else if (type == 'time' && this.timeEl) {
			this.timeEl.value = value;
		}
		this.validate(null, this, true);
	},

	getName: function() {
		return "date-time";
	},
	
	getSupportedProperties: function() {
		return [
		        { label: CMgs.format(langBundle, "showDate"), name: "showDate", type: "boolean", defaultValue: "true" },
				{ label: CMgs.format(langBundle, "showTime"), name: "showTime", type: "boolean" },
				{ label: CMgs.format(langBundle, "setNowLink"), name: "showNowLink", type: "boolean", defaultValue: "false" },
				{ label: CMgs.format(langBundle, "populated"), name: "populate", type: "boolean", defaultValue: "true" },
				{ label: CMgs.format(langBundle, "allowPastDate"), name: "allowPastDate", type: "boolean", defaultValue: "false" },
				{ label: CMgs.format(langBundle, "readonly"), name: "readonly", type: "boolean" },
				{ label: CMgs.format(langBundle, "readonlyOnEdit"), name: "readonlyEdit", type: "boolean", defaultValue: "false" }
			];
	},

	getSupportedConstraints: function() {
		return [
			{ label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" }
		];
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-date-time", CStudioForms.Controls.DateTime);