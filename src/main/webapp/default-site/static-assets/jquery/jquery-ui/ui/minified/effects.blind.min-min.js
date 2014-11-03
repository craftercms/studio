(function(b){b.effects.blind=function(a){return this.queue(function(){var q=b(this),r=["position","top","left"];
var m=b.effects.setMode(q,a.options.mode||"hide");
var n=a.options.direction||"vertical";
b.effects.save(q,r);
q.show();
var k=b.effects.createWrapper(q).css({overflow:"hidden"});
var p=(n=="vertical")?"height":"width";
var l=(n=="vertical")?k.height():k.width();
if(m=="show"){k.css(p,0)
}var o={};
o[p]=m=="show"?l:0;
k.animate(o,a.duration,a.options.easing,function(){if(m=="hide"){q.hide()
}b.effects.restore(q,r);
b.effects.removeWrapper(q);
if(a.callback){a.callback.apply(q[0],arguments)
}q.dequeue()
})
})
}
})(jQuery);