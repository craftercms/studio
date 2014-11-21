var avmStoreId = args.storeId;
var crProject = args.crProject;
var formId = args.formId;
var username = person.properties.userName;
var webprojQuery ="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"";

var fixPathsFn = function(node) {
	if(node) {
		var content = node.content;
		content = new java.lang.String("<#include '/templates/system/common/cstudio-support.ftl' />" + content);
		content = content.replaceAll("/css", "/static-assets/css");	
		content = content.replaceAll("/images", "/static-assets/images");
		content = content.replaceAll("PressReleaseAdvanced", "model");
		content = content.replaceAll("PressRelease", "model");
		content = content.replace("text/static-assets/css", "text/css");
		content = content.replace("</body>", "<@cstudioOverlaySupport/></body>");

			
		while(content.indexOf("|") != -1) {
			content = content.replace("|", "/");
		}	
		node.content = content;
		node.save();
	}
}

// lookup the avm form xsd content
var formXsdQuery = 'PATH:"/app:company_home/app:dictionary/app:wcm_forms/cm:'+args.formId+'"';
var form = search.luceneSearch(formXsdQuery)[0];
var xsdStream = new java.io.ByteArrayInputStream(form.children[0].content.getBytes("UTF-8"));

// load the transformation
// getting as url and then converting to file allows us to update w/o restart
var xsdPath = "/alfresco/templates/webscripts/org/craftercms/avm2craftercms/import-avm-webform/avmXsdToCrafterForm.xsl";
var xsltStreamUrl = java.lang.Thread.currentThread().getContextClassLoader().getResource(xsdPath);  
var xsltFile = java.io.File(new java.net.URI(xsltStreamUrl.toString())) 
var xsltReader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(xsltFile)));

// execute the transformation
var crFormDef = new java.io.StringWriter();
var builder = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
var document = builder.parse(xsdStream);

var styleSource = new Packages.javax.xml.transform.stream.StreamSource(xsltReader); 
var transformerFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();
var transformer = transformerFactory.newTransformer(styleSource);

transformer.setParameter("contentType", formId);
transformer.setParameter("contentTypeLabel", form.properties["cm:description"]);

transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(new java.io.StringReader(form.children[0].content)), 
                      new Packages.javax.xml.transform.stream.StreamResult(crFormDef));
          
var formDefContent = crFormDef.toString(); 

// write the new form definition to the Crafter CMS project
var typeConfig =
	'<content-type name="/page/' + formId +'" is-wcm-type="true">\r\n' +
		'<label>'+ form.properties["cm:description"] +'</label>\r\n'+
		'<form>/page/' + formId +'</form>\r\n'+
		'<form-path>simple</form-path>\r\n' +
		'<model-instance-path>NOT-USED-BY-SIMPLE-FORM-ENGINE</model-instance-path>\r\n' +
		'<file-extension>xml</file-extension>\r\n' +
		'<content-as-folder>false</content-as-folder>\r\n' +
		'<previewable>true</previewable>\r\n' +
		'<noThumbnail>true</noThumbnail>\r\n' +
	'</content-type>';

var controllerContent = 
	'<import resource="classpath:alfresco/templates/webscripts/org/craftercms/cstudio/common/lib/common-lifecycle-api.js">\r\n' +
	'controller = (controller) ? controller : {};\r\n'+
	'controller.execute();';	

var extractionContent = 
	'<import resource="classpath:alfresco/templates/webscripts/org/craftercms/cstudio/common/lib/common-extraction-api.js">\r\n' +
	'contentNode.addAspect("cstudio-core:pageMetadata");\r\n'+
	'var root = contentXml.getRootElement();\r\n'+
	'extractCommonProperties(contentNode, root);\r\n'+
	'contentNode.save();';

var rootFolderQuery = 'PATH:"/app:company_home/cm:cstudio/cm:config/cm:sites/cm:'+crProject+'/cm:content-types/cm:page"';
var rootFolder = search.luceneSearch(rootFolderQuery)[0];

var typeFolder = rootFolder.childByNamePath(formId);

if(typeFolder) {
	rootFolder.removeNode(typeFolder);
}

typeFolder = rootFolder.createFolder(formId);

var configFile = typeFolder.createFile("config.xml");
configFile.content = typeConfig;
configFile.save();
 
var defFile = typeFolder.createFile("form-definition.xml");
defFile.content = formDefContent;
defFile.save();

var controllerFile = typeFolder.createFile("controller.js");
controllerFile.content = controllerContent;
controllerFile.save();

var extractFile = typeFolder.createFile("extract.js");
extractFile.content = extractionContent;
extractFile.save();


// process templates
var formChildren = form.children;
var templateRootQuery = 'PATH:"/app:company_home/cm:wem-projects/cm:'+crProject+'/cm:'+crProject+'/cm:work-area/cm:templates/cm:web"';
var templateRootNode = search.luceneSearch(templateRootQuery)[0];
	
for(var j=0; j<formChildren.length; j++) {
	var formChild = formChildren[j];
	if(formChild.name.indexOf(".ftl") != -1 ) {
		var templateQuery = 'PATH:"/app:company_home/cm:wem-projects/cm:'+crProject+'/cm:'+crProject+'/cm:work-area/cm:templates/cm:web/cm:'+ formId+'.ftl"';
		var templateNode = search.luceneSearch(templateQuery)[0];
		
//		if(templateNode) {
//			try{
//				templateNode.parent.removeNode(templateNode);
//			}
//			catch(err) {
//			}
//		}
		
		templateNode = crossRepoCopy.copy(formChild, templateRootNode, formId+".ftl");
		fixPathsFn(templateNode);
		
	}
}