
CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * GoLive Constructor
 */
CStudioAuthoring.Dialogs.DialogScheduleToGoLive = CStudioAuthoring.Dialogs.DialogScheduleToGoLive || function() {   
    CStudioAuthoring.Dialogs.DialogScheduleToGoLive.superclass.constructor.call(this);             
    this.moduleName = "scheduleToGoLive";                                                                          
}; 

CStudioAuthoring.Module.requireModule("publish-dialog",
        		"/components/cstudio-dialogs/publish-dialog.js",
        		{},
        		{ moduleLoaded: function(moduleName, parentClass) 
        			{
						// Make GoLive constructor inherit from its parent (i.e. PublishDialog)
						YAHOO.lang.extend(CStudioAuthoring.Dialogs.DialogScheduleToGoLive, parentClass);
						
						// Extend GoLive's prototype with its own class functions
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.createPanel = function (panelName, modalState, zIdx) {
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
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.invokeScheduleToGoLiveService = function() {
							// remove unchecked items and dependencies from dependencyJsonObj
							this.selectedJsonObj = this.clone_obj(this.dependencyJsonObj);
							if (this.removeUncheckedItemsFromJson() == -1) { // no items selected
								return;
							}
						
							if (this.selectedJsonObj.items.length != 0) {
						
								this.appendPublishingChannelsData(this.selectedJsonObj);
						
								//get time from widget
								var dateValue = Dom.get('datepicker').value;
								var timeValue = Dom.get('timepicker').value;
								if ((dateValue == 'Date...') || (timeValue == 'Time...') || (timeValue == '')) {
									alert('Please provide a date and/or time');
									return;
								}
								var scheduledDate = this.getScheduledDateTimeForJson(dateValue, timeValue);
								// insert date in GoLiveItems.dependencyJsonObj
								this.selectedJsonObj.now = "false";
								this.selectedJsonObj.scheduledDate = scheduledDate;
								
								for(var idx = 0; idx < this.selectedJsonObj.items.length; idx++) {
									var browserUri = this.selectedJsonObj.items[idx].browserUri;
									if( this.ifExistsInUncheckedItemsArray( browserUri ) == -1 ) {
										this.setTimeInJsonObject(this.selectedJsonObj.items, scheduledDate, browserUri);
									}
								}
						
								this.selectedJsonObj.submissionComment=document.getElementById("acn-submission-comment").value;
								var jsonSubmitString = YAHOO.lang.JSON.stringify(this.selectedJsonObj),
									self = this,
									serviceCallback = {
										success:function(oResponse) {
											//hide loading image when submit is clicked.
											self.hideLoadingImage("approveschedule");		
											self.dialog.setBody(oResponse.responseText);
											self.setFocusOnDefaultButton();
										},
										failure: function (oResponse) {
											self.pageRedirect(oResponse);
											//hide loading image when submit is clicked.
											self.hideLoadingImage("approveschedule");
											//re enable if service failed to submit againg
											YDom.get("golivesubmitButton").disabled = false;
											YDom.get("golivecancelButton").disabled = false;
											if (oResponse.status == -1) {
												alert('Schedule To Go Live is taking longer. The icon status will be updated once the content scheduled to Go Live.');
												self.dialog.hide();
												CStudioAuthoring.Operations.pageReload();
											} else {
												alert('Schedule To Go Live Call Failed' + oResponse.statusText);
											}
										},
										timeout: CStudioAuthoring.Request.Timeout.GoLiveTimeout
									};
								
								//show loading image when submit is clicked.
								this.showLoadingImage("approveschedule");
								//disable submit button to protect multipale submit at the same time.
								YDom.get("golivesubmitButton").disabled = true;
								YDom.get("golivecancelButton").disabled = true;

								if (YConnect._isFormSubmit) {
									YConnect.resetFormState();
								}
								YConnect.setDefaultPostHeader(false);
								YConnect.initHeader("Content-Type", "application/json; charset=utf-8");

								YConnect.asyncRequest(
										'POST',
										CStudioAuthoringContext.baseUri +
										"/service/ui/workflow-actions/schedule-to-go-live?site=" +
										CStudioAuthoringContext.site, 
										serviceCallback, 
										jsonSubmitString);
							} else {
								alert('no items selected');
							}
						};
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.handleDependencies = function (matchedInputElement, isChecked) {
							this.updateUncheckedItemList(matchedInputElement, isChecked);
							
							var selectedElementURI = matchedInputElement.id,
								item = this.flatMap[selectedElementURI];
								
							if (isChecked) {
								if (item.submittedForDeletion) {
									//check all child elements
									this.checkAllChildren(matchedInputElement);
								} else {
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
								}
							} else {
								if (item.submittedForDeletion) {
									var parentURI = "/site/website" + item.parentPath + "/index.xml";
									if (parentURI) {
										var parentItem = this.flatMap[parentURI];
										if (parentItem && parentItem.submittedForDeletion) {
											var parentInputElement = YDom.get(parentURI);
											if (item.submittedForDeletion && parentInputElement.checked) {
												matchedInputElement.checked = true;
											}
										}
									}
								} else {
									var isParentSelectedForDelete = false;
									//check if parent element is submitted for delete?
									if (item.parentPath && item.parentPath != "") {
										var parentURI = "/site/website" + item.parentPath + "/index.xml";
										var parentItem = this.flatMap[parentURI];
										if (parentItem && parentItem.submittedForDeletion) {
											isParentSelectedForDelete = true;
											var parentInputElement = YDom.get(parentURI);
											if (parentInputElement.checked) {
												matchedInputElement.checked = true;
											}
										}
									}
						
									if (!isParentSelectedForDelete) {
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
								}
							}
						};
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.displayItemListWithDependencies = function (dependencyList) {
						
							// Instantiate the Panel
							this.dialog = this.createPanel('submitPanel', true, 10);
							this.dialog.beforeShowEvent.subscribe( function() {
						
								if (YUISelector.query("#acnScrollBoxDiv .acnLiveCellIndented").length) {
									// Only add notice if there are dependencies present
									var el = YUISelector.query('#acnVersionWrapper .acnBoxFloatLeft')[0],
										msg = document.createElement('span');
										msg.className = "notice";
										msg.textContent = "*Dependencies must be checked before you can Schedule to Go Live.";
						
									el.appendChild(msg);
								}
							});
							
							this.dialog.setBody(dependencyList);
							this.dialog.render(document.body);
						
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
						
							//set height of items div
							var oScrollBox = YDom.get('acnScrollBoxDiv');
							oScrollBox.style.height = "280px";
						
							//check for in-valid inline styles
							var oConfirmDialog = this.dialog;
							if (oConfirmDialog && oConfirmDialog.body && oConfirmDialog.body.style.height != "") {
								oConfirmDialog.body.style.height = "";
							}
							this.dialog.show();
						
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
						
							YEvent.addListener("golivesubmitButton", "click", this.invokeScheduleToGoLiveService, this, true);
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
								//set tab focus items.
								var oCancelButton = YDom.get("golivecancelButton");
								var oDatePicker = Dom.get("datepicker");
								this.dialog.firstElement = oDatePicker;
								this.dialog.lastElement = oCancelButton;
						
								CStudioAuthoring.Utils.setDefaultFocusOn(submittButton);
							}
						};
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.getDependenciesForGoLiveItemList = function(contentItems) {
							var self = this;
							
							if (this.itemArray.length) {
								var xmlString = CStudioAuthoring.Utils.createContentItemsXml(contentItems),
									dependencyUrl = CStudioAuthoringContext.baseUri +
                                                                                        "/service/ui/workflow-actions/schedule-to-go-live-dependencies?site=" +
                                                                                        CStudioAuthoringContext.site + "&baseUrl=" + CStudioAuthoringContext.baseUri;
						
								var serviceCallback = {
									success: function(o) {
										var respText = o.responseText,
                									timeZoneText = o.getResponseHeader.Timezone,
                									scriptString = self.getJsonObject(respText),
                									ftlWithoutScriptTag = self.removeScriptContent(respText),  // replace everything in between and including <script> tags
                								        testRoot;    // Will be used to test the existence of Publishing Channels
											
										testRoot = document.createElement("div");
										testRoot.innerHTML = ftlWithoutScriptTag;
										
										if (testRoot.querySelector("#go-pub-channel").children.length) {    
											// There are publishing channels set
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
        									} else {
        									        if (!parentClass.messagePanel) {
        									            parentClass.messagePanel = new parentClass.createMessagePanel("messageOverlay", true, 1000);
                                                        parentClass.messagePanel.setBody(parentClass.getNoPublishingChannelsBody());

                                                        parentClass.messagePanel.hideEvent.subscribe( function() {
                                                            // remove curtain on top of nav bar
                                                            YDom.get('curtain').style.display = 'none';

                                                            //clear the overlay mask if it remains after closing the dialog.
                                                            var tempMask = YDom.getElementsByClassName('mask');
                                                            for (var i = 0; i < tempMask.length; ++i) {
                                                                    tempMask[i].parentNode.removeChild(tempMask[i]);
                                                                }
                                                            });
                                                        parentClass.messagePanel.render(document.body);
        									        }
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
                                							
                                                    parentClass.messagePanel.show();
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
								YConnect.asyncRequest('POST', dependencyUrl, serviceCallback, xmlString);
								
							} else {
								alert('No items selected');
							}
						};
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.closeDialog = function () {    
							// remove curtain on top of nav bar
							YDom.get('curtain').style.display = 'none';
						
							this.dialog.destroy();
						
							//clear the overlay mask if it remains after closing the dialog.
							var tempMask = YDom.getElementsByClassName('mask');
							for (var i = 0; i < tempMask.length; ++i) {
								tempMask[i].parentNode.removeChild(tempMask[i]);
							}
						};
						
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.prototype.showDialog = function(site, contentItems) {
						
							var selectedContent = CStudioAuthoring.SelectedContent.getSelectedContent();
							this.init();
							
							for(var i=0; i < selectedContent.length; i++) {
								this.itemArray.push(selectedContent[i].uri);
							}             
							
							this.getDependenciesForGoLiveItemList(contentItems);      
						};
												
						// Create GoLive dialog instance
						var scheduleToGoLive = new CStudioAuthoring.Dialogs.DialogScheduleToGoLive();
						
						// Create a global pointer to the current dialog instance
						CStudioAuthoring.Dialogs.DialogScheduleToGoLive.instance = scheduleToGoLive;

						// dialog instance will be reused with every call to 'dialog-approve'
						CStudioAuthoring.Module.moduleLoaded("dialog-schedule-to-go-live", scheduleToGoLive);
					}
				});