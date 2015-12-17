(function(){var i=tinymce.DOM,f=tinymce.dom.Element,g=tinymce.dom.Event,h=tinymce.each,j=tinymce.is;
tinymce.create("tinymce.plugins.CStudioInlinePopups",{init:function(b,a){b.onBeforeRenderUI.add(function(){b.windowManager=new tinymce.InlineWindowManager(b);
i.loadCSS(a+"/skins/"+(b.settings.inlinepopups_skin||"clearlooks2")+"/window.css")
})
},getInfo:function(){return{longname:"Crafter Studio Inline Popups Customization",author:"Crafter Software",authorurl:"http://www.craftercms.org",infourl:"http://www.craftercms.org",version:"1.0"}
}});
tinymce.create("tinymce.InlineWindowManager:tinymce.WindowManager",{InlineWindowManager:function(b){var a=this;
a.parent(b);
a.zIndex=300000;
a.count=0;
a.windows={}
},open:function(u,d){var t=this,c,M="",I=t.editor,w=0,D=0,a,b,K,H,G,F,E,p,J,L,e;
u=u||{};
d=d||{};
if(!u.inline){return t.parent(u,d)
}p=t._frontWindow();
if(p&&i.get(p.id+"_ifr")){p.focussedElement=i.get(p.id+"_ifr").contentWindow.document.activeElement
}if(!u.type){t.bookmark=I.selection.getBookmark(1)
}c=i.uniqueId();
a=i.getViewPort();
u.width=parseInt(u.width||320);
L=u.url||"";
J=(!L||L.lastIndexOf("/")==-1)?"":L.substring(L.lastIndexOf("/")+1);
switch(J){case"color_picker.htm":u.height=270;
u.opaque=true;
break;
case"link.htm":u.height=244;
d.authoringObj=CStudioAuthoring;
d.contextObj=CStudioAuthoringContext;
break;
case"anchor.htm":u.height=148;
break;
case"table.htm":u.height=370;
break;
case"row.htm":u.height=295;
break;
case"cell.htm":u.height=320;
break;
case"image.htm":u.height=295;
d.contextObj=CStudioAuthoringContext;
break;
case"merge_cells.htm":u.height=180;
break;
default:u.height=u.height||100
}u.height+=tinymce.isIE?8:0;
u.min_width=parseInt(u.min_width||150);
u.min_height=parseInt(u.min_height||100);
u.max_width=parseInt(u.max_width||2000);
u.max_height=parseInt(u.max_height||2000);
u.left=u.left||Math.round(Math.max(a.x,a.x+(a.w/2)-(u.width/2)));
u.top=u.top||Math.round(Math.max(a.y,a.y+(a.h/2)-(u.height/2)));
u.movable=u.resizable=false;
d.mce_width=u.width;
d.mce_height=u.height;
d.mce_inline=true;
d.mce_window_id=c;
d.mce_auto_focus=u.auto_focus;
t.features=u;
t.params=d;
t.onOpen.dispatch(t,u,d);
t.mouseEvent=false;
if(u.type){M+=" mceModal";
if(u.type){M+=" mce"+u.type.substring(0,1).toUpperCase()+u.type.substring(1)
}u.resizable=false
}if(u.statusbar){M+=" mceStatusbar"
}if(u.resizable){M+=" mceResizable"
}if(u.minimizable){M+=" mceMinimizable"
}if(u.maximizable){M+=" mceMaximizable"
}if(u.movable){M+=" mceMovable"
}t._addAll(i.doc.body,["div",{id:c,role:"dialog","aria-labelledby":u.type?c+"_content":c+"_title","class":(u.opaque?"opaque":"")+" "+(I.settings.inlinepopups_skin||"clearlooks2")+(tinymce.isIE&&window.getSelection?" ie9":""),style:"width:100px;height:100px"},["div",{id:c+"_wrapper","class":"mceWrapper"+M},["div",{id:c+"_top","class":"mceTop"},["div",{"class":"mceLeft"}],["div",{"class":"mceCenter"}],["div",{"class":"mceRight"}],["span",{id:c+"_title"},u.title||""]],["div",{id:c+"_middle","class":"mceMiddle"},["div",{id:c+"_left","class":"mceLeft",tabindex:"0"}],["span",{id:c+"_content"}],["div",{id:c+"_right","class":"mceRight",tabindex:"0"}]],["div",{id:c+"_bottom","class":"mceBottom"},["div",{"class":"mceLeft"}],["div",{"class":"mceCenter"}],["div",{"class":"mceRight"}],["span",{id:c+"_status"},"Content"]],["a",{"class":"mceMove",tabindex:"-1",href:"javascript:;"}],["a",{"class":"mceMin",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceMax",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceMed",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceClose",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{id:c+"_resize_n","class":"mceResize mceResizeN",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_s","class":"mceResize mceResizeS",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_w","class":"mceResize mceResizeW",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_e","class":"mceResize mceResizeE",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_nw","class":"mceResize mceResizeNW",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_ne","class":"mceResize mceResizeNE",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_sw","class":"mceResize mceResizeSW",tabindex:"-1",href:"javascript:;"}],["a",{id:c+"_resize_se","class":"mceResize mceResizeSE",tabindex:"-1",href:"javascript:;"}]]]);
i.setStyles(c,{top:-10000,left:-10000});
if(tinymce.isGecko){i.setStyle(c,"overflow","auto")
}if(!u.type){w+=i.get(c+"_left").clientWidth;
w+=i.get(c+"_right").clientWidth;
D+=i.get(c+"_top").clientHeight;
D+=i.get(c+"_bottom").clientHeight
}i.setStyles(c,{top:u.top,left:u.left,width:u.width+w,height:u.height+D});
E=u.url||u.file;
if(E){if(tinymce.relaxedDomain){E+=(E.indexOf("?")==-1?"?":"&")+"mce_rdomain="+tinymce.relaxedDomain
}E=tinymce._addVer(E)
}if(!u.type){i.add(c+"_content","iframe",{id:c+"_ifr",src:'javascript:""',frameBorder:0,style:"border:0;width:10px;height:10px"});
i.setStyles(c+"_ifr",{width:"100%",height:u.height});
i.setAttrib(c+"_ifr","src",E);
amplify.subscribe("/rte/clicked",u.clickSubscription=function(){var l,k=document.getElementById(c+"_ifr");
if(k){l=k.contentDocument.getElementById("cancel");
if(l){t.mouseEvent=true;
l.click()
}}});
amplify.subscribe("/rte/blurred",u.blurSubscription=function(){var l,k=document.getElementById(c+"_ifr");
if(k){l=k.contentDocument.getElementById("cancel");
if(l){t.mouseEvent=true;
l.click()
}}})
}else{i.add(c+"_wrapper","a",{id:c+"_ok","class":"mceButton mceOk",href:"javascript:;",onmousedown:"return false;"},"Ok");
if(u.type=="confirm"){i.add(c+"_wrapper","a",{"class":"mceButton mceCancel",href:"javascript:;",onmousedown:"return false;"},"Cancel")
}i.add(c+"_middle","div",{"class":"mceIcon"});
i.setHTML(c+"_content",u.content.replace("\n","<br />"));
g.add(c,"keyup",function(k){var l=27;
if(k.keyCode===l){u.button_func(false);
return g.cancel(k)
}});
g.add(c,"keydown",function(l){var k,m=9;
if(l.keyCode===m){k=i.select("a.mceCancel",c+"_wrapper")[0];
if(k&&k!==l.target){k.focus()
}else{i.get(c+"_ok").focus()
}return g.cancel(l)
}})
}K=g.add(c,"mousedown",function(l){var k=l.target,m,n;
m=t.windows[c];
t.focus(c);
if(k.nodeName=="A"||k.nodeName=="a"){if(k.className=="mceMax"){m.oldPos=m.element.getXY();
m.oldSize=m.element.getSize();
n=i.getViewPort();
n.w-=2;
n.h-=2;
m.element.moveTo(n.x,n.y);
m.element.resizeTo(n.w,n.h);
i.setStyles(c+"_ifr",{width:n.w-m.deltaWidth,height:n.h-m.deltaHeight});
i.addClass(c+"_wrapper","mceMaximized")
}else{if(k.className=="mceMed"){m.element.moveTo(m.oldPos.x,m.oldPos.y);
m.element.resizeTo(m.oldSize.w,m.oldSize.h);
m.iframeElement.resizeTo(m.oldSize.w-m.deltaWidth,m.oldSize.h-m.deltaHeight);
i.removeClass(c+"_wrapper","mceMaximized")
}else{if(k.className=="mceMove"){return t._startDrag(c,l,k.className)
}else{if(i.hasClass(k,"mceResize")){return t._startDrag(c,l,k.className.substring(13))
}}}}}});
H=g.add(c,"click",function(k){var l=k.target;
t.focus(c);
if(l.nodeName=="A"||l.nodeName=="a"){switch(l.className){case"mceClose":t.close(null,c);
return g.cancel(k);
case"mceButton mceOk":case"mceButton mceCancel":u.button_func(l.className=="mceButton mceOk");
return g.cancel(k)
}}});
g.add([c+"_left",c+"_right"],"focus",function(n){var l=i.get(c+"_ifr");
if(l){var m=l.contentWindow.document.body;
var k=i.select(":input:enabled,*[tabindex=0]",m);
if(n.target.id===(c+"_left")){k[k.length-1].focus()
}else{k[0].focus()
}}else{i.get(c+"_ok").focus()
}});
F=t.windows[c]={id:c,mousedown_func:K,click_func:H,element:new f(c,{blocker:1,container:I.getContainer()}),iframeElement:new f(c+"_ifr"),features:u,deltaWidth:w,deltaHeight:D};
F.iframeElement.on("focus",function(){t.focus(c)
});
if(t.count==0&&t.editor.getParam("dialog_type","modal")=="modal"){i.add(i.doc.body,"div",{id:"mceModalBlocker","class":(t.editor.settings.inlinepopups_skin||"clearlooks2")+"_modalBlocker",style:{zIndex:t.zIndex-1}});
i.hide("mceModalBlocker");
i.setAttrib(i.doc.body,"aria-hidden","true")
}else{i.setStyle("mceModalBlocker","z-index",t.zIndex-1);
i.show("mceModalBlocker")
}if(tinymce.isIE6||/Firefox\/2\./.test(navigator.userAgent)||(tinymce.isIE&&!i.boxModel)){i.setStyles("mceModalBlocker",{position:"absolute",left:a.x,top:a.y,width:a.w-2,height:a.h-2})
}i.setAttrib(c,"aria-hidden","false");
t.focus(c);
t._fixIELayout(c,1);
if(i.get(c+"_ok")){i.get(c+"_ok").focus()
}t.count++;
return F
},focus:function(a){var b=this,c;
if(c=b.windows[a]){c.zIndex=this.zIndex++;
c.element.setStyle("zIndex",c.zIndex);
c.element.update();
a=a+"_wrapper";
i.removeClass(b.lastId,"mceFocus");
i.addClass(a,"mceFocus");
b.lastId=a;
if(c.focussedElement){c.focussedElement.focus()
}else{if(i.get(a+"_ok")){i.get(c.id+"_ok").focus()
}else{if(i.get(c.id+"_ifr")){i.get(c.id+"_ifr").focus()
}}}}},_addAll:function(b,d){var e,a,m=this,c=tinymce.DOM;
if(j(d,"string")){b.appendChild(c.doc.createTextNode(d))
}else{if(d.length){b=b.appendChild(c.create(d[0],d[1]));
for(e=2;
e<d.length;
e++){m._addAll(b,d[e])
}}}},_startDrag:function(e,d,w){var O=this,t,a,K=i.doc,X,R=O.windows[e],V=R.element,b=V.getXY(),c,M,p,W,N,I,J,T,U,Q,S,P,L;
W={x:0,y:0};
N=i.getViewPort();
N.w-=2;
N.h-=2;
T=d.screenX;
U=d.screenY;
Q=S=P=L=0;
t=g.add(K,"mouseup",function(k){g.remove(K,"mouseup",t);
g.remove(K,"mousemove",a);
if(X){X.remove()
}V.moveBy(Q,S);
V.resizeBy(P,L);
M=V.getSize();
i.setStyles(e+"_ifr",{width:M.w-R.deltaWidth,height:M.h-R.deltaHeight});
O._fixIELayout(e,1);
return g.cancel(k)
});
if(w!="Move"){H()
}function H(){if(X){return
}O._fixIELayout(e,0);
i.add(K.body,"div",{id:"mceEventBlocker","class":"mceEventBlocker "+(O.editor.settings.inlinepopups_skin||"clearlooks2"),style:{zIndex:O.zIndex+1}});
if(tinymce.isIE6||(tinymce.isIE&&!i.boxModel)){i.setStyles("mceEventBlocker",{position:"absolute",left:N.x,top:N.y,width:N.w-2,height:N.h-2})
}X=new f("mceEventBlocker");
X.update();
c=V.getXY();
M=V.getSize();
I=W.x+c.x-N.x;
J=W.y+c.y-N.y;
i.add(X.get(),"div",{id:"mcePlaceHolder","class":"mcePlaceHolder",style:{left:I,top:J,width:M.w,height:M.h}});
p=new f("mcePlaceHolder")
}a=g.add(K,"mousemove",function(k){var n,m,l;
H();
n=k.screenX-T;
m=k.screenY-U;
switch(w){case"ResizeW":Q=n;
P=0-n;
break;
case"ResizeE":P=n;
break;
case"ResizeN":case"ResizeNW":case"ResizeNE":if(w=="ResizeNW"){Q=n;
P=0-n
}else{if(w=="ResizeNE"){P=n
}}S=m;
L=0-m;
break;
case"ResizeS":case"ResizeSW":case"ResizeSE":if(w=="ResizeSW"){Q=n;
P=0-n
}else{if(w=="ResizeSE"){P=n
}}L=m;
break;
case"mceMove":Q=n;
S=m;
break
}if(P<(l=R.features.min_width-M.w)){if(Q!==0){Q+=P-l
}P=l
}if(L<(l=R.features.min_height-M.h)){if(S!==0){S+=L-l
}L=l
}P=Math.min(P,R.features.max_width-M.w);
L=Math.min(L,R.features.max_height-M.h);
Q=Math.max(Q,N.x-(I+N.x));
S=Math.max(S,N.y-(J+N.y));
Q=Math.min(Q,(N.w+N.x)-(I+M.w+N.x));
S=Math.min(S,(N.h+N.y)-(J+M.h+N.y));
if(Q+S!==0){if(I+Q<0){Q=0
}if(J+S<0){S=0
}p.moveTo(I+Q,J+S)
}if(P+L!==0){p.resizeTo(M.w+P,M.h+L)
}return g.cancel(k)
});
return g.cancel(d)
},resizeBy:function(c,b,a){var d=this.windows[a];
if(d){d.element.resizeBy(c,b);
d.iframeElement.resizeBy(c,b)
}},close:function(c,a){var e=this,l,b=i.doc,d,a;
a=e._findId(a||c);
l=e.windows[a];
if(!l){e.parent(c);
return
}else{if(l.features&&!l.features.type){amplify.unsubscribe("/rte/clicked",l.features.clickSubscription);
amplify.unsubscribe("/rte/blurred",l.features.blurSubscription)
}}e.count--;
if(e.count==0){i.remove("mceModalBlocker");
i.setAttrib(i.doc.body,"aria-hidden","false");
if(!e.mouseEvent){e.editor.focus()
}}else{if(e.count==1){i.hide("mceModalBlocker")
}}if(l){e.onClose.dispatch(e);
g.remove(b,"mousedown",l.mousedownFunc);
g.remove(b,"click",l.clickFunc);
g.clear(a);
g.clear(a+"_ifr");
i.setAttrib(a+"_ifr","src",'javascript:""');
l.element.remove();
delete e.windows[a];
d=e._frontWindow();
if(d){e.focus(d.id)
}}e.mouseEvent=false
},_frontWindow:function(){var a,b=0;
h(this.windows,function(c){if(c.zIndex>b){a=c;
b=c.zIndex
}});
return a
},setTitle:function(c,b){var a;
c=this._findId(c);
if(a=i.get(c+"_title")){a.innerHTML=i.encode(b)
}},alert:function(d,e,a){var b=this,c;
c=b.open({title:b,type:"alert",button_func:function(l){if(e){e.call(l||b,l)
}b.close(null,c.id)
},content:i.encode(b.editor.getLang(d,d)),inline:1,width:400,height:130})
},confirm:function(d,e,a){var b=this,c;
c=b.open({title:b,type:"confirm",button_func:function(l){if(e){e.call(l||b,l)
}b.close(null,c.id)
},content:i.encode(b.editor.getLang(d,d)),inline:1,width:400,height:130})
},_findId:function(b){var a=this;
if(typeof(b)=="string"){return b
}h(a.windows,function(d){var c=i.get(d.id+"_ifr");
if(c&&b==c.contentWindow){b=d.id;
return false
}});
return b
},_fixIELayout:function(a,b){var d,c;
if(!tinymce.isIE6){return
}h(["n","s","w","e","nw","ne","sw","se"],function(l){var e=i.get(a+"_resize_"+l);
i.setStyles(e,{width:b?e.clientWidth:"",height:b?e.clientHeight:"",cursor:i.getStyle(e,"cursor",1)});
i.setStyle(a+"_bottom","bottom","-1px");
e=0
});
if(d=this.windows[a]){d.element.hide();
d.element.show();
h(i.select("div,a",a),function(e,l){if(e.currentStyle.backgroundImage!="none"){c=new Image();
c.src=e.currentStyle.backgroundImage.replace(/url\(\"(.+)\"\)/,"$1")
}});
i.get(a).style.filter=""
}}});
tinymce.PluginManager.add("cs_inlinepopups",tinymce.plugins.CStudioInlinePopups)
})();