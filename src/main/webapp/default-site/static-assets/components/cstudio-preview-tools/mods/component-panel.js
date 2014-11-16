CStudioAuthoring.Utils.addJavascript("/static-assets/yui/dragdrop/dragdrop-min.js");

CStudioAuthoring.Module.requireModule(
	"publish-subscribe",
    '/static-assets/components/cstudio-common/amplify-core.js',
	{  },
	{ moduleLoaded: function () {
		
 
CStudioAuthoring.Module.requireModule(
	"cstudio-forms-engine",
    '/static-assets/components/cstudio-forms/forms-engine.js',
	{  },
	{ moduleLoaded: function () {
		
		var moduleName = "component-panel",		// ties css file to the js file
		modulePath = null,
		YDom = YAHOO.util.Dom,
		YElement = YAHOO.util.Element,
		dcNewComponentClass = "new-component",
		dcComponentClass = "cstudio-draggable-component",
		dcWrapperClass = "cstudio-component-ice",
		dcContainerClass = "cstudio-component-zone",
		componentsUpdated = false,
		copyStyles = ['border-collapse', 'border-spacing', 'caption-side', 'color', 'direction', 'empty-cells', 
												'font-family', 'font-size', 'font-style', 'font-weight', 'letter-spacing', 'line-height',
												'list-style-image', 'list-style-position', 'list-style-type', 'quotes', 'text-align', 
												'text-indent', 'text-transform', 'visibility', 'white-space', 'word-spacing'];

	// Load the component's css	
	modulePath = CStudioAuthoring.Utils.getScriptPath(moduleName + ".js");
	modulePath = modulePath.split(CStudioAuthoringContext.baseUri)[1];
	// We need to remove CStudioAuthoringContext.baseUri from the path to be able to use the addCss function
	if (modulePath) {
		// console.log("Loading css file: " + modulePath + moduleName + ".css");
		CStudioAuthoring.Utils.addCss(modulePath + moduleName + ".css");
	}		

	CStudioAuthoring.ComponentsPanel = CStudioAuthoring.ComponentsPanel || {
	
		initialized: false,
		componentsOn: false,
		dcOverlay: null,
		ajaxOverlay : null,
		rollbackContentMap: null, 	// use this content map to restore the app in case of any errors
		
		initialize: function(config) {
			// this.componentsOn = !!(sessionStorage.getItem('components-on'));  
			var self = this;
			
			if(this.initialized == false) {
				this.initialized = true;

				this.ajaxOverlay = this.createAjaxOverlay("ajax-overlay", "preview-tools-panel-container_c", -5);

				amplify.subscribe('/operation/started', function () {
					self.ajaxOverlay.show();
				});
				amplify.subscribe('/operation/completed', function () {
					self.ajaxOverlay.hide();
				});
				amplify.subscribe('/operation/failed', function () {
					self.ajaxOverlay.hide();
				});
				
				amplify.subscribe('/page-model/loaded', function (data) {
					dom = ( new window.DOMParser()).parseFromString(data.model.responseText, "text/xml");
                    dom = dom.children[0];

					var contentMap = CStudioForms.Util.xmlModelToMap(dom);

					switch (data.operation) {

						case "init-components":
							// console.log('linking model to components');
							self.rollbackContentMap = CStudioForms.Util.xmlModelToMap(dom);
							self.linkComponentsToModel(contentMap);
							amplify.publish('/operation/completed');
							return;

						case "save-components-new":
				  			self.updateComponentsInModel(contentMap);
				  			CStudioForms.Util.loadFormDefinition(contentMap["content-type"], {
								success: function(formDefinition) {
									amplify.publish('components/form-def/loaded', { 
										"pagePath" : data.pagePath,
										"formDefinition" : formDefinition, 
										"contentMap": contentMap,
										"isNew": true });
	
								},
								failure: function() {
									amplify.publish('/operation/failed');
									alert("failed to load form definition");
								}
							});
				  			return;

						case "save-components":
								// console.log('saving model ... ');
				  			self.updateComponentsInModel(contentMap);
				  			CStudioForms.Util.loadFormDefinition(contentMap["content-type"], {
								success: function(formDefinition) {
									amplify.publish('components/form-def/loaded', { 
										"pagePath" : data.pagePath,
										"formDefinition" : formDefinition, 
										"contentMap": contentMap });
								},
								failure: function() {
									amplify.publish('/operation/failed');
									alert("failed to load form definition");
								}
							});
				  			return;
					}
				});

				amplify.subscribe('components/form-def/loaded', function (data) {
					if(data.isNew && data.isNew == true) {
						amplify.subscribe('/operation/completed', function (data) {
							document.location = document.location;
						});
					}

					self.saveModel(data.pagePath, data.formDefinition, data.contentMap, false, true);
				});

				// Load page model
				this.getPageModel(this.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "init-components", true, false);
			}
		},
		
		render: function(containerEl, config) {
			// this.componentsOn = !!(sessionStorage.getItem('components-on'));
			
			// if(this.componentsOn == true) {
			// 	this.expand(containerEl, config);
			// }
		},

		getPreviewPagePath: function (previewPath) {
			var pagePath = previewPath.replace(".html", ".xml");

			if (pagePath.indexOf(".xml") == -1) {
				if (pagePath.substring(pagePath.length - 1) != "/") {
					pagePath += "/";
				}
				pagePath += "index.xml";
			}
			return pagePath;
		},

		/*
		 * Load the model of a page and publish it
		 * @param pagePath -path to the page
		 * @param operation -operation performed (e.g. init, save, etc)
		 * @param start - true (starting an operation) | false (in the middle of an operation)
		 * @param complete - true (the operation is completed in this function) | false (the operation doesn't 
		 					 complete in this function -ie. the operation continues in another function)
		 * @publish event: /components/model/loaded
		 */
		getPageModel: function (pagePath, operation, start, complete) {

			if (start) {
				amplify.publish('/operation/started');
			}

			// this code should move to common api
			var serviceUrl = CStudioAuthoring.Service.getContentUri 
		               + "?site=" + CStudioAuthoringContext.site 
		               + "&path=" + pagePath 
		               + "&edit=false";

			var callback = {
				success: function(model) {
					amplify.publish('/page-model/loaded', { 
						"pagePath": pagePath, 
						"model": model, 
						"operation": operation });
					if (complete) {
						amplify.publish('/operation/completed');	
					}
				},
				failure: function(err) {
					// The operation must be completed if there was a failure
					amplify.publish('/operation/failed');
					alert("failed to load model");
				}
			}
		               
			YConnect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(serviceUrl), callback);
		},

		copyObj : function (srcObj, destObj) {

			if (srcObj && typeof srcObj == "object" && !(srcObj instanceof Array) && 
				destObj && typeof destObj == "object" && !(destObj instanceof Array)) {

				for (var prop in srcObj) {
					if (srcObj.hasOwnProperty(prop)) {
						destObj[prop] = srcObj[prop];
					}
				}
			}
		},

		/*
		 * This step would be unnecessary if the model were brought to the front-end from the beginning .. and the DOM elements
		 * loaded from this model (right now the model is being used by the ftl, but it isn't bounded to the DOM elements at all).
		 *
		 */
		linkComponentsToModel: function (contentMap) {

			var containerEls = YDom.getElementsByClassName(dcContainerClass),
				dcEls;

			if (contentMap) {

				containerEls.forEach( function (el) {
					var containerName = el.id.replace("zone-","");

					dcEls = YDom.getElementsByClassName(dcComponentClass, "div", el);

					for (var i=0; i < dcEls.length; i++) {
						// link each DOM element to its corresponding model data
						// we could also create pointers from dcEls[i].modelData to contentMap[containerName][i], but then
						// we would make the model stay in memory. Instead, we will copy what we need off the model
						// and let the model be garbage collected.
						dcEls[i].modelData = {};
						this.copyObj(contentMap[containerName][i], dcEls[i].modelData);
					}
				}, this);
			}
			// console.log("initial model ", contentMap);
		},

		updateComponentsInModel: function (contentMap) {

			var containerEls = YDom.getElementsByClassName(dcContainerClass);

			if (contentMap) {

				containerEls.forEach( function (el) {

					var containerName = el.id.replace("zone-",""),
						dcEls = YDom.getElementsByClassName(dcComponentClass, "div", el);

					contentMap[containerName] = []; 	// Clean previous component content in model
					
					for (var i=0; i < dcEls.length; i++) {
						contentMap[containerName][i] = {};
						this.copyObj(dcEls[i].modelData, contentMap[containerName][i]);
					}
				}, this);
			}
			// console.log("model updated ", contentMap);
		},

		saveModel: function (pagePath, formDefinition, contentMap, start, complete) {

			if (start) {
				amplify.publish('/operation/started');
			}

			var form = { definition: formDefinition, model: contentMap },
				xml = CStudioForms.Util.serializeModelToXml(form);

			var saveServiceUrl = "/proxy/alfresco/cstudio/wcm/content/write-content" +
				"?site=" + CStudioAuthoringContext.site +
				"&phase=onSave" +
				"&path=" + pagePath +
				"&fileName=" + pagePath.substring(pagePath.lastIndexOf('/')+1) +
				"&user=" + CStudioAuthoringContext.user +
				"&contentType=" + contentMap["content-type"] +
				"&unlock=true";

			var saveCb = {
				success: function() {
					// console.log("Model saved!");
					if (complete) {
						amplify.publish('/operation/completed');	
					}
				},					
				failure: function() {
					amplify.publish('/operation/failed');
					// console.log("Model not saved ... roll back");
				}						
			};
						
			YAHOO.util.Connect.setDefaultPostHeader(false);
			YAHOO.util.Connect.initHeader("Content-Type", "application/xml; charset=utf-8");
			YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(saveServiceUrl), saveCb, xml);
		},

		expand: function(containerEl, config) {
			var self = this;
			sessionStorage.setItem('components-on', "on");
			
			CStudioAuthoring.Service.lookupConfigurtion(
					CStudioAuthoringContext.site, 
					"/preview-tools/components-config.xml", 
					{
						success: function(config) {
							self.renderComponents(containerEl, config);
							self.dcOverlay = self.createOverlay("draggable-background", "controls-overlay", 5);
							
							var zoneEls = YDom.getElementsByClassName(dcContainerClass);
							
							for(var i=0; i<zoneEls.length; i++) {
								var zoneEl = zoneEls[i];
								YDom.addClass(zoneEl, dcContainerClass + "-on");
								var zoneTarget = new YAHOO.util.DDTarget(zoneEl); 
							}

							var draggableComponentEls = YDom.getElementsByClassName(dcComponentClass);

							draggableComponentEls.forEach(function (el) {

								YDom.addClass(el, dcComponentClass + "-on");
								var ddproxy = new DragAndDropDecorator(el, null, { resizeFrame: false });
								var wrapperEl = YDom.getAncestorByClassName(el, dcWrapperClass);

								if (wrapperEl && YDom.getElementsByClassName('delete-control', null, wrapperEl).length == 0) {
									// Make the existing components serve as drag drop targets. If they aren't, then only the zones will
									// be targets and some of the drag drop events on components will not be registered.
									// var ddTarget = new YAHOO.util.DDTarget(el);

									// This doesn't apply to elements inside the preview tools menu
									var delControl = self.createDeleteControl('delete-control');
									delControl.onclick = function() {
										Utility.removeComponent(this, function () {
											CStudioAuthoring.ComponentsPanel.getPageModel(CStudioAuthoring.ComponentsPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "save-components", true, false);
										});
									};
									YDom.insertBefore(delControl, el.firstChild);
								}
							}, self);

							zoneEls = self.moveElementsToFront("." + dcContainerClass, self.dcOverlay.getOverlayElement(), "cp-front-element");
							zoneEls.forEach(function(el) {
								// Save any inline background attribute (in case we want to restore it later)
								var pEl = document.getElementById("placeholder-" + el.id);
								el.style.background = this.getInheritedBackground(pEl);
							}, self);
							self.dcOverlay.show();
						},
					
						failure: function() {
						}
					});	
		},
	
		collapse: function(containerEl, config) {

			sessionStorage.setItem('components-on', "");

			this.dcOverlay.hide( function () {
				var context = CStudioAuthoring.ComponentsPanel;
				context.moveElementsBack(context.dcOverlay.getOverlayElement(), "cp-front-element");
			});

			var zoneEls = YDom.getElementsByClassName(dcContainerClass);
			zoneEls.forEach( function (el) {
				YDom.removeClass(el, dcContainerClass + "-on");
			});
			
			var draggableComponentEls = YDom.getElementsByClassName(dcComponentClass);
			draggableComponentEls.forEach( function (el) {
				YDom.removeClass(el, dcComponentClass + "-on");
				var delControl = YDom.getElementsByClassName('delete-control', null, el)[0];
				if (delControl) {
					el.removeChild(delControl);
				}
			});
			
		},

		/*
		 * Create a DOM element to serve as a delete control
		 * @param className -class name to distinguish this type of element
		 * @return the new DOM element
		 */			
		createDeleteControl : function (className) {
			var deleteEl = document.createElement("a"),
				btnEl = document.createElement("img");

			YDom.addClass(deleteEl, className);

			btnEl.src = CStudioAuthoringContext.authoringAppBaseUri 
         		+ "/themes/cstudioTheme/images/icons/delete.png";
         	btnEl.style.width = "16px";
         	btnEl.style.height = "16px";
         								         	
			deleteEl.appendChild(btnEl);
			return deleteEl;
		},

		/*
		 * Create a new component to be inserted into the Preview Tools panel
		 * @param component -object with at least 3 properties: path, type and label
		 * @param tipText -text to display when the component is dragged from the Preview Tools panel
		 * @return the new DOM element
		 */
		createMenuComponent : function (component, tipText) {

			var menuComponent = document.createElement("div");

			if (component && component.path && component.type && component.label) {
				YDom.addClass(menuComponent, "acn-panel-component");
				menuComponent.innerHTML = '<div class="' + dcNewComponentClass + ' ' + dcComponentClass + 
												'" data-path="' + component.path + '" data-type="' + component.type +
												'"><div><span class="tipText">' + tipText + '</span><b>' + component.label + '</b></div></div>';
			}
			return menuComponent;
		},

		renderComponents: function(containerEl, config) {
			containerEl.innerHTML = "";
			
			if(!config.category.length) {
				config.category = [ config.category ];
			}
			
			for(var i=0; i<config.category.length; i++) {
				var category = config.category[i];
	
				if(!category.component.length) {
					category.component = [category.component];
				}
				
				for(var j=0; j<category.component.length; j++) {
					var component = category.component[j];
					
					var componentEl = this.createMenuComponent(component, "Adding new component:");
					containerEl.appendChild(componentEl);
				}
			}
		}, 

		/*
		 * Initializes an overlay to go over the content. The overlay z-index value will be relative to a another reference element's z-index.
		 * @param overlayId -ID of the overlay
		 * @param refElementId -ID of HTML element to use as reference (optional)
		 * @param zIndexDiff -Number difference between the overlay z-index value and the reference element (optional)
		 */ 
		createOverlay : function (overlayId, refElementId, zIndexDiff, showCallback, hideCallback) {

			var overlayEl = document.getElementById(overlayId),
				zIndexDiff = zIndexDiff || 0,
				refElement = null,
				zIndexOverlay;

			if (!overlayEl) {
				// If there's no content overlay element then create it
				overlayEl = new YElement(document.createElement("div"));
				overlayEl.set("id", overlayId);

				refElement = document.getElementById(refElementId);
				if (refElement) {
					zIndexOverlay = YDom.getStyle(refElement, "z-index");
					zIndexOverlay = zIndexOverlay > zIndexDiff ?  zIndexOverlay - zIndexDiff : 1;
				} else {
					zIndexOverlay = 1;
				}
				overlayEl.appendTo(document.body);
				YDom.setStyle(YDom.get(overlayEl), "z-index", zIndexOverlay);

				return (function(){
					var el = YDom.get(overlayEl),
						zIndex = zIndexOverlay,
						callbackOnShow = showCallback,		// Default callback on show
						callbackOnHide = hideCallback;		// Default callback on hide

					var transitionEndEvent = (function () { 
					    var el = document.createElement('telement');
					    var transitions = {
					      'transition':'transitionend',		// Should be 'transitionEnd'; changed to be compatible with FF
					      'OTransition':'oTransitionEnd',
					      'MSTransition':'msTransitionEnd',
					      'MozTransition':'transitionend',
					      'WebkitTransition':'webkitTransitionEnd'
					    }

					    for(var t in transitions){
					        if( el.style[t] !== undefined ){
					            return transitions[t];
					        }
					    }
					})();

					// Define callbacks after css transitions
					YAHOO.util.Event.on(el, transitionEndEvent, function () {
						if (YDom.hasClass(el, "visible")) {
							if (typeof callbackOnShow == "function") {
								callbackOnShow();
							}
						} else {
							if (typeof callbackOnHide == "function") {
								callbackOnHide();
							}
						}
					});

					return {
						getOverlayElement : function () {
							return el;
						},
						show : function (callback) {
							// Override callback if one is provided
							callbackOnShow = callback ? callback : callbackOnShow;	
							YDom.replaceClass(el, "invisible", "visible");
						},
						hide : function (callback) {
							// Override callback if one is provided
							callbackOnHide = callback ? callback : callbackOnHide;
							YDom.replaceClass(el, "visible", "invisible");
						},
						getzIndex : function () {
							return zIndex;
						}
					}
				})();
			} else {
				return this.dcOverlay;
			}
		},

		/* 
		 * Wrapper function around createOverlay that creates a special type of overlay (overlay to be used while there are ajax transactions ocurring 
		 * blocks access to the UI)
		 * @param -same as createOverlay function
		 * @return -reference to the newly created overlay
		 */
		createAjaxOverlay : function (overlayId, refElementId, zIndexDiff, showCallback, hideCallback) {
			var loaderImg = document.createElement("div"),
				overlayObj = this.createOverlay(overlayId, refElementId, zIndexDiff, showCallback, hideCallback);

				YDom.addClass(loaderImg, "ajax-loader");
				overlayObj.getOverlayElement().appendChild(loaderImg);
				return overlayObj;
		},

		/*
		 * Copies background styles inherited by an element and returns them in a string. 
		 * We stop copying when we find a backgroundColor rule or a backgroundRepeat in the nearest ancestor.
		 * @param el -the element for which we want to find the inherited background styles
		 * @return string with the inherited background styles by the element
		 * 
		 * TO-DO : This function will be more complex if we also consider multiple background images or background images or styles that show partly in the element.
		 */
		getInheritedBackground : function (el) {

			var stylesArr;

			/* FF bug fix: http://siderite.blogspot.com/2009/07/jquery-firexof-error-could-not-convert.html */
			if (YAHOO.env.ua.gecko && el == document) {
				return "#FFFFFF";
			}

			stylesArr = (window.getComputedStyle) ? window.getComputedStyle(el) : 
							(el.currentStyle) ? el.currentStyle : null;
							// el.currentStyle applies to IE v9 and below

			if (stylesArr) {
				if ((stylesArr.getPropertyValue('background-color') != "transparent" && stylesArr.getPropertyValue('background-color') != "rgba(0, 0, 0, 0)") ||
					(stylesArr.getPropertyValue('background-repeat').match(/^repeat/) && stylesArr.getPropertyValue('background-image') != "none")) {
					// stop condition: element has a background color or a background image that is set to repeat
					return stylesArr.getPropertyValue('background-color') + " " + stylesArr.getPropertyValue('background-image') + " " + 
								stylesArr.getPropertyValue('background-repeat') + " " + stylesArr.getPropertyValue('background-position');
				} else {
					if (el.parentNode) {
						return this.getInheritedBackground(el.parentNode);
					}
					return "";
				}
			} else {
				return "#FFFFFF";	// Don't have access to the styles so set default background
			}
		},

		/*
		 * Get the z-index inherited by an element. 
		 * @param el -the element for which we want to find the inherited z-index
		 * @return integer that is the z-index value
		 */
		getMaxzIndex : function (el, zIndex) {

			var stylesArr;

			/* FF bug fix: http://siderite.blogspot.com/2009/07/jquery-firexof-error-could-not-convert.html */
			if (YAHOO.env.ua.gecko && el == document) {
				return zIndex;
			}

			stylesArr = (window.getComputedStyle) ? window.getComputedStyle(el) : 
							(el.currentStyle) ? el.currentStyle : null;
							// el.currentStyle applies to IE v9 and below

			if (stylesArr) {
				if (stylesArr.getPropertyValue('z-index') != "auto" && +stylesArr.getPropertyValue('z-index') > zIndex) {
					// save the zIndex value if it's greater than what we already have
					return this.getMaxzIndex(el.parentNode, +stylesArr.getPropertyValue('z-index'));
				} else {
					if (el.parentNode && el.parentNode.nodeName != "BODY") {
						return this.getMaxzIndex(el.parentNode, zIndex);
					} else {
						return zIndex;
					}
				}
			} else {
				return zIndex;
			}	
		},

		/*
		 * Creates a placeholder (duplicate) of an element copying its dimensions, class names and its ID (adding a prefix to it)
		 * @param el -the element we are creating a placeholder for
		 */
		createPlaceholder : function (el) {
			var pId = "placeholder-" + el.id,
				pEl = document.createElement("div");

			pEl.id = pId;
			pEl.style.height = el.clientHeight + "px";
			pEl.style.width = el.clientWidth + "px";
			YAHOO.util.Dom.insertBefore(pEl, el);
		},

		/*
		 * Create a placeholder of a component by wrapping it with the necessary wrappers (to manipulate the component in the drag drop UI)
		 * @param component -the component we are creating a placeholder for
		 * @return placeholder (ie. a copy of the component with wrappers)
		 */
		createNewComponentPlaceholder : function (component) {	
			var cpl = document.createElement("div");
			YDom.addClass(cpl, dcWrapperClass);
			cpl.innerHTML = "<div>" + component.parentNode.innerHTML + "</div>";
			// It shouldn't be necessary to create a DD proxy out of the component placeholder because the user will see the form for 
			// the component when the drag drop completes. The component placeholder will then be replaced by the component with the 
			// real data.
			// var ddproxy = new DragAndDropDecorator(cpl, null, { resizeFrame: false });
			return cpl;
		},

		clearActive : function () {
			var bodyEl = document.getElementsByTagName("body")[0];
			var activeContainer = YDom.getFirstChildBy(bodyEl, function (el) {
				return YDom.hasClass(el, dcContainerClass + "-active");
			}); 
			YDom.removeClass(activeContainer, dcContainerClass + "-active");
		},

		/*
		 * Return an object will all the styles an element has inline (e.g. { 'color': '#333', 'font-weight': 'bold'})
		 */
		getInlineStyles : function (el) {
			var cssArr, len, res = {};

			// split the cssText value into an array of values 
			// e.g: "display: none; cursor: pointer;" becomes ["display", "none", "cursor", "pointer", ""]
			cssArr = el.style.cssText.split(/:\s*|;\s*/);

			// If the length is an odd number, that's because the last value of the array is an empty string => discard it
			len = (cssArr.length % 2) ? cssArr.length - 1 : cssArr.length;
			for (var i = 0; i < len; i+=2) {
				res[cssArr[i]] = cssArr[i + 1];
			}
			return res;
		},

		/*
		 * Absolutely position an element. Set the element's position to absolute, copy its X and Y coordinates and set them as inline styles,
		 * and set its z-index to one number higher than the reference z-index value provided. Save the original inlines values in the element.
		 * @param zIndex -integer z-index reference value (optional). If "auto" or not set, the element's z-index will be set to 1
		 */
		absolutePosition : function (el, zIndex) {
			zIndex = zIndex || "auto";

			el.style.width = (el.clientWidth - (+(el.style.paddingLeft.split("px")[0]) - (el.style.paddingRight.split("px")[0]))) + "px";
			el.style.left = YDom.getX(el) + "px";
			el.style.top = YDom.getY(el) + "px";
			el.style.position = "absolute";
			el.style.zIndex = (typeof zIndex == "number") ? zIndex + 1 : 1;
		},

		/*
		 * Copy a list of styles inline from the computed styles collection of an element
		 * @param el : element
		 * @param computedStyles : computed styles collection 
		 * @param stylesArr : array of styles that will be copied inline from the computed styles collection
		 */
		copyComputedStylesInline : function (el, computedStyles, stylesArr) {
			stylesArr.forEach( function(style) {
				var styleVal = computedStyles.getPropertyValue(style);
				YDom.setStyle(el, style, styleVal);
			});
		},

		/*
		 * Set all styles in a style object inline on an element. Remove any other inline styles the element may have.
		 */
		restoreInlineStyles : function (el, stylesObj) {
			var cssPropertiesArr = el.style.cssText.split(/:.+?;\s*/);
			cssPropertiesArr.forEach( function(cssProperty) {
				if (!cssProperty) {
					// Discard any empty strings (if any)
					return;
				}
				if (stylesObj.hasOwnProperty(cssProperty)) {
					YDom.setStyle(el, cssProperty, stylesObj[cssProperty]);
				} else {
					el.style.removeProperty(cssProperty);
				}
			});
		},

		/*
		 * Take all the elements that match a cssSelector and place them absolutely in the page. The elements will be moved from their original
		 * position in the DOM tree and will be placed after a reference element. Their z-index will also be one integer higher than that of 
		 * the reference element. After elements have been moved, they will be identifiable by a special class.
		 * @param cssSelector -match the elements that we want to move against this CSS selector
		 * @param refElement -element to use as reference point for moving the selected elements
		 * @param frontClass -special class that all elements moved will have
		 * @return an array with all the elements that were moved
		 */
		moveElementsToFront : function (cssSelector, refElement, frontClass) {

			var zoneEls = YAHOO.util.Selector.query(cssSelector);
				zIndexRef = +YDom.getStyle(refElement, "z-index");

			zoneEls.forEach( function(el) {
				// Save the element's inline styles
				el.origInlineStyles = this.getInlineStyles(el);

				// Save the original height value
				var heightVal = el.parentNode.style.height;
				var stylesArr = (window.getComputedStyle) ? window.getComputedStyle(el) : 
							(el.currentStyle) ? el.currentStyle : null;
							// el.currentStyle applies to IE v9 and below

				// Force height value into parent element to avoid scroll bug
				el.parentNode.style.height = stylesArr.getPropertyValue('height');
				this.absolutePosition(el, zIndexRef);
				this.createPlaceholder(el);
				// Restore height value in parent element
				el.parentNode.style.height = heightVal;
				this.copyComputedStylesInline(el, stylesArr, copyStyles);
				YDom.insertAfter(el, refElement);
				YDom.addClass(el, frontClass);
			}, this);

			return zoneEls;
		},

		/*
		 * Move an element back to its original location. 
		 * @param el -element that is being moved
		 * @param frontClass -special class that identifies the element as having moved to the front. This class will need to be removed.
		 */
		moveBackToSource : function (el, frontClass) {
			
			var pEl = document.getElementById("placeholder-" + el.id);
			
			YDom.setStyle(pEl, "visibility", "hidden");  // Set placeholder visibility to hidden 

			// Restore element's original inline styles
			this.restoreInlineStyles(el, el.origInlineStyles);

			// Move element after the placeholder
			YDom.insertAfter(el, pEl);
			YDom.removeClass(el, frontClass);

			// Remove the placeholder element
			el.parentNode.removeChild(pEl);
		},

		/*
		 * Counter function for moveElementsToFront.
		 * Move all elements that are siblings of a reference element and have a special class back to where they were originally 
		 * located and remove their placeholders.
		 * @param refElement -sibling element of all the elements that have been moved front
		 * @param frontClass -special class that identifies all elements that have been moved to the front.
		 * @return an array with all the elements that were moved
		 */
		moveElementsBack : function (refElement, frontClass) {
			var ancestor = refElement.parentNode;
			var zoneEls = YDom.getChildrenBy(ancestor, function(el) {
					return YDom.hasClass(el, frontClass);
			});
			zoneEls.forEach( function (el) {
				this.moveBackToSource(el, frontClass);
			}, this);

			return zoneEls;
		}

	}
	
	Utility = {

		/*
		 * Update the height of a container placeholder.
		 * @param container -container whose placeholder we're going to update
		 */
		refreshPlaceholderHeight : function (container) {

			var pContainer = document.getElementById("placeholder-" + container.id);

			var stylesArr = (window.getComputedStyle) ? window.getComputedStyle(container) : 
							(container.currentStyle) ? container.currentStyle : null;
							// el.currentStyle applies to IE v9 and below
			if (stylesArr) {
				pContainer.style.height = stylesArr.height;
			}
		},

		resizeProxy : function (el, dragEl) {

            var bt = parseInt( YDom.getStyle(dragEl, "borderTopWidth"    ), 10);
            var br = parseInt( YDom.getStyle(dragEl, "borderRightWidth"  ), 10);
            var bb = parseInt( YDom.getStyle(dragEl, "borderBottomWidth" ), 10);
            var bl = parseInt( YDom.getStyle(dragEl, "borderLeftWidth"   ), 10);

            if (isNaN(bt)) { bt = 0; }
            if (isNaN(br)) { br = 0; }
            if (isNaN(bb)) { bb = 0; }
            if (isNaN(bl)) { bl = 0; }

            var newWidth  = Math.max(0, el.offsetWidth  - br - bl);                                                                                           
            var newHeight = Math.max(0, el.offsetHeight);

            YDom.setStyle( dragEl, "width",  newWidth  + "px" );
            YDom.setStyle( dragEl, "height", newHeight + "px" );
		},

		/* --- UI Functions --- */
		addComponent : function (src, dest, callback) {

			var srcEl, srcContainer, destContainer;

	       	if (src && dest) {
	       		// We make sure the source and destination elements are properly formed
	       		srcContainer = YDom.getAncestorByClassName(src, dcContainerClass);
	       		destContainer = YDom.getAncestorByClassName(dest, dcContainerClass);
	       		
	       		srcEl = new YElement(src);
				srcEl.appendTo(dest);
				YAHOO.util.DragDropMgr.refreshCache();

				if (srcContainer) {
					// If it's a new component it will not have a container
					this.refreshPlaceholderHeight(srcContainer);
				}
				this.refreshPlaceholderHeight(destContainer);
				componentsUpdated = true;
			}
			if (typeof callback == "function") {
		        callback();
		    }
		},		

		moveComponent : function (src, dest, goingUp, callback) {

			var srcContainer, destContainer;

	       	if (src && dest) {
	       		// We make sure the source and destination elements are properly formed
	       		srcContainer = YDom.getAncestorByClassName(src, dcContainerClass);
	       		destContainer = YDom.getAncestorByClassName(dest, dcContainerClass);
	       		
				if (goingUp) {
					YDom.insertBefore(src, dest); // insert above
				} else {
					YDom.insertAfter(src, dest);
				}
				YAHOO.util.DragDropMgr.refreshCache();

				if (srcContainer) {
					// If it's a new component it will not have a container
					this.refreshPlaceholderHeight(srcContainer);
				}
				this.refreshPlaceholderHeight(destContainer);
				componentsUpdated = true;
			}
			if (typeof callback == "function") {
		        callback();
		    }
		},

		removeComponent : function (srcEl, callback) {
			var srcWrapper = YDom.getAncestorByClassName(srcEl, dcWrapperClass),
				srcContainer = YDom.getAncestorByClassName(srcEl, dcContainerClass);

			srcEl = srcWrapper ? srcWrapper : srcEl;
			srcEl.parentNode.removeChild(srcEl);

			Utility.refreshPlaceholderHeight(srcContainer);

			if (typeof callback == "function") {
		        callback();
		    }
		}
	};
	
	DragAndDropDecorator = function(id, sGroup, config) {
	
	    DragAndDropDecorator.superclass.constructor.call(this, id, sGroup, config);
	
	    var el = this.getDragEl();
	    var zIndexRef = (CStudioAuthoring.ComponentsPanel.dcOverlay) ? CStudioAuthoring.ComponentsPanel.dcOverlay.getzIndex() : 0;
	    YDom.setStyle(el, "opacity", 0.92); // The proxy is slightly transparent
	    YDom.setStyle(el, "z-index", zIndexRef + 2);
	    YDom.addClass(el, "ddproxy");
	
	    this.goingUp = false;
	    this.lastY = 0;
	    this.dragging = false;
	};
	
	YAHOO.extend(DragAndDropDecorator, YAHOO.util.DDProxy, {
	
	    startDrag: function(x, y) {
	    	// make the proxy look like the source element
	        var srcEl = this.getEl(),
	        	proxy = this.getDragEl(),
	        	zIndexRef;

	        if (!this.dragging) {
	        	this.dragging = true;

		        	if (YDom.hasClass(srcEl, dcNewComponentClass)) {
		        	zIndexRef = CStudioAuthoring.ComponentsPanel.getMaxzIndex(srcEl, 1);

		        	YDom.setStyle(proxy, "z-index", zIndexRef + 2);
		        	proxy.style.background = CStudioAuthoring.ComponentsPanel.getInheritedBackground(srcEl);
		        	proxy.style.borderColor = "#86BBEA";
		        	proxy.style.borderRadius = "5px";
		        	proxy.innerHTML = srcEl.parentNode.innerHTML;

		        	YDom.setStyle( proxy, "width",  "232px" );
	            YDom.setStyle( proxy, "height", "auto" );

		        } else {
		        	zIndexRef = (CStudioAuthoring.ComponentsPanel.dcOverlay) ? CStudioAuthoring.ComponentsPanel.dcOverlay.getzIndex() : 0;
				    YDom.setStyle(proxy, "z-index", zIndexRef + 2);
		        	Utility.resizeProxy(srcEl, proxy);
		        	proxy.style.background = CStudioAuthoring.ComponentsPanel.getInheritedBackground(srcEl);
		        	proxy.innerHTML = srcEl.innerHTML;
		        }
	        }
	    },
	
	    endDrag: function(e) {
	        var srcEl = this.getEl(),
	        	proxy = this.getDragEl();

	        if (srcEl.componentPlaceholder) {
	        	srcEl = srcEl.componentPlaceholder;
	        }

	        // Show the proxy element and animate it to the src element's location
	        YDom.setStyle(proxy, "visibility", "");
	        var a = new YAHOO.util.Motion( 
	            proxy, { 
	                points: { 
	                    to: YDom.getXY(srcEl)
	                }
	            }, 
	            0.2, 
	            YAHOO.util.Easing.easeOut 
	        );
	        var proxyid = proxy.id;
	        var thisid = this.id;
	
	        // Hide the proxy and show the source element when finished with the animation
	        a.onComplete.subscribe(function() {
	                YDom.setStyle(proxyid, "visibility", "hidden");
	                YDom.setStyle(thisid, "visibility", "");
	            });
	        a.animate();
	    },

	    onInvalidDrop : function (e) {
	    	var srcEl = this.getEl(),
	        	proxy = this.getDragEl();

	        this.dragging = false;
			Utility.resizeProxy(srcEl, proxy);
			CStudioAuthoring.ComponentsPanel.clearActive();

			if (componentsUpdated) {
				componentsUpdated = false;	// reset flag
				CStudioAuthoring.ComponentsPanel.getPageModel(CStudioAuthoring.ComponentsPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "save-components", true, false);
			}
	    },

		onDragDrop: function(e, id) {

			var srcEl = this.getEl(),
	        	proxy = this.getDragEl();

	        this.dragging = false;
			Utility.resizeProxy(srcEl, proxy);
			CStudioAuthoring.ComponentsPanel.clearActive();

			if (componentsUpdated) {
				componentsUpdated = false;	// reset flag
				// var destEl = YDom.get(id);
				// var destDD = YAHOO.util.DragDropMgr.getDDById(id);
						
		 		if (YDom.hasClass(srcEl, dcNewComponentClass)) {
		 			// We're adding a new component
		 			var componentPath = srcEl.getAttribute('data-path'),
		 				componentType = srcEl.getAttribute('data-type'),
		 				cpl;

		 			// The component placeholder includes the component wrapper; however, the model needs to be attached to the component
		 			// element (the one with the dcNewComponentClass) so we look for it inside the component placeholder.
		 			cpl = YAHOO.util.Selector.query("." + dcNewComponentClass, srcEl.componentPlaceholder, true);
		 			
		 			 CStudioAuthoring.Operations.performSimpleIceEdit(
		 			 	{ contentType: componentType, uri: componentPath }, 
		 			 	null, 
		 			 	false, {
		 			 		success: function(contentTO) {
		 			 			// Use the information from the newly created component entry and use it to load the model data for the 
		 			 			// component placeholder in the UI. After this update, we can then proceed to save all the components
		 			 			var value = (!!contentTO.item.internalName) ? contentTO.item.internalName : contentTO.item.uri;

		 			 			// TO-DO: Create a process for storing the model data instead of doing it manually. We should not need 
		 			 			// to know what properties a new component has.
		 			 			srcEl.componentPlaceholder.modelData = { key: contentTO.item.uri, value: value, include: contentTO.item.uri, };	
		 			 			cpl.modelData = { key: contentTO.item.uri, value: value, include: contentTO.item.uri };
						 		CStudioAuthoring.ComponentsPanel.getPageModel(CStudioAuthoring.ComponentsPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "save-components-new", true, false);
							},
		 			 		failure: function() {
		 			 		}
		 			 });		 			
		 		}
		 		else {
					CStudioAuthoring.ComponentsPanel.getPageModel(CStudioAuthoring.ComponentsPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "save-components", true, false);
		 		}
			}			 	
	   	},

	    onDrag: function(e) {
	        // Keep track of the direction of the drag for use during onDragOver
	        var y = YAHOO.util.Event.getPageY(e);
	
	        if (y < this.lastY) {
	            this.goingUp = true;
	        } else if (y > this.lastY) {
	            this.goingUp = false;
	        }
	
	        this.lastY = y;		
	    },
	
	    onDragEnter: function(e, id) {
	    	var proxy = this.getDragEl(),
	    		srcEl = this.getEl(),
				destEl = YDom.get(id),
				destContainer = YDom.getAncestorByClassName(destEl, dcContainerClass),
				srcContainer = YDom.getAncestorByClassName(srcEl, dcContainerClass),
				destTemp, 
				destActive;

			// Assign or re-assign the active class for container
			destActive = YDom.getElementsByClassName(dcContainerClass + "-active", "div")[0];
			destTemp = (YDom.hasClass(destEl, dcContainerClass)) ? destEl : destContainer;

			if (!destActive) {
				YDom.addClass(destTemp, dcContainerClass + "-active");
			} else {
				if (destActive !== destTemp) {
					YDom.removeClass(destActive, dcContainerClass + "-active");
					YDom.addClass(destTemp, dcContainerClass + "-active");
				}
			}

			if (srcEl.componentPlaceholder) {
				// We're moving a new component that already has a placeholder
				srcEl = srcEl.componentPlaceholder;
				srcContainer = YDom.getAncestorByClassName(srcEl, dcContainerClass);
			}

			if (!srcContainer && YDom.hasClass(srcEl, dcNewComponentClass)) {
				// We're adding a new component

				if (destContainer) {
					// console.log("Moving a new component to a container with elements");

					srcEl.componentPlaceholder = CStudioAuthoring.ComponentsPanel.createNewComponentPlaceholder(srcEl);
					destEl = YDom.hasClass(destEl, dcWrapperClass) ? destEl : YDom.getAncestorByClassName(destEl, dcWrapperClass);
					Utility.moveComponent(srcEl.componentPlaceholder, destEl, this.goingUp);
				} else {
					destTemp = YAHOO.util.Selector.query(".cstudio-ice div:last-child", destEl, true);
					
					if (YDom.hasClass(destEl, dcContainerClass) && !YDom.getFirstChild(destTemp)) {
						// console.log("Adding a new component to an empty container");
						srcEl.componentPlaceholder = CStudioAuthoring.ComponentsPanel.createNewComponentPlaceholder(srcEl);
						Utility.addComponent(srcEl.componentPlaceholder, destTemp);
					}		
				}
			}
			else if (srcContainer && destContainer &&
				!YDom.isAncestor(destEl, srcEl) && (srcEl !== destEl)) {
				// console.log("Moving an existing component to a container with elements");

				// Only process enter events for elements that are within elements with the 'dcContainerClass', the destination element is not 
				// an ancestor of the source element nor are the destination and source elements the same.
				
				// Make sure to move around the draggable component wrappers (which include the components' controls) and not just the draggable components
				srcEl = YDom.hasClass(srcEl, dcWrapperClass) ? srcEl : YDom.getAncestorByClassName(srcEl, dcWrapperClass);
				destEl = YDom.hasClass(destEl, dcWrapperClass) ? destEl : YDom.getAncestorByClassName(destEl, dcWrapperClass);

				Utility.moveComponent(srcEl, destEl, this.goingUp);
			} else {
				// Only insert an element to a container (with dcContainerClass) if it isn't the same container that has the source element
				// and it does not have any child elements

				destTemp = YAHOO.util.Selector.query(".cstudio-ice div:last-child", destEl, true);

				if (!destContainer && YDom.hasClass(destEl, dcContainerClass) && (destEl !== srcContainer) && 
					!YDom.getFirstChild(destTemp)) {
					// console.log("Moving an existing component to an empty container");
					srcEl = YDom.hasClass(srcEl, dcWrapperClass) ? srcEl : YDom.getAncestorByClassName(srcEl, dcWrapperClass);
					Utility.addComponent(srcEl, destTemp);
				}
			}
	    },
	    
	    onDragOut: function(e, id) {
			// var destEl = YDom.get(id);
			// destEl = YDom.getAncestorByClassName(destEl, dcContainerClass);
			// YDom.removeClass(destEl, dcContainerClass + "-active");
	    }
	});
	
	CStudioAuthoring.Module.moduleLoaded("component-panel", CStudioAuthoring.ComponentsPanel);

} });
} });