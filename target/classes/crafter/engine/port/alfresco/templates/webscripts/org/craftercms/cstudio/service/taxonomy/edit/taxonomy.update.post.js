var path = args.path;

valid = true;

if (path == undefined || path == "") {
	status.message = "type must be provided.";
	valid = false;
}

if(valid != false) {
	var taxonomyPathNode = search.luceneSearch("PATH:\"/app:company_home/cm:cstudio/cm:model/cm:taxonomies"+path+"\"")[0];
	
	if(taxonomyPathNode) {
		try{
			newItemNode.properties["{http://cstudio/assets/core/1.0}deleted"] = deleted;
			newItemNode.properties["{http://cstudio/assets/core/1.0}updated"] = true;
			newItemNode.properties["{http://www.alfresco.org/model/content/1.0}name"] = "crafter-taxonomy";
			newItemNode.properties["{http://www.alfresco.org/model/content/1.0}title"] = taxonomyId;
			newItemNode.save();
		}
		catch(errCreateSeq) {
			valid = false;
			status.message = "failed to create sequence: " + errCreateSeq;
		}				
	}
	else {
		status.message = "unable to remove taxonomy at path " +
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
