(function(){function b(a){do{if(a.className&&a.className.indexOf("mceItemLayer")!=-1){return a
}}while(a=a.parentNode)
}tinymce.create("tinymce.plugins.Layer",{init:function(a,f){var e=this;
e.editor=a;
a.addCommand("mceInsertLayer",e._insertLayer,e);
a.addCommand("mceMoveForward",function(){e._move(1)
});
a.addCommand("mceMoveBackward",function(){e._move(-1)
});
a.addCommand("mceMakeAbsolute",function(){e._toggleAbsolute()
});
a.addButton("moveforward",{title:"layer.forward_desc",cmd:"mceMoveForward"});
a.addButton("movebackward",{title:"layer.backward_desc",cmd:"mceMoveBackward"});
a.addButton("absolute",{title:"layer.absolute_desc",cmd:"mceMakeAbsolute"});
a.addButton("insertlayer",{title:"layer.insertlayer_desc",cmd:"mceInsertLayer"});
a.onInit.add(function(){var c=a.dom;
if(tinymce.isIE){a.getDoc().execCommand("2D-Position",false,true)
}});
a.onMouseUp.add(function(i,c){var d=b(c.target);
if(d){i.dom.setAttrib(d,"data-mce-style","")
}});
a.onMouseDown.add(function(m,c){var k=c.target,d=m.getDoc(),l;
if(tinymce.isGecko){if(b(k)){if(d.designMode!=="on"){d.designMode="on";
k=d.body;
l=k.parentNode;
l.removeChild(k);
l.appendChild(k)
}}else{if(d.designMode=="on"){d.designMode="off"
}}}});
a.onNodeChange.add(e._nodeChange,e);
a.onVisualAid.add(e._visualAid,e)
},getInfo:function(){return{longname:"Layer",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/layer",version:tinymce.majorVersion+"."+tinymce.minorVersion}
},_nodeChange:function(j,a,g){var i,h;
i=this._getParentLayer(g);
h=j.dom.getParent(g,"DIV,P,IMG");
if(!h){a.setDisabled("absolute",1);
a.setDisabled("moveforward",1);
a.setDisabled("movebackward",1)
}else{a.setDisabled("absolute",0);
a.setDisabled("moveforward",!i);
a.setDisabled("movebackward",!i);
a.setActive("absolute",i&&i.style.position.toLowerCase()=="absolute")
}},_visualAid:function(a,g,h){var e=a.dom;
tinymce.each(e.select("div,p",g),function(c){if(/^(absolute|relative|fixed)$/i.test(c.style.position)){if(h){e.addClass(c,"mceItemVisualAid")
}else{e.removeClass(c,"mceItemVisualAid")
}e.addClass(c,"mceItemLayer")
}})
},_move:function(i){var p=this.editor,m,l=[],n=this._getParentLayer(p.selection.getNode()),o=-1,d=-1,a;
a=[];
tinymce.walk(p.getBody(),function(c){if(c.nodeType==1&&/^(absolute|relative|static)$/i.test(c.style.position)){a.push(c)
}},"childNodes");
for(m=0;
m<a.length;
m++){l[m]=a[m].style.zIndex?parseInt(a[m].style.zIndex):0;
if(o<0&&a[m]==n){o=m
}}if(i<0){for(m=0;
m<l.length;
m++){if(l[m]<l[o]){d=m;
break
}}if(d>-1){a[o].style.zIndex=l[d];
a[d].style.zIndex=l[o]
}else{if(l[o]>0){a[o].style.zIndex=l[o]-1
}}}else{for(m=0;
m<l.length;
m++){if(l[m]>l[o]){d=m;
break
}}if(d>-1){a[o].style.zIndex=l[d];
a[d].style.zIndex=l[o]
}else{a[o].style.zIndex=l[o]+1
}}p.execCommand("mceRepaint")
},_getParentLayer:function(a){return this.editor.dom.getParent(a,function(d){return d.nodeType==1&&/^(absolute|relative|static)$/i.test(d.style.position)
})
},_insertLayer:function(){var h=this.editor,f=h.dom,g=f.getPos(f.getParent(h.selection.getNode(),"*")),a=h.getBody();
h.dom.add(a,"div",{style:{position:"absolute",left:g.x,top:(g.y>20?g.y:20),width:100,height:100},"class":"mceItemVisualAid mceItemLayer"},h.selection.getContent()||h.getLang("layer.content"));
if(tinymce.isIE){f.setHTML(a,a.innerHTML)
}},_toggleAbsolute:function(){var a=this.editor,d=this._getParentLayer(a.selection.getNode());
if(!d){d=a.dom.getParent(a.selection.getNode(),"DIV,P,IMG")
}if(d){if(d.style.position.toLowerCase()=="absolute"){a.dom.setStyles(d,{position:"",left:"",top:"",width:"",height:""});
a.dom.removeClass(d,"mceItemVisualAid");
a.dom.removeClass(d,"mceItemLayer")
}else{if(d.style.left==""){d.style.left=20+"px"
}if(d.style.top==""){d.style.top=20+"px"
}if(d.style.width==""){d.style.width=d.width?(d.width+"px"):"100px"
}if(d.style.height==""){d.style.height=d.height?(d.height+"px"):"100px"
}d.style.position="absolute";
a.dom.setAttrib(d,"data-mce-style","");
a.addVisual(a.getBody())
}a.execCommand("mceRepaint");
a.nodeChanged()
}}});
tinymce.PluginManager.add("layer",tinymce.plugins.Layer)
})();