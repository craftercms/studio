var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;


CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.UploadFlickrDialog = CStudioAuthoring.Dialogs.UploadFlickrDialog || {

	/**
	 * initialize module
	 */
	initialize: function(config) {
	},

	/**
	 * show dialog
	 */
	showDialog: function(site, path, callback, isUploadOverwrite) {	
		this._self = this;

		this.dialog = this.createDialog(path, site, isUploadOverwrite);

		this.site = site;
		this.path = path;
		this.asPopup = true;			
		this.callback = callback;
		this.isUploadOverwrite = isUploadOverwrite;
		this.dialog.show();
		document.getElementById("cstudio-wcm-popup-div_h").style.display = "none";
		
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
	createDialog: function(path, site, isUploadOverwrite) {
		var _self = this;
		YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");
		
		if (isUploadOverwrite == "overwrite") {
			path = path.substring(0, path.lastIndexOf("/"));	
		}

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
                           '<div class="contentTypePopupHeader">Insert Flickr Image</div> ' +
                           '<div><div  id="gutter1"><label for="flikr_search">Tag:</label><input style="height: 20px; margin-left: 10px; position: absolute; width: 200px;" type="text" value="" id="flickr_search"><br/><br/><div id="flickr_results"></div></div>' + 

                           '</div> ' +
                           '</div>';
		
		document.getElementById("upload-popup-inner").style.width = "550px";
		document.getElementById("upload-popup-inner").style.height = "265px";

		 // Instantiate the Dialog
		upload_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div", 
								{ width : "560px",
								  height : "275px",	
								  fixedcenter : true,
								  visible : false,
								  modal:true,
								  close:false,
								  constraintoviewport : true,
								  underlay:"none"							  							
								});	
								
		// Render the Dialog
		upload_dialog.render();

		YAHOO.util.Event.onAvailable('flickr_search', function() {

		    YAHOO.util.Event.on('flickr_results', 'mousedown', function(ev) {
		        YAHOO.util.Event.stopEvent(ev);
		        var tar = YAHOO.util.Event.getTarget(ev);
		        if (tar.tagName.toLowerCase() == 'img') {
		            if (tar.getAttribute('src', 2)) {
		            	var url = tar.getAttribute('src', 2);
		            	url = url.replace("_s", "_m");
		               _self.callback.success({
		               		relativeUrl: url,
		               		fileExtension: url.substring(url.lastIndexOf(".")+1)
		               });
		               
			        	CStudioAuthoring.Dialogs.UploadFlickrDialog.closeDialog();
		            }
		        }
		    }, this, true);
		    
		    var oACDS = new YAHOO.util.XHRDataSource("/studio/form-controller/flickr/service-search.jsp");
		    oACDS.responseSchema = {
		        resultNode: "photo",
		        fields: ["url"]
		    };
		    oACDS.responseType = YAHOO.widget.DS_XHR.TYPE_XML;
		    oACDS.maxCacheEntries = 100;
		
		    // Instantiate AutoComplete
		    oAutoComp = new YAHOO.widget.AutoComplete('flickr_search','flickr_results', oACDS);
		    oAutoComp.autoHighlight = false;
		    oAutoComp.alwaysShowContainer = true; 
		    oAutoComp.suppressInputUpdate = true;
		    oAutoComp.generateRequest = function(sQuery) {
		        return "?query="+sQuery;
		    };
		    oAutoComp.formatResult = function(oResultItem, sQuery) {
		        // This was defined by the schema array of the data source
		         var sMarkup = '<img src="' + oResultItem[1].url + '" title="Click to add this image to the editor">';
		        return (sMarkup);
		    };
		});

		var eventParams = {
			self: this
		};
		
		if (isUploadOverwrite == "upload") {
			YAHOO.util.Event.addListener("uploadButton", "click", this.uploadPopupSubmit, eventParams);
		} else {
			YAHOO.util.Event.addListener("uploadButton", "click", this.uploadPopupSubmit, eventParams);
//			YAHOO.util.Event.addListener("uploadButton", "click", this.overwritePopupSubmit, eventParams);
		}
		YAHOO.util.Event.addListener("uploadCancelButton", "click", this.uploadPopupCancel);

		
		return upload_dialog;
	},
		
	/**
	 * event fired when the ok is pressed - checks if the file already exists and has edit permission or not 
	 * by using the getPermissions Service call
	 */
	uploadPopupSubmit: function(event, args) {
		var path = args.self.path;
		
		var serviceCallback = {
			exists: function(jsonResponse) {
				//Get user permissions to get read write operations
				
				var checkPermissionsCb = {
		        	success: function(results) {
						var isWrite = CStudioAuthoring.Service.isWrite(results.permissions);
						if (isWrite == true) {
//							CStudioAuthoring.Dialogs.UploadDialog.overwritePopupSubmit(event, args);
							CStudioAuthoring.Dialogs.UploadDialog.uploadFile(args);
						} else {
							document.getElementById("indicator").innerHTML = "File already exists: User has no overwrite permission";
							YAHOO.util.Dom.setStyle('indicator', 'color', 'red');
						}
					},
					failure: function() { }
	        	};
				
				CStudioAuthoring.Clipboard.getPermissions(path, checkPermissionsCb);
			},
			failure: function(response) {		
				CStudioAuthoring.Dialogs.UploadDialog.uploadFile(args);
			}
		};

		YAHOO.util.Dom.setStyle('indicator', 'visibility', 'visible');
		CStudioAuthoring.Service.contentExists(args.self.path, serviceCallback);
	},
	
   /**
	 * upload file when upload pressed
	 */
	uploadFile: function(args) {
		var serviceUri = "";//CStudioAuthoring.Service.createServiceUri(args.self.serviceUri);

		var uploadHandler = {
			upload: function(o) {
				//console.log(o.responseText);
				YAHOO.util.Dom.setStyle('indicator', 'visibility', 'hidden');
				var r = eval('(' + o.responseText + ')');
				if(r.hasError){
					var errorString = '';
					for(var i=0; i < r.errors.length; i++){
						errorString += r.errors[i];
					}
					alert(errorString);
				}else{
					CStudioAuthoring.Dialogs.UploadDialog.closeDialog();				
					args.self.callback.success(r);
				}
			}
		};
		YAHOO.util.Dom.setStyle('indicator', 'visibility', 'visible');
		//the second argument of setForm is crucial,
		//which tells Connection Manager this is an file upload form
		YAHOO.util.Connect.setForm('asset_upload_form', true);
		YAHOO.util.Connect.asyncRequest('POST', serviceUri, uploadHandler);
	},
	
	/**
	 *
	 */
	 overwritePopupSubmit: function(event, args) {
	 	
        var callback = {
            success: function(response) {
				var serviceUri = ""; //CStudioAuthoring.Service.createServiceUri(args.self.serviceUri);
				var uploadHandler = {
					upload: function(o) {
						//console.log(o.responseText);
						YAHOO.util.Dom.setStyle('indicator', 'visibility', 'hidden');
						var r = eval('(' + o.responseText + ')');
						if(r.hasError){
							var errorString = '';
							for(var i=0; i < r.errors.length; i++){
								errorString += r.errors[i];
							}
							alert(errorString);
						}else{
							CStudioAuthoring.Dialogs.UploadDialog.closeDialog();				
						    args.self.callback.success(r);
						}
					}
				};
				YAHOO.util.Dom.setStyle('indicator', 'visibility', 'visible');
				//the second argument of setForm is crucial,
				//which tells Connection Manager this is an file upload form
				YAHOO.util.Connect.setForm('asset_upload_form', true);
				YAHOO.util.Connect.asyncRequest('POST', serviceUri, uploadHandler);				
            },

            failure: function() {
            }
        };

        CStudioAuthoring.Service.deleteContentForPathService(args.self.site, args.self.path, callback);
	 	
	 },

	/**
	 * event fired when the ok is pressed
	 */
	uploadPopupCancel: function(event) {
		CStudioAuthoring.Dialogs.UploadFlickrDialog.closeDialog();
	}


};

CStudioAuthoring.Module.moduleLoaded("flickr-dialog", CStudioAuthoring.Dialogs.UploadFlickrDialog);

