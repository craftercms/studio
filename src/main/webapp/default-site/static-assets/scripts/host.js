(function (window, amplify, CStudioAuthoring) {
    'use strict';

    var cstopic = crafter.studio.preview.cstopic;
    var Topics = crafter.studio.preview.Topics;
    var origin = 'http://127.0.0.1:8080';
    var communicator = new crafter.studio.Communicator(origin);
    // CStudioAuthoring.Utils.Cookies.readCookie('crafterSite')

    communicator.subscribe(Topics.GUEST_CHECKIN, function (url) {
        var site = CStudioAuthoring.Utils.Cookies.readCookie('crafterSite');
        setHash({
            page: url,
            site: site
        });
    });

    communicator.subscribe(Topics.GUEST_CHECKOUT, function () {
        // console.log('Guest checked out');
    });

    communicator.subscribe(Topics.ICE_ZONE_ON, function (message, scope) {
        CStudioAuthoring.InContextEdit.editControlClicked.call({
            content: {
                itemIsLoaded: true,
                field: message.iceId,
                item: CStudioAuthoring.SelectedContent.getSelectedContent()[0]
            }
        });
    });

    // Listen to the guest site load
    communicator.subscribe(Topics.GUEST_SITE_LOAD, function (message, scope) {

        if (message.url) {
            setHash({
                page:message.url,
                site: CStudioAuthoring.Utils.Cookies.readCookie('crafterSite')
            });
        }

        // Once the guest window notifies that the page as successfully loaded,
        // add the guest window as a target of messages sent by this window
        communicator.addTargetWindow({
            origin: origin,
            window: getEngineWindow().contentWindow
        });

    });

    communicator.subscribe(Topics.STOP_DRAG_AND_DROP, function () {
        CStudioAuthoring.PreviewTools.panel.show();
        YDom.replaceClass('component-panel-elem', 'expanded', 'contracted');
    });

    communicator.subscribe(Topics.COMPONENT_DROPPED, function (message) {
        amplify.publish(cstopic('COMPONENT_DROPPED'), message.type, message.path);
    });

    amplify.subscribe(cstopic('START_DRAG_AND_DROP'), function (config) {
        CStudioAuthoring.PreviewTools.panel.hide();
        communicator.publish(Topics.START_DRAG_AND_DROP, {
            components: config.components.category.component
        });
    });

    function setHashPage(url) {
        window.location.hash = '#/?page=' + url;
    }

    function setHash(params) {
        var hash = [];
        for (var key in params) {
            hash.push(key + '=' + params[key]);
        }
        window.location.hash = '#/?' + hash.join('&');
    }

    function getEngineWindow() {
        return document.getElementById('engineWindow');
    }

    function goToHashPage() {

        var win = getEngineWindow();
        var hash = parseHash(window.location.hash);
        var site = CStudioAuthoring.Utils.Cookies.readCookie('crafterSite');
        var siteChanged = false;

        if (hash.site) {
            CStudioAuthoring.Utils.Cookies.createCookie('crafterSite', hash.site);
            siteChanged = (site !== hash.site);
        }

        setTimeout(function () {
            // TODO this thing doesn't work well if document domain is not set on both windows. Problem?
            if (siteChanged || (
                win.src.replace(origin, '') !== hash.page &&
                win.contentWindow.location.href.replace(origin, '') !== hash.page)) {
                win.src = hash.page;
            }
        });

        var path = ('/site/website/' + ((hash.page.indexOf('.html') !== -1)
            ? hash.page.replace('.html', '.xml')
            : hash.page+'/index.xml')).replace('//','/');
        CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, path, {
            success: function(content) {
                CStudioAuthoring.SelectedContent.setContent(content.item);
            }
        });

    }

    // TODO better URL support. Find existing lib, use angular or use backbone router?
    function parseHash(hash) {

        var str = hash.replace('#/', ''),
            params = {},
            param;

        str = str.substr(str.indexOf('?') + 1);
        str = str.split('&');

        for (var i = 0; i < str.length; ++i) {
            param = str[i].split('=');
            params[param[0]] = param[1];
        }

        return params;

    }

    window.addEventListener("hashchange", function (e) {
        e.preventDefault();
        goToHashPage();
    }, false);

    window.addEventListener('load', function () {

        if (window.location.hash.indexOf('page') === -1) {
            setHash({
                page: '/',
                site: CStudioAuthoring.Utils.Cookies.readCookie('crafterSite')
            });
        } else {
            goToHashPage();
        }

    }, false);

}) (window, amplify, CStudioAuthoring);