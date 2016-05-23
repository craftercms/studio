var reorderPageMap = {"dirty": "false", "prevPath":"", "nextPath":""};
var reorderPanel = null;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.panelPageNavOrder = CStudioAuthoring.Dialogs.panelPageNavOrder || {

	/**
	 * initialize module
	 */
	initialize: function() {
		
	},

	create: function(id){
		reorderPageMap['dirty'] = false;
		//YAHOO.namespace("cstudio.container");

		reorderPanel = new YAHOO.widget.Panel(id, {
					width : "600px",
					height : "398px",
					fixedcenter : true,
					visible : true,
					close : false,
					underlay : "none",
					draggable : false,
					modal : true,
					constraintoviewport : true,
                    zindex : "1050"
		} );		
		reorderPanel.render();
		reorderPanel.show();

	    //set scroll bar position to set "This Page" element in view
	    var scrlDivObj = YAHOO.util.Dom.get("submitxformscroll");
	    if (scrlDivObj && scrlDivObj.clientHeight < scrlDivObj.scrollHeight) {
	        var sortableObj = YAHOO.util.Dom.get("sortable");
	        var liObj = YAHOO.util.Dom.get("left-drag-handle").parentNode;
	        var thisPageIndex = 0;
	        for (var liIdx=0; liIdx<sortableObj.children.length; liIdx++) {
	                if (sortableObj.children[liIdx].id == liObj.id) {
	                    thisPageIndex = (liIdx + 1);
	                    break;
	                }
	        }
	        var scrollPos = thisPageIndex * 27;
	        if (scrollPos > (scrlDivObj.clientHeight - 18)) {
	            scrlDivObj.scrollTop = scrollPos - 60;
	        }
	    }
	},

	destroy: function(id){
		reorderPanel.destroy();	
	},

	layout: function(config) {
		
	   var panelId = config.id;
	   var panelDiv = document.createElement('div');
	   panelDiv.id = panelId;
       document.body.appendChild(panelDiv);
	   
	   var reorderWrapperDiv = document.createElement('div');
       reorderWrapperDiv.id = "reorderWrapper";
        reorderWrapperDiv.className = "reorderWrapperDialog";
       YAHOO.util.Dom.get(panelId).appendChild(reorderWrapperDiv);

       var reorderContainerDiv = document.createElement('div');
       reorderContainerDiv.id = "reorderContainer";
       YAHOO.util.Dom.get('reorderWrapper').appendChild(reorderContainerDiv);

       var reorderHeadertextSpan = document.createElement('span');
       reorderHeadertextSpan.className = "reorderHeadertext";
       var txt = document.createTextNode("Edit Navigation Order");
       reorderHeadertextSpan.appendChild(txt);
       YAHOO.util.Dom.get('reorderContainer').appendChild(reorderHeadertextSpan);

       var reorderSubtextDiv = document.createElement('div');
       reorderSubtextDiv.className = "reorderSubtext";
       txt = document.createTextNode('Drag and Drop "This Page" to the desired location in the navigation structure.');
       reorderSubtextDiv.appendChild(txt);
       YAHOO.util.Dom.get('reorderContainer').appendChild(reorderSubtextDiv);

       var width100Div = document.createElement('div');
       width100Div.className = "width100";
       YAHOO.util.Dom.get('reorderContainer').appendChild(width100Div);

       var clearDiv = document.createElement('div');
       clearDiv.className = "clear";
       YAHOO.util.Dom.get('reorderContainer').appendChild(clearDiv);

       var submitxformscrollDiv = document.createElement('div');
       submitxformscrollDiv.id = "submitxformscroll";
       YAHOO.util.Dom.get('reorderContainer').appendChild(submitxformscrollDiv);	
	
	},

	// create the panel content for drag and drop
	content: function(panelId, orderJson, config){

       var CMgs = CStudioAuthoring.Messages;
       var langBundle = CMgs.getBundle("forms", CStudioAuthoringContext.lang);

	   var query = location.search.substring(1);
       // create object with thisPage
       var thisPageObject = {};
       thisPageObject.id = CStudioAuthoring.Utils.getQueryVariable (query, 'path');
       if(config.control.constructedPath && config.control.constructedPath !="") {
         thisPageObject.id = config.control.constructedPath;
       }
       thisPageObject.order = config.control.orderValue;
       thisPageObject.internalName = CMgs.format(langBundle, "currentPage");

       var orderLen = orderJson.order.length - 1;
       var orderToPageMap = {};
       var orderArray = new Array();
       var orderNum;
       for (var i=0; i<=orderLen; ++i) {
    	 var orderDetails = {};
    	 orderDetails.id = orderJson.order[i].id;
    	 orderDetails.internalName = orderJson.order[i].internalName ? orderJson.order[i].internalName : orderJson.order[i].name;
    	 if (orderJson.order[i].disabled && orderJson.order[i].disabled == 'true') {
    		 orderDetails.disabled = true;
    	 } else {
    		 orderDetails.disabled = false;
    	 }

    	 // Check if this is the current page. Check if this page is ordered and has an unsaved order value
		 // Take unsaved order value if present else take the order value from the server.
		 if(thisPageObject.id == orderDetails.id) {
			if(thisPageObject.order) 
			  orderNum = parseFloat(thisPageObject.order);
			else 
			  orderNum = parseFloat(orderJson.order[i].order);
		 } else {
			orderNum = parseFloat(orderJson.order[i].order);
		 }

		 // Added ~ to create a unique key. Order could be same for a new page
         orderToPageMap[orderNum+"~"+orderDetails.id] = orderDetails;
         orderArray[i] = orderNum+"~"+orderDetails.id;
         delete orderDetails;
       }

       orderArray.sort(function(a,b) {
            var order1 = a.split("~")[0];
            var order2 = b.split("~")[0];            
			return order1 - order2;
		});

	   var sortableDiv = document.createElement('ul');
       sortableDiv.id = "sortable";
       sortableDiv.className = "ui-sortable";
       YAHOO.util.Dom.get('submitxformscroll').appendChild(sortableDiv);

       var orderArrayLen = orderArray.length;
       var sortable = YAHOO.util.Dom.get("sortable");
       var listItem;
       var currentItem = sortable;
       // set up panel with body being the ordered page list
       var draggableItemId;
       for (var j=0; j<orderArrayLen; ++j) {
          var orderNumber = orderArray[j];
          var li = document.createElement('li');
          li.id = 'li1_' + eval(j+1);
          var txt;

          if (thisPageObject.id == orderToPageMap[orderNumber].id) {  //if this page then change text on element
             txt = document.createTextNode(orderToPageMap[orderNumber].internalName);
             li.style.color = '#006699';
		     li.style.backgroundColor = "#D5E6F2";
		     draggableItemId = li.id;

		     var arrowDiv = document.createElement("div");
		     arrowDiv.id = "left-drag-handle";
		     YAHOO.util.Dom.setStyle(arrowDiv, "cursor", "move");
		     li.appendChild(arrowDiv);
          } else {
             txt = document.createTextNode(orderToPageMap[orderNumber].internalName);
          }

  		  if (orderToPageMap[orderNumber].disabled) {
		      li.style.textDecoration = 'line-through';
		  }
          li.style.textAlign = "left";
             li.className = 'ui-state-default';
	      li.appendChild(txt);
	      sortableDiv.appendChild(li);
       }

	     // initialize the prev path and next path elements
       reorderPageMap['prevPath'] = null;
       reorderPageMap['nextPath'] = 'li1_' + orderToPageMap[orderArray[0]].id;       

       var dndSubmitButtonsDiv = document.createElement('div');
       dndSubmitButtonsDiv.id = 'reorderButtonWrapper';
       dndSubmitButtonsDiv.className = 'reorderSubmitWrapperButton';
       YAHOO.util.Dom.get('reorderWrapper').appendChild(dndSubmitButtonsDiv);

       var dndOKButtonDiv = document.createElement('input'); 
       dndOKButtonDiv.id = 'dndOKButton';
       dndOKButtonDiv.className = 'cstudio-xform-button btn btn-primary';
       dndOKButtonDiv.setAttribute('type', 'submit');
       dndOKButtonDiv.setAttribute('value', 'OK');
       YAHOO.util.Dom.get('reorderButtonWrapper').appendChild(dndOKButtonDiv);

       var dndCancelButtonDiv = document.createElement('input');
       dndCancelButtonDiv.id = 'dndCancelButton';       
       dndCancelButtonDiv.className = 'cstudio-xform-button btn btn-default';
       dndCancelButtonDiv.style.marginLeft = "15px";
       dndCancelButtonDiv.setAttribute('type', 'submit');
       dndCancelButtonDiv.setAttribute('value', 'Cancel');
       YAHOO.util.Dom.get('reorderButtonWrapper').appendChild(dndCancelButtonDiv);
		
		if(config.control.readonly != true){
        	new YAHOO.util.DDTarget("sortable");
	    	for (j=0;j<orderArrayLen;++j) { // set Dnd targets
          		new CStudioForms.DDList("li1_" +  eval(j+1), draggableItemId);  // set item to be dragged
        	}
		}else{
			dndOKButtonDiv.style.visibility = "hidden"; //Hidden the ok button
		}
		
	   	
		YAHOO.util.Event.addListener("dndOKButton", "click", function(){onDnDsubmitClick()});
	   	YAHOO.util.Event.addListener("dndCancelButton", "click", function(){onDnDCancelClick()});

	   var onDnDCancelClick = function () {
		  CStudioAuthoring.Dialogs.panelPageNavOrder.destroy(this.panelId);
	   };

	   var onDnDsubmitClick = function () {

			if (! reorderPageMap['dirty'] ||
				(orderJson.order && orderJson.order.length == 1)) { // only page exists in the given level
				CStudioAuthoring.Dialogs.panelPageNavOrder.destroy(this.panelId);
				reorderPageMap['dirty'] = false;
			        return;
			}

		   var prevPath = reorderPageMap['prevPath'];
		   if (prevPath) { //prev path not null
			  // remove li1_ from prevPath and nextPath
			  prevPathIdx = prevPath.indexOf ("li1_");
			  prevPath = prevPath.substring(prevPathIdx+4);           	
		   }

		   var nextPath = reorderPageMap['nextPath'];
		   if (nextPath) { //next path not null       
			  nextPathIdx = nextPath.indexOf ("li1_");
			  nextPath = nextPath.substring(nextPathIdx+4);
		   }      
		
		   // build part of query string with prevPath and nextPath
		   var pathString;
		   if (!prevPath) {
			  pathString = '&after=' + orderJson.order[nextPath-1].id;
		   } else if (!nextPath) {
			  pathString = '&before=' + orderJson.order[prevPath-1].id;
		   } else { // both are not null
			 pathString = '&before=' + orderJson.order[prevPath-1].id + '&after=' + orderJson.order[nextPath-1].id;
		   }

		   pathStringFinal = thisPageObject.id + pathString;

		   order = 'default';

		   var submitCallback = {
				success: function(contentTypes) {
					if(contentTypes.orderValue) {
						this.config.control.orderValue = contentTypes.orderValue;
						this.config.control._onChange(null, this.config.control);
						CStudioAuthoring.Dialogs.panelPageNavOrder.destroy(this.panelId);
					}
				},

				failure: function() {
					CStudioAuthoring.Dialogs.panelPageNavOrder.destroy(this.panelId);
				}
			}; // end of callback
			submitCallback.config = config;

			CStudioAuthoring.Service.reorderServiceRequest(CStudioAuthoringContext.site, pathStringFinal,order,submitCallback);

	   };
	   onDnDsubmitClick.orderJson = orderJson;
       }
};

CStudioForms.DDList = function(id, draggableItemId, sGroup, config) {
    CStudioForms.DDList.superclass.constructor.call(this, id, sGroup, config);

    this.logger = this.logger || YAHOO;
    var el = this.getDragEl();
    YAHOO.util.Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

    this.goingUp = false;
    this.lastY = 0;
    this.setHandleElId(draggableItemId);
};

YAHOO.extend(CStudioForms.DDList, YAHOO.util.DDProxy, {
	
    startDrag: function(x, y) {
        this.logger.log(this.id + " startDrag");

        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();

        dragEl.innerHTML = clickEl.innerHTML;

        YAHOO.util.Dom.setStyle(dragEl, "color", YAHOO.util.Dom.getStyle(clickEl, "color"));
        YAHOO.util.Dom.setStyle(dragEl, "backgroundColor", YAHOO.util.Dom.getStyle(clickEl, "backgroundColor"));
        YAHOO.util.Dom.setStyle(dragEl, "border", "2px solid #7EA6B2");
    },

    endDrag: function(e) {
    	reorderPageMap['dirty'] = true;
    	var srcEl = this.getEl();
        var prevSibling = YAHOO.util.Dom.getPreviousSibling(srcEl);

        if (!prevSibling)
        	reorderPageMap['prevPath'] = null;
        else
        	reorderPageMap['prevPath'] = YAHOO.util.Dom.getPreviousSibling(srcEl).id;

        var nextSibling = YAHOO.util.Dom.getNextSibling(srcEl);
        if (!nextSibling)
        	reorderPageMap['nextPath'] = null;
        else
        	reorderPageMap['nextPath'] = YAHOO.util.Dom.getNextSibling(srcEl).id;
        
        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        var a = new YAHOO.util.Motion( 
            proxy, { 
                points: { 
                    to: YAHOO.util.Dom.getXY(srcEl)
                }
            }, 
            0.2, 
            YAHOO.util.Easing.easeOut 
        )
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

        // If there is one drop interaction, the li was dropped either on the list,
        // or it was dropped on the current location of the source element.
        if (YAHOO.util.DragDropMgr.interactionInfo.drop.length === 1) {

            // The position of the cursor at the time of the drop (YAHOO.util.Point)
            var pt = YAHOO.util.DragDropMgr.interactionInfo.point; 

            // The region occupied by the source element at the time of the drop
            var region = YAHOO.util.DragDropMgr.interactionInfo.sourceRegion; 

            // Check to see if we are over the source element's location.  We will
            // append to the bottom of the list once we are sure it was a drop in
            // the negative space (the area of the list without any list items)
            if (!region.intersect(pt)) {
                var destEl = YAHOO.util.Dom.get(id);
                var destDD = YAHOO.util.DragDropMgr.getDDById(id);
                if(destEl.appendChild){
					destEl.appendChild(this.getEl());
					destDD.isEmpty = false;
					YAHOO.util.DragDropMgr.refreshCache();
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
		
		for(var i =0; i< destEl.length ; ++i){
			if(destEl[i].id.indexOf("li") != -1){
				destEl = YAHOO.util.Dom.get(destEl[i].id);
			}
		}
			
	    if(destEl.nodeName){
			if (destEl.nodeName.toLowerCase() == "li") {
				var orig_p = srcEl.parentNode;
				var p = destEl.parentNode;
				
				if (this.goingUp) {
					p.insertBefore(srcEl, destEl); // insert above
				} else {
					p.insertBefore(srcEl, destEl.nextSibling); // insert below
				}
		
				YAHOO.util.DragDropMgr.refreshCache();
			}
		}
    }

}); 

CStudioAuthoring.Module.moduleLoaded("dialog-nav-order", CStudioAuthoring.Dialogs.panelPageNavOrder);