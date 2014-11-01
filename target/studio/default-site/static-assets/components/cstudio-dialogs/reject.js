
CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * GoLive Constructor
 */
CStudioAuthoring.Dialogs.DialogReject = CStudioAuthoring.Dialogs.DialogReject || function() {
    CStudioAuthoring.Dialogs.DialogReject.superclass.constructor.call(this);
    this.moduleName = "reject";  	
    this.reasonHash = [];
}; 

CStudioAuthoring.Module.requireModule("publish-dialog",
        		"/components/cstudio-dialogs/publish-dialog.js",
        		{},
        		{ moduleLoaded: function(moduleName, dialogClass) 
        			{
						// Make GoLive constructor inherit from its parent (i.e. PublishDialog)
						YAHOO.lang.extend(CStudioAuthoring.Dialogs.DialogReject, dialogClass);
						
						// Extend GoLive's prototype with its own class functions
						CStudioAuthoring.Dialogs.DialogReject.prototype.createPanel = function (panelName, modalState, zIdx) {
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
																		
						CStudioAuthoring.Dialogs.DialogReject.prototype.invokeRejectService = function() {
							// check if rejection reason is filled out
							var tempStr = YAHOO.lang.trim(YDom.get('rejectMessageArea').value);
							if (tempStr && tempStr.length == 0) {
								alert('please choose a reason');
								return;
							}
							
							// remove unchecked items and dependencies from dependencyJsonObj
							this.selectedJsonObj = this.clone_obj(this.dependencyJsonObj);
							if (this.removeUncheckedItemsFromJson() == -1) { // no items selected
								return;
							}
							
							// rejectMessageArea
						    this.selectedJsonObj.reason = YDom.get('rejectMessageArea').value;
							if (this.selectedJsonObj.items.length) {
								
								var jsonSubmitString = YAHOO.lang.JSON.stringify(this.selectedJsonObj),
									self = this,
									serviceCallback = {
										success:function(oResponse) {
											//hide loading image when submit is clicked.
											self.hideLoadingImage("reject");
											//re enable if service failed to submit again
											YDom.get("golivesubmitButton").disabled = false;
											YDom.get("golivecancelButton").disabled = false;
											self.dialog.setBody(oResponse.responseText);
											self.setFocusOnDefaultButton();
										},
										failure: function (oResponse) {
											self.pageRedirect(oResponse);
											if (oResponse.status == -1) {
												alert('Reject is taking longer. The icon status will be updated once the content rejected.');
												self.dialog.hide();
												CStudioAuthoring.Operations.pageReload();
											} else {
												alert('reject items call failed ' + oResponse.statusText);
											}
										},
										timeout: CStudioAuthoring.Request.Timeout.GoLiveTimeout
									};
								
								//show loading image when submit is clicked.
								this.showLoadingImage("reject");
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
										"/service/ui/workflow-actions/reject-items?site=" +
										CStudioAuthoringContext.site, 
										serviceCallback, 
										jsonSubmitString);
							} else {
								alert('no items selected');
							}
						};
						
						CStudioAuthoring.Dialogs.DialogReject.prototype.handleDependencies = function (matchedInputElement, isChecked) {
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
									}
									var parentInputElement = YDom.get(parentURI);
									parentInputElement.checked = true;
									this.handleDependencies(parentInputElement, true);
								}									
							} else {
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
						
						CStudioAuthoring.Dialogs.DialogReject.prototype.displayItemListWithDependencies = function (dependencyList) {
						
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
						
							YEvent.addListener("golivesubmitButton", "click", this.invokeRejectService, this, true);
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
						
						//Listener to change select box message in reject pop-up
						CStudioAuthoring.Dialogs.DialogReject.prototype.onRejectSelectBoxChange = function (e) {
							var reasonList = YDom.get("rejectReasonDropDown");
							var chosenOption = reasonList.options[reasonList.selectedIndex];
							if (reasonList.selectedIndex != 0) {
								YDom.get('rejectMessageArea').value = this.reasonHash[chosenOption.value];
							} else {
								YDom.get('rejectMessageArea').value = '';
							}
						};
						
						CStudioAuthoring.Dialogs.DialogReject.prototype.getDependenciesForGoLiveItemList = function(contentItems) {
							var self = this;
							
							if (this.itemArray.length) {
								var xmlString = CStudioAuthoring.Utils.createContentItemsXml(contentItems),
									dependencyUrl = CStudioAuthoringContext.baseUri +
													"/service/ui/workflow-actions/reject-dependencies?site=" +
													CStudioAuthoringContext.site;
						
								var serviceCallback = {
									success: function(o) {
										var respText = o.responseText,
											timeZoneText = o.getResponseHeader.Timezone,
											scriptString = self.getJsonObject(respText),
											ftlWithoutScriptTag = self.removeScriptContent(respText);  // replace everything in between and including <script> tags
											
										self.dependencyJsonObj = eval('(' + scriptString + ')');
										self.flatMap = self.createItemMap();
										self.uncheckedItemsArray = [];                    
										self.displayItemListWithDependencies(ftlWithoutScriptTag);
										
										
										// get reject reason from hidden div
										var rejectReasonJsonObj = eval('(' + YDom.get("rejectReasonJson").innerHTML + ')'),
											reasonJsonArray = rejectReasonJsonObj.messages;
											
										for (var i = 0; i < reasonJsonArray.length; i++) {
											self.reasonHash[reasonJsonArray[i].title] = reasonJsonArray[i].body;
										}
										YEvent.addListener("rejectReasonDropDown", "change", self.onRejectSelectBoxChange, self, true);
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
						
						CStudioAuthoring.Dialogs.DialogReject.prototype.closeDialog = function () {    
							// remove curtain on top of nav bar
							YDom.get('curtain').style.display = 'none';
						
							this.dialog.destroy();
						
							//clear the overlay mask if it remains after closing the dialog.
							var tempMask = YDom.getElementsByClassName('mask');
							for (var i = 0; i < tempMask.length; ++i) {
								tempMask[i].parentNode.removeChild(tempMask[i]);
							}
						};
						
						CStudioAuthoring.Dialogs.DialogReject.prototype.showDialog = function(site, contentItems) {
						
							var selectedContent = CStudioAuthoring.SelectedContent.getSelectedContent();
							this.init();
							
							for(var i=0; i < selectedContent.length; i++) {
								this.itemArray.push(selectedContent[i].uri);
							}             
							
							this.getDependenciesForGoLiveItemList(contentItems);      
						};
						
						// Create GoLive dialog instance
						var reject = new CStudioAuthoring.Dialogs.DialogReject();
						
						// Create a global pointer to the current dialog instance
						CStudioAuthoring.Dialogs.DialogReject.instance = reject;

						// dialog instance will be reused with every call to 'dialog-approve'
						CStudioAuthoring.Module.moduleLoaded("dialog-reject", reject);
					}
				});