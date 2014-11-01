CodeMirror.defineMIME("text/x-erlang","erlang");
CodeMirror.defineMode("erlang",function(t,u){function o(N,O,M){if(M=="record"){N.context="record"
}else{N.context=false
}if(M!="whitespace"&&M!="comment"){N.lastToken=O.current()
}switch(M){case"atom":return"atom";
case"attribute":return"attribute";
case"builtin":return"builtin";
case"comment":return"comment";
case"fun":return"meta";
case"function":return"tag";
case"guard":return"property";
case"keyword":return"keyword";
case"macro":return"variable-2";
case"number":return"number";
case"operator":return"operator";
case"record":return"bracket";
case"string":return"string";
case"type":return"def";
case"variable":return"variable";
case"error":return"error";
case"separator":return null;
case"open_paren":return null;
case"close_paren":return null;
default:return null
}}var a=["-type","-spec","-export_type","-opaque"];
var I=["after","begin","catch","case","cond","end","fun","if","let","of","query","receive","try","when"];
var l=["->",";",":",".",","];
var h=["and","andalso","band","bnot","bor","bsl","bsr","bxor","div","not","or","orelse","rem","xor"];
var i=["+","-","*","/",">",">=","<","=<","=:=","==","=/=","/=","||","<-"];
var v=["<<","(","[","{"];
var D=["}","]",")",">>"];
var r=["is_atom","is_binary","is_bitstring","is_boolean","is_float","is_function","is_integer","is_list","is_number","is_pid","is_port","is_record","is_reference","is_tuple","atom","binary","bitstring","boolean","function","integer","list","number","pid","port","record","reference","tuple"];
var k=["abs","adler32","adler32_combine","alive","apply","atom_to_binary","atom_to_list","binary_to_atom","binary_to_existing_atom","binary_to_list","binary_to_term","bit_size","bitstring_to_list","byte_size","check_process_code","contact_binary","crc32","crc32_combine","date","decode_packet","delete_module","disconnect_node","element","erase","exit","float","float_to_list","garbage_collect","get","get_keys","group_leader","halt","hd","integer_to_list","internal_bif","iolist_size","iolist_to_binary","is_alive","is_atom","is_binary","is_bitstring","is_boolean","is_float","is_function","is_integer","is_list","is_number","is_pid","is_port","is_process_alive","is_record","is_reference","is_tuple","length","link","list_to_atom","list_to_binary","list_to_bitstring","list_to_existing_atom","list_to_float","list_to_integer","list_to_pid","list_to_tuple","load_module","make_ref","module_loaded","monitor_node","node","node_link","node_unlink","nodes","notalive","now","open_port","pid_to_list","port_close","port_command","port_connect","port_control","pre_loaded","process_flag","process_info","processes","purge_module","put","register","registered","round","self","setelement","size","spawn","spawn_link","spawn_monitor","spawn_opt","split_binary","statistics","term_to_binary","time","throw","tl","trunc","tuple_size","tuple_to_list","unlink","unregister","whereis"];
var K=[",",":","catch","after","of","cond","let","query"];
var p=/[a-z_]/;
var j=/[A-Z_]/;
var q=/[0-9]/;
var E=/[0-7]/;
var H=/[a-z_A-Z0-9]/;
var m=/[\+\-\*\/<>=\|:]/;
var A=/[<\(\[\{]/;
var C=/[>\)\]\}]/;
var w=/[\->\.,:;]/;
function G(M,N){return(-1<N.indexOf(M))
}function f(P,N){var Q=P.start;
var M=N.length;
if(M<=Q){var O=P.string.slice(Q-M,Q);
return O==N
}else{return false
}}function d(P,O){if(P.eatSpace()){return o(O,P,"whitespace")
}if((g(O).token==""||g(O).token==".")&&P.peek()=="-"){P.next();
if(P.eat(p)&&P.eatWhile(H)){if(G(P.current(),a)){return o(O,P,"type")
}else{return o(O,P,"attribute")
}}P.backUp(1)
}var N=P.next();
if(N=="%"){P.skipToEnd();
return o(O,P,"comment")
}if(N=="?"){P.eatWhile(H);
return o(O,P,"macro")
}if(N=="#"){P.eatWhile(H);
return o(O,P,"record")
}if(N=="$"){if(P.next()=="\\"){if(!P.eatWhile(E)){P.next()
}}return o(O,P,"string")
}if(N=="'"){if(s(P)){return o(O,P,"atom")
}else{return o(O,P,"error")
}}if(N=='"'){if(B(P)){return o(O,P,"string")
}else{return o(O,P,"error")
}}if(j.test(N)){P.eatWhile(H);
return o(O,P,"variable")
}if(p.test(N)){P.eatWhile(H);
if(P.peek()=="/"){P.next();
if(P.eatWhile(q)){return o(O,P,"fun")
}else{P.backUp(1);
return o(O,P,"atom")
}}var M=P.current();
if(G(M,I)){J(O,P);
return o(O,P,"keyword")
}if(P.peek()=="("){if(G(M,k)&&(!f(P,":")||f(P,"erlang:"))){return o(O,P,"builtin")
}else{return o(O,P,"function")
}}if(G(M,r)){return o(O,P,"guard")
}if(G(M,h)){return o(O,P,"operator")
}if(P.peek()==":"){if(M=="erlang"){return o(O,P,"builtin")
}else{return o(O,P,"function")
}}return o(O,P,"atom")
}if(q.test(N)){P.eatWhile(q);
if(P.eat("#")){P.eatWhile(q)
}else{if(P.eat(".")){P.eatWhile(q)
}if(P.eat(/[eE]/)){P.eat(/[-+]/);
P.eatWhile(q)
}}return o(O,P,"number")
}if(z(P,A,v)){J(O,P);
return o(O,P,"open_paren")
}if(z(P,C,D)){J(O,P);
return o(O,P,"close_paren")
}if(F(P,w,l)){if(O.context==false){J(O,P)
}return o(O,P,"separator")
}if(F(P,m,i)){return o(O,P,"operator")
}return o(O,P,null)
}function z(O,M,N){if(O.current().length==1&&M.test(O.current())){O.backUp(1);
while(M.test(O.peek())){O.next();
if(G(O.current(),N)){return true
}}O.backUp(O.current().length-1)
}return false
}function F(O,M,N){if(O.current().length==1&&M.test(O.current())){while(M.test(O.peek())){O.next()
}while(0<O.current().length){if(G(O.current(),N)){return true
}else{O.backUp(1)
}}O.next()
}return false
}function B(M){return n(M,'"',"\\")
}function s(M){return n(M,"'","\\")
}function n(P,N,M){while(!P.eol()){var O=P.next();
if(O==N){return true
}else{if(O==M){P.next()
}}}return false
}function e(M){this.token=M?M.current():"";
this.column=M?M.column():0;
this.indent=M?M.indentation():0
}function y(R,N){var M=t.indentUnit;
var O=["after","catch"];
var P=(g(R)).token;
var Q=c(N,/[^a-z]/);
if(G(P,v)){return(g(R)).column+P.length
}else{if(P=="."||P==""){return 0
}else{if(P=="->"){if(Q=="end"){return g(R,2).column
}else{if(g(R,2).token=="fun"){return g(R,2).column+M
}else{return(g(R)).indent+M
}}}else{if(G(Q,O)){return(g(R)).indent
}else{return(g(R)).column+M
}}}}}function c(O,N){var M=O.match(N);
return M?O.slice(0,M.index):O
}function L(M){return M.tokenStack.pop()
}function g(N,P){var M=N.tokenStack.length;
var O=(P?P:1);
if(M<O){return new e
}else{return N.tokenStack[M-O]
}}function J(N,O){var M=O.current();
var P=g(N).token;
if(G(M,K)){return false
}else{if(b(P,M)){L(N);
return false
}else{if(x(P,M)){L(N);
return J(N,O)
}else{N.tokenStack.push(new e(O));
return true
}}}}function x(M,N){switch(M+" "+N){case"when ->":return true;
case"-> end":return true;
case"-> .":return true;
case". .":return true;
default:return false
}}function b(M,N){switch(M+" "+N){case"( )":return true;
case"[ ]":return true;
case"{ }":return true;
case"<< >>":return true;
case"begin end":return true;
case"case end":return true;
case"fun end":return true;
case"if end":return true;
case"receive end":return true;
case"try end":return true;
case"-> ;":return true;
default:return false
}}return{startState:function(){return{tokenStack:[],context:false,lastToken:null}
},token:function(N,M){return d(N,M)
},indent:function(N,M){return y(N,M)
}}
});