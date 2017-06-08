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
		<link rel="stylesheet" href="/static-assets/css/jquery-ui.min.css" />
	</head>
	<body>
		<!-- Wrapper -->
			<div id="wrapper">

				<!-- Main -->
					<div id="main">
						<div class="inner">

							<!-- Header -->
							<@renderComponent component=contentModel.header.item />

							<!-- Banner -->
								<section id="banner" <@studio.iceAttr iceGroup="hero"/>>
									<div class="content">
										<header>${contentModel.hero_title}</header>
										${contentModel.hero_text}
									</div>
									<span class="image object">
										<img src="${contentModel.hero_image !""}" alt="" />
									</span>
								</section>

							<!-- Section -->
								<section <@studio.iceAttr iceGroup="features"/>>
									<header class="major">
										<h2>${contentModel.features_title}</h2>
									</header>
									<div class="features" <@studio.componentContainerAttr target="features" objectId=contentModel.objectId/>>
										<#if contentModel.features?? && contentModel.features.item??>
										  <#list contentModel.features.item as feature>
										      <@renderComponent component=feature />
										  </#list>
										</#if>
									</div>
								</section>

							<!-- Section -->
								<section>
									<header class="major">
										<h2>Featured Articles</h2>
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
				<@renderComponent component=contentModel.left\-rail.item />

			</div>

		<!-- Scripts -->
			<script src="/static-assets/js/jquery.min.js"></script>
			<script src="/static-assets/js/jquery-ui.min.js"></script>
			<script src="/static-assets/js/skel.min.js"></script>
			<script src="/static-assets/js/util.js"></script>
			<!--[if lte IE 8]><script src="/static-assets/js/ie/respond.min.js"></script><![endif]-->
			<script src="/static-assets/js/main.js"></script>

		<@studio.toolSupport/>
	</body>
</html>
