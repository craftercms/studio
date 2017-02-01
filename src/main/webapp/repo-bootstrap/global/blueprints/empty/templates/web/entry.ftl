<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
	<head>
      <meta charset="utf-8">
      <title>${model.title}</title>
	</head>
	<body>
    	<div <@studio.iceAttr iceGroup="title"/>>
			<h1>${model.title}</h1>
        </div>
      
    	<div <@studio.iceAttr iceGroup="main"/>>
            ${model.body}
        </div>

		<@studio.toolSupport/>	
	</body>
</html>