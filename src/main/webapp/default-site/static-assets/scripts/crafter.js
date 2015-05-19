(function (window) {
    'use strict';

    var crafter = {};

    crafter.noop = function () {};

    crafter.define = function (packageName, component) {
        var root = crafter,
            packages = packageName.split('.'),
            componentName = packages[packages.length - 1];
        for (var i = 0, l = (packages.length - 1); i < l; ++i) {
            var pkg = packages[i];
            if (!root[pkg]) root[pkg] = {};
            root = root[pkg];
        }
        root[componentName] = component;
        if (typeof define === "function" && define.amd) {
            define(dasherize(componentName), [], function () {
                return component;
            });
        }
        return ('crafter.' + packageName);
    };

    crafter.String = CrafterString;

    crafter.studio = {
        define: function (packageName, component) {
            packageName = ('studio.' + packageName);
            return crafter.define(packageName, component);
        },
        preview: {
            Topics: {
                "ALL": "*",

                "GUEST_CHECKIN": "GUEST_CHECKIN",
                "GUEST_CHECKOUT": "GUEST_CHECKOUT",
                "GUEST_SITE_LOAD": "GUEST_SITE_LOAD",
                "CHANGE_GUEST_REQUEST": "CHANGE_GUEST_REQUEST",
                "ICE_ZONE_ON": "ICE_ZONE_ON",
                "START_DRAG_AND_DROP": "START_DRAG_AND_DROP",
                "STOP_DRAG_AND_DROP": "STOP_DRAG_AND_DROP",
                "COMPONENT_DROPPED": "COMPONENT_DROPPED",
                "": ""
            },
            cstopic: function (topic) {
                return (crafter.studio.preview.Topics[topic] + '_cstd');
            }
        }
    };

    function CrafterString(string) {
        if (!(this instanceof CrafterString)) {
            return new CrafterString(string);
        }
        this.string = string;
    };

    CrafterString.prototype = {
        fmt: function (/* fmt1, fmt2, fmt2 */) {
            var index  = 0, formats = Array.prototype.splice.call(arguments, 0);
            return (this.string.replace(/%@([0-9]+)?/g, function(s, argIndex) {
                argIndex = (argIndex) ? parseInt(argIndex, 10) - 1 : index++;
                (index >= formats.length) && (index = 0);
                s = formats[argIndex];
                return (s === null) ? '(null)' : s;
            }));
        }
    };

    function dasherize(str) {
        return str.replace(/::/g, '/')
            .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')
            .replace(/([a-z\d])([A-Z])/g, '$1_$2')
            .replace(/_/g, '-')
            .toLowerCase();
    }

    if (typeof define === "function" && define.amd) {
        define('crafter', [], function () {
            return crafter;
        });
    } else {
        window.crafter = crafter;
    }

}) (window);