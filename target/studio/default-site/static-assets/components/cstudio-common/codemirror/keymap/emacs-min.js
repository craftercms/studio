(function(){var b=[];
function a(e){b.push(e);
if(b.length>50){b.shift()
}}function d(){return b[b.length-1]||""
}function c(){if(b.length>1){b.pop()
}return d()
}CodeMirror.keyMap.emacs={"Ctrl-X":function(e){e.setOption("keyMap","emacs-Ctrl-X")
},"Ctrl-W":function(e){a(e.getSelection());
e.replaceSelection("")
},"Ctrl-Alt-W":function(e){a(e.getSelection());
e.replaceSelection("")
},"Alt-W":function(e){a(e.getSelection())
},"Ctrl-Y":function(e){e.replaceSelection(d())
},"Alt-Y":function(e){e.replaceSelection(c())
},"Ctrl-/":"undo","Shift-Ctrl--":"undo","Shift-Alt-,":"goDocStart","Shift-Alt-.":"goDocEnd","Ctrl-S":"findNext","Ctrl-R":"findPrev","Ctrl-G":"clearSearch","Shift-Alt-5":"replace","Ctrl-Z":"undo","Cmd-Z":"undo","Alt-/":"autocomplete",fallthrough:["basic","emacsy"]};
CodeMirror.keyMap["emacs-Ctrl-X"]={"Ctrl-S":"save","Ctrl-W":"save",S:"saveAll",F:"open",U:"undo",K:"close",auto:"emacs",nofallthrough:true}
})();