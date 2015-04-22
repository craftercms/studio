<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
  <head>
	<title>Global Integrity</title>
    
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="/static-assets/css/bootstrap-responsive.css" rel="stylesheet">
	<link href="/static-assets/css/main.css" rel="stylesheet">

  </head>
  <body>
  	<div class="interior-page wrapper">
		
  		<header>
  			<div class="top">
  				<div class="pad">
  					
					<nav>
						<ul class="main-nav clearfix">
                           <@renderNavigation "/site/website", 2 />
                        </ul>
					</nav>
                    
                    <#include "/templates/web/common/page-actions.ftl" />
				</div>
			</div>
  			<div class="bottom">
  				<div class="pad">
                    <h1 class="off-text logo" href="/">Global Integrity</h1>
                    <#include "/templates/web/common/search.ftl" />
  					
  					<div class="museo300 header-slogan">
						<@ice id='title' />
                        <div class="title-small mb5">ARTICLES</div>
  						<span class="title-larger lh1-2 weight-normal">
	  						${model.title}
  						</span>
					</div>
					
  				</div>
  			</div>
		</header>

        <div class="content arial">	
			<div class="row-fluid mt20 mb20">
				<div class="span8">
                  <@ice id='body' />
                  ${model.body_html!""}
				</div>
				<div class="span4">

                    <h2 class="museo300 weight-normal text-darkblue mt0">Videos</h2>

                    <div class="videos mb20">

                        <button class="videos-control prev">Previous</button>
                        <button class="videos-control next">Next</button>

                        <div class="video">
                            <a href="javascript:">
                                <img src="/static-assets/images/video-thumb-1.png" />
                            </a>
                            <h3>The Agile Enterprise 1</h3>
                            <p>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sapien quam.
                            </p>
                        </div>
                        <div class="video">
                            <a href="javascript:">
                                <img src="/static-assets/images/video-thumb-2.png" />
                            </a>
                            <h3>The Agile Enterprise 2</h3>
                            <p>
                                Ullamcorper velit. In ornare turpis tristique nibh faucibus fringilla. Cras vel facilisis odio.
                            </p>
                        </div>
                        <div class="video">
                            <a href="javascript:">
                                <img src="/static-assets/images/video-thumb-1.png" />
                            </a>
                            <h3>The Agile Enterprise 3</h3>
                            <p>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sapien quam.
                            </p>
                        </div>
                        <div class="video">
                            <a href="javascript:">
                                <img src="/static-assets/images/video-thumb-2.png" />
                            </a>
                            <h3>The Agile Enterprise 4</h3>
                            <p>
                                Ullamcorper velit. In ornare turpis tristique nibh faucibus fringilla. Cras vel facilisis odio.
                            </p>
                        </div>
                    </div>

                    <h2 class="museo300 weight-normal text-darkblue">Latest Tweets</h2>

                    <div class="tweets">
                        <div class="box-2 tweet pad mb10">
                            <a href="javascript:">
                                <img src="/static-assets/images/avatar-1.png" />
                            </a>
                            <div class="body">
                                Volore niendi non rem eum quas rendi ne qui omnis dunt mos ab inctata esciaeri. Volore niendi non rem eum quas rendi ne qui omnis.
                            </div>
                            <div class="date">July 26, 2012 - 8:48 am</div>
                        </div>
                        <div class="box-2 tweet pad mb10">
                            <a href="javascript:">
                                <img src="/static-assets/images/avatar-2.png" />
                            </a>
                            <div class="body">
                                Volore niendi non rem eum quas rendi ne qui omnis dunt mos ab inctata esciaeri. Volore niendi non rem eum quas rendi ne qui omnis.
                            </div>
                            <div class="date">July 26, 2012 - 8:48 am</div>
                        </div>
                    </div>
					
				</div>
			</div>
		</div>
        
		
		<#include "/templates/web/common/footer.ftl" />

	</div>

    <script src="http://code.jquery.com/jquery-1.8.3.min.js"></script>
    <script src="/static-assets/js/bootstrap.js"></script>
    <script src="/static-assets/js/main.js"></script>

    <@cstudioOverlaySupport/>

  </body>
</html>