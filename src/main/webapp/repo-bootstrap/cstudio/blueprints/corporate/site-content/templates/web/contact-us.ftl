<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/common/google-map.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
  <head>
	<title>Global Integrity</title>
    
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="/static-assets/css/bootstrap-responsive.css" rel="stylesheet">
	<link href="/static-assets/css/main.css" rel="stylesheet">

		<@googleMapSupport />    
  </head>
  <body>
  	<div class="contact-us wrapper">
		
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

  					<h1 class="off-text logo" href="index.html">Global Integrity</h1>

                    <#include "/templates/web/common/search.ftl" />
                    
  					<div class="museo300 header-slogan">
                    	<@ice id='title' />
  						<div class="title-small mb5">RESOURCES</div>
  						<span class="title-large lh1-2">${model.title}</span>
					</div>
					
  				</div>
  			</div>
		</header>

        <div class="content arial">	
			<div class="row">
				<div class="span7 text-darkgrey mt40">
					
					<h3 class="museo300 weight-normal text-darkblue mb20">Corporate Headquarters</h3>

					<@ice id='locations' />
                    <#list model.locations.item as location>
					<div class="row locations-list">
    					<div class="span3">
    						<address class="mb10"><span class="sprite home xy-16-16 block float-left"></span>${location.address}</address>
        					<address class="mb10">
        						<span class="sprite phone xy-14-7 block float-left"></span>
        						${location.phone}
        					</address>	
        					<address class="mb10">
        						<span class="sprite email xy-16-16 block float-left"></span>
        						<a href="javascript:">${location.email}</a>
        					</address>
						</div>
    					<div class="span3">
    						<div class="wrap">
	    						<@googleMap id="map${location_index}" address=location.address />
    						</div>
    						
    					</div>
					</div>
					
					<div class="divide mb20" style="width:450px;"></div>
 					</#list>					
					
                    <h3 class="museo300 weight-normal text-darkblue mb10">Regional Offices</h3>
					
					<div class="accordion" id="accordion2">
						<div class="accordion-group">
							<div class="accordion-heading">
								<a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">
									<img src="/static-assets/images/regional-plus.png" class="plus" />
                                    <img src="/static-assets/images/regional-minus.png" class="minus" />
                                    <span>San Diego, California</span>
								</a>
							</div>
							<div id="collapseOne" class="accordion-body collapse">
								<div class="accordion-inner">
									
									<address class="mb10">
		        					   <span class="sprite home xy-16-16 block float-left"></span>
		        					   333 City Boulevard West<br/>
		        					   17th Floor<br/>
		        					   Orange, California 92868
		        					   United States+
		        					</address>	
		        					<address class="mb10">
		        						<span class="sprite phone xy-14-7 block float-left"></span>
		        						+1.555.555.5555
		        					</address>	
		        					<address class="mb10">
		        						<span class="sprite email xy-16-16 block float-left"></span>
		        						<a href="javascript:">us.info@globalintegrity.com</a>
		        					</address>
									
								</div>
							</div>
						</div>
						<div class="accordion-group">
							<div class="accordion-heading">
								<a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseTwo">
                                    <img src="/static-assets/images/regional-minus.png" class="minus" />
                                    <img src="/static-assets/images/regional-plus.png" class="plus" />
                                    <span>Florence, Italy</span>
								</a>
							</div>
							<div id="collapseTwo" class="accordion-body collapse">
								<div class="accordion-inner">
									
									<address>	
										<span class="sprite home xy-16-16 block float-left"></span>
										33 Viale Mazzini<br/>
										Florence Italy 85998<br/>
										United States+
									</address>
									<address>
										<span class="sprite phone xy-14-7 block float-left"></span>
										+1.555.555.5555
									</address>	
									<address>
										<span class="sprite email xy-16-16 block float-left"></span>
										<a href="javascript:">it.info@globalintegrity.com</a>
									</address>
									
								</div>
							</div>
						</div>
					</div>
					
					<div id="mapmodal" class="modal hide fade">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h3></h3>
						</div>
						<div class="modal-body">
							<iframe frameborder="0" marginheight="0" marginwidth="0" class="map-big"></iframe><br/>
							<a target="_blank" class="block text-right">View in GoogleMaps (new window)</a>
						</div>
					</div>

				</div>
				<div class="span4 mt40">

                    <h4 class="museo300 weight-normal text-darkblue mb20">Lets connect</h4>

					<div class="box pad style-inputs">

						<input class="block mb10" placeholder="Name" type="text" />
						<input class="block mb10" placeholder="Company" type="text" />
						<input class="block mb10" placeholder="Email" type="text" />
						<input class="block mb10" placeholder="Telephone" type="text" />
						<input class="block mb10 dropdown-style" type="text" placeholder="Country..." id="countries" />
						<textarea class="block mb10" placeholder="Reason for inquiry"></textarea>
						
						<input type="image" src="/static-assets/images/submit-button.png" />
						
					</div>
					
				</div>
			</div>
		</div>
        
		
		<#include "/templates/web/common/footer.ftl" />

	</div>

    <script src="/static-assets/js/jquery-1.10.2.min.js"></script>
    <script src="/static-assets/js/bootstrap.min.js"></script>
    <script src="/static-assets/js/main.js"></script>

    <@cstudioOverlaySupport/>
  </body>
</html>