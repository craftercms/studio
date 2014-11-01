CodeMirror.defineMode("vb",function(l,y){var b="error";
function p(F){return new RegExp("^(("+F.join(")|(")+"))\\b","i")
}var o=new RegExp("^[\\+\\-\\*/%&\\\\|\\^~<>!]");
var s=new RegExp("^[\\(\\)\\[\\]\\{\\}@,:`=;\\.]");
var a=new RegExp("^((==)|(<>)|(<=)|(>=)|(<>)|(<<)|(>>)|(//)|(\\*\\*))");
var i=new RegExp("^((\\+=)|(\\-=)|(\\*=)|(%=)|(/=)|(&=)|(\\|=)|(\\^=))");
var B=new RegExp("^((//=)|(>>=)|(<<=)|(\\*\\*=))");
var h=new RegExp("^[_A-Za-z][_A-Za-z0-9]*");
var u=["class","module","sub","enum","select","while","if","function","get","set","property","try"];
var r=["else","elseif","case","catch"];
var E=["next","loop"];
var d=p(["and","or","not","xor","in"]);
var x=["as","dim","break","continue","optional","then","until","goto","byval","byref","new","handles","property","return","const","private","protected","friend","public","shared","static","true","false"];
var v=["integer","string","double","decimal","boolean","short","char","float","single"];
var j=p(x);
var n=p(v);
var k='"';
var t=p(u);
var A=p(r);
var g=p(E);
var q=p(["end"]);
var c=p(["do"]);
var z=null;
function m(G,F){F.currentIndent++
}function e(G,F){F.currentIndent--
}function D(J,I){if(J.eatSpace()){return null
}var H=J.peek();
if(H==="'"){J.skipToEnd();
return"comment"
}if(J.match(/^((&H)|(&O))?[0-9\.a-f]/i,false)){var G=false;
if(J.match(/^\d*\.\d+F?/i)){G=true
}else{if(J.match(/^\d+\.\d*F?/)){G=true
}else{if(J.match(/^\.\d+F?/)){G=true
}}}if(G){J.eat(/J/i);
return"number"
}var F=false;
if(J.match(/^&H[0-9a-f]+/i)){F=true
}else{if(J.match(/^&O[0-7]+/i)){F=true
}else{if(J.match(/^[1-9]\d*F?/)){J.eat(/J/i);
F=true
}else{if(J.match(/^0(?![\dx])/i)){F=true
}}}}if(F){J.eat(/L/i);
return"number"
}}if(J.match(k)){I.tokenize=w(J.current());
return I.tokenize(J,I)
}if(J.match(B)||J.match(i)){return null
}if(J.match(a)||J.match(o)||J.match(d)){return"operator"
}if(J.match(s)){return null
}if(J.match(c)){m(J,I);
I.doInCurrentLine=true;
return"keyword"
}if(J.match(t)){if(!I.doInCurrentLine){m(J,I)
}else{I.doInCurrentLine=false
}return"keyword"
}if(J.match(A)){return"keyword"
}if(J.match(q)){e(J,I);
e(J,I);
return"keyword"
}if(J.match(g)){e(J,I);
return"keyword"
}if(J.match(n)){return"keyword"
}if(J.match(j)){return"keyword"
}if(J.match(h)){return"variable"
}J.next();
return b
}function w(F){var H=F.length==1;
var G="string";
return function I(K,J){while(!K.eol()){K.eatWhile(/[^'"]/);
if(K.match(F)){J.tokenize=D;
return G
}else{K.eat(/['"]/)
}}if(H){if(y.singleLineStringErrors){return b
}else{J.tokenize=D
}}return G
}
}function C(J,H){var G=H.tokenize(J,H);
var I=J.current();
if(I==="."){G=H.tokenize(J,H);
I=J.current();
if(G==="variable"){return"variable"
}else{return b
}}var F="[({".indexOf(I);
if(F!==-1){m(J,H)
}if(z==="dedent"){if(e(J,H)){return b
}}F="])}".indexOf(I);
if(F!==-1){if(e(J,H)){return b
}}return G
}var f={electricChars:"dDpPtTfFeE ",startState:function(F){return{tokenize:D,lastToken:null,currentIndent:0,nextLineIndent:0,doInCurrentLine:false}
},token:function(H,G){if(H.sol()){G.currentIndent+=G.nextLineIndent;
G.nextLineIndent=0;
G.doInCurrentLine=0
}var F=C(H,G);
G.lastToken={style:F,content:H.current()};
return F
},indent:function(H,F){var G=F.replace(/^\s+|\s+$/g,"");
if(G.match(g)||G.match(q)||G.match(A)){return l.indentUnit*(H.currentIndent-1)
}if(H.currentIndent<0){return 0
}return H.currentIndent*l.indentUnit
}};
return f
});
CodeMirror.defineMIME("text/x-vb","vb");