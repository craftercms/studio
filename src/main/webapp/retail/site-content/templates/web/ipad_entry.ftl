<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter Rivet Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Rivet Logic Corporation">

    <link href="/static-assets/css/main.css" rel="stylesheet">
    <link href="/static-assets/css/main-ipad.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="/static-assets/css/splashscreen.css" />
	
	<script src="http://ajax.microsoft.com/ajax/jQuery/jquery-1.4.4.min.js"></script>
	<script src="/static-assets/js/jquery.splashscreen.js"></script>
    
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

</head>
<body>
<div id="page">
    <div id="promoIMG">
    	<img src="/static-assets/images/logo-1.png" alt="Available Now" />
    </div>
</div>
<div id="main-container">


<div class="container-fluid" id="content-body">

        
    <div class="row-fluid">
        <div class="span3 mb10" id="site-nav">
        

			<ul class="nav nav-list amaranth uppercase">
				<@renderNavigation "/site/website", 2 />
			</ul>
            
        </div>
        <div class="span9" id="content">
		 <#assign device = "mobile" />
		 <#include "/templates/web/fragments/entry_tiles_large.ftl"/>                    
        </div>
    <hr>
    <!-- #include "/templates/web/fragments/footer.ftl"/ -->

</div>
<!-- /container -->

</div>

<script src="/static-assets/js/jquery.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>
<script>
 $(".tile").mouseover(function( ) {
	$(this).find(".gradient-overlay")[0].style.display =  "block";
});

 $(".tile").mouseout(function( ) {
	$(this).find(".gradient-overlay")[0].style.display =  "none";
});

	$(document).ready(
	    function() {  
	    	var buttons = $('.btn');
	        	for(var i=0; i<buttons.length; i++) {
	        		buttons[i].style.padding = "10px";
	        	}    	    		   	    
	    });

		$(document).ready(function(){

			$('#promoIMG').splashScreen({
				textLayers : [
					'/static-assets/images/logo-1.png'
				]
			});

			
		});

</script>

</body>
</html>
