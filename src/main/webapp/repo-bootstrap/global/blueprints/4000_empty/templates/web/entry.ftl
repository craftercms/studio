<#import "/templates/system/common/crafter.ftl" as crafter />

<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>${model.title_t}</title>
		<@crafter.head/>
	</head>
	<body>
		<@crafter.body_top/>
		<@crafter.tag $field="title_t">
			<h1>${model.title_t}</h1>
		</@crafter.tag>

		<@crafter.tag $field="body_html">
			${model.body_html}
		</@crafter.tag>

		<script src="/static-assets/js/jquery.core.js"></script>
		<@crafter.body_bottom/>
	</body>
</html>
