(function (window) {
    'use strict';

    function dasherize(str) {
        return str.replace(/::/g, '/')
            .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')
            .replace(/([a-z\d])([A-Z])/g, '$1_$2')
            .replace(/_/g, '-')
            .toLowerCase();
    }

    window.crafter = {
        studio: {
            define: function (packageName, component) {
                var root = crafter,
                    packages = ('studio.' + packageName).split('.'),
                    componentName = packages[packages.length-1];
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
                return ('crafter.studio.' + packageName);
            },
            preview: {
                Topics: {
                    "ALL": "*",
                    "GUEST_CHECKIN": "GUEST_CHECKIN",
                    "GUEST_CHECKOUT": "GUEST_CHECKOUT",
                    "GUEST_SITE_LOAD": "GUEST_SITE_LOAD",
                    "ICE_ZONE_ON": "ICE_ZONE_ON"
                }
            }
        }
    };

    String.prototype.fmt = function (/* fmt1, fmt2, fmt2 */) {
        var formats = Array.prototype.splice.call(arguments, 0),
            index  = 0;
        return (this.replace(/%@([0-9]+)?/g, function(s, argIndex) {

            argIndex = (argIndex) ? parseInt(argIndex, 10) - 1 : index++;
            (index >= formats.length) && (index = 0);

            s = formats[argIndex];
            return (s === null) ? '(null)' : s;

        }));
    };

}) (window);