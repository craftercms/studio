tinyMCEPopup.requireLangPack();
var ed;
function init(){ed=tinyMCEPopup.editor;
tinyMCEPopup.resizeToInnerSize();
document.getElementById("backgroundimagebrowsercontainer").innerHTML=getBrowserHTML("backgroundimagebrowser","backgroundimage","image","table");
document.getElementById("bordercolor_pickcontainer").innerHTML=getColorPickerHTML("bordercolor_pick","bordercolor");
document.getElementById("bgcolor_pickcontainer").innerHTML=getColorPickerHTML("bgcolor_pick","bgcolor");
var B=ed;
var z=ed.dom.getParent(ed.selection.getStart(),"td,th");
var A=document.forms[0];
var t=ed.dom.parseStyle(ed.dom.getAttrib(z,"style"));
var D=z.nodeName.toLowerCase();
var y=ed.dom.getAttrib(z,"align");
var w=ed.dom.getAttrib(z,"valign");
var G=trimSize(getStyle(z,"width","width"));
var v=trimSize(getStyle(z,"height","height"));
var C=convertRGBToHex(getStyle(z,"bordercolor","borderLeftColor"));
var r=convertRGBToHex(getStyle(z,"bgcolor","backgroundColor"));
var x=ed.dom.getAttrib(z,"class");
var s=getStyle(z,"background","backgroundImage").replace(new RegExp("url\\(['\"]?([^'\"]*)['\"]?\\)","gi"),"$1");
var H=ed.dom.getAttrib(z,"id");
var F=ed.dom.getAttrib(z,"lang");
var E=ed.dom.getAttrib(z,"dir");
var u=ed.dom.getAttrib(z,"scope");
addClassesToList("class","table_cell_styles");
TinyMCE_EditableSelects.init();
if(!ed.dom.hasClass(z,"mceSelected")){A.bordercolor.value=C;
A.bgcolor.value=r;
A.backgroundimage.value=s;
A.width.value=G;
A.height.value=v;
A.id.value=H;
A.lang.value=F;
A.style.value=ed.dom.serializeStyle(t);
selectByValue(A,"align",y);
selectByValue(A,"valign",w);
selectByValue(A,"class",x,true,true);
selectByValue(A,"celltype",D);
selectByValue(A,"dir",E);
selectByValue(A,"scope",u);
if(isVisible("backgroundimagebrowser")){document.getElementById("backgroundimage").style.width="180px"
}updateColor("bordercolor_pick","bordercolor");
updateColor("bgcolor_pick","bgcolor")
}else{tinyMCEPopup.dom.hide("action")
}}function updateAction(){var B,x=ed,v,s,u,w=document.forms[0];
if(!AutoValidator.validate(w)){tinyMCEPopup.alert(AutoValidator.getErrorMessages(w).join(". ")+".");
return false
}tinyMCEPopup.restoreSelection();
B=ed.selection.getStart();
v=ed.dom.getParent(B,"td,th");
s=ed.dom.getParent(B,"tr");
u=ed.dom.getParent(B,"table");
if(ed.dom.hasClass(v,"mceSelected")){tinymce.each(ed.dom.select("td.mceSelected,th.mceSelected"),function(a){updateCell(a)
});
ed.addVisual();
ed.nodeChanged();
x.execCommand("mceEndUndoLevel");
tinyMCEPopup.close();
return
}switch(getSelectValue(w,"action")){case"cell":var y=getSelectValue(w,"celltype");
var q=getSelectValue(w,"scope");
function r(a){if(a){updateCell(v);
ed.addVisual();
ed.nodeChanged();
x.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
}}if(ed.getParam("accessibility_warnings",1)){if(y=="th"&&q==""){tinyMCEPopup.confirm(ed.getLang("table_dlg.missing_scope","",true),r)
}else{r(1)
}return
}updateCell(v);
break;
case"row":var t=s.firstChild;
if(t.nodeName!="TD"&&t.nodeName!="TH"){t=nextCell(t)
}do{t=updateCell(t,true)
}while((t=nextCell(t))!=null);
break;
case"col":var i,A=0,t=s.firstChild,p=u.getElementsByTagName("tr");
if(t.nodeName!="TD"&&t.nodeName!="TH"){t=nextCell(t)
}do{if(t==v){break
}A+=t.getAttribute("colspan")
}while((t=nextCell(t))!=null);
for(var z=0;
z<p.length;
z++){t=p[z].firstChild;
if(t.nodeName!="TD"&&t.nodeName!="TH"){t=nextCell(t)
}i=0;
do{if(i==A){t=updateCell(t,true);
break
}i+=t.getAttribute("colspan")
}while((t=nextCell(t))!=null)
}break;
case"all":var p=u.getElementsByTagName("tr");
for(var z=0;
z<p.length;
z++){var t=p[z].firstChild;
if(t.nodeName!="TD"&&t.nodeName!="TH"){t=nextCell(t)
}do{t=updateCell(t,true)
}while((t=nextCell(t))!=null)
}break
}ed.addVisual();
ed.nodeChanged();
x.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
}function nextCell(b){while((b=b.nextSibling)!=null){if(b.nodeName=="TD"||b.nodeName=="TH"){return b
}}return null
}function updateCell(u,o){var q=ed;
var p=document.forms[0];
var v=u.nodeName.toLowerCase();
var r=getSelectValue(p,"celltype");
var a=q.getDoc();
var t=ed.dom;
if(!o){t.setAttrib(u,"id",p.id.value)
}t.setAttrib(u,"align",p.align.value);
t.setAttrib(u,"vAlign",p.valign.value);
t.setAttrib(u,"lang",p.lang.value);
t.setAttrib(u,"dir",getSelectValue(p,"dir"));
t.setAttrib(u,"style",ed.dom.serializeStyle(ed.dom.parseStyle(p.style.value)));
t.setAttrib(u,"scope",p.scope.value);
t.setAttrib(u,"class",getSelectValue(p,"class"));
ed.dom.setAttrib(u,"width","");
ed.dom.setAttrib(u,"height","");
ed.dom.setAttrib(u,"bgColor","");
ed.dom.setAttrib(u,"borderColor","");
ed.dom.setAttrib(u,"background","");
u.style.width=getCSSSize(p.width.value);
u.style.height=getCSSSize(p.height.value);
if(p.bordercolor.value!=""){u.style.borderColor=p.bordercolor.value;
u.style.borderStyle=u.style.borderStyle==""?"solid":u.style.borderStyle;
u.style.borderWidth=u.style.borderWidth==""?"1px":u.style.borderWidth
}else{u.style.borderColor=""
}u.style.backgroundColor=p.bgcolor.value;
if(p.backgroundimage.value!=""){u.style.backgroundImage="url('"+p.backgroundimage.value+"')"
}else{u.style.backgroundImage=""
}if(v!=r){var s=a.createElement(r);
for(var n=0;
n<u.childNodes.length;
n++){s.appendChild(u.childNodes[n].cloneNode(1))
}for(var c=0;
c<u.attributes.length;
c++){ed.dom.setAttrib(s,u.attributes[c].name,ed.dom.getAttrib(u,u.attributes[c].name))
}u.parentNode.replaceChild(s,u);
u=s
}t.setAttrib(u,"style",t.serializeStyle(t.parseStyle(u.style.cssText)));
return u
}function changedBackgroundImage(){var d=document.forms[0];
var c=ed.dom.parseStyle(d.style.value);
c["background-image"]="url('"+d.backgroundimage.value+"')";
d.style.value=ed.dom.serializeStyle(c)
}function changedSize(){var e=document.forms[0];
var h=ed.dom.parseStyle(e.style.value);
var g=e.width.value;
if(g!=""){h.width=getCSSSize(g)
}else{h.width=""
}var f=e.height.value;
if(f!=""){h.height=getCSSSize(f)
}else{h.height=""
}e.style.value=ed.dom.serializeStyle(h)
}function changedColor(){var d=document.forms[0];
var c=ed.dom.parseStyle(d.style.value);
c["background-color"]=d.bgcolor.value;
c["border-color"]=d.bordercolor.value;
d.style.value=ed.dom.serializeStyle(c)
}function changedStyle(){var d=document.forms[0];
var c=ed.dom.parseStyle(d.style.value);
if(c["background-image"]){d.backgroundimage.value=c["background-image"].replace(new RegExp("url\\('?([^']*)'?\\)","gi"),"$1")
}else{d.backgroundimage.value=""
}if(c.width){d.width.value=trimSize(c.width)
}if(c.height){d.height.value=trimSize(c.height)
}if(c["background-color"]){d.bgcolor.value=c["background-color"];
updateColor("bgcolor_pick","bgcolor")
}if(c["border-color"]){d.bordercolor.value=c["border-color"];
updateColor("bordercolor_pick","bordercolor")
}}tinyMCEPopup.onInit.add(init);