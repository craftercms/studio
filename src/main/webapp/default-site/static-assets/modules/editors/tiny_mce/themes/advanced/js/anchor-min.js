tinyMCEPopup.requireLangPack();
var AnchorDialog={init:function(a){var c,d,b=document.forms[0];
this.editor=a;
d=a.dom.getParent(a.selection.getNode(),"A");
v=a.dom.getAttrib(d,"name")||a.dom.getAttrib(d,"id");
if(v){this.action="update";
b.anchorName.value=v
}b.insert.value=a.getLang(d?"update":"insert")
},update:function(){var a=this.editor,f,c=document.forms[0].anchorName.value,e;
if(!c||!/^[a-z][a-z0-9\-\_:\.]*$/i.test(c)){tinyMCEPopup.alert("advanced_dlg.anchor_invalid");
return
}tinyMCEPopup.restoreSelection();
if(this.action!="update"){a.selection.collapse(1)
}var d=a.schema.getElementRule("a");
if(!d||d.attributes.name){e="name"
}else{e="id"
}f=a.dom.getParent(a.selection.getNode(),"A");
if(f){f.setAttribute(e,c);
f[e]=c;
a.undoManager.add()
}else{var b={"class":"mceItemAnchor"};
b[e]=c;
a.execCommand("mceInsertContent",0,a.dom.createHTML("a",b,"\uFEFF"));
a.nodeChanged()
}tinyMCEPopup.close()
}};
tinyMCEPopup.onInit.add(AnchorDialog.init,AnchorDialog);