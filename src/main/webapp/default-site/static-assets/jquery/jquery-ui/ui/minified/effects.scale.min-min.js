(function(b){b.effects.puff=function(a){return this.queue(function(){var k=b(this);
var n=b.extend(true,{},a.options);
var i=b.effects.setMode(k,a.options.mode||"hide");
var j=parseInt(a.options.percent,10)||150;
n.fade=true;
var l={height:k.height(),width:k.width()};
var m=j/100;
k.from=(i=="hide")?l:{height:l.height*m,width:l.width*m};
n.from=k.from;
n.percent=(i=="hide")?j:100;
n.mode=i;
k.effect("scale",n,a.duration,a.callback);
k.dequeue()
})
};
b.effects.scale=function(a){return this.queue(function(){var n=b(this);
var q=b.extend(true,{},a.options);
var k=b.effects.setMode(n,a.options.mode||"effect");
var m=parseInt(a.options.percent,10)||(parseInt(a.options.percent,10)==0?0:(k=="hide"?0:100));
var l=a.options.direction||"both";
var r=a.options.origin;
if(k!="effect"){q.origin=r||["middle","center"];
q.restore=true
}var o={height:n.height(),width:n.width()};
n.from=a.options.from||(k=="show"?{height:0,width:0}:o);
var p={y:l!="horizontal"?(m/100):1,x:l!="vertical"?(m/100):1};
n.to={height:o.height*p.y,width:o.width*p.x};
if(a.options.fade){if(k=="show"){n.from.opacity=0;
n.to.opacity=1
}if(k=="hide"){n.from.opacity=1;
n.to.opacity=0
}}q.from=n.from;
q.to=n.to;
q.mode=k;
n.effect("size",q,a.duration,a.callback);
n.dequeue()
})
};
b.effects.size=function(a){return this.queue(function(){var D=b(this),s=["position","top","left","width","height","overflow","opacity"];
var t=["position","top","left","overflow","opacity"];
var w=["width","height","overflow"];
var q=["fontSize"];
var v=["borderTopWidth","borderBottomWidth","paddingTop","paddingBottom"];
var A=["borderLeftWidth","borderRightWidth","paddingLeft","paddingRight"];
var z=b.effects.setMode(D,a.options.mode||"effect");
var x=a.options.restore||false;
var B=a.options.scale||"both";
var r=a.options.origin;
var C={height:D.height(),width:D.width()};
D.from=a.options.from||C;
D.to=a.options.to||C;
if(r){var y=b.effects.getBaseline(r,C);
D.from.top=(C.height-D.from.height)*y.y;
D.from.left=(C.width-D.from.width)*y.x;
D.to.top=(C.height-D.to.height)*y.y;
D.to.left=(C.width-D.to.width)*y.x
}var u={from:{y:D.from.height/C.height,x:D.from.width/C.width},to:{y:D.to.height/C.height,x:D.to.width/C.width}};
if(B=="box"||B=="both"){if(u.from.y!=u.to.y){s=s.concat(v);
D.from=b.effects.setTransition(D,v,u.from.y,D.from);
D.to=b.effects.setTransition(D,v,u.to.y,D.to)
}if(u.from.x!=u.to.x){s=s.concat(A);
D.from=b.effects.setTransition(D,A,u.from.x,D.from);
D.to=b.effects.setTransition(D,A,u.to.x,D.to)
}}if(B=="content"||B=="both"){if(u.from.y!=u.to.y){s=s.concat(q);
D.from=b.effects.setTransition(D,q,u.from.y,D.from);
D.to=b.effects.setTransition(D,q,u.to.y,D.to)
}}b.effects.save(D,x?s:t);
D.show();
b.effects.createWrapper(D);
D.css("overflow","hidden").css(D.from);
if(B=="content"||B=="both"){v=v.concat(["marginTop","marginBottom"]).concat(q);
A=A.concat(["marginLeft","marginRight"]);
w=s.concat(v).concat(A);
D.find("*[width]").each(function(){child=b(this);
if(x){b.effects.save(child,w)
}var c={height:child.height(),width:child.width()};
child.from={height:c.height*u.from.y,width:c.width*u.from.x};
child.to={height:c.height*u.to.y,width:c.width*u.to.x};
if(u.from.y!=u.to.y){child.from=b.effects.setTransition(child,v,u.from.y,child.from);
child.to=b.effects.setTransition(child,v,u.to.y,child.to)
}if(u.from.x!=u.to.x){child.from=b.effects.setTransition(child,A,u.from.x,child.from);
child.to=b.effects.setTransition(child,A,u.to.x,child.to)
}child.css(child.from);
child.animate(child.to,a.duration,a.options.easing,function(){if(x){b.effects.restore(child,w)
}})
})
}D.animate(D.to,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(z=="hide"){D.hide()
}b.effects.restore(D,x?s:t);
b.effects.removeWrapper(D);
if(a.callback){a.callback.apply(this,arguments)
}D.dequeue()
}})
})
}
})(jQuery);