<#import "/templates/system/common/crafter.ftl" as crafter />

<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html lang="en">
<head>
	<#include "/templates/web/fragments/head.ftl">
	<@crafter.head/>
</head>
<body>
<@crafter.body_top/>
<!-- Wrapper -->
<div id="wrapper">

	<!-- Main -->
	<div id="main">
		<div class="inner">

			<!-- Header -->
			<@renderComponent component=contentModel.header_o.item />

			<!-- Section -->
			<section>
				<header class="main">
					<h1>Search Results</h1>
					<h3>Refine by</h3>
				</header>
				<form id="categories">
					<div class="row uniform">
						<#list categories as category>
							<div class="3u 6u(medium) 12u$(small)">
								<input type="checkbox" id="${category.key}" name="${category.key}" value="${category.key}">
								<label for="${category.key}">${category.value}</label>
							</div>
						</#list>
					</div>
				</form>
				<hr class="major"/>
				<div id="search-results">
				</div>
			</section>

		</div>
	</div>

	<!-- Left Rail -->
	<@renderComponent component=contentModel.left_rail_o.item />

</div>

<!-- Handlebar Templates -->
<script id="search-results-template" type="text/x-handlebars-template">
	{{#each results}}
	<div>
		<h4><a href="{{url}}">{{title}}</a></h4>
		{{#if highlight}}
		<p>{{{highlight}}}</p>
		{{/if}}
	</div>
	{{else}}
	<p>No results found</p>
	{{/each}}
</script>

<#include "/templates/web/fragments/scripts.ftl">
<script src="/static-assets/js/handlebars.min-latest.js"></script>
<script src="/static-assets/js/search.js"></script>

<@crafter.body_bottom/>
</body>
</html>
