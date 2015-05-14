(function (angular) {
    'use strict';

    var app = angular.module('studio', [
        'ngCookies',
        'ui.router',
        'ui.bootstrap'
    ]);

    app.run([
        '$rootScope', '$state', '$stateParams', 'authService', 'Constants',
        function ($rootScope, $state, $stateParams, authService, Constants) {

            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;

            $rootScope.imagesDirectory = Constants.PATH_IMG;

            $rootScope.$on('$stateChangeStart', function (event, toState) {

                if (toState.name.indexOf('login') === -1 && !authService.isAuthenticated()) {
                    event.preventDefault();
                    $state.go('login');
                }

            });

        }
    ]);

    app.config([
        '$stateProvider', '$urlRouterProvider',
        function ($stateProvider, $urlRouterProvider) {

            $urlRouterProvider
                .otherwise('/sites');

            $stateProvider
                .state('home', {
                    url: '/',
                    abstract: true,
                    templateUrl: '/studio/static-assets/ng-views/layout.html',
                    controller: 'AppCtrl'
                })
                .state('home.sites', {
                    url: 'sites',
                    views: {
                        content: {
                            templateUrl: '/studio/static-assets/ng-views/sites.html',
                            controller: 'SitesCtrl'
                        }
                    }
                })
                .state('home.sites.create', {
                    url: '/create',
                    templateUrl: '/studio/static-assets/ng-views/create-site.html',
                     controller: 'SiteCtrl'
                })
                .state('home.sites.site', {
                    url: '/:siteId',
                    templateUrl: '/studio/static-assets/ng-views/site.html',
                    controller: 'SiteCtrl'
                })
                .state('home.settings', {
                    url: 'settings',
                    views: {
                        content: {
                            templateUrl: '/studio/static-assets/ng-views/settings.html',
                            controller: 'AppCtrl'
                        }
                    }
                })
                .state('login', {
                    url: '/login',
                    onEnter: [
                        '$rootScope', '$state', '$modal',
                        function ($rootScope, $state, $modal) {

                            $rootScope.loginModal = $modal.open({
                                templateUrl: '/studio/static-assets/ng-views/login.html',
                                controller: 'LoginCtrl',
                                backdrop: 'static',
                                keyboard: false,
                                size: 'sm'
                            });

                            $rootScope.loginModal.result.finally(function () {
                                $rootScope.loginModal = null;
                                $state.go('home.sites');
                            });

                        }
                    ],
                    onExit: [
                        '$rootScope',
                        function ($rootScope) {
                            if ($rootScope.loginModal) {
                                $rootScope.loginModal.close();
                            }
                        }
                    ]
                })
                .state('login.recover', {
                    url: '/recover',
                    onEnter: [
                        '$rootScope', '$state', '$modal',
                        function ($rootScope, $state, $modal) {

                            $rootScope.recoverModal = $modal.open({
                                templateUrl: '/studio/static-assets/ng-views/recover.html',
                                controller: 'RecoverCtrl',
                                backdrop: 'static',
                                keyboard: false,
                                size: 'sm'
                            });

                            $rootScope.recoverModal.result.finally(function () {
                                $rootScope.recoverModal = null;
                                $state.go('login');
                            });

                        }
                    ],
                    onExit: [
                        '$rootScope',
                        function ($rootScope) {
                            if ($rootScope.recoverModal) {
                                $rootScope.recoverModal.close();
                            }
                        }
                    ]
                })
                .state('preview', {
                    url: '/preview?site&url',
                    cssClass: 'studio-preview',
                    templateUrl: '/studio/static-assets/ng-views/preview.html',
                    controller: 'PreviewCtrl'
                });

        }
    ]);

    app.constant('Constants', {
        AUTH_SUCCESS: 'auth-success',
        PATH_IMG: '/images/',
        SERVICE: '/studio/api/1/services/api/1/'
    });

    app.service('authService', [
        '$rootScope', '$http', '$document', 'Constants',
        function ($rootScope, $http, $document, Constants) {

            var user = null;
            var script = $document[0].getElementById('user');

            if (script) {
                script = angular.element(script);
                user = JSON.parse(script.html());
            }

            this.isAuthenticated = function () {
                return !!user;
            };

            this.login = function (data) {
                return $http({
                    data: data,
                    method: 'POST',
                    url: api('login'),
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    transformRequest: function (obj) {
                        var str = [];
                        for (var p in obj) {
                            str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]));
                        }
                        return str.join('&');
                    }
                }).then(function (data) {
                    if (data.data.type === 'success') {

                        user = data.data.user;
                        $rootScope.$broadcast(Constants.AUTH_SUCCESS, user);

                    }
                    return data.data;
                });
            };

            this.logout = function () {
                user = null;
            };

            this.getUser = function () {
                return user;
            };

            this.recoverPassword = function (data) {
                return $http.post(api('reset-password'), data);
            };

            this.changePassword = function (data) {
                return $http.post(api('change-password'), data)
                    .then(function (data) {
                        return data.data;
                    });
            };

            function api(action) {
                return Constants.SERVICE + 'user/' + action + '.json';
            }

            return this;

        }
    ]);

    app.service('sitesService', [
        '$http', 'Constants', '$cookies', '$timeout', '$window',
        function ($http, Constants, $cookies, $timeout, $window) {

            var me = this;
            var cookieName = 'crafterSite';

            this.getSites = function() {
                return $http.get(json('get-sites-3'));
            };

            this.getSite = function(id) {
                return $http.get(json('get-site'), {
                    params: { siteId: id }
                });
            };

            this.setCookie = function (site) {
                $cookies[cookieName] = site.siteId;
            };

            this.editSite = function (site) {
                me.setCookie(site);
                $timeout(function () {

                    // For future in-app iframe
                    // $state.go('preview', { site: site.siteId, url: site.cstudioURL });

                    $window.location.href = '/studio/preview/#/?page=/&site=' + site.siteId;

                }, 0, false);
            };

            this.goToDashboard = function (site) {

                me.setCookie(site);
                $timeout(function () {
                    $window.location.href = '/studio/site-dashboard';
                }, 0, false);
            };

            this.create = function (site) {
                return $http.post(api('create-site'),site);
            };

            this.removeSite = function(site) {
                console.log(site.siteId);
                return $http.post(api('delete-site'), {
                    siteId: site.siteId
                });
            };

            this.getAvailableBlueprints = function() {
                return $http.get(api('get-available-blueprints'));
            };

            function api(action) {
                return Constants.SERVICE + 'site/' + action + '.json';
            }

            function json(action) {
                return Constants.SERVICE + 'user/' + action + '.json';
            }

            return this;

        }
    ]);

    app.controller('AppCtrl', [
        '$scope', '$state', 'authService', 'Constants',
        function ($scope, $state, authService, Constants) {

            function logout() {
                authService.logout();
                $state.go('login');
            }

            function changePassword() {
                authService.changePassword($scope.data)
                    .then(function (data) {
                        $scope.error = $scope.message = null;
                        if (data.type === 'error') {
                            $scope.error = data.message;
                        } else if (data.error) {
                            $scope.error = data.error;
                        } else {
                            $scope.message = data.message;
                        }
                    });
            }

            $scope.user = authService.getUser();
            $scope.data = { email: ($scope.user || { 'email': '' }).email };
            $scope.error = null;

            $scope.logout = logout;
            $scope.changePassword = changePassword;

            $scope.$on(Constants.AUTH_SUCCESS, function ($event, user) {
                $scope.user = user;
                $scope.data.email = $scope.user.email;
            });

        }
    ]);

    app.controller('SitesCtrl', [
        '$scope', '$state', 'sitesService',
        function ($scope, $state, sitesService) {

            $scope.sites = null;

            $scope.editSite = sitesService.editSite;

            $scope.goToDashboard = sitesService.goToDashboard;


            function getSites () {
                console.log("getsites");
                sitesService.getSites()
                    .success(function (data) {
                        $scope.sites = data;
                    })
                    .error(function () {
                        $scope.sites = null;
                    });
            }

            getSites();

            $scope.removeSiteSites = function (site){
                sitesService.removeSite(site)
                    .success(function (data) {
                        getSites();
                    })
                    .error(function () {
                        $scope.sites = null;
                    });


            }

        }


    ]);

    app.controller('SiteCtrl', [
        '$scope', '$state', 'sitesService', '$timeout', '$window',
        function ($scope, $state, sitesService,$timeout, $window) {

            $scope.blueprints = [];
            function getBlueprints() {
                sitesService.getAvailableBlueprints().success(function (data) {
                    $scope.blueprints = data;
                }).error(function () {
                    $scope.blueprints = [];
                });
            };

            getBlueprints();
            // View models
            $scope.site = { siteId: '', siteName: '', description: '', blueprint: $scope.blueprints[0] };

            // View methods
            $scope.percent = percent;
            $scope.select = select;
            $scope.create = create;
            $scope.setSiteId = setSiteId;

            $scope.$watch('sites', getSite);

            function setSiteId() {
                if ($scope.site.siteName != undefined){
                    $scope.site.siteId = $scope.site.siteName.replace(/\s+/g, '');
                }else{
                    $scope.site.siteId = '';
                }
            }

            function percent(data) {
                (!data) && (data = {});
                return Math.ceil((data.used * 100) / (data.total));
            }

            function select($event) {
                $event.target.select();
            }

            function getSite() {

                var siteId = $state.params.siteId;

                if (!$scope.sites) {
                    return;
                }

                for (var i = 0,
                         sites = $scope.sites,
                         site = sites[i],
                         l = sites.length;
                     i < l;
                     site = sites[++i]) {
                    if ((site.id + '') === (siteId + '')) {
                        $scope.site = site;
                        break;
                    }

                }
            }

            function create() {
                sitesService.create({
                    siteId: $scope.site.siteId,
                    siteName: $scope.site.siteName,
                    blueprintName: $scope.site.blueprint.id,
                    description: $scope.site.description
                }).success(function (data) {
                    sitesService.editSite($scope.site);
                });

            }

        }
    ]);

    app.controller('LoginCtrl', [
        '$scope', '$state', 'authService', '$timeout',
        function ($scope, $state, authService, $timeout) {

            var credentials = {};

            function login() {

                authService.login(credentials)
                    .then(function (data) {
                        if (data.type === 'error') {
                            $scope.error = data;
                        }  else if (data.error) {
                            $scope.error = data.error;
                        } else {
                            $state.go('home.sites');
                        }
                    });

            }

            function getModalEl() {
                return document.getElementById('loginView').parentNode.parentNode.parentNode;
            }

            function showModal() {
                var loginViewEl = getModalEl();
                angular.element(loginViewEl).addClass('in');
            }

            function hideModal() {
                var loginViewEl = getModalEl();
                angular.element(loginViewEl).removeClass('in');
            }

            $scope.error = null;
            $scope.credentials = credentials;

            $scope.login = login;

            $scope.$on('$stateChangeSuccess', function() {
                if ($state.current.name === 'login') {
                    showModal();
                } else if ($state.current.name === 'login.recover') {
                    hideModal();
                }
            });

            $scope.$on('$viewContentLoaded', function() {
                if ($state.current.name === 'login.recover') {
                    $timeout(hideModal, 50);
                }
            });

        }
    ]);

    app.controller('RecoverCtrl', [
        '$scope', '$state', 'authService',
        function ($scope, $state, authService) {

            var credentials = $scope.credentials = {};

            $scope.recover = function recover() {
                authService.recoverPassword(credentials)
                    .success(function (data) {
                        if (data.type === 'error') {
                            $scope.error = data.message;
                        }  else if (data.error) {
                            $scope.error = data.error;
                        } else {
                            $scope.success = data.message;
                        }
                    });
            };

        }
    ]);

    app.controller('PreviewCtrl', [
        '$scope', '$state', '$window', '$sce',
        function ($scope, $state, $window, $sce) {

            function getIFrame(getContentWindow) {
                var el = $window.document.getElementById('studioIFrame');
                return (getContentWindow) ? el.contentWindow : el;
            }

            function sendMessage() {

                var message = data.message;
                var popup = getIFrame(true);

                popup.postMessage(message, url);

            }

            function receiveMessage(event) {

                // checking this here is not secure anyway and origin needs to be dynamic, not hardcoded
                //if (event.origin !== 'http://HOST:PORT') {
                //    return;
                //}

                //var frame = event.source;
                //var message = event.data;

                $scope.$apply(function () {
                    $scope.status = event.data;
                });

            }

            function reloadIFrame() {
                getIFrame(true).location.reload();
            }

            var data = {};
            var url = $state.params.url;

            $scope.data = data;
            $scope.url = $sce.trustAsResourceUrl(url);
            $scope.status = '';

            $scope.sendMessage = sendMessage;
            $scope.reloadIFrame = reloadIFrame;

            $window.addEventListener('message', receiveMessage, false);

        }
    ]);

})(angular);
