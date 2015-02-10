(function (window) {
    'use strict';

    window.crafter = {
        studio: {
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