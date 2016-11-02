<!DOCTYPE html>
<html>
<head>

    <#include "/templates/system/common/versionInfo.ftl" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale = 1.0, user-scalable = no">

    <title>Crafter Studio</title>

    <link rel="stylesheet" href="/studio/static-assets/styles/cloud-site-main.css?version=${UIBuildId!''}">
    <link rel="stylesheet" href="/studio/static-assets/styles/user-dashboard.css?version=${UIBuildId!''}">
    <script src="/studio/static-assets//libs/modernizr/modernizr.js?version=${UIBuildId!''}"></script>
    <link rel="shortcut icon" href="/studio/static-assets/img/favicon.png?version=${UIBuildId!''}">

    <script>
        CStudioAuthoring = {
            cookieDomain: "${cookieDomain}"
        }
    </script>
</head>
<body>

<script type="text/x-handlebars">

    {{view App.TopbarView}}

    <div class="user-dashboard-container">
        {{outlet}}
    </div>

</script>

<script type="text/x-handlebars" data-template-name="topbar-view">

        <nav class="top-bar" data-topbar>
            <ul class="title-area">
                <li class="name">
                    <a href="javascript:">
                        <img src="/studio/static-assets//images/logo.png"
                             class="site-logo">
                    </a>
                </li>
                <li class="toggle-topbar menu-icon">
                    <a href="javascript:">Menu</a>
                </li>
            </ul>
            <section class="top-bar-section">
                <ul class="right">
                    {{#link-to 'all' tagName="li"}}
                    <a href="javascript:">
                        Sites
                    </a>
                    {{/link-to}}
                    <li>
                        <a href="https://craftersoftware.zendesk.com/home" target="_blank">Help</a>
                    </li>
                    <li class="has-dropdown">
                        <a href="javascript:">Account</a>
                        <ul class="dropdown" style="width: 300px">
                            <li id="userInfo">
                                {{view App.UserView}}
                            </li>
                            {{#link-to 'account' tagName="li"}}
                            <a href="javascript:">
                                Settings
                            </a>
                            {{/link-to}}
                            <li>
                                <a href="/logout">Sign Out</a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </section>
        </nav>

</script>

<script type="text/x-handlebars" data-template-name="dropdown-account">

    <h3 class="user-name">{{view.account.name}} {{view.account.surname}}</h3>
    <div class="email-display">
        {{view.account.email}}
    </div>

</script>

<script type="text/x-handlebars" id="sites">

    <div class="row">
        <div class="columns medium-3 mb20">
            <ul class="site-listing">
                <li>
                    {{#link-to 'all'}}
                    All Sites
                    {{/link-to}}
                </li>
                {{#each}}
                <li>
                    {{#link-to 'site' id}}{{name}}{{/link-to}}
                </li>
                {{/each}}
            </ul>
        </div>
        <div class="columns medium-9">
            {{outlet}}
        </div>
    </div>

</script>

<script type="text/x-handlebars" id="site">

    <h1>{{name}}</h1>

    <hr/>

    <div class="row">
        <div class="columns medium-8">

            <div class="row aspect">
                <div class="columns medium-12">
                    <h2 class="aspect-title">
                        Storage
                    </h2>
                    <div class="aspect-value">
                        <div class="progress">
                            {{meter storage}}
                        </div>
                        <small class="right">
                            {{kb-to-whatever storage.used}} of {{kb-to-gb storage.total}} GB
                        </small>
                    </div>
                </div>
            </div>

        </div>
        <div class="columns medium-4">

            <div class="row aspect">
                <div class="columns medium-12">
                    <h2 class="aspect-title">
                        Site Name
                    </h2>
                    <div class="aspect-value">
                        {{name}}
                    </div>
                </div>
            </div>

            <div class="row aspect">
                <div class="columns medium-12">
                    <h2 class="aspect-title">
                        Live URL
                    </h2>
                    <div class="aspect-value">
                        <a {{bind-attr href=url}} {{bind-attr title=url}}>{{url}}</a>
                    </div>
                </div>
            </div>

            <div class="row aspect">
                <div class="columns medium-12">
                    <h2 class="aspect-title">
                        Authoring
                    </h2>
                    <div class="aspect-value">
                        <a {{action 'goToDashboard'}} {{bind-attr href=cstudioURL}} {{bind-attr title=cstudioURL}}>{{cstudioURL}}</a>
                    </div>
                </div>
            </div>

        </div>
        <div class="columns medium-12">

            <hr/>

            <div class="row" data-equalizer>
                <div class="columns medium-6">

                    <div class="user-box radius" data-equalizer-watch>
                        <h2 class="title">Publishers</h2>
                        {{#if publishers}}
                        <table class="el-cells">
                            <thead>
                            <tr>
                                <th>Name</th>
                            </tr>
                            </thead>
                            <tbody>
                            {{#each publishers}}
                            <tr>
                                <td><div class="inner-cell">{{name}}</div></td>
                            </tr>
                            {{/each}}
                            </tbody>
                        </table>
                        {{else}}
                        <div class="alert-box info mb0">
                            <i class="foundicon-general-idea"></i> This site has no publishers.
                        </div>
                        {{/if}}
                    </div>

                </div>
                <div class="columns medium-6">

                    <div class="user-box radius" data-equalizer-watch>
                        <h2 class="title">Contributors</h2>
                        {{#if contributors}}
                        <table class="el-cells">
                            <thead>
                            <tr>
                                <th>Name</th>
                            </tr>
                            </thead>
                            <tbody>
                            {{#each contributors}}
                            <tr>
                                <td><div class="inner-cell">{{name}}</div></td>
                            </tr>
                            {{/each}}
                            </tbody>
                        </table>
                        {{else}}
                        <div class="alert-box info mb0">
                            <i class="foundicon-general-idea"></i> This site has no contributors.
                        </div>
                        {{/if}}
                    </div>

                </div>
            </div>

        </div>
    </div>

</script>

<script type="text/x-handlebars" id="all">

    <h1>
        <i class="foundicon-general-globe icon-adjust text-normal"></i>
        All Sites
    </h1>

    <hr class="mt0"/>

    <div class="table-wrp">
        <table class="sites-table">
            <thead>
            <tr>
                <th>
                    Name
                </th>
                <th>
                    Status
                </th>
                <th>
                    Live Site
                </th>
                <th>
                    Edit Site
                </th>
            </tr>
            </thead>
            <tbody>
            {{#each}}
            <tr>
                <td><div class="inner-cell name-col">{{name}}</div></td>
                <td><div class="inner-cell status-col">{{status}}</div></td>
                <td><div class="inner-cell url-col"><a {{bind-attr href=url}}>{{url}}</a></div></td>
                <td><div class="inner-cell dashboard-col"><a {{bind-attr data-site-id=siteId}} {{bind-attr href=cstudioURL}}>Go <i class="icon-adjust foundicon-general-right-arrow"></i></a></div></td>
            </tr>
            {{/each}}
            </tbody>
        </table>
    </div>

    <div class="reveal-modal" data-reveal data-iframe-revert>
        <iframe data-src="http://ur.goes.here"></iframe>
        <a class="close-reveal-modal">&times;</a>
    </div>

</script>

<script type="text/x-handlebars" id="account">

    <div class="row">
        <div class="columns medium-12">
            <h1>Account Management</h1>
            <hr/>
        </div>
    </div>

    <div class="row">
        <div class="columns medium-6">
            <h2>Profile</h2>

            <p>Your personal information is displayed below.</p>

            <div class="panel">

                <p class="lead">
                    <strong>Name</strong>: {{name}}  {{surname}}
                </p>
                <hr/>
                <p class="lead">
                    <strong>Email</strong>: {{email}}
                </p>

            </div>

        </div>

        <div class="columns medium-6">

            <h2>Change Password</h2>

            <p>Use the fields below to change your password.</p>

            <form id="changePasswordForm" action="/api/1/services/change-password" onsubmit="return false">

                {{input value=email type="hidden" name="email"}}

                <div class="row mb10">
                    <div class="columns medium-3 small-5">
                        <label for="current" class="text-right input-align bold" title="Current Password">Current</label>
                    </div>
                    <div class="columns medium-5 small-7">
                        <input id="current" type="password" name="original" class="mb0" />
                    </div>
                    <div class="columns medium-4 small-only-text-right mb10">
                        <small data-for="password">
                            Enter your current password.
                        </small>
                    </div>
                </div>

                <div class="row mb10">
                    <div class="columns medium-3 small-5">
                        <label for="password" class="text-right input-align bold">Password</label>
                    </div>
                    <div class="columns medium-5 small-7">
                        <input id="password" type="password" name="password" class="mb0" />
                    </div>
                    <div class="columns medium-4 small-only-text-right mb10">
                        <small data-for="password">
                            At least 6 characters and one number.
                        </small>
                    </div>
                </div>

                <div class="row mb10">
                    <div class="columns medium-3 small-5">
                        <label for="passwordConfirm" class="text-right input-align bold">Confirm</label>
                    </div>
                    <div class="columns medium-5 small-7">
                        <input id="passwordConfirm" type="password" name="confirmation" class="mb0" />
                    </div>
                    <div class="columns medium-4 small-only-text-right mb10">
                        <small data-for="passwordConfirm">
                            Please reenter your password.
                        </small>
                    </div>
                </div>

                <div class="row">
                    <div class="columns small-12 medium-9 medium-offset-3 small-offset-5" id="passwordMessages"></div>
                    <div class="columns medium-5 medium-offset-3 small-offset-5">
                        <button class="button" {{action 'changePassword'}}>
                        Submit
                        </button>
                    </div>
                </div>

            </form>

        </div>
    </div>

</script>

<div id="app"></div>

<footer class="text-center">
    &copy; CrafterCMS 2015
</footer>

<!-- build:js static-assets/scripts/user-dashboard.js -->
<!-- bower:js -->
<script src="/studio/static-assets/libs/jquery/jquery.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/jquery-cookie/jquery.cookie.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/jquery.browser/dist/jquery.browser.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/foundation/js/foundation/foundation.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/foundation/js/foundation/foundation.topbar.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/foundation/js/foundation/foundation.reveal.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/foundation/js/foundation/foundation.equalizer.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/handlebars/handlebars.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/libs/ember/ember.prod.js?version=${UIBuildId!''}"></script>
<script src="/studio/static-assets/scripts/user-dashboard.js?version=${UIBuildId!''}"></script>
<!-- endbuild -->

</body>
</html>