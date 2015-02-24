define('dnd-controller', ['crafter', 'jquery', 'jquery-ui', 'animator', 'communicator'], function (crafter, $, $ui, Animator, Communicator) {
    'use strict';

    var Topics = crafter.studio.preview.Topics,
        string = crafter.String;

    var OVERLAY_TPL = '<studio-element class="studio-dnd-controller-overlay"></studio-element>';
    var PALETTE_TPL = '<studio-components class="studio-view-scope"><div class="studio-row"></div><div class="studio-row"><div class="studio-column studio-small-12"><button class="studio-btn" data-action="done">Done</button></div></div></studio-components>';
    var COMPONENT_TPL = '<div class="studio-medium-3 studio-column"><div class="studio-component-drag-target" data-studio-component-path="%@" data-studio-component-type="%@"><h3>%@</h3></div></div>';
    var DRAGGABLE_SELECTION = 'studio-components .studio-component-drag-target';
    var DROPPABLE_SELECTION = '[data-studio-components-target]';

    var $body       = $('body:first');
    var $document   = $(document);
    var $window     = $(window);

    function DnDController(config) {

        var $overlay = $(OVERLAY_TPL),
            $palette = $(PALETTE_TPL),
            animator = new Animator(),
            config = config || {},
            active = false,
            me = this,
            timeout;

        $palette.on('click', '[data-action]', function (e) {
            e.stopPropagation();
            me[$(this).data('action')]();
        });

        this.active = function (value) {
            if (arguments.length) {
                (active = !!value);
                (active) ? $window.resize(onresize) : $window.unbind('resize', onresize);
            }
            return active;
        };

        this.cfg = function (property, value) {
            if (arguments.length > 1) config[property] = value;
            return config[property];
        };

        this.getAnimator = function ($el) {
            $el && animator.$el($el);
            return animator;
        };

        this.getOverlay = function () {
            return $overlay;
        };

        this.getPalette = function () {
            return $palette;
        };

        function onresize() {
            clearTimeout(timeout);
            timeout = setTimeout(function () {
                resize.call(me);
            }, 300);
        }

    }

    DnDController.prototype = {
        start: enableDnD,
        stop: disableDnD,
        done: done
    };

    return DnDController;

    function disableDnD() {

        if (!this.active()) return;
        this.active(false);

        $(DRAGGABLE_SELECTION).draggable('destroy');
        $(DROPPABLE_SELECTION).droppable('destroy').sortable('destroy');
        $body.removeClass('studio-dnd-enabled');

        var $p = this.getPalette(),
            $o = this.getOverlay();

        this.getAnimator($o).fadeOut();
        this.getAnimator($p).zoomOut(function () {
            $o.detach();
            $p.detach();
        });

    }

    function done() {
        this.stop();
        publish.call(this, Topics.STOP_DRAG_AND_DROP);
    }

    function enableDnD(components) {

        if (this.active()) return;
        this.active(true);

        var $p = this.getPalette(),
            $o = this.getOverlay(),
            me = this;

        $body.addClass('studio-dnd-enabled');

        $o.appendTo($body);
        $p.appendTo($body);
        resize.call(this);

        renderPalette.call(this, components);

        this.getAnimator($o).fadeIn();
        this.getAnimator($p).zoomIn();

        $(DRAGGABLE_SELECTION).draggable({
            revert: 'invalid',
            helper: 'clone'
        });
        $(DROPPABLE_SELECTION).droppable({
            connectWithSortable: true,
            drop: function (e, ui) {
                var $dropZone = $(this),
                    $component = ui.draggable,
                    path, type, name;
                if ($component.hasClass('studio-component-drag-target')) {
                    path = $component.attr('data-studio-component-path'),
                    type = $component.attr('data-studio-component-type'),
                    name = $component.html();
                    $dropZone.append(string('<div data-studio-component="%@" data-studio-component-path="%@">%@</div>')
                        .fmt(type, path, name));
                } else {
                    path = $component.attr('data-studio-component-path'),
                    type = $component.attr('data-studio-component');
                }
                publish.call(me, Topics.COMPONENT_DROPPED, {
                    type: type, path: path
                });
            }
        }).sortable({
            items: '[data-studio-component]'
        });

    }

    function publish(topic, message, com) {
        if (com = this.cfg('communicator')) {
            com.publish(topic, message);
        }
    }

    function resize() {
        this.getOverlay().css({
            width: $document.width(),
            height: $document.height()
        });
    }

    function renderPalette(components) {
        var html = [],
            $c = this.getPalette().children(':first');
        $.each(components || [], function (i, comp) {
            html.push(crafter.String(COMPONENT_TPL).fmt(comp.path, comp.type, comp.label));
        });
        $c.html(html.join(''));
    }

});