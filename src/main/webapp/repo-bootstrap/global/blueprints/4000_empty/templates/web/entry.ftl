<#import "/templates/system/common/ice.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>${model.title_t}</title>
	</head>
	<body>
		<@studio.tag $field="title_t">
			<h1>${model.title_t}</h1>
		</@studio.tag>

		<@studio.tag $field="body_html">
			${model.body_html}
		</@studio.tag>

		<script src="/static-assets/js/jquery.core.js"></script>
		<@studio.initPageBuilder/>
	</body>
</html>
