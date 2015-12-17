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
        this.crop_dialog = null;
        this.validExtensions = ["jpg", "jpeg", "gif", "png", "tiff", "tif", "bmp", "svg", "JPG", "JPEG", "GIF", "PNG", "TIFF", "TIF", "BMP", "SVG"];
        this.readonly = readonly;
        this.originalWidth = null;
        this.originalHeight = null;
        this.previewBoxHeight = 100;
        this.previewBoxWidth = 300;

        return this;
    }

YAHOO.extend(CStudioForms.Controls.ImagePicker, CStudioForms.CStudioFormField, {


    getLabel: function() {
        return CMgs.format(langBundle, "image");
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

                }, isDefault:false } ]
            });
        dialog.setHeader("CStudio Warning");
        dialog.render(document.body);
        dialog.show();
    },

    createCropDialog: function(Message, imageData, imagePickerCrop) {
        self = this;
        YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");

        var newdiv = YDom.get("cstudio-wcm-popup-div");
        if (newdiv == undefined) {
            newdiv = document.createElement("div");
            document.body.appendChild(newdiv);
        }

        var divIdName = "cstudio-wcm-popup-div";
        newdiv.setAttribute("id",divIdName);
        newdiv.className= "yui-pe-content";
        newdiv.innerHTML = '<div class="contentTypePopupInner" id="crop-popup-inner">' +
            '<div class="contentTypePopupContent" id="contentTypePopupContent"> ' +
                '<div class="contentTypePopupHeader">Crop Image</div> ' +
                '<div>'+
                    '<div class="contentTypeOuter clearfix">'+
                        '<div class="formDesc">' + Message + '</div> ' +
                        '<div class="leftControls">' +
                            '<div class="cropContainer">' +
                                '<img src="'+imageData.previewUrl+'?+'+ new Date().getTime()+'">' +
                            '</div>' +
                            '<div class="cropMethods">' +
                                '<div class="btn-group">' +
                                    '<button type="button" class="btn btn-primary" data-method="zoom" data-option="0.1" title="Zoom In" id="zoomIn">' +
                                        '<span class="docs-tooltip" data-toggle="tooltip" title="Zoom In" data-original-title="Zoom In">' +
                                            '<span class="status-icon zoom-in"></span>' +
                                        '</span>' +
                                    '</button>' +
                                    '<button type="button" class="btn btn-primary" data-method="zoom" data-option="-0.1" title="Zoom Out" id="zoomOut">' +
                                        '<span class="docs-tooltip" data-toggle="tooltip" title="Zoom Out" data-original-title="Zoom Out)">' +
                                            '<span class="status-icon zoom-out"></span>' +
                                        '</span>' +
                                    '</button>' +
                                '</div>' +
                                '<button type="button" class="btn btn-primary refresh" data-method="getContainerData" data-option="" id="refresh">' +
                                    '<span class="docs-tooltip" data-toggle="tooltip" title="Refresh" data-original-title="Refresh">' +
                                        '<span class="status-icon refresh"></span>' +
                                    '</span>' +
                                '</button>' +
                                '<span id="zoomMessage" class="hidden">Increasing zoom creates an image which is smaller than the constraints</span>' +
                            '</div>' +
                        '</div>' +
                        '<div class="rightControls">' +
                            '<div class="img-preview preview-sm"></div>' +
                            '<div class="docs-data">' +
                                '<div class="input-group">' +
                                    '<label class="input-group-addon" for="dataX">X</label>' +
                                    '<input type="text" class="form-control" id="dataX" placeholder="x" disabled>' +
                                    '<span class="input-group-addon">px</span>' +
                                '</div>' +
                                '<div class="input-group">' +
                                    '<label class="input-group-addon" for="dataY">Y</label>' +
                                    '<input type="text" class="form-control" id="dataY" placeholder="y" disabled>' +
                                    '<span class="input-group-addon">px</span>' +
                                '</div>' +
                                '<div class="input-group">' +
                                    '<label class="input-group-addon" for="dataWidth">Width</label>' +
                                    '<input type="text" class="form-control" id="dataWidth" placeholder="width" disabled>' +
                                    '<span class="input-group-addon">px</span>' +
                                '</div>' +
                                '<div class="input-group">' +
                                    '<label class="input-group-addon" for="dataHeight">Height</label>' +
                                    '<input type="text" class="form-control" id="dataHeight" placeholder="height" disabled>' +
                                    '<span class="input-group-addon">px</span>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="contentTypePopupBtn"> ' +
                        '<input type="button" class="btn btn-primary cstudio-xform-button ok" id="cropButton" value="Crop" />' +
                        '<input type="button" class="btn btn-default cstudio-xform-button" id="uploadCancelButton" value="Cancel" /></div>' +
                    '</div>' +
                '</div> ' +
            '</div>';

        // Instantiate the Dialog
        crop_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div",
            {   width : "830px",
                height : "492px",
                effect:{
                    effect: YAHOO.widget.ContainerEffect.FADE,
                    duration: 0.25
                },
                fixedcenter : true,
                visible : false,
                modal:true,
                close:false,
                constraintoviewport : true,
                underlay:"none"
            });

        // Render the Dialog
        crop_dialog.render();

        var $image = $('.cropContainer > img');
        var widthConstrains = JSON.parse(self.width);
        var heightConstrains = JSON.parse(self.height);
        var flag;
        var boxResizable = false;

        var $dataX = $('#dataX');
        var $dataY = $('#dataY');
        var $dataHeight = $('#dataHeight');
        var $dataWidth = $('#dataWidth');

        var widthCropBox, minWidthCropBox, maxWidthCropBox, heightCropBox, minHeightCropBox, maxHeightCropBox;

        function getPercentage(min, max){
            var result;
            if(min && max){
                result = min + ((max -  min) / 2);
            }else if(min){
                result = min + 1;
            }else{
                result = max - 1;
            }
            return result;
        }

        if(widthConstrains.exact){
            widthCropBox = widthConstrains.exact;
            flag = 'exact';
        }else if (!isNaN(widthConstrains)){
            widthCropBox = widthConstrains;
            flag = 'exact';
        }else{
            flag = 'variable';
            boxResizable = true;
            if(widthConstrains.min){
                minWidthCropBox = widthConstrains.min;
            }
            if(widthConstrains.max){
                maxWidthCropBox = widthConstrains.max;
            }
            if(minWidthCropBox || maxWidthCropBox){
                widthCropBox = getPercentage(parseInt(minWidthCropBox), parseInt(maxWidthCropBox));
            }
        }
        if(heightConstrains.exact){
            heightCropBox = heightConstrains.exact;
        }else if(!isNaN(heightConstrains)){
            heightCropBox = heightConstrains;
        }else{
            if(heightConstrains.min){
                minHeightCropBox = heightConstrains.min;
            }
            if(heightConstrains.max){
                maxHeightCropBox = heightConstrains.max;
            }
            if(minHeightCropBox || maxHeightCropBox){
                heightCropBox = getPercentage(parseInt(minHeightCropBox), parseInt(maxHeightCropBox));
            }
        }

        $image.cropper({
            minCropBoxWidth: this,
            dragCrop: false,
            cropBoxResizable: boxResizable,
            preview: '.img-preview',
            crop: function(e) {
                // Output the result data for cropping image.
                $dataX.val(Math.round(e.x));
                $dataY.val(Math.round(e.y));
                $dataHeight.val(Math.round(e.height));
                $dataWidth.val(Math.round(e.width));
                if(flag == "exact"){
                    if (!($dataHeight.val() == heightCropBox) && !($dataWidth.val() == widthCropBox)){
                        $('#zoomMessage').removeClass("hidden");
                        $dataWidth.addClass("error");
                        $dataHeight.addClass("error");
                        //$('#cropButton').prop('disabled',true);
                    }else{
                        $('#zoomMessage').addClass("hidden");
                        $dataWidth.removeClass("error");
                        $dataHeight.removeClass("error");
                        //$('#cropButton').prop('disabled',false);
                    }
                }else{
                    inputValidation(parseInt(minHeightCropBox), parseInt(maxHeightCropBox), $dataHeight, $dataWidth);
                    inputValidation(parseInt(minWidthCropBox), parseInt(maxWidthCropBox), $dataWidth, $dataHeight);
                }

            },
            built: function () {
                $image.cropper('setData', {"width":parseInt(widthCropBox),"height": parseInt(heightCropBox)});
                $dataHeight.val(heightCropBox);
                $dataWidth.val(widthCropBox);
                $('#zoomMessage').addClass("hidden");
                $dataHeight.removeClass("error");
                $dataWidth.removeClass("error");
                //$('#cropButton').prop('disabled',false);
            }
        });

        $('#zoomIn').on('click', function () {
            $image.cropper('zoom', 0.1);
        });

        $('#zoomOut').on('click', function () {
            $image.cropper('zoom', -0.1);
        });

        $('#refresh').on('click', function () {
            $image.cropper('reset');
            $image.cropper('setData', {"width":parseInt(widthCropBox),"height": parseInt(heightCropBox)});
        });

        function inputValidation(min, max, input, auxInput){
            if (((min && max) && (input.val() >= min) && (input.val() <= max)) ||
                ((min && !max) && (input.val() >= min)) ||
                ((!min && max) && (input.val() <= max))){
                $('#zoomMessage').addClass("hidden");
                input.removeClass("error");
                //$('#cropButton').prop('disabled',false);
            }else{
                $('#zoomMessage').removeClass("hidden");
                input.addClass("error");
               // $('#cropButton').prop('disabled',true);
            }
            if(input.hasClass("error") || auxInput.hasClass("error")){
                //$('#cropButton').prop('disabled',true);
                $('#zoomMessage').removeClass("hidden");
            }
        }

        function cropImage(){
            var imageInformation = $image.cropper('getData', true),
                path = imageData.relativeUrl,
                site = CStudioAuthoringContext.site;
            try {
                CStudioAuthoring.Service.cropImage(site, path, imageInformation.x, imageInformation.y, imageInformation.height, imageInformation.width, {
                    success: function(content) {
                        var imagePicker = self;
                        self.setImageData(imagePicker, imageData)
                        self.cropPopupCancel();
                    },
                    failure: function(message) {
                        self.showAlert(JSON.parse(message.responseText).message);
                        self.cropPopupCancel();
                    }
                });
            } catch(err) {
            }
        }

        YAHOO.util.Event.addListener("uploadCancelButton", "click", this.cropPopupCancel, this, true);
        YAHOO.util.Event.addListener("cropButton", "click", cropImage, this, true);
        this.crop_dialog = crop_dialog;
        return crop_dialog;
        upload_dialog.show();
    },

    cropPopupCancel: function(event) {
        this.crop_dialog.destroy();
    },

    setImageData: function(imagePicker, imageData){
        imagePicker.inputEl.value = imageData.relativeUrl;


        imagePicker.previewEl.src = imageData.previewUrl+ "?" +new Date().getTime();
        imagePicker.urlEl.innerHTML = imageData.relativeUrl;
        imagePicker.downloadEl.href = imageData.previewUrl;

        imagePicker.addEl.value = "Replace";

        imagePicker.noPreviewEl.style.display = "none";
        imagePicker.previewEl.style.display = "inline";
        YAHOO.util.Dom.addClass(imagePicker.previewEl, 'cstudio-form-control-asset-picker-preview-content');


        imagePicker.adjustImage();

        imagePicker._onChangeVal(null, imagePicker);
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
//            else {
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

                    //if (imageManagerNames.search(regexpr) > -1) {
                        if (imageManagerNames.indexOf(el.id) != -1) {
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
            //}
        }
        else if(imageManagerNames != ""){
            imageManagerNames = imageManagerNames.replace("[\"","").replace("\"]","");
            this._addImage(datasourceMap[imageManagerNames]);
        }
    },

    _addImage: function(datasourceEl) {
        self = this;
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
                                    var widthConstrains = JSON.parse(self.width);
                                    var heightConstrains = JSON.parse(self.height);
                                    message = "The uploaded file does not meet the specified width & height constraints";
                                    //imagePicker.deleteImage();
                                    if((widthConstrains.min && imagePicker.originalWidth < widthConstrains.min)
                                        || (heightConstrains.min && imagePicker.originalHeight < heightConstrains.min)
                                        || (widthConstrains.exact && imagePicker.originalWidth < widthConstrains.exact)
                                        || (heightConstrains.exact && imagePicker.originalHeight < heightConstrains.exact)
                                        || (widthConstrains && imagePicker.originalWidth < widthConstrains)
                                        || (heightConstrains && imagePicker.originalHeight < heightConstrains)){
                                        message = "Image is smaller than the constraint size";
                                        self.showAlert(message);
                                    }else{
                                        this.dialog = imagePicker.createCropDialog(message, imageData, this.imagePicker);
                                        this.dialog.show();

                                    }

                                    //this.isUploadOverwrite = isUploadOverwrite;
                                }else{
                                    if(this.setImageData){
                                        this.setImageData(imagePicker, imageData)
                                    }else{
                                        self.setImageData(imagePicker, imageData)
                                    }
                                }
                            };
                            image.addEventListener('load', imageLoaded, false);
                            image.addEventListener('error', function () {
                                message = "Unable to load the selected image. Please try again or select another image";
                                imagePicker.showAlert(message);
                            });
                            CStudioAuthoring.Operations.getImageRequest({
                                url: imageData.previewUrl,
                                image: image
                            });


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

            this._onChangeVal(null, this);
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
        downloadImageEl.src = "/studio/static-assets/themes/cstudioTheme/images/download.png";
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
        this.edited = false;
    },

    getName: function() {
        return "image-picker";
    },

    getSupportedProperties: function() {
        return [
            { label: CMgs.format(langBundle, "width"), name: "width", type: "range" },
            { label: CMgs.format(langBundle, "height"), name: "height", type: "range" },
            { label: CMgs.format(langBundle, "thumbnailWidth"), name: "thumbnailWidth", type: "int" },
            { label: CMgs.format(langBundle, "thumbnailHeight"), name: "thumbnailHeight", type: "int" },
            { label: CMgs.format(langBundle, "datasource"), name: "imageManager", type: "datasource:image" },
            { label: CMgs.format(langBundle, "readonly"), name: "readonly", type: "boolean" }
        ];
    },

    getSupportedConstraints: function() {
        return [
            { label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" }
        ];
    }
});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-image-picker", CStudioForms.Controls.ImagePicker);