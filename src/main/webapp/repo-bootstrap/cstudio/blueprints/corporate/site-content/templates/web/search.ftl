<#assign query = searchService.createQuery()>
<#if RequestParameters["q"]?? && RequestParameters["q"]!="">
    <#assign q = RequestParameters["q"]>
	<#assign start = 0>

	<#if RequestParameters["start"]??>
	    <#assign start = RequestParameters["start"]?number>
	</#if>

	<#assign rows = 10>
	
	<#assign query = query.setQuery("content-type:/page/article AND body_html:"+q)>
	<#assign query = query.setStart(start).setRows(rows)>
	
	<#assign results = searchService.search(query)>
	<#assign numFound = results.response.numFound>
	<#assign matches = results.response.documents>
</#if>


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
 	<div class="search-page wrapper">
		
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
  						<span class="title-larger lh1-2 weight-normal">
	  						Search Results
  						</span>
					</div>
					
  				</div>
  			</div>
		</header>


	    <div class="content arial">
	    
	    	<div class="box pad mt20 mb20 auto form-inline" id="insearch">
		    	<input id="search" <#if q?exists==true>value="${q}"</#if> type="text" class="input-style" />
                <input type="image" src="/static-assets/images/search-button-2.png"
                       onclick="document.location = ('/search?q='+document.getElementById('search').value); return false;"
                       alt="Search" />
	    	</div>
	    	
			<div class="row-fluid mb40">
				<div class="span8">
			    	<#if q?exists == true>

					<strong class="text-darkgrey">To narrow your search select a category:</strong><br/>
					<a href="javascript:">globalintegrity.com</a> | <a href="javascript:">Downloads</a> | <a href="javascript:">All Categories</a>
					
					<div class="text-right">
						<a href="javascript:">&lt; Previous</a> &nbsp; <a href="javascript:">Next &gt;</a>
					</div>
					
					<div class="box-1 square mb10">
						<div class="header pad">
							<h4>Key Match</h4>
						</div>
						<div class="body pad">
							<a href="javascript:void(null)" class="text-darkblue">
								Ti berem ationes sitesequam et occus as et maximus modi odis dis eum ent aciant iscides
							</a>
							<span class="muted">
								http://www.globalintegrity.com
							</span>
						</div>
					</div>

					<ul class="search-results">

		                <#if matches??>
		                    <#list matches as match>
							<li>
								<a href="javascript:void(null)" class="text-darkblue">
									${match.title!""}
								</a>
								<span class="muted">
								    <#assign blurb = match.body_html!"">
								    <#if (blurb?length > 500) == true>
								       <#assign blurb = blurb?substring(0,500)>
								    </#if>
									${blurb}
								</span>
							</li>
		
		                    </#list>
						</#if>
					</ul>
				</#if>	
				</div>
				<div class="span4">

					<h2 class="museo300 weight-normal text-darkblue mt0">Videos</h2>
					
					<div class="videos mb20 relative">
						
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