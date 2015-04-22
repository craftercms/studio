<#include "/templates/web/navigation/navigation.ftl">
<!DOCTYPE html>
<html lang="en">
<head>

	<title>Global Integrity</title>
	
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="/static-assets/css/main.css" rel="stylesheet">
	<link href="/static-assets/css/mobile.css" rel="stylesheet">

</head>
<body class="article-page mobile">

	<header>
	
		<div class="logo-bar clearfix">
			<a id="brand" class="brand museo300" href="/">Global Integrity</a>
			<ul class="options pull-right">
				<li>
					<a href="#">Log in</a>
				</li>
				<li>
					<a href="#">Contact Us</a>
				</li>
			</ul>
		</div>
		<div id="hero">
			<h2 class="title museo500">
				${model.title!""}
			</h2>
		</div>
        <nav>
            <ul class="clearfix">
                <@renderNavigation "/site/website", 2 />
            </ul>
        </nav>
	
	</header>

	<div class="content">
		${model.body_html!""}
	</div>
	
	<footer class="arial">
		
		<ul class="mpage-actions">
			<li>
				<a href="#">
					Privacy Policy
				</a>
			</li>
			<li class="lang">
				<div class="dropdown">
					<a  id="language" href="#"
						data-toggle="dropdown" class="dropdown-toggle"
						role="button" data-target="#">
						Language
					</a>
					<ul class="dropdown-menu languages" role="menu" aria-labelledby="dLabel">
						<li>
							<a href="#" lang="en">
								<span class="flag flag-us" lang="en"></span> English US
							</a>
						</li>
						<li>
							<a href="#" lang="en_gb">
								<span class="flag flag-gb" lang="en_gb"></span> English UK
							</a>
						</li>
						<li>
							<a href="#" lang="es">
								<span class="flag flag-es" lang="es"></span> Spanish
							</a>
						</li>
						<li>
							<a href="#" lang="de">
								<span class="flag flag-de" lang="de"></span>German
							</a>
						</li>
					</ul>
				</div>
				<span id="currentlang" class="flag flag-us" current-lang="en"></span>
			</li>
		</ul>
		
	</footer>

    <script src="/static-assets/js/jquery-1.10.2.min.js"></script>
	<script src="/static-assets/js/bootstrap.min.js"></script>
	<script src="/static-assets/js/main.js"></script>

</body>
</html>