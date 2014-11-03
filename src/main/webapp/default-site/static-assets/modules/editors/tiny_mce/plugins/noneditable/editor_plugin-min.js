(function(){var j=tinymce.dom.TreeWalker;
var g="contenteditable",i="data-mce-"+g;
var h=tinymce.VK;
function f(e){var v=e.dom,c=e.selection,a,d="mce_noneditablecaret",a="\uFEFF";
function s(k){var l;
if(k.nodeType===1){l=k.getAttribute(i);
if(l&&l!=="inherit"){return l
}l=k.contentEditable;
if(l!=="inherit"){return l
}}return null
}function y(l){var k;
while(l){k=s(l);
if(k){return k==="false"?l:null
}l=l.parentNode
}}function t(k){while(k){if(k.id===d){return k
}k=k.parentNode
}}function u(l){var k;
if(l){k=new j(l,l);
for(l=k.current();
l;
l=k.next()){if(l.nodeType===3){return l
}}}}function z(k,l){var n,m;
if(s(k)==="false"){if(v.isBlock(k)){c.select(k);
return
}}m=v.createRng();
if(s(k)==="true"){if(!k.firstChild){k.appendChild(e.getDoc().createTextNode("\u00a0"))
}k=k.firstChild;
l=true
}n=v.create("span",{id:d,"data-mce-bogus":true},a);
if(l){k.parentNode.insertBefore(n,k)
}else{v.insertAfter(n,k)
}m.setStart(n.firstChild,1);
m.collapse(true);
c.setRng(m);
return n
}function w(n){var k,m,l;
if(n){rng=c.getRng(true);
rng.setStartBefore(n);
rng.setEndBefore(n);
k=u(n);
if(k&&k.nodeValue.charAt(0)==a){k=k.deleteData(0,1)
}v.remove(n,true);
c.setRng(rng)
}else{m=t(c.getStart());
while((n=v.get(d))&&n!==l){if(m!==n){k=u(n);
if(k&&k.nodeValue.charAt(0)==a){k=k.deleteData(0,1)
}v.remove(n,true)
}l=n
}}}function b(){var p,l,n,o,m;
function k(N,K){var r,I,J,M,L;
r=o.startContainer;
I=o.startOffset;
if(r.nodeType==3){L=r.nodeValue.length;
if((I>0&&I<L)||(K?I==L:I==0)){return
}}else{if(I<r.childNodes.length){var H=!K&&I>0?I-1:I;
r=r.childNodes[H];
if(r.hasChildNodes()){r=r.firstChild
}}else{return !K?N:null
}}J=new j(r,N);
while(M=J[K?"prev":"next"]()){if(M.nodeType===3&&M.nodeValue.length>0){return
}else{if(s(M)==="true"){return M
}}}return N
}w();
n=c.isCollapsed();
p=y(c.getStart());
l=y(c.getEnd());
if(p||l){o=c.getRng(true);
if(n){p=p||l;
var q=c.getStart();
if(m=k(p,true)){z(m,true)
}else{if(m=k(p,false)){z(m,false)
}else{c.select(p)
}}}else{o=c.getRng(true);
if(p){o.setStartBefore(p)
}if(l){o.setEndAfter(l)
}c.setRng(o)
}}}function x(o,m){var J=m.keyCode,q,l,k,G;
function H(A,B){while(A=A[B?"previousSibling":"nextSibling"]){if(A.nodeType!==3||A.nodeValue.length>0){return A
}}}function p(B,A){c.select(B);
c.collapse(A)
}function I(B){var C,D,N,E;
function F(M){var P=D;
while(P){if(P===M){return
}P=P.parentNode
}v.remove(M);
b()
}function A(){var Q,M,R=o.schema.getNonEmptyElements();
M=new tinymce.dom.TreeWalker(D,o.getBody());
while(Q=(B?M.prev():M.next())){if(R[Q.nodeName.toLowerCase()]){break
}if(Q.nodeType===3&&tinymce.trim(Q.nodeValue).length>0){break
}if(s(Q)==="false"){F(Q);
return true
}}if(y(Q)){return true
}return false
}if(c.isCollapsed()){C=c.getRng(true);
D=C.startContainer;
N=C.startOffset;
D=t(D)||D;
if(E=y(D)){F(E);
return false
}if(D.nodeType==3&&(B?N>0:N<D.nodeValue.length)){return true
}if(D.nodeType==1){D=D.childNodes[N]||D
}if(A()){return false
}}return true
}k=c.getStart();
G=c.getEnd();
q=y(k)||y(G);
if(q&&(J<112||J>124)&&J!=h.DELETE&&J!=h.BACKSPACE){if((tinymce.isMac?m.metaKey:m.ctrlKey)&&(J==67||J==88||J==86)){return
}m.preventDefault();
if(J==h.LEFT||J==h.RIGHT){var r=J==h.LEFT;
if(o.dom.isBlock(q)){var n=r?q.previousSibling:q.nextSibling;
var L=new j(n,n);
var K=r?L.prev():L.next();
p(K,!r)
}else{p(q,r)
}}}else{if(J==h.LEFT||J==h.RIGHT||J==h.BACKSPACE||J==h.DELETE){l=t(k);
if(l){if(J==h.LEFT||J==h.BACKSPACE){q=H(l,true);
if(q&&s(q)==="false"){m.preventDefault();
if(J==h.LEFT){p(q,true)
}else{v.remove(q);
return
}}else{w(l)
}}if(J==h.RIGHT||J==h.DELETE){q=H(l);
if(q&&s(q)==="false"){m.preventDefault();
if(J==h.RIGHT){p(q,false)
}else{v.remove(q);
return
}}else{w(l)
}}}if((J==h.BACKSPACE||J==h.DELETE)&&!I(J==h.BACKSPACE)){m.preventDefault();
return false
}}}}e.onMouseDown.addToTop(function(m,k){var l=m.selection.getNode();
if(s(l)==="false"&&l==k.target){b()
}});
e.onMouseUp.addToTop(b);
e.onKeyDown.addToTop(x);
e.onKeyUp.addToTop(b)
}tinymce.create("tinymce.plugins.NonEditablePlugin",{init:function(c,a){var d,e,b;
function l(k,t){var s=b.length,r=t.content,q=tinymce.trim(e);
if(t.format=="raw"){return
}while(s--){r=r.replace(b[s],function(m){var n=arguments,o=n[n.length-2];
if(o>0&&r.charAt(o-1)=='"'){return m
}return'<span class="'+q+'" data-mce-content="'+k.dom.encode(n[0])+'">'+k.dom.encode(typeof(n[1])==="string"?n[1]:n[0])+"</span>"
})
}t.content=r
}d=" "+tinymce.trim(c.getParam("noneditable_editable_class","mceEditable"))+" ";
e=" "+tinymce.trim(c.getParam("noneditable_noneditable_class","mceNonEditable"))+" ";
b=c.getParam("noneditable_regexp");
if(b&&!b.length){b=[b]
}c.onPreInit.add(function(){f(c);
if(b){c.selection.onBeforeSetContent.add(l);
c.onBeforeSetContent.add(l)
}c.parser.addAttributeFilter("class",function(p){var k=p.length,r,q;
while(k--){q=p[k];
r=" "+q.attr("class")+" ";
if(r.indexOf(d)!==-1){q.attr(i,"true")
}else{if(r.indexOf(e)!==-1){q.attr(i,"false")
}}}});
c.serializer.addAttributeFilter(i,function(p,k){var r=p.length,q;
while(r--){q=p[r];
if(b&&q.attr("data-mce-content")){q.name="#text";
q.type=3;
q.raw=true;
q.value=q.attr("data-mce-content")
}else{q.attr(g,null);
q.attr(i,null)
}}});
c.parser.addAttributeFilter(g,function(p,k){var r=p.length,q;
while(r--){q=p[r];
q.attr(i,q.attr(g));
q.attr(g,null)
}})
})
},getInfo:function(){return{longname:"Non editable elements",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/noneditable",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("noneditable",tinymce.plugins.NonEditablePlugin)
})();