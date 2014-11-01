$(document).ready(function(){module("testMultiAttr");
test("test1",function(){expect(1);
var c='<span class="cm-tag">&lt;p </span><span class="cm-attribute">a1</span>=<span class="cm-string">"foo"</span> <span class="cm-attribute">a2</span>=<span class="cm-string">"bar"</span><span class="cm-tag">&gt;</span><span class="cm-word">hello</span> <span class="cm-word">world</span><span class="cm-tag">&lt;/p&gt;</span>';
$("#sandbox").html('<textarea id="editor"></textarea>');
$("#editor").html('<p a1="foo" a2="bar">hello world</p>');
var b=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,c);
$("#editor").html("")
})
});