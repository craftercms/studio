(function(){var F=tinymce.each,s=tinymce.dom.Event,D;
function u(a,b){while(a&&(a.nodeType===8||(a.nodeType===3&&/^[ \t\n\r]*$/.test(a.nodeValue)))){a=b(a)
}return a
}function I(a){return u(a,function(b){return b.previousSibling
})
}function B(a){return u(a,function(b){return b.nextSibling
})
}function G(c,a,b){return c.dom.getParent(a,function(d){return tinymce.inArray(b,d)!==-1
})
}function w(a){return a&&(a.tagName==="OL"||a.tagName==="UL")
}function H(c,b){var d,a,e;
d=I(c.lastChild);
while(w(d)){a=d;
d=I(a.previousSibling)
}if(a){e=b.create("li",{style:"list-style-type: none;"});
b.split(c,a);
b.insertAfter(e,a);
e.appendChild(a);
e.appendChild(a);
c=e.previousSibling
}return c
}function x(b,c,a){b=J(b,c,a);
return v(b,c,a)
}function J(b,d,a){var c=I(b.previousSibling);
if(c){return C(c,b,d?c:false,a)
}else{return b
}}function v(b,c,a){var d=B(b.nextSibling);
if(d){return C(b,d,c?d:false,a)
}else{return b
}}function C(b,d,c,a){if(y(b,d,!!c,a)){return E(b,d,c)
}else{if(b&&b.tagName==="LI"&&w(d)){b.appendChild(d)
}}return d
}function y(b,c,d,a){if(!b||!c){return false
}else{if(b.tagName==="LI"&&c.tagName==="LI"){return c.style.listStyleType==="none"||A(c)
}else{if(w(b)){return(b.tagName===c.tagName&&(d||b.style.listStyleType===c.style.listStyleType))||t(c)
}else{return a&&b.tagName==="P"&&c.tagName==="P"
}}}}function t(b){var c=B(b.firstChild),a=I(b.lastChild);
return c&&a&&w(b)&&c===a&&(w(c)||c.style.listStyleType==="none"||A(c))
}function A(a){var b=B(a.firstChild),c=I(a.lastChild);
return b&&c&&b===c&&w(b)
}function E(a,b,e){var c=I(a.lastChild),d=B(b.firstChild);
if(a.tagName==="P"){a.appendChild(a.ownerDocument.createElement("br"))
}while(b.firstChild){a.appendChild(b.firstChild)
}if(e){a.style.listStyleType=e.style.listStyleType
}b.parentNode.removeChild(b);
C(c,d,false);
return a
}function z(b,a){var c;
if(!a.is(b,"li,ol,ul")){c=a.getParent(b,"li");
if(c){b=c
}}return b
}tinymce.create("tinymce.plugins.Lists",{init:function(m){var U="TABBING";
var aa="EMPTY";
var l="ESCAPE";
var k="PARAGRAPH";
var g="UNKNOWN";
var o=g;
function X(K){return K.keyCode===tinymce.VK.TAB&&!(K.altKey||K.ctrlKey)&&(m.queryCommandState("InsertUnorderedList")||m.queryCommandState("InsertOrderedList"))
}function q(){var M=ac();
var K=M.parentNode.parentNode;
var L=M.parentNode.lastChild===M;
return L&&!Y(K)&&e(M)
}function Y(K){if(w(K)){return K.parentNode&&K.parentNode.tagName==="LI"
}else{return K.tagName==="LI"
}}function W(){return m.selection.isCollapsed()&&e(ac())
}function ac(){var K=m.selection.getStart();
return((K.tagName=="BR"||K.tagName=="")&&K.parentNode.tagName=="LI")?K.parentNode:K
}function e(L){var K=L.childNodes.length;
if(L.tagName==="LI"){return K==0?true:K==1&&(L.firstChild.tagName==""||L.firstChild.tagName=="BR"||p(L))
}return false
}function p(M){var L=tinymce.grep(M.parentNode.childNodes,function(O){return O.tagName=="LI"
});
var K=M==L[L.length-1];
var N=M.firstChild;
return tinymce.isIE9&&K&&(N.nodeValue==String.fromCharCode(160)||N.nodeValue==String.fromCharCode(32))
}function a(K){return K.keyCode===tinymce.VK.ENTER
}function ad(K){return a(K)&&!K.shiftKey
}function h(K){if(X(K)){return U
}else{if(ad(K)&&q()){return g
}else{if(ad(K)&&W()){return aa
}else{return g
}}}}function Z(L,K){if(o==U||o==aa||tinymce.isGecko&&o==l){s.cancel(K)
}}function ab(){var M=m.selection.getRng(true);
var L=M.startContainer;
if(L.nodeType==3){var K=L.nodeValue;
if(tinymce.isIE9&&K.length>1&&K.charCodeAt(K.length-1)==32){return(M.endOffset==K.length-1)
}else{return(M.endOffset==K.length)
}}else{if(L.nodeType==1){return M.endOffset==L.childNodes.length
}}return false
}function n(){var K=m.selection.getNode();
var L="h1,h2,h3,h4,h5,h6,p,div";
var M=m.dom.is(K,L)&&K.parentNode.tagName==="LI"&&K.parentNode.lastChild===K;
return m.selection.isCollapsed()&&M&&ab()
}function j(K,N){if(ad(N)&&n()){var O=K.selection.getNode();
var L=K.dom.create("li");
var M=K.dom.getParent(O,"li");
K.dom.insertAfter(L,M);
if(tinymce.isIE6||tinymce.isIE7||tinyMCE.isIE8){K.selection.setCursorLocation(L,1)
}else{K.selection.setCursorLocation(L,0)
}N.preventDefault()
}}function V(N,L){var T;
if(!tinymce.isGecko){return
}var P=N.selection.getStart();
if(L.keyCode!=tinymce.VK.BACKSPACE||P.tagName!=="IMG"){return
}function O(aj){var ai=aj.firstChild;
var ak=null;
do{if(!ai){break
}if(ai.tagName==="LI"){ak=ai
}}while(ai=ai.nextSibling);
return ak
}function R(ah,ai){while(ah.childNodes.length>0){ai.appendChild(ah.childNodes[0])
}}T=P.parentNode.previousSibling;
if(!T){return
}var K;
if(T.tagName==="UL"||T.tagName==="OL"){K=T
}else{if(T.previousSibling&&(T.previousSibling.tagName==="UL"||T.previousSibling.tagName==="OL")){K=T.previousSibling
}else{return
}}var S=O(K);
var Q=N.dom.createRng();
Q.setStart(S,1);
Q.setEnd(S,1);
N.selection.setRng(Q);
N.selection.collapse(true);
var M=N.selection.getBookmark();
var ae=P.parentNode.cloneNode(true);
if(ae.tagName==="P"||ae.tagName==="DIV"){R(ae,S)
}else{S.appendChild(ae)
}P.parentNode.parentNode.removeChild(P.parentNode);
N.selection.moveToBookmark(M)
}function r(M){var L=m.dom.getParent(M,"ol,ul");
if(L!=null){var K=L.lastChild;
m.selection.setCursorLocation(K,0)
}}this.ed=m;
m.addCommand("Indent",this.indent,this);
m.addCommand("Outdent",this.outdent,this);
m.addCommand("InsertUnorderedList",function(){this.applyList("UL","OL")
},this);
m.addCommand("InsertOrderedList",function(){this.applyList("OL","UL")
},this);
m.onInit.add(function(){m.editorCommands.addCommands({outdent:function(){var L=m.selection,K=m.dom;
function M(N){N=K.getParent(N,K.isBlock);
return N&&(parseInt(m.dom.getStyle(N,"margin-left")||0,10)+parseInt(m.dom.getStyle(N,"padding-left")||0,10))>0
}return M(L.getStart())||M(L.getEnd())||m.queryCommandState("InsertOrderedList")||m.queryCommandState("InsertUnorderedList")
}},"state")
});
m.onKeyUp.add(function(L,K){if(o==U){L.execCommand(K.shiftKey?"Outdent":"Indent",true,null);
o=g;
return s.cancel(K)
}else{if(o==aa){var M=ac();
var N=L.settings.list_outdent_on_enter===true||K.shiftKey;
L.execCommand(N?"Outdent":"Indent",true,null);
if(tinymce.isIE){r(M)
}return s.cancel(K)
}else{if(o==l){if(tinymce.isIE6||tinymce.isIE7||tinymce.isIE8){var O=L.getDoc().createTextNode("\uFEFF");
L.selection.getNode().appendChild(O)
}else{if(tinymce.isIE9||tinymce.isGecko){L.execCommand("Outdent");
return s.cancel(K)
}}}}}});
function i(L,M){var K=m.getDoc().createTextNode("\uFEFF");
L.insertBefore(K,M);
m.selection.setCursorLocation(K,0);
m.execCommand("mceRepaint")
}function c(L,O){if(a(O)){var M=ac();
if(M){var K=M.parentNode;
var N=K&&K.parentNode;
if(N&&N.nodeName=="LI"&&N.firstChild==K&&M==K.firstChild){i(N,K)
}}}}function b(L,N){if(a(N)){var M=ac();
if(L.dom.select("ul li",M).length===1){var K=M.firstChild;
i(M,K)
}}}function d(K,O){function Q(ae){var S=[];
var R=new tinymce.dom.TreeWalker(ae.firstChild,ae);
for(var T=R.current();
T;
T=R.next()){if(K.dom.is(T,"ol,ul,li")){S.push(T)
}}return S
}if(O.keyCode==tinymce.VK.BACKSPACE){var M=ac();
if(M){var N=K.dom.getParent(M,"ol,ul"),L=K.selection.getRng();
if(N&&N.firstChild===M&&L.startOffset==0){var P=Q(M);
P.unshift(M);
K.execCommand("Outdent",false,P);
K.undoManager.add();
return s.cancel(O)
}}}}function f(L,P){var M=ac();
if(P.keyCode===tinymce.VK.BACKSPACE&&L.dom.is(M,"li")&&M.parentNode.firstChild!==M){if(L.dom.select("ul,ol",M).length===1){var N=M.previousSibling;
L.dom.remove(L.dom.select("br",M));
L.dom.remove(M,true);
var K=tinymce.grep(N.childNodes,function(Q){return Q.nodeType===3
});
if(K.length===1){var O=K[0];
L.selection.setCursorLocation(O,O.length)
}L.undoManager.add();
return s.cancel(P)
}}}m.onKeyDown.add(function(L,K){o=h(K)
});
m.onKeyDown.add(Z);
m.onKeyDown.add(V);
m.onKeyDown.add(j);
if(tinymce.isGecko){m.onKeyUp.add(c)
}if(tinymce.isIE8){m.onKeyUp.add(b)
}if(tinymce.isGecko||tinymce.isWebKit){m.onKeyDown.add(d)
}if(tinymce.isWebKit){m.onKeyDown.add(f)
}},applyList:function(d,j){var p=this,b=p.ed,f=b.dom,n=[],g=false,l=false,h=false,q,i=b.selection.getSelectedBlocks();
function m(K){if(K&&K.tagName==="BR"){f.remove(K)
}}function k(Q){var P=f.create(d),O;
function K(L){if(L.style.marginLeft||L.style.paddingLeft){p.adjustPaddingFunction(false)(L)
}}if(Q.tagName==="LI"){}else{if(Q.tagName==="P"||Q.tagName==="DIV"||Q.tagName==="BODY"){a(Q,function(L,M){c(L,M,Q.tagName==="BODY"?null:L.parentNode);
O=L.parentNode;
K(O);
m(M)
});
if(O){if(O.tagName==="LI"&&(Q.tagName==="P"||i.length>1)){f.split(O.parentNode.parentNode,O.parentNode)
}x(O.parentNode,true)
}return
}else{O=f.create("li");
f.insertAfter(O,Q);
O.appendChild(Q);
K(Q);
Q=O
}}f.insertAfter(P,Q);
P.appendChild(Q);
x(P,true);
n.push(Q)
}function c(R,K,T){var Q,S=R,U;
while(!f.isBlock(R.parentNode)&&R.parentNode!==f.getRoot()){R=f.split(R.parentNode,R.previousSibling);
R=R.nextSibling;
S=R
}if(T){Q=T.cloneNode(true);
R.parentNode.insertBefore(Q,R);
while(Q.firstChild){f.remove(Q.firstChild)
}Q=f.rename(Q,"li")
}else{Q=f.create("li");
R.parentNode.insertBefore(Q,R)
}while(S&&S!=K){U=S.nextSibling;
Q.appendChild(S);
S=U
}if(Q.childNodes.length===0){Q.innerHTML='<br _mce_bogus="1" />'
}k(Q)
}function a(W,K){var Z,V,Y=3,ab=1,ac="br,ul,ol,p,div,h1,h2,h3,h4,h5,h6,table,blockquote,address,pre,form,center,dl";
function X(O,N){var M=f.createRng(),L;
D.keep=true;
b.selection.moveToBookmark(D);
D.keep=false;
L=b.selection.getRng(true);
if(!N){N=O.parentNode.lastChild
}M.setStartBefore(O);
M.setEndAfter(N);
return !(M.compareBoundaryPoints(Y,L)>0||M.compareBoundaryPoints(ab,L)<=0)
}function U(L){if(L.nextSibling){return L.nextSibling
}if(!f.isBlock(L.parentNode)&&L.parentNode!==f.getRoot()){return U(L.parentNode)
}}Z=W.firstChild;
var aa=false;
F(f.select(ac,W),function(L){if(L.hasAttribute&&L.hasAttribute("_mce_bogus")){return true
}if(X(Z,L)){f.addClass(L,"_mce_tagged_br");
Z=U(L)
}});
aa=(Z&&X(Z,undefined));
Z=W.firstChild;
F(f.select(ac,W),function(L){var M=U(L);
if(L.hasAttribute&&L.hasAttribute("_mce_bogus")){return true
}if(f.hasClass(L,"_mce_tagged_br")){K(Z,L,V);
V=null
}else{V=L
}Z=M
});
if(aa){K(Z,undefined,V)
}}function o(K){a(K,function(Q,O,P){c(Q,O);
m(O);
m(P)
})
}function r(K){if(tinymce.inArray(n,K)!==-1){return
}if(K.parentNode.tagName===j){f.split(K.parentNode,K);
k(K);
v(K.parentNode,false)
}n.push(K)
}function e(S){var Q,R,K,P;
if(tinymce.inArray(n,S)!==-1){return
}S=H(S,f);
while(f.is(S.parentNode,"ol,ul,li")){f.split(S.parentNode,S)
}n.push(S);
S=f.rename(S,"p");
K=x(S,false,b.settings.force_br_newlines);
if(K===S){Q=S.firstChild;
while(Q){if(f.isBlock(Q)){Q=f.split(Q.parentNode,Q);
P=true;
R=Q.nextSibling&&Q.nextSibling.firstChild
}else{R=Q.nextSibling;
if(P&&Q.tagName==="BR"){f.remove(Q)
}P=false
}Q=R
}}}F(i,function(K){K=z(K,f);
if(K.tagName===j||(K.tagName==="LI"&&K.parentNode.tagName===j)){l=true
}else{if(K.tagName===d||(K.tagName==="LI"&&K.parentNode.tagName===d)){g=true
}else{h=true
}}});
if(h&&!g||l||i.length===0){q={LI:r,H1:k,H2:k,H3:k,H4:k,H5:k,H6:k,P:k,BODY:k,DIV:i.length>1?k:o,defaultAction:o,elements:this.selectedBlocks()}
}else{q={defaultAction:e,elements:this.selectedBlocks(),processEvenIfEmpty:true}
}this.process(q)
},indent:function(){var d=this.ed,b=d.dom,a=[];
function f(g){var h=b.create("li",{style:"list-style-type: none;"});
b.insertAfter(h,g);
return h
}function e(m){var l=f(m),i=b.getParent(m,"ol,ul"),k=i.tagName,h=b.getStyle(i,"list-style-type"),g={},j;
if(h!==""){g.style="list-style-type: "+h+";"
}j=b.create(k,g);
l.appendChild(j);
return j
}function c(g){if(!G(d,g,a)){g=H(g,b);
var h=e(g);
h.appendChild(g);
x(h.parentNode,false);
x(h,false);
a.push(g)
}}this.process({LI:c,defaultAction:this.adjustPaddingFunction(true),elements:this.selectedBlocks()})
},outdent:function(h,b){var c=this,e=c.ed,g=e.dom,f=[];
function a(i){var k,l,j;
if(!G(e,i,f)){if(g.getStyle(i,"margin-left")!==""||g.getStyle(i,"padding-left")!==""){return c.adjustPaddingFunction(false)(i)
}j=g.getStyle(i,"text-align",true);
if(j==="center"||j==="right"){g.setStyle(i,"text-align","left");
return
}i=H(i,g);
k=i.parentNode;
l=i.parentNode.parentNode;
if(l.tagName==="P"){g.split(l,i.parentNode)
}else{g.split(k,i);
if(l.tagName==="LI"){g.split(l,i)
}else{if(!g.is(l,"ol,ul")){g.rename(i,"p")
}}}f.push(i)
}}var d=b&&tinymce.is(b,"array")?b:this.selectedBlocks();
this.process({LI:a,defaultAction:this.adjustPaddingFunction(false),elements:d});
F(f,x)
},process:function(f){var l=this,h=l.ed.selection,e=l.ed.dom,m,k;
function c(n){var o=tinymce.grep(n.childNodes,function(p){return !(p.nodeName==="BR"||p.nodeName==="SPAN"&&e.getAttrib(p,"data-mce-type")=="bookmark"||p.nodeType==3&&(p.nodeValue==String.fromCharCode(160)||p.nodeValue==""))
});
return o.length===0
}function g(o){e.removeClass(o,"_mce_act_on");
if(!o||o.nodeType!==1||!f.processEvenIfEmpty&&m.length>1&&c(o)){return
}o=z(o,e);
var n=f[o.tagName];
if(!n){n=f.defaultAction
}n(o)
}function i(n){l.splitSafeEach(n.childNodes,g,true)
}function b(o,n){return n>=0&&o.hasChildNodes()&&n<o.childNodes.length&&o.childNodes[n].tagName==="BR"
}function a(){var n=h.getNode();
var o=e.getParent(n,"td");
return o!==null
}m=f.elements;
k=h.getRng(true);
if(!k.collapsed){if(b(k.endContainer,k.endOffset-1)){k.setEnd(k.endContainer,k.endOffset-1);
h.setRng(k)
}if(b(k.startContainer,k.startOffset)){k.setStart(k.startContainer,k.startOffset+1);
h.setRng(k)
}}if(tinymce.isIE8){var j=l.ed.selection.getNode();
if(j.tagName==="LI"&&!(j.parentNode.lastChild===j)){var d=l.ed.getDoc().createTextNode("\uFEFF");
j.appendChild(d)
}}D=h.getBookmark();
f.OL=f.UL=i;
l.splitSafeEach(m,g);
h.moveToBookmark(D);
D=null;
if(!a()){l.ed.execCommand("mceRepaint")
}},splitSafeEach:function(a,b,c){if(c||(tinymce.isGecko&&(/Firefox\/[12]\.[0-9]/.test(navigator.userAgent)||/Firefox\/3\.[0-4]/.test(navigator.userAgent)))){this.classBasedEach(a,b)
}else{F(a,b)
}},classBasedEach:function(b,c){var a=this.ed.dom,e,d;
F(b,function(f){a.addClass(f,"_mce_act_on")
});
e=a.select("._mce_act_on");
while(e.length>0){d=e.shift();
a.removeClass(d,"_mce_act_on");
c(d);
e=a.select("._mce_act_on")
}},adjustPaddingFunction:function(b){var d,a,c=this.ed;
d=c.settings.indentation;
a=/[a-z%]+/i.exec(d);
d=parseInt(d,10);
return function(f){var g,e;
g=parseInt(c.dom.getStyle(f,"margin-left")||0,10)+parseInt(c.dom.getStyle(f,"padding-left")||0,10);
if(b){e=g+d
}else{e=g-d
}c.dom.setStyle(f,"padding-left","");
c.dom.setStyle(f,"margin-left",e>0?e+a:"")
}
},selectedBlocks:function(){var b=this.ed,a=b.selection.getSelectedBlocks();
return a.length==0?[b.dom.getRoot()]:a
},getInfo:function(){return{longname:"Lists",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/lists",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("lists",tinymce.plugins.Lists)
}());