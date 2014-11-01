(function(){var c=YAHOO.util.Dom,a=YAHOO.util.Event,b=YAHOO.lang;
if(YAHOO.widget.Button){YAHOO.widget.ToolbarButtonAdvanced=YAHOO.widget.Button;
YAHOO.widget.ToolbarButtonAdvanced.prototype.buttonType="rich";
YAHOO.widget.ToolbarButtonAdvanced.prototype.checkValue=function(d){var e=this.getMenu().getItems();
if(e.length===0){this.getMenu()._onBeforeShow();
e=this.getMenu().getItems()
}for(var f=0;
f<e.length;
f++){e[f].cfg.setProperty("checked",false);
if(e[f].value==d){e[f].cfg.setProperty("checked",true)
}}}
}else{YAHOO.widget.ToolbarButtonAdvanced=function(){}
}YAHOO.widget.ToolbarButton=function(f,g){if(b.isObject(arguments[0])&&!c.get(f).nodeType){g=f
}var d=(g||{});
var e={element:null,attributes:d};
if(!e.attributes.type){e.attributes.type="push"
}e.element=document.createElement("span");
e.element.setAttribute("unselectable","on");
e.element.className="yui-button yui-"+e.attributes.type+"-button";
e.element.innerHTML='<span class="first-child"><a href="#">LABEL</a></span>';
e.element.firstChild.firstChild.tabIndex="-1";
e.attributes.id=c.generateId();
YAHOO.widget.ToolbarButton.superclass.constructor.call(this,e.element,e.attributes)
};
YAHOO.extend(YAHOO.widget.ToolbarButton,YAHOO.util.Element,{buttonType:"normal",_handleMouseOver:function(){if(!this.get("disabled")){this.addClass("yui-button-hover");
this.addClass("yui-"+this.get("type")+"-button-hover")
}},_handleMouseOut:function(){this.removeClass("yui-button-hover");
this.removeClass("yui-"+this.get("type")+"-button-hover")
},checkValue:function(d){if(this.get("type")=="menu"){var e=this._button.options;
for(var f=0;
f<e.length;
f++){if(e[f].value==d){e.selectedIndex=f
}}}},init:function(d,e){YAHOO.widget.ToolbarButton.superclass.init.call(this,d,e);
this.on("mouseover",this._handleMouseOver,this,true);
this.on("mouseout",this._handleMouseOut,this,true)
},initAttributes:function(d){YAHOO.widget.ToolbarButton.superclass.initAttributes.call(this,d);
this.setAttributeConfig("value",{value:d.value});
this.setAttributeConfig("menu",{value:d.menu||false});
this.setAttributeConfig("type",{value:d.type,writeOnce:true,method:function(f){var g,h;
if(!this._button){this._button=this.get("element").getElementsByTagName("a")[0]
}switch(f){case"select":case"menu":g=document.createElement("select");
var e=this.get("menu");
for(var i=0;
i<e.length;
i++){h=document.createElement("option");
h.innerHTML=e[i].text;
h.value=e[i].value;
if(e[i].checked){h.selected=true
}g.appendChild(h)
}this._button.parentNode.replaceChild(g,this._button);
a.on(g,"change",this._handleSelect,this,true);
this._button=g;
break
}}});
this.setAttributeConfig("disabled",{value:d.disabled||false,method:function(e){if(e){this.addClass("yui-button-disabled");
this.addClass("yui-"+this.get("type")+"-button-disabled")
}else{this.removeClass("yui-button-disabled");
this.removeClass("yui-"+this.get("type")+"-button-disabled")
}if(this.get("type")=="menu"){this._button.disabled=e
}}});
this.setAttributeConfig("label",{value:d.label,method:function(e){if(!this._button){this._button=this.get("element").getElementsByTagName("a")[0]
}if(this.get("type")=="push"){this._button.innerHTML=e
}}});
this.setAttributeConfig("title",{value:d.title});
this.setAttributeConfig("container",{value:null,writeOnce:true,method:function(e){this.appendTo(e)
}})
},_handleSelect:function(e){var f=a.getTarget(e);
var d=f.options[f.selectedIndex].value;
this.fireEvent("change",{type:"change",value:d})
},getMenu:function(){return this.get("menu")
},destroy:function(){a.purgeElement(this.get("element"),true);
this.get("element").parentNode.removeChild(this.get("element"));
for(var d in this){if(b.hasOwnProperty(this,d)){this[d]=null
}}},fireEvent:function(d,e){if(this.DOM_EVENTS[d]&&this.get("disabled")){return
}YAHOO.widget.ToolbarButton.superclass.fireEvent.call(this,d,e)
},toString:function(){return"ToolbarButton ("+this.get("id")+")"
}})
})();
(function(){var c=YAHOO.util.Dom,a=YAHOO.util.Event,b=YAHOO.lang;
var d=function(e){var f=e;
if(b.isString(e)){f=this.getButtonById(e)
}if(b.isNumber(e)){f=this.getButtonByIndex(e)
}if((!(f instanceof YAHOO.widget.ToolbarButton))&&(!(f instanceof YAHOO.widget.ToolbarButtonAdvanced))){f=this.getButtonByValue(e)
}if((f instanceof YAHOO.widget.ToolbarButton)||(f instanceof YAHOO.widget.ToolbarButtonAdvanced)){return f
}return false
};
YAHOO.widget.Toolbar=function(g,h){if(b.isObject(arguments[0])&&!c.get(g).nodeType){h=g
}var e={};
if(h){b.augmentObject(e,h)
}var f={element:null,attributes:e};
if(b.isString(g)&&c.get(g)){f.element=c.get(g)
}else{if(b.isObject(g)&&c.get(g)&&c.get(g).nodeType){f.element=c.get(g)
}}if(!f.element){f.element=document.createElement("DIV");
f.element.id=c.generateId();
if(e.container&&c.get(e.container)){c.get(e.container).appendChild(f.element)
}}if(!f.element.id){f.element.id=((b.isString(g))?g:c.generateId())
}var j=document.createElement("fieldset");
var i=document.createElement("legend");
i.innerHTML="Toolbar";
j.appendChild(i);
var k=document.createElement("DIV");
f.attributes.cont=k;
c.addClass(k,"yui-toolbar-subcont");
j.appendChild(k);
f.element.appendChild(j);
f.element.tabIndex=-1;
f.attributes.element=f.element;
f.attributes.id=f.element.id;
YAHOO.widget.Toolbar.superclass.constructor.call(this,f.element,f.attributes)
};
YAHOO.extend(YAHOO.widget.Toolbar,YAHOO.util.Element,{_addMenuClasses:function(f,i,e){c.addClass(this.element,"yui-toolbar-"+e.get("value")+"-menu");
if(c.hasClass(e._button.parentNode.parentNode,"yui-toolbar-select")){c.addClass(this.element,"yui-toolbar-select-menu")
}var h=this.getItems();
for(var g=0;
g<h.length;
g++){c.addClass(h[g].element,"yui-toolbar-"+e.get("value")+"-"+((h[g].value)?h[g].value.replace(/ /g,"-").toLowerCase():h[g]._oText.nodeValue.replace(/ /g,"-").toLowerCase()));
c.addClass(h[g].element,"yui-toolbar-"+e.get("value")+"-"+((h[g].value)?h[g].value.replace(/ /g,"-"):h[g]._oText.nodeValue.replace(/ /g,"-")))
}},buttonType:YAHOO.widget.ToolbarButton,dd:null,_colorData:{"#111111":"Obsidian","#2D2D2D":"Dark Gray","#434343":"Shale","#5B5B5B":"Flint","#737373":"Gray","#8B8B8B":"Concrete","#A2A2A2":"Gray","#B9B9B9":"Titanium","#000000":"Black","#D0D0D0":"Light Gray","#E6E6E6":"Silver","#FFFFFF":"White","#BFBF00":"Pumpkin","#FFFF00":"Yellow","#FFFF40":"Banana","#FFFF80":"Pale Yellow","#FFFFBF":"Butter","#525330":"Raw Siena","#898A49":"Mildew","#AEA945":"Olive","#7F7F00":"Paprika","#C3BE71":"Earth","#E0DCAA":"Khaki","#FCFAE1":"Cream","#60BF00":"Cactus","#80FF00":"Chartreuse","#A0FF40":"Green","#C0FF80":"Pale Lime","#DFFFBF":"Light Mint","#3B5738":"Green","#668F5A":"Lime Gray","#7F9757":"Yellow","#407F00":"Clover","#8A9B55":"Pistachio","#B7C296":"Light Jade","#E6EBD5":"Breakwater","#00BF00":"Spring Frost","#00FF80":"Pastel Green","#40FFA0":"Light Emerald","#80FFC0":"Sea Foam","#BFFFDF":"Sea Mist","#033D21":"Dark Forrest","#438059":"Moss","#7FA37C":"Medium Green","#007F40":"Pine","#8DAE94":"Yellow Gray Green","#ACC6B5":"Aqua Lung","#DDEBE2":"Sea Vapor","#00BFBF":"Fog","#00FFFF":"Cyan","#40FFFF":"Turquoise Blue","#80FFFF":"Light Aqua","#BFFFFF":"Pale Cyan","#033D3D":"Dark Teal","#347D7E":"Gray Turquoise","#609A9F":"Green Blue","#007F7F":"Seaweed","#96BDC4":"Green Gray","#B5D1D7":"Soapstone","#E2F1F4":"Light Turquoise","#0060BF":"Summer Sky","#0080FF":"Sky Blue","#40A0FF":"Electric Blue","#80C0FF":"Light Azure","#BFDFFF":"Ice Blue","#1B2C48":"Navy","#385376":"Biscay","#57708F":"Dusty Blue","#00407F":"Sea Blue","#7792AC":"Sky Blue Gray","#A8BED1":"Morning Sky","#DEEBF6":"Vapor","#0000BF":"Deep Blue","#0000FF":"Blue","#4040FF":"Cerulean Blue","#8080FF":"Evening Blue","#BFBFFF":"Light Blue","#212143":"Deep Indigo","#373E68":"Sea Blue","#444F75":"Night Blue","#00007F":"Indigo Blue","#585E82":"Dockside","#8687A4":"Blue Gray","#D2D1E1":"Light Blue Gray","#6000BF":"Neon Violet","#8000FF":"Blue Violet","#A040FF":"Violet Purple","#C080FF":"Violet Dusk","#DFBFFF":"Pale Lavender","#302449":"Cool Shale","#54466F":"Dark Indigo","#655A7F":"Dark Violet","#40007F":"Violet","#726284":"Smoky Violet","#9E8FA9":"Slate Gray","#DCD1DF":"Violet White","#BF00BF":"Royal Violet","#FF00FF":"Fuchsia","#FF40FF":"Magenta","#FF80FF":"Orchid","#FFBFFF":"Pale Magenta","#4A234A":"Dark Purple","#794A72":"Medium Purple","#936386":"Cool Granite","#7F007F":"Purple","#9D7292":"Purple Moon","#C0A0B6":"Pale Purple","#ECDAE5":"Pink Cloud","#BF005F":"Hot Pink","#FF007F":"Deep Pink","#FF409F":"Grape","#FF80BF":"Electric Pink","#FFBFDF":"Pink","#451528":"Purple Red","#823857":"Purple Dino","#A94A76":"Purple Gray","#7F003F":"Rose","#BC6F95":"Antique Mauve","#D8A5BB":"Cool Marble","#F7DDE9":"Pink Granite","#C00000":"Apple","#FF0000":"Fire Truck","#FF4040":"Pale Red","#FF8080":"Salmon","#FFC0C0":"Warm Pink","#441415":"Sepia","#82393C":"Rust","#AA4D4E":"Brick","#800000":"Brick Red","#BC6E6E":"Mauve","#D8A3A4":"Shrimp Pink","#F8DDDD":"Shell Pink","#BF5F00":"Dark Orange","#FF7F00":"Orange","#FF9F40":"Grapefruit","#FFBF80":"Canteloupe","#FFDFBF":"Wax","#482C1B":"Dark Brick","#855A40":"Dirt","#B27C51":"Tan","#7F3F00":"Nutmeg","#C49B71":"Mustard","#E1C4A8":"Pale Tan","#FDEEE0":"Marble"},_colorPicker:null,STR_COLLAPSE:"Collapse Toolbar",STR_SPIN_LABEL:"Spin Button with value {VALUE}. Use Control Shift Up Arrow and Control Shift Down arrow keys to increase or decrease the value.",STR_SPIN_UP:"Click to increase the value of this input",STR_SPIN_DOWN:"Click to decrease the value of this input",_titlebar:null,browser:YAHOO.env.ua,_buttonList:null,_buttonGroupList:null,_sep:null,_sepCount:null,_dragHandle:null,_toolbarConfigs:{renderer:true},CLASS_CONTAINER:"yui-toolbar-container",CLASS_DRAGHANDLE:"yui-toolbar-draghandle",CLASS_SEPARATOR:"yui-toolbar-separator",CLASS_DISABLED:"yui-toolbar-disabled",CLASS_PREFIX:"yui-toolbar",init:function(e,f){YAHOO.widget.Toolbar.superclass.init.call(this,e,f)
},initAttributes:function(e){YAHOO.widget.Toolbar.superclass.initAttributes.call(this,e);
this.addClass(this.CLASS_CONTAINER);
this.setAttributeConfig("buttonType",{value:e.buttonType||"basic",writeOnce:true,validator:function(f){switch(f){case"advanced":case"basic":return true
}return false
},method:function(f){if(f=="advanced"){if(YAHOO.widget.Button){this.buttonType=YAHOO.widget.ToolbarButtonAdvanced
}else{this.buttonType=YAHOO.widget.ToolbarButton
}}else{this.buttonType=YAHOO.widget.ToolbarButton
}}});
this.setAttributeConfig("buttons",{value:[],writeOnce:true,method:function(f){for(var g in f){if(b.hasOwnProperty(f,g)){if(f[g].type=="separator"){this.addSeparator()
}else{if(f[g].group!==undefined){this.addButtonGroup(f[g])
}else{this.addButton(f[g])
}}}}}});
this.setAttributeConfig("disabled",{value:false,method:function(f){if(this.get("disabled")===f){return false
}if(f){this.addClass(this.CLASS_DISABLED);
this.set("draggable",false);
this.disableAllButtons()
}else{this.removeClass(this.CLASS_DISABLED);
if(this._configs.draggable._initialConfig.value){this.set("draggable",true)
}this.resetAllButtons()
}}});
this.setAttributeConfig("cont",{value:e.cont,readOnly:true});
this.setAttributeConfig("grouplabels",{value:((e.grouplabels===false)?false:true),method:function(f){if(f){c.removeClass(this.get("cont"),(this.CLASS_PREFIX+"-nogrouplabels"))
}else{c.addClass(this.get("cont"),(this.CLASS_PREFIX+"-nogrouplabels"))
}}});
this.setAttributeConfig("titlebar",{value:false,method:function(f){if(f){if(this._titlebar&&this._titlebar.parentNode){this._titlebar.parentNode.removeChild(this._titlebar)
}this._titlebar=document.createElement("DIV");
this._titlebar.tabIndex="-1";
a.on(this._titlebar,"focus",function(){this._handleFocus()
},this,true);
c.addClass(this._titlebar,this.CLASS_PREFIX+"-titlebar");
if(b.isString(f)){var g=document.createElement("h2");
g.tabIndex="-1";
g.innerHTML='<a href="#" tabIndex="0">'+f+"</a>";
this._titlebar.appendChild(g);
a.on(g.firstChild,"click",function(h){a.stopEvent(h)
});
a.on([g,g.firstChild],"focus",function(){this._handleFocus()
},this,true)
}if(this.get("firstChild")){this.insertBefore(this._titlebar,this.get("firstChild"))
}else{this.appendChild(this._titlebar)
}if(this.get("collapse")){this.set("collapse",true)
}}else{if(this._titlebar){if(this._titlebar&&this._titlebar.parentNode){this._titlebar.parentNode.removeChild(this._titlebar)
}}}}});
this.setAttributeConfig("collapse",{value:false,method:function(f){if(this._titlebar){var g=null;
var h=c.getElementsByClassName("collapse","span",this._titlebar);
if(f){if(h.length>0){return true
}g=document.createElement("SPAN");
g.innerHTML="X";
g.title=this.STR_COLLAPSE;
c.addClass(g,"collapse");
this._titlebar.appendChild(g);
a.addListener(g,"click",function(){if(c.hasClass(this.get("cont").parentNode,"yui-toolbar-container-collapsed")){this.collapse(false)
}else{this.collapse()
}},this,true)
}else{g=c.getElementsByClassName("collapse","span",this._titlebar);
if(g[0]){if(c.hasClass(this.get("cont").parentNode,"yui-toolbar-container-collapsed")){this.collapse(false)
}g[0].parentNode.removeChild(g[0])
}}}}});
this.setAttributeConfig("draggable",{value:(e.draggable||false),method:function(f){if(f&&!this.get("titlebar")){if(!this._dragHandle){this._dragHandle=document.createElement("SPAN");
this._dragHandle.innerHTML="|";
this._dragHandle.setAttribute("title","Click to drag the toolbar");
this._dragHandle.id=this.get("id")+"_draghandle";
c.addClass(this._dragHandle,this.CLASS_DRAGHANDLE);
if(this.get("cont").hasChildNodes()){this.get("cont").insertBefore(this._dragHandle,this.get("cont").firstChild)
}else{this.get("cont").appendChild(this._dragHandle)
}this.dd=new YAHOO.util.DD(this.get("id"));
this.dd.setHandleElId(this._dragHandle.id)
}}else{if(this._dragHandle){this._dragHandle.parentNode.removeChild(this._dragHandle);
this._dragHandle=null;
this.dd=null
}}if(this._titlebar){if(f){this.dd=new YAHOO.util.DD(this.get("id"));
this.dd.setHandleElId(this._titlebar);
c.addClass(this._titlebar,"draggable")
}else{c.removeClass(this._titlebar,"draggable");
if(this.dd){this.dd.unreg();
this.dd=null
}}}},validator:function(f){var g=true;
if(!YAHOO.util.DD){g=false
}return g
}})
},addButtonGroup:function(f){if(!this.get("element")){this._queue[this._queue.length]=["addButtonGroup",arguments];
return false
}if(!this.hasClass(this.CLASS_PREFIX+"-grouped")){this.addClass(this.CLASS_PREFIX+"-grouped")
}var e=document.createElement("DIV");
c.addClass(e,this.CLASS_PREFIX+"-group");
c.addClass(e,this.CLASS_PREFIX+"-group-"+f.group);
if(f.label){var i=document.createElement("h3");
i.innerHTML=f.label;
e.appendChild(i)
}if(!this.get("grouplabels")){c.addClass(this.get("cont"),this.CLASS_PREFIX,"-nogrouplabels")
}this.get("cont").appendChild(e);
var g=document.createElement("ul");
e.appendChild(g);
if(!this._buttonGroupList){this._buttonGroupList={}
}this._buttonGroupList[f.group]=g;
for(var h=0;
h<f.buttons.length;
h++){var j=document.createElement("li");
j.className=this.CLASS_PREFIX+"-groupitem";
g.appendChild(j);
if((f.buttons[h].type!==undefined)&&f.buttons[h].type=="separator"){this.addSeparator(j)
}else{f.buttons[h].container=j;
this.addButton(f.buttons[h])
}}},addButtonToGroup:function(g,f,e){var h=this._buttonGroupList[f];
var i=document.createElement("li");
i.className=this.CLASS_PREFIX+"-groupitem";
g.container=i;
this.addButton(g,e);
h.appendChild(i)
},addButton:function(q,r){if(!this.get("element")){this._queue[this._queue.length]=["addButton",arguments];
return false
}if(!this._buttonList){this._buttonList=[]
}if(!q.container){q.container=this.get("cont")
}if((q.type=="menu")||(q.type=="split")||(q.type=="select")){if(b.isArray(q.menu)){for(var k in q.menu){if(b.hasOwnProperty(q.menu,k)){var e={fn:function(x,w,y){if(!q.menucmd){q.menucmd=q.value
}q.value=((y.value)?y.value:y._oText.nodeValue)
},scope:this};
q.menu[k].onclick=e
}}}}var j={},m=false;
for(var o in q){if(b.hasOwnProperty(q,o)){if(!this._toolbarConfigs[o]){j[o]=q[o]
}}}if(q.type=="select"){j.type="menu"
}if(q.type=="spin"){j.type="push"
}if(j.type=="color"){if(YAHOO.widget.Overlay){j=this._makeColorButton(j)
}else{m=true
}}if(j.menu){if((YAHOO.widget.Overlay)&&(q.menu instanceof YAHOO.widget.Overlay)){q.menu.showEvent.subscribe(function(){this._button=j
})
}else{for(var l=0;
l<j.menu.length;
l++){if(!j.menu[l].value){j.menu[l].value=j.menu[l].text
}}if(this.browser.webkit){j.focusmenu=false
}}}if(m){q=false
}else{this._configs.buttons.value[this._configs.buttons.value.length]=q;
var g=new this.buttonType(j);
g.get("element").tabIndex="-1";
g.get("element").setAttribute("role","button");
g._selected=true;
if(this.get("disabled")){g.set("disabled",true)
}if(!q.id){q.id=g.get("id")
}if(r){var u=g.get("element");
var n=null;
if(r.get){n=r.get("element").nextSibling
}else{if(r.nextSibling){n=r.nextSibling
}}if(n){n.parentNode.insertBefore(u,n)
}}g.addClass(this.CLASS_PREFIX+"-"+g.get("value"));
var h=document.createElement("span");
h.className=this.CLASS_PREFIX+"-icon";
g.get("element").insertBefore(h,g.get("firstChild"));
if(g._button.tagName.toLowerCase()=="button"){g.get("element").setAttribute("unselectable","on");
var f=document.createElement("a");
f.innerHTML=g._button.innerHTML;
f.href="#";
f.tabIndex="-1";
a.on(f,"click",function(w){a.stopEvent(w)
});
g._button.parentNode.replaceChild(f,g._button);
g._button=f
}if(q.type=="select"){if(g._button.tagName.toLowerCase()=="select"){h.parentNode.removeChild(h);
var t=g._button;
var i=g.get("element");
i.parentNode.replaceChild(t,i)
}else{g.addClass(this.CLASS_PREFIX+"-select")
}}if(q.type=="spin"){if(!b.isArray(q.range)){q.range=[10,100]
}this._makeSpinButton(g,q)
}g.get("element").setAttribute("title",g.get("label"));
if(q.type!="spin"){if((YAHOO.widget.Overlay)&&(j.menu instanceof YAHOO.widget.Overlay)){var s=function(x){var w=true;
if(x.keyCode&&(x.keyCode==9)){w=false
}if(w){if(this._colorPicker){this._colorPicker._button=q.value
}var y=g.getMenu().element;
if(c.getStyle(y,"visibility")=="hidden"){g.getMenu().show()
}else{g.getMenu().hide()
}}YAHOO.util.Event.stopEvent(x)
};
g.on("mousedown",s,q,this);
g.on("keydown",s,q,this)
}else{if((q.type!="menu")&&(q.type!="select")){g.on("keypress",this._buttonClick,q,this);
g.on("mousedown",function(w){YAHOO.util.Event.stopEvent(w);
this._buttonClick(w,q)
},q,this);
g.on("click",function(w){YAHOO.util.Event.stopEvent(w)
})
}else{g.on("mousedown",function(w){YAHOO.util.Event.stopEvent(w)
});
g.on("click",function(w){YAHOO.util.Event.stopEvent(w)
});
g.on("change",function(w){if(!q.menucmd){q.menucmd=q.value
}q.value=w.value;
this._buttonClick(w,q)
},this,true);
var p=this;
g.on("appendTo",function(){var w=this;
if(w.getMenu()&&w.getMenu().mouseDownEvent){w.getMenu().mouseDownEvent.subscribe(function(x,y){var z=y[1];
YAHOO.util.Event.stopEvent(y[0]);
w._onMenuClick(y[0],w);
if(!q.menucmd){q.menucmd=q.value
}q.value=((z.value)?z.value:z._oText.nodeValue);
p._buttonClick.call(p,y[1],q);
w._hideMenu();
return false
});
w.getMenu().clickEvent.subscribe(function(x,y){YAHOO.util.Event.stopEvent(y[0])
});
w.getMenu().mouseUpEvent.subscribe(function(x,y){YAHOO.util.Event.stopEvent(y[0])
})
}})
}}}else{g.on("mousedown",function(w){YAHOO.util.Event.stopEvent(w)
});
g.on("click",function(w){YAHOO.util.Event.stopEvent(w)
})
}if(this.browser.ie){}if(this.browser.webkit){g.hasFocus=function(){return true
}
}this._buttonList[this._buttonList.length]=g;
if((q.type=="menu")||(q.type=="split")||(q.type=="select")){if(b.isArray(q.menu)){var v=g.getMenu();
if(v&&v.renderEvent){v.renderEvent.subscribe(this._addMenuClasses,g);
if(q.renderer){v.renderEvent.subscribe(q.renderer,g)
}}}}}return q
},addSeparator:function(i,f){if(!this.get("element")){this._queue[this._queue.length]=["addSeparator",arguments];
return false
}var h=((i)?i:this.get("cont"));
if(!this.get("element")){this._queue[this._queue.length]=["addSeparator",arguments];
return false
}if(this._sepCount===null){this._sepCount=0
}if(!this._sep){this._sep=document.createElement("SPAN");
c.addClass(this._sep,this.CLASS_SEPARATOR);
this._sep.innerHTML="|"
}var g=this._sep.cloneNode(true);
this._sepCount++;
c.addClass(g,this.CLASS_SEPARATOR+"-"+this._sepCount);
if(f){var e=null;
if(f.get){e=f.get("element").nextSibling
}else{if(f.nextSibling){e=f.nextSibling
}else{e=f
}}if(e){if(e==f){e.parentNode.appendChild(g)
}else{e.parentNode.insertBefore(g,e)
}}}else{h.appendChild(g)
}return g
},_createColorPicker:function(e){if(c.get(e+"_colors")){c.get(e+"_colors").parentNode.removeChild(c.get(e+"_colors"))
}var h=document.createElement("div");
h.className="yui-toolbar-colors";
h.id=e+"_colors";
h.style.display="none";
a.on(window,"load",function(){document.body.appendChild(h)
},this,true);
this._colorPicker=h;
var f="";
for(var g in this._colorData){if(b.hasOwnProperty(this._colorData,g)){f+='<a style="background-color: '+g+'" href="#">'+g.replace("#","")+"</a>"
}}f+="<span><em>X</em><strong></strong></span>";
window.setTimeout(function(){h.innerHTML=f
},0);
a.on(h,"mouseover",function(m){var j=this._colorPicker;
var i=j.getElementsByTagName("em")[0];
var k=j.getElementsByTagName("strong")[0];
var l=a.getTarget(m);
if(l.tagName.toLowerCase()=="a"){i.style.backgroundColor=l.style.backgroundColor;
k.innerHTML=this._colorData["#"+l.innerHTML]+"<br>"+l.innerHTML
}},this,true);
a.on(h,"focus",function(i){a.stopEvent(i)
});
a.on(h,"click",function(i){a.stopEvent(i)
});
a.on(h,"mousedown",function(k){a.stopEvent(k);
var l=a.getTarget(k);
if(l.tagName.toLowerCase()=="a"){var i=this.fireEvent("colorPickerClicked",{type:"colorPickerClicked",target:this,button:this._colorPicker._button,color:l.innerHTML,colorName:this._colorData["#"+l.innerHTML]});
if(i!==false){var j={color:l.innerHTML,colorName:this._colorData["#"+l.innerHTML],value:this._colorPicker._button};
this.fireEvent("buttonClick",{type:"buttonClick",target:this.get("element"),button:j})
}this.getButtonByValue(this._colorPicker._button).getMenu().hide()
}},this,true)
},_resetColorPicker:function(){var e=this._colorPicker.getElementsByTagName("em")[0];
var f=this._colorPicker.getElementsByTagName("strong")[0];
e.style.backgroundColor="transparent";
f.innerHTML=""
},_makeColorButton:function(e){if(!this._colorPicker){this._createColorPicker(this.get("id"))
}e.type="color";
e.menu=new YAHOO.widget.Overlay(this.get("id")+"_"+e.value+"_menu",{visible:false,position:"absolute",iframe:true});
e.menu.setBody("");
e.menu.render(this.get("cont"));
c.addClass(e.menu.element,"yui-button-menu");
c.addClass(e.menu.element,"yui-color-button-menu");
e.menu.beforeShowEvent.subscribe(function(){e.menu.cfg.setProperty("zindex",5);
e.menu.cfg.setProperty("context",[this.getButtonById(e.id).get("element"),"tl","bl"]);
this._resetColorPicker();
var f=this._colorPicker;
if(f.parentNode){f.parentNode.removeChild(f)
}e.menu.setBody("");
e.menu.appendToBody(f);
this._colorPicker.style.display="block"
},this,true);
return e
},_makeSpinButton:function(f,l){f.addClass(this.CLASS_PREFIX+"-spinbutton");
var e=this,j=f._button.parentNode.parentNode,o=l.range,p=document.createElement("a"),q=document.createElement("a");
p.href="#";
q.href="#";
p.tabIndex="-1";
q.tabIndex="-1";
p.className="up";
p.title=this.STR_SPIN_UP;
p.innerHTML=this.STR_SPIN_UP;
q.className="down";
q.title=this.STR_SPIN_DOWN;
q.innerHTML=this.STR_SPIN_DOWN;
j.appendChild(p);
j.appendChild(q);
var k=YAHOO.lang.substitute(this.STR_SPIN_LABEL,{VALUE:f.get("label")});
f.set("title",k);
var g=function(t){t=((t<o[0])?o[0]:t);
t=((t>o[1])?o[1]:t);
return t
};
var h=this.browser;
var r=false;
var m=this.STR_SPIN_LABEL;
if(this._titlebar&&this._titlebar.firstChild){r=this._titlebar.firstChild
}var s=function(u){YAHOO.util.Event.stopEvent(u);
if(!f.get("disabled")&&(u.keyCode!=9)){var t=parseInt(f.get("label"),10);
t++;
t=g(t);
f.set("label",""+t);
var v=YAHOO.lang.substitute(m,{VALUE:f.get("label")});
f.set("title",v);
if(!h.webkit&&r){}e._buttonClick(u,l)
}};
var i=function(u){YAHOO.util.Event.stopEvent(u);
if(!f.get("disabled")&&(u.keyCode!=9)){var t=parseInt(f.get("label"),10);
t--;
t=g(t);
f.set("label",""+t);
var v=YAHOO.lang.substitute(m,{VALUE:f.get("label")});
f.set("title",v);
if(!h.webkit&&r){}e._buttonClick(u,l)
}};
var n=function(t){if(t.keyCode==38){s(t)
}else{if(t.keyCode==40){i(t)
}else{if(t.keyCode==107&&t.shiftKey){s(t)
}else{if(t.keyCode==109&&t.shiftKey){i(t)
}}}}};
f.on("keydown",n,this,true);
a.on(p,"mousedown",function(t){a.stopEvent(t)
},this,true);
a.on(q,"mousedown",function(t){a.stopEvent(t)
},this,true);
a.on(p,"click",s,this,true);
a.on(q,"click",i,this,true)
},_buttonClick:function(g,m){var n=true;
if(g&&g.type=="keypress"){if(g.keyCode==9){n=false
}else{if((g.keyCode===13)||(g.keyCode===0)||(g.keyCode===32)){}else{n=false
}}}if(n){var e=true,k=false;
m.isSelected=this.isSelected(m.id);
if(m.value){k=this.fireEvent(m.value+"Click",{type:m.value+"Click",target:this.get("element"),button:m});
if(k===false){e=false
}}if(m.menucmd&&e){k=this.fireEvent(m.menucmd+"Click",{type:m.menucmd+"Click",target:this.get("element"),button:m});
if(k===false){e=false
}}if(e){this.fireEvent("buttonClick",{type:"buttonClick",target:this.get("element"),button:m})
}if(m.type=="select"){var h=this.getButtonById(m.id);
if(h.buttonType=="rich"){var i=m.value;
for(var j=0;
j<m.menu.length;
j++){if(m.menu[j].value==m.value){i=m.menu[j].text;
break
}}h.set("label",'<span class="yui-toolbar-'+m.menucmd+"-"+(m.value).replace(/ /g,"-").toLowerCase()+'">'+i+"</span>");
var f=h.getMenu().getItems();
for(var l=0;
l<f.length;
l++){if(f[l].value.toLowerCase()==m.value.toLowerCase()){f[l].cfg.setProperty("checked",true)
}else{f[l].cfg.setProperty("checked",false)
}}}}if(g){a.stopEvent(g)
}}},_keyNav:null,_navCounter:null,_navigateButtons:function(e){switch(e.keyCode){case 37:case 39:if(e.keyCode==37){this._navCounter--
}else{this._navCounter++
}if(this._navCounter>(this._buttonList.length-1)){this._navCounter=0
}if(this._navCounter<0){this._navCounter=(this._buttonList.length-1)
}if(this._buttonList[this._navCounter]){var f=this._buttonList[this._navCounter].get("element");
if(this.browser.ie){f=this._buttonList[this._navCounter].get("element").getElementsByTagName("a")[0]
}if(this._buttonList[this._navCounter].get("disabled")){this._navigateButtons(e)
}else{f.focus()
}}break
}},_handleFocus:function(){if(!this._keyNav){var e="keypress";
if(this.browser.ie){e="keydown"
}a.on(this.get("element"),e,this._navigateButtons,this,true);
this._keyNav=true;
this._navCounter=-1
}},getButtonById:function(e){var g=this._buttonList.length;
for(var f=0;
f<g;
f++){if(this._buttonList[f]&&this._buttonList[f].get("id")==e){return this._buttonList[f]
}}return false
},getButtonByValue:function(e){var h=this.get("buttons");
var j=h.length;
for(var g=0;
g<j;
g++){if(h[g].group!==undefined){for(var k=0;
k<h[g].buttons.length;
k++){if((h[g].buttons[k].value==e)||(h[g].buttons[k].menucmd==e)){return this.getButtonById(h[g].buttons[k].id)
}if(h[g].buttons[k].menu){for(var f=0;
f<h[g].buttons[k].menu.length;
f++){if(h[g].buttons[k].menu[f].value==e){return this.getButtonById(h[g].buttons[k].id)
}}}}}else{if((h[g].value==e)||(h[g].menucmd==e)){return this.getButtonById(h[g].id)
}if(h[g].menu){for(var i=0;
i<h[g].menu.length;
i++){if(h[g].menu[i].value==e){return this.getButtonById(h[g].id)
}}}}}return false
},getButtonByIndex:function(e){if(this._buttonList[e]){return this._buttonList[e]
}else{return false
}},getButtons:function(){return this._buttonList
},disableButton:function(e){var f=d.call(this,e);
if(f){f.set("disabled",true)
}else{return false
}},enableButton:function(e){if(this.get("disabled")){return false
}var f=d.call(this,e);
if(f){if(f.get("disabled")){f.set("disabled",false)
}}else{return false
}},isSelected:function(e){var f=d.call(this,e);
if(f){return f._selected
}return false
},selectButton:function(e,g){var h=d.call(this,e);
if(h){h.addClass("yui-button-selected");
h.addClass("yui-button-"+h.get("value")+"-selected");
h._selected=true;
if(g){if(h.buttonType=="rich"){var f=h.getMenu().getItems();
for(var i=0;
i<f.length;
i++){if(f[i].value==g){f[i].cfg.setProperty("checked",true);
h.set("label",'<span class="yui-toolbar-'+h.get("value")+"-"+(g).replace(/ /g,"-").toLowerCase()+'">'+f[i]._oText.nodeValue+"</span>")
}else{f[i].cfg.setProperty("checked",false)
}}}}}else{return false
}},deselectButton:function(e){var f=d.call(this,e);
if(f){f.removeClass("yui-button-selected");
f.removeClass("yui-button-"+f.get("value")+"-selected");
f.removeClass("yui-button-hover");
f._selected=false
}else{return false
}},deselectAllButtons:function(){var f=this._buttonList.length;
for(var e=0;
e<f;
e++){this.deselectButton(this._buttonList[e])
}},disableAllButtons:function(){if(this.get("disabled")){return false
}var f=this._buttonList.length;
for(var e=0;
e<f;
e++){this.disableButton(this._buttonList[e])
}},enableAllButtons:function(){if(this.get("disabled")){return false
}var f=this._buttonList.length;
for(var e=0;
e<f;
e++){this.enableButton(this._buttonList[e])
}},resetAllButtons:function(e){if(!b.isObject(e)){e={}
}if(this.get("disabled")){return false
}var i=this._buttonList.length;
for(var h=0;
h<i;
h++){var f=this._buttonList[h];
if(f){var g=f._configs.disabled._initialConfig.value;
if(e[f.get("id")]){this.enableButton(f);
this.selectButton(f)
}else{if(g){this.disableButton(f)
}else{this.enableButton(f)
}this.deselectButton(f)
}}}},destroyButton:function(e){var g=d.call(this,e);
if(g){var f=g.get("id");
g.destroy();
var i=this._buttonList.length;
for(var h=0;
h<i;
h++){if(this._buttonList[h]&&this._buttonList[h].get("id")==f){this._buttonList[h]=null
}}}else{return false
}},destroy:function(){this.get("element").innerHTML="";
this.get("element").className="";
for(var e in this){if(b.hasOwnProperty(this,e)){this[e]=null
}}return true
},collapse:function(e){var f=c.getElementsByClassName("collapse","span",this._titlebar);
if(e===false){c.removeClass(this.get("cont").parentNode,"yui-toolbar-container-collapsed");
if(f[0]){c.removeClass(f[0],"collapsed")
}this.fireEvent("toolbarExpanded",{type:"toolbarExpanded",target:this})
}else{if(f[0]){c.addClass(f[0],"collapsed")
}c.addClass(this.get("cont").parentNode,"yui-toolbar-container-collapsed");
this.fireEvent("toolbarCollapsed",{type:"toolbarCollapsed",target:this})
}},toString:function(){return"Toolbar (#"+this.get("element").id+") with "+this._buttonList.length+" buttons."
}})
})();
(function(){var c=YAHOO.util.Dom,a=YAHOO.util.Event,b=YAHOO.lang,d=YAHOO.widget.Toolbar;
YAHOO.widget.SimpleEditor=function(j,e){var k={};
if(b.isObject(j)&&(!j.tagName)&&!e){b.augmentObject(k,j);
j=document.createElement("textarea");
this.DOMReady=true;
if(k.container){var g=c.get(k.container);
g.appendChild(j)
}else{document.body.appendChild(j)
}}else{if(e){b.augmentObject(k,e)
}}var i={element:null,attributes:k},l=null;
if(b.isString(j)){l=j
}else{if(i.attributes.id){l=i.attributes.id
}else{this.DOMReady=true;
l=c.generateId(j)
}}i.element=j;
var h=document.createElement("DIV");
i.attributes.element_cont=new YAHOO.util.Element(h,{id:l+"_container"});
var m=document.createElement("div");
c.addClass(m,"first-child");
i.attributes.element_cont.appendChild(m);
if(!i.attributes.toolbar_cont){i.attributes.toolbar_cont=document.createElement("DIV");
i.attributes.toolbar_cont.id=l+"_toolbar";
m.appendChild(i.attributes.toolbar_cont)
}var f=document.createElement("DIV");
m.appendChild(f);
i.attributes.editor_wrapper=f;
YAHOO.widget.SimpleEditor.superclass.constructor.call(this,i.element,i.attributes)
};
YAHOO.extend(YAHOO.widget.SimpleEditor,YAHOO.util.Element,{_resizeConfig:{handles:["br"],autoRatio:true,status:true,proxy:true,useShim:true,setSize:false},_setupResize:function(){if(!YAHOO.util.DD||!YAHOO.util.Resize){return false
}if(this.get("resize")){var e={};
b.augmentObject(e,this._resizeConfig);
this.resize=new YAHOO.util.Resize(this.get("element_cont").get("element"),e);
this.resize.on("resize",function(j){var f=this.get("animate");
this.set("animate",false);
this.set("width",j.width+"px");
var i=j.height,h=(this.toolbar.get("element").clientHeight+2),g=0;
if(this.dompath){g=(this.dompath.clientHeight+1)
}var k=(i-h-g);
this.set("height",k+"px");
this.get("element_cont").setStyle("height","");
this.set("animate",f)
},this,true)
}},resize:null,_setupDD:function(){if(!YAHOO.util.DD){return false
}if(this.get("drag")){var e=this.get("drag"),f=YAHOO.util.DD;
if(e==="proxy"){f=YAHOO.util.DDProxy
}this.dd=new f(this.get("element_cont").get("element"));
this.toolbar.addClass("draggable");
this.dd.setHandleElId(this.toolbar._titlebar)
}},dd:null,_lastCommand:null,_undoNodeChange:function(){},_storeUndo:function(){},_checkKey:function(i,f){var h=false;
if((f.keyCode===i.key)){if(i.mods&&(i.mods.length>0)){var e=0;
for(var g=0;
g<i.mods.length;
g++){if(this.browser.mac){if(i.mods[g]=="ctrl"){i.mods[g]="meta"
}}if(f[i.mods[g]+"Key"]===true){e++
}}if(e===i.mods.length){h=true
}}else{h=true
}}return h
},_keyMap:{SELECT_ALL:{key:65,mods:["ctrl"]},CLOSE_WINDOW:{key:87,mods:["shift","ctrl"]},FOCUS_TOOLBAR:{key:27,mods:["shift"]},FOCUS_AFTER:{key:27},CREATE_LINK:{key:76,mods:["shift","ctrl"]},BOLD:{key:66,mods:["shift","ctrl"]},ITALIC:{key:73,mods:["shift","ctrl"]},UNDERLINE:{key:85,mods:["shift","ctrl"]},UNDO:{key:90,mods:["ctrl"]},REDO:{key:90,mods:["shift","ctrl"]},JUSTIFY_LEFT:{key:219,mods:["shift","ctrl"]},JUSTIFY_CENTER:{key:220,mods:["shift","ctrl"]},JUSTIFY_RIGHT:{key:221,mods:["shift","ctrl"]}},_cleanClassName:function(e){return e.replace(/ /g,"-").toLowerCase()
},_textarea:null,_docType:'<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">',editorDirty:null,_defaultCSS:"html { height: 95%; } body { padding: 7px; background-color: #fff; font:13px/1.22 arial,helvetica,clean,sans-serif;*font-size:small;*font:x-small; } a, a:visited, a:hover { color: blue !important; text-decoration: underline !important; cursor: text !important; } .warning-localfile { border-bottom: 1px dashed red !important; } .yui-busy { cursor: wait !important; } img.selected { border: 2px dotted #808080; } img { cursor: pointer !important; border: none; } body.ptags.webkit div { margin: 11px 0; }",_defaultToolbar:null,_lastButton:null,_baseHREF:function(){var e=document.location.href;
if(e.indexOf("?")!==-1){e=e.substring(0,e.indexOf("?"))
}e=e.substring(0,e.lastIndexOf("/"))+"/";
return e
}(),_lastImage:null,_blankImageLoaded:null,_fixNodesTimer:null,_nodeChangeTimer:null,_lastNodeChangeEvent:null,_lastNodeChange:0,_rendered:null,DOMReady:null,_selection:null,_mask:null,_showingHiddenElements:null,currentWindow:null,currentEvent:null,operaEvent:null,currentFont:null,currentElement:null,dompath:null,beforeElement:null,afterElement:null,invalidHTML:{form:true,input:true,button:true,select:true,link:true,html:true,body:true,iframe:true,script:true,style:true,textarea:true},toolbar:null,_contentTimer:null,_contentTimerCounter:0,_disabled:["createlink","fontname","fontsize","forecolor","backcolor"],_alwaysDisabled:{undo:true,redo:true},_alwaysEnabled:{},_semantic:{bold:true,italic:true,underline:true},_tag2cmd:{b:"bold",strong:"bold",i:"italic",em:"italic",u:"underline",sup:"superscript",sub:"subscript",img:"insertimage",a:"createlink",ul:"insertunorderedlist",ol:"insertorderedlist"},_createIframe:function(){var e=document.createElement("iframe");
e.id=this.get("id")+"_editor";
var g={border:"0",frameBorder:"0",marginWidth:"0",marginHeight:"0",leftMargin:"0",topMargin:"0",allowTransparency:"true",width:"100%"};
if(this.get("autoHeight")){g.scrolling="no"
}for(var f in g){if(b.hasOwnProperty(g,f)){e.setAttribute(f,g[f])
}}var h="javascript:;";
if(this.browser.ie){h="javascript:false;"
}e.setAttribute("src",h);
var i=new YAHOO.util.Element(e);
i.setStyle("visibility","hidden");
return i
},_isElement:function(e,f){if(e&&e.tagName&&(e.tagName.toLowerCase()==f)){return true
}if(e&&e.getAttribute&&(e.getAttribute("tag")==f)){return true
}return false
},_hasParent:function(e,f){if(!e||!e.parentNode){return false
}while(e.parentNode){if(this._isElement(e,f)){return e
}if(e.parentNode){e=e.parentNode
}else{return false
}}return false
},_getDoc:function(){var f=false;
if(this.get){if(this.get("iframe")){if(this.get("iframe").get){if(this.get("iframe").get("element")){try{if(this.get("iframe").get("element").contentWindow){if(this.get("iframe").get("element").contentWindow.document){f=this.get("iframe").get("element").contentWindow.document;
return f
}}}catch(e){}}}}}return false
},_getWindow:function(){return this.get("iframe").get("element").contentWindow
},_focusWindow:function(e){if(this.browser.webkit){if(e){this._getSelection().setBaseAndExtent(this._getDoc().body.firstChild,0,this._getDoc().body.firstChild,1);
if(this.browser.webkit3){this._getSelection().collapseToStart()
}else{this._getSelection().collapse(false)
}}else{this._getSelection().setBaseAndExtent(this._getDoc().body,1,this._getDoc().body,1);
if(this.browser.webkit3){this._getSelection().collapseToStart()
}else{this._getSelection().collapse(false)
}}this._getWindow().focus()
}else{this._getWindow().focus()
}},_hasSelection:function(){var e=this._getSelection();
var g=this._getRange();
var f=false;
if(!e||!g){return f
}if(this.browser.ie||this.browser.opera){if(g.text){f=true
}if(g.html){f=true
}}else{if(this.browser.webkit){if(e+""!==""){f=true
}}else{if(e&&(e.toString()!=="")&&(e!==undefined)){f=true
}}}return f
},_getSelection:function(){var e=null;
if(this._getDoc()&&this._getWindow()){if(this._getDoc().selection){e=this._getDoc().selection
}else{e=this._getWindow().getSelection()
}if(this.browser.webkit){if(e.baseNode){this._selection={};
this._selection.baseNode=e.baseNode;
this._selection.baseOffset=e.baseOffset;
this._selection.extentNode=e.extentNode;
this._selection.extentOffset=e.extentOffset
}else{if(this._selection!==null){e=this._getWindow().getSelection();
e.setBaseAndExtent(this._selection.baseNode,this._selection.baseOffset,this._selection.extentNode,this._selection.extentOffset);
this._selection=null
}}}}return e
},_selectNode:function(h,e){if(!h){return false
}var g=this._getSelection(),i=null;
if(this.browser.ie){try{i=this._getDoc().body.createTextRange();
i.moveToElementText(h);
i.select()
}catch(f){}}else{if(this.browser.webkit){if(e){g.setBaseAndExtent(h,1,h,h.innerText.length)
}else{g.setBaseAndExtent(h,0,h,h.innerText.length)
}}else{if(this.browser.opera){g=this._getWindow().getSelection();
i=this._getDoc().createRange();
i.selectNode(h);
g.removeAllRanges();
g.addRange(i)
}else{i=this._getDoc().createRange();
i.selectNodeContents(h);
g.removeAllRanges();
g.addRange(i)
}}}this.nodeChange()
},_getRange:function(){var h=this._getSelection();
if(h===null){return null
}if(this.browser.webkit&&!h.getRangeAt){var e=this._getDoc().createRange();
try{e.setStart(h.anchorNode,h.anchorOffset);
e.setEnd(h.focusNode,h.focusOffset)
}catch(f){e=this._getWindow().getSelection()+""
}return e
}if(this.browser.ie||this.browser.opera){try{return h.createRange()
}catch(g){return null
}}if(h.rangeCount>0){return h.getRangeAt(0)
}return null
},_setDesignMode:function(g){try{var e=true;
if(this.browser.ie&&(g.toLowerCase()=="off")){e=false
}if(e){this._getDoc().designMode=g
}}catch(f){}},_toggleDesignMode:function(){var e=this._getDoc().designMode.toLowerCase(),f="on";
if(e=="on"){f="off"
}this._setDesignMode(f);
return f
},_initEditorEvents:function(){var e=this._getDoc();
a.on(e,"mouseup",this._handleMouseUp,this,true);
a.on(e,"mousedown",this._handleMouseDown,this,true);
a.on(e,"click",this._handleClick,this,true);
a.on(e,"dblclick",this._handleDoubleClick,this,true);
a.on(e,"keypress",this._handleKeyPress,this,true);
a.on(e,"keyup",this._handleKeyUp,this,true);
a.on(e,"keydown",this._handleKeyDown,this,true)
},_removeEditorEvents:function(){var e=this._getDoc();
a.removeListener(e,"mouseup",this._handleMouseUp,this,true);
a.removeListener(e,"mousedown",this._handleMouseDown,this,true);
a.removeListener(e,"click",this._handleClick,this,true);
a.removeListener(e,"dblclick",this._handleDoubleClick,this,true);
a.removeListener(e,"keypress",this._handleKeyPress,this,true);
a.removeListener(e,"keyup",this._handleKeyUp,this,true);
a.removeListener(e,"keydown",this._handleKeyDown,this,true)
},_initEditor:function(){if(this.browser.ie){this._getDoc().body.style.margin="0"
}if(!this.get("disabled")){if(this._getDoc().designMode.toLowerCase()!="on"){this._setDesignMode("on");
this._contentTimerCounter=0
}}if(!this._getDoc().body){this._contentTimerCounter=0;
this._checkLoaded();
return false
}this.toolbar.on("buttonClick",this._handleToolbarClick,this,true);
if(!this.get("disabled")){this._initEditorEvents();
this.toolbar.set("disabled",false)
}this.fireEvent("editorContentLoaded",{type:"editorLoaded",target:this});
if(this.get("dompath")){var g=this;
setTimeout(function(){g._writeDomPath.call(g);
g._setupResize.call(g)
},150)
}var e=[];
for(var f in this.browser){if(this.browser[f]){e.push(f)
}}if(this.get("ptags")){e.push("ptags")
}c.addClass(this._getDoc().body,e.join(" "));
this.nodeChange(true)
},_checkLoaded:function(){this._contentTimerCounter++;
if(this._contentTimer){clearTimeout(this._contentTimer)
}if(this._contentTimerCounter>500){return false
}var e=false;
try{if(this._getDoc()&&this._getDoc().body){if(this.browser.ie){if(this._getDoc().body.readyState=="complete"){e=true
}}else{if(this._getDoc().body._rteLoaded===true){e=true
}}}}catch(f){e=false
}if(e===true){this._initEditor()
}else{var g=this;
this._contentTimer=setTimeout(function(){g._checkLoaded.call(g)
},20)
}},_setInitialContent:function(){var h=((this._textarea)?this.get("element").value:this.get("element").innerHTML),f=null;
var j=b.substitute(this.get("html"),{TITLE:this.STR_TITLE,CONTENT:this._cleanIncomingHTML(h),CSS:this.get("css"),HIDDEN_CSS:((this.get("hiddencss"))?this.get("hiddencss"):"/* No Hidden CSS */"),EXTRA_CSS:((this.get("extracss"))?this.get("extracss"):"/* No Extra CSS */")}),k=true;
if(document.compatMode!="BackCompat"){j=this._docType+"\n"+j
}else{}if(this.browser.ie||this.browser.webkit||this.browser.opera||(navigator.userAgent.indexOf("Firefox/1.5")!=-1)){try{if(this.browser.air){f=this._getDoc().implementation.createHTMLDocument();
var e=this._getDoc();
e.open();
e.close();
f.open();
f.write(j);
f.close();
var i=e.importNode(f.getElementsByTagName("html")[0],true);
e.replaceChild(i,e.getElementsByTagName("html")[0]);
e.body._rteLoaded=true
}else{f=this._getDoc();
f.open();
f.write(j);
f.close()
}}catch(g){k=false
}}else{this.get("iframe").get("element").src="data:text/html;charset=utf-8,"+encodeURIComponent(j)
}this.get("iframe").setStyle("visibility","");
if(k){this._checkLoaded()
}},_setMarkupType:function(e){switch(this.get("markup")){case"css":this._setEditorStyle(true);
break;
case"default":this._setEditorStyle(false);
break;
case"semantic":case"xhtml":if(this._semantic[e]){this._setEditorStyle(false)
}else{this._setEditorStyle(true)
}break
}},_setEditorStyle:function(e){try{this._getDoc().execCommand("useCSS",false,!e)
}catch(f){}},_getSelectedElement:function(){var f=this._getDoc(),i=null,h=null,e=null,j=true;
if(this.browser.ie){this.currentEvent=this._getWindow().event;
i=this._getRange();
if(i){e=i.item?i.item(0):i.parentElement();
if(this._hasSelection()){}if(e===f.body){e=null
}}if((this.currentEvent!==null)&&(this.currentEvent.keyCode===0)){e=a.getTarget(this.currentEvent)
}}else{h=this._getSelection();
i=this._getRange();
if(!h||!i){return null
}if(!this._hasSelection()&&this.browser.webkit3){}if(this.browser.gecko){if(i.startContainer){j=false;
if(i.startContainer.nodeType===3){e=i.startContainer.parentNode
}else{if(i.startContainer.nodeType===1){e=i.startContainer
}else{j=true
}}if(!j){this.currentEvent=null
}}}if(j){if(h.anchorNode&&(h.anchorNode.nodeType==3)){if(h.anchorNode.parentNode){e=h.anchorNode.parentNode
}if(h.anchorNode.nextSibling!=h.focusNode.nextSibling){e=h.anchorNode.nextSibling
}}if(this._isElement(e,"br")){e=null
}if(!e){e=i.commonAncestorContainer;
if(!i.collapsed){if(i.startContainer==i.endContainer){if(i.startOffset-i.endOffset<2){if(i.startContainer.hasChildNodes()){e=i.startContainer.childNodes[i.startOffset]
}}}}}}}if(this.currentEvent!==null){try{switch(this.currentEvent.type){case"click":case"mousedown":case"mouseup":if(this.browser.webkit){e=a.getTarget(this.currentEvent)
}break;
default:break
}}catch(g){}}else{if((this.currentElement&&this.currentElement[0])&&(!this.browser.ie)){}}if(this.browser.opera||this.browser.webkit){if(this.currentEvent&&!e){e=YAHOO.util.Event.getTarget(this.currentEvent)
}}if(!e||!e.tagName){e=f.body
}if(this._isElement(e,"html")){e=f.body
}if(this._isElement(e,"body")){e=f.body
}if(e&&!e.parentNode){e=f.body
}if(e===undefined){e=null
}return e
},_getDomPath:function(f){if(!f){f=this._getSelectedElement()
}var e=[];
while(f!==null){if(f.ownerDocument!=this._getDoc()){f=null;
break
}if(f.nodeName&&f.nodeType&&(f.nodeType==1)){e[e.length]=f
}if(this._isElement(f,"body")){break
}f=f.parentNode
}if(e.length===0){if(this._getDoc()&&this._getDoc().body){e[0]=this._getDoc().body
}}return e.reverse()
},_writeDomPath:function(){var g=this._getDomPath(),i=[],k="",f="";
for(var m=0;
m<g.length;
m++){var e=g[m].tagName.toLowerCase();
if((e=="ol")&&(g[m].type)){e+=":"+g[m].type
}if(c.hasClass(g[m],"yui-tag")){e=g[m].getAttribute("tag")
}if((this.get("markup")=="semantic")||(this.get("markup")=="xhtml")){switch(e){case"b":e="strong";
break;
case"i":e="em";
break
}}if(!c.hasClass(g[m],"yui-non")){if(c.hasClass(g[m],"yui-tag")){f=e
}else{k=((g[m].className!=="")?"."+g[m].className.replace(/ /g,"."):"");
if((k.indexOf("yui")!=-1)||(k.toLowerCase().indexOf("apple-style-span")!=-1)){k=""
}f=e+((g[m].id)?"#"+g[m].id:"")+k
}switch(e){case"body":f="body";
break;
case"a":if(g[m].getAttribute("href",2)){f+=":"+g[m].getAttribute("href",2).replace("mailto:","").replace("http://","").replace("https://","")
}break;
case"img":var l=g[m].height;
var h=g[m].width;
if(g[m].style.height){l=parseInt(g[m].style.height,10)
}if(g[m].style.width){h=parseInt(g[m].style.width,10)
}f+="("+h+"x"+l+")";
break
}if(f.length>10){f='<span title="'+f+'">'+f.substring(0,10)+"...</span>"
}else{f='<span title="'+f+'">'+f+"</span>"
}i[i.length]=f
}}var j=i.join(" "+this.SEP_DOMPATH+" ");
if(this.dompath.innerHTML!=j){this.dompath.innerHTML=j
}},_fixNodes:function(){var f=this._getDoc(),h=[];
for(var k in this.invalidHTML){if(YAHOO.lang.hasOwnProperty(this.invalidHTML,k)){if(k.toLowerCase()!="span"){var j=f.body.getElementsByTagName(k);
if(j.length){for(var i=0;
i<j.length;
i++){h.push(j[i])
}}}}}for(var g=0;
g<h.length;
g++){if(h[g].parentNode){if(b.isObject(this.invalidHTML[h[g].tagName.toLowerCase()])&&this.invalidHTML[h[g].tagName.toLowerCase()].keepContents){this._swapEl(h[g],"span",function(l){l.className="yui-non"
})
}else{h[g].parentNode.removeChild(h[g])
}}}var e=this._getDoc().getElementsByTagName("img");
c.addClass(e,"yui-img")
},_isNonEditable:function(h){if(this.get("allowNoEdit")){var i=a.getTarget(h);
if(this._isElement(i,"html")){i=null
}var e=this._getDomPath(i);
for(var j=(e.length-1);
j>-1;
j--){if(c.hasClass(e[j],this.CLASS_NOEDIT)){try{this._getDoc().execCommand("enableObjectResizing",false,"false")
}catch(f){}this.nodeChange();
a.stopEvent(h);
return true
}}try{this._getDoc().execCommand("enableObjectResizing",false,"true")
}catch(g){}}return false
},_setCurrentEvent:function(e){this.currentEvent=e
},_handleClick:function(e){var f=this.fireEvent("beforeEditorClick",{type:"beforeEditorClick",target:this,ev:e});
if(f===false){return false
}if(this._isNonEditable(e)){return false
}this._setCurrentEvent(e);
if(this.currentWindow){this.closeWindow()
}if(this.currentWindow){this.closeWindow()
}if(this.browser.webkit){var g=a.getTarget(e);
if(this._isElement(g,"a")||this._isElement(g.parentNode,"a")){a.stopEvent(e);
this.nodeChange()
}}else{this.nodeChange()
}this.fireEvent("editorClick",{type:"editorClick",target:this,ev:e})
},_handleMouseUp:function(f){var g=this.fireEvent("beforeEditorMouseUp",{type:"beforeEditorMouseUp",target:this,ev:f});
if(g===false){return false
}if(this._isNonEditable(f)){return false
}var h=this;
if(this.browser.opera){var e=a.getTarget(f);
if(this._isElement(e,"img")){this.nodeChange();
if(this.operaEvent){clearTimeout(this.operaEvent);
this.operaEvent=null;
this._handleDoubleClick(f)
}else{this.operaEvent=window.setTimeout(function(){h.operaEvent=false
},700)
}}}if(this.browser.webkit||this.browser.opera){if(this.browser.webkit){a.stopEvent(f)
}}this.nodeChange();
this.fireEvent("editorMouseUp",{type:"editorMouseUp",target:this,ev:f})
},_handleMouseDown:function(g){var h=this.fireEvent("beforeEditorMouseDown",{type:"beforeEditorMouseDown",target:this,ev:g});
if(h===false){return false
}if(this._isNonEditable(g)){return false
}this._setCurrentEvent(g);
var f=a.getTarget(g);
if(this.browser.webkit&&this._hasSelection()){var e=this._getSelection();
if(!this.browser.webkit3){e.collapse(true)
}else{e.collapseToStart()
}}if(this.browser.webkit&&this._lastImage){c.removeClass(this._lastImage,"selected");
this._lastImage=null
}if(this._isElement(f,"img")||this._isElement(f,"a")){if(this.browser.webkit){a.stopEvent(g);
if(this._isElement(f,"img")){c.addClass(f,"selected");
this._lastImage=f
}}if(this.currentWindow){this.closeWindow()
}this.nodeChange()
}this.fireEvent("editorMouseDown",{type:"editorMouseDown",target:this,ev:g})
},_handleDoubleClick:function(f){var g=this.fireEvent("beforeEditorDoubleClick",{type:"beforeEditorDoubleClick",target:this,ev:f});
if(g===false){return false
}if(this._isNonEditable(f)){return false
}this._setCurrentEvent(f);
var e=a.getTarget(f);
if(this._isElement(e,"img")){this.currentElement[0]=e;
this.toolbar.fireEvent("insertimageClick",{type:"insertimageClick",target:this.toolbar});
this.fireEvent("afterExecCommand",{type:"afterExecCommand",target:this})
}else{if(this._hasParent(e,"a")){this.currentElement[0]=this._hasParent(e,"a");
this.toolbar.fireEvent("createlinkClick",{type:"createlinkClick",target:this.toolbar});
this.fireEvent("afterExecCommand",{type:"afterExecCommand",target:this})
}}this.nodeChange();
this.fireEvent("editorDoubleClick",{type:"editorDoubleClick",target:this,ev:f})
},_handleKeyUp:function(e){var f=this.fireEvent("beforeEditorKeyUp",{type:"beforeEditorKeyUp",target:this,ev:e});
if(f===false){return false
}if(this._isNonEditable(e)){return false
}this._setCurrentEvent(e);
switch(e.keyCode){case this._keyMap.SELECT_ALL.key:if(this._checkKey(this._keyMap.SELECT_ALL,e)){this.nodeChange()
}break;
case 32:case 35:case 36:case 37:case 38:case 39:case 40:case 46:case 8:case this._keyMap.CLOSE_WINDOW.key:if((e.keyCode==this._keyMap.CLOSE_WINDOW.key)&&this.currentWindow){if(this._checkKey(this._keyMap.CLOSE_WINDOW,e)){this.closeWindow()
}}else{if(!this.browser.ie){if(this._nodeChangeTimer){clearTimeout(this._nodeChangeTimer)
}var g=this;
this._nodeChangeTimer=setTimeout(function(){g._nodeChangeTimer=null;
g.nodeChange.call(g)
},100)
}else{this.nodeChange()
}this.editorDirty=true
}break
}this.fireEvent("editorKeyUp",{type:"editorKeyUp",target:this,ev:e});
this._storeUndo()
},_handleKeyPress:function(e){var f=this.fireEvent("beforeEditorKeyPress",{type:"beforeEditorKeyPress",target:this,ev:e});
if(f===false){return false
}if(this.get("allowNoEdit")){if(e&&e.keyCode&&(e.keyCode==63272)){a.stopEvent(e)
}}if(this._isNonEditable(e)){return false
}this._setCurrentEvent(e);
if(this.browser.opera){if(e.keyCode===13){var g=this._getSelectedElement();
if(!this._isElement(g,"li")){this.execCommand("inserthtml","<br>");
a.stopEvent(e)
}}}if(this.browser.webkit){if(!this.browser.webkit3){if(e.keyCode&&(e.keyCode==122)&&(e.metaKey)){if(this._hasParent(this._getSelectedElement(),"li")){a.stopEvent(e)
}}}this._listFix(e)
}this.fireEvent("editorKeyPress",{type:"editorKeyPress",target:this,ev:e})
},_handleKeyDown:function(e){var h=this.fireEvent("beforeEditorKeyDown",{type:"beforeEditorKeyDown",target:this,ev:e});
if(h===false){return false
}var i=null,g=null;
if(this._isNonEditable(e)){return false
}this._setCurrentEvent(e);
if(this.currentWindow){this.closeWindow()
}if(this.currentWindow){this.closeWindow()
}var f=false,k=null,l=false;
switch(e.keyCode){case this._keyMap.FOCUS_TOOLBAR.key:if(this._checkKey(this._keyMap.FOCUS_TOOLBAR,e)){var j=this.toolbar.getElementsByTagName("h2")[0];
if(j&&j.firstChild){j.firstChild.focus()
}}else{if(this._checkKey(this._keyMap.FOCUS_AFTER,e)){this.afterElement.focus()
}}a.stopEvent(e);
f=false;
break;
case this._keyMap.CREATE_LINK.key:if(this._hasSelection()){if(this._checkKey(this._keyMap.CREATE_LINK,e)){var m=true;
if(this.get("limitCommands")){if(!this.toolbar.getButtonByValue("createlink")){m=false
}}if(m){this.execCommand("createlink","");
this.toolbar.fireEvent("createlinkClick",{type:"createlinkClick",target:this.toolbar});
this.fireEvent("afterExecCommand",{type:"afterExecCommand",target:this});
f=false
}}}break;
case this._keyMap.UNDO.key:case this._keyMap.REDO.key:if(this._checkKey(this._keyMap.REDO,e)){k="redo";
f=true
}else{if(this._checkKey(this._keyMap.UNDO,e)){k="undo";
f=true
}}break;
case this._keyMap.BOLD.key:if(this._checkKey(this._keyMap.BOLD,e)){k="bold";
f=true
}break;
case this._keyMap.ITALIC.key:if(this._checkKey(this._keyMap.ITALIC,e)){k="italic";
f=true
}break;
case this._keyMap.UNDERLINE.key:if(this._checkKey(this._keyMap.UNDERLINE,e)){k="underline";
f=true
}break;
case 9:if(this.browser.ie){g=this._getRange();
i=this._getSelectedElement();
if(!this._isElement(i,"li")){if(g){g.pasteHTML("&nbsp;&nbsp;&nbsp;&nbsp;");
g.collapse(false);
g.select()
}a.stopEvent(e)
}}if(this.browser.gecko>1.8){i=this._getSelectedElement();
if(this._isElement(i,"li")){if(e.shiftKey){this._getDoc().execCommand("outdent",null,"")
}else{this._getDoc().execCommand("indent",null,"")
}}else{if(!this._hasSelection()){this.execCommand("inserthtml","&nbsp;&nbsp;&nbsp;&nbsp;")
}}a.stopEvent(e)
}break;
case 13:if(this.get("ptags")&&!e.shiftKey){if(this.browser.gecko){i=this._getSelectedElement();
if(!this._isElement(i,"li")){f=true;
k="insertparagraph";
a.stopEvent(e)
}}if(this.browser.webkit){i=this._getSelectedElement();
if(!this._hasParent(i,"li")){f=true;
k="insertparagraph";
a.stopEvent(e)
}}}else{if(this.browser.ie){g=this._getRange();
i=this._getSelectedElement();
if(!this._isElement(i,"li")){if(g){g.pasteHTML("<br>");
g.collapse(false);
g.select()
}a.stopEvent(e)
}}}break
}if(this.browser.ie){this._listFix(e)
}if(f&&k){this.execCommand(k,null);
a.stopEvent(e);
this.nodeChange()
}this.fireEvent("editorKeyDown",{type:"editorKeyDown",target:this,ev:e})
},_listFix:function(g){var e=null,i=null,m=false,k=null;
if(this.browser.webkit){if(g.keyCode&&(g.keyCode==13)){if(this._hasParent(this._getSelectedElement(),"li")){var j=this._hasParent(this._getSelectedElement(),"li");
if(j.previousSibling){if(j.firstChild&&(j.firstChild.length==1)){this._selectNode(j)
}}}}}if(g.keyCode&&((!this.browser.webkit3&&(g.keyCode==25))||((this.browser.webkit3||!this.browser.webkit)&&((g.keyCode==9)&&g.shiftKey)))){e=this._getSelectedElement();
if(this._hasParent(e,"li")){e=this._hasParent(e,"li");
if(this._hasParent(e,"ul")||this._hasParent(e,"ol")){i=this._hasParent(e,"ul");
if(!i){i=this._hasParent(e,"ol")
}if(this._isElement(i.previousSibling,"li")){i.removeChild(e);
i.parentNode.insertBefore(e,i.nextSibling);
if(this.browser.ie){k=this._getDoc().body.createTextRange();
k.moveToElementText(e);
k.collapse(false);
k.select()
}if(this.browser.webkit){this._selectNode(e.firstChild)
}a.stopEvent(g)
}}}}if(g.keyCode&&((g.keyCode==9)&&(!g.shiftKey))){var l=this._getSelectedElement();
if(this._hasParent(l,"li")){m=this._hasParent(l,"li").innerHTML
}if(this.browser.webkit){this._getDoc().execCommand("inserttext",false,"\t")
}e=this._getSelectedElement();
if(this._hasParent(e,"li")){i=this._hasParent(e,"li");
var h=this._getDoc().createElement(i.parentNode.tagName.toLowerCase());
if(this.browser.webkit){var f=c.getElementsByClassName("Apple-tab-span","span",i);
if(f[0]){i.removeChild(f[0]);
i.innerHTML=b.trim(i.innerHTML);
if(m){i.innerHTML='<span class="yui-non">'+m+"</span>&nbsp;"
}else{i.innerHTML='<span class="yui-non">&nbsp;</span>&nbsp;'
}}}else{if(m){i.innerHTML=m+"&nbsp;"
}else{i.innerHTML="&nbsp;"
}}i.parentNode.replaceChild(h,i);
h.appendChild(i);
if(this.browser.webkit){this._getSelection().setBaseAndExtent(i.firstChild,1,i.firstChild,i.firstChild.innerText.length);
if(!this.browser.webkit3){i.parentNode.parentNode.style.display="list-item";
setTimeout(function(){i.parentNode.parentNode.style.display="block"
},1)
}}else{if(this.browser.ie){k=this._getDoc().body.createTextRange();
k.moveToElementText(i);
k.collapse(false);
k.select()
}else{this._selectNode(i)
}}a.stopEvent(g)
}if(this.browser.webkit){a.stopEvent(g)
}this.nodeChange()
}},nodeChange:function(f){var e=this;
this._storeUndo();
if(this.get("nodeChangeDelay")){window.setTimeout(function(){e._nodeChange.apply(e,arguments)
},0)
}else{this._nodeChange()
}},_nodeChange:function(y){var w=parseInt(this.get("nodeChangeThreshold"),10),p=Math.round(new Date().getTime()/1000),m=this;
if(y===true){this._lastNodeChange=0
}if((this._lastNodeChange+w)<p){if(this._fixNodesTimer===null){this._fixNodesTimer=window.setTimeout(function(){m._fixNodes.call(m);
m._fixNodesTimer=null
},0)
}}this._lastNodeChange=p;
if(this.currentEvent){try{this._lastNodeChangeEvent=this.currentEvent.type
}catch(C){}}var e=this.fireEvent("beforeNodeChange",{type:"beforeNodeChange",target:this});
if(e===false){return false
}if(this.get("dompath")){window.setTimeout(function(){m._writeDomPath.call(m)
},0)
}if(!this.get("disabled")){if(this.STOP_NODE_CHANGE){this.STOP_NODE_CHANGE=false;
return false
}else{var k=this._getSelection(),n=this._getRange(),z=this._getSelectedElement(),r=this.toolbar.getButtonByValue("fontname"),s=this.toolbar.getButtonByValue("fontsize"),u=this.toolbar.getButtonByValue("undo"),x=this.toolbar.getButtonByValue("redo");
var q={};
if(this._lastButton){q[this._lastButton.id]=true
}if(!this._isElement(z,"body")){if(r){q[r.get("id")]=true
}if(s){q[s.get("id")]=true
}}if(x){delete q[x.get("id")]
}this.toolbar.resetAllButtons(q);
for(var B=0;
B<this._disabled.length;
B++){var o=this.toolbar.getButtonByValue(this._disabled[B]);
if(o&&o.get){if(this._lastButton&&(o.get("id")===this._lastButton.id)){}else{if(!this._hasSelection()&&!this.get("insert")){switch(this._disabled[B]){case"fontname":case"fontsize":break;
default:this.toolbar.disableButton(o)
}}else{if(!this._alwaysDisabled[this._disabled[B]]){this.toolbar.enableButton(o)
}}if(!this._alwaysEnabled[this._disabled[B]]){this.toolbar.deselectButton(o)
}}}}var l=this._getDomPath();
var A=null,h=null;
for(var g=0;
g<l.length;
g++){A=l[g].tagName.toLowerCase();
if(l[g].getAttribute("tag")){A=l[g].getAttribute("tag").toLowerCase()
}h=this._tag2cmd[A];
if(h===undefined){h=[]
}if(!b.isArray(h)){h=[h]
}if(l[g].style.fontWeight.toLowerCase()=="bold"){h[h.length]="bold"
}if(l[g].style.fontStyle.toLowerCase()=="italic"){h[h.length]="italic"
}if(l[g].style.textDecoration.toLowerCase()=="underline"){h[h.length]="underline"
}if(l[g].style.textDecoration.toLowerCase()=="line-through"){h[h.length]="strikethrough"
}if(h.length>0){for(var i=0;
i<h.length;
i++){this.toolbar.selectButton(h[i]);
this.toolbar.enableButton(h[i])
}}switch(l[g].style.textAlign.toLowerCase()){case"left":case"right":case"center":case"justify":var j=l[g].style.textAlign.toLowerCase();
if(l[g].style.textAlign.toLowerCase()=="justify"){j="full"
}this.toolbar.selectButton("justify"+j);
this.toolbar.enableButton("justify"+j);
break
}}if(r){var f=r._configs.label._initialConfig.value;
r.set("label",'<span class="yui-toolbar-fontname-'+this._cleanClassName(f)+'">'+f+"</span>");
this._updateMenuChecked("fontname",f)
}if(s){s.set("label",s._configs.label._initialConfig.value)
}var t=this.toolbar.getButtonByValue("heading");
if(t){t.set("label",t._configs.label._initialConfig.value);
this._updateMenuChecked("heading","none")
}var v=this.toolbar.getButtonByValue("insertimage");
if(v&&this.currentWindow&&(this.currentWindow.name=="insertimage")){this.toolbar.disableButton(v)
}if(this._lastButton&&this._lastButton.isSelected){this.toolbar.deselectButton(this._lastButton.id)
}this._undoNodeChange()
}}this.fireEvent("afterNodeChange",{type:"afterNodeChange",target:this})
},_updateMenuChecked:function(h,g,e){if(!e){e=this.toolbar
}var f=e.getButtonByValue(h);
f.checkValue(g)
},_handleToolbarClick:function(h){var f="";
var e="";
var g=h.button.value;
if(h.button.menucmd){f=g;
g=h.button.menucmd
}this._lastButton=h.button;
if(this.STOP_EXEC_COMMAND){this.STOP_EXEC_COMMAND=false;
return false
}else{this.execCommand(g,f);
if(!this.browser.webkit){var i=this;
setTimeout(function(){i._focusWindow.call(i)
},5)
}}a.stopEvent(h)
},_setupAfterElement:function(){if(!this.beforeElement){this.beforeElement=document.createElement("h2");
this.beforeElement.className="yui-editor-skipheader";
this.beforeElement.tabIndex="-1";
this.beforeElement.innerHTML=this.STR_BEFORE_EDITOR;
this.get("element_cont").get("firstChild").insertBefore(this.beforeElement,this.toolbar.get("nextSibling"))
}if(!this.afterElement){this.afterElement=document.createElement("h2");
this.afterElement.className="yui-editor-skipheader";
this.afterElement.tabIndex="-1";
this.afterElement.innerHTML=this.STR_LEAVE_EDITOR;
this.get("element_cont").get("firstChild").appendChild(this.afterElement)
}},_disableEditor:function(e){if(e){this._removeEditorEvents();
if(!this._mask){if(!!this.browser.ie){this._setDesignMode("off")
}if(this.toolbar){this.toolbar.set("disabled",true)
}this._mask=document.createElement("DIV");
c.setStyle(this._mask,"height","100%");
c.setStyle(this._mask,"width","100%");
c.setStyle(this._mask,"position","absolute");
c.setStyle(this._mask,"top","0");
c.setStyle(this._mask,"left","0");
c.setStyle(this._mask,"opacity",".5");
c.addClass(this._mask,"yui-editor-masked");
this.get("iframe").get("parentNode").appendChild(this._mask)
}}else{this._initEditorEvents();
if(this._mask){this._mask.parentNode.removeChild(this._mask);
this._mask=null;
if(this.toolbar){this.toolbar.set("disabled",false)
}this._setDesignMode("on");
this._focusWindow();
var f=this;
window.setTimeout(function(){f.nodeChange.call(f)
},100)
}}},SEP_DOMPATH:"<",STR_LEAVE_EDITOR:"You have left the Rich Text Editor.",STR_BEFORE_EDITOR:"This text field can contain stylized text and graphics. To cycle through all formatting options, use the keyboard shortcut Shift + Escape to place focus on the toolbar and navigate between options with your arrow keys. To exit this text editor use the Escape key and continue tabbing. <h4>Common formatting keyboard shortcuts:</h4><ul><li>Control Shift B sets text to bold</li> <li>Control Shift I sets text to italic</li> <li>Control Shift U underlines text</li> <li>Control Shift L adds an HTML link</li></ul>",STR_TITLE:"Rich Text Area.",STR_IMAGE_HERE:"Image URL Here",STR_LINK_URL:"Link URL",STOP_EXEC_COMMAND:false,STOP_NODE_CHANGE:false,CLASS_NOEDIT:"yui-noedit",CLASS_CONTAINER:"yui-editor-container",CLASS_EDITABLE:"yui-editor-editable",CLASS_EDITABLE_CONT:"yui-editor-editable-container",CLASS_PREFIX:"yui-editor",browser:function(){var e=YAHOO.env.ua;
if(e.webkit>=420){e.webkit3=e.webkit
}else{e.webkit3=0
}e.mac=false;
if(navigator.userAgent.indexOf("Macintosh")!==-1){e.mac=true
}return e
}(),init:function(e,f){if(!this._defaultToolbar){this._defaultToolbar={collapse:true,titlebar:"Text Editing Tools",draggable:false,buttons:[{group:"fontstyle",label:"Font Name and Size",buttons:[{type:"select",label:"Arial",value:"fontname",disabled:true,menu:[{text:"Arial",checked:true},{text:"Arial Black"},{text:"Comic Sans MS"},{text:"Courier New"},{text:"Lucida Console"},{text:"Tahoma"},{text:"Times New Roman"},{text:"Trebuchet MS"},{text:"Verdana"}]},{type:"spin",label:"13",value:"fontsize",range:[9,75],disabled:true}]},{type:"separator"},{group:"textstyle",label:"Font Style",buttons:[{type:"push",label:"Bold CTRL + SHIFT + B",value:"bold"},{type:"push",label:"Italic CTRL + SHIFT + I",value:"italic"},{type:"push",label:"Underline CTRL + SHIFT + U",value:"underline"},{type:"push",label:"Strike Through",value:"strikethrough"},{type:"separator"},{type:"color",label:"Font Color",value:"forecolor",disabled:true},{type:"color",label:"Background Color",value:"backcolor",disabled:true}]},{type:"separator"},{group:"indentlist",label:"Lists",buttons:[{type:"push",label:"Create an Unordered List",value:"insertunorderedlist"},{type:"push",label:"Create an Ordered List",value:"insertorderedlist"}]},{type:"separator"},{group:"insertitem",label:"Insert Item",buttons:[{type:"push",label:"HTML Link CTRL + SHIFT + L",value:"createlink",disabled:true},{type:"push",label:"Insert Image",value:"insertimage"}]}]}
}YAHOO.widget.SimpleEditor.superclass.init.call(this,e,f);
YAHOO.widget.EditorInfo._instances[this.get("id")]=this;
this.currentElement=[];
this.on("contentReady",function(){this.DOMReady=true;
this.fireQueue()
},this,true)
},initAttributes:function(f){YAHOO.widget.SimpleEditor.superclass.initAttributes.call(this,f);
var e=this;
this.setAttributeConfig("nodeChangeDelay",{value:((f.nodeChangeDelay===false)?false:true)});
this.setAttributeConfig("maxUndo",{writeOnce:true,value:f.maxUndo||30});
this.setAttributeConfig("ptags",{writeOnce:true,value:f.ptags||false});
this.setAttributeConfig("insert",{writeOnce:true,value:f.insert||false,method:function(g){if(g){var h={fontname:true,fontsize:true,forecolor:true,backcolor:true};
var i=this._defaultToolbar.buttons;
for(var j=0;
j<i.length;
j++){if(i[j].buttons){for(var k=0;
k<i[j].buttons.length;
k++){if(i[j].buttons[k].value){if(h[i[j].buttons[k].value]){delete i[j].buttons[k].disabled
}}}}}}}});
this.setAttributeConfig("container",{writeOnce:true,value:f.container||false});
this.setAttributeConfig("plainText",{writeOnce:true,value:f.plainText||false});
this.setAttributeConfig("iframe",{value:null});
this.setAttributeConfig("textarea",{value:null,writeOnce:true});
this.setAttributeConfig("container",{readOnly:true,value:null});
this.setAttributeConfig("nodeChangeThreshold",{value:f.nodeChangeThreshold||3,validator:YAHOO.lang.isNumber});
this.setAttributeConfig("allowNoEdit",{value:f.allowNoEdit||false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("limitCommands",{value:f.limitCommands||false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("element_cont",{value:f.element_cont});
this.setAttributeConfig("editor_wrapper",{value:f.editor_wrapper||null,writeOnce:true});
this.setAttributeConfig("height",{value:f.height||c.getStyle(e.get("element"),"height"),method:function(h){if(this._rendered){if(this.get("animate")){var g=new YAHOO.util.Anim(this.get("iframe").get("parentNode"),{height:{to:parseInt(h,10)}},0.5);
g.animate()
}else{c.setStyle(this.get("iframe").get("parentNode"),"height",h)
}}}});
this.setAttributeConfig("autoHeight",{value:f.autoHeight||false,method:function(g){if(g){if(this.get("iframe")){this.get("iframe").get("element").setAttribute("scrolling","no")
}this.on("afterNodeChange",this._handleAutoHeight,this,true);
this.on("editorKeyDown",this._handleAutoHeight,this,true);
this.on("editorKeyPress",this._handleAutoHeight,this,true)
}else{if(this.get("iframe")){this.get("iframe").get("element").setAttribute("scrolling","auto")
}this.unsubscribe("afterNodeChange",this._handleAutoHeight);
this.unsubscribe("editorKeyDown",this._handleAutoHeight);
this.unsubscribe("editorKeyPress",this._handleAutoHeight)
}}});
this.setAttributeConfig("width",{value:f.width||c.getStyle(this.get("element"),"width"),method:function(h){if(this._rendered){if(this.get("animate")){var g=new YAHOO.util.Anim(this.get("element_cont").get("element"),{width:{to:parseInt(h,10)}},0.5);
g.animate()
}else{this.get("element_cont").setStyle("width",h)
}}}});
this.setAttributeConfig("blankimage",{value:f.blankimage||this._getBlankImage()});
this.setAttributeConfig("css",{value:f.css||this._defaultCSS,writeOnce:true});
this.setAttributeConfig("html",{value:f.html||'<html><head><title>{TITLE}</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /><base href="'+this._baseHREF+'"><style>{CSS}</style><style>{HIDDEN_CSS}</style><style>{EXTRA_CSS}</style></head><body onload="document.body._rteLoaded = true;">{CONTENT}</body></html>',writeOnce:true});
this.setAttributeConfig("extracss",{value:f.extracss||"",writeOnce:true});
this.setAttributeConfig("handleSubmit",{value:f.handleSubmit||false,method:function(j){if(this.get("element").form){if(!this._formButtons){this._formButtons=[]
}if(j){a.on(this.get("element").form,"submit",this._handleFormSubmit,this,true);
var i=this.get("element").form.getElementsByTagName("input");
for(var g=0;
g<i.length;
g++){var h=i[g].getAttribute("type");
if(h&&(h.toLowerCase()=="submit")){a.on(i[g],"click",this._handleFormButtonClick,this,true);
this._formButtons[this._formButtons.length]=i[g]
}}}else{a.removeListener(this.get("element").form,"submit",this._handleFormSubmit);
if(this._formButtons){a.removeListener(this._formButtons,"click",this._handleFormButtonClick)
}}}}});
this.setAttributeConfig("disabled",{value:false,method:function(g){if(this._rendered){this._disableEditor(g)
}}});
this.setAttributeConfig("saveEl",{value:this.get("element")});
this.setAttributeConfig("toolbar_cont",{value:null,writeOnce:true});
this.setAttributeConfig("toolbar",{value:f.toolbar||this._defaultToolbar,writeOnce:true,method:function(g){if(!g.buttonType){g.buttonType=this._defaultToolbar.buttonType
}this._defaultToolbar=g
}});
this.setAttributeConfig("animate",{value:((f.animate)?((YAHOO.util.Anim)?true:false):false),validator:function(g){var h=true;
if(!YAHOO.util.Anim){h=false
}return h
}});
this.setAttributeConfig("panel",{value:null,writeOnce:true,validator:function(g){var h=true;
if(!YAHOO.widget.Overlay){h=false
}return h
}});
this.setAttributeConfig("focusAtStart",{value:f.focusAtStart||false,writeOnce:true,method:function(g){if(g){this.on("editorContentLoaded",function(){var h=this;
setTimeout(function(){h._focusWindow.call(h,true);
h.editorDirty=false
},400)
},this,true)
}}});
this.setAttributeConfig("dompath",{value:f.dompath||false,method:function(g){if(g&&!this.dompath){this.dompath=document.createElement("DIV");
this.dompath.id=this.get("id")+"_dompath";
c.addClass(this.dompath,"dompath");
this.get("element_cont").get("firstChild").appendChild(this.dompath);
if(this.get("iframe")){this._writeDomPath()
}}else{if(!g&&this.dompath){this.dompath.parentNode.removeChild(this.dompath);
this.dompath=null
}}}});
this.setAttributeConfig("markup",{value:f.markup||"semantic",validator:function(g){switch(g.toLowerCase()){case"semantic":case"css":case"default":case"xhtml":return true
}return false
}});
this.setAttributeConfig("removeLineBreaks",{value:f.removeLineBreaks||false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("drag",{writeOnce:true,value:f.drag||false});
this.setAttributeConfig("resize",{writeOnce:true,value:f.resize||false})
},_getBlankImage:function(){if(!this.DOMReady){this._queue[this._queue.length]=["_getBlankImage",arguments];
return""
}var f="";
if(!this._blankImageLoaded){if(YAHOO.widget.EditorInfo.blankImage){this.set("blankimage",YAHOO.widget.EditorInfo.blankImage);
this._blankImageLoaded=true
}else{var e=document.createElement("div");
e.style.position="absolute";
e.style.top="-9999px";
e.style.left="-9999px";
e.className=this.CLASS_PREFIX+"-blankimage";
document.body.appendChild(e);
f=YAHOO.util.Dom.getStyle(e,"background-image");
f=f.replace("url(","").replace(")","").replace(/"/g,"");
f=f.replace("app:/","");
this.set("blankimage",f);
this._blankImageLoaded=true;
e.parentNode.removeChild(e);
YAHOO.widget.EditorInfo.blankImage=f
}}else{f=this.get("blankimage")
}return f
},_handleAutoHeight:function(){var f=this._getDoc(),i=f.body,e=f.documentElement;
var j=parseInt(c.getStyle(this.get("editor_wrapper"),"height"),10);
var h=i.scrollHeight;
if(this.browser.webkit){h=e.scrollHeight
}if(h<parseInt(this.get("height"),10)){h=parseInt(this.get("height"),10)
}if((j!=h)&&(h>=parseInt(this.get("height"),10))){c.setStyle(this.get("editor_wrapper"),"height",h+"px");
if(this.browser.ie){this.get("iframe").setStyle("height","99%");
this.get("iframe").setStyle("zoom","1");
var g=this;
window.setTimeout(function(){g.get("iframe").setStyle("height","100%")
},1)
}}},_formButtons:null,_formButtonClicked:null,_handleFormButtonClick:function(e){var f=a.getTarget(e);
this._formButtonClicked=f
},_handleFormSubmit:function(e){this.saveHTML();
var f=this.get("element").form,h=this._formButtonClicked||false;
a.removeListener(f,"submit",this._handleFormSubmit);
if(YAHOO.env.ua.ie){if(h&&!h.disabled){h.click()
}}else{if(h&&!h.disabled){h.click()
}var g=document.createEvent("HTMLEvents");
g.initEvent("submit",true,true);
f.dispatchEvent(g);
if(YAHOO.env.ua.webkit){if(YAHOO.lang.isFunction(f.submit)){f.submit()
}}}},_handleFontSize:function(e){var g=this.toolbar.getButtonById(e.button.id);
var f=g.get("label")+"px";
this.execCommand("fontsize",f);
this.STOP_EXEC_COMMAND=true
},_handleColorPicker:function(e){var f=e.button;
var g="#"+e.color;
if((f=="forecolor")||(f=="backcolor")){this.execCommand(f,g)
}},_handleAlign:function(e){var f=null;
for(var h=0;
h<e.button.menu.length;
h++){if(e.button.menu[h].value==e.button.value){f=e.button.menu[h].value
}}var g=this._getSelection();
this.execCommand(f,g);
this.STOP_EXEC_COMMAND=true
},_handleAfterNodeChange:function(){var e=this._getDomPath(),j=null,n=null,i=null,p=false,l=this.toolbar.getButtonByValue("fontname"),k=this.toolbar.getButtonByValue("fontsize"),q=this.toolbar.getButtonByValue("heading");
for(var o=0;
o<e.length;
o++){j=e[o];
var f=j.tagName.toLowerCase();
if(j.getAttribute("tag")){f=j.getAttribute("tag")
}n=j.getAttribute("face");
if(c.getStyle(j,"font-family")){n=c.getStyle(j,"font-family");
n=n.replace(/'/g,"")
}if(f.substring(0,1)=="h"){if(q){for(var m=0;
m<q._configs.menu.value.length;
m++){if(q._configs.menu.value[m].value.toLowerCase()==f){q.set("label",q._configs.menu.value[m].text)
}}this._updateMenuChecked("heading",f)
}}}if(l){for(var g=0;
g<l._configs.menu.value.length;
g++){if(n&&l._configs.menu.value[g].text.toLowerCase()==n.toLowerCase()){p=true;
n=l._configs.menu.value[g].text
}}if(!p){n=l._configs.label._initialConfig.value
}var h='<span class="yui-toolbar-fontname-'+this._cleanClassName(n)+'">'+n+"</span>";
if(l.get("label")!=h){l.set("label",h);
this._updateMenuChecked("fontname",n)
}}if(k){i=parseInt(c.getStyle(j,"fontSize"),10);
if((i===null)||isNaN(i)){i=k._configs.label._initialConfig.value
}k.set("label",""+i)
}if(!this._isElement(j,"body")&&!this._isElement(j,"img")){this.toolbar.enableButton(l);
this.toolbar.enableButton(k);
this.toolbar.enableButton("forecolor");
this.toolbar.enableButton("backcolor")
}if(this._isElement(j,"img")){if(YAHOO.widget.Overlay){this.toolbar.enableButton("createlink")
}}if(this._hasParent(j,"blockquote")){this.toolbar.selectButton("indent");
this.toolbar.disableButton("indent");
this.toolbar.enableButton("outdent")
}if(this._hasParent(j,"ol")||this._hasParent(j,"ul")){this.toolbar.disableButton("indent")
}this._lastButton=null
},_handleInsertImageClick:function(){if(this.get("limitCommands")){if(!this.toolbar.getButtonByValue("insertimage")){return false
}}this.toolbar.set("disabled",true);
this.on("afterExecCommand",function(){var g=this.currentElement[0],e="http://";
if(!g){g=this._getSelectedElement()
}if(g){if(g.getAttribute("src")){e=g.getAttribute("src",2);
if(e.indexOf(this.get("blankimage"))!=-1){e=this.STR_IMAGE_HERE
}}}var f=prompt(this.STR_LINK_URL+": ",e);
if((f!=="")&&(f!==null)){g.setAttribute("src",f)
}else{if(f===null){g.parentNode.removeChild(g);
this.currentElement=[];
this.nodeChange()
}}this.closeWindow();
this.toolbar.set("disabled",false)
},this,true)
},_handleInsertImageWindowClose:function(){this.nodeChange()
},_isLocalFile:function(e){if((e)&&(e!=="")&&((e.indexOf("file:/")!=-1)||(e.indexOf(":\\")!=-1))){return true
}return false
},_handleCreateLinkClick:function(){if(this.get("limitCommands")){if(!this.toolbar.getButtonByValue("createlink")){return false
}}this.toolbar.set("disabled",true);
this.on("afterExecCommand",function(){var g=this.currentElement[0],h="";
if(g){if(g.getAttribute("href",2)!==null){h=g.getAttribute("href",2)
}}var e=prompt(this.STR_LINK_URL+": ",h);
if((e!=="")&&(e!==null)){var f=e;
if((f.indexOf("://")==-1)&&(f.substring(0,1)!="/")&&(f.substring(0,6).toLowerCase()!="mailto")){if((f.indexOf("@")!=-1)&&(f.substring(0,6).toLowerCase()!="mailto")){f="mailto:"+f
}else{if(f.substring(0,1)!="#"){}}}g.setAttribute("href",f)
}else{if(e!==null){var i=this._getDoc().createElement("span");
i.innerHTML=g.innerHTML;
c.addClass(i,"yui-non");
g.parentNode.replaceChild(i,g)
}}this.closeWindow();
this.toolbar.set("disabled",false)
},this)
},_handleCreateLinkWindowClose:function(){this.nodeChange();
this.currentElement=[]
},render:function(){if(this._rendered){return false
}if(!this.DOMReady){this._queue[this._queue.length]=["render",arguments];
return false
}if(this.get("element")){if(this.get("element").tagName){this._textarea=true;
if(this.get("element").tagName.toLowerCase()!=="textarea"){this._textarea=false
}}else{return false
}}else{return false
}this._rendered=true;
var e=this;
window.setTimeout(function(){e._render.call(e)
},4)
},_render:function(){var f=this;
this.set("textarea",this.get("element"));
this.get("element_cont").setStyle("display","none");
this.get("element_cont").addClass(this.CLASS_CONTAINER);
this.set("iframe",this._createIframe());
window.setTimeout(function(){f._setInitialContent.call(f)
},10);
this.get("editor_wrapper").appendChild(this.get("iframe").get("element"));
if(this.get("disabled")){this._disableEditor(true)
}var e=this.get("toolbar");
if(e instanceof d){this.toolbar=e;
this.toolbar.set("disabled",true)
}else{e.disabled=true;
this.toolbar=new d(this.get("toolbar_cont"),e)
}this.fireEvent("toolbarLoaded",{type:"toolbarLoaded",target:this.toolbar});
this.toolbar.on("toolbarCollapsed",function(){if(this.currentWindow){this.moveWindow()
}},this,true);
this.toolbar.on("toolbarExpanded",function(){if(this.currentWindow){this.moveWindow()
}},this,true);
this.toolbar.on("fontsizeClick",this._handleFontSize,this,true);
this.toolbar.on("colorPickerClicked",function(g){this._handleColorPicker(g);
return false
},this,true);
this.toolbar.on("alignClick",this._handleAlign,this,true);
this.on("afterNodeChange",this._handleAfterNodeChange,this,true);
this.toolbar.on("insertimageClick",this._handleInsertImageClick,this,true);
this.on("windowinsertimageClose",this._handleInsertImageWindowClose,this,true);
this.toolbar.on("createlinkClick",this._handleCreateLinkClick,this,true);
this.on("windowcreatelinkClose",this._handleCreateLinkWindowClose,this,true);
this.get("parentNode").replaceChild(this.get("element_cont").get("element"),this.get("element"));
this.setStyle("visibility","hidden");
this.setStyle("position","absolute");
this.setStyle("top","-9999px");
this.setStyle("left","-9999px");
this.get("element_cont").appendChild(this.get("element"));
this.get("element_cont").setStyle("display","block");
c.addClass(this.get("iframe").get("parentNode"),this.CLASS_EDITABLE_CONT);
this.get("iframe").addClass(this.CLASS_EDITABLE);
this.get("element_cont").setStyle("width",this.get("width"));
c.setStyle(this.get("iframe").get("parentNode"),"height",this.get("height"));
this.get("iframe").setStyle("width","100%");
this.get("iframe").setStyle("height","100%");
this._setupDD();
window.setTimeout(function(){f._setupAfterElement.call(f)
},0);
this.fireEvent("afterRender",{type:"afterRender",target:this})
},execCommand:function(h,i){var e=this.fireEvent("beforeExecCommand",{type:"beforeExecCommand",target:this,args:arguments});
if((e===false)||(this.STOP_EXEC_COMMAND)){this.STOP_EXEC_COMMAND=false;
return false
}this._lastCommand=h;
this._setMarkupType(h);
if(this.browser.ie){this._getWindow().focus()
}var j=true;
if(this.get("limitCommands")){if(!this.toolbar.getButtonByValue(h)){j=false
}}this.editorDirty=true;
if((typeof this["cmd_"+h.toLowerCase()]=="function")&&j){var f=this["cmd_"+h.toLowerCase()](i);
j=f[0];
if(f[1]){h=f[1]
}if(f[2]){i=f[2]
}}if(j){try{this._getDoc().execCommand(h,false,i)
}catch(g){}}else{}this.on("afterExecCommand",function(){this.unsubscribeAll("afterExecCommand");
this.nodeChange()
},this,true);
this.fireEvent("afterExecCommand",{type:"afterExecCommand",target:this})
},cmd_underline:function(e){if(!this.browser.webkit){var f=this._getSelectedElement();
if(f&&this._isElement(f,"span")){if(f.style.textDecoration=="underline"){f.style.textDecoration="none"
}else{f.style.textDecoration="underline"
}return[false]
}}return[true]
},cmd_backcolor:function(e){var h=true,g=this._getSelectedElement(),f="backcolor";
if(this.browser.gecko||this.browser.opera){this._setEditorStyle(true);
f="hilitecolor"
}if(!this._isElement(g,"body")&&!this._hasSelection()){c.setStyle(g,"background-color",e);
this._selectNode(g);
h=false
}else{if(!this._isElement(g,"body")&&this._hasSelection()){c.setStyle(g,"background-color",e);
this._selectNode(g);
h=false
}else{if(this.get("insert")){g=this._createInsertElement({backgroundColor:e})
}else{this._createCurrentElement("span",{backgroundColor:e});
this._selectNode(this.currentElement[0])
}h=false
}}return[h,f]
},cmd_forecolor:function(e){var g=true,f=this._getSelectedElement();
if(!this._isElement(f,"body")&&!this._hasSelection()){c.setStyle(f,"color",e);
this._selectNode(f);
g=false
}else{if(!this._isElement(f,"body")&&this._hasSelection()){c.setStyle(f,"color",e);
this._selectNode(f);
g=false
}else{if(this.get("insert")){f=this._createInsertElement({color:e})
}else{this._createCurrentElement("span",{color:e});
this._selectNode(this.currentElement[0])
}g=false
}}return[g]
},cmd_unlink:function(e){this._swapEl(this.currentElement[0],"span",function(f){f.className="yui-non"
});
return[false]
},cmd_createlink:function(e){var f=this._getSelectedElement(),g=null;
if(this._hasParent(f,"a")){this.currentElement[0]=this._hasParent(f,"a")
}else{if(!this._isElement(f,"a")){this._createCurrentElement("a");
g=this._swapEl(this.currentElement[0],"a");
this.currentElement[0]=g
}else{this.currentElement[0]=f
}}return[false]
},cmd_insertimage:function(f){var k=true,j=null,g="insertimage",h=this._getSelectedElement();
if(f===""){f=this.get("blankimage")
}if(this._isElement(h,"img")){this.currentElement[0]=h;
k=false
}else{if(this._getDoc().queryCommandEnabled(g)){this._getDoc().execCommand("insertimage",false,f);
var e=this._getDoc().getElementsByTagName("img");
for(var i=0;
i<e.length;
i++){if(!YAHOO.util.Dom.hasClass(e[i],"yui-img")){YAHOO.util.Dom.addClass(e[i],"yui-img");
this.currentElement[0]=e[i]
}}k=false
}else{if(h==this._getDoc().body){j=this._getDoc().createElement("img");
j.setAttribute("src",f);
YAHOO.util.Dom.addClass(j,"yui-img");
this._getDoc().body.appendChild(j)
}else{this._createCurrentElement("img");
j=this._getDoc().createElement("img");
j.setAttribute("src",f);
YAHOO.util.Dom.addClass(j,"yui-img");
this.currentElement[0].parentNode.replaceChild(j,this.currentElement[0])
}this.currentElement[0]=j;
k=false
}}return[k]
},cmd_inserthtml:function(f){var i=true,g="inserthtml",h=null,e=null;
if(this.browser.webkit&&!this._getDoc().queryCommandEnabled(g)){this._createCurrentElement("img");
h=this._getDoc().createElement("span");
h.innerHTML=f;
this.currentElement[0].parentNode.replaceChild(h,this.currentElement[0]);
i=false
}else{if(this.browser.ie){e=this._getRange();
if(e.item){e.item(0).outerHTML=f
}else{e.pasteHTML(f)
}i=false
}}return[i]
},cmd_list:function(e){var k=true,h=null,q=0,w=null,l="",g=this._getSelectedElement(),j="insertorderedlist";
if(e=="ul"){j="insertunorderedlist"
}if(this.browser.webkit){if(this._isElement(g,"li")&&this._isElement(g.parentNode,e)){w=g.parentNode;
h=this._getDoc().createElement("span");
YAHOO.util.Dom.addClass(h,"yui-non");
l="";
var x=w.getElementsByTagName("li");
for(q=0;
q<x.length;
q++){l+="<div>"+x[q].innerHTML+"</div>"
}h.innerHTML=l;
this.currentElement[0]=w;
this.currentElement[0].parentNode.replaceChild(h,this.currentElement[0])
}else{this._createCurrentElement(e.toLowerCase());
h=this._getDoc().createElement(e);
for(q=0;
q<this.currentElement.length;
q++){var t=this._getDoc().createElement("li");
t.innerHTML=this.currentElement[q].innerHTML+'<span class="yui-non">&nbsp;</span>&nbsp;';
h.appendChild(t);
if(q>0){this.currentElement[q].parentNode.removeChild(this.currentElement[q])
}}this.currentElement[0].parentNode.replaceChild(h,this.currentElement[0]);
this.currentElement[0]=h;
var v=this.currentElement[0].firstChild;
v=c.getElementsByClassName("yui-non","span",v)[0];
this._getSelection().setBaseAndExtent(v,1,v,v.innerText.length)
}k=false
}else{w=this._getSelectedElement();
if(this._isElement(w,"li")&&this._isElement(w.parentNode,e)||(this.browser.ie&&this._isElement(this._getRange().parentElement,"li"))||(this.browser.ie&&this._isElement(w,"ul"))||(this.browser.ie&&this._isElement(w,"ol"))){if(this.browser.ie){if((this.browser.ie&&this._isElement(w,"ul"))||(this.browser.ie&&this._isElement(w,"ol"))){w=w.getElementsByTagName("li")[0]
}l="";
var u=w.parentNode.getElementsByTagName("li");
for(var i=0;
i<u.length;
i++){l+=u[i].innerHTML+"<br>"
}var f=this._getDoc().createElement("span");
f.innerHTML=l;
w.parentNode.parentNode.replaceChild(f,w.parentNode)
}else{this.nodeChange();
this._getDoc().execCommand(j,"",w.parentNode);
this.nodeChange()
}k=false
}if(this.browser.opera){var m=this;
window.setTimeout(function(){var A=m._getDoc().getElementsByTagName("li");
for(var z=0;
z<A.length;
z++){if(A[z].innerHTML.toLowerCase()=="<br>"){A[z].parentNode.parentNode.removeChild(A[z].parentNode)
}}},30)
}if(this.browser.ie&&k){var s="";
if(this._getRange().html){s="<li>"+this._getRange().html+"</li>"
}else{var r=this._getRange().text.split("\n");
if(r.length>1){s="";
for(var n=0;
n<r.length;
n++){s+="<li>"+r[n]+"</li>"
}}else{var o=this._getRange().text;
if(o===""){s='<li id="new_list_item">'+o+"</li>"
}else{s="<li>"+o+"</li>"
}}}this._getRange().pasteHTML("<"+e+">"+s+"</"+e+">");
var y=this._getDoc().getElementById("new_list_item");
if(y){var p=this._getDoc().body.createTextRange();
p.moveToElementText(y);
p.collapse(false);
p.select();
y.id=""
}k=false
}}return k
},cmd_insertorderedlist:function(e){return[this.cmd_list("ol")]
},cmd_insertunorderedlist:function(e){return[this.cmd_list("ul")]
},cmd_fontname:function(e){var h=true,f=this._getSelectedElement();
this.currentFont=e;
if(f&&f.tagName&&!this._hasSelection()&&!this._isElement(f,"body")&&!this.get("insert")){YAHOO.util.Dom.setStyle(f,"font-family",e);
h=false
}else{if(this.get("insert")&&!this._hasSelection()){var g=this._createInsertElement({fontFamily:e});
h=false
}}return[h]
},cmd_fontsize:function(e){var g=null;
if(this.currentElement&&(this.currentElement.length>0)&&(!this._hasSelection())&&(!this.get("insert"))){YAHOO.util.Dom.setStyle(this.currentElement,"fontSize",e)
}else{if(!this._isElement(this._getSelectedElement(),"body")){g=this._getSelectedElement();
YAHOO.util.Dom.setStyle(g,"fontSize",e);
if(this.get("insert")&&this.browser.ie){var f=this._getRange();
f.collapse(false);
f.select()
}else{this._selectNode(g)
}}else{if(this.get("insert")&&!this._hasSelection()){g=this._createInsertElement({fontSize:e});
this.currentElement[0]=g;
this._selectNode(this.currentElement[0])
}else{this._createCurrentElement("span",{fontSize:e});
this._selectNode(this.currentElement[0])
}}}return[false]
},_swapEl:function(g,h,e){var f=this._getDoc().createElement(h);
if(g){f.innerHTML=g.innerHTML
}if(typeof e=="function"){e.call(this,f)
}if(g){g.parentNode.replaceChild(f,g)
}return f
},_createInsertElement:function(f){this._createCurrentElement("span",f);
var e=this.currentElement[0];
if(this.browser.webkit){e.innerHTML='<span class="yui-non">&nbsp;</span>';
e=e.firstChild;
this._getSelection().setBaseAndExtent(e,1,e,e.innerText.length)
}else{if(this.browser.ie||this.browser.opera){e.innerHTML="&nbsp;"
}}this._focusWindow();
this._selectNode(e,true);
return e
},_createCurrentElement:function(t,q){t=((t)?t:"a");
var i=null,u=[],s=this._getDoc();
if(this.currentFont){if(!q){q={}
}q.fontFamily=this.currentFont;
this.currentFont=null
}this.currentElement=[];
var n=function(z,x){var y=null;
z=((z)?z:"span");
z=z.toLowerCase();
switch(z){case"h1":case"h2":case"h3":case"h4":case"h5":case"h6":y=s.createElement(z);
break;
default:y=s.createElement(z);
if(z==="span"){YAHOO.util.Dom.addClass(y,"yui-tag-"+z);
YAHOO.util.Dom.addClass(y,"yui-tag");
y.setAttribute("tag",z)
}for(var w in x){if(YAHOO.lang.hasOwnProperty(x,w)){y.style[w]=x[w]
}}break
}return y
};
if(!this._hasSelection()){if(this._getDoc().queryCommandEnabled("insertimage")){this._getDoc().execCommand("insertimage",false,"yui-tmp-img");
var o=this._getDoc().getElementsByTagName("img");
for(var j=0;
j<o.length;
j++){if(o[j].getAttribute("src",2)=="yui-tmp-img"){u=n(t,q);
o[j].parentNode.replaceChild(u,o[j]);
this.currentElement[this.currentElement.length]=u
}}}else{if(this.currentEvent){i=YAHOO.util.Event.getTarget(this.currentEvent)
}else{i=this._getDoc().body
}}if(i){u=n(t,q);
if(this._isElement(i,"body")||this._isElement(i,"html")){if(this._isElement(i,"html")){i=this._getDoc().body
}i.appendChild(u)
}else{if(i.nextSibling){i.parentNode.insertBefore(u,i.nextSibling)
}else{i.parentNode.appendChild(u)
}}this.currentElement[this.currentElement.length]=u;
this.currentEvent=null;
if(this.browser.webkit){this._getSelection().setBaseAndExtent(u,0,u,0);
if(this.browser.webkit3){this._getSelection().collapseToStart()
}else{this._getSelection().collapse(true)
}}}}else{this._setEditorStyle(true);
this._getDoc().execCommand("fontname",false,"yui-tmp");
var v=[],k,e=["font","span","i","b","u"];
if(!this._isElement(this._getSelectedElement(),"body")){e[e.length]=this._getDoc().getElementsByTagName(this._getSelectedElement().tagName);
e[e.length]=this._getDoc().getElementsByTagName(this._getSelectedElement().parentNode.tagName)
}for(var p=0;
p<e.length;
p++){var r=this._getDoc().getElementsByTagName(e[p]);
for(var f=0;
f<r.length;
f++){v[v.length]=r[f]
}}for(var h=0;
h<v.length;
h++){if((YAHOO.util.Dom.getStyle(v[h],"font-family")=="yui-tmp")||(v[h].face&&(v[h].face=="yui-tmp"))){u=n(t,q);
u.innerHTML=v[h].innerHTML;
if(this._isElement(v[h],"ol")||(this._isElement(v[h],"ul"))){var m=v[h].getElementsByTagName("li")[0];
v[h].style.fontFamily="inherit";
m.style.fontFamily="inherit";
u.innerHTML=m.innerHTML;
m.innerHTML="";
m.appendChild(u);
this.currentElement[this.currentElement.length]=u
}else{if(this._isElement(v[h],"li")){v[h].innerHTML="";
v[h].appendChild(u);
v[h].style.fontFamily="inherit";
this.currentElement[this.currentElement.length]=u
}else{if(v[h].parentNode){v[h].parentNode.replaceChild(u,v[h]);
this.currentElement[this.currentElement.length]=u;
this.currentEvent=null;
if(this.browser.webkit){this._getSelection().setBaseAndExtent(u,0,u,0);
if(this.browser.webkit3){this._getSelection().collapseToStart()
}else{this._getSelection().collapse(true)
}}if(this.browser.ie&&q&&q.fontSize){this._getSelection().empty()
}if(this.browser.gecko){this._getSelection().collapseToStart()
}}}}}}var g=this.currentElement.length;
for(var l=0;
l<g;
l++){if((l+1)!=g){if(this.currentElement[l]&&this.currentElement[l].nextSibling){if(this._isElement(this.currentElement[l],"br")){this.currentElement[this.currentElement.length]=this.currentElement[l].nextSibling
}}}}}},saveHTML:function(){var e=this.cleanHTML();
if(this._textarea){this.get("element").value=e
}else{this.get("element").innerHTML=e
}if(this.get("saveEl")!==this.get("element")){var f=this.get("saveEl");
if(b.isString(f)){f=c.get(f)
}if(f){if(f.tagName.toLowerCase()==="textarea"){f.value=e
}else{f.innerHTML=e
}}}return e
},setEditorHTML:function(e){var f=this._cleanIncomingHTML(e);
this._getDoc().body.innerHTML=f;
this.nodeChange()
},getEditorHTML:function(){var e=this._getDoc().body;
if(e===null){return null
}return this._getDoc().body.innerHTML
},show:function(){if(this.browser.gecko){this._setDesignMode("on");
this._focusWindow()
}if(this.browser.webkit){var e=this;
window.setTimeout(function(){e._setInitialContent.call(e)
},10)
}if(this.currentWindow){this.closeWindow()
}this.get("iframe").setStyle("position","static");
this.get("iframe").setStyle("left","")
},hide:function(){if(this.currentWindow){this.closeWindow()
}if(this._fixNodesTimer){clearTimeout(this._fixNodesTimer);
this._fixNodesTimer=null
}if(this._nodeChangeTimer){clearTimeout(this._nodeChangeTimer);
this._nodeChangeTimer=null
}this._lastNodeChange=0;
this.get("iframe").setStyle("position","absolute");
this.get("iframe").setStyle("left","-9999px")
},_cleanIncomingHTML:function(e){e=e.replace(/<strong([^>]*)>/gi,"<b$1>");
e=e.replace(/<\/strong>/gi,"</b>");
e=e.replace(/<embed([^>]*)>/gi,"<YUI_EMBED$1>");
e=e.replace(/<\/embed>/gi,"</YUI_EMBED>");
e=e.replace(/<em([^>]*)>/gi,"<i$1>");
e=e.replace(/<\/em>/gi,"</i>");
e=e.replace(/<YUI_EMBED([^>]*)>/gi,"<embed$1>");
e=e.replace(/<\/YUI_EMBED>/gi,"</embed>");
if(this.get("plainText")){e=e.replace(/\n/g,"<br>").replace(/\r/g,"<br>");
e=e.replace(/  /gi,"&nbsp;&nbsp;");
e=e.replace(/\t/gi,"&nbsp;&nbsp;&nbsp;&nbsp;")
}e=e.replace(/<script([^>]*)>/gi,"<bad>");
e=e.replace(/<\/script([^>]*)>/gi,"</bad>");
e=e.replace(/&lt;script([^>]*)&gt;/gi,"<bad>");
e=e.replace(/&lt;\/script([^>]*)&gt;/gi,"</bad>");
e=e.replace(/\n/g,"<YUI_LF>").replace(/\r/g,"<YUI_LF>");
e=e.replace(new RegExp("<bad([^>]*)>(.*?)</bad>","gi"),"");
e=e.replace(/<YUI_LF>/g,"\n");
return e
},cleanHTML:function(e){if(!e){e=this.getEditorHTML()
}var f=this.get("markup");
e=this.pre_filter_linebreaks(e,f);
e=e.replace(/<img([^>]*)\/>/gi,"<YUI_IMG$1>");
e=e.replace(/<img([^>]*)>/gi,"<YUI_IMG$1>");
e=e.replace(/<input([^>]*)\/>/gi,"<YUI_INPUT$1>");
e=e.replace(/<input([^>]*)>/gi,"<YUI_INPUT$1>");
e=e.replace(/<ul([^>]*)>/gi,"<YUI_UL$1>");
e=e.replace(/<\/ul>/gi,"</YUI_UL>");
e=e.replace(/<blockquote([^>]*)>/gi,"<YUI_BQ$1>");
e=e.replace(/<\/blockquote>/gi,"</YUI_BQ>");
e=e.replace(/<embed([^>]*)>/gi,"<YUI_EMBED$1>");
e=e.replace(/<\/embed>/gi,"</YUI_EMBED>");
if((f=="semantic")||(f=="xhtml")){e=e.replace(/<i(\s+[^>]*)?>/gi,"<em$1>");
e=e.replace(/<\/i>/gi,"</em>");
e=e.replace(/<b(\s+[^>]*)?>/gi,"<strong$1>");
e=e.replace(/<\/b>/gi,"</strong>")
}e=e.replace(/<font/gi,"<font");
e=e.replace(/<\/font>/gi,"</font>");
e=e.replace(/<span/gi,"<span");
e=e.replace(/<\/span>/gi,"</span>");
if((f=="semantic")||(f=="xhtml")||(f=="css")){e=e.replace(new RegExp('<font([^>]*)face="([^>]*)">(.*?)</font>',"gi"),'<span $1 style="font-family: $2;">$3</span>');
e=e.replace(/<u/gi,'<span style="text-decoration: underline;"');
if(this.browser.webkit){e=e.replace(new RegExp('<span class="Apple-style-span" style="font-weight: bold;">([^>]*)</span>',"gi"),"<strong>$1</strong>");
e=e.replace(new RegExp('<span class="Apple-style-span" style="font-style: italic;">([^>]*)</span>',"gi"),"<em>$1</em>")
}e=e.replace(/\/u>/gi,"/span>");
if(f=="css"){e=e.replace(/<em([^>]*)>/gi,"<i$1>");
e=e.replace(/<\/em>/gi,"</i>");
e=e.replace(/<strong([^>]*)>/gi,"<b$1>");
e=e.replace(/<\/strong>/gi,"</b>");
e=e.replace(/<b/gi,'<span style="font-weight: bold;"');
e=e.replace(/\/b>/gi,"/span>");
e=e.replace(/<i/gi,'<span style="font-style: italic;"');
e=e.replace(/\/i>/gi,"/span>")
}e=e.replace(/  /gi," ")
}else{e=e.replace(/<u/gi,"<u");
e=e.replace(/\/u>/gi,"/u>")
}e=e.replace(/<ol([^>]*)>/gi,"<ol$1>");
e=e.replace(/\/ol>/gi,"/ol>");
e=e.replace(/<li/gi,"<li");
e=e.replace(/\/li>/gi,"/li>");
e=this.filter_safari(e);
e=this.filter_internals(e);
e=this.filter_all_rgb(e);
e=this.post_filter_linebreaks(e,f);
if(f=="xhtml"){e=e.replace(/<YUI_IMG([^>]*)>/g,"<img $1 />");
e=e.replace(/<YUI_INPUT([^>]*)>/g,"<input $1 />")
}else{e=e.replace(/<YUI_IMG([^>]*)>/g,"<img $1>");
e=e.replace(/<YUI_INPUT([^>]*)>/g,"<input $1>")
}e=e.replace(/<YUI_UL([^>]*)>/g,"<ul$1>");
e=e.replace(/<\/YUI_UL>/g,"</ul>");
e=this.filter_invalid_lists(e);
e=e.replace(/<YUI_BQ([^>]*)>/g,"<blockquote$1>");
e=e.replace(/<\/YUI_BQ>/g,"</blockquote>");
e=e.replace(/<YUI_EMBED([^>]*)>/g,"<embed$1>");
e=e.replace(/<\/YUI_EMBED>/g,"</embed>");
e=e.replace(" &amp; ","YUI_AMP");
e=e.replace("&amp;","&");
e=e.replace("YUI_AMP","&amp;");
e=YAHOO.lang.trim(e);
if(this.get("removeLineBreaks")){e=e.replace(/\n/g,"").replace(/\r/g,"");
e=e.replace(/  /gi," ")
}if(e.substring(0,6).toLowerCase()=="<span>"){e=e.substring(6);
if(e.substring(e.length-7,e.length).toLowerCase()=="</span>"){e=e.substring(0,e.length-7)
}}for(var g in this.invalidHTML){if(YAHOO.lang.hasOwnProperty(this.invalidHTML,g)){if(b.isObject(g)&&g.keepContents){e=e.replace(new RegExp("<"+g+"([^>]*)>(.*?)</"+g+">","gi"),"$1")
}else{e=e.replace(new RegExp("<"+g+"([^>]*)>(.*?)</"+g+">","gi"),"")
}}}this.fireEvent("cleanHTML",{type:"cleanHTML",target:this,html:e});
return e
},filter_invalid_lists:function(e){e=e.replace(/<\/li>\n/gi,"</li>");
e=e.replace(/<\/li><ol>/gi,"</li><li><ol>");
e=e.replace(/<\/ol>/gi,"</ol></li>");
e=e.replace(/<\/ol><\/li>\n/gi,"</ol>\n");
e=e.replace(/<\/li><ul>/gi,"</li><li><ul>");
e=e.replace(/<\/ul>/gi,"</ul></li>");
e=e.replace(/<\/ul><\/li>\n?/gi,"</ul>\n");
e=e.replace(/<\/li>/gi,"</li>\n");
e=e.replace(/<\/ol>/gi,"</ol>\n");
e=e.replace(/<ol>/gi,"<ol>\n");
e=e.replace(/<ul>/gi,"<ul>\n");
return e
},filter_safari:function(e){if(this.browser.webkit){e=e.replace(/<span class="Apple-tab-span" style="white-space:pre">([^>])<\/span>/gi,"&nbsp;&nbsp;&nbsp;&nbsp;");
e=e.replace(/Apple-style-span/gi,"");
e=e.replace(/style="line-height: normal;"/gi,"");
e=e.replace(/<li><\/li>/gi,"");
e=e.replace(/<li> <\/li>/gi,"");
e=e.replace(/<li>  <\/li>/gi,"");
if(this.get("ptags")){e=e.replace(/<div([^>]*)>/g,"<p$1>");
e=e.replace(/<\/div>/gi,"</p>")
}else{e=e.replace(/<div>/gi,"");
e=e.replace(/<\/div>/gi,"<br>")
}}return e
},filter_internals:function(e){e=e.replace(/\r/g,"");
e=e.replace(/<\/?(body|head|html)[^>]*>/gi,"");
e=e.replace(/<YUI_BR><\/li>/gi,"</li>");
e=e.replace(/yui-tag-span/gi,"");
e=e.replace(/yui-tag/gi,"");
e=e.replace(/yui-non/gi,"");
e=e.replace(/yui-img/gi,"");
e=e.replace(/ tag="span"/gi,"");
e=e.replace(/ class=""/gi,"");
e=e.replace(/ style=""/gi,"");
e=e.replace(/ class=" "/gi,"");
e=e.replace(/ class="  "/gi,"");
e=e.replace(/ target=""/gi,"");
e=e.replace(/ title=""/gi,"");
if(this.browser.ie){e=e.replace(/ class= /gi,"");
e=e.replace(/ class= >/gi,"");
e=e.replace(/_height="([^>])"/gi,"");
e=e.replace(/_width="([^>])"/gi,"")
}return e
},filter_all_rgb:function(e){var f=new RegExp("rgb\\s*?\\(\\s*?([0-9]+).*?,\\s*?([0-9]+).*?,\\s*?([0-9]+).*?\\)","gi");
var i=e.match(f);
if(b.isArray(i)){for(var g=0;
g<i.length;
g++){var h=this.filter_rgb(i[g]);
e=e.replace(i[g].toString(),h)
}}return e
},filter_rgb:function(h){if(h.toLowerCase().indexOf("rgb")!=-1){var e=new RegExp("(.*?)rgb\\s*?\\(\\s*?([0-9]+).*?,\\s*?([0-9]+).*?,\\s*?([0-9]+).*?\\)(.*?)","gi");
var i=h.replace(e,"$1,$2,$3,$4,$5").split(",");
if(i.length==5){var f=parseInt(i[1],10).toString(16);
var g=parseInt(i[2],10).toString(16);
var j=parseInt(i[3],10).toString(16);
f=f.length==1?"0"+f:f;
g=g.length==1?"0"+g:g;
j=j.length==1?"0"+j:j;
h="#"+f+g+j
}}return h
},pre_filter_linebreaks:function(e,f){if(this.browser.webkit){e=e.replace(/<br class="khtml-block-placeholder">/gi,"<YUI_BR>");
e=e.replace(/<br class="webkit-block-placeholder">/gi,"<YUI_BR>")
}e=e.replace(/<br>/gi,"<YUI_BR>");
e=e.replace(/<br (.*?)>/gi,"<YUI_BR>");
e=e.replace(/<br\/>/gi,"<YUI_BR>");
e=e.replace(/<br \/>/gi,"<YUI_BR>");
e=e.replace(/<div><YUI_BR><\/div>/gi,"<YUI_BR>");
e=e.replace(/<p>(&nbsp;|&#160;)<\/p>/g,"<YUI_BR>");
e=e.replace(/<p><br>&nbsp;<\/p>/gi,"<YUI_BR>");
e=e.replace(/<p>&nbsp;<\/p>/gi,"<YUI_BR>");
e=e.replace(/<YUI_BR>$/,"");
e=e.replace(/<YUI_BR><\/p>/g,"</p>");
if(this.browser.ie){e=e.replace(/&nbsp;&nbsp;&nbsp;&nbsp;/g,"\t")
}return e
},post_filter_linebreaks:function(e,f){if(f=="xhtml"){e=e.replace(/<YUI_BR>/g,"<br />")
}else{e=e.replace(/<YUI_BR>/g,"<br>")
}return e
},clearEditorDoc:function(){this._getDoc().body.innerHTML="&nbsp;"
},openWindow:function(e){},moveWindow:function(){},_closeWindow:function(){},closeWindow:function(){this.toolbar.resetAllButtons();
this._focusWindow()
},destroy:function(){if(this.resize){this.resize.destroy()
}if(this.dd){this.dd.unreg()
}if(this.get("panel")){this.get("panel").destroy()
}this.saveHTML();
this.toolbar.destroy();
this.setStyle("visibility","visible");
this.setStyle("position","static");
this.setStyle("top","");
this.setStyle("left","");
var e=this.get("element");
this.get("element_cont").get("parentNode").replaceChild(e,this.get("element_cont").get("element"));
this.get("element_cont").get("element").innerHTML="";
this.set("handleSubmit",false);
return true
},toString:function(){var e="SimpleEditor";
if(this.get&&this.get("element_cont")){e="SimpleEditor (#"+this.get("element_cont").get("id")+")"+((this.get("disabled")?" Disabled":""))
}return e
}});
YAHOO.widget.EditorInfo={_instances:{},blankImage:"",window:{},panel:null,getEditorById:function(e){if(!YAHOO.lang.isString(e)){e=e.id
}if(this._instances[e]){return this._instances[e]
}return false
},toString:function(){var f=0;
for(var e in this._instances){if(b.hasOwnProperty(this._instances,e)){f++
}}return"Editor Info ("+f+" registered intance"+((f>1)?"s":"")+")"
}}
})();
YAHOO.register("simpleeditor",YAHOO.widget.SimpleEditor,{version:"2.6.0",build:"1321"});