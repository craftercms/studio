CStudioAuthoring.Utils.addJavascript(CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/yui/carousel/carousel-min.js");

CStudioAuthoring.TargetingPanel = CStudioAuthoring.TargetingPanel || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			
			this.initialized = true;
		}
	},
	
	render: function(containerEl, config) {
	},
	
	firstExpand: function(containerEl, config) {

		CStudioAuthoring.Service.lookupConfigurtion(
				CStudioAuthoringContext.site, 
				"/targeting/personas/personas-config.xml", 
				{
					success: function(config) {
						var userRotateHtml = "<div id='preview-tools-panel-persona-selected' ></div><div id='preview-tools-panel-persona-selected-title' ></div><div id='container'><ol id=''> </ol> </div>";

                        userRotateContainerEl = document.createElement("div");
                        userRotateContainerEl.innerHTML = userRotateHtml;
						containerEl.appendChild(userRotateContainerEl);

						var sliderEl = document.getElementById('container');
						
						var personalListEl = document.createElement("OL");
						personalListEl.id = "craftercmspersonacarousel";
						sliderEl.appendChild(personalListEl);
						
						if(!config.length) {
							config = [config.persona];
						}
						
						for(var i=0; i<config.length; i++) {
							var personaEl = document.createElement("LI");
							var personaImgEl = document.createElement("IMG");
							personaImgEl.style.height = "50px";
							personaImgEl.style.width = "50px";

							personaEl.appendChild(personaImgEl);

							personaEl.personaName = config[i].name;
							personaEl.personaDescription = config[i].description;
							
							if(config[i].settings) {
								if(config[i].settings.property) {
									if(config[i].settings.property.length) {
										
									}
									else {
										personaEl.personaProps = [];
									}
								}
								else {
									personaEl.personaProps = [];
								}								
							}
							else {
								personaEl.personaProps = [];
							}

							personaEl.personaDescription = config[i].description;
							personaImgEl.src = CStudioAuthoringContext.baseUri + '/api/1/services/api/1/content/get-content-at-path.bin?path=/cstudio/config/sites/' + CStudioAuthoringContext.site + "/targeting/personas/thumbs/"+config[i].thumb;
							
							personalListEl.appendChild(personaEl);
						}

						
						
						var spotlightEl = document.getElementById('preview-tools-panel-persona-selected');
						var spotlightTitleEl = document.getElementById('preview-tools-panel-persona-selected-title');

					    var carousel = new YAHOO.widget.Carousel(sliderEl, {
					      isCircular: true,
					      numVisible: 4
					    });

						carousel.parentControl = this;
						var getImageFn = function(parent) {
							var el = parent.firstChild;
  							while (el) {  
    							if (el.nodeName.toUpperCase() == "IMG") { 	
							      return el.src.replace(/_s\.jpg$/, "_m.jpg");
    							}
    							el = el.nextSibling;
  							}
							
							return "";
						};	

						CStudioAuthoring.TargetingPanel.carousel = carousel;
						carousel.getImageFn = getImageFn;
						carousel.spotlightTitleEl = spotlightTitleEl;
						carousel.spotlightEl = spotlightEl;
						carousel.personas = config;
						carousel.on("itemSelected", CStudioAuthoring.TargetingPanel.selectPersona);
						 
					    carousel.render();
					    carousel.show();


					
					var getCurrentCallback = {
						success: function(oResponse) {
							var json = oResponse.responseText;
							var currentProfile = eval("(" + json + ")");
							
							for(var i=0; i<config.length; i++) {
								if(config[i].name.toLowerCase() == currentProfile.username.toLowerCase()) {
									carousel.activePersona = currentProfile;
									CStudioAuthoring.TargetingPanel.selectPersona(i);
									break;
								}
							}
							
							if(!carousel.activePersona) {
								for(var i=0; i<config.length; i++) {
									if(config[i].name.toLowerCase() == "anonymous") {
										persona = config[i];
										carousel.activePersona = {"username":"Anonymous"};
										CStudioAuthoring.TargetingPanel.selectPersona(i);
										break;		
									}
								}	
							} 

						},
						failure: function() {
						}
					};
					
					var serviceUri = CStudioAuthoring.Service.createEngineServiceUri("/api/1/profile/get");
					YConnect.asyncRequest('GET', serviceUri, getCurrentCallback);


					},
				
					failure: function() {
					},
				
					context: this
				});	
	},
	
	
	expand: function(containerEl, config) {
		var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");
							
		if(reportContainerEl) {
			document.body.removeChild(reportContainerEl);
		}
		
		var reportContainerEl = document.createElement("div");
			reportContainerEl.id = "cstudioPreviewAnalyticsOverlay";
			YAHOO.util.Dom.addClass(reportContainerEl, "cstudio-analytics-overlay");
		
			reportContainerEl.style.position = "fixed";
			reportContainerEl.style.width = "800px";
			reportContainerEl.style.height = "300px";
			reportContainerEl.style.top = "100px";
		
			var x = (window.innerWidth / 2) - (reportContainerEl.offsetWidth / 2) - 400;
			reportContainerEl.style.left = x+"px";

			document.body.appendChild(reportContainerEl);
			var carousel = CStudioAuthoring.TargetingPanel.carousel;

			if(carousel) {
				var getCurrentCallback = {
					success: function(oResponse) {
						var json = oResponse.responseText;
						var currentProfile = eval("(" + json + ")");
						
						for(var i=0; i<carousel.personas.length; i++) {
							if(carousel.personas[i].name == currentProfile.username) {
								carousel.activePersona = currentProfile;
								CStudioAuthoring.TargetingPanel.selectPersona(i);
								break;
							}
						}
					},
					failure: function() {
					}
				};
				
				var serviceUri = CStudioAuthoring.Service.createEngineServiceUri("/api/1/profile/get");
				//YConnect.asyncRequest('GET', serviceUri, getCurrentCallback);
			}
	},

	collapse: function(containerEl, config) {
		var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");
							
		if(reportContainerEl) {
			document.body.removeChild(reportContainerEl);
		}
	},
		
	/**
	 * string trim
	 */
	trim: function(s) {
		return s.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,'').replace(/\s+/g,' ');
	},
	
	/**
	 * function is called when user browses to a persona in preview tools
	 */
	selectPersona: function (index) { 
		
		var carousel = CStudioAuthoring.TargetingPanel.carousel;
		var spotlightTitleEl = carousel.spotlightTitleEl;
		var spotlightEl = carousel.spotlightEl;

		var item = carousel.getElementForItem(index);
		var persona = carousel.personas[index]; 
		
		carousel.activePersonaAttributes = [];
		
        if(!carousel.activePersona.attributes) {
            carousel.activePersona.attributes = [];
        }
		
        for(key in carousel.activePersona) {
			carousel.activePersonaAttributes[key] = carousel.activePersona[key];
		}
		

							if (item) { 
								spotlightEl.innerHTML = "<img width='100px' height='100px' src=\"" + carousel.getImageFn(item) + "\">"; 
								spotlightTitleEl.innerHTML = item.personaName;
						 	}
						 	
						 	var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");
							
							var overlayHtml = "";
							if(reportContainerEl) {
								overlayHtml = 
									"<div style='color: white; font-weight: bold; font-size: 20px; font-family: Arial,Helvetica; margin: 15px;'>Persona: "+item.personaName;
									
									if(carousel.activePersona.username == item.personaName) {
										overlayHtml +=	" (Active) "
									}
									
								overlayHtml +=	
									"</div>" +
									"<img style='position: static; float: left; border:1px solid white; margin-left:15px; margin-right:15px;' width='150px' height='150px' src='" + carousel.getImageFn(item) + "'\>" +
									"<div style='color: white; font-size: 15px; font-family: Arial,Helvetica; margin: 15px;'>"+item.personaDescription+"</div>" +
									"<div style='color: white; font-weight: bold; font-size: 20px; font-family: Arial,Helvetica; margin: 15px;'>Properties</div>";
									
									
									
									
										overlayHtml += "<table>";
									 
									if(persona.settings) {
										if(persona.settings.property || persona.settings.length) {	
											if(!persona.settings.length) {
												persona.settings = [persona.settings.property];
											}							
											
											for(var j=0; j<persona.settings.length; j++){
												var property =  persona.settings[j];
												
												overlayHtml += "<tr style='color:white'>" +
												     "<td style='font-weight:bold' >" + property.label + "</td>";
												     
												     if(carousel.activePersona.username == item.personaName) {
												     	overlayHtml += 
												     	 "<td><input id='crPersona_" + property.name +"' "+ 
												     	        "value='" + carousel.activePersonaAttributes[property.name] + "'/></td>"; 
												     }
												     else {
												     	overlayHtml += "<td id='crPersona_" + property.name +"'>" + 
												     	                property.value + "</td>";
												     }
												     
												     overlayHtml +=
												     "</tr>";	
											}
										}
									 }
									  overlayHtml +=  "</table>";
									
									
									

									if(carousel.activePersona.username != item.personaName) {
										overlayHtml +=	
									"<div id='csupdatepersona' style='line-height: 30px; text-align: center; width: 120px; height: 30px; float: right; background-image: url("+CStudioAuthoringContext.baseUri +"/static-assets/themes/cstudioTheme/images/overlay-button.png); color: white; font-weight: bold; font-size: 16px; font-family: Arial,Helvetica; cursor: pointer;  bottom:0px; position:absolute; right:0px;'>Assume</div>";
									}
									else {
									overlayHtml +=	
									"<div id='csupdatepersona' style='line-height: 30px; text-align: center; width: 120px; height: 30px; float: right; background-image: url("+CStudioAuthoringContext.baseUri +"/static-assets/themes/cstudioTheme/images/overlay-green-button.png); color: white; font-weight: bold; font-size: 16px; font-family: Arial,Helvetica; cursor: pointer;  bottom:0px; position:absolute; right:0px;'>Update</div>";
									}
									
									
									reportContainerEl.innerHTML = overlayHtml;
									
									
									//if(carousel.activePersona != item.personaName) {
										var assumePersonaEl = document.getElementById("csupdatepersona");
										
										//assumePersonaEl.toggleFn = this.parentControl.context.toggleFn;
										assumePersonaEl.onclick = function(e) {
	
											var setCurrentCallback = {
												success: function(oResponse) {
													var json = oResponse.responseText;
													//this.control.toggleFn(this.event);
													window.location.reload();
												},
												failure: function() {
												},
												
												control: this,
												
												event: e
											};
											
											var serviceUri = "/api/1/profile/set" + 
												"?username=" + item.personaName;
											for(var l=0; l<persona.settings.length; l++){
												var property =  persona.settings[l];
												var valEl = document.getElementById("crPersona_" + property.name);
												var val =  (valEl.tagName == "INPUT") ? valEl.value : valEl.innerHTML;
												serviceUri+="&"+property.name + "=" + val.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,'').replace(/\s+/g,' ');
;	
											}
	
											YConnect.asyncRequest('GET', CStudioAuthoring.Service.createEngineServiceUri(serviceUri), setCurrentCallback);
										}; 
									//}
							} 
						}
}

CStudioAuthoring.Module.moduleLoaded("targeting-panel", CStudioAuthoring.TargetingPanel);