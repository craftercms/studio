CStudioAuthoring.Module.requireModule("codemirror","/components/cstudio-common/codemirror/lib/codemirror.js",{},{moduleLoaded:function(){CStudioAuthoring.Utils.addJavascript("/components/cstudio-common/codemirror/mode/xml/xml.js");
CStudioAuthoring.Utils.addJavascript("/components/cstudio-common/codemirror/mode/javascript/javascript.js");
CStudioAuthoring.Utils.addJavascript("/components/cstudio-common/codemirror/mode/htmlmixed/htmlmixed.js");
CStudioAuthoring.Utils.addJavascript("/components/cstudio-common/codemirror/mode/css/css.js");
CStudioAuthoring.Utils.addCss("/components/cstudio-common/codemirror/lib/codemirror.css");
CStudioAuthoring.Utils.addCss("/themes/cstudioTheme/css/template-editor.css");
var b=YAHOO.util.Dom,d=".crComponent",f;
CStudioForms.Controls.RTE.EditHTML=CStudioForms.Controls.RTE.EditHTML||{init:function(j,k){var l=this;
j.addButton("edithtml",{title:"Edit Code",image:CStudioAuthoringContext.authoringAppBaseUri+"/themes/cstudioTheme/images/icons/code-edit.gif",onclick:function(m){if(!this.controlManager.get("edithtml").active){this.controlManager.setActive("edithtml",true);
l.enableCodeView(j)
}else{this.controlManager.setActive("edithtml",false);
l.disableCodeView(j)
}}})
},resizeCodeView:function(k,m){var j=k.contextControl,l=j.containerEl.clientWidth-j.codeModeXreduction;
m.forEach(function(o){for(var n in o.styles){o.element.style[n]=o.styles[n]
}});
k.codeMirror.setSize(l,100);
k.contextControl.resizeCodeMirror(k.codeMirror)
},collapseComponents:function h(k,l){var j=YAHOO.util.Selector.query(l,k.getBody());
k["data-components"]={};
j.forEach(function(m){k["data-components"][m.id]=Array.prototype.slice.call(m.childNodes);
m.innerHTML=""
})
},extendComponents:function e(k,l){var j=YAHOO.util.Selector.query(l,k.getBody());
j.forEach(function(m){m.innerHTML="";
if(k["data-components"][m.id]){k["data-components"][m.id].forEach(function(n){m.appendChild(n)
})
}});
delete k["data-components"]
},getEditorControlsStates:function c(l){var n=[],m=l.controlManager.controls,k;
for(var j in m){if(m.hasOwnProperty(j)&&!m[j].controls&&!/_edithtml/.test(j)){k={id:j,isDisabled:m[j].isDisabled()};
n.push(k)
}}return n
},disableTextControls:function i(k,j){var l=k.controlManager.controls;
j.forEach(function(m){if(!m.isDisabled){l[m.id].setDisabled(true)
}})
},enableTextControls:function a(k,j){var l=k.controlManager.controls;
j.forEach(function(m){if(!m.isDisabled){l[m.id].setDisabled(false)
}})
},enableCodeView:function g(m){var k=m.contextControl,l=YAHOO.util.Selector.query(".cstudio-form-control-rte-container",k.containerEl,true);
var j=document.createElement("meta");
m.onDeactivate.dispatch(m,null);
k.clearTextEditorSelection();
m.onNodeChange.dispatch(m,m.controlManager,j,true,m);
f=this.getEditorControlsStates(m);
this.disableTextControls(m,f);
b.replaceClass(k.containerEl,"text-mode","code-mode");
this.collapseComponents(m,d);
m.codeTextArea.value=m.getContent();
if(!m.codeMirror){m.codeMirror=CodeMirror.fromTextArea(m.codeTextArea,{mode:"htmlmixed",lineNumbers:true,lineWrapping:true,smartIndent:true,onFocus:function(){k.form.setFocusedField(k)
},onChange:function(n){k.resizeCodeMirror(n)
}})
}else{if(YAHOO.env.ua.ie<=9&&m.codeMirror.getSelection()!=""&&m.codeMirror.getSelection()===m.codeMirror.getInputField().value){m.codeMirror.setSelection({line:0,ch:0});
m.codeMirror.getInputField().value=""
}m.codeMirror.setCursor({line:0,ch:0});
m.codeMirror.setValue(m.codeTextArea.value)
}this.resizeCodeView(m,[{element:l,styles:{maxWidth:"none",width:"auto",marginLeft:"auto"}},{element:b.get(m.id+"_tbl"),styles:{width:"auto"}}]);
m.codeMirror.focus();
m.codeMirror.scrollTo(0,0);
k.scrollToTopOfElement(k.containerEl,30)
},disableCodeView:function(l){var j=l.contextControl,k=YAHOO.util.Selector.query(".cstudio-form-control-rte-container",j.containerEl,true);
l.setContent(l.codeMirror.getValue());
this.extendComponents(l,d);
j.resizeTextView(j.containerEl,j.rteWidth,{"rte-container":k,"rte-table":b.get(l.id+"_tbl")});
b.replaceClass(j.containerEl,"code-mode","text-mode");
this.enableTextControls(l,f);
l.getWin().scrollTo(0,0);
j.clearTextEditorSelection();
l.focus();
j.scrollToTopOfElement(j.containerEl,30)
},createControl:function(k,j){return null
},getInfo:function(){return{longname:"Crafter Studio Edit Code",author:"Crafter Software",authorurl:"http://www.craftercms.org",infourl:"http://www.craftercms.org",version:"1.0"}
}};
tinymce.create("tinymce.plugins.CStudioEditHTMLPlugin",CStudioForms.Controls.RTE.EditHTML);
tinymce.PluginManager.add("edithtml",tinymce.plugins.CStudioEditHTMLPlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-edit-html",CStudioForms.Controls.RTE.EditHTML)
}});