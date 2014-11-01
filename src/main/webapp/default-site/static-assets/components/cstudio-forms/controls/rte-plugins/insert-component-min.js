CStudioForms.Controls.RTE.InsertComponent=CStudioForms.Controls.RTE.InsertComponent||(function(){var b="<img src='"+CStudioAuthoringContext.authoringAppBaseUri+"/themes/cstudioTheme/images/wait.gif' alt='Loading ...' />",e="<img src='"+CStudioAuthoringContext.authoringAppBaseUri+"/themes/cstudioTheme/images/fail.png' alt='Loading failed' />",d,c,a;
return{init:function(g,h){var f=this;
var i={beforeSave:function(){var m=this.editor.contextControl;
var l=this.editor.dom.doc.body;
if(l){var o=YAHOO.util.Dom.getElementsByClassName("crComponent",null,l),j=o.length;
if(j>0){for(var k=0;
k<j;
k++){var n=o[k];
n.innerHTML="";
this.editor.contextControl.save();
f.renderComponents(this.editor)
}m._onChange(null,m)
}}},editor:g};
g.contextControl.form.registerBeforeSaveCallback(i)
},createControl:function(l,g){var j=g.editor,i=j.contextControl.form.model,h=j.contextControl.rteConfig.rteWidgets,f=this;
if(l=="insertComponent"){j.onLoadContent.add(function(n,m){f.renderComponents(n)
});
j.onDblClick.add(function(m,n){if(f.isControl(n.target)){f.showControls(m,f.getWidgetContainerElement(n.target))
}});
j.onClick.add(function(m,n){if(f.firstClick&&f.firstClick==true){f.firstClick=false;
return
}f.handleComponentDrop(m,n);
f.hideControls(m)
});
amplify.subscribe("/rte/blurred",function(){f.hideControls(j)
});
if(!h){h=[]
}if(typeof h=="object"&&!Array.isArray(h)){h=[h.widget]
}if(h.length>0){var k=g.createMenuButton("insertComponent",{title:"Insert Component",style:"mce_insertComponent",});
k.rteWidgets=h;
k.onRenderMenu.add(function(r,n){for(var p=0;
p<h.length;
p++){var q=h[p];
var o=function(){var s={success:function(w,u,x){var y=u.substring(u.lastIndexOf("/")+1).replace(".xml","");
if(!i.rteComponents){i.rteComponents=[]
}var v={id:y,contentId:u,include:u};
i.rteComponents[i.rteComponents.length]=v;
j.execCommand("mceInsertContent",false,'<div id="'+y+"\" class='crComponent' >"+b+"</div>");
f.renderComponent(j,v)
},failure:function(){}};
var t=this.onclick.widget.contentPath;
t=t.replace("{objectGroupId}",i.objectGroupId);
t=t.replace("{objectId}",i.objectId);
t=t.replace("{objectGroupId2}",i.objectGroupId.substring(0,2));
t=t.replace("{parentPath}",CStudioAuthoring.Utils.getQueryParameterByName("path").replace(/\/[^\/]*\/[^\/]*\/([^\.]*)(\/[^\/]*\.xml)?$/,"$1"));
var m=new Date();
t=t.replace("{year}",m.getFullYear());
t=t.replace("{month}",("0"+(m.getMonth()+1)).slice(-2));
CStudioAuthoring.Operations.openContentWebForm(this.onclick.widget.contentType,null,null,t,false,false,s,[{name:"childForm",value:"true"}])
};
o.widget=q;
n.add({title:q.name,onclick:o})
}});
return k
}}return null
},renderComponents:function(k){var g=k.contextControl.form.model;
var f=this;
var l=[];
if(g.rteComponents&&g.rteComponents.length){l=g.rteComponents
}for(var j=0;
j<l.length;
j++){var h=l[j];
this.renderComponent(k,h)
}},renderComponent:function(h,g){var j=h.dom.doc.getElementById(g.id);
var f=this;
if(j){try{j.innerHTML=b;
previewCb={success:function(k){j.innerHTML=k;
YAHOO.util.Dom.addClass(j,"mceNonEditable")
},failure:function(){j.innerHTML=e
}};
CStudioAuthoring.Service.getComponentPreview(g.contentId,previewCb)
}catch(i){}}},showControls:function(k,j){var f=this;
var i=k.dom.doc.getElementById("cstudio-component-controls");
var h=k.contextControl.form.model;
i=k.dom.doc.createElement("ul");
i.id="cstudio-component-controls";
i.className="context-menu-off";
var g="<li class='edit'><a href='#'><span class='visuallyhidden'>Edit</span></a></li><li class='move'><a href='#'><span class='visuallyhidden'>Move</span></a></li><li class='delete'><a href='#'><span class='visuallyhidden'>Delete</span></a></li>";
i.innerHTML=g;
j.appendChild(i);
d=tinymce.DOM.select("#cstudio-component-controls .move > a",k.getDoc())[0];
d.onclick=function(){var o=j.id;
var l=null;
var n=h.rteComponents;
for(var m=0;
m<n.length;
m++){if(n[m].id==o){f.componentOnTheMove=n[m];
f.firstClick=true;
k.dom.doc.body.style.cursor="move";
k.dom.doc.body.focus();
break
}}};
c=tinymce.DOM.select("#cstudio-component-controls .edit > a",k.getDoc())[0];
c.onclick=function(){var p=j.id;
var l=null;
var o=h.rteComponents;
for(var n=0;
n<o.length;
n++){if(o[n].id==p){l=o[n];
break
}}if(l){var m={success:function(r){var q={success:function(t,s,u){f.renderComponent(tinyMCE.activeEditor,l)
},failure:function(){}};
CStudioAuthoring.Operations.openContentWebForm(r.item.contentType,null,null,l.contentId,true,false,q,[{name:"childForm",value:"true"}])
},failure:function(){}};
CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site,l.contentId,m)
}};
a=tinymce.DOM.select("#cstudio-component-controls .delete > a",k.getDoc())[0];
a.onclick=function(){var n=j.id;
var m=h.rteComponents;
j.parentNode.removeChild(j);
for(var l=0;
l<m.length;
l++){if(m[l].id==n){h.rteComponents.splice(l,1);
break
}}}
},handleComponentDrop:function(g,h){var f=this;
if(f.componentOnTheMove&&h.target!==d){var i=g.dom.doc.getElementById(f.componentOnTheMove.id);
i.parentNode.removeChild(i);
tinyMCE.activeEditor.execCommand("mceInsertContent",false,'<div id="'+f.componentOnTheMove.id+"\" class='crComponent' >"+b+"</div>");
f.renderComponent(tinyMCE.activeEditor,f.componentOnTheMove);
g.dom.doc.body.style.cursor="default";
f.componentOnTheMove=null
}},hideControls:function(g){var f;
try{f=g.dom.doc.getElementById("cstudio-component-controls")
}catch(h){}if(f){f.parentNode.removeChild(f)
}},getWidgetContainerElement:function(f){if(YAHOO.util.Dom.hasClass(f,"crComponent")){return f
}else{return YAHOO.util.Dom.getAncestorByClassName(f,"crComponent")
}},isControl:function(f){var g=this.getWidgetContainerElement(f);
return(g)?true:false
}}
})();
tinymce.create("tinymce.plugins.CStudioInsertComponentPlugin",CStudioForms.Controls.RTE.InsertComponent);
tinymce.PluginManager.add("insertcomponent",tinymce.plugins.CStudioInsertComponentPlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-component",CStudioForms.Controls.RTE.InsertComponent);