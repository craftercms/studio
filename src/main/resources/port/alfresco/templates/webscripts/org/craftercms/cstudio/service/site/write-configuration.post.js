var content = requestbody.content;
var path = args.path;

cmpath = path.replace(/\//g,'/cm:');

var rootFolder = 'PATH:\"//app:company_home/cm:cstudio';
var query = rootFolder + cmpath + '\"';
contentNode = search.luceneSearch(query)[0];

if(!contentNode) {
	var node = contentNode = search.luceneSearch(rootFolder + '\"')[0];
	var parts = path.split("/");

	for(var i=0; i<parts.length; i++) {
		var part = parts[i];
		if(part) {
			var tmpNode = node.childByNamePath(part);
			
			if(!tmpNode) {
				if(part.indexOf(".") == -1) {
					tmpNode = node.createFolder(part);
				}
				else {
					tmpNode = node.createFile(part);
				}
			} 
			
			node = tmpNode;
		}
	}
	
	node.content = content;
	node.save();
	
}
else {
	contentNode.content = content;
	contentNode.save();
}