<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
	<head>
      <meta charset="utf-8">
      <title>${model.title_t}</title>
	</head>
	<body>
    	<div <@studio.iceAttr iceGroup="title"/>>
			<h1>${model.title_t}</h1>
        </div>
      
    	<div <@studio.iceAttr iceGroup="main"/>>
            ${model.body_html}
        </div>

		<@studio.toolSupport/>	
	</body>
</html>
