/* global CStudioAuthoring, CStudioAuthoringContext, CStudioForms, YAHOO, CodeMirror, tinymce */

CStudioAuthoring.Module.requireModule(
	'codemirror',
	'/static-assets/components/cstudio-common/codemirror/lib/codemirror.js', {}, {
	moduleLoaded: function() {

		CStudioAuthoring.Utils.addJavascript('/static-assets/components/cstudio-common/codemirror/mode/xml/xml.js');
		CStudioAuthoring.Utils.addJavascript('/static-assets/components/cstudio-common/codemirror/mode/javascript/javascript.js');
		CStudioAuthoring.Utils.addJavascript('/static-assets/components/cstudio-common/codemirror/mode/htmlmixed/htmlmixed.js');
		CStudioAuthoring.Utils.addJavascript('/static-assets/components/cstudio-common/codemirror/mode/css/css.js');
		CStudioAuthoring.Utils.addCss('/static-assets/components/cstudio-common/codemirror/lib/codemirror.css');
		CStudioAuthoring.Utils.addCss('/static-assets/themes/cstudioTheme/css/template-editor.css');

		var YDom = YAHOO.util.Dom,
			componentSelector = '.crComponent',
			buttonStateArr;

		CStudioForms.Controls.RTE.EditHTML = CStudioForms.Controls.RTE.EditHTML || {
			init: function(ed, url) {
				var t = this;

				ed.addButton('edithtml', {
					title: 'Edit Code',
					image: CStudioAuthoringContext.authoringAppBaseUri + '/static-assets/themes/cstudioTheme/images/icons/code-edit.gif',
					onclick: function(e) {

						if (!this.controlManager.get('edithtml').active) {
							// Enable code view
							this.controlManager.setActive('edithtml', true);
							t.enableCodeView(ed);
						} else {
							// Disable code view
							this.controlManager.setActive('edithtml', false);
							t.disableCodeView(ed);
						}
					}
				});
			},

			resizeCodeView: function(editor, defaults) {
				var rteControl = editor.contextControl,
					cmWidth = rteControl.containerEl.clientWidth - rteControl.codeModeXreduction;

				// Reset the inline styles
				defaults.forEach(function(el) {
					for (var style in el.styles) {
						el.element.style[style] = el.styles[style];
					}
				});
				editor.codeMirror.setSize(cmWidth, 100); // the scrollheight depends on the width of the codeMirror so first we set the width (the 100 value could be replaced for anything)
				editor.contextControl.resizeCodeMirror(editor.codeMirror);
			},

			/*
			 * Look for all the components in the editor's content. Detach each component body from the DOM (so it's not visible in code mode)
			 * and attach it to the component root element as a DOM attribute. When switching back to text mode, the component body will be
			 * re-attached where it was
			 *
			 * @param editor
			 * @param componentSelector : css selector used to look for all components in the editor
			 */
			collapseComponents: function collapseComponents(editor, componentSelector) {
				var componentsArr = YAHOO.util.Selector.query(componentSelector, editor.getBody());

				editor['data-components'] = {};

				componentsArr.forEach(function(component) {
					editor['data-components'][component.id] = Array.prototype.slice.call(component.childNodes); // Copy children and store them in an attribute
					component.innerHTML = '';
				});
			},

			extendComponents: function extendComponents(editor, componentSelector) {
				var componentsArr = YAHOO.util.Selector.query(componentSelector, editor.getBody());

				componentsArr.forEach(function(component) {
					component.innerHTML = ''; // Replace any existing content with the original component content
					// The user may have changed the component id so test to see if the component ID exists
					if (editor['data-components'][component.id]) {
						editor['data-components'][component.id].forEach(function(child) {
							component.appendChild(child); // restore component children
						});
					}
				});
				delete editor['data-components'];
			},

			getEditorControlsStates: function getEditorControlsStates(editor) {
				var buttonStateArr = [],
					edControls = editor.controlManager.controls,
					buttonObj;
				for (var b in edControls) {

					// Filter out any inherited properties (e.g. prototype) and more complex controls that include controls themselves
					if (edControls.hasOwnProperty(b) && !edControls[b].controls && !/_edithtml/.test(b)) {
						buttonObj = {
							id: b,
							isDisabled: edControls[b].isDisabled()
						};
						buttonStateArr.push(buttonObj);
					}
				}
				return buttonStateArr;
			},

			disableTextControls: function disableTextControls(editor, controlsArr) {
				var edControls = editor.controlManager.controls;
				controlsArr.forEach(function(controlObj) {
					if (!controlObj.isDisabled) {
						edControls[controlObj.id].setDisabled(true);
					}
				});
			},

			enableTextControls: function enableTextControls(editor, controlsArr) {
				var edControls = editor.controlManager.controls;
				controlsArr.forEach(function(controlObj) {
					if (!controlObj.isDisabled) {
						edControls[controlObj.id].setDisabled(false);
					}
				});
			},

			enableCodeView: function enableCodeView(editor) {
				var rteControl = editor.contextControl,
					rteContainer = YAHOO.util.Selector.query('.cstudio-form-control-rte-container', rteControl.containerEl, true);

				// A meta node used to dispatch an artificial event. The reason to use a meta node is because it is VERY unlikely 
				// that any buttons will ever respond to changes on a node of this kind.
				var metaNode = document.createElement('meta');

				editor.onDeactivate.dispatch(editor, null); // Fire tinyMCE handlers for onDeactivate

				// Clear any selections on the text editor, then dispatch an artificial event so all buttons go back to their 
				// default state before saving their state. Then, when we restore the buttons' state (when we go back to text mode),
				// we'll have their default state again!
				rteControl.clearTextEditorSelection();
				editor.onNodeChange.dispatch(editor, editor.controlManager, metaNode, true, editor);

				buttonStateArr = this.getEditorControlsStates(editor);
				this.disableTextControls(editor, buttonStateArr);
				YDom.replaceClass(rteControl.containerEl, 'text-mode', 'code-mode');
				this.collapseComponents(editor, componentSelector);
				editor.codeTextArea.value = editor.getContent();

				if (!editor.codeMirror) {
					// console.log('Loading codeMirror');
					editor.codeMirror = CodeMirror.fromTextArea(editor.codeTextArea, {
						mode: 'htmlmixed',
						lineNumbers: true,
						lineWrapping: true,
						smartIndent: true, // Although this won't work unless there are opening and closing HTML tags
						onFocus: function() {
							rteControl.form.setFocusedField(rteControl);
						},
						onChange: function(ed) {
							rteControl.resizeCodeMirror(ed);
                            rteControl.edited = true;
						}
					});
				} else {
					// Bug fix for IE -9: flush the contents of the input field that stores selected content so that it is
					// not inserted into the code editor
					if (YAHOO.env.ua.ie <= 9 && 
								editor.codeMirror.getSelection() != "" && 
								editor.codeMirror.getSelection() === editor.codeMirror.getInputField().value) {

						editor.codeMirror.setSelection({line: 0, ch: 0});	
						editor.codeMirror.getInputField().value = "";
					}

					// Set the cursor to the beginning of the code editor; this will clear any text selection in
					// codeMirror -if there's any
					editor.codeMirror.setCursor({line: 0, ch: 0});
					editor.codeMirror.setValue(editor.codeTextArea.value);
				}
				// We resize codeMirror each time in case the user has resized the window
				this.resizeCodeView(editor, [{
					'element': rteContainer,
					'styles': {
						'maxWidth': 'none',
						'width': 'auto',
						'marginLeft': 'auto'
					}
				}, {
					'element': YDom.get(editor.id + '_tbl'),
					'styles': {
						'width': 'auto',
						'height': 'auto'
					}
				}]);
				editor.codeMirror.focus();
				editor.codeMirror.scrollTo(0, 0); // Scroll to the top of the editor window
				rteControl.scrollToTopOfElement(rteControl.containerEl, 30);
                editor.codeMirror.onChange()
			},

			disableCodeView: function(editor) {
				var rteControl = editor.contextControl,
					rteContainer = YAHOO.util.Selector.query('.cstudio-form-control-rte-container', rteControl.containerEl, true);

				editor.setContent(editor.codeMirror.getValue());
				this.extendComponents(editor, componentSelector);
				rteControl.resizeTextView(rteControl.containerEl, rteControl.rteWidth, {
					'rte-container': rteContainer,
					'rte-table': YDom.get(editor.id + '_tbl')
				});
				YDom.replaceClass(rteControl.containerEl, 'code-mode', 'text-mode');
				this.enableTextControls(editor, buttonStateArr);
				editor.getWin().scrollTo(0, 0); // Scroll to the top of the editor window

				rteControl.clearTextEditorSelection();
				editor.focus();

				rteControl.scrollToTopOfElement(rteControl.containerEl, 30);
			},

			createControl: function(n, cm) {
				return null;
			},

			getInfo: function() {
				return {
					longname: 'Crafter Studio Edit Code',
					author: 'Crafter Software',
					authorurl: 'http://www.craftercms.org',
					infourl: 'http://www.craftercms.org',
					version: '1.0'
				};
			}
		};

		tinymce.create('tinymce.plugins.CStudioEditHTMLPlugin', CStudioForms.Controls.RTE.EditHTML);
		tinymce.PluginManager.add('edithtml', tinymce.plugins.CStudioEditHTMLPlugin);

		CStudioAuthoring.Module.moduleLoaded('cstudio-forms-controls-rte-edit-html', CStudioForms.Controls.RTE.EditHTML);

	}
});