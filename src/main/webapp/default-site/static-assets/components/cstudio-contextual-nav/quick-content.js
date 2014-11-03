/**
 * Quick Content
 */
CStudioAuthoring.ContextualNav.QuickContentMod = CStudioAuthoring.ContextualNav.QuickContentMod || {

	initialized: false,
	openState: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.render();
	},
				
	render: function() {
		var el, containerEl, imageEl, ptoOn;

		el = YDom.get("acn-quick-article");	
		containerEl = document.createElement("div");
		containerEl.id = "acn-quick-content-button";

					
		el.appendChild(containerEl);
		containerEl.innerHTML = "<div id='acn-qc-wrapper' class='acn-dropdown-wrapper'>" +
								"<div class='acn-qc-inner' id='acn-dropdown-inner' style='width:60px; padding-left: 30px; background: url(/proxy/authoring/themes/cstudioTheme/images/icons/icon_strip_vertical.gif) no-repeat scroll 70px -43px rgba(0, 0, 0, 0); margin: 6px;'>" +
			                      "<a class='acn-qc-toggler acn-drop-arrow' href='#' id='acn-qc-toggler'>New</a>" +
		                        "</div>" +
		                        "<div id='acn-qc-dropdown' style='background: none repeat scroll 0 0 white; "+
		                                    "border: 1px solid black; color: #0176b1; display: none; "+
		                                    "font-weight: bold; list-style: none outside none; position: relative; "+
		                                    "text-align: start; top: 4px; width: 100px;'>"+
          "<ul style='list-style:none;'>" +
             "<li style='cursor:pointer; margin:10px; padding: 2px;' id='qc-article'>Article</li>"+
             "<li style='cursor:pointer; margin:10px; padding: 2px;'  id='qc-movie'>Movie</li>" +
          "</ul>"+
        "</div>"+
        "</div>";

        var buttonEl = document.getElementById("acn-qc-wrapper");
        buttonEl.control = this;
        buttonEl.onclick = function() { this.control.toggle(); }

        var qcMovieEl = document.getElementById("qc-movie");
        qcMovieEl.control = this;
        qcMovieEl.onclick = function() { this.control.newContent("/components/movies", "/site/quick-content/movie"); }

        var qcArticleEl = document.getElementById("qc-article");
        qcArticleEl.control = this;
        qcArticleEl.onclick = function() { this.control.newContent("/components/article", "/site/quick-content/article"); }

	},

	newContent: function(contentType, path) {
		var formSaveCb = {
			success: function() {
				document.location = document.location;
			}
		};

		CStudioAuthoring.Operations.openContentWebForm(
			contentType,
			null,
			null,
			path,
			false,
			true,
			formSaveCb,
			[]);
	},

	toggle: function() {

		var dropdownEl = document.getElementById("acn-qc-dropdown");
		var buttonEl = document.getElementById("acn-qc-wrapper");

		if(this.openState == true) {
			this.openState = false;
			dropdownEl.style.display = "none";
			buttonEl.style.backgroundColor = "transparent";
		}
		else {
			{
			this.openState = true;
			dropdownEl.style.display = "inline-block";
			buttonEl.style.backgroundColor = "#f0f0f0";
		}
		}

	}
}

CStudioAuthoring.Module.moduleLoaded("quick-content", CStudioAuthoring.ContextualNav.QuickContentMod);
