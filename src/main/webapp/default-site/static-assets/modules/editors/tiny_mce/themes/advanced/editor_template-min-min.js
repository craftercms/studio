(function(k){var j=k.DOM,l=k.dom.Event,p=k.extend,m=k.each,r=k.util.Cookie,n,o=k.explode;
function q(c,f){var h,g,d=c.dom,i="",e,a;
previewStyles=c.settings.preview_styles;
if(previewStyles===false){return""
}if(!previewStyles){previewStyles="font-family font-size font-weight text-decoration text-transform color background-color"
}function b(t){return t.replace(/%(\w+)/g,"")
}h=f.block||f.inline||"span";
g=d.create(h);
m(f.styles,function(u,v){u=b(u);
if(u){d.setStyle(g,v,u)
}});
m(f.attributes,function(u,v){u=b(u);
if(u){d.setAttrib(g,v,u)
}});
m(f.classes,function(t){t=b(t);
if(!d.hasClass(g,t)){d.addClass(g,t)
}});
d.setStyles(g,{position:"absolute",left:-65535});
c.getBody().appendChild(g);
e=d.getStyle(c.getBody(),"fontSize",true);
e=/px$/.test(e)?parseInt(e,10):0;
m(previewStyles.split(" "),function(v){var u=d.getStyle(g,v,true);
if(v=="background-color"&&/transparent|rgba\s*\([^)]+,\s*0\)/.test(u)){u=d.getStyle(c.getBody(),v,true);
if(d.toHex(u).toLowerCase()=="#ffffff"){return
}}if(v=="font-size"){if(/em|%$/.test(u)){if(e===0){return
}u=parseFloat(u,10)/(/%$/.test(u)?100:1);
u=(u*e)+"px"
}}i+=v+":"+u+";"
});
d.remove(g);
return i
}k.ThemeManager.requireLangPack("advanced");
k.create("tinymce.themes.AdvancedTheme",{sizes:[8,10,12,14,18,24,36],controls:{bold:["bold_desc","Bold"],italic:["italic_desc","Italic"],underline:["underline_desc","Underline"],strikethrough:["striketrough_desc","Strikethrough"],justifyleft:["justifyleft_desc","JustifyLeft"],justifycenter:["justifycenter_desc","JustifyCenter"],justifyright:["justifyright_desc","JustifyRight"],justifyfull:["justifyfull_desc","JustifyFull"],bullist:["bullist_desc","InsertUnorderedList"],numlist:["numlist_desc","InsertOrderedList"],outdent:["outdent_desc","Outdent"],indent:["indent_desc","Indent"],cut:["cut_desc","Cut"],copy:["copy_desc","Copy"],paste:["paste_desc","Paste"],undo:["undo_desc","Undo"],redo:["redo_desc","Redo"],link:["link_desc","mceLink"],unlink:["unlink_desc","unlink"],image:["image_desc","mceImage"],cleanup:["cleanup_desc","mceCleanup"],help:["help_desc","mceHelp"],code:["code_desc","mceCodeEditor"],hr:["hr_desc","InsertHorizontalRule"],removeformat:["removeformat_desc","RemoveFormat"],sub:["sub_desc","subscript"],sup:["sup_desc","superscript"],forecolor:["forecolor_desc","ForeColor"],forecolorpicker:["forecolor_desc","mceForeColor"],backcolor:["backcolor_desc","HiliteColor"],backcolorpicker:["backcolor_desc","mceBackColor"],charmap:["charmap_desc","mceCharMap"],visualaid:["visualaid_desc","mceToggleVisualAid"],anchor:["anchor_desc","mceInsertAnchor"],newdocument:["newdocument_desc","mceNewDocument"],blockquote:["blockquote_desc","mceBlockQuote"]},stateControls:["bold","italic","underline","strikethrough","bullist","numlist","justifyleft","justifycenter","justifyright","justifyfull","sub","sup","blockquote"],init:function(c,b){var a=this,f,d,e;
a.editor=c;
a.url=b;
a.onResolveName=new k.util.Dispatcher(this);
f=c.settings;
c.forcedHighContrastMode=c.settings.detect_highcontrast&&a._isHighContrast();
c.settings.skin=c.forcedHighContrastMode?"highcontrast":c.settings.skin;
if(!f.theme_advanced_buttons1){f=p({theme_advanced_buttons1:"bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect",theme_advanced_buttons2:"bullist,numlist,|,outdent,indent,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code",theme_advanced_buttons3:"hr,removeformat,visualaid,|,sub,sup,|,charmap"},f)
}a.settings=f=p({theme_advanced_path:true,theme_advanced_toolbar_location:"top",theme_advanced_blockformats:"p,address,pre,h1,h2,h3,h4,h5,h6",theme_advanced_toolbar_align:"left",theme_advanced_statusbar_location:"bottom",theme_advanced_fonts:"Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings,zapf dingbats",theme_advanced_more_colors:1,theme_advanced_row_height:23,theme_advanced_resize_horizontal:1,theme_advanced_resizing_use_cookie:1,theme_advanced_font_sizes:"1,2,3,4,5,6,7",theme_advanced_font_selector:"span",theme_advanced_show_current_color:0,readonly:c.settings.readonly},f);
if(!f.font_size_style_values){f.font_size_style_values="8pt,10pt,12pt,14pt,18pt,24pt,36pt"
}if(k.is(f.theme_advanced_font_sizes,"string")){f.font_size_style_values=k.explode(f.font_size_style_values);
f.font_size_classes=k.explode(f.font_size_classes||"");
e={};
c.settings.theme_advanced_font_sizes=f.theme_advanced_font_sizes;
m(c.getParam("theme_advanced_font_sizes","","hash"),function(g,h){var i;
if(h==g&&g>=1&&g<=7){h=g+" ("+a.sizes[g-1]+"pt)";
i=f.font_size_classes[g-1];
g=f.font_size_style_values[g-1]||(a.sizes[g-1]+"pt")
}if(/^\s*\./.test(g)){i=g.replace(/\./g,"")
}e[h]=i?{"class":i}:{fontSize:g}
});
f.theme_advanced_font_sizes=e
}if((d=f.theme_advanced_path_location)&&d!="none"){f.theme_advanced_statusbar_location=f.theme_advanced_path_location
}if(f.theme_advanced_statusbar_location=="none"){f.theme_advanced_statusbar_location=0
}if(c.settings.content_css!==false){c.contentCSS.push(c.baseURI.toAbsolute(b+"/skins/"+c.settings.skin+"/content.css"))
}c.onInit.add(function(){if(!c.settings.readonly){c.onNodeChange.add(a._nodeChanged,a);
c.onKeyUp.add(a._updateUndoStatus,a);
c.onMouseUp.add(a._updateUndoStatus,a);
c.dom.bind(c.dom.getRoot(),"dragend",function(){a._updateUndoStatus(c)
})
}});
c.onSetProgressState.add(function(v,x,i){var h,g=v.id,w;
if(x){a.progressTimer=setTimeout(function(){h=v.getContainer();
h=h.insertBefore(j.create("DIV",{style:"position:relative"}),h.firstChild);
w=j.get(v.id+"_tbl");
j.add(h,"div",{id:g+"_blocker","class":"mceBlocker",style:{width:w.clientWidth+2,height:w.clientHeight+2}});
j.add(h,"div",{id:g+"_progress","class":"mceProgress",style:{left:w.clientWidth/2,top:w.clientHeight/2}})
},i||0)
}else{j.remove(g+"_blocker");
j.remove(g+"_progress");
clearTimeout(a.progressTimer)
}});
j.loadCSS(f.editor_css?c.documentBaseURI.toAbsolute(f.editor_css):b+"/skins/"+c.settings.skin+"/ui.css");
if(f.skin_variant){j.loadCSS(b+"/skins/"+c.settings.skin+"/ui_"+f.skin_variant+".css")
}},_isHighContrast:function(){var b,a=j.add(j.getRoot(),"div",{style:"background-color: rgb(171,239,86);"});
b=(j.getStyle(a,"background-color",true)+"").toLowerCase().replace(/ /g,"");
j.remove(a);
return b!="rgb(171,239,86)"&&b!="#abef56"
},createControl:function(a,d){var c,b;
if(b=d.createControl(a)){return b
}switch(a){case"styleselect":return this._createStyleSelect();
case"formatselect":return this._createBlockFormats();
case"fontselect":return this._createFontSelect();
case"fontsizeselect":return this._createFontSizeSelect();
case"forecolor":return this._createForeColorMenu();
case"backcolor":return this._createBackColorMenu()
}if((c=this.controls[a])){return d.createButton(a,{title:"advanced."+c[0],cmd:c[1],ui:c[2],value:c[3]})
}},execCommand:function(b,c,a){var d=this["_"+b];
if(d){d.call(this,c,a);
return true
}return false
},_importClasses:function(a){var c=this.editor,b=c.controlManager.get("styleselect");
if(b.getLength()==0){m(c.dom.getClasses(),function(e,d){var f="style_"+d,g;
g={inline:"span",attributes:{"class":e["class"]},selector:"*"};
c.formatter.register(f,g);
b.add(e["class"],f,{style:function(){return q(c,g)
}})
})
}},_createStyleSelect:function(e){var b=this,d=b.editor,c=d.controlManager,a;
a=c.createListBox("styleselect",{title:"advanced.style_select",onselect:function(g){var f,i=[],h;
m(a.items,function(t){i.push(t.value)
});
d.focus();
d.undoManager.add();
f=d.formatter.matchAll(i);
k.each(f,function(t){if(!g||t==g){if(t){d.formatter.remove(t)
}h=true
}});
if(!h){d.formatter.apply(g)
}d.undoManager.add();
d.nodeChanged();
return false
}});
d.onPreInit.add(function(){var f=0,g=d.getParam("style_formats");
if(g){m(g,function(t){var i,h=0;
m(t,function(){h++
});
if(h>1){i=t.name=t.name||"style_"+(f++);
d.formatter.register(i,t);
a.add(t.title,i,{style:function(){return q(d,t)
}})
}else{a.add(t.title)
}})
}else{m(d.getParam("theme_advanced_styles","","hash"),function(h,i){var u,v;
if(h){u="style_"+(f++);
v={inline:"span",classes:h,selector:"*"};
d.formatter.register(u,v);
a.add(b.editor.translate(i),u,{style:function(){return q(d,v)
}})
}})
}});
if(a.getLength()==0){a.onPostRender.add(function(g,f){if(!a.NativeListBox){l.add(f.id+"_text","focus",b._importClasses,b);
l.add(f.id+"_text","mousedown",b._importClasses,b);
l.add(f.id+"_open","focus",b._importClasses,b);
l.add(f.id+"_open","mousedown",b._importClasses,b)
}else{l.add(f.id,"focus",b._importClasses,b)
}})
}return a
},_createFontSelect:function(){var a,b=this,c=b.editor;
a=c.controlManager.createListBox("fontselect",{title:"advanced.fontdefault",onselect:function(d){var e=a.items[a.selectedIndex];
if(!d&&e){c.execCommand("FontName",false,e.value);
return
}c.execCommand("FontName",false,d);
a.select(function(f){return d==f
});
if(e&&e.value==d){a.select(null)
}return false
}});
if(a){m(c.getParam("theme_advanced_fonts",b.settings.theme_advanced_fonts,"hash"),function(e,d){a.add(c.translate(d),e,{style:e.indexOf("dings")==-1?"font-family:"+e:""})
})
}return a
},_createFontSizeSelect:function(){var a=this,c=a.editor,e,b=0,d=[];
e=c.controlManager.createListBox("fontsizeselect",{title:"advanced.font_size",onselect:function(g){var f=e.items[e.selectedIndex];
if(!g&&f){f=f.value;
if(f["class"]){c.formatter.toggle("fontsize_class",{value:f["class"]});
c.undoManager.add();
c.nodeChanged()
}else{c.execCommand("FontSize",false,f.fontSize)
}return
}if(g["class"]){c.focus();
c.undoManager.add();
c.formatter.toggle("fontsize_class",{value:g["class"]});
c.undoManager.add();
c.nodeChanged()
}else{c.execCommand("FontSize",false,g.fontSize)
}e.select(function(h){return g==h
});
if(f&&(f.value.fontSize==g.fontSize||f.value["class"]&&f.value["class"]==g["class"])){e.select(null)
}return false
}});
if(e){m(a.settings.theme_advanced_font_sizes,function(g,h){var f=g.fontSize;
if(f>=1&&f<=7){f=a.sizes[parseInt(f)-1]+"pt"
}e.add(h,g,{style:"font-size:"+f,"class":"mceFontSize"+(b++)+(" "+(g["class"]||""))})
})
}return e
},_createBlockFormats:function(){var a,c={p:"advanced.paragraph",address:"advanced.address",pre:"advanced.pre",h1:"advanced.h1",h2:"advanced.h2",h3:"advanced.h3",h4:"advanced.h4",h5:"advanced.h5",h6:"advanced.h6",div:"advanced.div",blockquote:"advanced.blockquote",code:"advanced.code",dt:"advanced.dt",dd:"advanced.dd",samp:"advanced.samp"},b=this;
a=b.editor.controlManager.createListBox("formatselect",{title:"advanced.block",onselect:function(d){b.editor.execCommand("FormatBlock",false,d);
return false
}});
if(a){m(b.editor.getParam("theme_advanced_blockformats",b.settings.theme_advanced_blockformats,"hash"),function(e,d){a.add(b.editor.translate(d!=e?d:c[e]),e,{"class":"mce_formatPreview mce_"+e,style:function(){return q(b.editor,{block:e})
}})
})
}return a
},_createForeColorMenu:function(){var e,c=this,b=c.settings,a={},d;
if(b.theme_advanced_more_colors){a.more_colors_func=function(){c._mceColorPicker(0,{color:e.value,func:function(f){e.setColor(f)
}})
}
}if(d=b.theme_advanced_text_colors){a.colors=d
}if(b.theme_advanced_default_foreground_color){a.default_color=b.theme_advanced_default_foreground_color
}a.title="advanced.forecolor_desc";
a.cmd="ForeColor";
a.scope=this;
e=c.editor.controlManager.createColorSplitButton("forecolor",a);
return e
},_createBackColorMenu:function(){var e,c=this,b=c.settings,a={},d;
if(b.theme_advanced_more_colors){a.more_colors_func=function(){c._mceColorPicker(0,{color:e.value,func:function(f){e.setColor(f)
}})
}
}if(d=b.theme_advanced_background_colors){a.colors=d
}if(b.theme_advanced_default_background_color){a.default_color=b.theme_advanced_default_background_color
}a.title="advanced.backcolor_desc";
a.cmd="HiliteColor";
a.scope=this;
e=c.editor.controlManager.createColorSplitButton("backcolor",a);
return e
},renderUI:function(d){var b,c,a,h=this,s=h.editor,g=h.settings,i,e,f;
if(s.settings){s.settings.aria_label=g.aria_label+s.getLang("advanced.help_shortcut")
}b=e=j.create("span",{role:"application","aria-labelledby":s.id+"_voice",id:s.id+"_parent","class":"mceEditor "+s.settings.skin+"Skin"+(g.skin_variant?" "+s.settings.skin+"Skin"+h._ufirst(g.skin_variant):"")+(s.settings.directionality=="rtl"?" mceRtl":"")});
j.add(b,"span",{"class":"mceVoiceLabel",style:"display:none;",id:s.id+"_voice"},g.aria_label);
if(!j.boxModel){b=j.add(b,"div",{"class":"mceOldBoxModel"})
}b=i=j.add(b,"table",{role:"presentation",id:s.id+"_tbl","class":"mceLayout",cellSpacing:0,cellPadding:0});
b=a=j.add(b,"tbody");
switch((g.theme_advanced_layout_manager||"").toLowerCase()){case"rowlayout":c=h._rowLayout(g,a,d);
break;
case"customlayout":c=s.execCallback("theme_advanced_custom_layout",g,a,d,e);
break;
default:c=h._simpleLayout(g,a,d,e)
}b=d.targetNode;
f=i.rows;
j.addClass(f[0],"mceFirst");
j.addClass(f[f.length-1],"mceLast");
m(j.select("tr",a),function(t){j.addClass(t.firstChild,"mceFirst");
j.addClass(t.childNodes[t.childNodes.length-1],"mceLast")
});
if(j.get(g.theme_advanced_toolbar_container)){j.get(g.theme_advanced_toolbar_container).appendChild(e)
}else{j.insertAfter(e,b)
}l.add(s.id+"_path_row","click",function(t){t=t.target;
if(t.nodeName=="A"){h._sel(t.className.replace(/^.*mcePath_([0-9]+).*$/,"$1"));
return false
}});
if(!s.getParam("accessibility_focus")){l.add(j.add(e,"a",{href:"#"},"<!-- IE -->"),"focus",function(){tinyMCE.get(s.id).focus()
})
}if(g.theme_advanced_toolbar_location=="external"){d.deltaHeight=0
}h.deltaHeight=d.deltaHeight;
d.targetNode=null;
s.onKeyDown.add(function(u,w){var t=121,v=122;
if(w.altKey){if(w.keyCode===t){if(k.isWebKit){window.focus()
}h.toolbarGroup.focus();
return l.cancel(w)
}else{if(w.keyCode===v){j.get(u.id+"_path_row").focus();
return l.cancel(w)
}}}});
s.addShortcut("alt+0","","mceShortcuts",h);
return{iframeContainer:c,editorContainer:s.id+"_parent",sizeContainer:i,deltaHeight:d.deltaHeight}
},getInfo:function(){return{longname:"Advanced theme",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",version:k.majorVersion+"."+k.minorVersion}
},resizeBy:function(c,b){var a=j.get(this.editor.id+"_ifr");
this.resizeTo(a.clientWidth+c,a.clientHeight+b)
},resizeTo:function(d,g,b){var c=this.editor,a=this.settings,f=j.get(c.id+"_tbl"),e=j.get(c.id+"_ifr");
d=Math.max(a.theme_advanced_resizing_min_width||100,d);
g=Math.max(a.theme_advanced_resizing_min_height||100,g);
d=Math.min(a.theme_advanced_resizing_max_width||65535,d);
g=Math.min(a.theme_advanced_resizing_max_height||65535,g);
j.setStyle(f,"height","");
j.setStyle(e,"height",g);
if(a.theme_advanced_resize_horizontal){j.setStyle(f,"width","");
j.setStyle(e,"width",d);
if(d<f.clientWidth){d=f.clientWidth;
j.setStyle(e,"width",f.clientWidth)
}}if(b&&a.theme_advanced_resizing_use_cookie){r.setHash("TinyMCE_"+c.id+"_size",{cw:d,ch:g})
}},destroy:function(){var a=this.editor.id;
l.clear(a+"_resize");
l.clear(a+"_path_row");
l.clear(a+"_external_close")
},_simpleLayout:function(g,A,d,f){var h=this,t=h.editor,s=g.theme_advanced_toolbar_location,b=g.theme_advanced_statusbar_location,c,e,a,i;
if(g.readonly){c=j.add(A,"tr");
c=e=j.add(c,"td",{"class":"mceIframeContainer"});
return e
}if(s=="top"){h._addToolbars(A,d)
}if(s=="external"){c=i=j.create("div",{style:"position:relative"});
c=j.add(c,"div",{id:t.id+"_external","class":"mceExternalToolbar"});
j.add(c,"a",{id:t.id+"_external_close",href:"javascript:;","class":"mceExternalClose"});
c=j.add(c,"table",{id:t.id+"_tblext",cellSpacing:0,cellPadding:0});
a=j.add(c,"tbody");
if(f.firstChild.className=="mceOldBoxModel"){f.firstChild.appendChild(i)
}else{f.insertBefore(i,f.firstChild)
}h._addToolbars(a,d);
t.onMouseUp.add(function(){var u=j.get(t.id+"_external");
j.show(u);
j.hide(n);
var v=l.add(t.id+"_external_close","click",function(){j.hide(t.id+"_external");
l.remove(t.id+"_external_close","click",v);
return false
});
j.show(u);
j.setStyle(u,"top",0-j.getRect(t.id+"_tblext").h-1);
j.hide(u);
j.show(u);
u.style.filter="";
n=t.id+"_external";
u=null
})
}if(b=="top"){h._addStatusBar(A,d)
}if(!g.theme_advanced_toolbar_container){c=j.add(A,"tr");
c=e=j.add(c,"td",{"class":"mceIframeContainer"})
}if(s=="bottom"){h._addToolbars(A,d)
}if(b=="bottom"){h._addStatusBar(A,d)
}return e
},_rowLayout:function(i,c,e){var s=this,b=s.editor,t,h,g=b.controlManager,d,f,z,a;
t=i.theme_advanced_containers_default_class||"";
h=i.theme_advanced_containers_default_align||"center";
m(o(i.theme_advanced_containers||""),function(u,v){var w=i["theme_advanced_container_"+u]||"";
switch(u.toLowerCase()){case"mceeditor":d=j.add(c,"tr");
d=f=j.add(d,"td",{"class":"mceIframeContainer"});
break;
case"mceelementpath":s._addStatusBar(c,e);
break;
default:a=(i["theme_advanced_container_"+u+"_align"]||h).toLowerCase();
a="mce"+s._ufirst(a);
d=j.add(j.add(c,"tr"),"td",{"class":"mceToolbar "+(i["theme_advanced_container_"+u+"_class"]||t)+" "+a||h});
z=g.createToolbar("toolbar"+v);
s._addControls(w,z);
j.setHTML(d,z.renderHTML());
e.deltaHeight-=i.theme_advanced_row_height
}});
return f
},_addControls:function(c,d){var b=this,a=b.settings,f,e=b.editor.controlManager;
if(a.theme_advanced_disable&&!b._disabled){f={};
m(o(a.theme_advanced_disable),function(g){f[g]=1
});
b._disabled=f
}else{f=b._disabled
}m(o(c),function(g){var h;
if(f&&f[g]){return
}if(g=="tablecontrols"){m(["table","|","row_props","cell_props","|","row_before","row_after","delete_row","|","col_before","col_after","delete_col","|","split_cells","merge_cells"],function(i){i=b.createControl(i,e);
if(i){d.add(i)
}});
return
}h=b.createControl(g,e);
if(h){d.add(h)
}})
},_addToolbars:function(v,i){var d=this,c,e,F=d.editor,b=d.settings,f,s=F.controlManager,E,h,a=[],t,D,g=false;
D=s.createToolbarGroup("toolbargroup",{name:F.getLang("advanced.toolbar"),tab_focus_toolbar:F.getParam("theme_advanced_tab_focus_toolbar")});
d.toolbarGroup=D;
t=b.theme_advanced_toolbar_align.toLowerCase();
t="mce"+d._ufirst(t);
h=j.add(j.add(v,"tr",{role:"presentation"}),"td",{"class":"mceToolbar "+t,role:"toolbar"});
for(c=1;
(f=b["theme_advanced_buttons"+c]);
c++){g=true;
e=s.createToolbar("toolbar"+c,{"class":"mceToolbarRow"+c});
if(b["theme_advanced_buttons"+c+"_add"]){f+=","+b["theme_advanced_buttons"+c+"_add"]
}if(b["theme_advanced_buttons"+c+"_add_before"]){f=b["theme_advanced_buttons"+c+"_add_before"]+","+f
}d._addControls(f,e);
D.add(e);
i.deltaHeight-=b.theme_advanced_row_height
}if(!g){i.deltaHeight-=b.theme_advanced_row_height
}a.push(D.renderHTML());
a.push(j.createHTML("a",{href:"#",accesskey:"z",title:F.getLang("advanced.toolbar_focus"),onfocus:"tinyMCE.getInstanceById('"+F.id+"').focus();"},"<!-- IE -->"));
j.setHTML(h,a.join(""))
},_addStatusBar:function(b,e){var d,h=this,a=h.editor,g=h.settings,f,s,i,c;
d=j.add(b,"tr");
d=c=j.add(d,"td",{"class":"mceStatusbar"});
d=j.add(d,"div",{id:a.id+"_path_row",role:"group","aria-labelledby":a.id+"_path_voice"});
if(g.theme_advanced_path){j.add(d,"span",{id:a.id+"_path_voice"},a.translate("advanced.path"));
j.add(d,"span",{},": ")
}else{j.add(d,"span",{},"&#160;")
}if(g.theme_advanced_resizing){j.add(c,"a",{id:a.id+"_resize",href:"javascript:;",onclick:"return false;","class":"mceResize",tabIndex:"-1"});
if(g.theme_advanced_resizing_use_cookie){a.onPostRender.add(function(){var u=r.getHash("TinyMCE_"+a.id+"_size"),t=j.get(a.id+"_tbl");
if(!u){return
}h.resizeTo(u.cw,u.ch)
})
}a.onPostRender.add(function(){l.add(a.id+"_resize","click",function(t){t.preventDefault()
});
l.add(a.id+"_resize","mousedown",function(P){var O,v,Q,H,u,I,x,M,J,N,L;
function K(t){t.preventDefault();
J=x+(t.screenX-u);
N=M+(t.screenY-I);
h.resizeTo(J,N)
}function w(t){l.remove(j.doc,"mousemove",O);
l.remove(a.getDoc(),"mousemove",v);
l.remove(j.doc,"mouseup",Q);
l.remove(a.getDoc(),"mouseup",H);
J=x+(t.screenX-u);
N=M+(t.screenY-I);
h.resizeTo(J,N,true);
a.nodeChanged()
}P.preventDefault();
u=P.screenX;
I=P.screenY;
L=j.get(h.editor.id+"_ifr");
x=J=L.clientWidth;
M=N=L.clientHeight;
O=l.add(j.doc,"mousemove",K);
v=l.add(a.getDoc(),"mousemove",K);
Q=l.add(j.doc,"mouseup",w);
H=l.add(a.getDoc(),"mouseup",w)
})
})
}e.deltaHeight-=21;
d=b=null
},_updateUndoStatus:function(b){var c=b.controlManager,a=b.undoManager;
c.setDisabled("undo",!a.hasUndo()&&!a.typing);
c.setDisabled("redo",!a.hasRedo())
},_nodeChanged:function(L,h,i,v,g){var a=this,s,f=0,b,d,K=a.settings,c,N,e,t,M,O,P;
k.each(a.stateControls,function(u){h.setActive(u,L.queryCommandState(a.controls[u][1]))
});
function J(x){var w,y=g.parents,u=x;
if(typeof(x)=="string"){u=function(z){return z.nodeName==x
}
}for(w=0;
w<y.length;
w++){if(u(y[w])){return y[w]
}}}h.setActive("visualaid",L.hasVisual);
a._updateUndoStatus(L);
h.setDisabled("outdent",!L.queryCommandState("Outdent"));
s=J("A");
if(d=h.get("link")){d.setDisabled((!s&&v)||(s&&!s.href));
d.setActive(!!s&&(!s.name&&!s.id))
}if(d=h.get("unlink")){d.setDisabled(!s&&v);
d.setActive(!!s&&!s.name&&!s.id)
}if(d=h.get("anchor")){d.setActive(!v&&!!s&&(s.name||(s.id&&!s.href)))
}s=J("IMG");
if(d=h.get("image")){d.setActive(!v&&!!s&&i.className.indexOf("mceItem")==-1)
}if(d=h.get("styleselect")){a._importClasses();
O=[];
m(d.items,function(u){O.push(u.value)
});
P=L.formatter.matchAll(O);
d.select(P[0]);
k.each(P,function(u,w){if(w>0){d.mark(u)
}})
}if(d=h.get("formatselect")){s=J(L.dom.isBlock);
if(s){d.select(s.nodeName.toLowerCase())
}}J(function(u){if(u.nodeName==="SPAN"){if(!c&&u.className){c=u.className
}}if(L.dom.is(u,K.theme_advanced_font_selector)){if(!N&&u.style.fontSize){N=u.style.fontSize
}if(!e&&u.style.fontFamily){e=u.style.fontFamily.replace(/[\"\']+/g,"").replace(/^([^,]+).*/,"$1").toLowerCase()
}if(!t&&u.style.color){t=u.style.color
}if(!M&&u.style.backgroundColor){M=u.style.backgroundColor
}}return false
});
if(d=h.get("fontselect")){d.select(function(u){return u.replace(/^([^,]+).*/,"$1").toLowerCase()==e
})
}if(d=h.get("fontsizeselect")){if(K.theme_advanced_runtime_fontsize&&!N&&!c){N=L.dom.getStyle(i,"fontSize",true)
}d.select(function(u){if(u.fontSize&&u.fontSize===N){return true
}if(u["class"]&&u["class"]===c){return true
}})
}if(K.theme_advanced_show_current_color){function I(u,w){if(d=h.get(u)){if(!w){w=d.settings.default_color
}if(w!==d.value){d.displayColor(w)
}}}I("forecolor",t);
I("backcolor",M)
}if(K.theme_advanced_show_current_color){function I(u,w){if(d=h.get(u)){if(!w){w=d.settings.default_color
}if(w!==d.value){d.displayColor(w)
}}}I("forecolor",t);
I("backcolor",M)
}if(K.theme_advanced_path&&K.theme_advanced_statusbar_location){s=j.get(L.id+"_path")||j.add(L.id+"_path_row","span",{id:L.id+"_path"});
if(a.statusKeyboardNavigation){a.statusKeyboardNavigation.destroy();
a.statusKeyboardNavigation=null
}j.setHTML(s,"");
J(function(w){var z=w.nodeName.toLowerCase(),y,u,x="";
if(w.nodeType!=1||z==="br"||w.getAttribute("data-mce-bogus")||j.hasClass(w,"mceItemHidden")||j.hasClass(w,"mceItemRemoved")){return
}if(k.isIE&&w.scopeName!=="HTML"&&w.scopeName){z=w.scopeName+":"+z
}z=z.replace(/mce\:/g,"");
switch(z){case"b":z="strong";
break;
case"i":z="em";
break;
case"img":if(b=j.getAttrib(w,"src")){x+="src: "+b+" "
}break;
case"a":if(b=j.getAttrib(w,"name")){x+="name: "+b+" ";
z+="#"+b
}if(b=j.getAttrib(w,"href")){x+="href: "+b+" "
}break;
case"font":if(b=j.getAttrib(w,"face")){x+="font: "+b+" "
}if(b=j.getAttrib(w,"size")){x+="size: "+b+" "
}if(b=j.getAttrib(w,"color")){x+="color: "+b+" "
}break;
case"span":if(b=j.getAttrib(w,"style")){x+="style: "+b+" "
}break
}if(b=j.getAttrib(w,"id")){x+="id: "+b+" "
}if(b=w.className){b=b.replace(/\b\s*(webkit|mce|Apple-)\w+\s*\b/g,"");
if(b){x+="class: "+b+" ";
if(L.dom.isBlock(w)||z=="img"||z=="span"){z+="."+b
}}}z=z.replace(/(html:)/g,"");
z={name:z,node:w,title:x};
a.onResolveName.dispatch(a,z);
x=z.title;
z=z.name;
u=j.create("a",{href:"javascript:;",role:"button",onmousedown:"return false;",title:x,"class":"mcePath_"+(f++)},z);
if(s.hasChildNodes()){s.insertBefore(j.create("span",{"aria-hidden":"true"},"\u00a0\u00bb "),s.firstChild);
s.insertBefore(u,s.firstChild)
}else{s.appendChild(u)
}},L.getBody());
if(j.select("a",s).length>0){a.statusKeyboardNavigation=new k.ui.KeyboardNavigation({root:L.id+"_path_row",items:j.select("a",s),excludeFromTabOrder:true,onCancel:function(){L.focus()
}},j)
}}},_sel:function(a){this.editor.execCommand("mceSelectNodeDepth",false,a)
},_mceInsertAnchor:function(a,b){var c=this.editor;
c.windowManager.open({url:this.url+"/anchor.htm",width:320+parseInt(c.getLang("advanced.anchor_delta_width",0)),height:90+parseInt(c.getLang("advanced.anchor_delta_height",0)),inline:true},{theme_url:this.url})
},_mceCharMap:function(){var a=this.editor;
a.windowManager.open({url:this.url+"/charmap.htm",width:550+parseInt(a.getLang("advanced.charmap_delta_width",0)),height:265+parseInt(a.getLang("advanced.charmap_delta_height",0)),inline:true},{theme_url:this.url})
},_mceHelp:function(){var a=this.editor;
a.windowManager.open({url:this.url+"/about.htm",width:480,height:380,inline:true},{theme_url:this.url})
},_mceShortcuts:function(){var a=this.editor;
a.windowManager.open({url:this.url+"/shortcuts.htm",width:480,height:380,inline:true},{theme_url:this.url})
},_mceColorPicker:function(a,b){var c=this.editor;
b=b||{};
c.windowManager.open({url:this.url+"/color_picker.htm",width:375+parseInt(c.getLang("advanced.colorpicker_delta_width",0)),height:250+parseInt(c.getLang("advanced.colorpicker_delta_height",0)),close_previous:false,inline:true},{input_color:b.color,func:b.func,theme_url:this.url})
},_mceCodeEditor:function(b,a){var c=this.editor;
c.windowManager.open({url:this.url+"/source_editor.htm",width:parseInt(c.getParam("theme_advanced_source_editor_width",720)),height:parseInt(c.getParam("theme_advanced_source_editor_height",580)),inline:true,resizable:true,maximizable:true},{theme_url:this.url})
},_mceImage:function(b,a){var c=this.editor;
if(c.dom.getAttrib(c.selection.getNode(),"class","").indexOf("mceItem")!=-1){return
}c.windowManager.open({url:this.url+"/image.htm",width:355+parseInt(c.getLang("advanced.image_delta_width",0)),height:275+parseInt(c.getLang("advanced.image_delta_height",0)),inline:true},{theme_url:this.url})
},_mceLink:function(b,a){var c=this.editor;
c.windowManager.open({url:this.url+"/link.htm",width:310+parseInt(c.getLang("advanced.link_delta_width",0)),height:200+parseInt(c.getLang("advanced.link_delta_height",0)),inline:true},{theme_url:this.url})
},_mceNewDocument:function(){var a=this.editor;
a.windowManager.confirm("advanced.newdocument",function(b){if(b){a.execCommand("mceSetContent",false,"")
}})
},_mceForeColor:function(){var a=this;
this._mceColorPicker(0,{color:a.fgColor,func:function(b){a.fgColor=b;
a.editor.execCommand("ForeColor",false,b)
}})
},_mceBackColor:function(){var a=this;
this._mceColorPicker(0,{color:a.bgColor,func:function(b){a.bgColor=b;
a.editor.execCommand("HiliteColor",false,b)
}})
},_ufirst:function(a){return a.substring(0,1).toUpperCase()+a.substring(1)
}});
k.ThemeManager.add("advanced",k.themes.AdvancedTheme)
}(tinymce));