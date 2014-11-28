<!doctype html>
<html class="no-js" lang="" ng-app="userDashboard">
<head>
    <meta charset="utf-8">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, minimal-ui">
    <title>Crafter Studio</title>
    <!-- Place favicon.ico and apple-touch-icon.png in the root directory -->

<#-- TODO: Check if user is in session and spit it out here as shown by the example below.
<script type="application/json" id="user">{"name":"Roy","surname":"Art","email":"rart@rivetlogic.com"}</script>
-->

    <link rel="stylesheet" href="studio/static-assets/styles/main.css">

    <script src="studio/static-assets/scripts/modernizr.js"></script>

</head>
<body>
<!--[if lt IE 10]>
<p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> to improve your experience.</p>
<![endif]-->

<nav class="navbar navbar-default navbar-static-top" role="navigation" ng-controller="AppCtrl">
    <div class="container-fluid">

        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="/">
                <img src="/studio/static-assets/images/crafter_studio_360.png" alt="Crafter Studio"/>
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse"
             ng-if="!$state.$current.includes['login']">
            <ul class="nav navbar-nav navbar-right">
                <li ui-sref-active="active"><a ui-sref="home">Sites</a></li>
                <li><a href="https://craftersoftware.zendesk.com/home" target="_blank">Help</a></li>
                <li class="dropdown" dropdown>
                    <a class="dropdown-toggle" dropdown-toggle>Account <span class="caret"></span></a>
                    <ul class="dropdown-menu" role="menu">
                        <li class="user-display">
                            <div class="name">{{user.name}} {{user.surname}}</div>
                            <div class="email">{{user.email}}</div>
                        </li>
                        <li><a ui-sref-active="active" ui-sref="settings">Settings</a></li>
                        <li><a ng-click="logout()">Sign out</a></li>
                    </ul>
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container-fluid -->
</nav>

<div class="container">
    <ui-view></ui-view>
</div>

<script type="text/ng-template" id="templates/home">
    <div class="home-view">
        <div class="row" ng-if="sites">
            <div class="col-sm-4 col-md-3">
                <div class="list-group">
                    <a class="list-group-item"
                       ui-sref="home" ng-class="{ 'active': $state.$current.name === 'home' }">
                        All Sites
                    </a>
                    <a class="list-group-item"
                       ui-sref="home.site({ siteId: {{site.id}} })"
                       ui-sref-active="active"
                       ng-repeat="site in sites">
                        {{site.name}}
                    </a>
                </div>
            </div>
            <div class="col-sm-8 col-md-9" ui-view>

                <h1>All Sites</h1>
                <table class="table table-bordered">
                    <thead>
                    <tr>
                        <th>Site Name</th>
                        <th>CMS/Edit Link</th>
                        <th>Live Site Link</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr ng-repeat="site in sites">
                        <td>{{site.name}}</td>
                        <td>
                            <a href="{{site.cstudioURL}}">Edit site &raquo;</a>
                        </td>
                        <td>
                            <a href="{{site.url}}">Go to live site &raquo;</a>
                        </td>
                    </tr>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</script>

<script type="text/ng-template" id="templates/login">
    <div id="loginView" class="login-view">
        <div class="pad" ui-view>

            <div class="row">
                <div class="col-lg-12 columns text-center">
                    <img src="studio/static-assets/images/crafter_studio_360.png"
                         style="width: 60%; margin: 0 auto 20px; display: block;"/>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12 columns">
                    <div class="form-group">
                        <label for="username">Email/Username</label>
                        <input id="username" type="text"
                               ng-model="credentials.username"
                               class="form-control" placeholder="john@domain.com"
                               ng-model="credentials.username"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12 columns">
                    <div class="form-group">
                        <label for="username">Password</label>
                        <input type="password" id="password"
                               class="form-control" placeholder=""
                               ng-model="credentials.password" />
                        <a ui-sref="login.recover">Forgot your Password?</a>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12 columns">
                    <div class="alert alert-danger" ng-if="error">
                        {{error.message}}
                    </div>
                    <button type="submit" class="btn btn-primary" ng-click="login()">Sign in</button>
                </div>
            </div>

        </div>
    </div>
</script>

<script type="text/ng-template" id="templates/login/recover">
    <div class="recover-view">
        <div class="pad">

            <button class="close" ui-sref="login" ng-show="!success">&laquo; back</button>
            <h2>Recover</h2>

            <div ng-if="success">
                <div class="alert alert-success" ng-if="success">{{success}}</div>
                <button type="submit"
                        class="btn btn-info btn-block"
                        ui-sref="login">
                    &laquo; Back to login
                </button>
            </div>

            <div class="row" ng-if="!success">
                <div class="col-lg-12 columns">
                    <div class="form-group">
                        <label for="username">Email/Username</label>
                        <input type="email" id="username"
                               class="form-control" placeholder="john@domain.com"
                               ng-model="credentials.username"/>
                    </div>
                </div>
                <div class="col-lg-12 columns">
                    <div class="alert alert-danger" ng-if="error">{{error}}</div>
                    <button type="submit" class="btn btn-primary"
                            ng-click="recover()">
                        Submit
                    </button>
                </div>
            </div>

        </div>
    </div>
</script>

<script type="text/ng-template" id="templates/site">
    <div class="site-view" ng-if="sites">

        <h1>
            {{site.name}}
            <small class="text-sm text-italic"><a href="{{site.url}}" target="_blank" title="Live URL">{{site.url}} &raquo;</a></small>
        </h1>


        <div class="row">
            <div class="col-sm-12">

                <h3>Storage</h3>
                <progressbar animate="true" value="percent(site.storage)">
                    {{percent(site.storage)}}%
                </progressbar>

                <h3>
                    <a href="{{site.cstudioURL}}">
                        Authoring &raquo;
                    </a>
                </h3>
                <input readonly value="{{site.cstudioURL}}" type="text" class="form-control" onclick="this.select()"/>

            </div>
        </div>

        <div class="row">
            <div class="col-sm-6">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Publishers</h3>
                    </div>
                    <table class="table table-striped">
                        <thead class="hide">
                        <tr>
                            <th>Name</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr ng-repeat="publisher in site.publishers">
                            <td><div class="inner-cell">{{publisher.name}}</div></td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="alert alert-info" ng-if="!site.publishers.length">
                        This site has no publishers.
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Contributors</h3>
                    </div>
                    <table class="table table-striped">
                        <thead class="hide">
                        <tr>
                            <th>Name</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr ng-repeat="contributor in site.contributors">
                            <td><div class="inner-cell">{{contributor.name}}</div></td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="alert alert-info" ng-if="!site.contributors.length">
                        This site has no contributors.
                    </div>
                </div>
            </div>
        </div>

    </div>
</script>

<script type="text/ng-template" id="templates/settings">
    <div class="settings-view">
        <div class="row"
             ng-init="data.action = '/api/1/services/change-password'">

            <div class="col-sm-12">
                <h1>Account Management</h1>
            </div>
            <div class="col-sm-6">
                <h2>Profile</h2>
                <div class="well">
                    <div class="user-display">
                        <div class="name">{{user.name}} {{user.surname}}</div>
                        <div class="email">{{user.email}}</div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <h2>Change Password</h2>
                <div class="form">
                    <div class="form-group">
                        <label for="current">Current Password</label>
                        <input class="form-control" type="password" id="current" ng-model="data.original"/>
                    </div>
                    <div class="form-group">
                        <label for="password">New Password</label>
                        <input class="form-control" type="password" id="password" ng-model="data.password"/>
                    </div>
                    <div class="form-group">
                        <label for="confirm">Confirm Password</label>
                        <input class="form-control" type="password" id="confirm" ng-model="data.confirmation"/>
                    </div>
                    <div class="form-group">
                        <div ng-if="error" class="alert alert-danger">{{error}}</div>
                        <div ng-if="message" class="alert alert-success">{{message}}</div>
                        <button class="btn btn-primary btn-lg" ng-click="changePassword()">
                            Submit
                        </button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</script>

<script src="studio/static-assets/scripts/angular.js"></script>

<script src="studio/static-assets/scripts/main.js"></script>

</body>
</html>
