// extract common properties
function extractCommonProperties(contentNode, root) {
	// this method should be called first.  
	// TODO: addCommonAspects should be called from form-extractors, to make sure no properties has been modified in between.  
	addCommonAspects(contentNode);
	extractContentType(contentNode, root);
	extractFloating(contentNode, root);
	extractTemplateVersion(contentNode, root);
	extractWCMIdentifiable(contentNode, root);

    extractOrders(contentNode, root);
    extractDisabled(contentNode, root);

	contentNode.properties["cstudio-core:internalName"] = root.valueOf("internal-name");
}

// aspect should be added, before introducing any property
function addCommonAspects(contentNode) {
	// part of WCM Identifiable common extractor
	contentNode.addAspect("cstudio-core:wcmIdentifiable");
}

// extract content template version
function extractTemplateVersion(contentNode, root) {
	var version = root.valueOf("template-version");
	if (version != null && version != "") {
		contentNode.properties["cstudio-core:templateVersion"] = version;
	}
}

// extract content type property
function extractContentType(contentNode, root) {
	var contentType = root.valueOf("content-type");
	if (contentType != null && contentType != "") {
		contentNode.properties["cstudio-core:contentType"] = contentType;
	}
}

// a special case that value=false means property=true
function extractFloating(contentNode, root) {
	var value = root.valueOf("placeInNav")
	if (value != undefined && value == "false") {
		contentNode.properties["cstudio-core:floating"] = true;
	} else {
		contentNode.properties["cstudio-core:floating"] = false;
	}
}

function extractPublicPrivate(contentNode, property, value) {
	if (value != undefined && value == "true") {
		contentNode.properties[property] = "Private";
	} else {
		contentNode.properties[property] = "Public";
	}
}

function extractPublicPrivateFromText(contentNode, property, value) {
	if (value != undefined && value == "private") {
		contentNode.properties[property] = "Private";
	} else {
		contentNode.properties[property] = "Public";
	}
}

// extract multi-valued property
function extractMultiTaxonomyTerms(contentNode, property, nodes) {
	if (nodes != null) {
		var count = 0;
		var termsArray = new Array();
		for (i=0; i < nodes.size(); i++) {
			var value = nodes.get(i).getText();
			if (value != null && value !="" && !isNaN(value)) {
				termsArray[count++] = value;
			} 
		}
		contentNode.properties[property] = termsArray;
	}
}

// extract single-valued property
function extractSingleTaxonomyTerm(contentNode, property, value) {
	if (value != null && value !="" && !isNaN(value)) {
		contentNode.properties[property] = value;
	}
}

//extract single-valued property
function extractSingleTaxonomyTermFromNode(contentNode, property, node) {
	if (node != null) {
		var value = node.get(0).getText();
		if (value != null && value !="" && !isNaN(value)) {
			contentNode.properties[property] = value;
		}
	}
}

// convert date using the format specified 	
function extractDateProperty(contentNode, property, value, converter) {
	if (value != null && value !="") {
		var dateValue = converter.convertFromFormDate("yyyy-MM-dd'T'HH:mm:ssZ", value);
		contentNode.properties[property] = dateValue;
	}
}
	
//convert date ONLY using the format specified 	
function extractDateOnlyProperty(contentNode, property, value, converter) {
	if (value != null && value !="") {
		var dateValue = converter.convertFromFormDate("yyyy-MM-dd'T'HH:mm:ssZ", value);
		contentNode.properties[property] = dateValue;
	}
}
	
// extract boolean with default for empty or null value 
function extractBooleanFromNode(contentNode, property, value, defaultValue) {
	if (value != null && value !="" && (value == "true" || value == "false") ) {
		contentNode.properties[property] = value;
	} else {
		contentNode.properties[property] = defaultValue;
	}
}

// extract the wcm-identifiable attributes, the pageId and pageIdGroup
function extractWCMIdentifiable(contentNode, root) {
	// moved to adding common aspect
	//contentNode.addAspect("cstudio-core:wcmIdentifiable");

	var pageId = root.valueOf("pageId");	
	var pageIdGroup = root.valueOf("pageIdGroup");
	
	try{
		if (pageId != null && pageId != "") {
			contentNode.properties["cstudio-core:wcmId"] = Number(pageId);
		}
		if (pageIdGroup != null && pageIdGroup != "") {
			contentNode.properties["cstudio-core:wcmGroupId"] = Number(pageIdGroup);
		}
	} catch (exception){
		logger.log("There was a number format exception: " + exception);
	}
}

// extract orders value
function extractOrders(contentNode, root) {
    var orderDefault = root.valueOf("orderDefault_f");
    try {
        if (orderDefault != null && orderDefault != "") {
            contentNode.properties["cstudio-core:orderDefault"] = Number(orderDefault);
        }
    } catch (exception){
        logger.log("There was a number format exception: " + exception);
    }
}

function extractDisabled(contentNode, root) {
    var disabled = root.valueOf("disabled");
    try {
        if (disabled != null && disabled !="" && (disabled == "true" || disabled == "false") ) {
            contentNode.properties["cstudio-core:disabled"] = disabled;
        } else {
            contentNode.properties["cstudio-core:disabled"] = false;
        }
    } catch (exception){
        logger.log("There was boolean format exception: " + exception);
    }
}
