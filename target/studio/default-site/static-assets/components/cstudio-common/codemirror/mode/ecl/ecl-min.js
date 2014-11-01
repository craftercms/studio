CodeMirror.defineMode("ecl",function(t){function i(z){var x={},y=z.split(" ");
for(var w=0;
w<y.length;
++w){x[y[w]]=true
}return x
}function b(x,w){if(!w.startOfLine){return false
}x.skipToEnd();
return"meta"
}function r(y,x){var w;
while((w=y.next())!=null){if(w=='"'&&!y.eat('"')){x.tokenize=null;
break
}}return"string"
}var k=t.indentUnit;
var s=i("abs acos allnodes ascii asin asstring atan atan2 ave case choose choosen choosesets clustersize combine correlation cos cosh count covariance cron dataset dedup define denormalize distribute distributed distribution ebcdic enth error evaluate event eventextra eventname exists exp failcode failmessage fetch fromunicode getisvalid global graph group hash hash32 hash64 hashcrc hashmd5 having if index intformat isvalid iterate join keyunicode length library limit ln local log loop map matched matchlength matchposition matchtext matchunicode max merge mergejoin min nolocal nonempty normalize parse pipe power preload process project pull random range rank ranked realformat recordof regexfind regexreplace regroup rejected rollup round roundup row rowdiff sample set sin sinh sizeof soapcall sort sorted sqrt stepped stored sum table tan tanh thisnode topn tounicode transfer trim truncate typeof ungroup unicodeorder variance which workunit xmldecode xmlencode xmltext xmlunicode");
var o=i("apply assert build buildindex evaluate fail keydiff keypatch loadxml nothor notify output parallel sequential soapcall wait");
var m=i("__compressed__ all and any as atmost before beginc++ best between case const counter csv descend encrypt end endc++ endmacro except exclusive expire export extend false few first flat from full function group header heading hole ifblock import in interface joined keep keyed last left limit load local locale lookup macro many maxcount maxlength min skew module named nocase noroot noscan nosort not of only opt or outer overwrite packed partition penalty physicallength pipe quote record relationship repeat return right scan self separator service shared skew skip sql store terminator thor threshold token transform trim true type unicodeorder unsorted validate virtual whole wild within xml xpath");
var j=i("ascii big_endian boolean data decimal ebcdic integer pattern qstring real record rule set of string token udecimal unicode unsigned varstring varunicode");
var u=i("checkpoint deprecated failcode failmessage failure global independent onwarning persist priority recovery stored success wait when");
var l=i("catch class do else finally for if switch try while");
var c=i("true false null");
var a={"#":b};
var g;
var f=/[+\-*&%=<>!?|\/]/;
var p;
function v(C,A){var y=C.next();
if(a[y]){var w=a[y](C,A);
if(w!==false){return w
}}if(y=='"'||y=="'"){A.tokenize=e(y);
return A.tokenize(C,A)
}if(/[\[\]{}\(\),;\:\.]/.test(y)){p=y;
return null
}if(/\d/.test(y)){C.eatWhile(/[\w\.]/);
return"number"
}if(y=="/"){if(C.eat("*")){A.tokenize=h;
return h(C,A)
}if(C.eat("/")){C.skipToEnd();
return"comment"
}}if(f.test(y)){C.eatWhile(f);
return"operator"
}C.eatWhile(/[\w\$_]/);
var B=C.current().toLowerCase();
if(s.propertyIsEnumerable(B)){if(l.propertyIsEnumerable(B)){p="newstatement"
}return"keyword"
}else{if(o.propertyIsEnumerable(B)){if(l.propertyIsEnumerable(B)){p="newstatement"
}return"variable"
}else{if(m.propertyIsEnumerable(B)){if(l.propertyIsEnumerable(B)){p="newstatement"
}return"variable-2"
}else{if(j.propertyIsEnumerable(B)){if(l.propertyIsEnumerable(B)){p="newstatement"
}return"variable-3"
}else{if(u.propertyIsEnumerable(B)){if(l.propertyIsEnumerable(B)){p="newstatement"
}return"builtin"
}else{var x=B.length-1;
while(x>=0&&(!isNaN(B[x])||B[x]=="_")){--x
}if(x>0){var z=B.substr(0,x+1);
if(j.propertyIsEnumerable(z)){if(l.propertyIsEnumerable(z)){p="newstatement"
}return"variable-3"
}}}}}}}if(c.propertyIsEnumerable(B)){return"atom"
}return null
}function e(w){return function(B,z){var A=false,y,x=false;
while((y=B.next())!=null){if(y==w&&!A){x=true;
break
}A=!A&&y=="\\"
}if(x||!(A||g)){z.tokenize=v
}return"string"
}
}function h(z,y){var w=false,x;
while(x=z.next()){if(x=="/"&&w){y.tokenize=v;
break
}w=(x=="*")
}return"comment"
}function n(A,x,w,z,y){this.indented=A;
this.column=x;
this.type=w;
this.align=z;
this.prev=y
}function d(y,w,x){return y.context=new n(y.indented,w,x,null,y.context)
}function q(x){var w=x.context.type;
if(w==")"||w=="]"||w=="}"){x.indented=x.context.indented
}return x.context=x.context.prev
}return{startState:function(w){return{tokenize:null,context:new n((w||0)-k,0,"top",false),indented:0,startOfLine:true}
},token:function(z,y){var w=y.context;
if(z.sol()){if(w.align==null){w.align=false
}y.indented=z.indentation();
y.startOfLine=true
}if(z.eatSpace()){return null
}p=null;
var x=(y.tokenize||v)(z,y);
if(x=="comment"||x=="meta"){return x
}if(w.align==null){w.align=true
}if((p==";"||p==":")&&w.type=="statement"){q(y)
}else{if(p=="{"){d(y,z.column(),"}")
}else{if(p=="["){d(y,z.column(),"]")
}else{if(p=="("){d(y,z.column(),")")
}else{if(p=="}"){while(w.type=="statement"){w=q(y)
}if(w.type=="}"){w=q(y)
}while(w.type=="statement"){w=q(y)
}}else{if(p==w.type){q(y)
}else{if(w.type=="}"||w.type=="top"||(w.type=="statement"&&p=="newstatement")){d(y,z.column(),"statement")
}}}}}}}y.startOfLine=false;
return x
},indent:function(A,x){if(A.tokenize!=v&&A.tokenize!=null){return 0
}var w=A.context,z=x&&x.charAt(0);
if(w.type=="statement"&&z=="}"){w=w.prev
}var y=z==w.type;
if(w.type=="statement"){return w.indented+(z=="{"?0:k)
}else{if(w.align){return w.column+(y?0:1)
}else{return w.indented+(y?0:k)
}}},electricChars:"{}"}
});
CodeMirror.defineMIME("text/x-ecl","ecl");