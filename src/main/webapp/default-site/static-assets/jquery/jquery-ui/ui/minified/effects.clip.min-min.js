(function(b){b.effects.clip=function(a){return this.queue(function(){var q=b(this),m=["position","top","left","height","width"];
var n=b.effects.setMode(q,a.options.mode||"hide");
var l=a.options.direction||"vertical";
b.effects.save(q,m);
q.show();
var t=b.effects.createWrapper(q).css({overflow:"hidden"});
var r=q[0].tagName=="IMG"?t:q;
var p={size:(l=="vertical")?"height":"width",position:(l=="vertical")?"top":"left"};
var s=(l=="vertical")?r.height():r.width();
if(n=="show"){r.css(p.size,0);
r.css(p.position,s/2)
}var o={};
o[p.size]=n=="show"?s:0;
o[p.position]=n=="show"?0:s/2;
r.animate(o,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(n=="hide"){q.hide()
}b.effects.restore(q,m);
b.effects.removeWrapper(q);
if(a.callback){a.callback.apply(q[0],arguments)
}q.dequeue()
}})
})
}
})(jQuery);