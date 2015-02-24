define('ice-overlay', ['crafter', 'jquery', 'animator'], function (crafter, $, Animator) {
    'use strict';

    var studio = crafter.studio;

    function ICEOverlay() {

        var $overlay = $('<div class="studio-ice-overlay" style="display: none;"></div>');
        $overlay.appendTo('body');

        this.animator = new Animator($overlay);

        this.getElement = function () {
            return $overlay;
        }

    }

    ICEOverlay.prototype = {
        show: showOverlay,
        hide: hideOverlay
    };

    function showOverlay(props) {
        this.getElement().css(props);
        this.animator.fadeIn();
    }

    function hideOverlay() {
        this.animator.fadeOut();
    }

    return ICEOverlay;

});