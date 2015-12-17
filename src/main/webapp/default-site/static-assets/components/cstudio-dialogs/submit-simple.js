
CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * GoLive Constructor
 */
CStudioAuthoring.Dialogs.DialogSimpleSubmit = CStudioAuthoring.Dialogs.DialogSimpleSubmit || function() {
    CStudioAuthoring.Dialogs.DialogSimpleSubmit.superclass.constructor.call(this);
    this.moduleName = "submitToGoLive";
    this.schedulePolicyPanel = null;
};

CStudioAuthoring.Module.requireModule("publish-dialog",
    "/static-assets/components/cstudio-dialogs/publish-dialog.js",
    {},
    { moduleLoaded: function(moduleName, dialogClass)
    {
        // Make GoLive constructor inherit from its parent (i.e. PublishDialog)
        YAHOO.lang.extend(CStudioAuthoring.Dialogs.DialogSimpleSubmit, dialogClass);

        // Extend GoLive's prototype with its own class functions
        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.createPanel = function (panelName, modalState, zIdx) {
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

        //method for creating modal panel for scheduling policy.
        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.createSchedulePolicyPanel = function (panelName, modalState, zIdx) {
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

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.setUpGoLiveListeners = function () {
            YEvent.addListener("globalSetToNow", "click", this.changeToNow, this, true);
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.invokeSubmitToGoLiveService = function() {
            // remove unchecked items and dependencies from dependencyJsonObj
            this.selectedJsonObj = this.clone_obj(this.dependencyJsonObj);
            if (this.removeUncheckedItemsFromJson() == -1) { // no items selected
                return;
            }

            if (this.selectedJsonObj.items.length != 0) {

                // add isNow and scheduledDate fields
                // check radio button to see which one was clicked
                if (YDom.get('now').checked) {
                    this.selectedJsonObj.now = "true";
                    this.selectedJsonObj.scheduledDate = "2010-02-26T15:00:00";
                    for(var i = 0; i < this.selectedJsonObj.items.length; i++) {
                        this.selectedJsonObj.items[i].now = true;
                        this.selectedJsonObj.items[i].scheduledDate = "";
                    }
                } else { //get time from widget
                    var dateValue = Dom.get('datepicker').value;
                    var timeValue = Dom.get('timepicker').value;
                    if ((dateValue == 'Date...') || (timeValue == 'Time...') || (timeValue == '')) {
                        alert('Please provide a date and/or time');
                        return;
                    }
                    var scheduledDate = this.getScheduledDateTimeForJson(dateValue, timeValue);
                    // insert date in this.dependencyJsonObj
                    this.selectedJsonObj.now = "false";
                    this.selectedJsonObj.scheduledDate = scheduledDate;
                    for(var i = 0; i < this.selectedJsonObj.items.length; i++) {
                        this.selectedJsonObj.items[i].now = false;
                        this.selectedJsonObj.items[i].scheduledDate = scheduledDate;
                    }
                }
                // check email flag
                if (Dom.get('email').checked) {
                    this.selectedJsonObj.sendEmail = "true";
                } else {
                    this.selectedJsonObj.sendEmail = "false";
                }

                this.selectedJsonObj.submissionComment=document.getElementById("acn-submission-comment").value;
                var jsonSubmitString = YAHOO.lang.JSON.stringify(this.selectedJsonObj),
                    self = this,
                    serviceCallback = {
                        success:function(oResponse) {
                            //hide loading image when submit is clicked.
                            self.hideLoadingImage("simplesubmit");
                            self.dialog.setBody(oResponse.responseText);
                            self.setFocusOnDefaultButton();
                        },
                        failure: function (oResponse) {
                            self.pageRedirect(oResponse);
                            //hide loading image when submit is clicked.
                            self.hideLoadingImage("simplesubmit");
                            //re enable if service failed to submit againg
                            YDom.get("golivesubmitButton").disabled = false;
                            YDom.get("golivecancelButton").disabled = false;
                            if (oResponse.status == -1) {
                                alert('Submit To Go Live is taking longer. The icon status will be updated once the content submitted to Go Live.');
                                self.dialog.hide();
                                CStudioAuthoring.Operations.pageReload();
                            } else {
                                alert('Submit To Go Live Call Failed ' + oResponse.statusText);
                            }
                        },
                        timeout: CStudioAuthoring.Request.Timeout.GoLiveTimeout
                    };

                //show loading image when submit is clicked.
                this.showLoadingImage("simplesubmit");
                //disable submit button to protect multiple submit at the same time.
                YDom.get("golivesubmitButton").disabled = true;
                YDom.get("golivecancelButton").disabled = true;
                // submit to service
                if (YConnect._isFormSubmit) {
                    YConnect.resetFormState();
                }
                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                // call go-live
                YConnect.asyncRequest(
                    'POST',
                    CStudioAuthoringContext.baseUri +
                    "/service/ui/workflow-actions/submit-to-go-live?site=" +
                    CStudioAuthoringContext.site,
                    serviceCallback,
                    jsonSubmitString);
            } else {
                alert('no items selected');
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.handleDependencies = function (matchedInputElement, isChecked) {
            this.updateUncheckedItemList(matchedInputElement, isChecked);

            var selectedElementURI = matchedInputElement.id,
                item = this.flatMap[selectedElementURI];

            if (isChecked) {
                //check all parents
                var parentURI = item.mandatoryParent;
                if (parentURI) {
                    var parentItem = this.flatMap[parentURI];
                    if (parentItem && parentItem.pages && parentItem.pages.length >= 1) {
                        var isReferencePage = this.checkReferencePages(parentItem.pages, item.browserUri);

                        if (isReferencePage) {
                            //no need to check the parent item
                            return;
                        }
                    }
                    var parentInputElement = YDom.get(parentURI);
                    parentInputElement.checked = true;
                    this.handleDependencies(parentInputElement, true);
                }

                //check all page references along with parent page.
                if (item.pages && item.pages.length >= 1) {
                    for (var pagesIdx = 0; pagesIdx < item.pages.length; pagesIdx++) {
                        var pageInputElement = YDom.get(item.pages[pagesIdx].uri);
                        pageInputElement.checked = true;
                        this.updateUncheckedItemList(pageInputElement, true);
                    }
                }
            } else {

                //deselect all children
                //Check for page references in mandatoryParent.
                var parentURI = item.mandatoryParent;
                if (parentURI) {
                    var parentItem = this.flatMap[parentURI];
                    if (parentItem && parentItem.pages && parentItem.pages.length >= 1) {
                        var isReferencePage = this.checkReferencePages(parentItem.pages, item.browserUri);
                        var parentInputElement = YDom.get(parentURI);
                        if (isReferencePage && parentInputElement.checked) {
                            matchedInputElement.checked = true;
                            return;
                        }
                    }
                }

                var children = this.getChildren(item);
                if (children.length) {
                    for (var i = 0; i < children.length; i++) {
                        var child = children[i];
                        var childInputElement = YDom.get(child.uri);
                        childInputElement.checked = false;
                        this.handleDependencies(childInputElement, false);
                    }
                }
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.setUpSubmitToGoLiveListeners = function () {

            var onSchedulePolicyClick = function (e) {
                var self = this;
                var serviceCallback = {
                    success: function(oResponse) {
                        if (!self.schedulePolicyPanel) {
                            self.schedulePolicyPanel = self.createSchedulePolicyPanel("schedulePolicyPanel", true, 1000);
                            self.schedulePolicyPanel.setBody(oResponse.responseText);
                            // Render the Submit panel
                            self.schedulePolicyPanel.render(document.body);
                        }
                        self.schedulePolicyPanel.show();
                        self.setFocusOnDefaultButton("schedulePolicySubmitButtons");
                    },
                    failure: function (oResponse) {
                        self.pageRedirect(oResponse);
                        alert('schedule policy pop-up failed' + oResponse.statusText);
                    }
                };

                if (YConnect._isFormSubmit) {
                    YConnect.resetFormState();
                }
                YConnect.asyncRequest(
                    'GET',
                    CStudioAuthoringContext.baseUri +
                    "/service/ui/workflow-actions/schedule-policy?site=" +
                    CStudioAuthoringContext.site,
                    serviceCallback);

            };
            if (YDom.get('schedulePolicy')) { // schedulePolicy link exists
                // if schedulingPolicy id exists
                YEvent.addListener("schedulePolicy", "click", onSchedulePolicyClick, this, true);
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.displayItemListWithDependencies = function (dependencyList) {

            // Instantiate the Panel
            this.dialog = this.createPanel('submitPanel', true, 10);
            this.dialog.setBody(dependencyList);
            this.dialog.render(document.body);
            this.dialog.show();

            //set z-index for panel so that it will appear over context nav bar also.
            var oContainerPanel = YDom.get('submitPanel_c');
            if (oContainerPanel && oContainerPanel.style.zIndex != "") {
                var zIdx = oContainerPanel.style.zIndex;
                if (!isNaN(zIdx) && parseInt(zIdx, 10) <= 100) {
                    oContainerPanel.style.zIndex = "101";
                }
            }

            // put up curtain on top of nav bar
            YDom.get('curtain').style.display = 'block';
            // set up listeners
            this.setUpSubmitToGoLiveListeners();

            //set height of items div
            var oScrollBox = YDom.get('acnScrollBoxDiv');
            var oDepWrn = YDom.get('dependenciesWarning');
            if (oScrollBox && !oDepWrn) {
                oScrollBox.style.height = "199px";
            }

            //check for in-valid inline styles
            var oConfirmDialog = this.dialog;
            if (oConfirmDialog && oConfirmDialog.body && oConfirmDialog.body.style.height != "") {
                oConfirmDialog.body.style.height = "";
            }

            var onCheckBoxSubmittedItemClick = function (event, matchedEl) {
                // skipping email checkbox
                if (matchedEl.id == "email")
                    return;
                this.handleDependencies(matchedEl, matchedEl.checked);
                this.anyoneSelected = false;

                for (var key in this.flatMap) {
                    if (this.flatMap.hasOwnProperty(key)) {
                        var inputElement = YDom.get(key);
                        this.anyoneSelected = this.anyoneSelected || inputElement.checked;
                    }
                }
                var submittButton = YDom.get("golivesubmitButton");
                submittButton.disabled = !this.anyoneSelected;
            };

            // handle checkbox clicks
            YEvent.delegate("acnVersionWrapper", "click", onCheckBoxSubmittedItemClick, ".acnLiveTableCheckbox > input", this, true);
            YEvent.delegate("acnSubmitWrapper", "click", onCheckBoxSubmittedItemClick, ".acnLiveTableCheckbox > input", this, true);

            this.publishingChannelsInit();

            YEvent.addListener("golivesubmitButton", "click", this.invokeSubmitToGoLiveService, this, true);
            YEvent.addListener("golivecancelButton", "click", this.closeDialog, this, true);

            // hide dependency line if only 1 item
            if (this.dependencyJsonObj.items.length == 1) { // only 1 item in the json obj
                if (this.dependencyJsonObj.items[0].numOfChildren == 0) { // and no children
                    var dependencyText = YDom.get('depText');
                    if (dependencyText) {
                        YDom.setStyle(dependencyText, "display", "none");
                    }
                }
            }

            YEvent.removeListener("now", "click", this.toggleTimeSelection);
            YEvent.addListener("now", "click", this.toggleTimeSelection);
            YEvent.removeListener("settime", "click", this.toggleTimeSelection);
            YEvent.addListener("settime", "click", this.toggleTimeSelection);

            var submittButton = YDom.get("golivesubmitButton");
            if (submittButton) {
                CStudioAuthoring.Utils.setDefaultFocusOn(submittButton);
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.getDependenciesForGoLiveItemList = function(contentItems) {
            var self = this;

            if (this.itemArray.length) {
                var jsonString = CStudioAuthoring.Utils.createContentItemsJson(contentItems),
                    dependencyUrl = CStudioAuthoringContext.baseUri + CStudioAuthoring.Service.getDependenciesServiceUrl + '?site=' + CStudioAuthoringContext.site;

                var serviceCallback = {
                    success: function(o) {
                        var respText = o.responseText,
                            timeZoneText = o.getResponseHeader.Timezone,
                            scriptString = respText;//self.getJsonObject(respText),
                        ftlWithoutScriptTag = respText;//self.removeScriptContent(respText);  // replace everything in between and including <script> tags

                        self.dependencyJsonObj = eval('(' + scriptString + ')');
                        self.flatMap = self.createItemMap();
                        self.uncheckedItemsArray = [];
                        self.displayItemListWithDependencies(ftlWithoutScriptTag);

                        //init yui datepicker
                        var afterRenderFn = function(sourceElementId){
                            if(afterRenderFn.firecount == 0){
                                afterRenderFn.firecount++;
                                var today = new Date();
                                today.setDate(today.getDate() + 1);
                                YDom.get(sourceElementId).value = [
                                    (today.getMonth()+1),
                                    today.getDate(),
                                    today.getFullYear()
                                ].join("/");
                            }
                        };
                        afterRenderFn.firecount = 0;
                        var initCalendar = CStudioAuthoring.Utils.yuiCalendar('datepicker', 'focus', 'datepicker', afterRenderFn),
                            status = CStudioAuthoring.Utils.initCursorPosition('timepicker', ['click', 'keydown', 'keyup', 'keypress', 'mouseup', 'mousedown']),
                            initTimeFormat = CStudioAuthoring.Utils.textFieldTimeHelper('timepicker', 'blur', 'timepicker'),
                            initTimeIncrementButton = CStudioAuthoring.Utils.textFieldTimeIncrementHelper('timeIncrementButton', 'timepicker', 'click'),
                            initTimeDecrementButton = CStudioAuthoring.Utils.textFieldTimeDecrementHelper('timeDecrementButton', 'timepicker', 'click');

                        // Updating time zone name dynamically
                        if (timeZoneText) {
                            timeZoneText = timeZoneText.replace(/^\s+|\s+$/, '');

                            var oTimeZoneSpan = YDom.get("timeZone");
                            if (oTimeZoneSpan) {
                                oTimeZoneSpan.innerHTML = timeZoneText;
                            }
                        }
                    },
                    failure: function (o) {
                        self.pageRedirect(o);
                        alert(o.statusText);
                    }
                };

                if (YConnect._isFormSubmit) {
                    YConnect.resetFormState();
                }
                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
                YConnect.asyncRequest('POST', dependencyUrl, serviceCallback, jsonString);

            } else {
                alert('No items selected');
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.closeDialog = function () {
            // remove curtain on top of nav bar
            YDom.get('curtain').style.display = 'none';

            this.dialog.destroy();

            //clear the overlay mask if it remains after closing the dialog.
            var tempMask = YDom.getElementsByClassName('mask');
            for (var i = 0; i < tempMask.length; ++i) {
                tempMask[i].parentNode.removeChild(tempMask[i]);
            }
        };

        CStudioAuthoring.Dialogs.DialogSimpleSubmit.prototype.showDialog = function(site, contentItems) {

            var selectedContent = CStudioAuthoring.SelectedContent.getSelectedContent();
            this.init();

            for(var i=0; i < selectedContent.length; i++) {
                this.itemArray.push(selectedContent[i].uri);
            }

            this.getDependenciesForGoLiveItemList(contentItems);
        };

        // Create GoLive dialog instance
        var submitToGoLive = new CStudioAuthoring.Dialogs.DialogSimpleSubmit();

        // Create a global pointer to the current dialog instance
        CStudioAuthoring.Dialogs.DialogSimpleSubmit.instance = submitToGoLive;

        // dialog instance will be reused with every call to 'dialog-approve'
        CStudioAuthoring.Module.moduleLoaded("dialog-simple-submit", submitToGoLive);
    }
    });