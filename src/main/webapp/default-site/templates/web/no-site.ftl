<#include "/templates/system/common/versionInfo.ftl" />
 <!doctype html>
<head>
  <meta charset="utf-8">

  <!-- Use the .htaccess and remove these lines to avoid edge case issues.
       More info: h5bp.com/b/378 -->
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <!-- meta http-equiv="refresh" content="10; url=/share" -->

  <title>Crafter WEM Preview: NO SITE SET</title>
  <meta name="description" content="">
  <meta name="author" content="">

  <!-- Mobile viewport optimized: j.mp/bplateviewport -->
  <meta name="viewport" content="width=device-width,initial-scale=1">

  <!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

  <!-- CSS: implied media=all -->
  <!-- CSS concatenated and minified via ant build script-->
  <link href='${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/css/default-style.css')}' rel='stylesheet' type='text/css'>
  <link href='//fonts.googleapis.com/css?family=Pontano+Sans' rel='stylesheet' type='text/css'>
  <!-- end CSS-->

  <!-- More ideas for your <head> here: h5bp.com/d/head-Tips -->

  <!-- All JavaScript at the bottom, except for Modernizr / Respond.
       Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
       For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->
  <script src="js/libs/modernizr-2.0.6.min.js"></script>
</head>

<!-- TODO: SJ: Clean up this page -->

<body>
  <div id="container">
  	<div id="side-col">
		<header>
			<a class="logo" href="#"><img src="/static-assets/img/crafter-logo-transparent.png" alt="Crafter WEM by Crafter Software"   /></a>
			<nav>
				<ul>
           <li class="active"><span>Crafter CMS</span></li>
          <li><a href="http://craftersoftware.com/products"><span>Overview &amp; Features</span></a></li>
          <li><a href="http://craftercms.org/downloads"><span>Download</span></a></li>
          <li><a href="http://docs.craftercms.org"><span>Manuals and Documentation</span></a></li>
          <li><a href="http://craftercms.org"><span>Our Contributors</span></a></li>
          <li><a href="http://craftersoftware.com/about/partners"><span>Professional Services</span></a></li>
          <li><a href="http://craftersoftware.com"><span>Crafter Software</span></a></li>

				</ul>
			</nav>
		</header>
		<aside>



		</aside>
	</div>
    <div id="main" role="main">
    	<section class="intro bgOpaque">
			<h1>Welcome to Crafter WEM</h1>
			<p>Crafter WEM is an award-winning, open source Web Experience Management system built on top of Alfresco, the world's leading open platform for content management.  Crafter WEM consists of two major applications: Crafter Studio for content authoring, management, and publishing, and Crafter Engine for content delivery of dynamic website applications.</p> </section>
    	<section class="features bgOpaque">
    		<h1>OOPS! No Site is Set!</h1>
    		<p>To preview pages you must be logged in to Crafter Studio.</p>
    	</section>
    </div>

  </div> <!--! end of #container -->
  <footer>
  	<p>Copyright &copy; 2007 - 2017, Crafter Software Corporation. All rights reserved.<br />
  	Crafter WEM is free and open source software licensed under the GNU General Public License (GPL) version 3.0
  	</p>
  </footer>

  <!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
  <script src="/static-assets/js/libs/jquery-1.6.2.min.js?version=${UIBuildId!''}"></script>


  <!-- scripts concatenated and minified via ant build script-->
  <script defer src="/static-assets/js/plugins.js?version=${UIBuildId!''}"></script>
  <script defer src="/static-assets/js/script1.js?version=${UIBuildId!''}"></script>
  <!-- end scripts-->

</body>
</html>