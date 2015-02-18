define('ice-overlay', ['crafter', 'jquery'], function (crafter, $) {
    'use strict';

    var studio = crafter.studio,
        animationEndEvent = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend',
        preAnimationRemoveClasses = 'fadeIn fadeOut',
        fadeInClasses = 'fadeIn',
        fadeOutClasses = 'fadeOut';

    function ICEOverlay() {

        var $overlay = $('<div class="crafter-studio-ice-overlay" style="display: none;"></div>');
        $overlay.appendTo('body');

        this.getElement = function () {
            return $overlay;
        }

    }

    ICEOverlay.prototype = {
        show: showOverlay,
        hide: hideOverlay
    };

    function showOverlay(props) {
        this.getElement().css(props)
            .show()
            .removeClass(preAnimationRemoveClasses)
            .addClass(fadeInClasses)
            .one(animationEndEvent, function () {
                $(this).removeClass(fadeInClasses);
            });
    }

    function hideOverlay() {
        this.getElement()
            .removeClass(preAnimationRemoveClasses)
            .addClass(fadeOutClasses)
            .one(animationEndEvent, function () {
                var $el = $(this);
                if (!$el.hasClass(fadeInClasses)) $el.hide();
                $el.removeClass(fadeOutClasses);
            });
    }

    return ICEOverlay;

});