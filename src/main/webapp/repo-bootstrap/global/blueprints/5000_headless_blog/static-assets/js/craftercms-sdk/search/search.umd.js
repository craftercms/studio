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
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('rxjs/operators'), require('@craftercms/utils'), require('@craftercms/classes'), require('@craftercms/search')) :
  typeof define === 'function' && define.amd ? define('@craftercms/search', ['exports', 'rxjs/operators', '@craftercms/utils', '@craftercms/classes', '@craftercms/search'], factory) :
  (global = global || self, factory((global.craftercms = global.craftercms || {}, global.craftercms.search = {}), global.rxjs.operators, global.craftercms.utils, global.craftercms.classes, global.craftercms.search));
}(this, (function (exports, operators, utils, classes, search$1) { 'use strict';

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
  // TODO add return types
  /**
   * Query Object
   */
  var Query = /** @class */ (function () {
      /**
       * Creates an empty query
       * @constructor
       */
      function Query(params) {
          if (params === void 0) { params = {}; }
          this.params = params;
      }
      /**
       * Sets a single value parameter in the query object
       * @param {string} name - Name of the parameter
       * @param {object} value - Value of the parameter
       */
      Query.prototype.setParam = function (name, value) {
          this.params[name] = value;
      };
      /**
       * Adds a value for a parameter in the query object
       * @param {string} name - Name of the parameter
       * @param {object} value - Value of the parameter
       */
      Query.prototype.addParam = function (name, value) {
          if (this.params[name]) {
              if (Array.isArray(this.params[name])) {
                  this.params[name].push(value);
              }
              else {
                  this.params[name] = [this.params[name], value];
              }
          }
          else {
              this.params[name] = value;
          }
      };
      return Query;
  }());

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
  /* global Reflect, Promise */

  var extendStatics = function(d, b) {
      extendStatics = Object.setPrototypeOf ||
          ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
          function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
      return extendStatics(d, b);
  };

  function __extends(d, b) {
      extendStatics(d, b);
      function __() { this.constructor = d; }
      d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
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
  /**
   * Query implementation for Solr
   */
  var SolrQuery = /** @class */ (function (_super) {
      __extends(SolrQuery, _super);
      function SolrQuery() {
          return _super !== null && _super.apply(this, arguments) || this;
      }
      Object.defineProperty(SolrQuery.prototype, "offset", {
          // Synonym of start, added for consistency with Java Search Client
          /**
           * Sets the offset of the results.
           * @param {int} offset - Number of results to skip
           */
          set: function (offset) {
              this.start = offset;
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "numResults", {
          // Synonym of rows, added for consistency with Java Search Client
          /**
           * Sets the number of results to return.
           * @param {int} numResults - Number of results to return
           */
          set: function (numResults) {
              this.rows = numResults;
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "start", {
          /**
           * Sets the offset of the results.
           * @param {int} start - Number of results to skip
           */
          set: function (start) {
              _super.prototype.setParam.call(this, 'start', start);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "rows", {
          /**
           * Sets the number of results to return.
           * @param {int} rows - Number of results to return
           */
          set: function (rows) {
              _super.prototype.setParam.call(this, 'rows', rows);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "query", {
          /**
           * Sets the actual query.
           * @param {string} query - Solr query string
           */
          set: function (query) {
              _super.prototype.setParam.call(this, 'q', query);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "sort", {
          /**
           * Sets the sort order.
           * @param {string} sort - Sort order
           */
          set: function (sort) {
              _super.prototype.setParam.call(this, 'sort', sort);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "fieldsToReturn", {
          /**
           * Sets the fields that should be returned.
           * @param {Array} fields - List of field names
           */
          set: function (fields) {
              _super.prototype.setParam.call(this, 'fl', fields);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "highlight", {
          /**
           * Enables or disables highlighting in the results
           * @param {string} highlight - Indicates if highlighting should be used
           */
          set: function (highlight) {
              _super.prototype.setParam.call(this, 'hl', highlight);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "highlightFields", {
          /**
           * Sets the field to apply highlighting in the results
           * @param {string} fields - List of field names to use for highlighting
           */
          set: function (fields) {
              this.highlight = true;
              _super.prototype.setParam.call(this, 'hl.fl', fields);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "highlightSnippets", {
          /**
           * Sets the number of snippets to generate per field in highlighting
           * @param {int} snippets - Number of snippets
           */
          set: function (snippets) {
              _super.prototype.setParam.call(this, 'hl.snippets', snippets);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "highlightSnippetSize", {
          /**
           * Sets the size of snippets to generate per field in highlighting
           * @param {int} size - Size of snippets
           */
          set: function (size) {
              _super.prototype.setParam.call(this, 'hl.fragsize', size);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "filterQueries", {
          /**
           * Sets the filter queries used to reduce the search results
           * @param {Array} queries - List of filter queries
           */
          set: function (queries) {
              _super.prototype.addParam.call(this, 'fq', queries);
          },
          enumerable: false,
          configurable: true
      });
      Object.defineProperty(SolrQuery.prototype, "disableAdditionalFilters", {
          /**
           * Sets if the additional Crafter Search filters should be disabled on query execution.
           * @param {bool} disableAdditionalFilters - Indicates if additional filters should be used
           */
          set: function (disableAdditionalFilters) {
              _super.prototype.setParam.call(this, 'disable_additional_filters', disableAdditionalFilters);
          },
          enumerable: false,
          configurable: true
      });
      return SolrQuery;
  }(Query));

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
  /**
   * Query implementation for Elasticsearch
   */
  var ElasticQuery = /** @class */ (function (_super) {
      __extends(ElasticQuery, _super);
      function ElasticQuery() {
          return _super !== null && _super.apply(this, arguments) || this;
      }
      Object.defineProperty(ElasticQuery.prototype, "query", {
          /**
           * Sets the actual query.
           * @param {string} query - Query string
           */
          set: function (query) {
              this.params = query;
          },
          enumerable: false,
          configurable: true
      });
      return ElasticQuery;
  }(Query));

  var commonjsGlobal = typeof globalThis !== 'undefined' ? globalThis : typeof window !== 'undefined' ? window : typeof global !== 'undefined' ? global : typeof self !== 'undefined' ? self : {};

  function createCommonjsModule(fn, module) {
  	return module = { exports: {} }, fn(module, module.exports), module.exports;
  }

  var rngBrowser = createCommonjsModule(function (module) {
  // Unique ID creation requires a high quality random # generator.  In the
  // browser this is a little complicated due to unknown quality of Math.random()
  // and inconsistent support for the `crypto` API.  We do the best we can via
  // feature-detection

  // getRandomValues needs to be invoked in a context where "this" is a Crypto
  // implementation. Also, find the complete implementation of crypto on IE11.
  var getRandomValues = (typeof(crypto) != 'undefined' && crypto.getRandomValues && crypto.getRandomValues.bind(crypto)) ||
                        (typeof(msCrypto) != 'undefined' && typeof window.msCrypto.getRandomValues == 'function' && msCrypto.getRandomValues.bind(msCrypto));

  if (getRandomValues) {
    // WHATWG crypto RNG - http://wiki.whatwg.org/wiki/Crypto
    var rnds8 = new Uint8Array(16); // eslint-disable-line no-undef

    module.exports = function whatwgRNG() {
      getRandomValues(rnds8);
      return rnds8;
    };
  } else {
    // Math.random()-based (RNG)
    //
    // If all else fails, use Math.random().  It's fast, but is of unspecified
    // quality.
    var rnds = new Array(16);

    module.exports = function mathRNG() {
      for (var i = 0, r; i < 16; i++) {
        if ((i & 0x03) === 0) r = Math.random() * 0x100000000;
        rnds[i] = r >>> ((i & 0x03) << 3) & 0xff;
      }

      return rnds;
    };
  }
  });

  /**
   * Convert array of 16 byte values to UUID string format of the form:
   * XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
   */
  var byteToHex = [];
  for (var i = 0; i < 256; ++i) {
    byteToHex[i] = (i + 0x100).toString(16).substr(1);
  }

  function bytesToUuid(buf, offset) {
    var i = offset || 0;
    var bth = byteToHex;
    // join used to fix memory issue caused by concatenation: https://bugs.chromium.org/p/v8/issues/detail?id=3175#c4
    return ([
      bth[buf[i++]], bth[buf[i++]],
      bth[buf[i++]], bth[buf[i++]], '-',
      bth[buf[i++]], bth[buf[i++]], '-',
      bth[buf[i++]], bth[buf[i++]], '-',
      bth[buf[i++]], bth[buf[i++]], '-',
      bth[buf[i++]], bth[buf[i++]],
      bth[buf[i++]], bth[buf[i++]],
      bth[buf[i++]], bth[buf[i++]]
    ]).join('');
  }

  var bytesToUuid_1 = bytesToUuid;

  // **`v1()` - Generate time-based UUID**
  //
  // Inspired by https://github.com/LiosK/UUID.js
  // and http://docs.python.org/library/uuid.html

  var _nodeId;
  var _clockseq;

  // Previous uuid creation time
  var _lastMSecs = 0;
  var _lastNSecs = 0;

  // See https://github.com/uuidjs/uuid for API details
  function v1(options, buf, offset) {
    var i = buf && offset || 0;
    var b = buf || [];

    options = options || {};
    var node = options.node || _nodeId;
    var clockseq = options.clockseq !== undefined ? options.clockseq : _clockseq;

    // node and clockseq need to be initialized to random values if they're not
    // specified.  We do this lazily to minimize issues related to insufficient
    // system entropy.  See #189
    if (node == null || clockseq == null) {
      var seedBytes = rngBrowser();
      if (node == null) {
        // Per 4.5, create and 48-bit node id, (47 random bits + multicast bit = 1)
        node = _nodeId = [
          seedBytes[0] | 0x01,
          seedBytes[1], seedBytes[2], seedBytes[3], seedBytes[4], seedBytes[5]
        ];
      }
      if (clockseq == null) {
        // Per 4.2.2, randomize (14 bit) clockseq
        clockseq = _clockseq = (seedBytes[6] << 8 | seedBytes[7]) & 0x3fff;
      }
    }

    // UUID timestamps are 100 nano-second units since the Gregorian epoch,
    // (1582-10-15 00:00).  JSNumbers aren't precise enough for this, so
    // time is handled internally as 'msecs' (integer milliseconds) and 'nsecs'
    // (100-nanoseconds offset from msecs) since unix epoch, 1970-01-01 00:00.
    var msecs = options.msecs !== undefined ? options.msecs : new Date().getTime();

    // Per 4.2.1.2, use count of uuid's generated during the current clock
    // cycle to simulate higher resolution clock
    var nsecs = options.nsecs !== undefined ? options.nsecs : _lastNSecs + 1;

    // Time since last uuid creation (in msecs)
    var dt = (msecs - _lastMSecs) + (nsecs - _lastNSecs)/10000;

    // Per 4.2.1.2, Bump clockseq on clock regression
    if (dt < 0 && options.clockseq === undefined) {
      clockseq = clockseq + 1 & 0x3fff;
    }

    // Reset nsecs if clock regresses (new clockseq) or we've moved onto a new
    // time interval
    if ((dt < 0 || msecs > _lastMSecs) && options.nsecs === undefined) {
      nsecs = 0;
    }

    // Per 4.2.1.2 Throw error if too many uuids are requested
    if (nsecs >= 10000) {
      throw new Error('uuid.v1(): Can\'t create more than 10M uuids/sec');
    }

    _lastMSecs = msecs;
    _lastNSecs = nsecs;
    _clockseq = clockseq;

    // Per 4.1.4 - Convert from unix epoch to Gregorian epoch
    msecs += 12219292800000;

    // `time_low`
    var tl = ((msecs & 0xfffffff) * 10000 + nsecs) % 0x100000000;
    b[i++] = tl >>> 24 & 0xff;
    b[i++] = tl >>> 16 & 0xff;
    b[i++] = tl >>> 8 & 0xff;
    b[i++] = tl & 0xff;

    // `time_mid`
    var tmh = (msecs / 0x100000000 * 10000) & 0xfffffff;
    b[i++] = tmh >>> 8 & 0xff;
    b[i++] = tmh & 0xff;

    // `time_high_and_version`
    b[i++] = tmh >>> 24 & 0xf | 0x10; // include version
    b[i++] = tmh >>> 16 & 0xff;

    // `clock_seq_hi_and_reserved` (Per 4.2.2 - include variant)
    b[i++] = clockseq >>> 8 | 0x80;

    // `clock_seq_low`
    b[i++] = clockseq & 0xff;

    // `node`
    for (var n = 0; n < 6; ++n) {
      b[i + n] = node[n];
    }

    return buf ? buf : bytesToUuid_1(b);
  }

  var v1_1 = v1;

  function v4(options, buf, offset) {
    var i = buf && offset || 0;

    if (typeof(options) == 'string') {
      buf = options === 'binary' ? new Array(16) : null;
      options = null;
    }
    options = options || {};

    var rnds = options.random || (options.rng || rngBrowser)();

    // Per 4.4, set bits for version and `clock_seq_hi_and_reserved`
    rnds[6] = (rnds[6] & 0x0f) | 0x40;
    rnds[8] = (rnds[8] & 0x3f) | 0x80;

    // Copy bytes to buffer, if provided
    if (buf) {
      for (var ii = 0; ii < 16; ++ii) {
        buf[i + ii] = rnds[ii];
      }
    }

    return buf || bytesToUuid_1(rnds);
  }

  var v4_1 = v4;

  var uuid = v4_1;
  uuid.v1 = v1_1;
  uuid.v4 = v4_1;

  var uuid_1 = uuid;

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
  function search(queryOrParams, config) {
      config = classes.crafterConf.mix(config);
      var requestURL;
      var params = (queryOrParams instanceof Query)
          ? queryOrParams.params
          : queryOrParams, searchParams = new URLSearchParams();
      if (queryOrParams instanceof search$1.ElasticQuery) {
          requestURL = utils.composeUrl(config, classes.crafterConf.getConfig().endpoints.ELASTICSEARCH) + '?crafterSite=' + config.site;
          return classes.SDKService.httpPost(requestURL, params)
              .pipe(operators.map(function (response) {
              return response.hits;
          }));
      }
      else {
          requestURL = utils.composeUrl(config, classes.crafterConf.getConfig().endpoints.SEARCH);
          for (var param in params) {
              if (params.hasOwnProperty(param)) {
                  if (Array.isArray(params[param])) {
                      for (var x = 0; x < params[param].length; x++) {
                          searchParams.append(param, params[param][x]);
                      }
                  }
                  else {
                      searchParams.append(param, params[param]);
                  }
              }
          }
          searchParams.append('index_id', config.searchId ? config.searchId : config.site);
          return classes.SDKService.httpGet(requestURL, searchParams);
      }
  }
  function createQuery(searchEngineOrParams, params) {
      if (searchEngineOrParams === void 0) { searchEngineOrParams = 'solr'; }
      if (params === void 0) { params = {}; }
      var query, queryId = (params && params['uuid'])
          ? params['uuid']
          : uuid_1(), engine = (typeof searchEngineOrParams === 'string')
          ? searchEngineOrParams.toLowerCase()
          : 'solr';
      if (typeof searchEngineOrParams !== 'string') {
          params = searchEngineOrParams;
      }
      switch (engine) {
          case 'elastic':
          case 'elasticsearch':
              query = new search$1.ElasticQuery();
              break;
          case 'solr':
          default:
              query = new search$1.SolrQuery();
              break;
      }
      Object.assign(query.params, params);
      query.uuid = queryId;
      return query;
  }
  /**
   * Implementation of Search Service for Solr
   */
  var SearchService = {
      search: search,
      createQuery: createQuery
  };

  exports.ElasticQuery = ElasticQuery;
  exports.Query = Query;
  exports.SearchService = SearchService;
  exports.SolrQuery = SolrQuery;
  exports.createQuery = createQuery;
  exports.search = search;

  Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=search.umd.js.map
