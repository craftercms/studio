var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

(function (global, factory) {
    (typeof exports === 'undefined' ? 'undefined' : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? module.exports = factory() : typeof define === 'function' && define.amd ? define(factory) : (global = global || self, global.AsyncComputed = factory());
})(this, function () {
    'use strict';

    function isComputedLazy(item) {
        return item.hasOwnProperty('lazy') && item.lazy;
    }

    function isLazyActive(vm, key) {
        return vm[lazyActivePrefix + key];
    }

    var lazyActivePrefix = 'async_computed$lazy_active$',
        lazyDataPrefix = 'async_computed$lazy_data$';

    function initLazy(data, key) {
        data[lazyActivePrefix + key] = false;
        data[lazyDataPrefix + key] = null;
    }

    function makeLazyComputed(key) {
        return {
            get: function get() {
                this[lazyActivePrefix + key] = true;
                return this[lazyDataPrefix + key];
            },
            set: function set(value) {
                this[lazyDataPrefix + key] = value;
            }
        };
    }

    function silentSetLazy(vm, key, value) {
        vm[lazyDataPrefix + key] = value;
    }
    function silentGetLazy(vm, key) {
        return vm[lazyDataPrefix + key];
    }

    var getGetterWatchedByArray = function getGetterWatchedByArray(computedAsyncProperty) {
        return function getter() {
            var _this = this;

            computedAsyncProperty.watch.forEach(function (key) {
                // Check if nested key is watched.
                var splittedByDot = key.split('.');
                if (splittedByDot.length === 1) {
                    // If not, just access it.
                    // eslint-disable-next-line no-unused-expressions
                    _this[key];
                } else {
                    // Access the nested propety.
                    try {
                        var start = _this;
                        splittedByDot.forEach(function (part) {
                            start = start[part];
                        });
                    } catch (error) {
                        console.error('AsyncComputed: bad path: ', key);
                        throw error;
                    }
                }
            });
            return computedAsyncProperty.get.call(this);
        };
    };

    var getGetterWatchedByFunction = function getGetterWatchedByFunction(computedAsyncProperty) {
        return function getter() {
            computedAsyncProperty.watch.call(this);
            return computedAsyncProperty.get.call(this);
        };
    };

    function getWatchedGetter(computedAsyncProperty) {
        if (typeof computedAsyncProperty.watch === 'function') {
            return getGetterWatchedByFunction(computedAsyncProperty);
        } else if (Array.isArray(computedAsyncProperty.watch)) {
            computedAsyncProperty.watch.forEach(function (key) {
                if (typeof key !== 'string') {
                    throw new Error('AsyncComputed: watch elemnts must be strings');
                }
            });
            return getGetterWatchedByArray(computedAsyncProperty);
        } else {
            throw Error('AsyncComputed: watch should be function or an array');
        }
    }

    var prefix = '_async_computed$';
    var DidNotUpdate = typeof Symbol === 'function' ? Symbol('did-not-update') : {};

    var AsyncComputed = {
        install: function install(Vue, pluginOptions) {
            pluginOptions = pluginOptions || {};

            Vue.config.optionMergeStrategies.asyncComputed = Vue.config.optionMergeStrategies.computed;

            Vue.mixin({
                data: function data() {
                    return {
                        _asyncComputed: {}
                    };
                },
                beforeCreate: function beforeCreate() {
                    var _this2 = this;

                    var optionData = this.$options.data;
                    var asyncComputed = this.$options.asyncComputed || {};

                    if (!this.$options.computed) this.$options.computed = {};

                    this.$options.computed.$asyncComputed = function () {
                        return _this2.$data._asyncComputed;
                    };

                    if (!Object.keys(asyncComputed).length) return;

                    for (var key in asyncComputed) {
                        var getter = getterFn(key, this.$options.asyncComputed[key]);
                        this.$options.computed[prefix + key] = getter;
                    }

                    this.$options.data = function vueAsyncComputedInjectedDataFn(vm) {
                        var data = (typeof optionData === 'function' ? optionData.call(this, vm) : optionData) || {};
                        for (var _key in asyncComputed) {
                            var item = this.$options.asyncComputed[_key];
                            if (isComputedLazy(item)) {
                                initLazy(data, _key);
                                this.$options.computed[_key] = makeLazyComputed(_key);
                            } else {
                                data[_key] = null;
                            }
                        }
                        return data;
                    };
                },
                created: function created() {
                    var _this3 = this;

                    for (var key in this.$options.asyncComputed || {}) {
                        var item = this.$options.asyncComputed[key],
                            value = generateDefault.call(this, item, pluginOptions);
                        if (isComputedLazy(item)) {
                            silentSetLazy(this, key, value);
                        } else {
                            this[key] = value;
                        }
                    }

                    var _loop = function _loop(_key2) {
                        var promiseId = 0;
                        var watcher = function watcher(newPromise) {
                            var thisPromise = ++promiseId;

                            if (newPromise === DidNotUpdate) {
                                return;
                            }

                            if (!newPromise || !newPromise.then) {
                                newPromise = Promise.resolve(newPromise);
                            }
                            setAsyncState(_this3, _key2, 'updating');

                            newPromise.then(function (value) {
                                if (thisPromise !== promiseId) return;
                                setAsyncState(_this3, _key2, 'success');
                                _this3[_key2] = value;
                            }).catch(function (err) {
                                if (thisPromise !== promiseId) return;

                                setAsyncState(_this3, _key2, 'error');
                                Vue.set(_this3.$data._asyncComputed[_key2], 'exception', err);
                                if (pluginOptions.errorHandler === false) return;

                                var handler = pluginOptions.errorHandler === undefined ? console.error.bind(console, 'Error evaluating async computed property:') : pluginOptions.errorHandler;

                                if (pluginOptions.useRawError) {
                                    handler(err);
                                } else {
                                    handler(err.stack);
                                }
                            });
                        };
                        Vue.set(_this3.$data._asyncComputed, _key2, {
                            exception: null,
                            update: function update() {
                                watcher(getterOnly(_this3.$options.asyncComputed[_key2]).apply(_this3));
                            }
                        });
                        setAsyncState(_this3, _key2, 'updating');
                        _this3.$watch(prefix + _key2, watcher, { immediate: true });
                    };

                    for (var _key2 in this.$options.asyncComputed || {}) {
                        _loop(_key2);
                    }
                }
            });
        }
    };

    function setAsyncState(vm, stateObject, state) {
        vm.$set(vm.$data._asyncComputed[stateObject], 'state', state);
        vm.$set(vm.$data._asyncComputed[stateObject], 'updating', state === 'updating');
        vm.$set(vm.$data._asyncComputed[stateObject], 'error', state === 'error');
        vm.$set(vm.$data._asyncComputed[stateObject], 'success', state === 'success');
    }

    function getterOnly(fn) {
        if (typeof fn === 'function') return fn;

        return fn.get;
    }

    function getterFn(key, fn) {
        if (typeof fn === 'function') return fn;

        var getter = fn.get;

        if (fn.hasOwnProperty('watch')) {
            getter = getWatchedGetter(fn);
        }

        if (fn.hasOwnProperty('shouldUpdate')) {
            var previousGetter = getter;
            getter = function getter() {
                if (fn.shouldUpdate.call(this)) {
                    return previousGetter.call(this);
                }
                return DidNotUpdate;
            };
        }

        if (isComputedLazy(fn)) {
            var nonLazy = getter;
            getter = function lazyGetter() {
                if (isLazyActive(this, key)) {
                    return nonLazy.call(this);
                } else {
                    return silentGetLazy(this, key);
                }
            };
        }
        return getter;
    }

    function generateDefault(fn, pluginOptions) {
        var defaultValue = null;

        if ('default' in fn) {
            defaultValue = fn.default;
        } else if ('default' in pluginOptions) {
            defaultValue = pluginOptions.default;
        }

        if (typeof defaultValue === 'function') {
            return defaultValue.call(this);
        } else {
            return defaultValue;
        }
    }

    /* istanbul ignore if */
    if (typeof window !== 'undefined' && window.Vue) {
        // Auto install in dist mode
        window.Vue.use(AsyncComputed);
    }

    return AsyncComputed;
});
