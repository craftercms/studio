
CStudioAdminConsole.Tool.SocialMention = CStudioAdminConsole.Tool.SocialMention ||  function(config, el)  {
	//<tool><name>socialmention</name><label>Social Mention: Nike</label><query><title>Nike</title><terms>nike</terms><types><type>microblogs</type><type>news</type></types><sources><source>facebook</source><source>twitter</source></sources></query></tool>

	this.config = config;
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.SocialMention, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		workareaEl.innerHTML = "<div class='cs-socialmention-spinner' ><img src='" + CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/wait.gif" + "'></div>";
		
		var cssEl = document.createElement("link");
		cssEl.setAttribute("type", "text/css");
		cssEl.setAttribute("rel", "stylesheet");
		cssEl.setAttribute("href", CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/components/cstudio-admin/mods/socialmention/plugin.css");
		document.head.appendChild(cssEl);
		
		CStudioAdminConsole.Tool.SocialMentionRender.query = this.config.query;
		
		this.makeUpdateRequest(CStudioAdminConsole.Tool.SocialMentionRender.query);
	},
	
	makeUpdateRequest: function(query) {

		var url = "http://socialmention.com/search?q="+query.terms+"&f=json";
		
		if(query.types) {
			for(var i=0; i<query.types.length; i++) {
				var type = query.types[i];
				url += "&t[]=" + type;
			}
		}
				
		if(query.sources) {
			for(var j=0; i<query.sources.length; j++) {
				var source = query.source[j];
				url += "&src[]=" + source;
			}
		}
						
		url += "&callback=CStudioAdminConsole.Tool.SocialMentionRender.update";
		
		var script = document.createElement("script");
		script.setAttribute("src", url);
		document.head.appendChild(script);
	}
});

CStudioAdminConsole.Tool.SocialMentionRender = CStudioAdminConsole.Tool.SocialMentionRender ||  {
	update: function(data) {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		var html = 
			"<div class='cs-socialmention-query'>" +
				"<div class='cs-socialmention-query-lastupdate'>"+new Date()+"</div>" +
				"<div class='cs-socialmention-query-heading'>"+CStudioAdminConsole.Tool.SocialMentionRender.query.title+"</div>" +
				"<div class='cs-socialmention-query-count'>Result count: "+data.count+"</div>" +
			"</div>";

		html += 
			"<div class='cs-socialmention-results'>" +
				"<ul class='cs-socialmention-results-list'>";
			
				for(var i=0; i<data.items.length; i++) {
					var item = data.items[i];
					
					html += 
					"<li class='cs-socialmention-result-item'>"+ 
						"<div class='cs-socialmention-result'>" +
							"<div class='cs-socialmention-result-time'>" + this.dateFormat(new Date(item.timestamp*1000)) + "</div>" +
							"<div class='cs-socialmention-result-source cs-socialmention-result-source-"+ item.source + "' ></div>" +
							"<div class='cs-socialmention-result-type'>" + item.type + "</div>" +
							
							"<div class='cs-socialmention-result-title'><a href='" +  item.link + "'>" + item.title + "</a></div>" +					
							"<div class='cs-socialmention-result-description'>" + item.description + "</div>" +
							"<div class='cs-socialmention-result-image'><a href='" +  item.link + "'><img src='"+ item.image + "' /></a></div>" +
						"</div>" +
					"</li>";
				}
			
			html += "</ul></div>";
			
			workareaEl.innerHTML = html;
	},
	
	dateFormat: function(d) {
		var curr_date = d.getDate();
		var curr_month = d.getMonth();
		var curr_year = d.getFullYear();

		return (curr_month + "-" + curr_date + "-" + curr_year);
	}
}

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-socialmention",CStudioAdminConsole.Tool.SocialMention);