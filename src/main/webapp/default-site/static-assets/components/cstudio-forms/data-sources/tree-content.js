CStudioForms.Datasources.TreeContent= CStudioForms.Datasources.TreeContent ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
   	
   	for(var i=0; i<properties.length; i++) {
   		if(properties[i].name == "taxonomyName") {
 			this.taxonomyName = properties[i].value;
   		}
   	} 
	
	return this;
}

var urlPrefix = CStudioAuthoringContext.authoringAppBaseUri + 
					"/proxy/alfresco/slingshot/doclib/categorynode/node/alfresco/category/root";
var urlSuffix = "?perms=false&children=true";
var currentItem = "";
var expandedElement = "";

YAHOO.extend(CStudioForms.Datasources.TreeContent, CStudioForms.CStudioFormDatasource, {

	add: function(control) {
		var _self = this;
		control.parentControl = this;

		var addContainerEl = null;

		if(control.addContainerEl) {
			control.addButtonEl.value = "Add";
			addContainerEl = control.addContainerEl;
			control.addContainerEl = null;
			control.containerEl.removeChild(addContainerEl);
		} else {
			control.addButtonEl.value = "Close";
			var addContainerEl = document.createElement("div");
	        control.containerEl.appendChild(addContainerEl);
	        YAHOO.util.Dom.addClass(addContainerEl, 'cstudio-form-control-node-selector-add-container');
	        control.addContainerEl = addContainerEl;
	
			addContainerEl.style.left = ( control.addButtonEl.offsetLeft - 170 ) + "px";
	       	addContainerEl.style.top = control.addButtonEl.offsetTop + 27 + "px";
	
			var initTree = {
				success:function(response) {
					var parentArray = new Array(this.taxonomyName);
	
					var copyTree = eval("(" + response.responseText + ")");
					this.control.parentControl.constructTree(this.addContainerEl, copyTree, this.control, parentArray);
		    	},
	
				failure:function() {
					alert('failure');
				}
			};
	
			initTree.control = control;
			initTree.taxonomyName = this.taxonomyName;
			initTree.addContainerEl = addContainerEl;
	
			var treeUrl = urlPrefix	+ "/" + this.taxonomyName + urlSuffix;
			YConnect.asyncRequest('GET', treeUrl, initTree);
		}
	},

	constructTree: function(treeContainerDiv, copyTree, control, parentArray) {
		if (copyTree && copyTree.totalResults && copyTree.totalResults > 0) {
	    	var ulEl = document.createElement("ul");
			YAHOO.util.Dom.addClass(ulEl, 'treeUlContainerClass');
	    	ulEl.style.border = "1px solid #000000";
	    	ulEl.style.zIndex = "3";
	   		ulEl.style.width = "175px";
	    	ulEl.style.padding = "0px";
	    	ulEl.style.backgroundColor = "#fff";
	   		ulEl.style.marginTop = "-6px";
	   		ulEl.style.marginLeft = "-6px";
	   		ulEl.style.position = "absolute";
	   		ulEl.style.left = "175px";
	   		ulEl.style.top = "0px";

			for (var i=0; i< copyTree.totalResults;i++) {
				var pArray = parentArray.slice();
				child = copyTree.items[i];
	
	   			var liEl = document.createElement("li");
	   			liEl.style.paddingLeft = "15px";
	   			liEl.style.paddingRight = "15px";
	   			liEl.style.paddingTop = "10px";
	   			liEl.style.paddingLeft = "10px";
	   			liEl.style.position = "relative";

				var labelEl = document.createElement("div");
	   		 	labelEl.setAttribute("value", child.nodeRef);
	   		 	labelEl.style.width = "175px";
	   		 	labelEl.innerHTML = child.name;
	   		 	labelEl.taxonomyName = this.taxonomyName;
	   		 	labelEl.liEl = liEl;
	   		 	labelEl.control = control;
	   		 	labelEl.parentArray = pArray;
	   		 	liEl.appendChild(labelEl);

	   		 	YAHOO.util.Event.addListener(labelEl, 'mouseover', control.parentControl.expandTree);
	   		 	YAHOO.util.Event.addListener(labelEl, 'mouseout', control.parentControl.collapseTree);
	   		 	YAHOO.util.Event.addListener(labelEl, 'click', control.parentControl.addItem);

	   		 	liEl.setAttribute( 'class', 'li-normal' );

	   		 	ulEl.appendChild(liEl);
			}

			treeContainerDiv.appendChild(ulEl);
		}
	},

	expandTree: function() {
		if (currentItem != this.innerHTML) {
			var wait = 500;
			var date = new Date();
			var curDate = null;

			do { 
			    curDate = new Date();
			} while (curDate-date < wait);

			if (this.liEl.lastChild && YAHOO.util.Dom.hasClass(this.liEl.lastChild, 'treeUlContainerClass')) {
				this.liEl.removeChild(this.liEl.lastChild);
				this.parentArray.pop();
			}

			if (expandedElement != "" && this.parentArray.indexOf(expandedElement) == -1) {
				var elements = YAHOO.util.Dom.getElementsByClassName('treeUlContainerClass');
				if (elements && elements.length > 0) {
					for (var i=1; i< elements.length; i++) {
						var root = elements[i].parentElement;
						root.removeChild(elements[i]);
					}
				}
			}
			expandedElement = "";

			var initTree = {
				success:function(response) {
					var copyTree = eval("(" + response.responseText + ")");
					if (this.parentArray.indexOf(this.currentItem) == -1)
						this.parentArray.push(this.currentItem);
					this.control.parentControl.constructTree(this.containerDiv, copyTree, this.control, this.parentArray);
		    	},

				failure:function() {
					alert('failure');
				}
			};

			initTree.control = this.control;
			initTree.taxonomyName = this.taxonomyName;
			initTree.containerDiv = this.liEl;
			initTree.parentArray = this.parentArray;
			initTree.currentItem = this.innerHTML;

			// use parent array and construct the url
			var url = "";
			for (var i=0; i<this.parentArray.length; i++) {
				if (this.parentArray[i] != this.innerHTML) 
					url += "/" + this.parentArray[i];
			}

			url += "/" + this.innerHTML;

			var treeUrl = urlPrefix	+ url + urlSuffix;
			YConnect.asyncRequest('GET', treeUrl, initTree);
		}
	},

	collapseTree: function() {
		expandedElement = this.innerHTML;
	},

	addItem: function() {
		this.control.insertItem(this.attributes[0].value, this.innerHTML);
	},

    getLabel: function() {
        return CMgs.format(langBundle, "treeContent");
    },

   	getInterface: function() {
   		return "item";
   	},

	getName: function() {
		return "tree-content";
	},

	getSupportedProperties: function() {
		return [
			{ label: CMgs.format(langBundle, "taxonomyName"), name: "taxonomyName", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-tree-content", CStudioForms.Datasources.TreeContent);