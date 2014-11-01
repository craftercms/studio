function splitLines(a){return a.split(/\r?\n|\r/)
}function StringStream(a){this.pos=this.start=0;
this.string=a
}StringStream.prototype={eol:function(){return this.pos>=this.string.length
},sol:function(){return this.pos==0
},peek:function(){return this.string.charAt(this.pos)||null
},next:function(){if(this.pos<this.string.length){return this.string.charAt(this.pos++)
}},eat:function(a){var c=this.string.charAt(this.pos);
if(typeof a=="string"){var b=c==a
}else{var b=c&&(a.test?a.test(c):a(c))
}if(b){++this.pos;
return c
}},eatWhile:function(a){var b=this.pos;
while(this.eat(a)){}return this.pos>b
},eatSpace:function(){var a=this.pos;
while(/[\s\u00a0]/.test(this.string.charAt(this.pos))){++this.pos
}return this.pos>a
},skipToEnd:function(){this.pos=this.string.length
},skipTo:function(a){var b=this.string.indexOf(a,this.pos);
if(b>-1){this.pos=b;
return true
}},backUp:function(a){this.pos-=a
},column:function(){return this.start
},indentation:function(){return 0
},match:function(d,b,a){if(typeof d=="string"){function e(f){return a?f.toLowerCase():f
}if(e(this.string).indexOf(e(d),this.pos)==this.pos){if(b!==false){this.pos+=d.length
}return true
}}else{var c=this.string.slice(this.pos).match(d);
if(c&&b!==false){this.pos+=c[0].length
}return c
}},current:function(){return this.string.slice(this.start,this.pos)
}};
exports.StringStream=StringStream;
exports.startState=function(c,b,a){return c.startState?c.startState(b,a):true
};
var modes=exports.modes={},mimeModes=exports.mimeModes={};
exports.defineMode=function(a,b){modes[a]=b
};
exports.defineMIME=function(b,a){mimeModes[b]=a
};
exports.getMode=function(c,a){if(typeof a=="string"&&mimeModes.hasOwnProperty(a)){a=mimeModes[a]
}if(typeof a=="string"){var e=a,b={}
}else{if(a!=null){var e=a.name,b=a
}}var d=modes[e];
if(!d){throw new Error("Unknown mode: "+a)
}return d(c,b||{})
};
exports.runMode=function(g,c,k){var f=exports.getMode({indentUnit:2},c);
var l=splitLines(g),b=exports.startState(f);
for(var d=0,h=l.length;
d<h;
++d){if(d){k("\n")
}var j=new exports.StringStream(l[d]);
while(!j.eol()){var a=f.token(j,b);
k(j.current(),a,d,j.start);
j.start=j.pos
}}};