<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Global Integrity</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/static-assets/css/main.css" rel="stylesheet">

</head>
<body>
<div class="promo-page wrapper">
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
    <div class="bottom wrapper">
        <div class="pad">
            <h1 class="off-text logo">Global Integrity</h1>

            <div class="search">
                <div class="input-wrapper">
                    <div class="pad">
                        <input placeholder="search" type="text" data-provide="typeahead" data-source="[&quot;Alabama&quot;,&quot;Alaska&quot;,&quot;Arizona&quot;,&quot;Arkansas&quot;,&quot;California&quot;,&quot;Colorado&quot;,&quot;Connecticut&quot;,&quot;Delaware&quot;,&quot;Florida&quot;,&quot;Georgia&quot;,&quot;Hawaii&quot;,&quot;Idaho&quot;,&quot;Illinois&quot;,&quot;Indiana&quot;,&quot;Iowa&quot;,&quot;Kansas&quot;,&quot;Kentucky&quot;,&quot;Louisiana&quot;,&quot;Maine&quot;,&quot;Maryland&quot;,&quot;Massachusetts&quot;,&quot;Michigan&quot;,&quot;Minnesota&quot;,&quot;Mississippi&quot;,&quot;Missouri&quot;,&quot;Montana&quot;,&quot;Nebraska&quot;,&quot;Nevada&quot;,&quot;New Hampshire&quot;,&quot;New Jersey&quot;,&quot;New Mexico&quot;,&quot;New York&quot;,&quot;North Dakota&quot;,&quot;North Carolina&quot;,&quot;Ohio&quot;,&quot;Oklahoma&quot;,&quot;Oregon&quot;,&quot;Pennsylvania&quot;,&quot;Rhode Island&quot;,&quot;South Carolina&quot;,&quot;South Dakota&quot;,&quot;Tennessee&quot;,&quot;Texas&quot;,&quot;Utah&quot;,&quot;Vermont&quot;,&quot;Virginia&quot;,&quot;Washington&quot;,&quot;West Virginia&quot;,&quot;Wisconsin&quot;,&quot;Wyoming&quot;]" />
                        <a href="#" class="search-btn off-text">
                            Go
                        </a>
                    </div>
                </div>
                <span class="search-text arial lighter-weight">Global Risk Management</span>
            </div>

        </div>
    </div>
</header>
<div class="content">

    <img src="/static-assets/images/promo-1.png"
         style="position: absolute; top: 50%; left:50%; margin: -260px auto auto -214.5px;"
         alt="Predictive Analytics for GRC whitepaper cover" />
    <a id="downloadlink" class="uppercase" href="javascript:" data-toggle="modal" data-target="#popup">
        Download Whitepaper
    </a>

</div>

<#include "/templates/web/common/footer.ftl" />


<!-- login to download moda -->
<div id="popup" class="modal fade hide">
    <div class="modal-body">

        <button type="button"
                class="close off-text"
                data-dismiss="modal"
                aria-hidden="true">
            Close
        </button>

        <div class="mb20">
            <h3 class="text-darkblue museo300 weight-normal">My Global Integrity</h3>

            <p class="text-darkgrey">
                Login to access downloads.
            </p>

            <input class="input-style block mb10" type="text" placeholder="Username" />
            <input class="input-style block mb10" type="text" placeholder="Password" />

            <input type="image" src="/static-assets/images/login-button.png" value="LOG IN" />

        </div>

        <div class="arial">
            <a href="javascript:">Forgot Your Password</a>
            <div class="divide"></div>
            <a href="javascript:">New Users</a>
            <div class="divide"></div>
            <a href="javascript:">More Options</a>
        </div>

    </div>
</div>

</div>

<script src="http://code.jquery.com/jquery-1.8.3.min.js"></script>
<script src="/static-assets/js/bootstrap.js"></script>
<script src="/static-assets/js/main.js"></script>
    <@cstudioOverlaySupport/>
</body>
</html>