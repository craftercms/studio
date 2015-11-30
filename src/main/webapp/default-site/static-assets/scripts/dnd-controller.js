define('dnd-controller', ['crafter', 'jquery', 'jquery-ui', 'animator', 'communicator'], function (crafter, $, $ui, Animator, Communicator) {
    'use strict';

    var Topics = crafter.studio.preview.Topics,
        string = crafter.String;

    var OVERLAY_TPL = '<sdiv class="studio-dnd-controller-overlay"></sdiv>';
    var PALETTE_TPL = [
        '<sdiv class="studio-components-panel">',
        '<sbutton class="btn btn-primary" data-action="done" data-translation="done">Done</sbutton>',
        '<sh1 class="studio-panel-title" data-translation="components">Components</sh1>',
        '<sdiv class="studio-component-search"><input type="search" placeholder="search components..." /></sdiv>',
        '<sdiv class="studio-components-container"></sdiv>',
        '</sdiv>'].join('');
    var COMPONENT_TPL = '<sli><sa class="studio-component-drag-target" data-studio-component data-studio-component-path="%@" data-studio-component-type="%@"><span class="status-icon component"></span>%@</sa></sli>';
    var DRAGGABLE_SELECTION = '.studio-components-container .studio-component-drag-target';
    var DROPPABLE_SELECTION = '[data-studio-components-target]';
    var PANEL_ON_BD_CLASS = 'studio-dnd-enabled';
    var DROPPABLE_SELECTION_SIZE = '[data-studio-components-size]';

    var $body       = $('body:first');
    var $document   = $(document);
    var $window     = $(window);
    var found = {};
    var componentsModelLoadPath;


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
            communicator.on(Topics.DND_COMPONENT_MODEL_LOAD, function (data) {
                componentModelLoad.call(me, data.trackingNumber, data.model);
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
        $(DROPPABLE_SELECTION).sortable('destroy');
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
        var iceOn = !!(sessionStorage.getItem('ice-on'));
        amplify.publish(Topics.ICE_TOOLS_OFF);
        this.stop();
        publish.call(this, Topics.STOP_DRAG_AND_DROP);
        if(iceOn){
            setTimeout(function(){ amplify.publish(Topics.ICE_TOOLS_ON); }, 430);
        }
    }

    function enableDnD(components, initialComponentModel) {
        amplify.publish(Topics.ICE_TOOLS_OFF);
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

        $("[data-studio-components-size='small']").each(function( index ) {
            $(this).width($(this).width()/2);
        });

        $(DRAGGABLE_SELECTION).draggable({
            revert: 'invalid',
            helper: 'clone',
            appendTo: 'body',
            cursor: 'move',
            connectToSortable: DROPPABLE_SELECTION,
            zIndex: 1030
        });

        $(DROPPABLE_SELECTION).sortable({
            me: this,
            items: '[data-studio-component]',
            cursor: 'move',
            forceHelperSize: true,
            forcePlaceholderSize: true,
            greedy: true,
            connectWith: DROPPABLE_SELECTION,
            hoverClass: 'studio-draggable-over',
            over: function( event, ui ) {
                $(this).addClass('studio-draggable-over');
            },
            out: function( event, ui ) {
                $(this).removeClass('studio-draggable-over');
            },
            start: function( event, ui ) {
                ui.item.addClass('studio-component-over');
            },
            stop: function( event, ui ) {
                ui.item.removeClass('studio-component-over');
            },
            update: function (e, ui) {
                var $dropZone = $(this),
                    $component = ui.item,
                    compPath = $component.attr('data-studio-component-path'),
                    zonePath = $dropZone.parents('[data-studio-component-path="' + compPath + '"]').attr('data-studio-component-path'),
                    orgZoneComp = ui.item.parents('[data-studio-components-target]').parents('[data-studio-component-path]'),
                    destZoneComp = $dropZone.parents('[data-studio-component-path]');
                if (compPath != zonePath && ((orgZoneComp.attr('data-studio-component-path') != destZoneComp.attr('data-studio-component-path') ||
                    (orgZoneComp.attr('data-studio-component-path') == destZoneComp.attr('data-studio-component-path') &&
                        $dropZone.attr('data-studio-components-objectid') != ui.item.parents('[data-studio-components-target]').attr('data-studio-components-objectid')) ||
                    (orgZoneComp.attr('data-studio-component-path') == destZoneComp.attr('data-studio-component-path') &&
                        orgZoneComp.attr('data-studio-tracking-number') == destZoneComp.attr('data-studio-tracking-number') &&
                        $dropZone.attr('data-studio-components-objectid') == ui.item.parents('[data-studio-components-target]').attr('data-studio-components-objectid'))))) {
                    componentDropped.call(me, $dropZone, $component);
                } else {
                    $(DROPPABLE_SELECTION).sortable("cancel");
                }

            }
        });

        $('[data-studio-component]').each(function () {
            $(this).attr('data-studio-tracking-number', crafter.guid());
        });

        $('[data-studio-components-target]').each(function (i) {
            var $me = $(this);
            $me.attr('data-studio-zone-tracking', crafter.guid());
            //$me.attr('data-studio-components-target', i + '_' + $me.attr('data-studio-components-target'));
        });

        componentsModelLoad.call(me, initialComponentModel);

        $( ".ui-sortable-handle" ).each(function( index ) {
            var delControl = createDeleteControl('removeComp');
            delControl.onclick = function() {
                var compPath = $(this).parent().parents('[data-studio-component-path]').attr('data-studio-component-path');
                var objectId = $(this).parent().parents('[data-studio-components-target]').attr('data-studio-components-objectid');
                var compTracking = $(this).parent().parents('[data-studio-component-path]').attr('data-studio-tracking-number');
                var dropName = $($(this).parent().parents('[data-studio-components-target]')[0]).attr('data-studio-components-target');
                removeComponent(this, function () {
                    var zones = {};
                    var conRepeat = 0;
                    setTimeout(function () {

                        $('[data-studio-components-target]').each(function () {
                            if(objectId == $(this).attr('data-studio-components-objectid')){
                                if(dropName == $(this).attr('data-studio-components-target')){
                                    conRepeat++;
                                }
                                if( compTracking == $(this).parents('[data-studio-component-path]').attr('data-studio-tracking-number')) {
                                    var $el = $(this),
                                        zoneName = $el.attr('data-studio-components-target');
                                    zones[zoneName] = [];
                                    $el.find('> [data-studio-component]').each(function (i, el) {
                                        var $comp = $(this);
                                        zones[zoneName].push($comp.data('model') || tracking);
                                    });
                                }
                            }
                        });

                        publish.call(me, Topics.SAVE_DRAG_AND_DROP, {
                            isNew: false,
                            zones: zones,
                            compPath: compPath,
                            conComp: (conRepeat > 1) ? true : false
                        });

                    });
                });
            };
            $( this ).append(delControl);
        });

        var iceOn = !!(sessionStorage.getItem('ice-on'));
        if(iceOn){
            setTimeout(function(){ amplify.publish(Topics.ICE_TOOLS_ON); }, 400);
        }

    }

    function componentDropped($dropZone, $component) {

        var iceOn = !!(sessionStorage.getItem('ice-on'));
        var compPath = $dropZone.parents('[data-studio-component-path]').attr('data-studio-component-path');
        var compTracking = $dropZone.parents('[data-studio-component-path]').attr('data-studio-tracking-number');
        var objectId = $dropZone.attr('data-studio-components-objectid');
        var trackingZone = $dropZone.attr('data-studio-zone-tracking');
        var dropName = $dropZone.attr('data-studio-components-target');

        if(iceOn){
            amplify.publish(Topics.ICE_TOOLS_ON);
        }

        var me = this,
            isNew = $component.hasClass('studio-component-drag-target'),
            tracking, path, type, name, zones = {};

        if (isNew) {
            path = $component.attr('data-studio-component-path');
            type = $component.attr('data-studio-component-type');
            name = $component.text();
            tracking = crafter.guid();
            $component.before(
                string('<div data-studio-component="%@" data-studio-component-path="%@" data-studio-tracking-number="%@">%@</div>')
                    .fmt(type, path, tracking, name));
            $component.remove();
        } else {
            tracking = $component.attr('data-studio-tracking-number');
            path = $component.attr('data-studio-component-path');
            type = $component.attr('data-studio-component');
        }

        // DOM Reorganization hasn't happened at this point,
        // need a timeout to grab out the updated DOM structure
        var conRepeat = 0;
        setTimeout(function () {

            $('[data-studio-components-target]').each(function () {
                if(objectId == $(this).attr('data-studio-components-objectid')){
                    if(dropName == $(this).attr('data-studio-components-target')){
                        conRepeat++;
                    }
                    if( compTracking == $(this).parents('[data-studio-component-path]').attr('data-studio-tracking-number')) {
                        var $el = $(this),
                            zoneName = $el.attr('data-studio-components-target');
                        zones[zoneName] = [];
                        $el.find('> [data-studio-component]').each(function (i, el) {
                            var $comp = $(this);
                            zones[zoneName].push($comp.data('model') || tracking);
                        });
                    }
                }
            });

            publish.call(me, Topics.COMPONENT_DROPPED, {
                path: path,
                type: type,
                isNew: isNew,
                zones: zones,
                trackingNumber: tracking,
                compPath: compPath,
                conComp: (conRepeat > 1) ? true : false
            });

        });
    }

    function componentModelLoad(tracking, data) {
        $('[data-studio-tracking-number="'+tracking+'"]').data('model', data);
    }

    function componentsModelLoad(data) {
        var aNotFound = [];
        var me = this;
        $('[data-studio-components-target]').each(function () {
            var $el = $(this),
                //target = $el.attr('data-studio-components-target').split('_'),
                //name = target[1],
                objectId = $el.attr('data-studio-components-objectid'),
                tracking = $el.attr('data-studio-zone-tracking'),
                name = $el.attr('data-studio-components-target'),
                path = $el.parents('[data-studio-component-path]').attr('data-studio-component-path'),
                id = objectId + "-" + name;
            if(!found[id] || objectId == data['objectId']){
                if ((data[name] || data[name] == "") && objectId == data['objectId']) { ///objid?
                    found[id] = true;
                    $el.find('> [data-studio-component]').each(function (i, el) {
                        $(this).data('model', data[name][i]);
                    });
                } else {
                    var repeated = false;
                    for(var j=0; j<aNotFound.length ; j++){
                        if(aNotFound[j].path == path && aNotFound[j].name == name){
                            repeated = true;
                        }
                    }
                    if(!repeated){
                        aNotFound.push({path: path, name:name});
                    }
                }
            }
        });
        if(aNotFound.length){
            publish.call(this, Topics.DND_ZONES_MODEL_REQUEST, {
                aNotFound: aNotFound[0]
            });
        }
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
            if(category.components.length){
                $.each(category.components, function (j, component) {
                    html.push(crafter.String(COMPONENT_TPL)
                        .fmt(component.path, component.type, component.label));
                });
            }else{
                html.push(crafter.String(COMPONENT_TPL)
                    .fmt(category.components.path, category.components.type, category.components.label));
            }
            html.push('</sul>');
            html.push('</sdiv>');
        });
        html.push('<button class="btn btn-primary add-component" data-translation="addComponent">Add Component</button>');
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