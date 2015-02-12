require(['jquery', 'preview', 'amplify', 'communicator', 'ice-overlay'], function ($, p, a, Communicator, ICEOverlay) {
    'use strict';

    $.noConflict(true);

    // TODO
    document.domain = "127.0.0.1";

    var origin = 'http://127.0.0.1:8080';
    var Events = crafter.studio.preview.Topics;
    var communicator = new Communicator({window: window.parent, origin: origin}, origin);

    // When the page has successfully loaded, notify the host window of it's readiness
    communicator.publish(Events.GUEST_SITE_LOAD, {
        location: window.location.href,
        url: window.location.href.replace(window.location.origin, '')
    });

    function loadCss(url) {
        var link = document.createElement("link");
        link.type = "text/css";
        link.rel = "stylesheet";
        link.href = url;
        document.getElementsByTagName("head")[0].appendChild(link);
    }

    var count = 0,
        overlay = new ICEOverlay(),
        $document = $(document),
        $window = $(window);

    function initICETarget(elem) {

        var $elem = $(elem),
            position = $elem.offset(),
            iceRef = $elem.data('studioIce') + '-' + count++;

        $elem.attr('data-studio-ice-target', iceRef);

        $('<i class="crafter-studio-ice-indicator" data-studio-ice-trigger="%@"></i>'.fmt(iceRef)).css({
            top: position.top,
            left: position.left
        }).appendTo('body');

    }

    function initICERegions() {
        $('.crafter-studio-ice-indicator').remove();
        var elems = document.querySelectorAll('[data-studio-ice]');
        for (var i = 0; i < elems.length; ++i) {
            initICETarget(elems[i]);
        }
    }

    /*$document.on('mouseover', '[data-studio-ice]', function (e) {

    });

    $document.on('mouseleave', '[data-studio-ice]', function () {

    });

    $document.on('mouseout', '.crafter-studio-ice-overlay', function () {

    });*/

    $document.on('mouseover', '.crafter-studio-ice-indicator', function (e) {

        var $i = $(this),
            $e = $('[data-studio-ice-target="%@"]'.fmt($i.data('studioIceTrigger'))),
            iceId = $e.data('studioIce');

        var position = $e.offset(),
            width = $e.width() - 4, // border-left-width + border-right-width = 4,
            height = $e.height() - 4, // border-top-width + border-bottom-width = 4
            props = {
                top: position.top,
                left: position.left,
                width: width,
                height: height
            };

        overlay.show(props);

    });

    $document.on('mouseout', '.crafter-studio-ice-indicator', function (e) {
        overlay.hide();
    });

    $document.on('click', '.crafter-studio-ice-indicator', function (e) {

        var $i = $(this),
            $e = $('[data-studio-ice-target="%@"]'.fmt($i.data('studioIceTrigger'))),
            iceId = $e.data('studioIce');

        var position = $e.offset(),
            props = {
                top: position.top,
                left: position.left,
                width: $e.width(),
                height: $e.height()
            };

        // overlay.show(props);

        props.iceId = iceId;
        props.scrollTop = $window.scrollTop();
        props.scrollLeft = $window.scrollLeft();

        communicator.publish(Events.ICE_ZONE_ON, props);

    });

    $window.resize(function () {
        initICERegions();
    });

    loadCss('/studio/static-assets/styles/studio-guest.css');
    initICERegions();

});
