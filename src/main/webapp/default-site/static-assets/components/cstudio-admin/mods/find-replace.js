CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/find-replace.css");
CStudioAdminConsole.Tool.FindReplace = CStudioAdminConsole.Tool.FindReplace ||  function(config, el)  {
	this.containerEl = el;
	this.config = config;
	this.types = [];
	return this;
}

/**
 * Overarching class that drives the content type tools
 */
YAHOO.extend(CStudioAdminConsole.Tool.FindReplace, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = 
			"<div id='find-replace-console'  style='position: relative; top: 50px; left: 50%; right: auto;'>" +
			  "<table>" +   
 				"<tr>" +
			      "<td  colspan='2'><div id='findreplacestatus'></div></td>" +
			    "</tr><tr> " + 
			      "<td style='padding: 15px;'>Find</td><td><input id='term' /></td>" +
			    "</tr><tr> " + 
                  "<td style='padding: 15px;'>Replace</td><td><input id='replace' /></td>	" +
                "</tr><tr> " +
                  "<td  style='padding: 15px;' colspan='2'><button id='findreplacego' style='background: none repeat scroll 0% 0% rgb(51, 153, 204); padding: 10px; font-weight: bolder; border: 1px solid black; cursor: pointer; color: white;'>Find and Replace</button></td>" +
                "</tr>"+
              "</table>"+
			"</div>";
			
		var actions = [];

		var termEl = document.getElementById('term');
		var replaceEl = document.getElementById("replace");
		var buttonEl = document.getElementById("findreplacego");

		buttonEl.termEl = termEl;
		buttonEl.replaceEl = replaceEl;
		buttonEl.tool = this;
		buttonEl.onclick = function() {
			this.tool.findAndReplace(this.termEl.value, this.replaceEl.value);
		}
	},

	findAndReplace: function(term, replaceVal) {
		var statusEl = document.getElementById('findreplacestatus');
		var searchContext = CStudioAuthoring.Service.createSearchContext();
		searchContext.contentTypes = [];
		searchContext.keywords = term;

		statusEl.innerHTML = "searching...";

		var callback = {
			success: function(data) {
				// get the results
				var results = data.objectList;
				var resultsCount = results.length;
				var updates = 0;
				// iterate the results
				for(var i=0; i<=resultsCount; i++) {
				     // read the content
				     result = results[i];

				     if(result.item.uri.indexOf(".xml") == -1 
				     && result.item.uri.indexOf(".ftl") == -1) {
				     	// we're not interested in JS, CSS, images etc
				        updates++;
					 	statusEl.innerHTML = "skipping file";
				     }
				     else {
				     	 // process the find and replace
					     var getContentCb = {
					     	success: function(response) {
	 				            // perform the replace
	 				            var item = this.item;
	 				            var content = response.responseText; 
	                            content = content.replace(new RegExp(this.term, 'gi'), this.replaceVal);
 
					     		// write the content
					     		var writeCb = {
					     			success: function() {
					     				// update the status
						 				this.statusEl.innerHTML = 
						 				   "replaced "+ this.term + " with " +
						 				   this.replaceVal;
					     			},
					     			failure: function() {
					     			},

					     			item: this.item,
					     	        statusEl: this.statusEl,
					     	        term: this.term, 
					     	        replaceVal: this.replaceVal

					     		};

					     		CStudioAuthoring.Service.writeContent(
					     			item.uri, 
					     			item.name, 
					     			item.uri, 
					     			content, 
					     			item.contentType, 
					     			CStudioAuthoringContext.site, 
					     			true, 
					     			false, 
					     			false, 
					     			true, 
					     			writeCb);
					     	},
					     	failure: function() {

					     	},

					     	item: result.item,
					     	statusEl: statusEl,
					     	term: term, 
					     	replaceVal: replaceVal
					     };

					     CStudioAuthoring.Service.getContent(result.item.uri, true, getContentCb);
					 }
				}  // end loop    
			},
			failure: function(err) {
				statusEl.innerHTML = "error performing search for find / replace";
			}
		};

		CStudioAuthoring.Service.search(
			CStudioAuthoringContext.site, 
			searchContext, callback); 
	}
});
		
CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-find-replace",CStudioAdminConsole.Tool.FindReplace);