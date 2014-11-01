/**
 * Order Taxonomy Dialogs
 *
 * @author Russ Danner
 * @email Russ Danner
 */
CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};


CStudioAuthoring.Dialogs.DialogOrderTaxonomy = CStudioAuthoring.Dialogs.DialogOrderTaxonomy || {

    /**
     * initialize module
     */
    initialize: function(config) {

    },

    /**
     * show dialog
     */
    showDialog: function(site, modelName, level, orderedCb) {
        this._self = this;
        this.dialog = this.createDialog(site, modelName, level, orderedCb);
        this.dialog.show();
    },

    closeDialog:function() {
        this.dialog.hide();
        var element = YDom.get("cstudio-wcm-popup-div");
        
        if(element) {
            // need to work on this... because we make many calls this call back gets called over and over
            // best option is to make 1 call.
            element.parentNode.removeChild(element);
        }
    },

    cancelDialogClick: function(event, matchedEl) {
        CStudioAuthoring.Dialogs.DialogOrderTaxonomy.closeDialog();
    },

    okDialogClick: function(event, matchedEl) {
        var modelName = CStudioAuthoring.Dialogs.DialogOrderTaxonomy.modelName;
        var dialog = CStudioAuthoring.Dialogs.DialogOrderTaxonomy;
        var itemContainerEl = document.getElementById("orderTaxonomyItems");
        var itemEls = itemContainerEl.getElementsByTagName("li");
        var itemElsLen = itemEls.length;
        var changeSet = [];
        
        
        /* determine change set and new values */
        for(var i=0; i<itemElsLen; i++) {
            changeSet[changeSet.length] = {
                deleted: itemEls[i].data.deleted,
                description: itemEls[i].data.description,
                id: itemEls[i].data.id,
                label: itemEls[i].data.label,
                order: i*1000,
                type: itemEls[i].data.type, 
                created: false, 
                updated: true,
                nodeRef: itemEls[i].data.value,
                name: itemEls[i].data.label,
                value: itemEls[i].data.value,
                children: []
            };
        }

       
        var updateCb = {
            success: function() {
                this.dialog.closeDialog();
                if(CStudioAuthoring.Dialogs.DialogOrderTaxonomy.orderedCb) {
                    CStudioAuthoring.Dialogs.DialogOrderTaxonomy.orderedCb.success();
                    CStudioAuthoring.Dialogs.DialogOrderTaxonomy.orderedCb = null;
                }
            },
            
            failure: function() {
                alert("update taxonomy failure");
            },
            
            dialog: dialog
        };

       // for(var j=0; j<changeSet.length; j++) {
        
            CStudioAuthoring.Service.updateTaxonomies(CStudioAuthoringContext.site, { taxonomies:  changeSet }, updateCb);
        //} 
    },
    
    /**
     * this function should go away -- sort in the service 
     */
    sort: function(treeData) {
        var modelData = treeData.modelData;
        
        for(var i=0; i<modelData.length; i++) {
            for(var j=0; j<modelData.length; j++) {
                if(modelData[i].order < modelData[j].order) {
                    var hold = modelData[i];
                    modelData[i] = modelData[j];
                    modelData[j] = hold;
                }
            }            
        }
        
        treeData.modelData = modelData;
        
        return treeData;
    },
    
    createDialog: function(site, modelName, level, orderedCb) {
        CStudioAuthoring.Dialogs.DialogOrderTaxonomy.modelName = modelName;
        CStudioAuthoring.Dialogs.DialogOrderTaxonomy.orderedCb = orderedCb;
        
        var context = CStudioAuthoring.Dialogs.DialogOrderTaxonomy.context;
        var flatMap = {};
        YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");
        var newdiv = document.createElement("div");
        var divIdName = "cstudio-wcm-popup-div";
        newdiv.setAttribute("id", divIdName);
        newdiv.className = "yui-pe-content";
        newdiv.innerHTML = '<div class="contentTypePopupInner" id="contentTypePopupInner">' +
                           '<div class="contentTypePopupContent" id="contentTypePopupContent"> ' +
                           '<div class="contentTypePopupHeader">Order Taxonomy</div> ' +
                           '<div>Drag items to order them</div> ' +
                           '<div class="copy-content-container">' +
                           '<div style="position:relative;">' +
                           '<div id="orderTaxonomyItems"></div>' +
                           '</div>' +
                           '</div>' +
                           '<div class="contentTypePopupBtn"> ' +
                                '<input type="submit" class="cstudio-xform-button ok" id="otOkButton" value="OK" />' +
                                '<input type="submit" class="cstudio-xform-button" id="otCancelButton" value="Cancel" />' +
                           '</div> ' +
                           '</div> ' +
                           '</div>';

        document.body.appendChild(newdiv);
        
        var dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div",
        {   width : "610px",
            height : "443px",
            fixedcenter : true,
            visible : false,
            modal:true,
            close:false,
            constraintoviewport : true,
            underlay:"none"
        });

        // Render the Dialog
        dialog.setBody("bd");
        dialog.render();

        dialog.cfg.subscribe("configChanged", function (p_sType, p_aArgs) {
            var aProperty = p_aArgs[0],
                    sPropertyName = aProperty[0],
                    oPropertyValue = aProperty[1];
            if (sPropertyName == 'zindex') {
                var siteContextNavZIndex = 100;
                YDom.get("cstudio-wcm-popup-div").parentNode.style.zIndex = oPropertyValue + siteContextNavZIndex;
            }
        });   
          
        dataCb = {
            success: function(treeData) {
            
                treeData = CStudioAuthoring.Dialogs.DialogOrderTaxonomy.sort(treeData); // shouldn't have to do this
                
                var modelDataLen = treeData.modelData.length;
                var ordeItemsEl = document.getElementById("orderTaxonomyItems");
                var ulEl = document.createElement("ul");
                ulEl.id = "orderTaxonomyItemsList";
                ordeItemsEl.appendChild(ulEl);
                
                var ddTarget = new YAHOO.util.DDTarget("orderTaxonomyItemsList");
                
                for(var i=0; i<modelDataLen; i++) {
                    var dataItem = treeData.modelData[i];
                    var liEl = document.createElement("li");
                    liEl.id = "otl-"+dataItem.id;
                    YDom.addClass(liEl, "draglist");
                    
                    liEl.innerHTML = dataItem.label;
                    liEl.type = dataItem.type;
                    liEl.name = dataItem.value;
                    liEl.data = dataItem;
                    
                    ulEl.appendChild(liEl);
                    var ddItem = new CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDList("otl-"+dataItem.id);
                }                
            },
            
            failure: function() {
            },
            
            _self: this
        };
        
        CStudioAuthoring.Service.getTaxonomy(
            site, 
            modelName,
            level, 
            false, 
            "item", 
            dataCb);
            
        
        YEvent.addListener("otCancelButton", "click", 
            CStudioAuthoring.Dialogs.DialogOrderTaxonomy.cancelDialogClick);

        YEvent.addListener("otOkButton", "click", 
            CStudioAuthoring.Dialogs.DialogOrderTaxonomy.okDialogClick);
                    
        return dialog;

    }
};

CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDListClass = {

    startDrag: function(x, y) {

        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();
        YDom.setStyle(clickEl, "visibility", "hidden");

        dragEl.innerHTML = clickEl.innerHTML;

        YDom.setStyle(dragEl, "color", YDom.getStyle(clickEl, "color"));
        YDom.setStyle(dragEl, "backgroundColor", YDom.getStyle(clickEl, "backgroundColor"));
        YDom.setStyle(dragEl, "border", "2px solid gray");
    },

    endDrag: function(e) {

        var srcEl = this.getEl();
        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        YDom.setStyle(proxy, "visibility", "");
        var a = new YAHOO.util.Motion( 
            proxy, { 
                points: { 
                    to: YDom.getXY(srcEl)
                }
            }, 
            0.2, 
            YAHOO.util.Easing.easeOut 
        )
        var proxyid = proxy.id;
        var thisid = this.id;

        // Hide the proxy and show the source element when finished with the animation
        a.onComplete.subscribe(function() {
                YDom.setStyle(proxyid, "visibility", "hidden");
                YDom.setStyle(thisid, "visibility", "");
            });
        a.animate();
    },

    onDragDrop: function(e, id) {
        var DDM = YAHOO.util.DragDropMgr;
        
        // If there is one drop interaction, the li was dropped either on the list,
        // or it was dropped on the current location of the source element.
        if (DDM.interactionInfo.drop.length === 1) {

            // The position of the cursor at the time of the drop (YAHOO.util.Point)
            var pt = DDM.interactionInfo.point; 

            // The region occupied by the source element at the time of the drop
            var region = DDM.interactionInfo.sourceRegion; 

            // Check to see if we are over the source element's location.  We will
            // append to the bottom of the list once we are sure it was a drop in
            // the negative space (the area of the list without any list items)
            if (!region.intersect(pt)) {
                var destEl = YDom.get(id);
                var destDD = DDM.getDDById(id);
                destEl.appendChild(this.getEl());
                destDD.isEmpty = false;
                DDM.refreshCache();
            }
        }

        //alert(this.id);
    },

    onDrag: function(e) {

        // Keep track of the direction of the drag for use during onDragOver
        var y = YEvent.getPageY(e);

        if (y < this.lastY) {
            this.goingUp = true;
        } else if (y > this.lastY) {
            this.goingUp = false;
        }

        this.lastY = y;
    },

    onDragOver: function(e, id) {
        var DDM = YAHOO.util.DragDropMgr;
        var srcEl = this.getEl();
        var destEl = YDom.get(id);

        // We are only concerned with list items, we ignore the dragover
        // notifications for the list.
        if (destEl.nodeName.toLowerCase() == "li") {
            var orig_p = srcEl.parentNode;
            var p = destEl.parentNode;

            if (this.goingUp) {
                p.insertBefore(srcEl, destEl); // insert above
            } else {
                p.insertBefore(srcEl, destEl.nextSibling); // insert below
            }

            DDM.refreshCache();
        }
    }
};

CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDList = function(id, sGroup, config) { 

    CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDList.superclass.constructor.call(
        this, 
        id, 
        sGroup, 
        config); 

    var el = this.getDragEl(); 
    YDom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent 

    this.goingUp = false; 
    this.lastY = 0; 
};

YAHOO.extend(
    CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDList, 
    YAHOO.util.DDProxy, 
    CStudioAuthoring.Dialogs.DialogOrderTaxonomy.DDListClass); 


CStudioAuthoring.Module.moduleLoaded("dialog-order-taxonomy", CStudioAuthoring.Dialogs.DialogOrderTaxonomy);
