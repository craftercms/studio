/**
 * Created by rart on 18/02/15.
 */
(window.define || function (a, b, f) {
    f(crafter, $);
})('animator', ['crafter', 'jquery'], function (crafter, $) {

    var ANIMATE_CLASS = 'animated',
        END_EVENT = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';

    var BOUNCE = { enter: 'bounceIn', exit: 'bounceOut'},
        FADE = { enter: 'fadeIn', exit: 'fadeOut'},
        ZOOM = { enter: 'zoomIn', exit: 'zoomOut'},
        SLIDE_LEFT = { enter: 'slideInLeft', exit: 'slideOutLeft'},
        SLIDE_RIGHT = { enter: 'slideInRight', exit: 'slideOutRight'},
        SLIDE_UP = { enter: 'slideInUp', exit: 'slideOutUp'},
        SLIDE_DOWN = { enter: 'slideInDown', exit: 'slideOutDown'};

    function Animator(element) {
        var $element = $(element).addClass(ANIMATE_CLASS);
        this.$el = function (el) {
            if (el) $element = $(el).addClass(ANIMATE_CLASS);
            return $element;
        };
    }

    Animator.prototype = {
        fadeIn: function (callback) {
            enter.call(this, FADE, callback);
        },
        fadeOut: function (callback)  {
            exit.call(this, FADE, callback);
        },
        bounceIn: function (callback) {
            enter.call(this, BOUNCE, callback);
        },
        bounceOut: function (callback)  {
            exit.call(this, BOUNCE, callback);
        },
        zoomIn: function (callback) {
            enter.call(this, ZOOM, callback);
        },
        zoomOut: function (callback)  {
            exit.call(this, ZOOM, callback);
        },
        slideIn: function (callback) {
            enter.call(this, SLIDE_LEFT, callback);
        },
        slideOut: function (callback) {
            exit.call(this, SLIDE_LEFT, callback);
        },
        slideInRight: function (callback) {
            enter.call(this, SLIDE_RIGHT, callback);
        },
        slideOutRight: function (callback) {
            exit.call(this, SLIDE_RIGHT, callback);
        },
        slideInUp: function (callback) {
            enter.call(this, SLIDE_UP, callback);
        },
        slideOutDown: function (callback) {
            enter.call(this, SLIDE_DOWN, callback);
        },
        slideInDown: function (callback) {
            enter.call(this, SLIDE_DOWN, callback);
        },
        slideOutUp: function (callback) {
            enter.call(this, SLIDE_UP, callback);
        }
    };

    crafter.studio.define('Animator', Animator);

    return Animator;

    function enter(effect, callback) {
        var $element = this.$el();
        $element
            .show()
            .removeClass(effect.enter)
            .removeClass(effect.exit)
            .addClass(effect.enter)
            .one(END_EVENT, function () {
                $element.removeClass(effect.enter);
                callback && callback($element);
            });
    }

    function exit(effect, callback) {
        var $element = this.$el();
        $element
            .removeClass(effect.enter)
            .removeClass(effect.exit)
            .addClass(effect.exit)
            .one(END_EVENT, function () {
                if (!$element.hasClass(effect.enter)) $element.hide();
                $element.removeClass(effect.exit);
                callback && callback($element);
            });
    }

});