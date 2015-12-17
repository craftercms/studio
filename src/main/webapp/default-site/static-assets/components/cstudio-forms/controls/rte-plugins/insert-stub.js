CStudioForms.Controls.RTE.InsertStub = CStudioForms.Controls.RTE.InsertStub || {
    createControl: function(n, cm, editor) {
        switch (n) {
            case 'insertStub':
				var config = tinyMCE.activeEditor.contextControl.rteConfig;
				var layouts = config.rteStubs.stub;
				
				if(!layouts) {
					layouts = [];
				}
				
				if(!layouts.length) {
					layouts = [layouts];
				}

				if(layouts.length > 0) {	
					var c = cm.createMenuButton('insertStub', {
	                	title : 'Insert Stub',
	                    //image : 'img/example.gif',
	                    style: "mce_insertComponentPencil",
//	                    icons : false
	                });
	                
	                c.layouts = layouts;
	
	 				c.onRenderMenu.add(function(c, m) {
						for(var i=0; i<layouts.length; i++) {
							var layout = layouts[i];
                            var prototype = layout.prototype;
                           
                            
                            var onClickFn = function() {
		                    	tinyMCE.activeEditor.execCommand('mceInsertContent', false, this.layoutPrototype);
		                    	ed.contextControl.save();
		                   	};
		                   	
		                   	var layoutItem = {title : layout.name, onclick : onClickFn, layoutPrototype: prototype}
 
 		                	m.add(layoutItem);
				        };
					});

			        return c;
				}
				else {
					// no layouts to render
				}
			};
	
	        return null;
    }
}

tinymce.create('tinymce.plugins.CStudioInsertStubPlugin', CStudioForms.Controls.RTE.InsertStub);

// Register plugin with a short name
tinymce.PluginManager.add('insertstub', tinymce.plugins.CStudioInsertStubPlugin);

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-stub", CStudioForms.Controls.RTE.InsertStub);