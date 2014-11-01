CodeMirror.defineMode("scheme",function(y,j){var m="builtin",r="comment",i="string",d="atom",l="number",g="bracket",h="keyword";
var p=2,f=1;
function v(C){var A={},B=C.split(" ");
for(var z=0;
z<B.length;
++z){A[B[z]]=true
}return A
}var e=v("λ case-lambda call/cc class define-class exit-handler field import inherit init-field interface let*-values let-values let/ec mixin opt-lambda override protect provide public rename require require-for-syntax syntax syntax-case syntax-error unit/sig unless when with-syntax and begin call-with-current-continuation call-with-input-file call-with-output-file case cond define define-syntax delay do dynamic-wind else for-each if lambda let let* let-syntax letrec letrec-syntax map or syntax-rules abs acos angle append apply asin assoc assq assv atan boolean? caar cadr call-with-input-file call-with-output-file call-with-values car cdddar cddddr cdr ceiling char->integer char-alphabetic? char-ci<=? char-ci<? char-ci=? char-ci>=? char-ci>? char-downcase char-lower-case? char-numeric? char-ready? char-upcase char-upper-case? char-whitespace? char<=? char<? char=? char>=? char>? char? close-input-port close-output-port complex? cons cos current-input-port current-output-port denominator display eof-object? eq? equal? eqv? eval even? exact->inexact exact? exp expt #f floor force gcd imag-part inexact->exact inexact? input-port? integer->char integer? interaction-environment lcm length list list->string list->vector list-ref list-tail list? load log magnitude make-polar make-rectangular make-string make-vector max member memq memv min modulo negative? newline not null-environment null? number->string number? numerator odd? open-input-file open-output-file output-port? pair? peek-char port? positive? procedure? quasiquote quote quotient rational? rationalize read read-char real-part real? remainder reverse round scheme-report-environment set! set-car! set-cdr! sin sqrt string string->list string->number string->symbol string-append string-ci<=? string-ci<? string-ci=? string-ci>=? string-ci>? string-copy string-fill! string-length string-ref string-set! string<=? string<? string=? string>=? string>? string? substring symbol->string symbol? #t tan transcript-off transcript-on truncate values vector vector->list vector-fill! vector-length vector-ref vector-set! with-input-from-file with-output-to-file write write-char zero?");
var q=v("define let letrec let* lambda");
function c(z,A,B){this.indent=z;
this.type=A;
this.prev=B
}function s(B,z,A){B.indentStack=new c(z,A,B.indentStack)
}function n(z){z.indentStack=z.indentStack.prev
}var b=new RegExp(/^(?:[-+]i|[-+][01]+#*(?:\/[01]+#*)?i|[-+]?[01]+#*(?:\/[01]+#*)?@[-+]?[01]+#*(?:\/[01]+#*)?|[-+]?[01]+#*(?:\/[01]+#*)?[-+](?:[01]+#*(?:\/[01]+#*)?)?i|[-+]?[01]+#*(?:\/[01]+#*)?)(?=[()\s;"]|$)/i);
var x=new RegExp(/^(?:[-+]i|[-+][0-7]+#*(?:\/[0-7]+#*)?i|[-+]?[0-7]+#*(?:\/[0-7]+#*)?@[-+]?[0-7]+#*(?:\/[0-7]+#*)?|[-+]?[0-7]+#*(?:\/[0-7]+#*)?[-+](?:[0-7]+#*(?:\/[0-7]+#*)?)?i|[-+]?[0-7]+#*(?:\/[0-7]+#*)?)(?=[()\s;"]|$)/i);
var t=new RegExp(/^(?:[-+]i|[-+][\da-f]+#*(?:\/[\da-f]+#*)?i|[-+]?[\da-f]+#*(?:\/[\da-f]+#*)?@[-+]?[\da-f]+#*(?:\/[\da-f]+#*)?|[-+]?[\da-f]+#*(?:\/[\da-f]+#*)?[-+](?:[\da-f]+#*(?:\/[\da-f]+#*)?)?i|[-+]?[\da-f]+#*(?:\/[\da-f]+#*)?)(?=[()\s;"]|$)/i);
var u=new RegExp(/^(?:[-+]i|[-+](?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*)i|[-+]?(?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*)@[-+]?(?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*)|[-+]?(?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*)[-+](?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*)?i|(?:(?:(?:\d+#+\.?#*|\d+\.\d*#*|\.\d+#*|\d+)(?:[esfdl][-+]?\d+)?)|\d+#*\/\d+#*))(?=[()\s;"]|$)/i);
function a(z){return z.match(b)
}function k(z){return z.match(x)
}function w(A,z){if(z===true){A.backUp(1)
}return A.match(u)
}function o(z){return z.match(t)
}return{startState:function(){return{indentStack:null,indentation:0,mode:false,sExprComment:false}
},token:function(L,B){if(B.indentStack==null&&L.sol()){B.indentation=L.indentation()
}if(L.eatSpace()){return null
}var H=null;
switch(B.mode){case"string":var F,A=false;
while((F=L.next())!=null){if(F=='"'&&!A){B.mode=false;
break
}A=!A&&F=="\\"
}H=i;
break;
case"comment":var F,I=false;
while((F=L.next())!=null){if(F=="#"&&I){B.mode=false;
break
}I=(F=="|")
}H=r;
break;
case"s-expr-comment":B.mode=false;
if(L.peek()=="("||L.peek()=="["){B.sExprComment=0
}else{L.eatWhile(/[^/s]/);
H=r;
break
}default:var z=L.next();
if(z=='"'){B.mode="string";
H=i
}else{if(z=="'"){H=d
}else{if(z=="#"){if(L.eat("|")){B.mode="comment";
H=r
}else{if(L.eat(/[tf]/i)){H=d
}else{if(L.eat(";")){B.mode="s-expr-comment";
H=r
}else{var D=null,E=false,K=true;
if(L.eat(/[ei]/i)){E=true
}else{L.backUp(1)
}if(L.match(/^#b/i)){D=a
}else{if(L.match(/^#o/i)){D=k
}else{if(L.match(/^#x/i)){D=o
}else{if(L.match(/^#d/i)){D=w
}else{if(L.match(/^[-+0-9.]/,false)){K=false;
D=w
}else{if(!E){L.eat("#")
}}}}}}if(D!=null){if(K&&!E){L.match(/^#[ei]/i)
}if(D(L)){H=l
}}}}}}else{if(/^[-+0-9.]/.test(z)&&w(L,true)){H=l
}else{if(z==";"){L.skipToEnd();
H=r
}else{if(z=="("||z=="["){var J="";
var G=L.column(),C;
while((C=L.eat(/[^\s\(\[\;\)\]]/))!=null){J+=C
}if(J.length>0&&q.propertyIsEnumerable(J)){s(B,G+p,z)
}else{L.eatSpace();
if(L.eol()||L.peek()==";"){s(B,G+1,z)
}else{s(B,G+L.current().length,z)
}}L.backUp(L.current().length-1);
if(typeof B.sExprComment=="number"){B.sExprComment++
}H=g
}else{if(z==")"||z=="]"){H=g;
if(B.indentStack!=null&&B.indentStack.type==(z==")"?"(":"[")){n(B);
if(typeof B.sExprComment=="number"){if(--B.sExprComment==0){H=r;
B.sExprComment=false
}}}}else{L.eatWhile(/[\w\$_\-!$%&*+\.\/:<=>?@\^~]/);
if(e&&e.propertyIsEnumerable(L.current())){H=m
}else{H="variable"
}}}}}}}}}return(typeof B.sExprComment=="number")?r:H
},indent:function(A,z){if(A.indentStack==null){return A.indentation
}return A.indentStack.indent
}}
});
CodeMirror.defineMIME("text/x-scheme","scheme");