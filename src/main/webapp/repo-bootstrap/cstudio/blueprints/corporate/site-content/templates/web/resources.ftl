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
<div class="resources wrapper contact-us">

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
        <div class="">
            <div class="span9 text-darkgrey mt20">

                <div class="mb20">

                    <h3 class="museo300 weight-normal text-darkblue mb20">Select</h3>

                    <input class="block dropdown-style span6 mb10" type="text" placeholder="Topic" name="topic" />
                    <input class="block dropdown-style span6" type="text" placeholder="Industry" name="industry" />

                </div>

                <div class="row-fluid">
                    <div class="span4 articles">
                        <h2>Articles</h2>
                        <p>Access the latest article and reports from Global Integrity thought leaders</p>
                        <div class="article clearfix">
                            <a href="#">
                                <img src="http://placehold.it/49x58" />
                            </a>
                            <h3><a href="#">Article Title</a></h3>
                            <p>Description goes here description goes here</p>
                        </div>
                        <div class="article">
                            <a href="#">
                                <img src="http://placehold.it/49x58" />
                            </a>
                            <h3><a href="#">Article Title</a></h3>
                            <p>Description goes here description goes here</p>
                        </div>
                    </div>
                    <div class="span4 videos-n-webinars">
                        <h2>Videos &amp; Webinars</h2>
                        <p>Gain new insights on emerging trends and best practices.</p>
                        <div class="item">
                            <a href="#">
                                <img src="http://placehold.it/94x54" />
                            </a>
                            <h3><a href="#">Video Title</a></h3>
                        </div>
                        <div class="item">
                            <a href="#">
                                <img src="http://placehold.it/94x54" />
                            </a>
                            <h3><a href="#">Video Title</a></h3>
                        </div>
                    </div>
                    <div class="span4 white-papers">
                        <h2>White Papers</h2>
                        <p>Explore in-depth analysis of GRC solutions, strategies and technologies</p>
                        <div class="articles">
                            <div class="article clearfix">
                                <a href="#">
                                    <img src="http://placehold.it/49x58" />
                                </a>
                                <h3><a href="#">Article Title</a></h3>
                                <p>Description goes here description goes here</p>
                            </div>
                            <div class="article">
                                <a href="#">
                                    <img src="http://placehold.it/49x58" />
                                </a>
                                <h3><a href="#">Article Title</a></h3>
                                <p>Description goes here description goes here</p>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="span3 mt20">

                <a href="#">
                    <img src="/static-assets/images/myGI-link-btn.png" alt="My Global Integrity" />
                </a>

                <div class="featured-downloads mt20">
                    <h3 class="museo300">Featured Downloads</h3>
                    <div class="item clearfix">
                        <a href="#">
                            <img src="/static-assets/images/featured-downloads-basel.png" />
                        </a>
                        <h4><a href="#">2013 Basel III Legislation Unlikely</a></h4>
                        <p>Why financial institutions need to ready despite delay</p>
                    </div>
                    <div class="item clearfix">
                        <a href="#">
                            <img src="/static-assets/images/featured-downloads-supplychain.png" />
                        </a>
                        <h4><a href="#">2013 Basel III Legislation Unlikely</a></h4>
                        <p>Why financial institutions need to ready despite delay</p>
                    </div>
                    <div class="item clearfix">
                        <a href="#">
                            <img src="/static-assets/images/featured-downloads-scada.png" />
                        </a>
                        <h4><a href="#">2013 Basel III Legislation Unlikely</a></h4>
                        <p>Why financial institutions need to ready despite delay</p>
                    </div>
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