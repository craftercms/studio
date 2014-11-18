var name = args.name;
var type = args.type;
var path = args.path;

valid = true;

if (name == undefined || name == "") {
	status.message = "name must be provided.";
	valid = false;
}
else if (type == undefined || type == "") {
	status.message = "type must be provided.";
	valid = false;
}
else if (path == undefined || path == "") {
	status.message = "type must be provided.";
	valid = false;
}

if(valid != false) {
	//var taxonomyPathNode = search.luceneSearch("PATH:\"/app:company_home/cm:cstudio/cm:model/cm:taxonomies"+path+"\"")[0];
	var taxonomyPathNode = search.luceneSearch("ID:\""+path+"\"")[0];
	
	if(taxonomyPathNode) {
		try{
			if(sequenceService.sequenceExists("crafter-taxonomy") == false) {
				sequenceService.createSequence("crafter-taxonomy");
			}
			
			var taxonomyId = sequenceService.next("crafter-taxonomy");

			try {
				var newItemNode = taxonomyPathNode.createNode(name, type);
				
				newItemNode.properties["{http://cstudio/assets/core/1.0}order"] = 1;
				newItemNode.properties["{http://cstudio/assets/core/1.0}isCurrent"] = true;
				newItemNode.properties["{http://cstudio/assets/core/1.0}namespace"] = "crafter-taxonomy";
				newItemNode.properties["{http://cstudio/assets/core/1.0}id"] = taxonomyId;
				newItemNode.save();
			}
			catch(errCreateNode) {
				valid = false;
				status.message = "failed to create node: " + errCreateNode;
			}				
		}
		catch(errCreateSeq) {
			valid = false;
			status.message = "failed to create sequence: " + errCreateSeq;
		}				
	}
	else {
		status.message = "unable to create taxonomy at path " +
		                  "(/company home/cstudio/model/taxonomies"+path+") "+
		                  "because it does not exist.";
		valid = false;
	}
}
else {
	valid = false;
}

if(valid) {
	model.isValid = "true";
}
else {
	model.isValid = "false";
	status.code = 200;
	status.redirect = true;
}
