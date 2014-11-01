CStudioForms.Controls.ImagePicker = CStudioForms.Controls.ImagePicker ||
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
        this.validExtensions = ["jpg", "jpeg", "gif", "png", "tiff", "tif", "bmp", "JPG", "JPEG", "GIF", "PNG", "TIFF", "TIF", "BMP"];
        this.readonly = readonly;
        this.originalWidth = null;
        this.originalHeight = null;
        this.previewBoxHeight = 100;
        this.previewBoxWidth = 300;

        return this;
    }

YAHOO.extend(CStudioForms.Controls.ImagePicker, CStudioForms.CStudioFormField, {


    getLabel: function() {
        return "Image";
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



        var width = (this.originalWidth)?this.originalWidth:500;
        var height = (this.originalHeight)?this.originalHeight:500;
        newdiv.innerHTML = '<img width=\"' + width + 'px\" height=\"' + height + 'px\" src=\"' +
            CStudioAuthoringContext.previewAppBaseUri + this.inputEl.value + '\"></img>' +
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
     * event fired when the ok is pressed
     */
    uploadPopupCancel: function(event) {
        this.upload_dialog.destroy();
    },

    showAlert: function(message){
        var dialog = new YAHOO.widget.SimpleDialog("alertDialog",
            { width: "400px",fixedcenter: true, visible: false, draggable: false, close: false, modal: true,
                text: message, icon: YAHOO.widget.SimpleDialog.ICON_ALARM,
                constraintoviewport: true,
                buttons: [ { text:"OK", handler: function(){
                    this.destroy();

                }, isDefault:true } ]
            });
        dialog.setHeader("CStudio Warning");
        dialog.render(document.body);
        dialog.show();
    },

    addImage: function(){
        var _self = this;
        var imageManagerNames = this.datasources;

        imageManagerNames = (!imageManagerNames) ? "" :
            (Array.isArray(imageManagerNames)) ? imageManagerNames.join(",") : imageManagerNames;
        var datasourceMap = this.form.datasourceMap,
            datasourceDef = this.form.definition.datasources;

        if(imageManagerNames != "" && imageManagerNames.indexOf(",") != -1){

            if(this.addContainerEl) {
                addContainerEl = this.addContainerEl;
                this.addContainerEl = null;
                this.ctrlOptionsEl.removeChild(addContainerEl);
            }
            else {
                addContainerEl = document.createElement("div")
                this.ctrlOptionsEl.appendChild(addContainerEl);
                YAHOO.util.Dom.addClass(addContainerEl, 'cstudio-form-control-image-picker-add-container');
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

                    if (imageManagerNames.search(regexpr) > -1) {
                        mapDatasource = datasourceMap[el.id];

                        var itemEl = document.createElement("div");
                        YAHOO.util.Dom.addClass(itemEl, 'cstudio-form-control-image-picker-add-container-item');
                        itemEl.innerHTML = el.title;
                        addContainerEl.appendChild(itemEl);

                        YAHOO.util.Event.on(itemEl, 'click', function() {
                            _self.addContainerEl = null;
                            _self.ctrlOptionsEl.removeChild(addContainerEl);

                            _self._addImage(mapDatasource);

                        }, itemEl);
                    }
                }
                datasourceDef.forEach(addMenuOption);
            }
        }
        else if(imageManagerNames != ""){
            this._addImage(datasourceMap[imageManagerNames]);
        }
    },

    _addImage: function(datasourceEl) {
        var datasource = datasourceEl;
        if(datasource) {
            if(datasource.insertImageAction) {
                var callback = {
                    success: function(imageData) {
                        var valid = false;
                        var message = '';

                        if (this.imagePicker.validExtensions.indexOf(imageData.fileExtension) != -1) {
                            valid = true;
                        } else {
                            message = "The uploaded file is not of type image";
                        }

                        if (!valid) {
                            this.imagePicker.showAlert(message);
                            //this.imagePicker.deleteImage();
                        } else {
                            var image = new Image();
                            var imagePicker = this.imagePicker;

                            function imageLoaded(){
                                imagePicker.originalWidth = this.width;
                                imagePicker.originalHeight = this.height;

                                valid = imagePicker.isImageValid();
                                if (!valid) {
                                    message = "The uploaded file does not fulfill the width & height constraints";
                                    //imagePicker.deleteImage();
                                    imagePicker.showAlert(message);
                                }else{
                                    imagePicker.inputEl.value = imageData.relativeUrl;


                                    imagePicker.previewEl.src = imageData.previewUrl;
                                    imagePicker.urlEl.innerHTML = imageData.relativeUrl;
                                    imagePicker.downloadEl.href = imageData.previewUrl;

                                    imagePicker.addEl.value = "Replace";

                                    imagePicker.noPreviewEl.style.display = "none";
                                    imagePicker.previewEl.style.display = "inline";
                                    YAHOO.util.Dom.addClass(imagePicker.previewEl, 'cstudio-form-control-asset-picker-preview-content');


                                    imagePicker.adjustImage();

                                    imagePicker._onChange(null, imagePicker);
                                }
                            };
                            image.addEventListener('load', imageLoaded, false);
                            image.addEventListener('error', function () {
                                message = "Unable to load the selected image. Please try again or select another image";
                                imagePicker.showAlert(message);
                            });
                            image.src = imageData.previewUrl;


                        }
                    },
                    failure: function(message) {
                        this.imagePicker.showAlert(message);
                    }
                };
                callback.imagePicker = this;
                datasource.insertImageAction(callback);
            }
        }
    },

    deleteImage: function() {
        if(this.addContainerEl) {
            addContainerEl = this.addContainerEl;
            this.addContainerEl = null;
            this.ctrlOptionsEl.removeChild(addContainerEl);
        }

        if(this.inputEl.value != "") {
            this.inputEl.value = '';
            this.urlEl.innerHTML = '';
            this.previewEl.style.display = "none";
            this.previewEl.src = "";
            this.noPreviewEl.style.display = "inline";
            this.addEl.value = "Add";

            this.downloadEl.style.display = "none";
            this.zoomEl.style.display = "none";

            this.originalWidth = null;
            this.originalHeight = null;

            YAHOO.util.Dom.addClass(this.previewEl, 'cstudio-form-control-asset-picker-preview-content');
            YAHOO.util.Dom.setStyle(this.imageEl,  'width', this.previewBoxWidth + "px");
            YAHOO.util.Dom.setStyle(this.imageEl,  'height', this.previewBoxHeight + "px");

            this._onChange(null, this);
        }
    },

    render: function(config, containerEl) {
        containerEl.id = this.id;

        var divPrefix = config.id + "-";
        var datasource = null;

        this.containerEl = containerEl;

        // we need to make the general layout of a control inherit from common
        // you should be able to override it -- but most of the time it wil be the same
        var titleEl = document.createElement("span");
        YAHOO.util.Dom.addClass(titleEl, 'label');
        YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
        titleEl.innerHTML = config.title;

        var controlWidgetContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-image-picker-container');

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

        var bodyEl = document.createElement("div");
        YAHOO.util.Dom.addClass(bodyEl, 'cstudio-form-control-asset-picker-body');
        controlWidgetContainerEl.appendChild(bodyEl);


        var imageEl = document.createElement("div");
        this.imageEl = imageEl;
        imageEl.id = divPrefix + "cstudio-form-image-picker";
        YAHOO.util.Dom.addClass(imageEl, 'cstudio-form-control-asset-picker-preview-block');
        bodyEl.appendChild(imageEl);

        var noPreviewEl = document.createElement("span");
        this.noPreviewEl = noPreviewEl;
        noPreviewEl.innerHTML = "No Image Available";
        YAHOO.util.Dom.addClass(noPreviewEl, 'cstudio-form-control-asset-picker-no-preview-content');
        imageEl.appendChild(noPreviewEl);

        var previewEl = document.createElement("img");
        this.previewEl = previewEl;
        YAHOO.util.Dom.addClass(previewEl, 'cstudio-form-control-asset-picker-preview-content');
        previewEl.style.display = "none";
        imageEl.appendChild(previewEl);

        var zoomEl = document.createElement("input");
        this.zoomEl = zoomEl;
        zoomEl.type = "button";
        YAHOO.util.Dom.addClass(zoomEl, 'cstudio-form-control-asset-picker-zoom-button');
        zoomEl.style.display = "none";
        imageEl.appendChild(zoomEl);

        var downloadEl = document.createElement("a");
        this.downloadEl = downloadEl;
        downloadEl.href = inputEl.value;
        downloadEl.target = "_new";
        var downloadImageEl = document.createElement("img");
        downloadImageEl.src = Alfresco.constants.URL_CONTEXT + "themes/cstudioTheme/images/download.png";
        downloadEl.appendChild(downloadImageEl);
        YAHOO.util.Dom.addClass(downloadEl, 'cstudio-form-control-asset-picker-download-button');
        downloadEl.style.display = "none";
        imageEl.appendChild(downloadEl);

        var ctrlOptionsEl = document.createElement("div")
        YAHOO.util.Dom.addClass(ctrlOptionsEl, 'cstudio-form-control-image-picker-options');
        bodyEl.appendChild(ctrlOptionsEl);

        this.ctrlOptionsEl = ctrlOptionsEl;

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
        ctrlOptionsEl.appendChild(addEl);

        var delEl = document.createElement("input");
        this.delEl = delEl;
        delEl.type = "button";
        delEl.value = "Delete";
        delEl.style.position = "relative";
        YAHOO.util.Dom.addClass(delEl, 'cstudio-button');

        ctrlOptionsEl.appendChild(delEl);

        for(var i=0; i<config.properties.length; i++) {
            var prop = config.properties[i];

            if(prop.name == "imageManager") {
                if(prop.value && prop.value != "") {
                    this.datasources = prop.value;
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

            if (prop.name == "thumbnailHeight") {
                if (prop.value && prop.value != "") {
                    this.previewBoxHeight = prop.value;
                }
            }

            if (prop.name == "thumbnailWidth") {
                if (prop.value && prop.value != "") {
                    this.previewBoxWidth = prop.value;
                }
            }

            if(prop.name == "readonly" && prop.value == "true"){
                this.readonly = true;
            }
        }

        YAHOO.util.Dom.setStyle(this.imageEl, 'height', this.previewBoxHeight + "px");
        YAHOO.util.Dom.setStyle(this.imageEl, 'width',  this.previewBoxWidth + "px");

        var helpContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(helpContainerEl, 'cstudio-form-field-help-container');
        ctrlOptionsEl.appendChild(helpContainerEl);

        this.renderHelp(config, helpContainerEl);

        this.renderImageConstraints(bodyEl);

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

        YAHOO.util.Event.addListener(addEl, "click", this.addImage, this, true);
        YAHOO.util.Event.addListener(delEl, "click", this.deleteImage, this, true);
        YAHOO.util.Event.addListener(zoomEl, "click", this.createDialog, this, true);

        YAHOO.util.Event.addListener(imageEl,"mouseover", this.showButtons,this,true);
        YAHOO.util.Event.addListener(imageEl,"mouseout", this.hideButtons,this,true);
    },

    showButtons: function(evt){
        if(this.value != ""){
            if(this.originalWidth > this.previewBoxWidth || this.originalHeight > this.previewBoxHeight){
                this.zoomEl.style.display = "inline-block";
                this.downloadEl.style.marginLeft =  "0px";
            }else{
                this.downloadEl.style.marginLeft =  "-20px";
            }
            this.downloadEl.style.display = "inline-block";
        }
    },

    hideButtons: function(evt){
        this.zoomEl.style.display = "none";
        this.downloadEl.style.display = "none";
    },

    getValue: function() {
        return this.value;
    },

    renderImageConstraints: function(containerEl) {
        var checkFn = function(label, value){
            var message =  "";

            if(value){

                var obj =  (typeof value == "string") ? eval("(" + value + ")") : value;

                if(typeof obj == 'number'){
                    message = label + ": equal to " + obj + "px";
                }else{
                    if(obj.exact != ""){
                        message = label + ": equal to " + obj.exact + "px";
                    }else{
                        if( obj.min != "" && obj.max != ""){
                            message = label + ": between " + obj.min + "px and " + obj.max + "px";
                        }else if ( obj.min != "" ){
                            message = label + ": equal or greater than " + obj.min + "px";
                        }else if ( obj.max != ""){
                            message = label + ": equal or less than " + obj.max + "px";
                        }
                    }
                }
            }

            return message;
        }

        var widthConstraints = checkFn("Width", this.width);
        var heightConstraints = checkFn("Height", this.height);

        if(widthConstraints != "" || heightConstraints != ""){
            var requirementsEl = document.createElement("div");
            requirementsEl.innerHTML = '<div class="title">Image Requirements</div>' +
                '<div class="width-constraint">' + widthConstraints + '</div>' +
                '<div class="height-constraint">' + heightConstraints + '</div>';
            YAHOO.util.Dom.addClass(requirementsEl, 'cstudio-form-field-image-picker-constraints');
            containerEl.appendChild(requirementsEl);
        }
    },

    isImageValid: function() {
        var result =  true;

        var checkFn = function(value, srcValue){
            var internalResult =  true;

            if(value){
                internalResult = false;

                var obj =  (typeof value == "string") ? eval("(" + value + ")") : value;

                if(typeof obj == 'number' && obj == srcValue){
                    internalResult = true;
                }else{
                    if(obj.exact != "") {
                        if( obj.exact == srcValue ){
                            internalResult = true;
                        }
                    }else if( ( ( obj.min != "" && obj.min <= srcValue) || obj.min == "" ) &&
                        ( (obj.max != "" && obj.max >= srcValue) || obj.max == "" ) ){
                        internalResult = true;
                    }
                }
            }

            return internalResult;
        }

        result = checkFn(this.width, this.originalWidth) && checkFn(this.height, this.originalHeight);

        return result;
    },

    adjustImage: function(){
        var wImg = this.originalWidth || 0;
        var hImg = this.originalHeight || 0;
        var wThb = parseInt(this.previewBoxWidth,10);
        var hThb = parseInt(this.previewBoxHeight,10);
        var adjustedWidth = 0;
        var adjustedHeight = 0;

        YAHOO.util.Dom.setStyle(this.previewEl, 'height', "100%");
        YAHOO.util.Dom.setStyle(this.previewEl, 'width', "100%");

        if(wImg < wThb && hImg < hThb){
            YAHOO.util.Dom.removeClass(this.previewEl, 'cstudio-form-control-asset-picker-preview-content');
            YAHOO.util.Dom.setStyle(this.imageEl,  'height', hImg + "px");
            YAHOO.util.Dom.setStyle(this.imageEl,  'width', wImg + "px");
        }else{
            if(wImg && hImg){
                var conversionFactor = (wImg / wThb > hImg / hThb)? wImg / wThb : hImg / hThb;
                    adjustedHeight = Math.floor(hImg / conversionFactor);
                    adjustedWidth = Math.floor(wImg / conversionFactor);


                YAHOO.util.Dom.setStyle(this.imageEl, 'height', adjustedHeight + "px");
                YAHOO.util.Dom.setStyle(this.imageEl, 'width',  adjustedWidth + "px");
            }else{
                YAHOO.util.Dom.setStyle(this.imageEl, 'height', hThb + "px");
                YAHOO.util.Dom.setStyle(this.imageEl, 'width',  wThb+ "px");
            }
        }
    },

    setValue: function(value) {
        var _self = this;
        this.value = value;
        this.inputEl.value = value;

        if (value == null || value == '') {
            this.noPreviewEl.style.display = "inline";
        } else {
            this.previewEl.src = CStudioAuthoringContext.previewAppBaseUri + value;
            this.previewEl.style.display = "inline";
            this.noPreviewEl.style.display = "none";
            this.urlEl.innerHTML = value;
            this.downloadEl.href = CStudioAuthoringContext.previewAppBaseUri + value;

            this.addEl.value = "Replace";

            var loaded = false;
            var image = new Image();
            image.src="";
            function imageLoaded(){
                _self.originalWidth = this.width;
                _self.originalHeight = this.height;
                _self.adjustImage();
            };
            image.addEventListener('load', imageLoaded, false);
            image.src = CStudioAuthoringContext.previewAppBaseUri + value + "?" + (new Date().getTime());
        }
        this._onChange(null, this);
    },

    getName: function() {
        return "image-picker";
    },

    getSupportedProperties: function() {
        return [
            { label: "Width", name: "width", type: "range" },
            { label: "Height", name: "height", type: "range" },
            { label: "Thumbnail Width", name: "thumbnailWidth", type: "int" },
            { label: "Thumbnail Height", name: "thumbnailHeight", type: "int" },
            { label: "Data Source", name: "imageManager", type: "datasource:image" },
            { label: "Readonly", name: "readonly", type: "boolean" }
        ];
    },

    getSupportedConstraints: function() {
        return [
            { label: "Required", name: "required", type: "boolean" }
        ];
    }
});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-image-picker", CStudioForms.Controls.ImagePicker);