/**
 * editor tools
 */
CStudioAuthoring.MediumPanel.IPad = CStudioAuthoring.MediumPanel.IPad || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			
			this.initialized = true;
		}
	},
	
	render: function(containerEl, config) {
		var emulateEl = document.getElementById("cstudio-emulate");
		var mode = "vert";
        
		if(!emulateEl) {
			emulateEl = document.createElement("div");
			emulateEl.id = "cstudio-emulate";
			document.body.appendChild(emulateEl);
		}
		else { 
			emulateEl.innerHTML = "";
            mode = emulateEl.mode; 
		}
		
        if(mode == "vert") {
            emulateEl.style.position = "absolute";
            emulateEl.style.width = "850px";
            emulateEl.style.height = "1100px";
            emulateEl.style.top = "32px";
            emulateEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/ipad/ipad.gif')";
            emulateEl.style.marginLeft = ""+ ((CStudioAuthoring.Utils.viewportWidth()/2)-440) +"px";
            
            var iframeEl = document.createElement("iframe");
            emulateEl.appendChild(iframeEl);
		
            iframeEl.style.border = "none";
            iframeEl.style.height = "885px";
            iframeEl.style.marginLeft = "93px";
            iframeEl.style.marginTop = "105px";
            iframeEl.style.width = "665px";
            iframeEl.style.background = "white";
            iframeEl.style.scrolling = "no";

            var rotateControlEl = document.createElement("div");
            emulateEl.appendChild(rotateControlEl);
        
            rotateControlEl.style.background = "url('"+     CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/ipad/object-rotate-right.png')";

            rotateControlEl.style.width = "30px";
            rotateControlEl.style.height = "32px";
            rotateControlEl.style.position = "absolute";
            rotateControlEl.style.top = "5px";
            rotateControlEl.style.left = "850px";
            rotateControlEl.style.cursor = "pointer";
            rotateControlEl.mode = "vert";
        }
        else {
            emulateEl.style.position = "absolute";
            emulateEl.style.width = "1100px";
            emulateEl.style.height = "850px";
            emulateEl.style.top = "32px";
            emulateEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/ipad/ipad-hozbg.gif') repeat scroll 0px 0pt transparent";
            emulateEl.style.marginLeft = ""+ ((CStudioAuthoring.Utils.viewportWidth()/2)-540) +"px";
	
            var iframeEl = document.createElement("iframe");
            emulateEl.appendChild(iframeEl);
		
            iframeEl.style.border = "none";
            iframeEl.style.height = "663px";
            iframeEl.style.marginLeft = "110px";
            iframeEl.style.marginTop = "92px";
            iframeEl.style.width = "884px";
            iframeEl.style.background = "white";
            iframeEl.style.scrolling = "no";
        
            var rotateControlEl = document.createElement("div");
            emulateEl.appendChild(rotateControlEl);
        
            rotateControlEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/ipad/object-rotate-left.png')";

            rotateControlEl.style.width = "30px";
            rotateControlEl.style.height = "32px";
            rotateControlEl.style.position = "absolute";
            rotateControlEl.style.top = "5px";
            rotateControlEl.style.left = "1100px";
            rotateControlEl.style.cursor = "pointer";
            rotateControlEl.mode = "horiz";
        }

        rotateControlEl.control = emulateEl;
        rotateControlEl.controller = this;
        
        rotateControlEl.onclick = function() {
            if(this.mode == "vert") {
                this.control.mode = "horiz";
            }
            else {
                this.control.mode = "vert";            
            }
            
            this.controller.render();
        }

        var location = document.location.href;
        if(location.indexOf("?") != -1) {
            location += "&cstudio-useragent=ipad";  
        }
        else {
            location += "?cstudio-useragent=ipad";
        }
        
        iframeEl.onload = function() {
        var els = YAHOO.util.Dom.getElementsBy(
            function(el){ return true; }, 
            "a", 
            this.contentDocument.body, 
            function(el){ return true; });
            

            for(var k=0; k<els.length; k++) {
                var link = els[k].href;
                
                if(link.indexOf("#") == -1) {
                    if(link.indexOf("?") != -1) {
                        link += "&cstudio-useragent=ipad";  
                    }
                    else {
                        link += "?cstudio-useragent=ipad";    
                    }
            
                    els[k].href = ""+link;
                }
            }
        }
        
        iframeEl.src = location;
	}
}

CStudioAuthoring.Module.moduleLoaded("medium-panel-ipad", CStudioAuthoring.MediumPanel.IPad);