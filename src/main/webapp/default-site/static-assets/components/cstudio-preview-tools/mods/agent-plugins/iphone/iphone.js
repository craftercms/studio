/**
 * editor tools
 */
CStudioAuthoring.MediumPanel.IPhoneVert = CStudioAuthoring.MediumPanel.IPhoneVert || {

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
            emulateEl.style.width = "399px";
            emulateEl.style.height = "746px";
            emulateEl.style.top = "32px";
            emulateEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/iphone/iphonebg.png')";
            emulateEl.style.marginLeft = ""+ ((CStudioAuthoring.Utils.viewportWidth()/2)-200) +"px";
	
            var iframeEl = document.createElement("iframe");
            emulateEl.appendChild(iframeEl);
		
            iframeEl.style.border = "none";
            iframeEl.style.height = "461px";
            iframeEl.style.marginLeft = "28px";
            iframeEl.style.marginTop = "153px";
            iframeEl.style.width = "340px";
            iframeEl.style.background = "white";
            iframeEl.style.scrolling = "no";
        
            var rotateControlEl = document.createElement("div");
            emulateEl.appendChild(rotateControlEl);
        
            rotateControlEl.style.background = "url('"+     CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/iphone/object-rotate-right.png')";

            rotateControlEl.style.width = "30px";
            rotateControlEl.style.height = "32px";
            rotateControlEl.style.position = "absolute";
            rotateControlEl.style.top = "5px";
            rotateControlEl.style.left = "377px";
            rotateControlEl.style.cursor = "pointer";
            rotateControlEl.mode = "vert";
        }
        else {
            emulateEl.style.position = "absolute";
            emulateEl.style.width = "750px";
            emulateEl.style.height = "390px";
            emulateEl.style.top = "32px";
            emulateEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/iphone/iphone-hozbg.png') repeat scroll -125px 0pt transparent";
            emulateEl.style.marginLeft = ""+ ((CStudioAuthoring.Utils.viewportWidth()/2)-400) +"px";
	
            var iframeEl = document.createElement("iframe");
            emulateEl.appendChild(iframeEl);
		
            iframeEl.style.border = "none";
            iframeEl.style.height = "320px";
            iframeEl.style.marginLeft = "131px";
            iframeEl.style.marginTop = "38px";
            iframeEl.style.width = "490px";
            iframeEl.style.background = "white";
            iframeEl.style.scrolling = "no";
        
            var rotateControlEl = document.createElement("div");
            emulateEl.appendChild(rotateControlEl);
        
            rotateControlEl.style.background = "url('"+ CStudioAuthoringContext.authoringAppBaseUri + "/components/cstudio-preview-tools/mods/agent-plugins/iphone/object-rotate-left.png')";

            rotateControlEl.style.width = "30px";
            rotateControlEl.style.height = "32px";
            rotateControlEl.style.position = "absolute";
            rotateControlEl.style.top = "5px";
            rotateControlEl.style.left = "735px";
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
            location += "&cstudio-useragent=iphone";  
        }
        else {
            location += "?cstudio-useragent=iphone";    
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
                        link += "&cstudio-useragent=iphone";  
                    }
                    else {
                        link += "?cstudio-useragent=iphone";    
                    }
            
                    els[k].href = ""+link;
                }
            }
        }
        
        iframeEl.src = location;
	}
}

CStudioAuthoring.Module.moduleLoaded("medium-panel-iphone", CStudioAuthoring.MediumPanel.IPhoneVert);