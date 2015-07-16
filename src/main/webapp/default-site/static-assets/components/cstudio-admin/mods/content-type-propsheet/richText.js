CStudioAdminConsole.Tool.ContentTypes.PropertyType.RichText = CStudioAdminConsole.Tool.ContentTypes.PropertyType.RichText ||  function(fieldName, containerEl)  {
    this.fieldName = fieldName;
    this.containerEl = containerEl;
    return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes.PropertyType.RichText, CStudioAdminConsole.Tool.ContentTypes.PropertyType, {
    render: function(value, updateFn) {
        this.value = value;
        this.updateFn = updateFn;
        var containerEl = this.containerEl;
        var valueEl = document.createElement("input");
        YAHOO.util.Dom.addClass(valueEl, "content-type-property-sheet-property-value");
        YDom.setStyle(valueEl,"font-style","italic");
        containerEl.appendChild(valueEl);
        valueEl.value = this.valueToString(value);
        valueEl.context = this;
        valueEl.fieldName = this.fieldName;
        // don't let the user type anything
        YAHOO.util.Event.on(valueEl, 'keydown', function(evt) { YAHOO.util.Event.stopEvent(evt); }, valueEl);
        YAHOO.util.Event.on(valueEl, 'focus', this.showEdit, this);
        this.valueEl = valueEl;
    },

    getValue: function() {
        return this.value;
    },

    valueToString: function(value) {
        var strValue = "";

        if(!Array.isArray(value) && value !== ""){
           strValue = "Edit message...";
        }else{
           strValue = "Set message...";
        }

        return strValue;
    },

    showEdit: function() {
        var _self = this;
        var richTextDialogEl = document.getElementById("richTextDialog");
        if(!richTextDialogEl) {
            var maskEl = document.createElement("div");
            maskEl.id = 'keyValueDialogMask';
            maskEl.style.display = 'block';
            richTextDialogEl = document.createElement("div");
            richTextDialogEl.id = 'richTextDialog';
			YAHOO.util.Dom.addClass(richTextDialogEl, "rich-text-dialog");
            YAHOO.util.Dom.addClass(richTextDialogEl, "seethrough");

            document.body.appendChild(maskEl);
            document.body.appendChild(richTextDialogEl);

            richTextDialogEl.value = "";
            richTextDialogEl.value = _self.context.value;
        }

        richTextDialogEl.style.display = "block";
        richTextDialogEl.innerHTML = "";

        var titleEl = document.createElement("div");
        YAHOO.util.Dom.addClass(titleEl, "property-dialog-title");
        titleEl.innerHTML = "Field Content";
        richTextDialogEl.appendChild(titleEl);

        var richTextDialogContainerEl = document.createElement("div");
		richTextDialogContainerEl.id = "richTextBodyDialog";
        YAHOO.util.Dom.addClass(richTextDialogContainerEl, "property-dialog-body-container");
        richTextDialogEl.appendChild(richTextDialogContainerEl);

        this.context.renderText();

        var buttonContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(buttonContainerEl, "property-dialog-button-container");
        richTextDialogEl.appendChild(buttonContainerEl);

        var cancelEl = document.createElement("div");
        cancelEl.style.marginRight = "6px";
        YAHOO.util.Dom.addClass(cancelEl, "btn btn-default");
        cancelEl.innerHTML = "Cancel";
        buttonContainerEl.appendChild(cancelEl);

        YAHOO.util.Event.on(cancelEl, 'click', function(evt) {
            _self.context.cancel();
        }, cancelEl);

        var saveEl = document.createElement("div");
        saveEl.style.marginRight = "16px";
        YAHOO.util.Dom.addClass(saveEl, "btn btn-primary");
        saveEl.innerHTML = "Save";
        buttonContainerEl.appendChild(saveEl);
        YAHOO.util.Event.on(saveEl, 'click', function(evt) {
            _self.context.save();
        }, saveEl);

    },

    renderText: function() {
        //TODO: Add rte control
        var richTextDialogEl = document.getElementById("richTextDialog");
        var dialogContainerEl = document.getElementById("richTextBodyDialog");
        YDom.setStyle(dialogContainerEl,"text-align","center");

        var rteContainerEl = document.createElement("div");
        rteContainerEl.id = "richTextDialogRteContainer";
        dialogContainerEl.appendChild(rteContainerEl);
        YDom.addClass(rteContainerEl, "text-mode");
        YDom.addClass(rteContainerEl, "rte-active");
        YDom.setStyle(rteContainerEl,"text-align","center");
        YDom.setStyle(rteContainerEl,"display","inline-block");
        YDom.setStyle(rteContainerEl,"width","90%");

        var _self = this;
        var rteUniqueInitClass = CStudioAuthoring.Utils.generateUUID();
        var value = richTextDialogEl.value;
        var inputEl = document.createElement("textarea");
        rteContainerEl.appendChild(inputEl);
        YDom.addClass(inputEl, rteUniqueInitClass);
        YDom.addClass(inputEl, 'cstudio-form-control-input');
        richTextDialogEl.inputEl = inputEl;

        inputEl.value = CStudioForms.Util.unEscapeXml(value);


        var editor = tinyMCE.init({
            // General options
            autoresize_min_height: 200,
            autoresize_max_height: 500,
            mode : "textareas",
            editor_selector : rteUniqueInitClass,
            theme : "advanced",
            width : "100%",
            focusHeight : "90%",
            height: "100%",
            encoding : "xml",
            paste_auto_cleanup_on_paste : true,
            relative_urls : true,
            readonly: false,
            // autoresize_on_init: true,
            force_p_newlines: true,
            force_br_newlines: false,
            forced_root_block: false,
            document_base_url: CStudioAuthoringContext.previewAppBaseUri,
            theme_advanced_resizing : false,
            theme_advanced_resize_horizontal : false,
            theme_advanced_toolbar_location : "top",
            theme_advanced_toolbar_align : "left",
            theme_advanced_statusbar_location : "bottom",

            theme_advanced_buttons1 : "bold,italic,underline,|,forecolor,backcolor,|,bullist,numlist,|,link,unlink,anchor,|,undo,redo",
            theme_advanced_buttons2 : "formatselect,fontselect,fontsizeselect,|,justifyleft,justifycenter,justifyright,justifyfull",
            theme_advanced_buttons3 : "",
            theme_advanced_buttons4 : "",
            content_css : "",

            // Drop lists for link/image/media/template dialogs
            template_external_list_url : "js/template_list.js",
            external_link_list_url : "js/link_list.js",
            external_image_list_url : "js/image_list.js",
            media_external_list_url : "js/media_list.js",
            plugins : "paste, noneditable, ",

            setup: function(ed) {

                try {
                    ed.contextControl = richTextDialogEl;
                    richTextDialogEl.editor = ed;

                }catch(err){
                    //log failure
                }
            }
        });


    },

    cancel: function() {
        var richTextDialogEl = document.getElementById("richTextDialog");
        var keyValueDialogMaskEl = document.getElementById("keyValueDialogMask");
        richTextDialogEl.parentNode.removeChild(keyValueDialogMaskEl);
        richTextDialogEl.parentNode.removeChild(richTextDialogEl);
    },

    save: function() {
        var richTextDialogEl = document.getElementById("richTextDialog");
        var keyValueDialogMaskEl = document.getElementById("keyValueDialogMask");
        if(richTextDialogEl.editor) {
            richTextDialogEl.editor.save();
            richTextDialogEl.value = richTextDialogEl.inputEl.value;
        }

        this.value = richTextDialogEl.value;
        this.valueEl.value = this.valueToString(this.value);
        richTextDialogEl.parentNode.removeChild(keyValueDialogMaskEl);
        richTextDialogEl.parentNode.removeChild(richTextDialogEl);
        this.updateFn(null, { fieldName: this.fieldName, value: this.value });
    }

});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types-proptype-richText", CStudioAdminConsole.Tool.ContentTypes.PropertyType.RichText);