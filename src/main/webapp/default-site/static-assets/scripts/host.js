(function (window) {
    'use strict';

    var Events = crafter.studio.preview.Topics;
    var origin = 'http://127.0.0.1:8080';
    var communicator = new crafter.Communicator(origin);

    communicator.subscribe(Events.GUEST_CHECKIN, function (url) {
        // console.log('Guest checked in');
        setHashPage(url);
    });

    communicator.subscribe(Events.GUEST_CHECKOUT, function () {
        // console.log('Guest checked out');
    });

    communicator.subscribe(Events.ICE_ZONE_ON, function (message, scope) {
        CStudioAuthoring.InContextEdit.editControlClicked.call({
            content: {
                itemIsLoaded: true,
                field: message.iceId,
                item: CStudioAuthoring.SelectedContent.getSelectedContent()[0]
            }
        });
    });

    // Listen to the guest site load
    communicator.subscribe(Events.GUEST_SITE_LOAD, function (message, scope) {

        if (message.url) {
            setHashPage(message.url);
        }

        // Once the guest window notifies that the page as successfully loaded,
        // add the guest window as a target of messages sent by this window
        communicator.addTargetWindow({
            origin: origin,
            window: getEngineWindow().contentWindow
        });

    });

    function setHashPage(url) {
        window.location.hash = '#/?page=' + url;
    }

    function getEngineWindow() {
        return document.getElementById('engineWindow');
    }

    function goToHashPage() {

        var win = getEngineWindow();
        var hash = parseHash(window.location.hash);

        if (hash.site) {
            CStudioAuthoring.Utils.Cookies.createCookie('crafterSite', hash.site);
        }

        setTimeout(function () {
            // TODO this thing doesn't work well if document domain is not set on both windows. Problem?
            if (win.src.replace(origin, '') !== hash.page &&
                win.contentWindow.location.href.replace(origin, '') !== hash.page) {
                win.src = hash.page;
            }
        });

        CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, '/site/website/index.xml', {
            success: function(content) {
                CStudioAuthoring.SelectedContent.selectContent(content.item);
            },
            failure: function() {}
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
            setHashPage('/');
        } else {
            goToHashPage();
        }

    }, false);

}) (window);