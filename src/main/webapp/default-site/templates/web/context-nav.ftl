
<div id="studioBar" class="studio-view">
    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container-fluid">

            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/studio/site-dashboard">
                    <img src="/studio/static-assets/images/crafter_studio_360.png" alt="Crafter Studio">
                </a>
            </div>

            <div class="collapse navbar-collapse">

                <ul class="nav navbar-nav">
                    <li id="acn-dropdown-wrapper" class="acn-dropdown-wrapper"></li>
                </ul>

                <ul class="nav navbar-nav" id="activeContentActions"></ul>

                <a class="navbar-text navbar-right" href="/studio/logout" style="margin-left: 0">Log Out</a>
                <div id="acn-persona" class="navbar-right"></div>
                <div class="navbar-form navbar-right" role="search">
                    <div class="form-group">
                        <input type="text" class="form-control" id="acn-searchtext" value="" maxlength="256" />
                    </div>
                </div>
                <div id="acn-preview-tools" class="navbar-right"></div>
                <div id="acn-ice-tools" class="navbar-right"></div>

            </div>
        </div>
    </nav>
</div>

<div id="acn-wrapper" style="display: non e !important;">
    <div id="curtain" class="curtain-style"></div>
    <div id="authoringContextNavHeader">
        <div id="acn-bar">
            <div id="acn-group">
                <div id="acn-wcm-logo">
                    <a id="acn-wcm-logo-link" href="javascript:">
                        <img id="acn-wcm-logo-image"
                             class="acn-logo-image"
                             alt="Dashboard"/>
                    </a>
                </div>
                <div id="_acn-dropdown-wrapper" class="acn-dropdown-wrapper"></div>
                <div id="acn-active-content"></div>
                <div id="acn-admin-console" style="float: left"></div>
                <div id="contextual_nav_menu_items"></div>
            </div>
            <div id="acn-right">
                <div id="_acn-ice-tools" style="float: left"></div>
                <div id="_acn-preview-tools" style="float: left"></div>
                <div id="_acn-persona" style="float: left"></div>
                <div id="_acn-search"></div>
                <div id="acn-logout">
                    <a id="acn-logout-link" href="#">Log Out</a>
                </div>
                <div id="_contextual_nav_menu_items"></div>
            </div>
        </div>
    </div>
</div>
