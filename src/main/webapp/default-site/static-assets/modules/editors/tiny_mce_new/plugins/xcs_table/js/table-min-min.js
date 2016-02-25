tinyMCEPopup.requireLangPack();
var action,orgTableWidth,orgTableHeight,dom=tinyMCEPopup.editor.dom;
function insertTable(){var O=document.forms[0];
var S=tinyMCEPopup.editor,C=S.dom;
var K=2,M=2,E=0,B=-1,V=-1,D,G,H,T,A,I,L;
var J="",F,R;
var y,x,U;
tinyMCEPopup.restoreSelection();
if(!AutoValidator.validate(O)){tinyMCEPopup.alert(AutoValidator.getErrorMessages(O).join(". ")+".");
return false
}R=C.getParent(S.selection.getNode(),"table");
K=O.elements.cols.value;
M=O.elements.rows.value;
E=O.elements.border.value!=""?O.elements.border.value:0;
B=O.elements.cellpadding.value!=""?O.elements.cellpadding.value:"";
V=O.elements.cellspacing.value!=""?O.elements.cellspacing.value:"";
D=getSelectValue(O,"align");
I=getSelectValue(O,"tframe");
L=getSelectValue(O,"rules");
G=O.elements.width.value;
H=O.elements.height.value;
bordercolor=O.elements.bordercolor.value;
bgcolor=O.elements.bgcolor.value;
T=getSelectValue(O,"class");
id=O.elements.id.value;
summary=O.elements.summary.value;
style=O.elements.style.value;
dir=O.elements.dir.value;
lang=O.elements.lang.value;
background=O.elements.backgroundimage.value;
A=O.elements.caption.checked;
y=tinyMCEPopup.getParam("table_cell_limit",false);
x=tinyMCEPopup.getParam("table_row_limit",false);
U=tinyMCEPopup.getParam("table_col_limit",false);
if(U&&K>U){tinyMCEPopup.alert(S.getLang("table_dlg.col_limit").replace(/\{\$cols\}/g,U));
return false
}else{if(x&&M>x){tinyMCEPopup.alert(S.getLang("table_dlg.row_limit").replace(/\{\$rows\}/g,x));
return false
}else{if(y&&K*M>y){tinyMCEPopup.alert(S.getLang("table_dlg.cell_limit").replace(/\{\$cells\}/g,y));
return false
}}}if(action=="update"){C.setAttrib(R,"cellPadding",B,true);
C.setAttrib(R,"cellSpacing",V,true);
C.setAttrib(R,"border",E);
C.setAttrib(R,"align",D);
C.setAttrib(R,"frame",I);
C.setAttrib(R,"rules",L);
C.setAttrib(R,"class",T);
C.setAttrib(R,"style",style);
C.setAttrib(R,"id",id);
C.setAttrib(R,"summary",summary);
C.setAttrib(R,"dir",dir);
C.setAttrib(R,"lang",lang);
F=S.dom.select("caption",R)[0];
if(F&&!A){F.parentNode.removeChild(F)
}if(!F&&A){F=R.ownerDocument.createElement("caption");
if(!tinymce.isIE){F.innerHTML='<br data-mce-bogus="1"/>'
}R.insertBefore(F,R.firstChild)
}if(G&&S.settings.inline_styles){C.setStyle(R,"width",G);
C.setAttrib(R,"width","")
}else{C.setAttrib(R,"width",G,true);
C.setStyle(R,"width","")
}C.setAttrib(R,"borderColor","");
C.setAttrib(R,"bgColor","");
C.setAttrib(R,"background","");
if(H&&S.settings.inline_styles){C.setStyle(R,"height",H);
C.setAttrib(R,"height","")
}else{C.setAttrib(R,"height",H,true);
C.setStyle(R,"height","")
}if(background!=""){R.style.backgroundImage="url('"+background+"')"
}else{R.style.backgroundImage=""
}if(bordercolor!=""){R.style.borderColor=bordercolor;
R.style.borderStyle=R.style.borderStyle==""?"solid":R.style.borderStyle;
R.style.borderWidth=E==""?"1px":E
}else{R.style.borderColor=""
}R.style.backgroundColor=bgcolor;
R.style.height=getCSSSize(H);
S.addVisual();
S.nodeChanged();
S.execCommand("mceEndUndoLevel");
if(O.width.value!=orgTableWidth||O.height.value!=orgTableHeight){S.execCommand("mceRepaint")
}tinyMCEPopup.close();
return true
}J+="<table";
J+=makeAttrib("id",id);
J+=makeAttrib("border",E);
J+=makeAttrib("cellpadding",B);
J+=makeAttrib("cellspacing",V);
J+=makeAttrib("data-mce-new","1");
if(G&&S.settings.inline_styles){if(style){style+="; "
}if(/^[0-9\.]+$/.test(G)){G+="px"
}style+="width: "+G
}else{J+=makeAttrib("width",G)
}J+=makeAttrib("align",D);
J+=makeAttrib("frame",I);
J+=makeAttrib("rules",L);
J+=makeAttrib("class",T);
J+=makeAttrib("style",style);
J+=makeAttrib("summary",summary);
J+=makeAttrib("dir",dir);
J+=makeAttrib("lang",lang);
J+=">";
if(A){if(!tinymce.isIE){J+='<caption><br data-mce-bogus="1"/></caption>'
}else{J+="<caption></caption>"
}}for(var P=0;
P<M;
P++){J+="<tr>";
for(var N=0;
N<K;
N++){if(!tinymce.isIE){J+='<td><br data-mce-bogus="1"/></td>'
}else{J+="<td></td>"
}}J+="</tr>"
}J+="</table>";
if(S.settings.fix_table_elements){var Q="";
S.focus();
S.selection.setContent('<br class="_mce_marker" />');
tinymce.each("h1,h2,h3,h4,h5,h6,p".split(","),function(a){if(Q){Q+=","
}Q+=a+" ._mce_marker"
});
tinymce.each(S.dom.select(Q),function(a){S.dom.split(S.dom.getParent(a,"h1,h2,h3,h4,h5,h6,p"),a)
});
C.setOuterHTML(C.select("br._mce_marker")[0],J)
}else{S.execCommand("mceInsertContent",false,J)
}tinymce.each(C.select("table[data-mce-new]"),function(c){var b=C.select("td",c);
try{S.selection.select(b[0],true);
S.selection.collapse()
}catch(a){}C.setAttrib(c,"data-mce-new","")
});
S.addVisual();
S.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
}function makeAttrib(g,h){var e=document.forms[0];
var f=e.elements[g];
if(typeof(h)=="undefined"||h==null){h="";
if(f){h=f.value
}}if(h==""){return""
}h=h.replace(/&/g,"&amp;");
h=h.replace(/\"/g,"&quot;");
h=h.replace(/</g,"&lt;");
h=h.replace(/>/g,"&gt;");
return" "+g+'="'+h+'"'
}function init(){tinyMCEPopup.resizeToInnerSize();
document.getElementById("backgroundimagebrowsercontainer").innerHTML=getBrowserHTML("backgroundimagebrowser","backgroundimage","image","table");
document.getElementById("backgroundimagebrowsercontainer").innerHTML=getBrowserHTML("backgroundimagebrowser","backgroundimage","image","table");
document.getElementById("bordercolor_pickcontainer").innerHTML=getColorPickerHTML("bordercolor_pick","bordercolor");
document.getElementById("bgcolor_pickcontainer").innerHTML=getColorPickerHTML("bgcolor_pick","bgcolor");
var O=2,P=2,H=tinyMCEPopup.getParam("table_default_border","0"),A=tinyMCEPopup.getParam("table_default_cellpadding",""),W=tinyMCEPopup.getParam("table_default_cellspacing","");
var G="",I="",L="",D="",X="",V="";
var K="",S="",C="",J="",i="",B="",X="",D="",Q="",M="";
var U=tinyMCEPopup.editor,E=U.dom;
var R=document.forms[0];
var T=E.getParent(U.selection.getNode(),"table");
action=tinyMCEPopup.getWindowArg("action");
if(!action){action=T?"update":"insert"
}if(T&&action!="insert"){var N=T.rows;
var O=0;
for(var F=0;
F<N.length;
F++){if(N[F].cells.length>O){O=N[F].cells.length
}}O=O;
P=N.length;
st=E.parseStyle(E.getAttrib(T,"style"));
H=trimSize(getStyle(T,"border","borderWidth"));
A=E.getAttrib(T,"cellpadding","");
W=E.getAttrib(T,"cellspacing","");
I=trimSize(getStyle(T,"width","width"));
L=trimSize(getStyle(T,"height","height"));
D=convertRGBToHex(getStyle(T,"bordercolor","borderLeftColor"));
X=convertRGBToHex(getStyle(T,"bgcolor","backgroundColor"));
G=E.getAttrib(T,"align",G);
M=E.getAttrib(T,"frame");
Q=E.getAttrib(T,"rules");
V=tinymce.trim(E.getAttrib(T,"class").replace(/mceItem.+/g,""));
K=E.getAttrib(T,"id");
S=E.getAttrib(T,"summary");
C=E.serializeStyle(st);
J=E.getAttrib(T,"dir");
i=E.getAttrib(T,"lang");
B=getStyle(T,"background","backgroundImage").replace(new RegExp("url\\(['\"]?([^'\"]*)['\"]?\\)","gi"),"$1");
R.caption.checked=T.getElementsByTagName("caption").length>0;
orgTableWidth=I;
orgTableHeight=L;
action="update";
R.insert.value=U.getLang("update")
}addClassesToList("class","table_styles");
TinyMCE_EditableSelects.init();
selectByValue(R,"align",G);
selectByValue(R,"tframe",M);
selectByValue(R,"rules",Q);
selectByValue(R,"class",V,true,true);
R.cols.value=O;
R.rows.value=P;
R.border.value=H;
R.cellpadding.value=A;
R.cellspacing.value=W;
R.width.value=I;
R.height.value=L;
R.bordercolor.value=D;
R.bgcolor.value=X;
R.id.value=K;
R.summary.value=S;
R.style.value=C;
R.dir.value=J;
R.lang.value=i;
R.backgroundimage.value=B;
updateColor("bordercolor_pick","bordercolor");
updateColor("bgcolor_pick","bgcolor");
if(isVisible("backgroundimagebrowser")){document.getElementById("backgroundimage").style.width="180px"
}if(action=="update"){R.cols.disabled=true;
R.rows.disabled=true
}}function changedSize(){var d=document.forms[0];
var f=dom.parseStyle(d.style.value);
var e=d.height.value;
if(e!=""){f.height=getCSSSize(e)
}else{f.height=""
}d.style.value=dom.serializeStyle(f)
}function changedBackgroundImage(){var d=document.forms[0];
var c=dom.parseStyle(d.style.value);
c["background-image"]="url('"+d.backgroundimage.value+"')";
d.style.value=dom.serializeStyle(c)
}function changedBorder(){var d=document.forms[0];
var c=dom.parseStyle(d.style.value);
if(d.border.value!=""&&d.bordercolor.value!=""){c["border-width"]=d.border.value+"px"
}d.style.value=dom.serializeStyle(c)
}function changedColor(){var d=document.forms[0];
var c=dom.parseStyle(d.style.value);
c["background-color"]=d.bgcolor.value;
if(d.bordercolor.value!=""){c["border-color"]=d.bordercolor.value;
if(!c["border-width"]){c["border-width"]=d.border.value==""?"1px":d.border.value+"px"
}}d.style.value=dom.serializeStyle(c)
}function changedStyle(){var d=document.forms[0];
var c=dom.parseStyle(d.style.value);
if(c["background-image"]){d.backgroundimage.value=c["background-image"].replace(new RegExp("url\\(['\"]?([^'\"]*)['\"]?\\)","gi"),"$1")
}else{d.backgroundimage.value=""
}if(c.width){d.width.value=trimSize(c.width)
}if(c.height){d.height.value=trimSize(c.height)
}if(c["background-color"]){d.bgcolor.value=c["background-color"];
updateColor("bgcolor_pick","bgcolor")
}if(c["border-color"]){d.bordercolor.value=c["border-color"];
updateColor("bordercolor_pick","bordercolor")
}}tinyMCEPopup.onInit.add(init);