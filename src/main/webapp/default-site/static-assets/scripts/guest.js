define('guest', ['crafter', 'jquery', 'communicator', 'ice-overlay', 'dnd-controller'], function (crafter, $, Communicator, ICEOverlay, DnDController, CStudioAuthoring) {
    'use strict';

    $.noConflict(true);

    if (!window.location.origin) {
        window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
    }

    var origin = window.location.origin; // 'http://127.0.0.1:8080';
    var Topics = crafter.studio.preview.Topics;
    var communicator = new Communicator({window: window.parent, origin: origin}, origin);

    // When the page has successfully loaded, notify the host window of it's readiness
    communicator.publish(Topics.GUEST_SITE_LOAD, {
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
        $window = $(window),
        dndController;

    communicator.on(Topics.START_DRAG_AND_DROP, function (message) {
        require(['dnd-controller'], function (DnDController) {

            (typeof dndController === 'undefined') && (dndController = new DnDController({
                communicator: communicator
            }));

            dndController.start(message.components, message.contentModel);

            var translation = message.translation;
            var elements = $('[data-translation]');
            elements.each(function() {
                if($( this ).attr( "data-translation" ) == "done")$( this ).html(translation.done);
                if($( this ).attr( "data-translation" ) == "components")$( this ).html(translation.components);
                if($( this ).attr( "data-translation" ) == "addComponent")$( this ).html(translation.addComponent);
            });

        });
    });

    communicator.on(Topics.REFRESH_PREVIEW, function (message) {
        window.location.reload();
    })

    communicator.on(Topics.ICE_TOOLS_OFF, function (message) {
        removeICERegions();
    });

    communicator.on(Topics.ICE_TOOLS_ON, function (message) {
        initICERegions();
    });

    communicator.on(Topics.ICE_TOOLS_REGIONS, function (message) {
        var elt = document.querySelectorAll('[data-studio-ice'+message.label+'=' + message.region + ']')[0];
        if(elt) {
            elt.scrollIntoView();
            window.scrollBy(0,-150);
            window.setTimeout(function() {
                initOverlay($(elt));
                window.setTimeout(function() {
                    overlay.hide();
                }, 1000);
            }, 500);
        } else {
            alert("Region " + message.region + " could not be found");
        }
    });

    function setRegionsCookie() {
        sessionStorage.setItem("ice-tools-content", '');
        var elts = document.querySelectorAll('[data-studio-ice]'),
            regions = [];
        if (elts.length > 0) {
            for (var i = 0; i <= (elts.length) - 1; i++) {
                regions.push({id: elts[i].getAttribute('data-studio-ice'), formId: elts[i].getAttribute('data-studio-ice'), label: elts[i].getAttribute('data-studio-ice-label')});
            }
        }

        sessionStorage.setItem("ice-tools-content", JSON.stringify(regions));

    };

    function initICETarget(elem) {

        var $elem = $(elem),
            position = $elem.offset(),
            iceRef = $elem.data('studioIce') + '-' + count++;

        $elem.attr('data-studio-ice-target', iceRef);

        $(crafter.String('<i class="studio-ice-indicator" data-studio-ice-trigger="%@"></i>').fmt(iceRef)).css({
            top: position.top,
            left: position.left
        }).appendTo('body');

    }

    function initICERegions() {
        removeICERegions();
        var elems = document.querySelectorAll('[data-studio-ice]');

        for (var i = 0; i < elems.length; ++i) {
            initICETarget(elems[i]);
        }
    }

    function removeICERegions(){
        $('.studio-ice-indicator').remove();
    }

    function initOverlay(elt) {
        var position = elt.offset(),
            width = elt.width() - 4, // border-left-width + border-right-width = 4,
            height = elt.height() - 4, // border-top-width + border-bottom-width = 4
            props = {
                top: position.top,
                left: position.left,
                width: width,
                height: height
            };

        overlay.show(props);
    }

    $document.on('mouseover', '.studio-ice-indicator', function (e) {

        var $i = $(this),
            $e = $(crafter.String('[data-studio-ice-target="%@"]').fmt($i.data('studioIceTrigger'))),
            iceId = $e.data('studioIce');

        initOverlay($e);

    });

    $document.on('mouseout', '.studio-ice-indicator', function (e) {
        overlay.hide();
    });

    $document.on('click', '.studio-ice-indicator', function (e) {

        var $i = $(this);
        var $e = $(crafter.String('[data-studio-ice-target="%@"]').fmt($i.data('studioIceTrigger')));
        var iceId = $e.data('studioIce');
        var icePath = $e.data('studioIcePath');

        var position = $e.offset(),
            props = {
                top: position.top,
                left: position.left,
                width: $e.width(),
                height: $e.height()
            };

        props.iceId = iceId;
        props.itemId = icePath;
        props.scrollTop = $window.scrollTop();
        props.scrollLeft = $window.scrollLeft();

        communicator.publish(Topics.ICE_ZONE_ON, props);

    });

    $window.resize(function () {
        if (!!(sessionStorage.getItem('ice-on'))) {
            initICERegions();
        }
    });

    loadCss('/studio/static-assets/styles/guest.css');

    if (!!(sessionStorage.getItem('ice-on'))) {
        initICERegions();
    }

    setRegionsCookie();
    window.parent.initRegCookie();

});
