CStudioForms.Controls.VideoPicker = CStudioForms.Controls.VideoPicker ||
    function(id, form, owner, properties, constraints, readonly)  {
        this.owner = owner;
        this.owner.registerField(this);
        this.errors = [];
        this.properties = properties;
        this.constraints = constraints;
        this.inputEl = null;
        this.required = false;
        this.value = "_not-set";
        this.form = form;
        this.id = id;
        this.datasources = null;
        this.upload_dialog = null;
        this.validExtensions = ["MOV", "mov", "MP4", "mp4", "wmv", "WMV"];
        this.readonly = readonly;

        return this;
    }

YAHOO.extend(CStudioForms.Controls.VideoPicker, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return CMgs.format(langBundle, "video");
    },

    _onChange: function(evt, obj) {
        obj.value = obj.inputEl.value;

        if(obj.required) {
            if(obj.inputEl.value == "") {
                obj.setError("required", "Field is Required");
                obj.renderValidation(true, false);
            }
            else {
                obj.clearError("required");
                obj.renderValidation(true, true);
            }
        }
        else {
            obj.renderValidation(false, true);
        }

        obj.owner.notifyValidation();
        obj.form.updateModel(obj.id, obj.getValue());
    },

    _onChangeVal: function(evt, obj) {
        obj.edited = true;
        this._onChange(evt,obj);
    },

    /**
     * perform count calculation on keypress
     * @param evt event
     * @param el element
     */
    count: function(evt, countEl, el) {
    },

    /**
     * create dialog
     */
    createDialog: function() {
        YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");

        var newdiv = YDom.get("cstudio-wcm-popup-div");
        if (newdiv == undefined) {
            newdiv = document.createElement("div");
            document.body.appendChild(newdiv);
        }

        var divIdName = "cstudio-wcm-popup-div";
        newdiv.setAttribute("id",divIdName);
        newdiv.className= "yui-pe-content";

        newdiv.innerHTML = '<embed src=\"' +
            CStudioAuthoringContext.previewAppBaseUri + this.inputEl.value + '\" width=\"500px\" height=\"500px\"></embed>' +
            '<input type="button" class="cstudio-button cstudio-form-control-asset-picker-zoom-cancel-button" id="zoomCancelButton" value="Close"/>';

        // Instantiate the Dialog
        upload_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div",
            { fixedcenter : true,
                visible : false,
                modal:true,
                close:true,
                constraintoviewport : true,
                underlay:"none"
            });

        // Render the Dialog
        upload_dialog.render();
        YAHOO.util.Event.addListener("zoomCancelButton", "click", this.uploadPopupCancel, this, true);
        this.upload_dialog = upload_dialog;
        upload_dialog.show();
    },

    /**
     * Show Alert
     */
    showAlert: function(message){
        var dialog = new YAHOO.widget.SimpleDialog("alertDialog",
            { width: "400px",fixedcenter: true, visible: false, draggable: false, close: false, modal: true,
                text: message, icon: YAHOO.widget.SimpleDialog.ICON_ALARM,
                constraintoviewport: true,
                buttons: [ { text:"OK", handler: function(){
                    this.destroy();

                }, isDefault:false } ]
            });
        dialog.setHeader("CStudio Warning");
        dialog.render(document.body);
        dialog.show();
    },

    /**
     * event fired when the ok is pressed
     */
    uploadPopupCancel: function(event) {
        this.upload_dialog.destroy();
    },

    addVideo: function() {
        var _self = this;
        var videoManagerNames = this.datasources;

        videoManagerNames = (!videoManagerNames) ? "" :
            (Array.isArray(videoManagerNames)) ? videoManagerNames.join(",") : videoManagerNames;
        var datasourceMap = this.form.datasourceMap,
            datasourceDef = this.form.definition.datasources;

        if(videoManagerNames != "" && videoManagerNames.indexOf(",") != -1){

            if(this.addContainerEl) {
                addContainerEl = this.addContainerEl;
                this.addContainerEl = null;
                this.containerEl.removeChild(addContainerEl);
            }
            //else {
            addContainerEl = document.createElement("div")
            this.containerEl.appendChild(addContainerEl);
            YAHOO.util.Dom.addClass(addContainerEl, 'cstudio-form-control-video-picker-add-container');
            this.addContainerEl = addContainerEl;



            addContainerEl.style.left = this.addEl.offsetLeft + "px";
            addContainerEl.style.top = this.addEl.offsetTop + 22 + "px";

            // The datasource title is only found in the definition.datasources. It'd make more sense to have all
            // the information in just one place.

            var addMenuOption = function (el) {
                // We want to avoid possible substring conflicts by using a reg exp (a simple indexOf
                // would fail if a datasource id string is a substring of another datasource id)
                var regexpr = new RegExp("(" + el.id + ")[\\s,]|(" + el.id + ")$"),
                    mapDatasource;

                //if (videoManagerNames.search(regexpr) > -1) {
                if (videoManagerNames.indexOf(el.id) != -1) {
                    mapDatasource = datasourceMap[el.id];

                    var itemEl = document.createElement("div");
                    YAHOO.util.Dom.addClass(itemEl, 'cstudio-form-control-video-picker-add-container-item');
                    itemEl.innerHTML = el.title;
                    addContainerEl.appendChild(itemEl);

                    YAHOO.util.Event.on(itemEl, 'click', function() {
                        _self.addContainerEl = null;
                        _self.containerEl.removeChild(addContainerEl);

                        _self._addVideo(mapDatasource);

                    }, itemEl);
                }
            }
            datasourceDef.forEach(addMenuOption);
        }
        else if(videoManagerNames != ""){
            videoManagerNames = videoManagerNames.replace("[\"","").replace("\"]","");
            this._addVideo(datasourceMap[videoManagerNames]);
        }
    },

    _addVideo: function(datasourceEl) {
        var datasource = datasourceEl;
        if(datasource) {
            if(datasource.insertVideoAction) {
                var callback = {
                    success: function(videoData) {
                        this.videoPicker.inputEl.value = videoData.relativeUrl;

                        var valid = false;
                        var message = '';

                        if (this.videoPicker.validExtensions.indexOf(videoData.fileExtension) != -1) {
                            valid = true;
                        } else {
                            message = "The uploaded file is not of type video";
                        }

                        if (!valid) {
                            this.videoPicker.showAlert(message);
                            //this.videoPicker.deleteVideo();
                        } else {
                            this.videoPicker.previewEl.src = videoData.previewUrl;
                            this.videoPicker.previewEl.setAttribute("controls", "true");
                            this.videoPicker.urlEl.innerHTML = videoData.relativeUrl;
                            this.videoPicker.downloadEl.href = videoData.previewUrl;

                            this.videoPicker.addEl.value = "Replace";

                            this.videoPicker.noPreviewEl.style.display = "none";
                            this.videoPicker.previewEl.style.display = "inline";

                            this.videoPicker.downloadEl.style.display = "inline-block";
                            this.videoPicker.zoomEl.style.display = "inline-block";

                            this.videoPicker.delEl.disabled = false;
                            YAHOO.util.Dom.removeClass(this.videoPicker.delEl, 'cstudio-button-disabled');

                            this.videoPicker._onChangeVal(null, this.videoPicker);
                        }
                    },
                    failure: function(message) {
                        this.imagePicker.showAlert(message);
                    }
                };
                callback.videoPicker = this;
                datasource.insertVideoAction(callback);
            }
        }
    },

    deleteVideo: function() {
        if(this.inputEl.value != "") {
            this.inputEl.value = '';
            this.urlEl.innerHTML = '';
            this.previewEl.src = '';
            this.previewEl.style.display = "none";
            this.noPreviewEl.style.display = "inline";
            this.addEl.value = "Add";

            this.downloadEl.style.display = "none";
            this.zoomEl.style.display = "none";

            this.delEl.disabled = true;
            YAHOO.util.Dom.addClass(this.delEl, 'cstudio-button-disabled');

            this._onChangeVal(null, this);
        }
    },

    render: function(config, containerEl) {
        containerEl.id = this.id;

        var divPrefix = config.id + "-";
        var datasource = null;

        var titleEl = document.createElement("span");

        YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
        titleEl.innerHTML = config.title;

        var controlWidgetContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-video-picker-container');

        var validEl = document.createElement("span");
        YAHOO.util.Dom.addClass(validEl, 'validation-hint');
        YAHOO.util.Dom.addClass(validEl, 'cstudio-form-control-validation');
        controlWidgetContainerEl.appendChild(validEl);

        var inputEl = document.createElement("input");
        this.inputEl = inputEl;
        inputEl.style.display = "none";
        YAHOO.util.Dom.addClass(inputEl, 'datum');
        controlWidgetContainerEl.appendChild(inputEl);

        var urlEl = document.createElement("div");
        this.urlEl = urlEl;
        urlEl.innerHTML = this.inputEl.value;
        controlWidgetContainerEl.appendChild(urlEl);

        var videoEl = document.createElement("div");
        this.videoEl = videoEl;
        videoEl.id = divPrefix + "cstudio-form-video-picker";

        YAHOO.util.Dom.addClass(videoEl, 'cstudio-form-control-asset-picker-preview-block');
        controlWidgetContainerEl.appendChild(videoEl);

        var noPreviewEl = document.createElement("span");
        this.noPreviewEl = noPreviewEl;
        noPreviewEl.innerHTML = "No Video Available";

        YAHOO.util.Dom.addClass(noPreviewEl, 'cstudio-form-control-asset-picker-no-preview-content');
        noPreviewEl.style.paddingLeft = "5px";
        videoEl.appendChild(noPreviewEl);

        var previewEl = document.createElement("video");
        this.previewEl = previewEl;
        //previewEl.setAttribute("autoplay", "0");
        //previewEl.setAttribute("autostart", "0");

        YAHOO.util.Dom.addClass(previewEl, 'cstudio-form-control-asset-picker-preview-content');

        videoEl.appendChild(previewEl);

        var zoomEl = document.createElement("input");
        this.zoomEl = zoomEl;
        zoomEl.type = "button";

        YAHOO.util.Dom.addClass(zoomEl, 'cstudio-form-control-asset-picker-zoom-button video-zoom');

        if (this.inputEl.value == null || this.inputEl.value == "") {
            zoomEl.style.display = "none";
        } else {
            zoomEl.style.display = "inline-block";
        }

        videoEl.appendChild(zoomEl);

        var downloadEl = document.createElement("a");
        this.downloadEl = downloadEl;
        downloadEl.href = inputEl.value;
        downloadEl.target = "_new";
        var downloadVideoEl = document.createElement("img");
        downloadVideoEl.src = "/studio/static-assets/themes/cstudioTheme/images/download.png";
        downloadEl.appendChild(downloadVideoEl);

        YAHOO.util.Dom.addClass(downloadEl, 'cstudio-form-control-asset-picker-download-button video-zoom');

        if (this.inputEl.value == null || this.inputEl.value == "") {
            downloadEl.style.display = "none";
        } else {
            downloadEl.style.display = "inline-block";
        }

        videoEl.appendChild(downloadEl);

        var addEl = document.createElement("input");
        this.addEl = addEl;
        addEl.type = "button";
        addEl.style.position = "relative";
        if (this.inputEl.value == null || this.inputEl.value == "") {
            addEl.value = "Add";
        } else {
            addEl.value = "Replace";
        }

        YAHOO.util.Dom.addClass(addEl, 'cstudio-button');
        controlWidgetContainerEl.appendChild(addEl);

        var delEl = document.createElement("input");
        this.delEl = delEl;
        delEl.type = "button";
        delEl.value = "Delete";
        delEl.style.position = "relative";
        delEl.disabled = true;
        YAHOO.util.Dom.addClass(delEl, 'cstudio-button');
        YAHOO.util.Dom.addClass(delEl, 'cstudio-button-disabled');

        controlWidgetContainerEl.appendChild(delEl);

        for(var i=0; i<config.properties.length; i++) {
            var prop = config.properties[i];

            if(prop.name == "videoManager") {
                if(prop.value && prop.value != "") {
                    //var datasourceName = prop.value;
                    //datasource = this.form.datasourceMap[datasourceName];
                    //this.datasource = datasource;

                    this.datasources = prop.value;
                }else{
                    addEl.disabled = true;
                    YAHOO.util.Dom.addClass(addEl, 'cstudio-button-disabled');
                }
            }

            if (prop.name == "height") {
                if (prop.value && prop.value != "") {
                    this.height = prop.value;
                }
            }

            if (prop.name == "width") {
                if (prop.value && prop.value != "") {
                    this.width = prop.value;
                }
            }

            if(prop.name == "readonly" && prop.value == "true"){
                this.readonly = true;
            }
        }


        var helpContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(helpContainerEl, 'cstudio-form-field-help-container');
        controlWidgetContainerEl.appendChild(helpContainerEl);

        this.renderHelp(config, helpContainerEl);

        var descriptionEl = document.createElement("span");
        YAHOO.util.Dom.addClass(descriptionEl, 'description');
        YAHOO.util.Dom.addClass(descriptionEl, 'cstudio-form-field-description');
        descriptionEl.innerHTML = config.description;
        descriptionEl.style.marginLeft = "341px";
        descriptionEl.style.position = "relative";

        containerEl.appendChild(titleEl);
        containerEl.appendChild(controlWidgetContainerEl);
        containerEl.appendChild(descriptionEl);

        if(this.readonly == true){
            addEl.disabled = true;
            delEl.disabled = true;
            YAHOO.util.Dom.addClass(addEl, 'cstudio-button-disabled');
            YAHOO.util.Dom.addClass(delEl, 'cstudio-button-disabled');
        }

        YAHOO.util.Event.addListener(addEl, "click", this.addVideo, this, true);
        YAHOO.util.Event.addListener(delEl, "click", this.deleteVideo, this, true);
        YAHOO.util.Event.addListener(zoomEl, "click", this.createDialog, this, true);
    },

    getValue: function() {
        return this.value;
    },

    setValue: function(value) {
        this.value = value;
        this.inputEl.value = value;

        if (value == null || value == '') {
            this.noPreviewEl.style.display = "inline";
        } else {
            this.previewEl.src = CStudioAuthoringContext.previewAppBaseUri + value;
            this.previewEl.style.display = "block";
            this.previewEl.setAttribute("controls", "true");
            this.noPreviewEl.style.display = "none";
            this.urlEl.innerHTML = value;

            this.zoomEl.style.display = "inline-block";
            this.downloadEl.style.display = "inline-block";
            this.addEl.value = "Replace";
            this.delEl.disabled = false;
            YAHOO.util.Dom.removeClass(this.delEl, 'cstudio-button-disabled');
        }

        this._onChange(null, this);
        this.edited = false;
    },

    getName: function() {
        return "video-picker";
    },

    getSupportedProperties: function() {
        return [
            { label: CMgs.format(langBundle, "datasource"), name: "videoManager", type: "datasource:video" },
            { label: CMgs.format(langBundle, "readonly"), name: "readonly", type: "boolean" }
        ];
    },

    getSupportedConstraints: function() {
        return [
            { label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" }
        ];
    }
});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-video-picker", CStudioForms.Controls.VideoPicker);