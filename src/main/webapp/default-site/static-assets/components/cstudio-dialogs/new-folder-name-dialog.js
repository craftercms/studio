var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.NewFolderNameDialog = CStudioAuthoring.Dialogs.NewFolderNameDialog || {

	/**
	 * initialize module
	 */
	initialize: function(config) {
	},

	/**
	 * show dialog
	 */
	showDialog: function(site, path, serviceUri, callingWindow, callback) {	
		this._self = this;

		this.dialog = this.createDialog(path, site, serviceUri);

		this.path = path;
		this.site = site;
		this.asPopup = true;			
		this.serviceUri = serviceUri;
		this.callingWindow = callingWindow;
		this.callback = callback;
		this.dialog.show();
	},
	
	/**
	 * hide dialog
	 */
    closeDialog:function() {
        this.dialog.destroy();
    },
	 	
	/**
	 * create dialog
	 */
	createDialog: function(path, site, serviceUri) {
		YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");
		
		var newdiv = YDom.get("cstudio-wcm-popup-div");
		if (newdiv == undefined) {
			newdiv = document.createElement("div");
			document.body.appendChild(newdiv);
		}
		var divIdName = "cstudio-wcm-popup-div";
		newdiv.setAttribute("id",divIdName);
		newdiv.className= "yui-pe-content";
        newdiv.innerHTML = '<div class="contentTypePopupInner" id="upload-popup-inner">' +
                           '<div class="contentTypePopupContent" id="contentTypePopupContent"> ' +
                           '<div class="contentTypePopupHeader">Create a New Folder</div> ' +
                           '<div style="margin-bottom:10px;font-style:italic;">Please enter a folder name</div> ' +
						   '<div>' +
						   '<div><table><tr><td><div style="margin-right:10px;">Folder Name:</div></td><td><input type="text" name="folderName" id="folderNameId" style="width:210px" /></td></tr></table></div>' +
						   '<div class="contentTypePopupBtn"> ' +
						        '<input type="button" class="cstudio-xform-button ok" id="createButton" value="Create" />' +
                                '<input type="button" class="cstudio-xform-button" id="createCancelButton" value="Cancel" /></div>' +
						   '</div>' +
						   '<div><div  style="visibility:hidden; margin:10px 0;" id="indicator">Creating a folder...</div>' +
                           '</div> ' +
                           '</div>';

		document.getElementById("upload-popup-inner").style.width = "350px";
		document.getElementById("upload-popup-inner").style.height = "auto";
		
		 // Instantiate the Dialog
		create_folder_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div", {
            width: "360px",
            height: "auto",
            fixedcenter: true,
            visible: false,
            modal: true,
            close: false,
            constraintoviewport: true,
            underlay: "none"
        });
								
		// Render the Dialog
		create_folder_dialog.render();
		
		var eventParams = {
			self: this
		};
		
		var inputEl = document.getElementById("folderNameId"),
            me = this;

		YAHOO.util.Event.addListener("createButton", "click", this.createPopupSubmit, eventParams);
		YAHOO.util.Event.addListener("createCancelButton", "click", this.createPopupCancel);
		YAHOO.util.Event.on(inputEl, 'keyup', function (e) {
            if (e.which !== 13) {
                me.processKey(e, inputEl);
            } else {
                me.createPopupSubmit(e, eventParams);
            }
        }, inputEl);

		return create_folder_dialog;
	},
		
	/**
	 * event fired when the ok is pressed
	 */
	createPopupSubmit: function(event, args) {
		var contentType = "folder";
		var newFolderName = document.getElementById("folderNameId").value;
		var serviceUri = CStudioAuthoring.Service.createServiceUri(args.self.serviceUri
				+ "?site=" + args.self.site + "&path=" + args.self.path + "&name=" + newFolderName);
		
        var serviceCallback = {
            success: function(oResponse) {
				//reload the page for now, need to improve to reload the tree dynamically
				CStudioAuthoring.Dialogs.NewFolderNameDialog.closeDialog();
				args.self.callback.success(); //.callingWindow.location.reload(true);
            },

            failure: function (response) {
            	var responseJson = eval("("+response.responseText+")");
            	var message = responseJson.message;
                document.getElementById("indicator").innerHTML = message;
				YAHOO.util.Dom.setStyle('indicator', 'color', 'red');
            },
            
            callback: args.self.callback
        };
		YAHOO.util.Dom.setStyle('indicator', 'visibility', 'visible');
		YConnect.asyncRequest('POST', serviceUri, serviceCallback);
	},

	/**
	 * event fired when the ok is pressed
	 */
	createPopupCancel: function() {
		CStudioAuthoring.Dialogs.NewFolderNameDialog.closeDialog();
	},

	/**
	 * don't allow characters which are invalid for file names and check length
	 */
	processKey: function(evt, el) {
        var invalid = new RegExp("[!@#$%^&*\\(\\)\\+=\\[\\]\\\\\\\'`;,\\.\\/\\{\\}|\":<>\\?~_ ]", 'g');
	    var cursorPosition = el.selectionStart;
	    //change url to lower case
	    if (el.value != "" && el.value != el.value.toLowerCase()) {
	        el.value = el.value.toLowerCase();
	        if (cursorPosition && typeof cursorPosition == "number") {
	            el.selectionStart = cursorPosition;
	            el.selectionEnd = cursorPosition;
	        }
	    }
	    var data = el.value;

	    if (invalid.exec(data) != null) {
	        el.value = data.replace(invalid, "");
	        if (cursorPosition && typeof cursorPosition == "number") {
	            el.selectionStart = cursorPosition-1;
	            el.selectionEnd = cursorPosition-1;
	        }
	        // commented out since this is causing a js error: Event.stopEvent is not a function
	        //Event.stopEvent(evt);
	    }

	    if (el.maxLength != -1 && data.length > el.maxLength) {
	    	data = data.substr(0,el.maxLength);
	    	el.value = data;
	    }
	}	


};

CStudioAuthoring.Module.moduleLoaded("new-folder-name-dialog", CStudioAuthoring.Dialogs.NewFolderNameDialog);