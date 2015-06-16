define('dnd-controller', ['crafter', 'jquery', 'jquery-ui', 'animator', 'communicator'], function (crafter, $, $ui, Animator, Communicator) {
    'use strict';

    var Topics = crafter.studio.preview.Topics,
        string = crafter.String;

    var OVERLAY_TPL = '<sdiv class="studio-dnd-controller-overlay"></sdiv>';
    var PALETTE_TPL = [
        '<sdiv class="studio-components-panel">',
        '<sbutton class="btn btn-primary" data-action="done">Done</sbutton>',
        '<sh1 class="studio-panel-title">Components</sh1>',
        '<sdiv class="studio-component-search"><input type="search" placeholder="search components..." /></sdiv>',
        '<sdiv class="studio-components-container"></sdiv>',
        '</sdiv>'].join('');
    var COMPONENT_TPL = '<sli><sa class="studio-component-drag-target" data-studio-component data-studio-component-path="%@" data-studio-component-type="%@"><span class="status-icon component"></span>%@</sa></sli>';
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
            communicator = config.communicator,
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

        // TODO currently not in use.
        // component-panel.js loads from page load rather than when enabling dnd
        // hence the page model loads from page load too.
        if (communicator) {
            communicator.on(Topics.DND_COMPONENT_MODEL_LOAD, function (tracking, data) {
                componentModelLoad.call(me, tracking, data);
            });
            communicator.on(Topics.DND_COMPONENTS_MODEL_LOAD, function (data) {
                componentsModelLoad.call(me, data);
            });
        }

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

        sessionStorage.setItem('components-on', '');

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

        $('.removeComp').remove();

    }

    function done() {
        this.stop();
        publish.call(this, Topics.STOP_DRAG_AND_DROP);
    }

    function enableDnD(components, initialComponentModel) {
        sessionStorage.setItem('components-on', 'true');

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
            hoverClass: 'studio-draggable-over',
            connectWithSortable: true,
            drop: function (e, ui) {
                var $dropZone = $(this),
                    $component = ui.draggable;
                componentDropped.call(me, $dropZone, $component);
            }
        }).sortable({
            items: '[data-studio-component]',
            connectWith: DROPPABLE_SELECTION
        });

        $('[data-studio-component]').each(function () {
            $(this).attr('data-studio-tracking-number', crafter.guid());
        });

        componentsModelLoad(initialComponentModel);

        //$('.ui-sortable-handle').append('<a class="removeComp"><img src="/studio/static-assets/themes/cstudioTheme/images/icons/delete.png" /></a>');

        $( ".ui-sortable-handle" ).each(function( index ) {
            //$( this ).append('<a class="removeComp"><img src="/studio/static-assets/themes/cstudioTheme/images/icons/delete.png" /></a>');
            var delControl = createDeleteControl('removeComp');
            delControl.onclick = function() {
                removeComponent(this, function () {
                    //CStudioAuthoring.DamPanel.getPageModel(CStudioAuthoring.DamPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath), "save-components", true, false);
                    //window.location.reload();
                    //publish.call(me, Topics.SAVE_DRAG_AND_DROP, {isNew : true});
                    var zones = {};
                    setTimeout(function () {

                        $('[data-studio-components-target]').each(function () {
                            var $el = $(this),
                                zoneName = $el.attr('data-studio-components-target');
                            zones[zoneName] = [];
                            $el.find('[data-studio-component]').each(function (i, el) {
                                var $comp = $(this);
                                zones[zoneName].push($comp.data('model') || tracking);
                            });
                        });

                        publish.call(me, Topics.SAVE_DRAG_AND_DROP, {
                            isNew: 'false',
                            zones: zones
                        });

                    });
                });
            };
            $( this ).append(delControl);
        });

    }

    function componentDropped($dropZone, $component) {

        var me = this,
            isNew = $component.hasClass('studio-component-drag-target'),
            tracking, path, type, name, zones = {};

        if (isNew) {
            path = $component.attr('data-studio-component-path');
            type = $component.attr('data-studio-component-type');
            name = $component.text();
            tracking = crafter.guid();
            $dropZone.append(
                string('<div data-studio-component="%@" data-studio-component-path="%@" data-studio-tracking-number="%@">%@</div>')
                    .fmt(type, path, tracking, name));
        } else {
            tracking = $component.attr('data-studio-tracking-number');
            path = $component.attr('data-studio-component-path');
            type = $component.attr('data-studio-component');
        }

        // DOM Reorganization hasn't happened at this point,
        // need a timeout to grab out the updated DOM structure
        setTimeout(function () {

            $('[data-studio-components-target]').each(function () {
                var $el = $(this),
                    zoneName = $el.attr('data-studio-components-target');
                zones[zoneName] = [];
                $el.find('[data-studio-component]').each(function (i, el) {
                    var $comp = $(this);
                    zones[zoneName].push($comp.data('model') || tracking);
                });
            });

            publish.call(me, Topics.COMPONENT_DROPPED, {
                path: path,
                type: type,
                isNew: isNew,
                zones: zones,
                trackingNumber: tracking
            });

        });
    }

    function componentModelLoad(tracking, data) {
        $('[data-studio-tracking-number="'+tracking+'"]').data('model', data);
    }

    function componentsModelLoad(data) {
        $('[data-studio-components-target]').each(function () {
            var $el = $(this), name = $el.attr('data-studio-components-target');
            $el.find('[data-studio-component]').each(function (i, el) {
                $(this).data('model', data[name][i]);
            });
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
        console.log(components);
        var html = [],
            $c = this.getPalette().children('.studio-components-container');
        $.each(components || [], function (i, category) {
            html.push('<sdiv class="studio-category">');
            html.push('<sh2 class="studio-category-name">'+category.label+'</sh2>');
            html.push('<sul>');
            $.each(category.components, function (j, component) {
                html.push(crafter.String(COMPONENT_TPL)
                    .fmt(component.path, component.type, component.label));
            });
            html.push('</sul>');
            html.push('</sdiv>');
        });
        html.push('<button class="btn btn-primary add-component">Add Component</button>');
        $c.html(html.join(''));
    }

    function createDeleteControl(className) {
        var deleteEl = document.createElement("a"),
            btnEl = document.createElement("img");

        $( deleteEl).addClass(className);

        btnEl.src = "/studio/static-assets/themes/cstudioTheme/images/icons/delete.png";
        btnEl.style.width = "16px";
        btnEl.style.height = "16px";

        deleteEl.appendChild(btnEl);
        return deleteEl;
    }

    function removeComponent (srcEl, callback) {

        srcEl.parentNode.remove();

        //Utility.refreshPlaceholderHeight(srcContainer);

        if (typeof callback == "function") {
            callback();
        }
    }

});