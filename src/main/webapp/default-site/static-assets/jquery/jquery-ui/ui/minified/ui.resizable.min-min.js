(function(f){f.widget("ui.resizable",f.extend({},f.ui.mouse,{_init:function(){var m=this,b=this.options;
this.element.addClass("ui-resizable");
f.extend(this,{_aspectRatio:!!(b.aspectRatio),aspectRatio:b.aspectRatio,originalElement:this.element,_proportionallyResizeElements:[],_helper:b.helper||b.ghost||b.animate?b.helper||"ui-resizable-helper":null});
if(this.element[0].nodeName.match(/canvas|textarea|input|select|button|img/i)){if(/relative/.test(this.element.css("position"))&&f.browser.opera){this.element.css({position:"relative",top:"auto",left:"auto"})
}this.element.wrap(f('<div class="ui-wrapper" style="overflow: hidden;"></div>').css({position:this.element.css("position"),width:this.element.outerWidth(),height:this.element.outerHeight(),top:this.element.css("top"),left:this.element.css("left")}));
this.element=this.element.parent().data("resizable",this.element.data("resizable"));
this.elementIsWrapper=true;
this.element.css({marginLeft:this.originalElement.css("marginLeft"),marginTop:this.originalElement.css("marginTop"),marginRight:this.originalElement.css("marginRight"),marginBottom:this.originalElement.css("marginBottom")});
this.originalElement.css({marginLeft:0,marginTop:0,marginRight:0,marginBottom:0});
this.originalResizeStyle=this.originalElement.css("resize");
this.originalElement.css("resize","none");
this._proportionallyResizeElements.push(this.originalElement.css({position:"static",zoom:1,display:"block"}));
this.originalElement.css({margin:this.originalElement.css("margin")});
this._proportionallyResize()
}this.handles=b.handles||(!f(".ui-resizable-handle",this.element).length?"e,s,se":{n:".ui-resizable-n",e:".ui-resizable-e",s:".ui-resizable-s",w:".ui-resizable-w",se:".ui-resizable-se",sw:".ui-resizable-sw",ne:".ui-resizable-ne",nw:".ui-resizable-nw"});
if(this.handles.constructor==String){if(this.handles=="all"){this.handles="n,e,s,w,se,sw,ne,nw"
}var a=this.handles.split(",");
this.handles={};
for(var l=0;
l<a.length;
l++){var c=f.trim(a[l]),n="ui-resizable-"+c;
var i=f('<div class="ui-resizable-handle '+n+'"></div>');
if(/sw|se|ne|nw/.test(c)){i.css({zIndex:++b.zIndex})
}if("se"==c){i.addClass("ui-icon ui-icon-gripsmall-diagonal-se")
}this.handles[c]=".ui-resizable-"+c;
this.element.append(i)
}}this._renderAxis=function(j){j=j||this.element;
for(var g in this.handles){if(this.handles[g].constructor==String){this.handles[g]=f(this.handles[g],this.element).show()
}if(this.elementIsWrapper&&this.originalElement[0].nodeName.match(/textarea|input|select|button/i)){var q=f(this.handles[g],this.element),k=0;
k=/sw|ne|nw|se|n|s/.test(g)?q.outerHeight():q.outerWidth();
var h=["padding",/ne|nw|n/.test(g)?"Top":/se|sw|s/.test(g)?"Bottom":/^e$/.test(g)?"Right":"Left"].join("");
j.css(h,k);
this._proportionallyResize()
}if(!f(this.handles[g]).length){continue
}}};
this._renderAxis(this.element);
this._handles=f(".ui-resizable-handle",this.element).disableSelection();
this._handles.mouseover(function(){if(!m.resizing){if(this.className){var g=this.className.match(/ui-resizable-(se|sw|ne|nw|n|e|s|w)/i)
}m.axis=g&&g[1]?g[1]:"se"
}});
if(b.autoHide){this._handles.hide();
f(this.element).addClass("ui-resizable-autohide").hover(function(){f(this).removeClass("ui-resizable-autohide");
m._handles.show()
},function(){if(!m.resizing){f(this).addClass("ui-resizable-autohide");
m._handles.hide()
}})
}this._mouseInit()
},destroy:function(){this._mouseDestroy();
var b=function(c){f(c).removeClass("ui-resizable ui-resizable-disabled ui-resizable-resizing").removeData("resizable").unbind(".resizable").find(".ui-resizable-handle").remove()
};
if(this.elementIsWrapper){b(this.element);
var a=this.element;
a.parent().append(this.originalElement.css({position:a.css("position"),width:a.outerWidth(),height:a.outerHeight(),top:a.css("top"),left:a.css("left")})).end().remove()
}this.originalElement.css("resize",this.originalResizeStyle);
b(this.originalElement)
},_mouseCapture:function(b){var a=false;
for(var c in this.handles){if(f(this.handles[c])[0]==b.target){a=true
}}return this.options.disabled||!!a
},_mouseStart:function(l){var b=this.options,m=this.element.position(),n=this.element;
this.resizing=true;
this.documentScroll={top:f(document).scrollTop(),left:f(document).scrollLeft()};
if(n.is(".ui-draggable")||(/absolute/).test(n.css("position"))){n.css({position:"absolute",top:m.top,left:m.left})
}if(f.browser.opera&&(/relative/).test(n.css("position"))){n.css({position:"relative",top:"auto",left:"auto"})
}this._renderProxy();
var a=d(this.helper.css("left")),k=d(this.helper.css("top"));
if(b.containment){a+=f(b.containment).scrollLeft()||0;
k+=f(b.containment).scrollTop()||0
}this.offset=this.helper.offset();
this.position={left:a,top:k};
this.size=this._helper?{width:n.outerWidth(),height:n.outerHeight()}:{width:n.width(),height:n.height()};
this.originalSize=this._helper?{width:n.outerWidth(),height:n.outerHeight()}:{width:n.width(),height:n.height()};
this.originalPosition={left:a,top:k};
this.sizeDiff={width:n.outerWidth()-n.width(),height:n.outerHeight()-n.height()};
this.originalMousePosition={left:l.pageX,top:l.pageY};
this.aspectRatio=(typeof b.aspectRatio=="number")?b.aspectRatio:((this.originalSize.width/this.originalSize.height)||1);
var c=f(".ui-resizable-"+this.axis).css("cursor");
f("body").css("cursor",c=="auto"?this.axis+"-resize":c);
n.addClass("ui-resizable-resizing");
this._propagate("start",l);
return true
},_mouseDrag:function(z){var w=this.helper,x=this.options,r={},b=this,u=this.originalMousePosition,o=this.axis;
var a=(z.pageX-u.left)||0,c=(z.pageY-u.top)||0;
var v=this._change[o];
if(!v){return false
}var s=v.apply(this,[z,a,c]),t=f.browser.msie&&f.browser.version<7,y=this.sizeDiff;
if(this._aspectRatio||z.shiftKey){s=this._updateRatio(s,z)
}s=this._respectSize(s,z);
this._propagate("resize",z);
w.css({top:this.position.top+"px",left:this.position.left+"px",width:this.size.width+"px",height:this.size.height+"px"});
if(!this._helper&&this._proportionallyResizeElements.length){this._proportionallyResize()
}this._updateCache(s);
this._trigger("resize",z,this.ui());
return false
},_mouseStop:function(q){this.resizing=false;
var p=this.options,b=this;
if(this._helper){var r=this._proportionallyResizeElements,t=r.length&&(/textarea/i).test(r[0].nodeName),s=t&&f.ui.hasScroll(r[0],"left")?0:b.sizeDiff.height,n=t?0:b.sizeDiff.width;
var a={width:(b.size.width-n),height:(b.size.height-s)},o=(parseInt(b.element.css("left"),10)+(b.position.left-b.originalPosition.left))||null,c=(parseInt(b.element.css("top"),10)+(b.position.top-b.originalPosition.top))||null;
if(!p.animate){this.element.css(f.extend(a,{top:c,left:o}))
}b.helper.height(b.size.height);
b.helper.width(b.size.width);
if(this._helper&&!p.animate){this._proportionallyResize()
}}f("body").css("cursor","auto");
this.element.removeClass("ui-resizable-resizing");
this._propagate("stop",q);
if(this._helper){this.helper.remove()
}return false
},_updateCache:function(b){var a=this.options;
this.offset=this.helper.offset();
if(e(b.left)){this.position.left=b.left
}if(e(b.top)){this.position.top=b.top
}if(e(b.height)){this.size.height=b.height
}if(e(b.width)){this.size.width=b.width
}},_updateRatio:function(c,j){var b=this.options,a=this.position,k=this.size,l=this.axis;
if(c.height){c.width=(k.height*this.aspectRatio)
}else{if(c.width){c.height=(k.width/this.aspectRatio)
}}if(l=="sw"){c.left=a.left+(k.width-c.width);
c.top=null
}if(l=="nw"){c.top=a.top+(k.height-c.height);
c.left=a.left+(k.width-c.width)
}return c
},_respectSize:function(v,A){var x=this.helper,y=this.options,b=this._aspectRatio||A.shiftKey,c=this.axis,D=e(v.width)&&y.maxWidth&&(y.maxWidth<v.width),u=e(v.height)&&y.maxHeight&&(y.maxHeight<v.height),z=e(v.width)&&y.minWidth&&(y.minWidth>v.width),a=e(v.height)&&y.minHeight&&(y.minHeight>v.height);
if(z){v.width=y.minWidth
}if(a){v.height=y.minHeight
}if(D){v.width=y.maxWidth
}if(u){v.height=y.maxHeight
}var B=this.originalPosition.left+this.originalSize.width,o=this.position.top+this.size.height;
var w=/sw|nw|w/.test(c),C=/nw|ne|n/.test(c);
if(z&&w){v.left=B-y.minWidth
}if(D&&w){v.left=B-y.maxWidth
}if(a&&C){v.top=o-y.minHeight
}if(u&&C){v.top=o-y.maxHeight
}var t=!v.width&&!v.height;
if(t&&!v.left&&v.top){v.top=null
}else{if(t&&!v.top&&v.left){v.left=null
}}return v
},_proportionallyResize:function(){var a=this.options;
if(!this._proportionallyResizeElements.length){return
}var i=this.helper||this.element;
for(var k=0;
k<this._proportionallyResizeElements.length;
k++){var c=this._proportionallyResizeElements[k];
if(!this.borderDif){var l=[c.css("borderTopWidth"),c.css("borderRightWidth"),c.css("borderBottomWidth"),c.css("borderLeftWidth")],b=[c.css("paddingTop"),c.css("paddingRight"),c.css("paddingBottom"),c.css("paddingLeft")];
this.borderDif=f.map(l,function(j,g){var h=parseInt(j,10)||0,o=parseInt(b[g],10)||0;
return h+o
})
}if(f.browser.msie&&!(!(f(i).is(":hidden")||f(i).parents(":hidden").length))){continue
}c.css({height:(i.height()-this.borderDif[0]-this.borderDif[2])||0,width:(i.width()-this.borderDif[1]-this.borderDif[3])||0})
}},_renderProxy:function(){var i=this.element,a=this.options;
this.elementOffset=i.offset();
if(this._helper){this.helper=this.helper||f('<div style="overflow:hidden;"></div>');
var j=f.browser.msie&&f.browser.version<7,c=(j?1:0),b=(j?2:-1);
this.helper.addClass(this._helper).css({width:this.element.outerWidth()+b,height:this.element.outerHeight()+b,position:"absolute",left:this.elementOffset.left-c+"px",top:this.elementOffset.top-c+"px",zIndex:++a.zIndex});
this.helper.appendTo("body").disableSelection()
}else{this.helper=this.element
}},_change:{e:function(a,b,c){return{width:this.originalSize.width+b}
},w:function(c,k,l){var a=this.options,j=this.originalSize,b=this.originalPosition;
return{left:b.left+k,width:j.width-k}
},n:function(c,k,l){var a=this.options,j=this.originalSize,b=this.originalPosition;
return{top:b.top+l,height:j.height-l}
},s:function(a,b,c){return{height:this.originalSize.height+c}
},se:function(a,b,c){return f.extend(this._change.s.apply(this,arguments),this._change.e.apply(this,[a,b,c]))
},sw:function(a,b,c){return f.extend(this._change.s.apply(this,arguments),this._change.w.apply(this,[a,b,c]))
},ne:function(a,b,c){return f.extend(this._change.n.apply(this,arguments),this._change.e.apply(this,[a,b,c]))
},nw:function(a,b,c){return f.extend(this._change.n.apply(this,arguments),this._change.w.apply(this,[a,b,c]))
}},_propagate:function(a,b){f.ui.plugin.call(this,a,[b,this.ui()]);
(a!="resize"&&this._trigger(a,b,this.ui()))
},plugins:{},ui:function(){return{originalElement:this.originalElement,element:this.element,helper:this.helper,position:this.position,size:this.size,originalSize:this.originalSize,originalPosition:this.originalPosition}
}}));
f.extend(f.ui.resizable,{version:"1.7.2",eventPrefix:"resize",defaults:{alsoResize:false,animate:false,animateDuration:"slow",animateEasing:"swing",aspectRatio:false,autoHide:false,cancel:":input,option",containment:false,delay:0,distance:1,ghost:false,grid:false,handles:"e,s,se",helper:false,maxHeight:null,maxWidth:null,minHeight:10,minWidth:10,zIndex:1000}});
f.ui.plugin.add("resizable","alsoResize",{start:function(c,b){var h=f(this).data("resizable"),a=h.options;
_store=function(g){f(g).each(function(){f(this).data("resizable-alsoresize",{width:parseInt(f(this).width(),10),height:parseInt(f(this).height(),10),left:parseInt(f(this).css("left"),10),top:parseInt(f(this).css("top"),10)})
})
};
if(typeof(a.alsoResize)=="object"&&!a.alsoResize.parentNode){if(a.alsoResize.length){a.alsoResize=a.alsoResize[0];
_store(a.alsoResize)
}else{f.each(a.alsoResize,function(j,g){_store(j)
})
}}else{_store(a.alsoResize)
}},resize:function(n,l){var o=f(this).data("resizable"),c=o.options,m=o.originalSize,a=o.originalPosition;
var b={height:(o.size.height-m.height)||0,width:(o.size.width-m.width)||0,top:(o.position.top-a.top)||0,left:(o.position.left-a.left)||0},p=function(h,g){f(h).each(function(){var j=f(this),i=f(this).data("resizable-alsoresize"),k={},r=g&&g.length?g:["width","height","top","left"];
f.each(r||["width","height","top","left"],function(v,q){var u=(i[q]||0)+(b[q]||0);
if(u&&u>=0){k[q]=u||null
}});
if(/relative/.test(j.css("position"))&&f.browser.opera){o._revertToRelativePosition=true;
j.css({position:"absolute",top:"auto",left:"auto"})
}j.css(k)
})
};
if(typeof(c.alsoResize)=="object"&&!c.alsoResize.nodeType){f.each(c.alsoResize,function(h,g){p(h,g)
})
}else{p(c.alsoResize)
}},stop:function(b,a){var c=f(this).data("resizable");
if(c._revertToRelativePosition&&f.browser.opera){c._revertToRelativePosition=false;
el.css({position:"relative"})
}f(this).removeData("resizable-alsoresize-start")
}});
f.ui.plugin.add("resizable","animate",{stop:function(r,b){var a=f(this).data("resizable"),q=a.options;
var s=a._proportionallyResizeElements,v=s.length&&(/textarea/i).test(s[0].nodeName),u=v&&f.ui.hasScroll(s[0],"left")?0:a.sizeDiff.height,o=v?0:a.sizeDiff.width;
var t={width:(a.size.width-o),height:(a.size.height-u)},p=(parseInt(a.element.css("left"),10)+(a.position.left-a.originalPosition.left))||null,c=(parseInt(a.element.css("top"),10)+(a.position.top-a.originalPosition.top))||null;
a.element.animate(f.extend(t,c&&p?{top:c,left:p}:{}),{duration:q.animateDuration,easing:q.animateEasing,step:function(){var g={width:parseInt(a.element.css("width"),10),height:parseInt(a.element.css("height"),10),top:parseInt(a.element.css("top"),10),left:parseInt(a.element.css("left"),10)};
if(s&&s.length){f(s[0]).css({width:g.width,height:g.height})
}a._updateCache(g);
a._propagate("resize",r)
}})
}});
f.ui.plugin.add("resizable","containment",{start:function(z,b){var B=f(this).data("resizable"),v=B.options,t=B.element;
var y=v.containment,u=(y instanceof f)?y.get(0):(/parent/.test(y))?t.parent().get(0):y;
if(!u){return
}B.containerElement=f(u);
if(/document/.test(y)||y==document){B.containerOffset={left:0,top:0};
B.containerPosition={left:0,top:0};
B.parentData={element:f(document),left:0,top:0,width:f(document).width(),height:f(document).height()||document.body.parentNode.scrollHeight}
}else{var o=f(u),w=[];
f(["Top","Right","Left","Bottom"]).each(function(g,h){w[g]=d(o.css("padding"+h))
});
B.containerOffset=o.offset();
B.containerPosition=o.position();
B.containerSize={height:(o.innerHeight()-w[3]),width:(o.innerWidth()-w[1])};
var c=B.containerOffset,A=B.containerSize.height,p=B.containerSize.width,x=(f.ui.hasScroll(u,"left")?u.scrollWidth:p),a=(f.ui.hasScroll(u)?u.scrollHeight:A);
B.parentData={element:u,left:c.left,top:c.top,width:x,height:a}
}},resize:function(A,c){var D=f(this).data("resizable"),y=D.options,B=D.containerSize,o=D.containerOffset,u=D.size,t=D.position,b=D._aspectRatio||A.shiftKey,C={top:0,left:0},z=D.containerElement;
if(z[0]!=document&&(/static/).test(z.css("position"))){C=o
}if(t.left<(D._helper?o.left:0)){D.size.width=D.size.width+(D._helper?(D.position.left-o.left):(D.position.left-C.left));
if(b){D.size.height=D.size.width/y.aspectRatio
}D.position.left=y.helper?o.left:0
}if(t.top<(D._helper?o.top:0)){D.size.height=D.size.height+(D._helper?(D.position.top-o.top):D.position.top);
if(b){D.size.width=D.size.height*y.aspectRatio
}D.position.top=D._helper?o.top:0
}D.offset.left=D.parentData.left+D.position.left;
D.offset.top=D.parentData.top+D.position.top;
var v=Math.abs((D._helper?D.offset.left-C.left:(D.offset.left-C.left))+D.sizeDiff.width),a=Math.abs((D._helper?D.offset.top-C.top:(D.offset.top-o.top))+D.sizeDiff.height);
var w=D.containerElement.get(0)==D.element.parent().get(0),x=/relative|absolute/.test(D.containerElement.css("position"));
if(w&&x){v-=D.parentData.left
}if(v+D.size.width>=D.parentData.width){D.size.width=D.parentData.width-v;
if(b){D.size.height=D.size.width/D.aspectRatio
}}if(a+D.size.height>=D.parentData.height){D.size.height=D.parentData.height-a;
if(b){D.size.width=D.size.height*D.aspectRatio
}}},stop:function(w,h){var b=f(this).data("resizable"),v=b.options,r=b.position,o=b.containerOffset,x=b.containerPosition,u=b.containerElement;
var t=f(b.helper),a=t.offset(),c=t.outerWidth()-b.sizeDiff.width,s=t.outerHeight()-b.sizeDiff.height;
if(b._helper&&!v.animate&&(/relative/).test(u.css("position"))){f(this).css({left:a.left-x.left-o.left,width:c,height:s})
}if(b._helper&&!v.animate&&(/static/).test(u.css("position"))){f(this).css({left:a.left-x.left-o.left,width:c,height:s})
}}});
f.ui.plugin.add("resizable","ghost",{start:function(c,b){var j=f(this).data("resizable"),a=j.options,i=j.size;
j.ghost=j.originalElement.clone();
j.ghost.css({opacity:0.25,display:"block",position:"relative",height:i.height,width:i.width,margin:0,left:0,top:0}).addClass("ui-resizable-ghost").addClass(typeof a.ghost=="string"?a.ghost:"");
j.ghost.appendTo(j.helper)
},resize:function(c,b){var h=f(this).data("resizable"),a=h.options;
if(h.ghost){h.ghost.css({position:"relative",height:h.size.height,width:h.size.width})
}},stop:function(c,b){var h=f(this).data("resizable"),a=h.options;
if(h.ghost&&h.helper){h.helper.get(0).removeChild(h.ghost.get(0))
}}});
f.ui.plugin.add("resizable","grid",{resize:function(v,c){var a=f(this).data("resizable"),s=a.options,p=a.size,r=a.originalSize,q=a.originalPosition,b=a.axis,o=s._aspectRatio||v.shiftKey;
s.grid=typeof s.grid=="number"?[s.grid,s.grid]:s.grid;
var t=Math.round((p.width-r.width)/(s.grid[0]||1))*(s.grid[0]||1),u=Math.round((p.height-r.height)/(s.grid[1]||1))*(s.grid[1]||1);
if(/^(se|s|e)$/.test(b)){a.size.width=r.width+t;
a.size.height=r.height+u
}else{if(/^(ne)$/.test(b)){a.size.width=r.width+t;
a.size.height=r.height+u;
a.position.top=q.top-u
}else{if(/^(sw)$/.test(b)){a.size.width=r.width+t;
a.size.height=r.height+u;
a.position.left=q.left-t
}else{a.size.width=r.width+t;
a.size.height=r.height+u;
a.position.top=q.top-u;
a.position.left=q.left-t
}}}}});
var d=function(a){return parseInt(a,10)||0
};
var e=function(a){return !isNaN(parseInt(a,10))
}
})(jQuery);