$(document).ready(function(){module("testEmptySequenceKeyword");
test("testEmptySequenceKeyword",function(){expect(1);
var b='"foo" instance of empty-sequence()';
var d='<span class="cm-string">"foo"</span> <span class="cm-keyword">instance</span> <span class="cm-keyword">of</span> <span class="cm-keyword">empty-sequence</span>()';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
})
});