CodeMirror.defineMode("haskell",function(h,k){function l(s,t,r){t(r);
return r(s,t)
}var d=/[a-z_]/;
var p=/[A-Z]/;
var m=/[0-9]/;
var c=/[0-9A-Fa-f]/;
var q=/[0-7]/;
var o=/[a-z_A-Z0-9']/;
var n=/[-!#$%&*+.\/<=>?@\\^|~:]/;
var e=/[(),;[\]`{}]/;
var g=/[ \t\v\f]/;
function i(u,v){if(u.eatWhile(g)){return null
}var s=u.next();
if(e.test(s)){if(s=="{"&&u.eat("-")){var r="comment";
if(u.eat("#")){r="meta"
}return l(u,v,j(r,1))
}return null
}if(s=="'"){if(u.eat("\\")){u.next()
}else{u.next()
}if(u.eat("'")){return"string"
}return"error"
}if(s=='"'){return l(u,v,f)
}if(p.test(s)){u.eatWhile(o);
if(u.eat(".")){return"qualifier"
}return"variable-2"
}if(d.test(s)){u.eatWhile(o);
return"variable"
}if(m.test(s)){if(s=="0"){if(u.eat(/[xX]/)){u.eatWhile(c);
return"integer"
}if(u.eat(/[oO]/)){u.eatWhile(q);
return"number"
}}u.eatWhile(m);
var r="number";
if(u.eat(".")){r="number";
u.eatWhile(m)
}if(u.eat(/[eE]/)){r="number";
u.eat(/[-+]/);
u.eatWhile(m)
}return r
}if(n.test(s)){if(s=="-"&&u.eat(/-/)){u.eatWhile(/-/);
if(!u.eat(n)){u.skipToEnd();
return"comment"
}}var r="variable";
if(s==":"){r="variable-2"
}u.eatWhile(n);
return r
}return"error"
}function j(r,s){if(s==0){return i
}return function(v,w){var t=s;
while(!v.eol()){var u=v.next();
if(u=="{"&&v.eat("-")){++t
}else{if(u=="-"&&v.eat("}")){--t;
if(t==0){w(i);
return r
}}}}w(j(r,t));
return r
}
}function f(s,t){while(!s.eol()){var r=s.next();
if(r=='"'){t(i);
return"string"
}if(r=="\\"){if(s.eol()||s.eat(g)){t(b);
return"string"
}if(s.eat("&")){}else{s.next()
}}}t(i);
return"error"
}function b(r,s){if(r.eat("\\")){return l(r,s,f)
}r.next();
s(i);
return"error"
}var a=(function(){var r={};
function s(u){return function(){for(var t=0;
t<arguments.length;
t++){r[arguments[t]]=u
}}
}s("keyword")("case","class","data","default","deriving","do","else","foreign","if","import","in","infix","infixl","infixr","instance","let","module","newtype","of","then","type","where","_");
s("keyword")("..",":","::","=","\\",'"',"<-","->","@","~","=>");
s("builtin")("!!","$!","$","&&","+","++","-",".","/","/=","<","<=","=<<","==",">",">=",">>",">>=","^","^^","||","*","**");
s("builtin")("Bool","Bounded","Char","Double","EQ","Either","Enum","Eq","False","FilePath","Float","Floating","Fractional","Functor","GT","IO","IOError","Int","Integer","Integral","Just","LT","Left","Maybe","Monad","Nothing","Num","Ord","Ordering","Rational","Read","ReadS","Real","RealFloat","RealFrac","Right","Show","ShowS","String","True");
s("builtin")("abs","acos","acosh","all","and","any","appendFile","asTypeOf","asin","asinh","atan","atan2","atanh","break","catch","ceiling","compare","concat","concatMap","const","cos","cosh","curry","cycle","decodeFloat","div","divMod","drop","dropWhile","either","elem","encodeFloat","enumFrom","enumFromThen","enumFromThenTo","enumFromTo","error","even","exp","exponent","fail","filter","flip","floatDigits","floatRadix","floatRange","floor","fmap","foldl","foldl1","foldr","foldr1","fromEnum","fromInteger","fromIntegral","fromRational","fst","gcd","getChar","getContents","getLine","head","id","init","interact","ioError","isDenormalized","isIEEE","isInfinite","isNaN","isNegativeZero","iterate","last","lcm","length","lex","lines","log","logBase","lookup","map","mapM","mapM_","max","maxBound","maximum","maybe","min","minBound","minimum","mod","negate","not","notElem","null","odd","or","otherwise","pi","pred","print","product","properFraction","putChar","putStr","putStrLn","quot","quotRem","read","readFile","readIO","readList","readLn","readParen","reads","readsPrec","realToFrac","recip","rem","repeat","replicate","return","reverse","round","scaleFloat","scanl","scanl1","scanr","scanr1","seq","sequence","sequence_","show","showChar","showList","showParen","showString","shows","showsPrec","significand","signum","sin","sinh","snd","span","splitAt","sqrt","subtract","succ","sum","tail","take","takeWhile","tan","tanh","toEnum","toInteger","toRational","truncate","uncurry","undefined","unlines","until","unwords","unzip","unzip3","userError","words","writeFile","zip","zip3","zipWith","zipWith3");
return r
})();
return{startState:function(){return{f:i}
},copyState:function(r){return{f:r.f}
},token:function(v,u){var s=u.f(v,function(t){u.f=t
});
var r=v.current();
return(r in a)?a[r]:s
}}
});
CodeMirror.defineMIME("text/x-haskell","haskell");