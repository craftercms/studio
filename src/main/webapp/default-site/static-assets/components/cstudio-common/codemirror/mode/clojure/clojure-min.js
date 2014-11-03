CodeMirror.defineMode("clojure",function(v,l){var p="builtin",s="comment",k="string",a="tag",f="atom",o="number",i="bracket",j="keyword";
var q=2,h=1;
function u(z){var x={},y=z.split(" ");
for(var w=0;
w<y.length;
++w){x[y[w]]=true
}return x
}var b=u("true false nil");
var g=u("defn defn- def def- defonce defmulti defmethod defmacro defstruct deftype defprotocol defrecord defproject deftest slice defalias defhinted defmacro- defn-memo defnk defnk defonce- defunbound defunbound- defvar defvar- let letfn do case cond condp for loop recur when when-not when-let when-first if if-let if-not . .. -> ->> doto and or dosync doseq dotimes dorun doall load import unimport ns in-ns refer try catch finally throw with-open with-local-vars binding gen-class gen-and-load-class gen-and-save-class handler-case handle");
var d=u("* *1 *2 *3 *agent* *allow-unresolved-vars* *assert *clojure-version* *command-line-args* *compile-files* *compile-path* *e *err* *file* *flush-on-newline* *in* *macro-meta* *math-context* *ns* *out* *print-dup* *print-length* *print-level* *print-meta* *print-readably* *read-eval* *source-path* *use-context-classloader* *warn-on-reflection* + - / < <= = == > >= accessor aclone agent agent-errors aget alength alias all-ns alter alter-meta! alter-var-root amap ancestors and apply areduce array-map aset aset-boolean aset-byte aset-char aset-double aset-float aset-int aset-long aset-short assert assoc assoc! assoc-in associative? atom await await-for await1 bases bean bigdec bigint binding bit-and bit-and-not bit-clear bit-flip bit-not bit-or bit-set bit-shift-left bit-shift-right bit-test bit-xor boolean boolean-array booleans bound-fn bound-fn* butlast byte byte-array bytes case cast char char-array char-escape-string char-name-string char? chars chunk chunk-append chunk-buffer chunk-cons chunk-first chunk-next chunk-rest chunked-seq? class class? clear-agent-errors clojure-version coll? comment commute comp comparator compare compare-and-set! compile complement concat cond condp conj conj! cons constantly construct-proxy contains? count counted? create-ns create-struct cycle dec decimal? declare definline defmacro defmethod defmulti defn defn- defonce defstruct delay delay? deliver deref derive descendants destructure disj disj! dissoc dissoc! distinct distinct? doall doc dorun doseq dosync dotimes doto double double-array doubles drop drop-last drop-while empty empty? ensure enumeration-seq eval even? every? extend extend-protocol extend-type extends? extenders false? ffirst file-seq filter find find-doc find-ns find-var first float float-array float? floats flush fn fn? fnext for force format future future-call future-cancel future-cancelled? future-done? future? gen-class gen-interface gensym get get-in get-method get-proxy-class get-thread-bindings get-validator hash hash-map hash-set identical? identity if-let if-not ifn? import in-ns inc init-proxy instance? int int-array integer? interleave intern interpose into into-array ints io! isa? iterate iterator-seq juxt key keys keyword keyword? last lazy-cat lazy-seq let letfn line-seq list list* list? load load-file load-reader load-string loaded-libs locking long long-array longs loop macroexpand macroexpand-1 make-array make-hierarchy map map? mapcat max max-key memfn memoize merge merge-with meta method-sig methods min min-key mod name namespace neg? newline next nfirst nil? nnext not not-any? not-empty not-every? not= ns ns-aliases ns-imports ns-interns ns-map ns-name ns-publics ns-refers ns-resolve ns-unalias ns-unmap nth nthnext num number? odd? or parents partial partition pcalls peek persistent! pmap pop pop! pop-thread-bindings pos? pr pr-str prefer-method prefers primitives-classnames print print-ctor print-doc print-dup print-method print-namespace-doc print-simple print-special-doc print-str printf println println-str prn prn-str promise proxy proxy-call-with-super proxy-mappings proxy-name proxy-super push-thread-bindings pvalues quot rand rand-int range ratio? rational? rationalize re-find re-groups re-matcher re-matches re-pattern re-seq read read-line read-string reify reduce ref ref-history-count ref-max-history ref-min-history ref-set refer refer-clojure release-pending-sends rem remove remove-method remove-ns repeat repeatedly replace replicate require reset! reset-meta! resolve rest resultset-seq reverse reversible? rseq rsubseq satisfies? second select-keys send send-off seq seq? seque sequence sequential? set set-validator! set? short short-array shorts shutdown-agents slurp some sort sort-by sorted-map sorted-map-by sorted-set sorted-set-by sorted? special-form-anchor special-symbol? split-at split-with str stream? string? struct struct-map subs subseq subvec supers swap! symbol symbol? sync syntax-symbol-anchor take take-last take-nth take-while test the-ns time to-array to-array-2d trampoline transient tree-seq true? type unchecked-add unchecked-dec unchecked-divide unchecked-inc unchecked-multiply unchecked-negate unchecked-remainder unchecked-subtract underive unquote unquote-splicing update-in update-proxy use val vals var-get var-set var? vary-meta vec vector vector? when when-first when-let when-not while with-bindings with-bindings* with-in-str with-loading-context with-local-vars with-meta with-open with-out-str with-precision xml-seq");
var r=u("ns fn def defn defmethod bound-fn if if-not case condp when while when-not when-first do future comment doto locking proxy with-open with-precision reify deftype defrecord defprotocol extend extend-protocol extend-type try catch let letfn binding loop for doseq dotimes when-let if-let defstruct struct-map assoc testing deftest handler-case handle dotrace deftrace");
var e={digit:/\d/,digit_or_colon:/[\d:]/,hex:/[0-9a-f]/i,sign:/[+-]/,exponent:/e/i,keyword_char:/[^\s\(\[\;\)\]]/,basic:/[\w\$_\-]/,lang_keyword:/[\w*+!\-_?:\/]/};
function c(w,x,y){this.indent=w;
this.type=x;
this.prev=y
}function t(y,w,x){y.indentStack=new c(w,x,y.indentStack)
}function n(w){w.indentStack=w.indentStack.prev
}function m(w,x){if(w==="0"&&x.eat(/x/i)){x.eatWhile(e.hex);
return true
}if((w=="+"||w=="-")&&(e.digit.test(x.peek()))){x.eat(e.sign);
w=x.next()
}if(e.digit.test(w)){x.eat(w);
x.eatWhile(e.digit);
if("."==x.peek()){x.eat(".");
x.eatWhile(e.digit)
}if(x.eat(e.exponent)){x.eat(e.sign);
x.eatWhile(e.digit)
}return true
}return false
}return{startState:function(){return{indentStack:null,indentation:0,mode:false}
},token:function(E,y){if(y.indentStack==null&&E.sol()){y.indentation=E.indentation()
}if(E.eatSpace()){return null
}var C=null;
switch(y.mode){case"string":var A,x=false;
while((A=E.next())!=null){if(A=='"'&&!x){y.mode=false;
break
}x=!x&&A=="\\"
}C=k;
break;
default:var w=E.next();
if(w=='"'){y.mode="string";
C=k
}else{if(w=="'"&&!(e.digit_or_colon.test(E.peek()))){C=f
}else{if(w==";"){E.skipToEnd();
C=s
}else{if(m(w,E)){C=o
}else{if(w=="("||w=="["){var D="",B=E.column(),z;
if(w=="("){while((z=E.eat(e.keyword_char))!=null){D+=z
}}if(D.length>0&&(r.propertyIsEnumerable(D)||/^(?:def|with)/.test(D))){t(y,B+q,w)
}else{E.eatSpace();
if(E.eol()||E.peek()==";"){t(y,B+1,w)
}else{t(y,B+E.current().length,w)
}}E.backUp(E.current().length-1);
C=i
}else{if(w==")"||w=="]"){C=i;
if(y.indentStack!=null&&y.indentStack.type==(w==")"?"(":"[")){n(y)
}}else{if(w==":"){E.eatWhile(e.lang_keyword);
return f
}else{E.eatWhile(e.basic);
if(g&&g.propertyIsEnumerable(E.current())){C=j
}else{if(d&&d.propertyIsEnumerable(E.current())){C=p
}else{if(b&&b.propertyIsEnumerable(E.current())){C=f
}else{C=null
}}}}}}}}}}}return C
},indent:function(x,w){if(x.indentStack==null){return x.indentation
}return x.indentStack.indent
}}
});
CodeMirror.defineMIME("text/x-clojure","clojure");