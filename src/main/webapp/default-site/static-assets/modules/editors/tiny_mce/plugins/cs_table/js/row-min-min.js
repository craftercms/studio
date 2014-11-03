tinyMCEPopup.requireLangPack();
function init(){tinyMCEPopup.resizeToInnerSize();
document.getElementById("backgroundimagebrowsercontainer").innerHTML=getBrowserHTML("backgroundimagebrowser","backgroundimage","image","table");
document.getElementById("bgcolor_pickcontainer").innerHTML=getColorPickerHTML("bgcolor_pick","bgcolor");
var y=tinyMCEPopup.editor;
var z=y.dom;
var t=z.getParent(y.selection.getStart(),"tr");
var x=document.forms[0];
var r=z.parseStyle(z.getAttrib(t,"style"));
var B=t.parentNode.nodeName.toLowerCase();
var w=z.getAttrib(t,"align");
var u=z.getAttrib(t,"valign");
var s=trimSize(getStyle(t,"height","height"));
var v=z.getAttrib(t,"class");
var p=convertRGBToHex(getStyle(t,"bgcolor","backgroundColor"));
var q=getStyle(t,"background","backgroundImage").replace(new RegExp("url\\(['\"]?([^'\"]*)['\"]?\\)","gi"),"$1");
var D=z.getAttrib(t,"id");
var C=z.getAttrib(t,"lang");
var A=z.getAttrib(t,"dir");
selectByValue(x,"rowtype",B);
if(z.select("td.mceSelected,th.mceSelected",t).length==0){addClassesToList("class","table_row_styles");
TinyMCE_EditableSelects.init();
x.bgcolor.value=p;
x.backgroundimage.value=q;
x.height.value=s;
x.id.value=D;
x.lang.value=C;
x.style.value=z.serializeStyle(r);
selectByValue(x,"align",w);
selectByValue(x,"valign",u);
selectByValue(x,"class",v,true,true);
selectByValue(x,"dir",A);
if(isVisible("backgroundimagebrowser")){document.getElementById("backgroundimage").style.width="180px"
}updateColor("bgcolor_pick","bgcolor")
}else{tinyMCEPopup.dom.hide("action")
}}function updateAction(){var m=tinyMCEPopup.editor,k=m.dom,l,i,j=document.forms[0];
var n=getSelectValue(j,"action");
if(!AutoValidator.validate(j)){tinyMCEPopup.alert(AutoValidator.getErrorMessages(j).join(". ")+".");
return false
}tinyMCEPopup.restoreSelection();
l=k.getParent(m.selection.getStart(),"tr");
i=k.getParent(m.selection.getStart(),"table");
if(k.select("td.mceSelected,th.mceSelected",l).length>0){tinymce.each(i.rows,function(a){var b;
for(b=0;
b<a.cells.length;
b++){if(k.hasClass(a.cells[b],"mceSelected")){updateRow(a,true);
return
}}});
m.addVisual();
m.nodeChanged();
m.execCommand("mceEndUndoLevel");
tinyMCEPopup.close();
return
}switch(n){case"row":updateRow(l);
break;
case"all":var o=i.getElementsByTagName("tr");
for(var p=0;
p<o.length;
p++){updateRow(o[p],true)
}break;
case"odd":case"even":var o=i.getElementsByTagName("tr");
for(var p=0;
p<o.length;
p++){if((p%2==0&&n=="odd")||(p%2!=0&&n=="even")){updateRow(o[p],true,true)
}}break
}m.addVisual();
m.nodeChanged();
m.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
}function updateRow(u,t,s){var w=tinyMCEPopup.editor;
var v=document.forms[0];
var y=w.dom;
var B=u.parentNode.nodeName.toLowerCase();
var A=getSelectValue(v,"rowtype");
var q=w.getDoc();
if(!t){y.setAttrib(u,"id",v.id.value)
}y.setAttrib(u,"align",getSelectValue(v,"align"));
y.setAttrib(u,"vAlign",getSelectValue(v,"valign"));
y.setAttrib(u,"lang",v.lang.value);
y.setAttrib(u,"dir",getSelectValue(v,"dir"));
y.setAttrib(u,"style",y.serializeStyle(y.parseStyle(v.style.value)));
y.setAttrib(u,"class",getSelectValue(v,"class"));
y.setAttrib(u,"background","");
y.setAttrib(u,"bgColor","");
y.setAttrib(u,"height","");
u.style.height=getCSSSize(v.height.value);
u.style.backgroundColor=v.bgcolor.value;
if(v.backgroundimage.value!=""){u.style.backgroundImage="url('"+v.backgroundimage.value+"')"
}else{u.style.backgroundImage=""
}if(B!=A&&!s){var i=u.cloneNode(1);
var z=y.getParent(u,"table");
var r=A;
var p=null;
for(var x=0;
x<z.childNodes.length;
x++){if(z.childNodes[x].nodeName.toLowerCase()==r){p=z.childNodes[x]
}}if(p==null){p=q.createElement(r);
if(z.firstChild.nodeName=="CAPTION"){w.dom.insertAfter(p,z.firstChild)
}else{z.insertBefore(p,z.firstChild)
}}p.appendChild(i);
u.parentNode.removeChild(u);
u=i
}y.setAttrib(u,"style",y.serializeStyle(y.parseStyle(u.style.cssText)))
}function changedBackgroundImage(){var e=document.forms[0],f=tinyMCEPopup.editor.dom;
var d=f.parseStyle(e.style.value);
d["background-image"]="url('"+e.backgroundimage.value+"')";
e.style.value=f.serializeStyle(d)
}function changedStyle(){var e=document.forms[0],f=tinyMCEPopup.editor.dom;
var d=f.parseStyle(e.style.value);
if(d["background-image"]){e.backgroundimage.value=d["background-image"].replace(new RegExp("url\\('?([^']*)'?\\)","gi"),"$1")
}else{e.backgroundimage.value=""
}if(d.height){e.height.value=trimSize(d.height)
}if(d["background-color"]){e.bgcolor.value=d["background-color"];
updateColor("bgcolor_pick","bgcolor")
}}function changedSize(){var e=document.forms[0],g=tinyMCEPopup.editor.dom;
var h=g.parseStyle(e.style.value);
var f=e.height.value;
if(f!=""){h.height=getCSSSize(f)
}else{h.height=""
}e.style.value=g.serializeStyle(h)
}function changedColor(){var e=document.forms[0],f=tinyMCEPopup.editor.dom;
var d=f.parseStyle(e.style.value);
d["background-color"]=e.bgcolor.value;
e.style.value=f.serializeStyle(d)
}tinyMCEPopup.onInit.add(init);