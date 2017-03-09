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
		<!--[if lte IE 8]><script src="static-assets/js/ie/html5shiv.js"></script><![endif]-->
		<link rel="stylesheet" href="static-assets/css/main.css" />
		<!--[if lte IE 9]><link rel="stylesheet" href="static-assets/css/ie9.css" /><![endif]-->
		<!--[if lte IE 8]><link rel="stylesheet" href="static-assets/css/ie8.css" /><![endif]-->
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
										<ul class="actions">
											<li><a href="${contentModel.hero_learn_more_link}" class="button big">${contentModel.hero_learn_more_text}</a></li>
										</ul>
									</div>
									<span class="image object">
										<img src="${contentModel.hero_image !""}" alt="" />
									</span>
								</section>

							<!-- Section -->
								<section <@studio.iceAttr iceGroup="features"/>>
									<header class="major">
										<h2>${contentModel.section_1_title}</h2>
									</header>
									<div class="features">
										<#list contentModel.features.item as item>
											<article>
												<span class="icon ${item.feature_icon}"></span>
												<div class="content">
													<h3>${item.feature_title}</h3>
													${item.feature_body}
												</div>
											</article>
										</#list>
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
											<a href="#" class="image">
                                            	<#if article.image??>
                                                	<#assign articleImage = article.image/>
                                                <#else>
 													<#assign articleImage = "static-assets/images/pic01.jpg"/>                                                	
                                                </#if>
                                                <img src="${articleImage}" alt="" />
                                            </a>
											<h3>${article.title}</h3>
											<p>${article.headline}</p>
											<ul class="actions">
												<li><a href="${article.url}" class="button">More</a></li>
											</ul>
										</article>
                                        </#list>
									</div>
								</section>

						</div>
					</div>

				<!-- Sidebar -->
					<div id="sidebar">
						<div class="inner">

							<!-- Search -->
								<section id="search" class="alt">
									<form method="post" action="#">
										<input type="text" name="query" id="query" placeholder="Search" />
									</form>
								</section>

							<!-- Menu -->
								<nav id="menu">
									<header class="major">
										<h2>Menu</h2>
									</header>
									<ul>
										<li><a href="index.html">Homepage</a></li>
										<li><a href="generic.html">Generic</a></li>
										<li><a href="elements.html">Elements</a></li>
										<li>
											<span class="opener">Submenu</span>
											<ul>
												<li><a href="#">Lorem Dolor</a></li>
												<li><a href="#">Ipsum Adipiscing</a></li>
												<li><a href="#">Tempus Magna</a></li>
												<li><a href="#">Feugiat Veroeros</a></li>
											</ul>
										</li>
										<li><a href="#">Etiam Dolore</a></li>
										<li><a href="#">Adipiscing</a></li>
										<li>
											<span class="opener">Another Submenu</span>
											<ul>
												<li><a href="#">Lorem Dolor</a></li>
												<li><a href="#">Ipsum Adipiscing</a></li>
												<li><a href="#">Tempus Magna</a></li>
												<li><a href="#">Feugiat Veroeros</a></li>
											</ul>
										</li>
										<li><a href="#">Maximus Erat</a></li>
										<li><a href="#">Sapien Mauris</a></li>
										<li><a href="#">Amet Lacinia</a></li>
									</ul>
								</nav>

							<!-- Section -->
								<section>
									<header class="major">
										<h2>Ante interdum</h2>
									</header>
									<div class="mini-posts">
										<article>
											<a href="#" class="image"><img src="static-assets/images/pic07.jpg" alt="" /></a>
											<p>Aenean ornare velit lacus, ac varius enim lorem ullamcorper dolore aliquam.</p>
										</article>
										<article>
											<a href="#" class="image"><img src="static-assets/images/pic08.jpg" alt="" /></a>
											<p>Aenean ornare velit lacus, ac varius enim lorem ullamcorper dolore aliquam.</p>
										</article>
										<article>
											<a href="#" class="image"><img src="static-assets/images/pic09.jpg" alt="" /></a>
											<p>Aenean ornare velit lacus, ac varius enim lorem ullamcorper dolore aliquam.</p>
										</article>
									</div>
									<ul class="actions">
										<li><a href="#" class="button">More</a></li>
									</ul>
								</section>

							<!-- Section -->
								<section>
									<header class="major">
										<h2>Get in touch</h2>
									</header>
									<p>Sed varius enim lorem ullamcorper dolore aliquam aenean ornare velit lacus, ac varius enim lorem ullamcorper dolore. Proin sed aliquam facilisis ante interdum. Sed nulla amet lorem feugiat tempus aliquam.</p>
									<ul class="contact">
										<li class="fa-envelope-o"><a href="#">information@untitled.tld</a></li>
										<li class="fa-phone">(000) 000-0000</li>
										<li class="fa-home">1234 Somewhere Road #8254<br />
										Nashville, TN 00000-0000</li>
									</ul>
								</section>

							<!-- Footer -->
								<footer id="footer">
									<p class="copyright">&copy; Untitled. All rights reserved. Demo Images: <a href="https://unsplash.com">Unsplash</a>. Design: <a href="https://html5up.net">HTML5 UP</a>.</p>
								</footer>

						</div>
					</div>

			</div>

		<!-- Scripts -->
			<script src="static-assets/js/jquery.min.js"></script>
			<script src="static-assets/js/skel.min.js"></script>
			<script src="static-assets/js/util.js"></script>
			<!--[if lte IE 8]><script src="static-assets/js/ie/respond.min.js"></script><![endif]-->
			<script src="static-assets/js/main.js"></script>

		<@studio.toolSupport/>
	</body>
</html>