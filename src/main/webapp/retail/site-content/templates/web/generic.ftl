<#import "/templates/system/common/cstudio-support.ftl" as studio />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
<head>
    
    <meta charset="utf-8">
    <title>Rosie's Rivets - Crafter CMS Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="author" content="Rosie's Rivets">

     <link href="/static-assets/css/main.css" rel="stylesheet">
    
    <script src="/static-assets/js/jquery.core.js" ></script>
    <script src="/static-assets/js/crafter-support-1-0-0.js" ></script> 
    
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>
<body>
<div id="main-container" class="product-page">

<#include "/templates/web/fragments/header.ftl"/>

<div class="container-fluid" id="content-body">
    
    <div class="row-fluid">
        <div class="span3 mb10" id="site-nav">
        
            <div class="input-append" id="site-search">
                <input type="text" class="wauto" placeholder="search" />
                <a class="add-on">
                    <i class="icon icon-search"></i>
                </a>
            </div>
            
            <ul class="nav nav-list amaranth uppercase">
                <@renderNavigation "/site/website", 1 />    
            </ul>
            
        </div>
        <div class="span9" id="content">

            <h1 <@studio.iceAttr iceGroup="headline"/> id="headerTitle">
                ${model.headline}
            </h1>

            <div <@studio.iceAttr iceGroup="body"/> id="abc">
                ${model.body}
            </div>
            <br/>
            <div <@studio.iceAttr iceGroup="author"/> >
              Article Author: ${model.author!""}
            </div>
       </div>


</div>

    <hr>
    <#include "/templates/system/common/components-support.ftl"/>
<#include "/templates/web/fragments/footer.ftl"/>
</div>
<!-- /container -->


</div>

<!-- - - - - - - - -->
<!-- - - - SUI - - -->
<!-- - - - - - - - -->
<script>

    var crafterSocial_cfg = {

        // The SUI base URL
        'url.base'                      : '/static-assets/sui/',
        // The fixtures URL. May be relative.
       // 'url.service'                   : '/static-assets/sui/fixtures/api/2/',
'url.service': 'http://127.0.0.1:8080/crafter-social/api/2/',
// The Templates URL. May be relative.
        'url.templates'                 : '/static-assets/sui/templates/',
        // 'url.security'                  : '...',
        // 'url.ugc.file'                  : '{attachmentId}.json',
        // 'url.ugc.{id}.get_attachments'  : '.json?tenant={tenant}',
        // 'url.ugc.{id}.add_attachment'   : '.json'

    };
/*
    function crafterSocial_onAppReady ( director, CrafterSocial ) {
        // console.log('Crafter Social is Ready!');

        // Initialise the "session user".
        director.setProfile({
            displayName: 'You',
            roles: [
                'SOCIAL_ADVISORY',
                'SOCIAL_ADMIN',
                'SOCIAL_MODERATOR'
            ]
        });


        director.socialise({
            target: '#abc',
            tenant: 'craftercms'
        });

        director.socialise({
            target: '#socialised2',
            tenant: 'craftercms'
        });

        director.socialise({
            target: '#socialised3',
            tenant: 'craftercms'
        });


    }
*/
</script>
<link rel="stylesheet" href="/static-assets/sui/styles/main.css" />
<script type="text/javascript" src="/static-assets/sui/scripts/social.js"></script>
<style>
    .crafter-social-view.crafter-social-discussion-view.crafter-social-popover {
        bottom: auto;
    }
    .crafter-social-bar,
    .crafter-social-view.crafter-social-commentable-options {
        z-index: 11;
    }
    .modal-body .mod-content p.highlight:last-child:after {
        margin-bottom: 20px;
    }
    .crafter-social-view.crafter-social-bar-form,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-feedback table.table,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget table.table {
        color: #ebebeb !important;
    }
    .crafter-social-view.crafter-social-bar-form input[type="text"],
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget input[type="text"],
    .crafter-social-view.crafter-social-bar-form textarea,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget textarea,
    .crafter-social-view.crafter-social-bar-form select,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget select {
        color: #333;
    }
</style>
<!-- - - - - - - - -->
<!-- - - - SUI - - -->
<!-- - - - - - - - -->

<script src="/static-assets/js/jquery.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>
<@studio.toolSupport />
 
</body>
</html>
