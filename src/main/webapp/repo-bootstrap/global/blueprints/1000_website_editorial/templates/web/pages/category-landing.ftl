<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>
	<head>
		<title>${contentModel.title_t}</title>
		<meta charset="utf-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<!--[if lte IE 8]><script src="/static-assets/js/ie/html5shiv.js"></script><![endif]-->
		<link rel="stylesheet" href="/static-assets/css/main.css?v=${siteContext.siteName}" />
		<!--[if lte IE 9]><link rel="stylesheet" href="/static-assets/css/ie9.css" /><![endif]-->
		<!--[if lte IE 8]><link rel="stylesheet" href="/static-assets/css/ie8.css" /><![endif]-->
		<link rel="stylesheet" href="/static-assets/css/jquery-ui.min.css" />
	</head>
	<body>
		<!-- Wrapper -->
			<div id="wrapper">

				<!-- Main -->
					<div id="main">
						<div class="inner">

							<!-- Header -->
							<@renderComponent component=contentModel.header_o.item />

							<!-- Section -->
								<section <@studio.iceAttr iceGroup="articles"/>>
									<header class="main">
										<h1>${contentModel.articles_title_t}</h1>
									</header>
									<div class="posts">
										<#list articles as article>
										<article>
											<a href="${article.url}" class="image">
												<#if article.image??>
													<#assign articleImage = article.image/>
												<#else>
													<#assign articleImage = "/static-assets/images/placeholder.png"/>
												</#if>
												<img src="${articleImage}" alt="" />
											</a>
											<h3><a href="${article.url}">${article.title}</a></h3>
											<p>${article.summary}</p>
											<ul class="actions">
												<li><a href="${article.url}" class="button">More</a></li>
											</ul>
										</article>
										</#list>
									</div>
								</section>

						</div>
					</div>

					<!-- Left Rail -->
					<@renderComponent component=contentModel.left\-rail_o.item />

			</div>

		<!-- Scripts -->
			<script src="/static-assets/js/jquery.min.js"></script>
			<script src="/static-assets/js/jquery-ui.min.js"></script>
			<script src="/static-assets/js/skel.min.js"></script>
			<script src="/static-assets/js/util.js"></script>
			<!--[if lte IE 8]><script src="/static-assets/js/ie/respond.min.js"></script><![endif]-->
			<script src="/static-assets/js/main.js?v=${siteContext.siteName}"></script>

		<@studio.toolSupport/>
	</body>
