(function(){var K="";
var f="f";
var q="";
var r=0;
var g=[];
var P=0;
function I(){q=""
}function y(i){q+=i
}function B(i){return function(W){K+=i
}
}function s(){var W=parseInt(K,10);
K="";
return W||1
}function j(X){for(var W=0,Y=s();
W<Y;
++W){X(W,W==Y-1)
}}function u(i){if(typeof i=="string"){i=CodeMirror.commands[i]
}return function(W){j(function(){i(W)
})
}
}function a(W,i){for(var X in W){if(W.hasOwnProperty(X)){i(X,W[X])
}}}function C(W,Y){for(var X=0;
X<W.length;
++X){Y(W[X])
}}function T(i){if(i.slice(0,6)=="Shift-"){return i.slice(0,1)
}else{if(i=="Space"){return" "
}if(i.length==3&&i[0]=="'"&&i[2]=="'"){return i[1]
}return i.toLowerCase()
}}var e="~`!@#$%^&*()_-+=[{}]\\|/?.,<>:;\"'1234567890";
function b(W){if(W==" "){return"Space"
}var i=e.indexOf(W);
if(i!=-1){return"'"+W+"'"
}if(W.toLowerCase()==W){return W.toUpperCase()
}return"Shift-"+W.toUpperCase()
}var F=[/\w/,/[^\w\s]/],M=[/\S/];
function k(ae,ac,X,ab){var ad=0,aa=-1;
if(X>0){ad=ae.length;
aa=0
}var W=ad,Y=ad;
outer:for(;
ac!=ad;
ac+=X){for(var Z=0;
Z<ab.length;
++Z){if(ab[Z].test(ae.charAt(ac+aa))){W=ac;
for(;
ac!=ad;
ac+=X){if(!ab[Z].test(ae.charAt(ac+aa))){break
}}Y=ac;
break outer
}}}return{from:Math.min(W,Y),to:Math.max(W,Y)}
}function V(ad,ac,Y,X,ab){var ae=ad.getCursor();
for(var aa=0;
aa<X;
aa++){var af=ad.getLine(ae.line),Z=ae.ch,W;
while(true){if(ae.ch==af.length&&Y>0){ae.line++;
ae.ch=0;
af=ad.getLine(ae.line)
}else{if(ae.ch==0&&Y<0){ae.line--;
ae.ch=af.length;
af=ad.getLine(ae.line)
}}if(!af){break
}W=k(af,ae.ch,Y,ac);
ae.ch=W[ab=="end"?"to":"from"];
if(Z==ae.ch&&W.from!=W.to){ae.ch=W[Y<0?"from":"to"]
}else{break
}}}return ae
}function w(i){var Y=i.getCursor(),X=Y.ch,W=i.getLine(Y.line);
CodeMirror.commands.goLineEnd(i);
if(Y.line!=i.lineCount()){CodeMirror.commands.goLineEnd(i);
i.replaceSelection(" ","end");
CodeMirror.commands.delCharRight(i)
}}function E(X,W){var aa=g[W];
if(aa===undefined){return
}var Z=X.getCursor().line,ac=aa>Z?Z:aa,Y=aa>Z?aa:Z;
X.setCursor(ac);
for(var ab=ac;
ab<=Y;
ab++){y("\n"+X.getLine(ac));
X.removeLine(ac)
}}function d(X,W){var aa=g[W];
if(aa===undefined){return
}var Z=X.getCursor().line,ac=aa>Z?Z:aa,Y=aa>Z?aa:Z;
for(var ab=ac;
ab<=Y;
ab++){y("\n"+X.getLine(ab))
}X.setCursor(ac)
}function U(i){var X=i.getCursor(),W=i.getLine(X.line).search(/\S/);
i.setCursor(X.line,W==-1?line.length:W,true)
}function o(X,W,ac){var ab=X.getCursor(),Y=X.getLine(ab.line),i;
var Z=T(W),aa=ac;
if(aa.forward){i=Y.indexOf(Z,ab.ch+1);
if(i!=-1&&aa.inclusive){i+=1
}}else{i=Y.lastIndexOf(Z,ab.ch);
if(i!=-1&&!aa.inclusive){i+=1
}}return i
}function p(X,W,Z){var i=o(X,W,Z),Y=X.getCursor();
if(i!=-1){X.setCursor({line:Y.line,ch:i})
}}function m(X,W,Z){var i=o(X,W,Z);
var Y=X.getCursor();
if(i!==-1){if(Z.forward){X.replaceRange("",{line:Y.line,ch:Y.ch},{line:Y.line,ch:i})
}else{X.replaceRange("",{line:Y.line,ch:i},{line:Y.line,ch:Y.ch})
}}}function R(i){s();
i.setOption("keyMap","vim-insert")
}function N(i,Y,W,X){if(i.openDialog){i.openDialog(Y,X)
}else{X(prompt(W,""))
}}function l(i,X){var W=X.replace(/[<&]/,function(Y){return Y=="<"?"&lt;":"&amp;"
});
if(i.openDialog){i.openDialog(W+" <button type=button>OK</button>")
}else{alert(X)
}}var x=CodeMirror.keyMap.vim={"'|'":function(i){i.setCursor(i.getCursor().line,s()-1,true)
},A:function(i){i.setCursor(i.getCursor().line,i.getCursor().ch+1,true);
R(i)
},"Shift-A":function(i){CodeMirror.commands.goLineEnd(i);
R(i)
},I:function(i){R(i)
},"Shift-I":function(i){U(i);
R(i)
},O:function(i){CodeMirror.commands.goLineEnd(i);
CodeMirror.commands.newlineAndIndent(i);
R(i)
},"Shift-O":function(i){CodeMirror.commands.goLineStart(i);
i.replaceSelection("\n","start");
i.indentLine(i.getCursor().line);
R(i)
},G:function(i){i.setOption("keyMap","vim-prefix-g")
},"Shift-D":function(i){I();
g["Shift-D"]=i.getCursor(false).line;
i.setCursor(i.getCursor(true).line);
E(i,"Shift-D");
g=[]
},S:function(i){u(function(W){CodeMirror.commands.delCharRight(W)
})(i);
R(i)
},M:function(i){i.setOption("keyMap","vim-prefix-m");
g=[]
},Y:function(i){i.setOption("keyMap","vim-prefix-y");
I();
r=0
},"Shift-Y":function(i){I();
g["Shift-D"]=i.getCursor(false).line;
i.setCursor(i.getCursor(true).line);
d(i,"Shift-D");
g=[]
},"/":function(i){var W=CodeMirror.commands.find;
W&&W(i);
f="f"
},"'?'":function(i){var W=CodeMirror.commands.find;
if(W){W(i);
CodeMirror.commands.findPrev(i);
f="r"
}},N:function(i){var W=CodeMirror.commands.findNext;
if(W){f!="r"?W(i):CodeMirror.commands.findPrev(i)
}},"Shift-N":function(i){var W=CodeMirror.commands.findNext;
if(W){f!="r"?CodeMirror.commands.findPrev(i):W.findNext(i)
}},"Shift-G":function(i){K==""?i.setCursor(i.lineCount()):i.setCursor(parseInt(K,10)-1);
s();
CodeMirror.commands.goLineStart(i)
},"':'":function(i){var W=': <input type="text" style="width: 90%"/>';
N(i,W,":",function(X){if(X.match(/^\d+$/)){i.setCursor(X-1,i.getCursor().ch)
}else{l(i,"Bad command: "+X)
}})
},nofallthrough:true,style:"fat-cursor"};
C(["d","t","T","f","F","c","r"],function(i){CodeMirror.keyMap.vim[b(i)]=function(W){W.setOption("keyMap","vim-prefix-"+i);
I()
}
});
function J(X){X["0"]=function(i){K.length>0?B("0")(i):CodeMirror.commands.goLineStart(i)
};
for(var W=1;
W<10;
++W){X[W]=B(W)
}}J(CodeMirror.keyMap.vim);
a({Left:"goColumnLeft",Right:"goColumnRight",Down:"goLineDown",Up:"goLineUp",Backspace:"goCharLeft",Space:"goCharRight",X:function(i){CodeMirror.commands.delCharRight(i)
},P:function(i){var W=i.getCursor().line;
if(q!=""){if(q[0]=="\n"){CodeMirror.commands.goLineEnd(i)
}i.replaceRange(q,i.getCursor())
}},"Shift-X":function(i){CodeMirror.commands.delCharLeft(i)
},"Shift-J":function(i){w(i)
},"Shift-P":function(i){var W=i.getCursor().line;
if(q!=""){CodeMirror.commands.goLineUp(i);
CodeMirror.commands.goLineEnd(i);
i.replaceSelection(q,"end")
}i.setCursor(W+1)
},"'~'":function(W){var X=W.getCursor(),i=W.getRange({line:X.line,ch:X.ch},{line:X.line,ch:X.ch+1});
i=i!=i.toLowerCase()?i.toLowerCase():i.toUpperCase();
W.replaceRange(i,{line:X.line,ch:X.ch},{line:X.line,ch:X.ch+1});
W.setCursor(X.line,X.ch+1)
},"Ctrl-B":function(i){CodeMirror.commands.goPageUp(i)
},"Ctrl-F":function(i){CodeMirror.commands.goPageDown(i)
},"Ctrl-P":"goLineUp","Ctrl-N":"goLineDown",U:"undo","Ctrl-R":"redo"},function(i,W){x[i]=u(W)
});
C(["vim-prefix-d'","vim-prefix-y'","vim-prefix-df","vim-prefix-dF","vim-prefix-dt","vim-prefix-dT","vim-prefix-c","vim-prefix-cf","vim-prefix-cF","vim-prefix-ct","vim-prefix-cT","vim-prefix-","vim-prefix-f","vim-prefix-F","vim-prefix-t","vim-prefix-T","vim-prefix-r","vim-prefix-m"],function(i){CodeMirror.keyMap[i]={auto:"vim",nofallthrough:true,style:"fat-cursor"}
});
CodeMirror.keyMap["vim-prefix-g"]={E:u(function(i){i.setCursor(V(i,F,-1,1,"start"))
}),"Shift-E":u(function(i){i.setCursor(V(i,M,-1,1,"start"))
}),G:function(i){i.setCursor({line:0,ch:i.getCursor().ch})
},auto:"vim",nofallthrough:true,style:"fat-cursor"};
CodeMirror.keyMap["vim-prefix-d"]={D:u(function(i){y("\n"+i.getLine(i.getCursor().line));
i.removeLine(i.getCursor().line);
i.setOption("keyMap","vim")
}),"'":function(i){i.setOption("keyMap","vim-prefix-d'");
I()
},B:function(i){var Y=i.getCursor();
var W=i.getLine(Y.line);
var X=W.lastIndexOf(" ",Y.ch);
y(W.substring(X,Y.ch));
i.replaceRange("",{line:Y.line,ch:X},Y);
i.setOption("keyMap","vim")
},nofallthrough:true,style:"fat-cursor"};
J(CodeMirror.keyMap["vim-prefix-d"]);
CodeMirror.keyMap["vim-prefix-c"]={B:function(i){u("delWordLeft")(i);
R(i)
},C:function(i){j(function(W,X){CodeMirror.commands.deleteLine(i);
if(W){CodeMirror.commands.delCharRight(i);
if(X){CodeMirror.commands.deleteLine(i)
}}});
R(i)
},nofallthrough:true,style:"fat-cursor"};
C(["vim-prefix-d","vim-prefix-c","vim-prefix-"],function(i){C(["f","F","T","t"],function(W){CodeMirror.keyMap[i][b(W)]=function(X){X.setOption("keyMap",i+W);
I()
}
})
});
var G={t:{inclusive:false,forward:true},f:{inclusive:true,forward:true},T:{inclusive:false,forward:false},F:{inclusive:true,forward:false}};
function n(i){CodeMirror.keyMap["vim-prefix-m"][i]=function(W){g[i]=W.getCursor().line
};
CodeMirror.keyMap["vim-prefix-d'"][i]=function(W){E(W,i)
};
CodeMirror.keyMap["vim-prefix-y'"][i]=function(W){d(W,i)
};
CodeMirror.keyMap["vim-prefix-r"][i]=function(W){var X=W.getCursor();
W.replaceRange(T(i),{line:X.line,ch:X.ch},{line:X.line,ch:X.ch+1});
CodeMirror.commands.goColumnLeft(W)
};
a(G,function(X,W){CodeMirror.keyMap["vim-prefix-"+X][i]=function(Y){p(Y,i,W)
};
CodeMirror.keyMap["vim-prefix-d"+X][i]=function(Y){m(Y,i,W)
};
CodeMirror.keyMap["vim-prefix-c"+X][i]=function(Y){m(Y,i,W);
R(Y)
}
})
}for(var O=65;
O<65+26;
O++){var A=String.fromCharCode(O);
n(b(A));
n(b(A.toLowerCase()))
}for(var O=0;
O<e.length;
++O){n(b(e.charAt(O)))
}n("Space");
CodeMirror.keyMap["vim-prefix-y"]={Y:u(function(i){y("\n"+i.getLine(i.getCursor().line+r));
r++;
i.setOption("keyMap","vim")
}),"'":function(i){i.setOption("keyMap","vim-prefix-y'");
I()
},nofallthrough:true,style:"fat-cursor"};
CodeMirror.keyMap["vim-insert"]={Esc:function(i){i.setCursor(i.getCursor().line,i.getCursor().ch-1,true);
i.setOption("keyMap","vim")
},"Ctrl-N":"autocomplete","Ctrl-P":"autocomplete",fallthrough:["default"]};
function h(ab,ac,i){var ad=ac.line;
var i=i?i:ab.getLine(ad)[ac.ch];
var X=["(","[","{"].indexOf(i)!=-1;
var Y=(function(ae){switch(ae){case"(":return")";
case"[":return"]";
case"{":return"}";
case")":return"(";
case"]":return"[";
case"}":return"{";
default:return null
}})(i);
if(Y==null){return ac
}var Z=X?0:1;
while(true){if(ad==ac.line){var W=X?ab.getLine(ad).substr(ac.ch).split(""):ab.getLine(ad).substr(0,ac.ch).split("").reverse()
}else{var W=X?ab.getLine(ad).split(""):ab.getLine(ad).split("").reverse()
}for(var aa=0;
aa<W.length;
aa++){if(W[aa]==i){Z++
}else{if(W[aa]==Y){Z--
}}if(Z==0){if(X&&ac.line==ad){return{line:ad,ch:aa+ac.ch}
}else{if(X){return{line:ad,ch:aa}
}else{return{line:ad,ch:W.length-aa-1}
}}}}if(X){ad++
}else{ad--
}}}function S(W,Z,i){var Y=W.getCursor();
var X=h(W,Y,Z);
var aa=h(W,X);
aa.ch+=i?1:0;
X.ch+=i?0:1;
return{start:aa,end:X}
}var D=["B","E","J","K","H","L","W","Shift-W","'^'","'$'","'%'","Esc"];
motions={B:function(i,W){return V(i,F,-1,W)
},"Shift-B":function(i,W){return V(i,M,-1,W)
},E:function(i,W){return V(i,F,1,W,"end")
},"Shift-E":function(i,W){return V(i,M,1,W,"end")
},J:function(i,X){var W=i.getCursor();
return{line:W.line+X,ch:W.ch}
},K:function(i,X){var W=i.getCursor();
return{line:W.line-X,ch:W.ch}
},H:function(i,X){var W=i.getCursor();
return{line:W.line,ch:W.ch-X}
},L:function(i,X){var W=i.getCursor();
return{line:W.line,ch:W.ch+X}
},W:function(i,W){return V(i,F,1,W)
},"Shift-W":function(i,W){return V(i,M,1,W)
},"'^'":function(i,Z){var Y=i.getCursor();
var W=i.getLine(Y.line).split("");
if(W.length==0){return Y
}for(var X=0;
X<W.length;
X++){if(W[X].match(/[^\s]/)){return{line:Y.line,ch:X}
}}},"'$'":function(i){var X=i.getCursor();
var W=i.getLine(X.line);
return{line:X.line,ch:W.length}
},"'%'":function(i){return h(i,i.getCursor())
},Esc:function(i){i.setOption("vim");
P=0;
return i.getCursor()
}};
D.forEach(function(W,i,X){CodeMirror.keyMap["vim-prefix-d"][W]=function(Y){var ab=Y.getCursor();
var Z=motions[W](Y,P?P:1);
if((ab.line>Z.line)||(ab.line==Z.line&&ab.ch>Z.ch)){var aa=true
}y(Y.getRange(aa?Z:ab,aa?ab:Z));
Y.replaceRange("",aa?Z:ab,aa?ab:Z);
P=0;
Y.setOption("keyMap","vim")
};
CodeMirror.keyMap["vim-prefix-c"][W]=function(Y){var ab=Y.getCursor();
var Z=motions[W](Y,P?P:1);
if((ab.line>Z.line)||(ab.line==Z.line&&ab.ch>Z.ch)){var aa=true
}y(Y.getRange(aa?Z:ab,aa?ab:Z));
Y.replaceRange("",aa?Z:ab,aa?ab:Z);
P=0;
Y.setOption("keyMap","vim-insert")
};
CodeMirror.keyMap["vim-prefix-y"][W]=function(Y){var ab=Y.getCursor();
var Z=motions[W](Y,P?P:1);
if((ab.line>Z.line)||(ab.line==Z.line&&ab.ch>Z.ch)){var aa=true
}y(Y.getRange(aa?Z:ab,aa?ab:Z));
P=0;
Y.setOption("keyMap","vim")
};
CodeMirror.keyMap.vim[W]=function(Y){var Z=motions[W](Y,P?P:1);
Y.setCursor(Z.line,Z.ch);
P=0
}
});
var L=[1,2,3,4,5,6,7,8,9];
L.forEach(function(W,i,X){CodeMirror.keyMap.vim[W]=function(Y){P=(P*10)+W
};
CodeMirror.keyMap["vim-prefix-d"][W]=function(Y){P=(P*10)+W
};
CodeMirror.keyMap["vim-prefix-y"][W]=function(Y){P=(P*10)+W
};
CodeMirror.keyMap["vim-prefix-c"][W]=function(Y){P=(P*10)+W
}
});
var Q=["d","y","c"];
Q.forEach(function(W,i,X){CodeMirror.keyMap["vim-prefix-"+W+"a"]={auto:"vim",nofallthrough:true,style:"fat-cursor"};
CodeMirror.keyMap["vim-prefix-"+W+"i"]={auto:"vim",nofallthrough:true,style:"fat-cursor"};
CodeMirror.keyMap["vim-prefix-"+W]["A"]=function(Y){P=0;
Y.setOption("keyMap","vim-prefix-"+W+"a")
};
CodeMirror.keyMap["vim-prefix-"+W]["I"]=function(Y){P=0;
Y.setOption("keyMap","vim-prefix-"+W+"i")
}
});
function z(W,Y,Z){for(var X=Z==null?W.length:Z;
X>=0;
--X){if(Y.test(W.charAt(X))){return X
}}return -1
}var c=["W","Shift-[","Shift-9","["];
var v={W:function(X,W){var aa=X.getCursor();
var Z=X.getLine(aa.line);
var i=new String(Z.substring(0,aa.ch));
var ab=z(i,/[^a-zA-Z0-9]/)+1;
var Y=motions.E(X,1);
Y.ch+=W?1:0;
return{start:{line:aa.line,ch:ab},end:Y}
},"Shift-[":function(W,i){return S(W,"}",i)
},"Shift-9":function(W,i){return S(W,")",i)
},"[":function(W,i){return S(W,"]",i)
}};
function t(ac,X,aa,ad,ab){var Z=v[X](ac,ab);
var i=Z.start;
var Y=Z.end;
if((i.line>Y.line)||(i.line==Y.line&&i.ch>Y.ch)){var W=true
}y(ac.getRange(W?Y:i,W?i:Y));
if(aa){ac.replaceRange("",W?Y:i,W?i:Y)
}if(ad){ac.setOption("keyMap","vim-insert")
}}for(var O=0;
O<c.length;
++O){var H=c[O];
(function(i){CodeMirror.keyMap["vim-prefix-di"][i]=function(W){t(W,i,true,false,false)
};
CodeMirror.keyMap["vim-prefix-da"][i]=function(W){t(W,i,true,false,true)
};
CodeMirror.keyMap["vim-prefix-yi"][i]=function(W){t(W,i,false,false,false)
};
CodeMirror.keyMap["vim-prefix-ya"][i]=function(W){t(W,i,false,false,true)
};
CodeMirror.keyMap["vim-prefix-ci"][i]=function(W){t(W,i,true,true,false)
};
CodeMirror.keyMap["vim-prefix-ca"][i]=function(W){t(W,i,true,true,true)
}
})(H)
}})();