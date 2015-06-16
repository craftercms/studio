<import resource="classpath:alfresco/templates/webscripts/org/craftercms/cstudio/common/lib/common-extraction-api.js">
contentNode.addAspect("cstudio-core:pageMetadata");
var root = contentXml.getRootElement();
extractCommonProperties(contentNode, root);
contentNode.save();