   		var jsonString = 	'{'
			+ '"searchType": "cstudio-rdy:readinessArticle",'
			+ '"nodeName": "document", 	'
			+ '"keyword": "XenServer Storage", 	'
			+ '"page": "1", 	'
			+ '"sortBy": "Title", 	'
			+ '"sortAscending": "false", 	'
			+ '"pageSize": "20", 	'
			+ '"filters": 	['
//			+ '		{"qname": "cstudio-pdl:downloadType", "value": "workspace://SpacesStore/2d623952-7ca0-4f16-b1c6-a7b4349f234d"} '
			+ '		],'
			+ '"columns": 	['
			+ '		{"title": "Content", "qname": "cm:content", "searchable": "false"}, '
			+ '		{"title": "Title", "qname": "cstudio-core:title", "searchable": "true"}, '
			+ '		{"title": "Description", "qname": "cstudio-core:description", "searchable": "true"}, '
			+ '		{"title": "modifier", "qname": "cm:modifier", "searchable": "false"}, '
			+ '		{"title": "creator", "qname": "cm:creator", "searchable": "false"}, '
			+ '		{"title": "modifyDate", "qname": "cm:modified", "searchable": "false"} '
			+ '		]'
			+ '}';
   		
   		jsonString = requestbody.content;
   		
   		model.result = contentSearchService.search(jsonString);
