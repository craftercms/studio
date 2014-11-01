(function(b){b.effects.bounce=function(a){return this.queue(function(){var z=b(this),t=["position","top","left"];
var u=b.effects.setMode(z,a.options.mode||"effect");
var r=a.options.direction||"up";
var B=a.options.distance||20;
var A=a.options.times||5;
var x=a.duration||250;
if(/show|hide/.test(u)){t.push("opacity")
}b.effects.save(z,t);
z.show();
b.effects.createWrapper(z);
var y=(r=="up"||r=="down")?"top":"left";
var i=(r=="up"||r=="left")?"pos":"neg";
var B=a.options.distance||(y=="top"?z.outerHeight({margin:true})/3:z.outerWidth({margin:true})/3);
if(u=="show"){z.css("opacity",0).css(y,i=="pos"?-B:B)
}if(u=="hide"){B=B/(A*2)
}if(u!="hide"){A--
}if(u=="show"){var w={opacity:1};
w[y]=(i=="pos"?"+=":"-=")+B;
z.animate(w,x/2,a.options.easing);
B=B/2;
A--
}for(var v=0;
v<A;
v++){var q={},s={};
q[y]=(i=="pos"?"-=":"+=")+B;
s[y]=(i=="pos"?"+=":"-=")+B;
z.animate(q,x/2,a.options.easing).animate(s,x/2,a.options.easing);
B=(u=="hide")?B*2:B/2
}if(u=="hide"){var w={opacity:0};
w[y]=(i=="pos"?"-=":"+=")+B;
z.animate(w,x/2,a.options.easing,function(){z.hide();
b.effects.restore(z,t);
b.effects.removeWrapper(z);
if(a.callback){a.callback.apply(this,arguments)
}})
}else{var q={},s={};
q[y]=(i=="pos"?"-=":"+=")+B;
s[y]=(i=="pos"?"+=":"-=")+B;
z.animate(q,x/2,a.options.easing).animate(s,x/2,a.options.easing,function(){b.effects.restore(z,t);
b.effects.removeWrapper(z);
if(a.callback){a.callback.apply(this,arguments)
}})
}z.queue("fx",function(){z.dequeue()
});
z.dequeue()
})
}
})(jQuery);