<#include "/templates/web/navigation/navigation.ftl">
<!DOCTYPE html>
<html lang="en">
<head>

	<title>Global Integrity</title>
	
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="/static-assets/css/main.css" rel="stylesheet">
	<link href="/static-assets/css/mobile.css" rel="stylesheet">

</head>
<body class="home mobile">

	<header>
	
		<div class="logo-bar clearfix">
			<a id="brand" class="brand museo300" href="#">Global Integrity</a>
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
			<span class="slogan museo500">
				How will you manage<br/>
				the global risks you can't<br/>
				control, measure or predict?<br/>
			</span>
		</div>
		<div class="navbar">
		    <div class="navbar-inner">
		        <div class="container">
		            <a class="btn btn-navbar" onclick="return false;" data-toggle="collapse" data-target=".nav-collapse">
		                <span class="icon-bar"></span>
		                <span class="icon-bar"></span>
		                <span class="icon-bar"></span>
		            </a>
		
		            <nav class="nav-collapse collapse">
		                <ul class="nav">
							<@renderNavigation "/site/website", 2 />
		                </ul>
		            </nav>
		            <!--/.nav-collapse -->
		        </div>
		    </div>
		</div>
	
	</header>

	<div><!-- Content... --></div>
	
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