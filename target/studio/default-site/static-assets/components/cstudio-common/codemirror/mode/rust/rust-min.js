CodeMirror.defineMode("rust",function(){var z=4,J=2;
var j={"if":"if-style","while":"if-style","else":"else-style","do":"else-style",ret:"else-style",fail:"else-style","break":"atom",cont:"atom","const":"let",resource:"fn",let:"let",fn:"fn","for":"for",alt:"alt",iface:"iface",impl:"impl",type:"type","enum":"enum",mod:"mod",as:"op","true":"atom","false":"atom",assert:"op",check:"op",claim:"op","native":"ignore",unsafe:"ignore","import":"else-style","export":"else-style",copy:"op",log:"op",log_err:"op",use:"op",bind:"op",self:"atom"};
var e=function(){var ac={fn:"fn",block:"fn",obj:"obj"};
var r="bool uint int i8 i16 i32 i64 u8 u16 u32 u64 float f32 f64 str char".split(" ");
for(var ab=0,ad=r.length;
ab<ad;
++ab){ac[r[ab]]="atom"
}return ac
}();
var X=/[+\-*&%=<>!?|\.@]/;
var p,n;
function V(r,ab){p=r;
return ab
}function N(ad,ac){var ab=ad.next();
if(ab=='"'){ac.tokenize=m;
return ac.tokenize(ad,ac)
}if(ab=="'"){p="atom";
if(ad.eat("\\")){if(ad.skipTo("'")){ad.next();
return"string"
}else{return"error"
}}else{ad.next();
return ad.eat("'")?"string":"error"
}}if(ab=="/"){if(ad.eat("/")){ad.skipToEnd();
return"comment"
}if(ad.eat("*")){ac.tokenize=v(1);
return ac.tokenize(ad,ac)
}}if(ab=="#"){if(ad.eat("[")){p="open-attr";
return null
}ad.eatWhile(/\w/);
return V("macro","meta")
}if(ab==":"&&ad.match(":<")){return V("op",null)
}if(ab.match(/\d/)||(ab=="."&&ad.eat(/\d/))){var r=false;
if(!ad.match(/^x[\da-f]+/i)&&!ad.match(/^b[01]+/)){ad.eatWhile(/\d/);
if(ad.eat(".")){r=true;
ad.eatWhile(/\d/)
}if(ad.match(/^e[+\-]?\d+/i)){r=true
}}if(r){ad.match(/^f(?:32|64)/)
}else{ad.match(/^[ui](?:8|16|32|64)/)
}return V("atom","number")
}if(ab.match(/[()\[\]{}:;,]/)){return V(ab,null)
}if(ab=="-"&&ad.eat(">")){return V("->",null)
}if(ab.match(X)){ad.eatWhile(X);
return V("op",null)
}ad.eatWhile(/\w/);
n=ad.current();
if(ad.match(/^::\w/)){ad.backUp(1);
return V("prefix","variable-2")
}if(ac.keywords.propertyIsEnumerable(n)){return V(ac.keywords[n],n.match(/true|false/)?"atom":"keyword")
}return V("name","variable")
}function m(ad,ab){var r,ac=false;
while(r=ad.next()){if(r=='"'&&!ac){ab.tokenize=N;
return V("atom","string")
}ac=!ac&&r=="\\"
}return V("op","string")
}function v(r){return function(ae,ad){var ab=null,ac;
while(ac=ae.next()){if(ac=="/"&&ab=="*"){if(r==1){ad.tokenize=N;
break
}else{ad.tokenize=v(r-1);
return ad.tokenize(ae,ad)
}}if(ac=="*"&&ab=="/"){ad.tokenize=v(r+1);
return ad.tokenize(ae,ad)
}ab=ac
}return"comment"
}
}var y={state:null,stream:null,marked:null,cc:null};
function a(){for(var r=arguments.length-1;
r>=0;
r--){y.cc.push(arguments[r])
}}function M(){a.apply(null,arguments);
return true
}function f(ab,ac){var r=function(){var ad=y.state;
ad.lexical={indented:ad.indented,column:y.stream.column(),type:ab,prev:ad.lexical,info:ac}
};
r.lex=true;
return r
}function L(){var r=y.state;
if(r.lexical.prev){if(r.lexical.type==")"){r.indented=r.lexical.indented
}r.lexical=r.lexical.prev
}}function B(){y.state.keywords=e
}function A(){y.state.keywords=j
}L.lex=B.lex=A.lex=true;
function W(ac,r){function ab(ad){if(ad==","){return M(ac,ab)
}if(ad==r){return M()
}return M(ab)
}return function(ad){if(ad==r){return M()
}return a(ac,ab)
}
}function Z(ab,r){return M(f("stat",r),ab,L,l)
}function l(r){if(r=="}"){return M()
}if(r=="let"){return Z(k,"let")
}if(r=="fn"){return Z(I)
}if(r=="type"){return M(f("stat"),q,O,L,l)
}if(r=="enum"){return Z(d)
}if(r=="mod"){return Z(c)
}if(r=="iface"){return Z(F)
}if(r=="impl"){return Z(U)
}if(r=="open-attr"){return M(f("]"),W(C,"]"),L)
}if(r=="ignore"||r.match(/[\]\);,]/)){return M(l)
}return a(f("stat"),C,L,O,l)
}function O(r){if(r==";"){return M()
}return a()
}function C(r){if(r=="atom"||r=="name"){return M(t)
}if(r=="{"){return M(f("}"),Y,L)
}if(r.match(/[\[\(]/)){return S(r,C)
}if(r.match(/[\]\)\};,]/)){return a()
}if(r=="if-style"){return M(C,C)
}if(r=="else-style"||r=="op"){return M(C)
}if(r=="for"){return M(h,o,aa,C,C)
}if(r=="alt"){return M(C,P)
}if(r=="fn"){return M(I)
}if(r=="macro"){return M(g)
}return M()
}function t(r){if(n=="."){return M(K)
}if(n=="::<"){return M(w,t)
}if(r=="op"||n==":"){return M(C)
}if(r=="("||r=="["){return S(r,C)
}return a()
}function K(r){if(n.match(/^\w+$/)){y.marked="variable";
return M(t)
}return a(C)
}function Y(r){if(r=="op"){if(n=="|"){return M(b,L,f("}","block"),l)
}if(n=="||"){return M(L,f("}","block"),l)
}}if(n=="mutable"||(n.match(/^\w+$/)&&y.stream.peek()==":"&&!y.stream.match("::",false))){return a(H(C))
}return a(l)
}function H(r){function ab(ac){if(n=="mutable"||n=="with"){y.marked="keyword";
return M(ab)
}if(n.match(/^\w*$/)){y.marked="variable";
return M(ab)
}if(ac==":"){return M(r,ab)
}if(ac=="}"){return M()
}return M(ab)
}return ab
}function b(r){if(r=="name"){y.marked="def";
return M(b)
}if(r=="op"&&n=="|"){return M()
}return M(b)
}function k(r){if(r.match(/[\]\)\};]/)){return M()
}if(n=="="){return M(C,i)
}if(r==","){return M(k)
}return a(h,o,k)
}function i(r){if(r.match(/[\]\)\};,]/)){return a(k)
}else{return a(C,i)
}}function o(r){if(r==":"){return M(B,T,A)
}return a()
}function aa(r){if(r=="name"&&n=="in"){y.marked="keyword";
return M()
}return a()
}function I(r){if(n=="@"||n=="~"){y.marked="keyword";
return M(I)
}if(r=="name"){y.marked="def";
return M(I)
}if(n=="<"){return M(w,I)
}if(r=="{"){return a(C)
}if(r=="("){return M(f(")"),W(E,")"),L,I)
}if(r=="->"){return M(B,T,A,I)
}if(r==";"){return M()
}return M(I)
}function q(r){if(r=="name"){y.marked="def";
return M(q)
}if(n=="<"){return M(w,q)
}if(n=="="){return M(B,T,A)
}return M(q)
}function d(r){if(r=="name"){y.marked="def";
return M(d)
}if(n=="<"){return M(w,d)
}if(n=="="){return M(B,T,A,O)
}if(r=="{"){return M(f("}"),B,x,A,L)
}return M(d)
}function x(r){if(r=="}"){return M()
}if(r=="("){return M(f(")"),W(T,")"),L,x)
}if(n.match(/^\w+$/)){y.marked="def"
}return M(x)
}function c(r){if(r=="name"){y.marked="def";
return M(c)
}if(r=="{"){return M(f("}"),l,L)
}return a()
}function F(r){if(r=="name"){y.marked="def";
return M(F)
}if(n=="<"){return M(w,F)
}if(r=="{"){return M(f("}"),l,L)
}return a()
}function U(r){if(n=="<"){return M(w,U)
}if(n=="of"||n=="for"){y.marked="keyword";
return M(T,U)
}if(r=="name"){y.marked="def";
return M(U)
}if(r=="{"){return M(f("}"),l,L)
}return a()
}function w(r){if(n==">"){return M()
}if(n==","){return M(w)
}if(n==":"){return M(T,w)
}return a(T,w)
}function E(r){if(r=="name"){y.marked="def";
return M(E)
}if(r==":"){return M(B,T,A)
}return a()
}function T(r){if(r=="name"){y.marked="variable-3";
return M(G)
}if(n=="mutable"){y.marked="keyword";
return M(T)
}if(r=="atom"){return M(G)
}if(r=="op"||r=="obj"){return M(T)
}if(r=="fn"){return M(D)
}if(r=="{"){return M(f("{"),H(T),L)
}return S(r,T)
}function G(r){if(n=="<"){return M(w)
}return a()
}function D(r){if(r=="("){return M(f("("),W(T,")"),L,D)
}if(r=="->"){return M(T)
}return a()
}function h(r){if(r=="name"){y.marked="def";
return M(s)
}if(r=="atom"){return M(s)
}if(r=="op"){return M(h)
}if(r.match(/[\]\)\};,]/)){return a()
}return S(r,h)
}function s(r){if(r=="op"&&n=="."){return M()
}if(n=="to"){y.marked="keyword";
return M(h)
}else{return a()
}}function P(r){if(r=="{"){return M(f("}","alt"),R,L)
}return a()
}function R(r){if(r=="}"){return M()
}if(r=="|"){return M(R)
}if(n=="when"){y.marked="keyword";
return M(C,Q)
}if(r.match(/[\]\);,]/)){return M(R)
}return a(h,Q)
}function Q(r){if(r=="{"){return M(f("}","alt"),l,L,R)
}else{return a(R)
}}function g(r){if(r.match(/[\[\(\{]/)){return S(r,C)
}return a()
}function S(ab,r){if(ab=="["){return M(f("]"),W(r,"]"),L)
}if(ab=="("){return M(f(")"),W(r,")"),L)
}if(ab=="{"){return M(f("}"),W(r,"}"),L)
}return M()
}function u(ac,ad,r){var ae=ac.cc;
y.state=ac;
y.stream=ad;
y.marked=null,y.cc=ae;
while(true){var ab=ae.length?ae.pop():l;
if(ab(p)){while(ae.length&&ae[ae.length-1].lex){ae.pop()()
}return y.marked||r
}}}return{startState:function(){return{tokenize:N,cc:[],lexical:{indented:-z,column:0,type:"top",align:false},keywords:j,indented:0}
},token:function(ac,ab){if(ac.sol()){if(!ab.lexical.hasOwnProperty("align")){ab.lexical.align=false
}ab.indented=ac.indentation()
}if(ac.eatSpace()){return null
}p=n=null;
var r=ab.tokenize(ac,ab);
if(r=="comment"){return r
}if(!ab.lexical.hasOwnProperty("align")){ab.lexical.align=true
}if(p=="prefix"){return r
}if(!n){n=ac.current()
}return u(ab,ac,r)
},indent:function(af,r){if(af.tokenize!=N){return 0
}var ae=r&&r.charAt(0),ac=af.lexical,ad=ac.type,ab=ae==ad;
if(ad=="stat"){return ac.indented+z
}if(ac.align){return ac.column+(ab?0:1)
}return ac.indented+(ab?0:(ac.info=="alt"?J:z))
},electricChars:"{}"}
});
CodeMirror.defineMIME("text/x-rustsrc","rust");