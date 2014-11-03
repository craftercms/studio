(function(b){b.effects.shake=function(a){return this.queue(function(){var z=b(this),t=["position","top","left"];
var u=b.effects.setMode(z,a.options.mode||"effect");
var r=a.options.direction||"left";
var B=a.options.distance||20;
var A=a.options.times||3;
var x=a.duration||a.options.duration||140;
b.effects.save(z,t);
z.show();
b.effects.createWrapper(z);
var y=(r=="up"||r=="down")?"top":"left";
var i=(r=="up"||r=="left")?"pos":"neg";
var w={},q={},s={};
w[y]=(i=="pos"?"-=":"+=")+B;
q[y]=(i=="pos"?"+=":"-=")+B*2;
s[y]=(i=="pos"?"-=":"+=")+B*2;
z.animate(w,x,a.options.easing);
for(var v=1;
v<A;
v++){z.animate(q,x,a.options.easing).animate(s,x,a.options.easing)
}z.animate(q,x,a.options.easing).animate(w,x/2,a.options.easing,function(){b.effects.restore(z,t);
b.effects.removeWrapper(z);
if(a.callback){a.callback.apply(this,arguments)
}});
z.queue("fx",function(){z.dequeue()
});
z.dequeue()
})
}
})(jQuery);