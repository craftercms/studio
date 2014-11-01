<!DOCTYPE html>
<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale = 1.0, user-scalable = no">
 
    
    
    <!--[if lt IE 9]>
    <script src="/static-assets/js/html5shiv.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-57-precomposed.png">
    <link rel="shortcut icon" href="http://craftersoftware.com/static-assets/ico/favicon.png">
    
    <title>Crafter Studio</title>

    <link rel="stylesheet" href="/studio/static-assets/styles/cloud-site-main.css">

    <script src="/studio/static-assets/libs/modernizr/modernizr.js"></script>

</head>
<body>



<div class="section" data-slide="1" data-magellan-destination="hero"
     data-stellar-background-ratio="0.5"
     style="background-image: url('${model.bgimage1}');">
    ${model.entryBody}
</div>



<div class="contain-to-grid sticky" data-magellan-expedition>
    <nav class="top-bar" data-topbar>
        <ul class="title-area">
            <li class="name">
                <a href="http://www.craftersoftware.com">
                    <img src="/studio/static-assets/images/logo.png"
                         class="site-logo">
                </a>
            </li>
            <li class="toggle-topbar menu-icon">
                <a href="javascript:">Menu</a>
            </li>
        </ul>
        <section class="top-bar-section">
            <!-- Right Nav Section -->
            <ul class="right">
                <li data-slide-to="1" data-magellan-arrival="hero">
                    <a href="javascript:">Home</a>
                </li>
                <li data-slide-to="2" data-magellan-arrival="about">
                    <a href="javascript:">About</a>
                </li>
                <li data-slide-to="4" data-magellan-arrival="services">
                    <a href="javascript:">Features</a>
                </li>
                <li data-slide-to="6" data-magellan-arrival="examples">
                    <a href="javascript:">Pricing</a>
                </li>
                <li>
                    <a href="javascript:" data-reveal-id="signInModal" data-reveal>Sign In</a>
                </li>
            </ul>
        </section>
    </nav>
</div>

<div class="section" data-slide="2" data-magellan-destination="about">
    ${model.aboutBody}
</div>

<#if RequestParameters["c"]??==false>
<div class="section" data-slide="3"
     data-stellar-background-ratio="0.5"
     style="background-image: url('${model.bgimage2}');">
    ${model.promoteBody1}
</div>
</#if>

<div class="section" data-slide="4" data-magellan-destination="services">
    ${model.servicesBody}
</div>

<div class="section" data-slide="5"
     data-stellar-background-ratio="0.5"
     <#if RequestParameters["c"]??==false>style="background-image: url('${model.bgimage3}');"<#else>style="background-color:#C2B59B;"</#if>>
    ${model.promoteBody2}
</div>

<div class="section" data-slide="6" data-magellan-destination="examples">
    ${model.pricingBody}
</div>

<footer class="main-footer">
    <div class="foot-sticker">

        <button class="button small" style="background-color:#ED1C24;" onclick="ga('send', 'event', 'CTA-Trial', 'Request a Trial', 'Home'); return true;" data-reveal-id="requestTrialModal" data-reveal="">
            <span class="hide-for-small">Request</span> Trial
        </button>
        <a href="http://www.craftersoftware.com/resources/lp?id=62485214-807a-e6a8-9f67-564fb327e234&t=v"><button class="button small" style="background-color:#ED1C24;" >
            <span class="hide-for-small">Free</span> Demo
        </button></a>
        <button class="button small alert" data-reveal-id="contactModal" style="background-color:#ED1C24;" >
            Contact <span class="hide-for-small">Us</span>
        </button>

   </div>
    
    <div class="row links">
        <div class="columns medium-3">
            <h5>Products</h5>
            <a href="http://craftersoftware.com/products/overview">Overview</a>
            <a href="http://craftersoftware.com/products/crafter-studio">Crafter Studio</a>
            <a href="http://craftersoftware.com/products/crafter-engine">Crafter Engine</a>
            <a href="http://craftersoftware.com/products/crafter-profile">Crafter Profile</a>
            <a href="http://craftersoftware.com/products/crafter-social">Crafter Social</a>
        </div>
        <div class="columns medium-3">
            <h5>Services</h5>
            <a href="http://craftersoftware.com/services/software-support">Crafter Enterprise</a>
            <a href="http://craftersoftware.com/services/cloud">Crafter Cloud</a>            
            <a href="http://craftersoftware.com/services/consulting">Consulting</a>
            <a href="http://craftersoftware.com/services/training">Training</a>
        </div>
        <div class="columns medium-3">
            <h5>Resources</h5>
            <a href="http://craftersoftware.com/resources/white-papers">White Papers</a>
            <a href="http://craftersoftware.com/resources/case-studies">Case Studies</a>
            <a href="http://craftersoftware.com/resources/e-books">E-Books</a>
            <a href="http://craftersoftware.com/resources/webcasts">Webcasts</a>
            <a href="http://blog.craftersoftware.com">Blog</a>
            <a href="http://craftercms.org">CrafterCMS.org</a>
        </div>
        <div class="columns medium-3">
            <h5>About</h5>
            <a href="http://craftersoftware.com/about/company">Company Overview</a>
            <a href="http://craftersoftware.com/about/customers">Our Customers</a>
            <a href="http://craftersoftware.com/about/partners">Our Partners</a>
            <a href="http://craftersoftware.com/about/awards">Awards</a>
            <a href="http://craftersoftware.com/about/about/news">News</a>
            <a href="http://craftersoftware.com/about/events">Events</a>
            <a href="http://craftersoftware.com/about/team">Leadership Team</a>
            <a href="http://craftersoftware.com/about/contact">Contact Us</a>
        </div>
    </div>
    <div class="row">
        <div class="columns small-12">
            <div class="copyright left">
                &copy; 2014 Crafter Software Corporation. All Rights Reserved.</div>
            <a class="button radius tiny right" data-slide-to="1">top</a>
        </div>
    </div>
</footer>

<form id="signInModal" action="/studio/api/1/services/login" class="reveal-modal small" data-reveal>
    <div class="row">
        <div class="large-12 columns">
            <h2>Crafter Cloud</h2>
            <h3>Customer Login</h3>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
                Email
                <input type="text" name="username" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
            <a data-reveal data-reveal-id="forgotPasswordModal" class="right" href="javascript:">Forgot your Password?</a>
                Password
                <input type="password" name="password" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
         <div class="feedback hide"></div>
            <button type="submit" class="button" id="signInButton">Sign in</button>
        </div>
    </div>

    <a class="close-reveal-modal">&#215;</a>
</form>

<form id="forgotPasswordModal" action="/studio/api/1/services/reset-password" class="reveal-modal small" data-reveal>
    <div class="row">
        <div class="large-12 columns">
            <h2>Recover Password</h2>
            <p class="lead">Type your email to recover your password.</p>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
                Email
                <input type="text" name="username" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <div class="feedback hide"></div>
            <button type="submit" class="button" id="recoverPasswordButton">Submit</button>
        </div>
    </div>
    <a class="close-reveal-modal">&#215;</a>
</form>

<script src="/studio/static-assets/scripts/cloud-home.js"></script>

</body>
</html>