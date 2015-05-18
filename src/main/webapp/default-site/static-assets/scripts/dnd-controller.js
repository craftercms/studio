define('dnd-controller', ['crafter', 'jquery', 'jquery-ui', 'animator', 'communicator'], function (crafter, $, $ui, Animator, Communicator) {
    'use strict';

    var Topics = crafter.studio.preview.Topics,
        string = crafter.String;

    var OVERLAY_TPL = '<sdiv class="studio-dnd-controller-overlay"></sdiv>';
    var PALETTE_TPL = [
        '<sdiv class="studio-components-panel">',
        '<sbutton data-action="done">done</sbutton>',
        '<sh1 class="studio-panel-title">Components</sh1>',
        '<sdiv class="studio-component-search"><input type="search" placeholder="search components..." /></sdiv>',
        '<sdiv class="studio-components-container"></sdiv>',
        '</sdiv>'].join('');
    var COMPONENT_TPL = '<sli><sa class="studio-component-drag-target" data-studio-component-path="%@" data-studio-component-type="%@">%@</sa></sli>';
    var DRAGGABLE_SELECTION = '.studio-components-container .studio-component-drag-target';
    var DROPPABLE_SELECTION = '[data-studio-components-target]';
    var PANEL_ON_BD_CLASS = 'studio-dnd-enabled';

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

        $palette.on('click', '.studio-category-name', function () {
            $(this).parent().toggleClass('studio-collapse');
        });

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

    function findZIndex() {
        var highest = -999;
        $("*").each(function() {
            var current = parseInt($(this).css("z-index"), 10);
            if(current && highest < current) highest = current;
        });
        return highest;
    }

    function disableDnD() {

        if (!this.active()) return;
        this.active(false);

        $(DRAGGABLE_SELECTION).draggable('destroy');
        $(DROPPABLE_SELECTION).droppable('destroy').sortable('destroy');
        $body.removeClass(PANEL_ON_BD_CLASS);

        var $p = this.getPalette(),
            $o = this.getOverlay();

        this.getAnimator($o).fadeOut();
        this.getAnimator($p).slideOutRight(function () {
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

        $body.addClass(PANEL_ON_BD_CLASS);

        $o.appendTo($body);
        $p.appendTo($body);
        resize.call(this);

        renderPalette.call(this, components);

        this.getAnimator($o).fadeIn();
        this.getAnimator($p).slideInRight();

        $(DRAGGABLE_SELECTION).draggable({
            revert: 'invalid',
            helper: 'clone',
            appendTo: 'body',
            zIndex: 1030
        });
        $(DROPPABLE_SELECTION).droppable({
            connectWithSortable: true,
            drop: function (e, ui) {
                var $dropZone = $(this),
                    $component = ui.draggable,
                    path, type, name;
                if ($component.hasClass('studio-component-drag-target')) {
                    path = $component.attr('data-studio-component-path');
                    type = $component.attr('data-studio-component-type');
                    name = $component.html();
                    $dropZone.append(
                        string('<div data-studio-component="%@" data-studio-component-path="%@">%@</div>')
                            .fmt(type, path, name));
                } else {
                    path = $component.attr('data-studio-component-path');
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
            $c = this.getPalette().children('.studio-components-container');
        $.each(components || [], function (i, category) {
            html.push('<sdiv class="studio-category">');
            html.push('<sh2 class="studio-category-name">'+category.label+'</sh2>');
            html.push('<sul>');
            $.each(category.components, function (j, component) {
                html.push(crafter.String(COMPONENT_TPL).fmt(component.path, component.type, component.label));
            });
            html.push('</sul>');
            html.push('</sdiv>');
        });
        $c.html(html.join(''));
    }

});