<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>
	<head>
		<title>${contentModel.title}</title>
		<meta charset="utf-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<!--[if lte IE 8]><script src="/static-assets/js/ie/html5shiv.js"></script><![endif]-->
		<link rel="stylesheet" href="/static-assets/css/main.css" />
		<!--[if lte IE 9]><link rel="stylesheet" href="/static-assets/css/ie9.css" /><![endif]-->
		<!--[if lte IE 8]><link rel="stylesheet" href="/static-assets/css/ie8.css" /><![endif]-->
	</head>
	<body>
		<!-- Wrapper -->
			<div id="wrapper">

				<!-- Main -->
					<div id="main">
						<div class="inner">

							<!-- Header -->
							<@renderComponent component=contentModel.header.item />

							<!-- Section -->
								<section>
									<header class="main">
										<h1>Search Results</h1>
									</header>
                  <form id="categories">
                  	<div class="row uniform">
											<div class="3u 6u(medium) 12u$(small)">
												<input type="checkbox" id="style" name="style" value="style">
												<label for="style">Style</label>
											</div>
											<div class="3u 6u$(medium) 12u$(small)">
												<input type="checkbox" id="health" name="health" value="health">
												<label for="health">Health</label>
											</div>
											<div class="3u 6u(medium) 12u$(small)">
												<input type="checkbox" id="entertainment" name="entertainment" value="entertainment">
												<label for="entertainment">Entertainment</label>
											</div>
											<div class="3u 6u$(medium) 12u$(small)">
												<input type="checkbox" id="technology" name="technology" value="technology">
												<label for="technology">Technology</label>
											</div>
										</div>
                  </form>
									<hr class="major"/>
									<div id="search-results">
									</div>
								</section>

						</div>
					</div>

					<!-- Sidebar -->
					<@renderComponent component=contentModel.sidebar.item />

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

		<!-- Scripts -->
			<script src="/static-assets/js/jquery.min.js"></script>
			<script src="/static-assets/js/skel.min.js"></script>
			<script src="/static-assets/js/handlebars.min-latest.js"></script>
			<script src="/static-assets/js/util.js"></script>
			<!--[if lte IE 8]><script src="/static-assets/js/ie/respond.min.js"></script><![endif]-->
			<script src="/static-assets/js/main.js"></script>
			<script src="/static-assets/js/search.js"></script>

		<@studio.toolSupport/>
	</body>
</html>
