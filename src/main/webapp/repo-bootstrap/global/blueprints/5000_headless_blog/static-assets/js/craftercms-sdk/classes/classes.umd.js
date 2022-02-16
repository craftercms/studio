/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('rxjs'), require('rxjs/operators'), require('@craftercms/utils'), require('@craftercms/models'), require('rxjs/ajax')) :
    typeof define === 'function' && define.amd ? define('@craftercms/classes', ['exports', 'rxjs', 'rxjs/operators', '@craftercms/utils', '@craftercms/models', 'rxjs/ajax'], factory) :
    (global = global || self, factory((global.craftercms = global.craftercms || {}, global.craftercms.classes = {}), global.rxjs, global.rxjs.operators, global.craftercms.utils, global.craftercms.models, global.rxjs.ajax));
}(this, (function (exports, rxjs, operators, utils, models, ajax) { 'use strict';

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU General Public License version 3 as published by
     * the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License
     * along with this program.  If not, see <http://www.gnu.org/licenses/>.
     */

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    var Messenger = /** @class */ (function () {
        function Messenger() {
            var _this = this;
            this.targets = [];
            this.origins = [];
            var multiCaster = new rxjs.Subject(), messages = rxjs.fromEvent(window, 'message')
                .pipe(operators.tap(function (event) {
                return !_this.originAllowed(event.origin) &&
                    console.log('Messenger: Message received from a disallowed origin.', event);
            }), operators.filter(function (event) {
                return _this.originAllowed(event.origin) &&
                    (typeof event.data === 'object') &&
                    ('topic' in event.data) &&
                    ('data' in event.data) &&
                    ('scope' in event.data);
            }), operators.map(function (event) { return ({
                topic: event.data.topic,
                data: event.data.data,
                scope: event.data.scope || models.MessageScope.Broadcast
            }); }), operators.multicast(multiCaster), operators.refCount());
            this.messages$ = messages;
            this.multiCaster$ = multiCaster;
        }
        Messenger.prototype.subscribe = function (observerOrNext) {
            var operators = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                operators[_i - 1] = arguments[_i];
            }
            return this.messages$
                .pipe.apply(this.messages$, operators)
                .subscribe(observerOrNext);
        };
        Messenger.prototype.subscribeTo = function (topic, subscriber, scope) {
            var operations = [];
            for (var _i = 3; _i < arguments.length; _i++) {
                operations[_i - 3] = arguments[_i];
            }
            var ops = [];
            // operations = operations || [];
            if (!utils.notNullOrUndefined(scope)) {
                ops.push(operators.filter(function (message) {
                    return message.scope === scope && message.topic === topic;
                }));
            }
            else {
                ops.push(operators.filter(function (message) { return message.topic === topic; }));
            }
            return this.messages$
                .pipe.apply(this.messages$, ops.concat(operations))
                .subscribe(subscriber);
        };
        Messenger.prototype.addTarget = function (target) {
            this.removeTarget(target);
            this.targets.push(target);
        };
        Messenger.prototype.resetTargets = function () {
            this.targets = [];
        };
        Messenger.prototype.removeTarget = function (target) {
            this.targets = this.targets.filter(function (item) {
                return item !== target;
            });
        };
        Messenger.prototype.addOrigin = function (origin) {
            this.removeOrigin(origin);
            this.origins.push(origin);
        };
        Messenger.prototype.resetOrigins = function () {
            this.origins = [];
        };
        Messenger.prototype.removeOrigin = function (origin) {
            this.origins = this.origins.filter(function (item) {
                return item !== origin;
            });
        };
        Messenger.prototype.publish = function (topicOrMessage, data, scope) {
            if (data === void 0) { data = null; }
            if (scope === void 0) { scope = models.MessageScope.Broadcast; }
            var message;
            if (typeof topicOrMessage === 'string' && topicOrMessage in models.MessageTopic) {
                message = {
                    topic: topicOrMessage,
                    data: data,
                    scope: scope
                };
            }
            else {
                message = topicOrMessage;
            }
            switch (scope) {
                case models.MessageScope.Local:
                    this.multiCaster$.next(message);
                    break;
                case models.MessageScope.External:
                    this.sendMessage(message);
                    break;
                case models.MessageScope.Broadcast:
                    this.multiCaster$.next(message);
                    this.sendMessage(message);
                    break;
            }
        };
        Messenger.prototype.sendMessage = function (message, targetOrigin) {
            var _this = this;
            if (targetOrigin === void 0) { targetOrigin = '*'; }
            this.targets.forEach(function (target) {
                // TODO need to determine where to get the origin
                if (!target.postMessage) {
                    target = target.contentWindow;
                }
                if (!target || !target.postMessage) {
                    // Garbage collection: get rid of any windows that no longer exist.
                    _this.removeTarget(target);
                }
                else {
                    target.postMessage(message, targetOrigin);
                }
            });
        };
        Messenger.prototype.originAllowed = function (origin) {
            for (var origins = this.origins, i = 0, l = origins.length; i < l; ++i) {
                if (origins[i] === origin) {
                    return true;
                }
            }
            return false;
        };
        return Messenger;
    }());

    var commonjsGlobal = typeof globalThis !== 'undefined' ? globalThis : typeof window !== 'undefined' ? window : typeof global !== 'undefined' ? global : typeof self !== 'undefined' ? self : {};

    /**
     *
     *
     * @author Jerry Bendy <jerry@icewingcc.com>
     * @licence MIT
     *
     */

    (function(self) {

        var nativeURLSearchParams = (self.URLSearchParams && self.URLSearchParams.prototype.get) ? self.URLSearchParams : null,
            isSupportObjectConstructor = nativeURLSearchParams && (new nativeURLSearchParams({a: 1})).toString() === 'a=1',
            // There is a bug in safari 10.1 (and earlier) that incorrectly decodes `%2B` as an empty space and not a plus.
            decodesPlusesCorrectly = nativeURLSearchParams && (new nativeURLSearchParams('s=%2B').get('s') === '+'),
            __URLSearchParams__ = "__URLSearchParams__",
            // Fix bug in Edge which cannot encode ' &' correctly
            encodesAmpersandsCorrectly = nativeURLSearchParams ? (function() {
                var ampersandTest = new nativeURLSearchParams();
                ampersandTest.append('s', ' &');
                return ampersandTest.toString() === 's=+%26';
            })() : true,
            prototype = URLSearchParamsPolyfill.prototype,
            iterable = !!(self.Symbol && self.Symbol.iterator);

        if (nativeURLSearchParams && isSupportObjectConstructor && decodesPlusesCorrectly && encodesAmpersandsCorrectly) {
            return;
        }


        /**
         * Make a URLSearchParams instance
         *
         * @param {object|string|URLSearchParams} search
         * @constructor
         */
        function URLSearchParamsPolyfill(search) {
            search = search || "";

            // support construct object with another URLSearchParams instance
            if (search instanceof URLSearchParams || search instanceof URLSearchParamsPolyfill) {
                search = search.toString();
            }
            this [__URLSearchParams__] = parseToDict(search);
        }


        /**
         * Appends a specified key/value pair as a new search parameter.
         *
         * @param {string} name
         * @param {string} value
         */
        prototype.append = function(name, value) {
            appendTo(this [__URLSearchParams__], name, value);
        };

        /**
         * Deletes the given search parameter, and its associated value,
         * from the list of all search parameters.
         *
         * @param {string} name
         */
        prototype['delete'] = function(name) {
            delete this [__URLSearchParams__] [name];
        };

        /**
         * Returns the first value associated to the given search parameter.
         *
         * @param {string} name
         * @returns {string|null}
         */
        prototype.get = function(name) {
            var dict = this [__URLSearchParams__];
            return name in dict ? dict[name][0] : null;
        };

        /**
         * Returns all the values association with a given search parameter.
         *
         * @param {string} name
         * @returns {Array}
         */
        prototype.getAll = function(name) {
            var dict = this [__URLSearchParams__];
            return name in dict ? dict [name].slice(0) : [];
        };

        /**
         * Returns a Boolean indicating if such a search parameter exists.
         *
         * @param {string} name
         * @returns {boolean}
         */
        prototype.has = function(name) {
            return name in this [__URLSearchParams__];
        };

        /**
         * Sets the value associated to a given search parameter to
         * the given value. If there were several values, delete the
         * others.
         *
         * @param {string} name
         * @param {string} value
         */
        prototype.set = function set(name, value) {
            this [__URLSearchParams__][name] = ['' + value];
        };

        /**
         * Returns a string containg a query string suitable for use in a URL.
         *
         * @returns {string}
         */
        prototype.toString = function() {
            var dict = this[__URLSearchParams__], query = [], i, key, name, value;
            for (key in dict) {
                name = encode(key);
                for (i = 0, value = dict[key]; i < value.length; i++) {
                    query.push(name + '=' + encode(value[i]));
                }
            }
            return query.join('&');
        };

        // There is a bug in Safari 10.1 and `Proxy`ing it is not enough.
        var forSureUsePolyfill = !decodesPlusesCorrectly;
        var useProxy = (!forSureUsePolyfill && nativeURLSearchParams && !isSupportObjectConstructor && self.Proxy);
        /*
         * Apply polifill to global object and append other prototype into it
         */
        Object.defineProperty(self, 'URLSearchParams', {
            value: (useProxy ?
                // Safari 10.0 doesn't support Proxy, so it won't extend URLSearchParams on safari 10.0
                new Proxy(nativeURLSearchParams, {
                    construct: function(target, args) {
                        return new target((new URLSearchParamsPolyfill(args[0]).toString()));
                    }
                }) :
                URLSearchParamsPolyfill)
        });

        var USPProto = self.URLSearchParams.prototype;

        USPProto.polyfill = true;

        /**
         *
         * @param {function} callback
         * @param {object} thisArg
         */
        USPProto.forEach = USPProto.forEach || function(callback, thisArg) {
            var dict = parseToDict(this.toString());
            Object.getOwnPropertyNames(dict).forEach(function(name) {
                dict[name].forEach(function(value) {
                    callback.call(thisArg, value, name, this);
                }, this);
            }, this);
        };

        /**
         * Sort all name-value pairs
         */
        USPProto.sort = USPProto.sort || function() {
            var dict = parseToDict(this.toString()), keys = [], k, i, j;
            for (k in dict) {
                keys.push(k);
            }
            keys.sort();

            for (i = 0; i < keys.length; i++) {
                this['delete'](keys[i]);
            }
            for (i = 0; i < keys.length; i++) {
                var key = keys[i], values = dict[key];
                for (j = 0; j < values.length; j++) {
                    this.append(key, values[j]);
                }
            }
        };

        /**
         * Returns an iterator allowing to go through all keys of
         * the key/value pairs contained in this object.
         *
         * @returns {function}
         */
        USPProto.keys = USPProto.keys || function() {
            var items = [];
            this.forEach(function(item, name) {
                items.push(name);
            });
            return makeIterator(items);
        };

        /**
         * Returns an iterator allowing to go through all values of
         * the key/value pairs contained in this object.
         *
         * @returns {function}
         */
        USPProto.values = USPProto.values || function() {
            var items = [];
            this.forEach(function(item) {
                items.push(item);
            });
            return makeIterator(items);
        };

        /**
         * Returns an iterator allowing to go through all key/value
         * pairs contained in this object.
         *
         * @returns {function}
         */
        USPProto.entries = USPProto.entries || function() {
            var items = [];
            this.forEach(function(item, name) {
                items.push([name, item]);
            });
            return makeIterator(items);
        };


        if (iterable) {
            USPProto[self.Symbol.iterator] = USPProto[self.Symbol.iterator] || USPProto.entries;
        }


        function encode(str) {
            var replace = {
                '!': '%21',
                "'": '%27',
                '(': '%28',
                ')': '%29',
                '~': '%7E',
                '%20': '+',
                '%00': '\x00'
            };
            return encodeURIComponent(str).replace(/[!'\(\)~]|%20|%00/g, function(match) {
                return replace[match];
            });
        }

        function decode(str) {
            return decodeURIComponent(str.replace(/\+/g, ' '));
        }

        function makeIterator(arr) {
            var iterator = {
                next: function() {
                    var value = arr.shift();
                    return {done: value === undefined, value: value};
                }
            };

            if (iterable) {
                iterator[self.Symbol.iterator] = function() {
                    return iterator;
                };
            }

            return iterator;
        }

        function parseToDict(search) {
            var dict = {};

            if (typeof search === "object") {
                for (var key in search) {
                    if (search.hasOwnProperty(key)) {
                        appendTo(dict, key, search[key]);
                    }
                }

            } else {
                // remove first '?'
                if (search.indexOf("?") === 0) {
                    search = search.slice(1);
                }

                var pairs = search.split("&");
                for (var j = 0; j < pairs.length; j++) {
                    var value = pairs [j],
                        index = value.indexOf('=');

                    if (-1 < index) {
                        appendTo(dict, decode(value.slice(0, index)), decode(value.slice(index + 1)));

                    } else {
                        if (value) {
                            appendTo(dict, decode(value), '');
                        }
                    }
                }
            }

            return dict;
        }

        function appendTo(dict, name, value) {
            var val = typeof value === 'string' ? value : (
                value !== null && value !== undefined && typeof value.toString === 'function' ? value.toString() : JSON.stringify(value)
            );

            if (name in dict) {
                dict[name].push(val);
            } else {
                dict[name] = [val];
            }
        }

    })(typeof commonjsGlobal !== 'undefined' ? commonjsGlobal : (typeof window !== 'undefined' ? window : commonjsGlobal));

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    function httpGet(requestURL, params) {
        if (params === void 0) { params = {}; }
        var searchParams = new URLSearchParams(params);
        return ajax.ajax.get(requestURL + "?" + searchParams.toString()).pipe(operators.pluck('response'));
    }
    function httpPost(requestURL, body) {
        if (body === void 0) { body = {}; }
        return ajax.ajax.post(requestURL, body, { 'Content-Type': 'application/json' }).pipe(operators.pluck('response'));
    }
    var SDKService = {
        httpGet: httpGet,
        httpPost: httpPost
    };

    /*! *****************************************************************************
    Copyright (c) Microsoft Corporation. All rights reserved.
    Licensed under the Apache License, Version 2.0 (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.apache.org/licenses/LICENSE-2.0

    THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED
    WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
    MERCHANTABLITY OR NON-INFRINGEMENT.

    See the Apache Version 2.0 License for specific language governing permissions
    and limitations under the License.
    ***************************************************************************** */

    var __assign = function() {
        __assign = Object.assign || function __assign(t) {
            for (var s, i = 1, n = arguments.length; i < n; i++) {
                s = arguments[i];
                for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
            }
            return t;
        };
        return __assign.apply(this, arguments);
    };

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    var DEFAULTS = {
        site: '',
        baseUrl: '',
        searchId: null,
        endpoints: {
            GET_ITEM_URL: '/api/1/site/content_store/item.json',
            GET_DESCRIPTOR: '/api/1/site/content_store/descriptor.json',
            GET_CHILDREN: '/api/1/site/content_store/children.json',
            GET_TREE: '/api/1/site/content_store/tree.json',
            GET_NAV_TREE: '/api/1/site/navigation/tree.json',
            GET_BREADCRUMB: '/api/1/site/navigation/breadcrumb.json',
            TRANSFORM_URL: '/api/1/site/url/transform.json',
            SEARCH: 'crafter-search/api/2/search/search.json',
            ELASTICSEARCH: 'api/1/site/elasticsearch/search'
        },
        contentTypeRegistry: {}
    };
    var ConfigManager = /** @class */ (function () {
        function ConfigManager() {
            this.config = __assign({}, DEFAULTS);
            this.config$ = new rxjs.BehaviorSubject(__assign({}, DEFAULTS));
        }
        ConfigManager.prototype.publishConfig = function (config) {
            this.config = __assign({}, config);
            this.config$.next(__assign({}, config));
        };
        ConfigManager.prototype.subscribe = function (observerOrNext) {
            var operators = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                operators[_i - 1] = arguments[_i];
            }
            return this.config$.pipe.apply(this.config$, operators).subscribe(observerOrNext);
        };
        ConfigManager.prototype.entry = function (propPath, nextValue) {
            var _a;
            var config = this.config;
            if (!propPath)
                return __assign({}, config);
            var getter = (nextValue == null);
            var path = propPath.split('.');
            var prop = (!getter) && (path.pop());
            var value = (function () {
                try {
                    var l_1 = path.length - 1;
                    return path.length ? path.reduce(function (cfg, property, i) {
                        return getter && (l_1 === i) && utils.isPlainObject(cfg[property])
                            ? __assign({}, cfg[property]) : cfg[property];
                    }, config) : config;
                }
                catch (e) {
                    utils.log("Error retrieving crafter config prop '" + propPath + "': " + (e.message || e), utils.log.WARN);
                    return null;
                }
            })();
            if (getter) {
                return value;
            }
            else if ((prop in value) ||
                (path[path.length - 1] === 'contentTypeRegistry')) {
                this.publishConfig(__assign(__assign({}, value), (_a = {}, _a[prop] = nextValue, _a)));
            }
        };
        ConfigManager.prototype.getConfig = function () {
            return __assign({}, this.config);
        };
        ConfigManager.prototype.mix = function (mixin) {
            if (mixin === void 0) { mixin = {}; }
            return __assign(__assign({}, this.config), mixin);
        };
        ConfigManager.prototype.configure = function (nextConfig) {
            var newConfig = utils.extendDeepExistingProps(__assign({}, this.config), nextConfig);
            this.publishConfig(newConfig);
        };
        return ConfigManager;
    }());
    var crafterConf = new ConfigManager();

    exports.Messenger = Messenger;
    exports.SDKService = SDKService;
    exports.crafterConf = crafterConf;
    exports.httpGet = httpGet;
    exports.httpPost = httpPost;

    Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=classes.umd.js.map
