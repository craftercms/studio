//These global variables are needed in common-api.js
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var Element = YAHOO.util.Element;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.PublishDialog = CStudioAuthoring.Dialogs.PublishDialog || function() {
    
    this.dependencyJsonObj = null;
    this.selectedJsonObj = null;
    this.flatMap = null;
    this.anyoneSelected = true;
    this.itemArray = [];
    this.uncheckedItemsArray = [];
    this.dialog = null;
    this.elementThatClickedMiniScheduler = null;
    this.browserUriOfItemClickedInMiniScheduler = null;
    this.messagePanel = null;
};

CStudioAuthoring.Dialogs.PublishDialog.messagePanel = null;

CStudioAuthoring.Dialogs.PublishDialog.copy = {
        noPublishingChannels : {
            header : "No publishing channels are set",
            message : "Please set up a publishing channel to enable this feature."
        }
    }; 	
    
CStudioAuthoring.Dialogs.PublishDialog.getNoPublishingChannelsBody = function () {
      var copy = CStudioAuthoring.Dialogs.PublishDialog.copy.noPublishingChannels;
      return '<div class="message">' +
        '<div class="header">' + copy.header + '</div>' +
        '<p>' + copy.message + '</p>' +
        '<div class="acnSubmitButtons">' +
            '<input type="button" onClick="CStudioAuthoring.Dialogs.PublishDialog.messagePanel.hide();" value="OK" />' +
        '</div>' +
      '</div>';  
   };

//method for creating modal panel for scheduling policy. 
CStudioAuthoring.Dialogs.PublishDialog.createMessagePanel = function (panelName, modalState, zIdx) {
	return new YAHOO.widget.Panel(panelName, {
			fixedcenter : true,
        	visible : false,
        	close : false,
        	draggable : false,
        	underlay : "none",
        	modal : modalState,
        	zIndex : zIdx,
        	constraintoviewport : true,
        	autofillheight: null
	});
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.getTimeInJsonObject = function (jsonArray, browserUri) {
	for (var i = 0; i < jsonArray.length; ++i) {
		if (jsonArray[i].browserUri == browserUri) {
			if ((jsonArray[i].scheduled == true || jsonArray[i].now == false) &&
				 jsonArray[i].scheduledDate && jsonArray[i].scheduledDate != "") {
				return jsonArray[i].scheduledDate;
			} else {
				return "now";
			}
		} else { // check in children
			var jsonArrayChildrenLen = jsonArray[i].numOfChildren;
			for (var j = 0; j < jsonArrayChildrenLen; ++j) {
				if (jsonArray[i].children[j].browserUri == browserUri) {
					if ((jsonArray[i].children[j].scheduled == true || jsonArray[i].children[j].now == false) &&
						 jsonArray[i].children[j].scheduledDate && jsonArray[i].children[j].scheduledDate != "") {
						return jsonArray[i].children[j].scheduledDate;
					} else {
						return "now";
					}
				}
			}
		}
	}
	return "now";
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.setTimeConfiguration = function (scheduledDate) {
	if (scheduledDate) {
		var datePick = YDom.get('datepicker');
		var timePick = YDom.get('timepicker');
		datePick.value = "Date...";
		timePick.value = "Time...";
		if (scheduledDate == "now") {
			YDom.get('now').checked = true;
		} else {
			YDom.get('settime').checked = true;
			var shedSplit = scheduledDate.split("T");
			if(shedSplit.length == 2) {
				var dateArray = shedSplit[0].split("-");
				if (dateArray.length == 3) {
					datePick.value = (isNaN(dateArray[1])?dateArray[1]:parseInt(dateArray[1], 10)) + "/" +
									 (isNaN(dateArray[2])?dateArray[2]:parseInt(dateArray[2], 10)) + "/" + dateArray[0];
				}
				var timeArray = shedSplit[1].split(":");
				if (timeArray.length == 3) {
					var hours = (isNaN(timeArray[0])?timeArray[0]:parseInt(timeArray[0], 10));
					var mins  = (isNaN(timeArray[1])?timeArray[1]:parseInt(timeArray[1], 10));
					var secs  = (isNaN(timeArray[2])?timeArray[2]:parseInt(timeArray[2], 10));
					var hr = hours;
					if (hours == 0) {
					   hours = 12;
					} else if (hours > 12) {
					   hours = hours - 12;
					}
					var timeString = (hours<10?("0"+hours):hours) + ":" +
								 (mins<10?("0"+mins):mins) + ":" +
								 (secs<10?("0"+secs):secs);
					if (hr >= 12) {
						timePick.value = timeString + " p.m.";
					} else {
						timePick.value = timeString + " a.m.";
					}
				}
			}
		}
		this.toggleTimeSelection();
		YEvent.removeListener("now", "click", this.toggleTimeSelection);
		YEvent.addListener("now", "click", this.toggleTimeSelection);
		YEvent.removeListener("settime", "click", this.toggleTimeSelection);
		YEvent.addListener("settime", "click", this.toggleTimeSelection);
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.getScheduledDateTimeForJson = function (dateValue, timeValue) {    
	var dateValueArray = dateValue.split("/"),
		timeValueArray = timeValue.split(" "),
		timeSplit = timeValueArray[0].split(":");
	
	//converting am/pm to 24 hour time.    
	var hr = parseInt(timeSplit[0], 10);
	if (timeValueArray[1] == "a.m." || timeValueArray[1] == "AM"){
		if (hr == 12) {
			hr = 0;
		}
	} else if(timeValueArray[1] == "p.m." || timeValueArray[1] == "PM"){
		if (hr != 12) hr = hr + 12;
	}
	timeSplit[0] = hr;
	
	var schedDate = new Date(dateValueArray[2], dateValueArray[0] - 1, dateValueArray[1], timeSplit[0], timeSplit[1], timeSplit[2], "");
	var schedDateMonth = schedDate.getMonth() + 1;
	var scheduledDate = schedDate.getFullYear() + '-' + schedDateMonth + '-'
			+ schedDate.getDate() + 'T' + schedDate.getHours() + ':'
			+ schedDate.getMinutes() + ':' + schedDate.getSeconds();
	
	return scheduledDate;
};


CStudioAuthoring.Dialogs.PublishDialog.prototype.setTimeInJsonObject = function (jsonArray, jsonTime, browserUri) {
	for (var i = 0; i < jsonArray.length; ++i) {
		if (jsonArray[i].browserUri == browserUri) {
			if (jsonTime == 'now') {
				jsonArray[i].now = true;
				jsonArray[i].scheduledDate = "";
			} else {
				jsonArray[i].now = false;
				jsonArray[i].scheduledDate = jsonTime;
			}
		} else { // check in children
			var jsonArrayChildrenLen = jsonArray[i].numOfChildren;
			for (var j = 0; j < jsonArrayChildrenLen; ++j) {
				if (jsonArray[i].children[j].browserUri == browserUri) {
					if (jsonTime == 'now') {
						jsonArray[i].children[j].now = true;
						jsonArray[i].children[j].scheduledDate = "";
					} else {
						jsonArray[i].children[j].now = false;
						jsonArray[i].children[j].scheduledDate = jsonTime;
					}
				}
			}
		}
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.uncheckAll = function () {
	var inputs = document.getElementsByTagName('input');
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type == 'checkbox') { // uncheck the input checkboxes
			inputs[i].checked = false;
		}
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.reset = function () {
	// clear the arrays
	this.itemArray.splice(0, this.itemArray.length);
	this.uncheckedItemsArray.splice(0, this.uncheckedItemsArray.length);

	if (window.location.href.indexOf(CStudioAuthoringContext.previewAppBaseUri) < 0) {
		CStudioAuthoring.SelectedContent.init();
		CStudioAuthoring.ContextualNav.WcmActiveContent.drawNav();
		// remove checkboxes on dashboard
		this.uncheckAll();
	}
};  

CStudioAuthoring.Dialogs.PublishDialog.prototype.pageRedirect = function(response) {
	if (response && response.status == 401) {
		alert ("Authentication failed, redirecting to login page.");
		window.location.reload(true);
	}            
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.getJsonObject = function (ftlString) {
	// get json object from response and save in dependencyJsonObj
	var scriptLen = "<script>".length,
		scriptStr = ftlString,
		scriptStrIdx1 = scriptStr.indexOf("<script>"),
		scriptStrIdx2 = scriptStr.indexOf("<\/script>");
		
	return scriptStr.substring(scriptStrIdx1 + scriptLen, scriptStrIdx2);
};


CStudioAuthoring.Dialogs.PublishDialog.prototype.createItemMap = function() {
	var map = {},
		itemArray = this.dependencyJsonObj.items;
	
	var _populateMap = function(itemArray, map) {
		for (var i = 0; i < itemArray.length; i++) {
			var item = itemArray[i];
			map[item.uri] = item;
			if (item.children.length) {
				_populateMap(item.children, map);
			}
		}
	}
	_populateMap(itemArray, map);
	return map;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.removeScriptContent = function (htmlString) {
	var scriptOpenLen = "<script>".length,
		scriptCloseLen = "</script>".length,
		htmlStrIdx1 = htmlString.indexOf("<script>"),
		htmlStrIdx2 = htmlString.indexOf("<\/script>"),
		ftl1 = htmlString.substring(0, htmlStrIdx1),
		ftl2 = htmlString.substring(htmlStrIdx2 + scriptCloseLen);
		
	return ftl1.concat(ftl2);
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.ifExistsInUncheckedItemsArray = function(url) {
	var found = -1;
	for (var i = 0; i < this.uncheckedItemsArray.length; ++i) {
		if (this.uncheckedItemsArray[i] == url) {
			found = i;
			break;
		}
	}
	return found;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.updateUncheckedItemList = function(matchedElement, isChecked) {
	// walk the DOM to get the path
	// get parent of current element
	var parentTD = YDom.getAncestorByTagName(matchedElement, "td"),
		url = matchedElement.value;
	
	if (isChecked == false) { // add unchecked items to array
		// check if this item exists in uncheckedItemsArray
		if (this.ifExistsInUncheckedItemsArray(url) == -1) { //add only if item does not exist in array
			this.uncheckedItemsArray.push(url);
		}
	} else { //checked==true
		var found = this.ifExistsInUncheckedItemsArray(url);
		if (found != -1) {
			this.uncheckedItemsArray.splice(found, 1); // remove element
		}
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.checkReferencePages = function (refPages, browserUri) {
	for (var refIdx = 0; refIdx < refPages.length; refIdx++) {
		if (refPages[refIdx].browserUri == browserUri) {
			return true;
		}
	}
	return false;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.getChildren = function(parentItem) {
	var children = [];
	for (var key in this.flatMap) {
		var aItem = this.flatMap[key];
		if (aItem.mandatoryParent == parentItem.uri) {
			children.push(aItem);
		}
	}
	return children;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.checkAllChildren = function (parentElment) {
	for (var key in this.flatMap) {
		if (this.flatMap.hasOwnProperty(key)) {
			var itemJson = this.flatMap[key];
			if (itemJson && itemJson.parentPath && itemJson.parentPath != "") {
				var inputElement = YDom.get(key);
				var parentURI = "/site/website" + itemJson.parentPath + "/index.xml";
				if (parentURI) {
					var parentInputElement = YDom.get(parentURI);
					if (parentInputElement && parentInputElement == parentElment) {
						inputElement.checked = true;
						this.updateUncheckedItemList(inputElement, true);
						this.checkAllChildren(inputElement);
					}
				}
			}
		}
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.toggleTimeSelection = function () {
	var oSetTime = YDom.get("settime"),
		oDatePicker = YDom.get("datepicker"),
		oTimePicker = YDom.get("timepicker");
		
	if (oSetTime.checked) {
		oDatePicker.style.border = "1px solid #0176B1";
		oDatePicker.style.color = "#000000";
		oTimePicker.style.border = "1px solid #0176B1";
		oTimePicker.style.color = "#000000";
	} else {
		oDatePicker.style.border = "1px solid #C5D6E2";
		oDatePicker.style.color = "";
		oTimePicker.style.border = "1px solid #C5D6E2";
		oTimePicker.style.color = "";
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.setAllToNow = function (items) {
    this.setAllTo(items);
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.setAllTo = function (items, scheduledDate) {

    var isNow = false,
        children;

    if ('undefined' === typeof scheduledDate) {
        scheduledDate = '';
        isNow = true;
    }

    for( var i = 0, length = items.length;
         i < length;
         i++ ) {

        items[i].now = isNow;
        items[i].scheduledDate = scheduledDate;

        children = items[i].children;

        if (children && children.length > 0) {
            this.setAllTo(children, scheduledDate);
        }

    }

}

CStudioAuthoring.Dialogs.PublishDialog.prototype.changeToNow = function () {

	var items = YDom.getElementsBy(function(el) {
		return (el.getAttribute('title') == 'scheduledDate');
	}, 'a', YDom.getElementsByClassName("liveTable")[0]);

	for (var i in items) {
		items[i].innerHTML = 'Now';
	}

	this.dependencyJsonObj.now = "true";     // set now in json obj
	this.setAllToNow(this.dependencyJsonObj.items);

};

CStudioAuthoring.Dialogs.PublishDialog.prototype.clone_obj = function(obj) {
    var c = obj instanceof Array ? [] : {};
    for (var i in obj) {
        var prop = obj[i];
        if (typeof prop == 'object') {
            if (prop instanceof Array) {
                c[i] = [];

                for (var j = 0; j < prop.length; j++) {
                    if (typeof prop[j] != 'object') {
                        c[i].push(prop[j]);
                    } else {
                        c[i].push(this.clone_obj(prop[j]));
                    }
                }
            } else {
                c[i] = this.clone_obj(prop);
            }
        } else {
            c[i] = prop;
        }
    }
    return c;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.removeItem = function (jsonArray, browserUri) {
	for (var i = 0; i < jsonArray.length; i++) {
		var obj = jsonArray[i];
		if ('browserUri' in obj) {
			if (obj['browserUri'] == browserUri) {
				//push all child pages into selected list array,
				//so that, if any item selected iside the child items
				//will come into selected elements list
				if (obj['children'].length >= 1) {
					for (var chdIdx =0; chdIdx < obj['children'].length; chdIdx++) {
						jsonArray.push(obj['children'][chdIdx]);
					}
				}

				if (jsonArray.length == 1) {
					jsonArray.length = 0;  // make array of 0 length
					jsonArray.splice(i, 1); // remove element    
				} else {
					jsonArray.splice(i, 1); // remove element
				}
				break;
			}
		}
		if ('children' in obj) {
			this.removeItem(obj.children, browserUri);
		}
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.removeUncheckedItemsFromJson = function() {
	// the idea here is to resubmit the dependencyJsonObj with the unchecked items removed
	var uncheckedItems = this.uncheckedItemsArray,
		uncheckedItemsArrayLen = uncheckedItems.length;
		
	for (var i = 0; i < uncheckedItemsArrayLen; ++i) {
		this.removeItem(this.selectedJsonObj.items, uncheckedItems[i]);
	}

	return uncheckedItemsArrayLen;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.getItem = function (items, browserUri) {
	var retItem = null;
	for (var idx = 0; idx < items.length; idx++) {
		if (items[idx].browserUri == browserUri) {
			retItem = items[idx];
		} else if (items[idx].children && items[idx].children.length >= 1) {
			retItem = this.getItem(items[idx].children, browserUri)
		}

		if (retItem != null) {
			break;
		}
	}
	return retItem;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.parseToDate = function (jsonSchedDate) {
	var dateObj = 0;
	try {
		var shedSplit = jsonSchedDate.split("T");
		if(shedSplit.length == 2) {
			
			var dateArray = shedSplit[0].split("-"),
				timeArray = shedSplit[1].split(":");
				
			if (dateArray.length == 3 && timeArray.length == 3) {
				dateObj = new Date(parseInt(dateArray[0], 10),
								   (parseInt(dateArray[1], 10) - 1),
								   parseInt(dateArray[2], 10),
								   parseInt(timeArray[0], 10),
								   parseInt(timeArray[1], 10),
								   parseInt(timeArray[2], 10), 0);
			}
		}
	} catch (e) { }

	return dateObj;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.isReferencePage = function (items, browserUri) {
	var isRefPage = false;
	for (var itemIdx=0; !isRefPage && itemIdx<items.length; itemIdx++) {
		var itemJson = items[itemIdx];
		if (itemJson.pages && itemJson.pages.length >= 1) {
			for (var refIdx=0; refIdx < itemJson.pages.length; refIdx++) {
				if (itemJson.pages[refIdx].browserUri == browserUri) {
					isRefPage = true;
					break;
				}
			}
		}

		if (!isRefPage && itemJson.children && itemJson.children.length >= 1) {
			isRefPage = this.isReferencePage(itemJson.children, browserUri)
		}
	}
	return isRefPage;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.checkParentSchedule = function (items, browserUri, jsonSchedTime, isSubmittedForDeletion, isChildNodes) {
        
	var itemJson = this.getItem(items, browserUri),
		parItemJson = null,
		alertFlag = false,
		multipleChilds = false,
		multipleReferenceChilds = false,
		parentPageIntName = "",
		childPageIntName = "",
		referencePageIntName = "",
		childItem = null;
		
	if (isChildNodes) {
		var parSchedTime = itemJson.now? "now" : ((itemJson.scheduledDate == "") ? "now" : itemJson.scheduledDate);
		parentPageIntName = itemJson.internalName;
		if (itemJson && isSubmittedForDeletion) {
			//in submitted for delete case child items can not allow to set after date
			for (var itemIdx = 0; itemIdx < items.length; itemIdx++) {
				var chdItem = items[itemIdx];
				//check for child pages.
				var isChildPage = (chdItem.parentPath && chdItem.parentPath == itemJson.browserUri);
				if (isChildPage) {
					var chdSchedTime = chdItem.now? "now" : ((chdItem.scheduledDate == "") ? "now" : chdItem.scheduledDate),
						errorFlag = false;

					if (parSchedTime == "now" && chdSchedTime != "now") {
						alertFlag = errorFlag = true;
					} else if (parSchedTime != "now" && chdSchedTime != "now") {
						var childGoLiveDate = this.parseToDate(chdSchedTime),
							parentGoLiveDate = this.parseToDate(parSchedTime);
						if (parentGoLiveDate < childGoLiveDate) {
							alertFlag = errorFlag = true;
						}
					}

					if (errorFlag) {
						if (childPageIntName == "") {
							childPageIntName = chdItem.internalName;
						} else {
							if (!multipleChilds) {
								childPageIntName = "\n    - " + childPageIntName;
							}
							childPageIntName += "\n    - " + chdItem.internalName;
							multipleChilds = true;
						}
					}
				}
			}
		} else {
		//for flat second level child elements, we have to find parent-child, parent-reference relations.
		//finding child pages using mandatoryParent property of child page
			for (var itemIdx=0; itemIdx<items.length; itemIdx++) {
				var chdItem = items[itemIdx];
				//check for page references.
				//page reference can go-live before  parent go-live.
				var isRefPage = this.isReferencePage(items, chdItem.browserUri);
				var isChildPage = (!isRefPage && (chdItem.mandatoryParent && chdItem.mandatoryParent == itemJson.uri));
				var chdSchedTime = chdItem.now ? "now" : ((chdItem.scheduledDate=="") ? "now" : chdItem.scheduledDate);
				var errorFlag = false,
					referencePageError = false,
					childPageError = false;
					
				if (isRefPage && chdSchedTime != "now") {
					// check if current item depends on reference page or not
					if (chdItem.mandatoryParent && chdItem.mandatoryParent == itemJson.uri) {
						if (parSchedTime == "now") {
							alertFlag = errorFlag = referencePageError = true;
						} else {
							var childGoLiveDate = this.parseToDate(chdSchedTime),
								parentGoLiveDate = this.parseToDate(parSchedTime);
								
							if (childGoLiveDate > parentGoLiveDate) {
								alertFlag = errorFlag = referencePageError = true;
							}
						}
					}
				} else if (isChildPage && parSchedTime != "now") {
					if (chdSchedTime == "now") {
						alertFlag = errorFlag = childPageError = true;
					} else {
						var childGoLiveDate = this.parseToDate(chdSchedTime);
						var parentGoLiveDate = this.parseToDate(parSchedTime);
						
						if (parentGoLiveDate > childGoLiveDate) {
							alertFlag = errorFlag = childPageError = true;
						}
					}
				}
	
				if (errorFlag) {
					if (referencePageError) {
						if (referencePageIntName == "") {
							referencePageIntName = chdItem.internalName;
						} else {
							if (!multipleReferenceChilds) {
								referencePageIntName = "\n    - " + referencePageIntName;
							}
							referencePageIntName += "\n    - " + chdItem.internalName;
							multipleReferenceChilds = true;
						}
					} else if(childPageError) {
						if (childPageIntName == "") {
							childPageIntName = chdItem.internalName;
						} else {
							if (!multipleChilds) {
								childPageIntName = "\n    - " + childPageIntName;
							}
							childPageIntName += "\n    - " + chdItem.internalName;
							multipleChilds = true;
						}
					}
				}
			}
		}
	} else {
		if (itemJson && isSubmittedForDeletion) {
			parentPageIntName = itemJson.internalName;
			//in submitted for delete case child items can not allow to set after date
			if (itemJson.children && itemJson.children.length >= 1) {
				for (var chdIdx=0; chdIdx < itemJson.children.length; chdIdx++) {
					var chdItem = itemJson.children[chdIdx];
					var childSchedTime = chdItem.now ? "now" : ((chdItem.scheduledDate=="") ? "now" : chdItem.scheduledDate);
					var errorFlag = false;
					if (jsonSchedTime == "now" && childSchedTime != "now") {
						alertFlag = errorFlag = true;
					} else if (jsonSchedTime != "now" && childSchedTime != "now") {
						var childGoLiveDate = this.parseToDate(childSchedTime);
						var parentGoLiveDate = this.parseToDate(jsonSchedTime);
						if (parentGoLiveDate < childGoLiveDate) {
							alertFlag = errorFlag = true;
						}
					}

					if (errorFlag) {
						if (childPageIntName == "") {
							childPageIntName = chdItem.internalName;
						} else {
							if (!multipleChilds) {
								childPageIntName = "\n    - " + childPageIntName;
							}
							childPageIntName += "\n    - " + chdItem.internalName;
							multipleChilds = true;
						}
					}
				}
			} else {
				//in submitted for delete case child items can not allow to set after date
				for (var itemIdx = 0; itemIdx < items.length; itemIdx++) {
					var chdItem = items[itemIdx];
					//check for child pages.
					var isChildPage = (chdItem.parentPath && chdItem.parentPath == itemJson.browserUri);
					if (isChildPage) {
						var chdSchedTime = chdItem.now ? "now" : ((chdItem.scheduledDate=="") ? "now" : chdItem.scheduledDate);
						var errorFlag = false;

						if (parSchedTime == "now" && chdSchedTime != "now") {
							alertFlag = errorFlag = true;
						} else if (parSchedTime != "now" && chdSchedTime != "now") {
							var childGoLiveDate = this.parseToDate(chdSchedTime);
							var parentGoLiveDate = this.parseToDate(parSchedTime);
							if (parentGoLiveDate < childGoLiveDate) {
								alertFlag = errorFlag = true;
							}
						}

						if (errorFlag) {
							if (childPageIntName == "") {
								childPageIntName = chdItem.internalName;
							} else {
								if (!multipleChilds) {
									childPageIntName = "\n    - " + childPageIntName;
								}
								childPageIntName += "\n    - " + chdItem.internalName;
								multipleChilds = true;
							}
						}
					}
				}
			}
		} else if (itemJson && itemJson.newFile &&
				   itemJson.children && itemJson.children.length >= 1) {
					   
			parentPageIntName = itemJson.internalName;
			//this check is only for new parents
			for (var chdIdx=0; chdIdx < itemJson.children.length; chdIdx++) {
				var chdItem = itemJson.children[chdIdx];

				//check for page references.
				//page reference can go-live before  parent go-live.
				var isRefPage = this.isReferencePage(items, chdItem.browserUri);
				var parSchedTime = itemJson.now ? "now" : ((itemJson.scheduledDate == "") ? "now" : itemJson.scheduledDate);
				var chdSchedTime = chdItem.now ? "now" : ((chdItem.scheduledDate=="") ? "now" : chdItem.scheduledDate);
				var errorFlag = false,
					referencePageError = false,
					childPageError = false;
					
				if (isRefPage && chdSchedTime != "now") {
					// check if current item depends on reference page or not
					if (chdItem.mandatoryParent && chdItem.mandatoryParent == itemJson.uri) {
						if (parSchedTime == "now") {
							alertFlag = errorFlag = referencePageError = true;
						} else {
							var childGoLiveDate = this.parseToDate(chdSchedTime);
							var parentGoLiveDate = this.parseToDate(parSchedTime);
							if (childGoLiveDate > parentGoLiveDate) {
								alertFlag = errorFlag = referencePageError = true;
							}
						}
					}
				} else if (!isRefPage && parSchedTime != "now") {
					if (chdSchedTime == "now") {
						alertFlag = errorFlag = childPageError = true;
					} else {
						var childGoLiveDate = this.parseToDate(chdSchedTime);
						var parentGoLiveDate = this.parseToDate(parSchedTime);
						if (parentGoLiveDate > childGoLiveDate) {
							alertFlag = errorFlag = childPageError = true;
						}
					}
				}

				if (errorFlag) {
					if (referencePageError) {
						if (referencePageIntName == "") {
							referencePageIntName = chdItem.internalName;
						} else {
							if (!multipleReferenceChilds) {
								referencePageIntName = "\n    - " + referencePageIntName;
							}
							referencePageIntName += "\n    - " + chdItem.internalName;
							multipleReferenceChilds = true;
						}
					} else if(childPageError) {
						if (childPageIntName == "") {
							childPageIntName = chdItem.internalName;
						} else {
							if (!multipleChilds) {
								childPageIntName = "\n    - " + childPageIntName;
							}
							childPageIntName += "\n    - " + chdItem.internalName;
							multipleChilds = true;
						}
					}
				}
			}
		}
	}

	if (alertFlag) {
		if (isSubmittedForDeletion) {
			if (multipleChilds) {
				alert ("The parent page (" + parentPageIntName + ") cannot be deleted before the children pages below:" + childPageIntName);
			} else {
				alert ("The parent page (" + parentPageIntName + ") cannot be deleted before the child page (" +  childPageIntName + ").");
			}
		} else {
			var errorMsg = "";
			if (childPageIntName != "") {
				if (multipleChilds) {
					errorMsg = "The children below cannot Go Live before the parent page (" + parentPageIntName + ") goes live." + childPageIntName;
				} else {
					errorMsg = "The child page (" + childPageIntName + ") cannot Go Live before the parent page (" + parentPageIntName + ").";
				}
			}

			if (referencePageIntName != "") {
				if (errorMsg != "") { errorMsg += "\n\n"; }
				if (multipleReferenceChilds) {
					errorMsg += "Page (" + parentPageIntName + ") cannot Go Live without the reference pages below:" + referencePageIntName;
				} else {
					errorMsg += "Page (" + parentPageIntName + ") cannot Go Live without the reference page (" + referencePageIntName + ").";
				}
			}

			if (errorMsg != "") {
				alert(errorMsg);
			} else {
				alert("Invalid parent child scheduling.");
			}
		}
		return false;
	}
	
	return true;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.checkParentChildSchedules = function (items, isChildNodes) {
	for (var itemIdx=0; itemIdx < items.length; itemIdx++) {
		var item = items[itemIdx];
		var scheduledDate = "now";
		if (!item.now) {
			scheduledDate = item.scheduledDate;
		}

		if (item && !this.checkParentSchedule(items, item.browserUri, scheduledDate, item.submittedForDeletion, isChildNodes)) {
			return false;
		}

		if (item && item.children && item.children.length >= 1) {
			if (!this.checkParentChildSchedules(item.children, true)) {
				return false;
			}
		}
	}
	return true;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.appendPublishingChannelsData = function(containerObj) {
        
	var publishObj = {},
		statusObj = {},
		selectEl = document.getElementById('go-pub-channel'),
		statusRoot, inputArr, temp;

	if (selectEl.selectedIndex >= 0) {
		// Only set if a channel was picked; otherwise, leave as an empty object
		publishObj.index = selectEl.options[selectEl.selectedIndex].value.replace(/channel-/, "");
		publishObj.name = selectEl.options[selectEl.selectedIndex].text;
	}

	statusObj.channels = [];
	statusRoot = YDom.getElementsBy(function(el){
						 return YDom.hasClass(el, 'pub-status');
					 }, 'div', 'acnVersionWrapper');
	statusRoot = statusRoot[0];     // grab the first element of the returned array

	inputArr = YDom.getElementsBy(function(el){
					return el.checked == true;
				}, 'input', statusRoot);

	for (var i = 0; i < inputArr.length; i++) {
		statusObj.channels[i] = {};
		statusObj.channels[i].index = inputArr[i].id.replace("pub-status-", "");
		statusObj.channels[i].name = inputArr[i].value;
	}
	statusObj.message = document.getElementById('go-status-msg').value;

	containerObj.publishChannel = publishObj;
	containerObj.status = statusObj;
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.showLoadingImage = function(elementId) {
    var elem = YDom.get(elementId + "-loading");
    elem && (elem.style.display = "block");
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.hideLoadingImage = function(elementId) {
    var elem = YDom.get(elementId + "-loading");
    elem && (elem.style.display = "none");
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.setFocusOnDefaultButton = function(className) {
	//set foucs on OK button
	var oBtnDiv = YDom.getElementsByClassName("acnSubmitButtons")
	if (className) {
		oBtnDiv = YDom.getElementsByClassName(className);
	}

	if (oBtnDiv && oBtnDiv.length == 1) {
		var oBtnArray = oBtnDiv[0].getElementsByTagName("INPUT");
		if(oBtnArray && oBtnArray.length >= 1 && oBtnArray[0].type == "button") {
			if (oBtnArray[0].id == "") {
				oBtnArray[0].id = "acnOKButton";
			}
			CStudioAuthoring.Utils.setDefaultFocusOn(oBtnArray[0]);
		}
	}

	//check for in-valid inline styles
	var oConfirmDialog = this.dialog;
	if (oConfirmDialog && oConfirmDialog.body && oConfirmDialog.body.style.height != "") {
		oConfirmDialog.body.style.height = "";
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.publishingChannelsInit = function() {

	var TWITTER_MSG_LIMIT = 140,
		NUM_CHAR_EM = 10,       // At/below this number of characters, the number of characters left will be emphasized
		commonAncestor, twitterCheckBox, counterContainer, counterEl, counterTextEl, paramObj;

	// -- Helper Functions --
	// Updates the character counter and any classes related to its states
	var updateMsgLimit = function(event, obj) {

		var counterTextEl,
			numChars = document.getElementById(obj.msgElId).value.length;

		if (numChars <= TWITTER_MSG_LIMIT) {
			YDom.removeClass(obj.counterContainer, "over-limit");
			obj.counterTextEl.innerHTML = " characters available";
			numChars = TWITTER_MSG_LIMIT - numChars;    // Get the number of characters left
			obj.counterEl.innerHTML = numChars;
			if (numChars <= NUM_CHAR_EM) {
				YDom.addClass(obj.counterEl, "emphasize");
			} else {
				YDom.removeClass(obj.counterEl, "emphasize");
			}
		} else {
			YDom.addClass(obj.counterContainer, "over-limit");
			obj.counterEl.innerHTML = numChars - TWITTER_MSG_LIMIT;
			obj.counterTextEl.innerHTML = " characters over the maximum. Any characters over the maximum will not be posted in Twitter";

		}
	};

	// Controls the showing/hiding of the twitter message
	var toggleEl = function(event, obj) {

		if (YDom.hasClass(obj.counterContainer, "hidden")) {
			YDom.replaceClass(obj.counterContainer, "hidden", "show");
		}
		else {
			YDom.replaceClass(obj.counterContainer, "show", "hidden");
		}
	};

	// -- Event Handler Functions --
	// Sets event handler on the twitter checkbox
	var twitterToggleMessageInit = function(paramObj) {

		YEvent.on(paramObj.checkbox, "click", toggleEl, paramObj);
	};

	// Sets event handlers for the texarea
	var statusMessageInit = function(paramObj) {

		YEvent.on(paramObj.msgElId, "keypress", updateMsgLimit, paramObj);

		// Chrome and Safari do not support keypress on the backscape key :(
		// and Firefox doesn't seem to call keypress consistently so we'll make sure the counter is up to date
		// by calling the update function both onKeyUp and onKeyPress
		YEvent.on(paramObj.msgElId, "keyup", updateMsgLimit, paramObj);
	};

	// -- Setting of global scope variables --
	// common ancestor for both the twitter checkbox and the counter element
	commonAncestor = YDom.getElementsBy(function(el){
			return YDom.hasClass(el, 'pub-status');
		}, 'div', 'acnVersionWrapper');
	commonAncestor = commonAncestor[0];     // grab the first element of the returned array

	twitterCheckBox = YDom.getElementsBy(function(el){
		var attr = el.getAttribute('value');
		if (attr) {
			return attr.match(/twitter/i);
		}
		return false;
	}, 'input', commonAncestor);
	twitterCheckBox = (twitterCheckBox.length > 0) ? twitterCheckBox[0] : null;

	counterContainer = YDom.getElementsBy(function(el){
		return YDom.hasClass(el, 'counter');
	}, 'span', commonAncestor);
	counterContainer = (counterContainer.length > 0) ? counterContainer[0] : null;

	if (twitterCheckBox && counterContainer) {
		counterEl = YDom.getFirstChildBy(counterContainer, function(el){
			return el.tagName == "B";
		});
		counterTextEl = YDom.getFirstChildBy(counterContainer, function(el){
			return el.tagName == "SPAN";
		});
		paramObj = { msgElId : 'go-status-msg',
					 checkbox : twitterCheckBox,
					 counterContainer : counterContainer,
					 counterEl : counterEl,
					 counterTextEl : counterTextEl };

		twitterToggleMessageInit(paramObj);
		statusMessageInit(paramObj);
	}
};

CStudioAuthoring.Dialogs.PublishDialog.prototype.init = function() {
    this.dependencyJsonObj = null;
    this.selectedJsonObj = null;
    this.flatMap = null;
    this.anyoneSelected = true;
    this.itemArray = [];
    this.uncheckedItemsArray = [];
    this.dialog = null;
    this.elementThatClickedMiniScheduler = null;
    this.browserUriOfItemClickedInMiniScheduler = null;
};

CStudioAuthoring.Module.moduleLoaded("publish-dialog", CStudioAuthoring.Dialogs.PublishDialog);