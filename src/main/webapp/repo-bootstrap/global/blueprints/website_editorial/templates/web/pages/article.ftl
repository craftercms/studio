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
							<@renderComponent component = contentModel.header.item />

							<!-- Content -->
								<section>
									<header class="main" <@studio.iceAttr iceGroup="subject"/>>
										<h1>${contentModel.subject!""}</h1>
										<h2>by ${contentModel.author!""}</h2>
									</header>
									<#if contentModel.image??>
										<#assign image = contentModel.image/>
									<#else>
										<#assign image = "/static-assets/images/placeholder.png"/>
									</#if>
									<span class="image main"><img src="${image}" alt="" /></span>
									<#list contentModel.sections.item as item>
										<div <@studio.iceAttr iceGroup="article"/>>
											${item.section_html}
										</div>
										<hr class="major" />
									</#list>
								</section>
						</div>
					</div>

					<#assign articleCategories = contentModel.queryValues("//categories/item/key")/>
					<#assign articlePath = contentModel.storeUrl />
					<#assign additionalModel = {"articleCategories": articleCategories, "articlePath": articlePath }/>

					<!-- Left Rail -->
					<@renderComponent component = contentModel.left\-rail.item additionalModel = additionalModel />

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
