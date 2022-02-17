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
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
    typeof define === 'function' && define.amd ? define('@craftercms/utils', ['exports'], factory) :
    (global = global || self, factory((global.craftercms = global.craftercms || {}, global.craftercms.utils = {})));
}(this, (function (exports) { 'use strict';

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
    function composeUrl(crafterConfigOrBaseUrl, endpoint) {
        var base = (typeof crafterConfigOrBaseUrl === 'string')
            ? crafterConfigOrBaseUrl
            : (crafterConfigOrBaseUrl.baseUrl ? crafterConfigOrBaseUrl.baseUrl + "/" : '');
        return ("" + base + endpoint).replace(/([^:]\/)\/+/g, "$1");
    }
    function isPlainObject(obj) {
        return typeof obj === 'object' && obj !== null && obj.constructor == Object;
    }
    function extendDeep(target, source) {
        for (var prop in source) {
            if (source.hasOwnProperty(prop)) {
                if (prop in target && isPlainObject(target[prop]) && isPlainObject(source[prop])) {
                    extendDeep(target[prop], source[prop]);
                }
                else {
                    target[prop] = source[prop];
                }
            }
        }
        return target;
    }
    function extendDeepExistingProps(target, source) {
        for (var prop in source) {
            if (prop in target && source.hasOwnProperty(prop)) {
                if (isPlainObject(target[prop]) && isPlainObject(source[prop])) {
                    extendDeep(target[prop], source[prop]);
                }
                else {
                    target[prop] = source[prop];
                }
            }
        }
        return target;
    }
    function nullOrUndefined(value) {
        return value == null;
    }
    function notNullOrUndefined(value) {
        return !nullOrUndefined(value);
    }
    function createLookupTable(list, idProp) {
        if (idProp === void 0) { idProp = 'id'; }
        return list.reduce(function (table, item) {
            var id = item[idProp];
            table[id] = item;
            return table;
        }, {});
    }

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
    function log(message, type) {
        if (type === void 0) { type = log.DEFAULT; }
        console && (console[type] && !console[type](message) || console.log && console.log(message));
    }
    (function (log) {
        log.DEFAULT = 'log';
        log.ERROR = 'error';
        log.WARN = 'warn';
    })(log || (log = {}));

    exports.composeUrl = composeUrl;
    exports.createLookupTable = createLookupTable;
    exports.extendDeep = extendDeep;
    exports.extendDeepExistingProps = extendDeepExistingProps;
    exports.isPlainObject = isPlainObject;
    exports.log = log;
    exports.notNullOrUndefined = notNullOrUndefined;
    exports.nullOrUndefined = nullOrUndefined;

    Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=utils.umd.js.map
