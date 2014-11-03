CStudioForms.Controls.RTE.ImageInsert = CStudioForms.Controls.RTE.ImageInsert || {
                /**
                 * Initializes the plugin
                 * @param {tinymce.Editor} ed Editor instance that the plugin is initialized in.
                 * @param {string} url Absolute URL to where the plugin is located.
                 */
                init : function(ed, url) {

                        var _self = this;
               			var beforeSaveCb = {
							beforeSave: function() {
								
                       			var docBodyEl = ed.dom.doc.body;
								
								if (docBodyEl){
									var imgEls = docBodyEl.getElementsByTagName("img");

                                    if (imgEls.length > 0) {
                                        for(var i = --imgEls.length; i>=0; i--){
                                            var currentEl = imgEls[i];
                                            
                                            var relativeUrl = _self.cleanUrl(currentEl.src);
                                            currentEl.setAttribute("src" , relativeUrl);
                                            currentEl.setAttribute("data-mce-src" , relativeUrl);
                                        }
                                        ed.contextControl.save();
                                    }
								}
							},
							editor: ed
						};

						ed.contextControl.form.registerBeforeSaveCallback(beforeSaveCb);

                        // Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceExample');
                        ed.addCommand('mceInsertManagedImage', function(param, datasource) {
                        	
                        	if(datasource) {
                        		if(datasource.insertImageAction) {
	                        		datasource.insertImageAction({
	                        			success: function(imageData) {
	                        				ed.execCommand('mceInsertContent', false, '<img src="' + imageData.previewUrl + '" />');
	                        				ed.contextControl.save();
	                        			},
	                        			failure: function(message) {
	                        				alert(message);
	                        			}
	                        		});
                        		}
                        		else {
                        		    alert("The configured datasource is not an image manager");
                        		}
                        	}
                        	else {
                        		alert("No datasource has been associated with this editor");
                        	}
                        });

                        // Add a node change handler, selects the button in the UI when a image is selected
                        ed.onNodeChange.add(function(ed, cm, n) {
                                cm.setActive('managedImage', n.nodeName == 'IMG');
                        });
                },

                cleanUrl: function (url) {
                    var urlVal = url.replace(CStudioAuthoringContext.previewAppBaseUri, "");    // remove domain name & junction (optional) from image URL
                    return urlVal;
                },

                createControl: function(name, cm) {

                    var imageManagerNames = cm.editor.contextControl.imageManagerName;  // List of image datasource IDs, could be an array or a string

                    imageManagerNames = (!imageManagerNames) ? "" :
                                        (Array.isArray(imageManagerNames)) ? imageManagerNames.join(",") : imageManagerNames;  // Turn the list into a string

                    if (name == "managedImage" && imageManagerNames) {
                        var c = cm.createMenuButton('managedImage', {
                            title: 'Insert Image',
                            image: CStudioAuthoringContext.authoringAppBaseUri + '/static-assets/themes/cstudioTheme/images/insert_image.png',
                            icons: false
                        });

                        c.onRenderMenu.add( function(c, m) {
                            var datasourceMap = this.editor.contextControl.form.datasourceMap,
                                datasourceDef = this.editor.contextControl.form.definition.datasources;
                                // The datasource title is only found in the definition.datasources. It'd make more sense to have all
                                // the information in just one place.

                            var addMenuOption = function (el) {
                                // We want to avoid possible substring conflicts by using a reg exp (a simple indexOf
                                // would fail if a datasource id string is a substring of another datasource id)
                                var regexpr = new RegExp("(" + el.id + ")[\\s,]|(" + el.id + ")$"),
                                    mapDatasource;

                                if (imageManagerNames.search(regexpr) > -1) {
                                    mapDatasource = datasourceMap[el.id];

                                    this.add({
                                        title: el.title,
                                        onclick: function() {
                                            tinyMCE.activeEditor.execCommand('mceInsertManagedImage', false, mapDatasource);
                                        }
                                    });
                                }
                            }
                            datasourceDef.forEach(addMenuOption, m);
                        });

                        // Return the new menu button instance
                        return c;
                    }
                    return null;
                },

                /**
                 * Returns information about the plugin as a name/value array.
                 * The current keys are longname, author, authorurl, infourl and version.
                 *
                 * @return {Object} Name/value array containing information about the plugin.
                 */
                getInfo : function() {
                        return {
                                longname : 'Crafter Studio Insert Image',
                                author : 'Crafter Software',
                                authorurl : 'http://www.craftercms.org',
                                infourl : 'http://www.craftercms.org',
                                version : "1.0"
                        };
                }

}

tinymce.create('tinymce.plugins.CStudioManagedImagePlugin', CStudioForms.Controls.RTE.ImageInsert);

// Register plugin with a short name
tinymce.PluginManager.add('insertimage', tinymce.plugins.CStudioManagedImagePlugin);

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-image", CStudioForms.Controls.RTE.ImageInsert);