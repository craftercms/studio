define('dnd-controller', ['jquery', 'jquery-ui'], function ($, $ui) {
    'use strict';

    var OVERLAY_TPL = '<studio-element class="studio-dnd-controller-overlay"></studio-element>';
    var $bd = $('body:first');

    function DnDController() {

        var $overlay = $(OVERLAY_TPL);

        this.getOverlay = function () {
            return $overlay;
        }

    }

    DnDController.prototype = {
        start: enableComponentDnD,
        stop: disableComponentDnD
    };

    return DnDController;

    function enableComponentDnD() {
        $bd.addClass('studio-dnd-enabled');

        console.log($(window).width(), $bd.width())
        console.log($(window).height(), $bd.height())

        this.getOverlay()
            .css({
                width: $bd.width(),
                height: $bd.height()
            })
            .appendTo($bd)
            .addClass('fadeIn animated');
    }

    function disableComponentDnD() {
        $bd.removeClass('studio-dnd-enabled');
        this.getOverlay()
            .removeClass('fadeIn')
            .addClass('fadeOut');
    }

});