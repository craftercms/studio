<#import "/templates/system/common/crafter.ftl" as crafter />

<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>${model.title_t}</title>
		<style>
			html, body {
				color: #333;
				height: 100%;
				background: #f3f3f3;
				font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
			}
			main {
				max-width: 800px;
				padding: 40px;
				background: rgba(255,255,255,0.6);
				border-radius: 20px;
				margin: 100px auto;
			}
		</style>
		<@crafter.head/>
	</head>
	<body>
		<@crafter.body_top/>
		<main>
			<@crafter.h1 $field="title_t">${model.title_t}</@crafter.h1>
			<@crafter.div $field="body_html">${model.body_html}</@crafter.div>
		</main>
		<@crafter.body_bottom/>
	</body>
</html>
