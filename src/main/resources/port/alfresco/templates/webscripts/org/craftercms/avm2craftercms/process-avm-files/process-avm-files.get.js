model.files = "";
var avmStoreId = args.storeId;
var crProject = args.crProject;
var username = person.properties.userName;

var fixPathsFn = function(node) {
	if(node) {
		var content = node.content;
		content = content.replaceAll("/css", "/static-assets/css");	
		content = content.replaceAll("/images", "/static-assets/images");	
		while(content.indexOf("|") != -1) {
			content = content.replace("|", "/");
		}	
		node.content = content;
		node.save();
	}
}


var processChildrenFn = function(node) {
	var children = node.children;
	for(var i=0; i<children.length; i++) {
		var child = children[i];
		if(child.isContainer || child.name == "ROOT") {
			processChildrenFn(child);
		}
		else {
			processFileFn(child);
		}
	}
};

var processFileFn = function(node) {	
	if(node.name.indexOf(".xml") != -1) {
		processFileXmlFn(node);
	}
	else if(node.name.indexOf(".html") != -1) {
		processFileHtmlFn(node);
	}
	else {
		processFileStaticFn(node);
	}
}

var processFileXmlFn = function(node) {
	// get the avm xml content
	var avmXmlStream = new java.io.ByteArrayInputStream(node.content.getBytes("UTF-8"));
	
	// get the transform
	var xsltPath = "/alfresco/templates/webscripts/org/craftercms/avm2craftercms/process-avm-files/avmXmlToPage.xsl";
	var xsltStreamUrl = java.lang.Thread.currentThread().getContextClassLoader().getResource(xsltPath);  
	var xsltFile = java.io.File(new java.net.URI(xsltStreamUrl.toString())) 
	var xsltReader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(xsltFile)));
	
	// perform the transform
	var crPageXml = new java.io.StringWriter();
	var builder = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
	var document = builder.parse(avmXmlStream);
	
	var styleSource = new Packages.javax.xml.transform.stream.StreamSource(xsltReader); 
	var transformerFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();
	var transformer = transformerFactory.newTransformer(styleSource);
	
	var internalName = node.name.replace(".xml","");
	transformer.setParameter("contentType", node.properties["wca:parentformname"]);
	transformer.setParameter("internalName", internalName);
	
	transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(new java.io.StringReader(node.content)), 
	                      new Packages.javax.xml.transform.stream.StreamResult(crPageXml));
	          
	var crXmlContent = crPageXml.toString(); 
	
	// write the output
	var destNode = createDmDestinationFn(node, "/cm:site/cm:website");
	
	var destFile = destNode.childByNamePath(node.name);
	if(!destFile) {
		destFile = destNode.createFile(node.name);
	}
	
	destFile.content = crXmlContent;
	destFile.addAspect("cstudio-core:previewable");
	destFile.save();
		
}

var processFileHtmlFn = function(node) {
	if(!node.properties["wca:primaryforminstancedata"] 
	|| node.properties["wca:primaryforminstancedata"] == "") {
		processFileStaticFn(node);
	}
}

var processFileStaticFn = function(node) {
	var destNode = createDmDestinationFn(node, "/cm:static-assets");
	var copiedNode = crossRepoCopy.copy(node, destNode, node.name);
	copiedNode.addAspect("cstudio-core:previewable");
	
	if(node.name.indexOf(".css") != -1) {
		fixPathsFn(copiedNode);
	}
}

var createDmDestinationFn = function(node, location) {
	var path = node.getDisplayPath();
	path = path.replace("/www/avm_webapps","");
	path = path.replace("/ROOT","");
	
	var pathParts = path.split("/");

	var siteRootQuery = 'PATH:"/app:company_home/cm:wem-projects/cm:'+crProject+'/cm:'+crProject+'/cm:work-area' + location + '"';
	
	model.files += '"'+path+'/'+ node.name +'",\r\n';
	
	var siteRootNode = search.luceneSearch(siteRootQuery)[0];
	
	var curNode = siteRootNode;
	for(var i=0; i<pathParts.length; i++) {
		var partName = pathParts[i];
		if(partName && partName != '' && partName != 'ROOT') {
			if(!curNode.childByNamePath(partName)) {
				curNode.createFolder(partName);
				curNode = curNode.childByNamePath(partName);
			}
			else {
				curNode = curNode.childByNamePath(partName);
			}
		}
	}
	
	return curNode;
}

//try {
	var avmRootPath = avmStoreId+":/www/avm_webapps";
	var projectRoot = avm.lookupNode(avmRootPath);
	processChildrenFn(projectRoot);
//}
//catch(err) {
//}
