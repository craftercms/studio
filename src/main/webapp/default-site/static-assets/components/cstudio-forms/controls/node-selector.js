CStudioForms.Controls.NodeSelector = CStudioForms.Controls.NodeSelector ||
    function(id, form, owner, properties, constraints, readonly)  {
        this.owner = owner;
        this.owner.registerField(this);
        this.errors = [];
        this.nodes = [];
        this.properties = properties;
        this.constraints = constraints;
        this.inputEl = null;
        this.countEl = null;
        this.required = false;
        this.value = "_not-set";
        this.form = form;
        this.id = id;
        this.allowEdit = false;
        this.selectedItemIndex = -1;
        this.items =  [];
        this.readonly = readonly;
        this.allowDuplicates = false;
        this.minSize = 0;
        this.maxSize = 0;
        this.readonly = readonly;
        this.defaultValue = "";
        this.disableFlattening = false;
        this.useSingleValueFilename = false;
        amplify.subscribe("/datasource/loaded", this, this.onDatasourceLoaded);

        return this;
    }

CStudioForms.Controls.NodeSelector.prototype = {
    Node: {
        label: '',
        value: ''
    }
}

YAHOO.extend(CStudioForms.Controls.NodeSelector, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return CMgs.format(langBundle, "itemSelector");
    },

    getRequirementCount: function() {
        var count = 0;

        if(this.minSize > 0){
            count++;
        }

        return count;
    },

    _onChange: function() {

        if(this.minSize > 0){//Needs validation
            if(this.items.length < this.minSize) {
                this.setError("minCount", "# items are required");
                this.renderValidation(true, false);
            }
            else {
                this.clearError("minCount");
                this.renderValidation(true, true);
            }
        }

        this.owner.notifyValidation();
        this.form.updateModel(this.id, this.getValue());
        this.inputEl.value = JSON.stringify(this.getValue());
        this._renderItems();
    },

    // Node object
    node: {
        label: '',
        value: ''
    },

    addNode: function() {
        // Create element
        // Add to global node array
    },

    deleteNode: function() {
        // Drop element from global node array
    },

    editNode: function() {
    },

    onDatasourceLoaded: function( data ) {
        if(this.datasourceName === data.name && !this.datasource){
            this._setActions();
        }
    },

    render: function(config, containerEl) {
        containerEl.id = this.id;
        this.maxSize = 0;
        this.minSize = 0;

        for(var i=0;i<config.constraints.length;i++){
            var constraint = config.constraints[i];

            if(constraint.name == "allowDuplicates" && constraint.value == "true"){
                this.allowDuplicates = true;
            }
        }

        var _self = this;
        for(var i=0; i<config.properties.length; i++) {
            var prop = config.properties[i];

            if(prop.name == "itemManager") {
                this.datasourceName = (Array.isArray(prop.value))?prop.value[0]:prop.value;
                this.datasourceName = this.datasourceName.replace("[\"","").replace("\"]","");
            }
            if(prop.name == "minSize" && prop.value != "") {
                this.minSize = parseInt(prop.value, 10);
            }
            if(prop.name == "maxSize" && prop.value != "") {
                this.maxSize = parseInt(prop.value, 10);
            }
            if(prop.name == "readonly" && prop.value == "true"){
                this.readonly = true;
            }
            if(prop.name == "useSingleValueFilename" && prop.value == "true"){
                this.useSingleValueFilename = true;
            }
            if(prop.name == "disableFlattening" && prop.value == "true"){
                this.disableFlattening = true;
            }
        }

        var titleEl = document.createElement("span");
        YAHOO.util.Dom.addClass(titleEl, 'label');
        YAHOO.util.Dom.addClass(titleEl, 'cstudio-form-field-title');
        titleEl.innerHTML = config.title;

        var controlWidgetContainerEl = document.createElement("div");
        YAHOO.util.Dom.addClass(controlWidgetContainerEl, 'cstudio-form-control-node-selector-container');

        var validEl = document.createElement("span");
        YAHOO.util.Dom.addClass(validEl, 'validation-hint');
        YAHOO.util.Dom.addClass(validEl, 'cstudio-form-control-validation');
        controlWidgetContainerEl.appendChild(validEl);

        var hiddenEl = document.createElement("input");
        hiddenEl.type = "hidden";
        YAHOO.util.Dom.addClass(hiddenEl, 'datum');
        this.inputEl = hiddenEl;
        controlWidgetContainerEl.appendChild(hiddenEl);

        var nodeControlboxEl = document.createElement("div")
        YAHOO.util.Dom.addClass(nodeControlboxEl, 'cstudio-form-control-node-selector-controlbox');
        controlWidgetContainerEl.appendChild(nodeControlboxEl);

        var nodeItemsContainerEl = document.createElement("div")
        nodeItemsContainerEl.id = this.id + "-target";
        YAHOO.util.Dom.addClass(nodeItemsContainerEl, 'cstudio-form-control-node-selector-items-container');
        nodeControlboxEl.appendChild(nodeItemsContainerEl);
        this.itemsContainerEl = nodeItemsContainerEl;

        var nodeOptionsEl = document.createElement("div")
        YAHOO.util.Dom.addClass(nodeOptionsEl, 'cstudio-form-control-node-selector-options');
        nodeControlboxEl.appendChild(nodeOptionsEl);

        //Add button
        var addButtonEl = document.createElement("input")
        addButtonEl.type = 'button';
        addButtonEl.value = 'Add';
        addButtonEl.disabled = true;
        YAHOO.util.Dom.addClass(addButtonEl, 'cstudio-button');
        YAHOO.util.Dom.addClass(addButtonEl, 'cstudio-drop-arrow-button');
        YAHOO.util.Dom.addClass(addButtonEl, 'cstudio-button-disabled');
        nodeOptionsEl.appendChild(addButtonEl);
        this.addButtonEl = addButtonEl;

        //Edit button
        var editButtonEl = document.createElement("input")
        editButtonEl.type = 'button';
        editButtonEl.value = 'Edit';
        editButtonEl.disabled = true;
        YAHOO.util.Dom.addClass(editButtonEl, 'cstudio-button');
        YAHOO.util.Dom.addClass(editButtonEl, 'cstudio-button-disabled');
        nodeOptionsEl.appendChild(editButtonEl);
        this.editButtonEl = editButtonEl;

        //Delete button
        var deleteButtonEl = document.createElement("input")
        deleteButtonEl.type = 'button';
        deleteButtonEl.value = 'X';
        YAHOO.util.Dom.addClass(deleteButtonEl, 'cstudio-button');
        YAHOO.util.Dom.addClass(deleteButtonEl, 'cstudio-button-disabled');
        nodeOptionsEl.appendChild(deleteButtonEl);
        deleteButtonEl.disabled = true;
        this.deleteButtonEl = deleteButtonEl;

        if(this.readonly == true){
            addButtonEl.disabled = true;
            editButtonEl.disabled = true;
            deleteButtonEl.disabled = true;
            YAHOO.util.Dom.addClass(addButtonEl, 'cstudio-button-disabled');
            YAHOO.util.Dom.addClass(editButtonEl, 'cstudio-button-disabled');
            YAHOO.util.Dom.addClass(deleteButtonEl, 'cstudio-button-disabled');
        }

        this.renderHelp(config, nodeOptionsEl);

        var countEl = document.createElement("div");
        YAHOO.util.Dom.addClass(countEl, 'item-count');
        YAHOO.util.Dom.addClass(countEl, 'cstudio-form-control-node-selector-count');
        this.countEl = countEl;
        nodeOptionsEl.appendChild(countEl);

        var descriptionEl = document.createElement("span");
        YAHOO.util.Dom.addClass(descriptionEl, 'description');
        YAHOO.util.Dom.addClass(descriptionEl, 'cstudio-form-field-description');
        descriptionEl.innerHTML = config.description;
        controlWidgetContainerEl.appendChild(descriptionEl);

        containerEl.appendChild(titleEl);
        containerEl.appendChild(controlWidgetContainerEl);
        this.defaultValue = config.defaultValue;

        this._renderItems();
        this._setActions();
    },

    _setActions: function () {
        var _self = this;
        var datasource = this.form.datasourceMap[this.datasourceName];

        if( datasource && this.readonly == false ){
            this.datasource = datasource;

            if (datasource.add) {
                YAHOO.util.Dom.removeClass(this.addButtonEl, 'cstudio-button-disabled');
                this.addButtonEl.disabled = false;
                // give control to the node selector to render the add
                YAHOO.util.Event.on(this.addButtonEl, 'click', function() {
                    var selectItemsCount = _self.getItemsLeftCount();
                    if (selectItemsCount == 0) {
                        alert("You can't add more items, Remove one and try again.");
                    }
                    else{
                        datasource.selectItemsCount = selectItemsCount;
                        datasource.add(_self);
                    }
                }, this.addButtonEl);
            }

            if (datasource.edit) {
                this.allowEdit = true;
                YAHOO.util.Event.on(this.editButtonEl, 'click', function() {
                    datasource.edit(_self.items[_self.selectedItemIndex].key, _self);
                }, this.editButtonEl);
            }

            YAHOO.util.Event.on(this.deleteButtonEl, 'click', function() {
                _self.deleteItem(_self.selectedItemIndex);
                _self._renderItems();
            }, this.deleteButtonEl);
        }
    },

    _renderItems: function() {
        var itemsContainerEl = this.itemsContainerEl;

        if((typeof this.items) == "string") {
            this.items = [];
        }



        var items =  this.items;

        itemsContainerEl.innerHTML = "";
        var tar = new YAHOO.util.DDTarget(itemsContainerEl);
        for(var i=0; i<items.length; i++) {
            var item = items[i];
            var itemEl = document.createElement("div");
            if(this.readonly != true){
                var dd = new NodeSelectorDragAndDropDecorator(itemEl);
            }

            YAHOO.util.Dom.addClass(itemEl, 'cstudio-form-control-node-selector-item');
            itemEl.innerHTML = item.value;
            itemEl.style.backgroundColor = "#F0F0F0"; // stylesheet not working due to proxy?
            itemEl._index = i;
            itemEl.context = this;

            if(this.selectedItemIndex == i) {
                YAHOO.util.Dom.addClass(itemEl, 'cstudio-form-control-node-selector-item-selected');
            }

            if(this.readonly != true){
                itemEl._onMouseDown =  function(){
                    this.context.selectedItemIndex = this._index;
                    var selectedEl = YAHOO.util.Dom.getElementsByClassName("cstudio-form-control-node-selector-item-selected", null, this.context.itemsContainerEl)[0];
                    if(selectedEl){
                        YAHOO.util.Dom.removeClass(selectedEl,"cstudio-form-control-node-selector-item-selected");
                    }

                    YAHOO.util.Dom.addClass(this, 'cstudio-form-control-node-selector-item-selected');
                    YAHOO.util.Dom.removeClass(this.context.deleteButtonEl, 'cstudio-button-disabled');
                    this.context.deleteButtonEl.disabled = false;

                    if(this.context.allowEdit == true) {
                        YAHOO.util.Dom.removeClass(this.context.editButtonEl, 'cstudio-button-disabled');
                        this.context.editButtonEl.disabled = false;
                    }
                }
            }
            itemsContainerEl.appendChild(itemEl);

        }
    },

    getItemsLeftCount: function(){
        if((typeof this.items) == "string") {
            this.items = [];
        }

        if(this.maxSize > 0){
            return this.maxSize - this.items.length;
        }
        return -1;
    },

    getIndex: function(key) {
        var index = -1;
        var values = this.getValue();

        for(var i=0; i<values.length; i++) {
            if(values[i].key == key) {
                index = i;
                break;
            }
        }

        return index;
    },

    /**
     * move a item from it's current position to a new position
     */
    moveExistingItemBefore: function(onTheMoveIndex, beforeItemIndex) {
        var item = this.items[onTheMoveIndex];
        this.items.splice(onTheMoveIndex, 1);
        this.items.splice(beforeItemIndex, 0, item);
        this.selectedItemIndex = beforeItemIndex;
        this._onChange();
    },

    deleteItem: function(index) {
        if(index != -1) {
            this.items.splice(index, 1);
            this.count();
            this._onChange();
        }
    },

    insertItem: function(key, value, fileType, fileSize) {
        var successful = true;
        var message = "";
        if(this.allowDuplicates != true){
            var items =  this.items;
            for(var i=0; i<items.length;i++){
                var item = items[i];
                if(item.key == key){
                    successful = false;
                    message = "The item \"" + value + "\" already exists.";
                    break;
                }
            }
        }

        if(this.maxSize > 0){
            if(this.items.length >= this.maxSize){
                successful = false;
                message = "You can't add more items, Remove one and try again.";
            }
        }

        if(successful){
            var item = {};

            if(this.useSingleValueFilename == true) {
            /* the initial assumption was that a node selector would be used to pick a single file. _s tells
             * the search index that the value is a single value.  If the node selector is used to pick multiple files
             * the indexing operation will fail. Because the node selctor is inheriently multi-valued in nature the 
             * default going forward is to treat these values as multi-valued.  For backward compatibility we will support
             * _s if the form definition specifies that we do so
             */
                if (fileType && fileSize) {
                    item = { key: key, value: value, fileType_s: fileType, fileSize_s: fileSize };
                } else if (fileType && !fileSize) {
                    item = { key: key, value: value, fileType_s: fileType };
                } else if (!fileType && fileSize) {
                    item = { key: key, value: value, fileSize_s: fileSize };
                } else {
                    item = { key: key, value: value};
                }
            }
            else {
                if (fileType && fileSize) {
                    item = { key: key, value: value, fileType_mvs: fileType, fileSize_s: fileSize };
                } else if (fileType && !fileSize) {
                    item = { key: key, value: value, fileType_mvs: fileType };
                } else if (!fileType && fileSize) {
                    item = { key: key, value: value, fileSize_mvs: fileSize };
                } else {
                    item = { key: key, value: value};
                }
            }

            this.items[this.items.length] = item

            if(this.datasource.itemsAreContentReferences) {
                if(key.indexOf(".xml") != -1) {
                    item.include = key;
                    item.disableFlattening = this.disableFlattening;
                }
            }

            this.count();
            this._onChange();
        }else{
            alert(message);
        }
        this.count();
        this._onChange();
    },

    count: function(){
        var itemCount = this.items.length;


        if (this.maxSize && this.maxSize != 0) {
            this.countEl.innerHTML = itemCount + ' / ' + this.maxSize;
        }
        else {
            this.countEl.innerHTML = itemCount;
        }
    },

    getValue: function() {
        return this.items;
    },

    updateEditedItem: function(value) {
        var item = this.items[this.selectedItemIndex];
        item.value =  value;
        this._renderItems();
        this._onChange();
    },

    updateItems: function() {
        if(this.datasource && this.datasource.updateItem){
            for(var i = 0; i < this.items.length; i++){
                this.datasource.updateItem(this.items[i],this);
                this.items[i].disableFlattening = this.disableFlattening;
            }
        }
    },

    setValue: function(value) {
        this.items = value;


        if((typeof this.items) == "string") {
            //Check if the current value is the default value, split it by comma and load it using key/value pair
            if(this.items === this.defaultValue && this.items != ""){
                this.items = [];
                var defaultItems = this.defaultValue.split(",");
                for(var i = 0; i < defaultItems.length; i++){
                    var item = { key: defaultItems[i], value: defaultItems[i] };
                    this.items[this.items.length] = item
                    if(this.datasource.itemsAreContentReferences) {
                        if(defaultItems[i].indexOf(".xml") != -1) {
                            item.include = defaultItems[i];
                            item.disableFlattening = this.disableFlattening;
                        }
                    }
                }
            }else{
                this.items = [];
            }
        }

        if(this.items.length > 0) {
            if(this.items[0].value == "") {
                this.items = this.items[0].splice();
            }
        }
        
        this.updateItems();
        this._onChange();
        this.count();
    },

    getName: function() {
        return "node-selector";
    },

    getSupportedProperties: function() {
        return [
            { label: CMgs.format(langBundle, "minSize"), name: "minSize", type: "int" },
            { label: CMgs.format(langBundle, "maxSize"), name: "maxSize", type: "int" },
            { label: CMgs.format(langBundle, "itemManager"), name: "itemManager", type: "datasource:item" },
            { label: CMgs.format(langBundle, "readonly"), name: "readonly", type: "boolean" },
            { label: CMgs.format(langBundle, "disableFlatteningSearch"), name: "disableFlattening", type: "boolean" },
            { label: CMgs.format(langBundle, "singleValueFilename"), name: "useSingleValueFilename", type: "boolean" }
        ];
    },

    getSupportedConstraints: function() {
        return [
            { label: CMgs.format(langBundle, "allowDuplicate"), name: "allowDuplicates", type: "boolean" }
        ];
    }
});

/**
 * drag and drop controls
 */
NodeSelectorDragAndDropDecorator = function(id, sGroup, config) {

    NodeSelectorDragAndDropDecorator.superclass.constructor.call(this, id, sGroup, config);

    this.logger = this.logger || YAHOO;
    var el = this.getDragEl();
    YAHOO.util.Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

    this.goingUp = false;
    this.lastY = 0;
    this.oldIndex = null;
};


YAHOO.extend(NodeSelectorDragAndDropDecorator, YAHOO.util.DDProxy, {

    onMouseDown: function(e){
        var clickEl = this.getEl();
        clickEl._onMouseDown();
    },

    startDrag: function(x, y) {
        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();
        this.oldIndex = clickEl._index;
        YAHOO.util.Dom.setStyle(clickEl, "visibility", "hidden");

        dragEl.innerHTML = clickEl.innerHTML;
        YAHOO.util.Dom.setStyle(dragEl, "color", YAHOO.util.Dom.getStyle(clickEl, "color"));
        YAHOO.util.Dom.setStyle(dragEl, "backgroundColor", YAHOO.util.Dom.getStyle(clickEl, "backgroundColor"));
        YAHOO.util.Dom.setStyle(dragEl, "border", "2px solid #7EA6B2");
    },

    endDrag: function(e) {
        var srcEl = this.getEl();
        if(this.oldIndex != srcEl._index){
            srcEl.context.moveExistingItemBefore(this.oldIndex,srcEl._index);
        }

        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        YAHOO.util.Dom.setStyle(proxy, "visibility", "");
        var a = new YAHOO.util.Motion(
            proxy, {
                points: {
                    to: YAHOO.util.Dom.getXY(srcEl)
                }
            },
            0.2,
            YAHOO.util.Easing.easeOut
        );
        var proxyid = proxy.id;
        var thisid = this.id;

        // Hide the proxy and show the source element when finished with the animation
        a.onComplete.subscribe(function() {
            YAHOO.util.Dom.setStyle(proxyid, "visibility", "hidden");
            YAHOO.util.Dom.setStyle(thisid, "visibility", "");
        });
        a.animate();
    },

    onDragDrop: function(e, id) {
        if (YAHOO.util.DragDropMgr.interactionInfo.drop.length === 1) {

            var pt = YAHOO.util.DragDropMgr.interactionInfo.point;
            var region = YAHOO.util.DragDropMgr.interactionInfo.sourceRegion;

            if(region){
                if (!region.intersect(pt)) {
                    var srcEl = this.getEl();
                    var destEl = YAHOO.util.Dom.get(id);
                    if(!YDom.isAncestor(srcEl.context.id + "-target", destEl)) return;

                    var destDD = YAHOO.util.DragDropMgr.getDDById(id);
                    if(destEl.appendChild){
                        destEl.appendChild(srcEl);
                        destDD.isEmpty = false;
                        YAHOO.util.DragDropMgr.refreshCache();
                    }
                }
            }
        }
    },

    onDrag: function(e) {

        // Keep track of the direction of the drag for use during onDragOver
        var y = YAHOO.util.Event.getPageY(e);

        if (y < this.lastY) {
            this.goingUp = true;
        } else if (y > this.lastY) {
            this.goingUp = false;
        }

        this.lastY = y;
    },

    onDragOver: function(e, id) {
        var srcEl = this.getEl();
        var destEl = YAHOO.util.Dom.get(id);

        if(!destEl) return;

        if(!YDom.isAncestor(srcEl.context.id + "-target", destEl)) return;

        if (destEl.nodeName.toLowerCase() == "div" && YAHOO.util.Dom.hasClass(destEl,"cstudio-form-control-node-selector-item")) {
            var orig_p = srcEl.parentNode;
            var p = destEl.parentNode;
            if (this.goingUp) {
                p.insertBefore(srcEl, destEl); // insert above
            } else {
                p.insertBefore(srcEl, destEl.nextSibling); // insert below
            }

            var srcIndex = srcEl._index;
            srcEl._index = destEl._index;
            destEl._index = srcIndex;

            YAHOO.util.DDM.refreshCache();
        }
    }
});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-node-selector", CStudioForms.Controls.NodeSelector);