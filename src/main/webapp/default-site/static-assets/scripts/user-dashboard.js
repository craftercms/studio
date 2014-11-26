
var App = Ember.Application.create({
    rootElement: '#app',
    ready: function () {

    }
});

(function (App, Ember, $) {

    if (!typeof CStudioAuthoring === 'undefined') {
        console && console.error && console.error('Warning: CStudioAuthoring is undefined.');
        CStudioAuthoring = { cookieDomain: '' };
    }

    var TYPE_ERROR = 'error';
    var TYPE_SUCCESS = 'success';
    var MSG_TPL = '<div class="alert-box %@ radius mb10"><i class="%@"></i> %@</div>';

    var $window     = $(window),
        $body       = $('body'),
        $app        = $('#app'),
        $footer     = $('footer.main-footer'),
        $navbar     = $app.find('.top-bar');

    var sites,
        user = {
            name: 'Roy',
            lastName: 'Art',
            email: 'roy.art@rivetlogic.com'
        };

    $app.css('min-height', $window.height() - $footer.height() - 60);

    /*api = (function ( isDevMode ) {
     var url = isDevMode ? '' : '';
     }) (window.location.hostname.indexOf('craftersoftware.com') === -1);*/

    function api ( service ) {
        return ('/studio/api/1/services/api/1/user/%@.json').fmt(service);
    };

    function get ( service ) {
        return $.getJSON( api(service) );
    };

    function goToDashboard () {

        var context = this.context;

        $.cookie('crafterSite', context.siteId, { domain: CStudioAuthoring.cookieDomain, path: '/' });

        window.location.href = context.cstudioURL;

    };

    $('body').delegate('a[data-site-id]', 'click', function (e) {
        e.preventDefault();

        var $a = $(this);
        var id = $a.data('siteId');

        $.cookie('crafterSite', id, { domain: CStudioAuthoring.cookieDomain, path: '/' });

        window.location.href = $a.attr('href');

    });

    App.user = Ember.Object.create({});

    get('get-user').success(function (data) {
        App.user.setProperties(data);
    });

    App.Router.map(function () {

        this.resource('account');

        this.resource('sites', function () {
            this.resource('all');
            this.resource('site', { path: ':site_id' });
        });

    });

    App.AccountRoute = Ember.Route.extend({
        model: function () {
            return get('get-user');
        },
        setupController: function( controller, account ) {
            controller.set('model', account);
        },
        actions: {
            saveAccount: function () {
                console.log('save...');
            },
            changePassword: function () {

                var $form     = $('#changePasswordForm'),
                    $current  = $('#current'),
                    $password = $('#password'),
                    $passConf = $('#passwordConfirm'),
                    $messages = $('#passwordMessages'),
                    $passFlds = $().add($current).add($password).add($passConf),
                    $error    = $();

                $passFlds.each(function () {

                    var $field = $(this),
                        fldId  = $field.attr('id'),
                        value  = $field.val();

                    var $lbl = $('label[for="' + fldId + '"]'),
                        $sml = $('small[data-="' + fldId + '"]');

                    $field.siblings('small').remove();
                    $lbl.removeClass('error');
                    $sml.removeClass('bold');

                    if (!value || value.length < 6) {
                        $error = $error.add($field);
                        $('<small class="error">Invalid value.</small>').insertAfter($field);
                        $lbl.addClass('error');
                        $sml.addClass('bold');
                    }

                });

                if ($error.length) {

                    $error.filter(':first').select();

                } else if ($password.val() !== $passConf.val()) {

                    $messages.html(
                        MSG_TPL.fmt(
                            'error',
                            'foundicon-general-flag',
                            'Passwords do not match.'));

                } else {

                    var me = this;

                    $messages.html(
                        MSG_TPL.fmt(
                            'secondary', '', 'Loading&hellip;'));

                    $.ajax({
                        type: 'POST',
                        data: $form.serialize(),
                        url: $form.attr('action') + '.json'
                    }).always(function ( data ) {
                        if ( typeof data === 'object' ) {

                            if (TYPE_SUCCESS === data.type) {
                                me.send('clearPasswordForm');
                            }

                            $messages.html(
                                MSG_TPL.fmt(
                                    data.type, '', data.message));

                        }
                    })

                }

            },
            clearPasswordForm: function () {

                var $current  = $('#current'),
                    $password = $('#password'),
                    $passConf = $('#passwordConfirm'),
                    $messages = $('#passwordMessages'),
                    $passFlds = $().add($current).add($password).add($passConf);

                $messages.html('');

                $passFlds.each(function () {

                    var $field = $(this),
                        fldId  = $field.attr('id');

                    var $lbl = $('label[for="' + fldId + '"]'),
                        $sml = $('small[data-="' + fldId + '"]');

                    $field.siblings('small').remove();
                    $lbl.removeClass('error');
                    $sml.removeClass('bold');

                });

            }
        }
    });

    App.UserView = Ember.View.extend({
        templateName: 'dropdown-account',
        classNames: 'user-info'.w(),
        accountBinding: 'App.user'
    });

    App.AllRoute = Ember.Route.extend({
        model: function () {
            return get('get-sites-2').success(function (data) {
                sites = data;
            });
        },
        actions: {
            goToDashboard: goToDashboard
        }
    });

    App.SitesRoute = Ember.Route.extend({
        model: function () {
            return get('get-sites-2').success(function (data) {
                sites = data;
            });
        }
    });

    App.SiteRoute = Ember.Route.extend({
        actions: {
            goToDashboard: goToDashboard
        },
        model: function ( params ) {
            return sites.findBy('id', parseInt(params.site_id));
        }
    });

    App.SiteController = Ember.ObjectController.extend({
        isEditing: false,
        actions: {
            action: function() { }
        }
    });

    App.TopbarView = Ember.View.extend({
        templateName: 'topbar-view',
        classNames: 'contain-to-grid sticky'.w(),
        didInsertElement: function () {
            this._super();
            this.$().foundation();
        }
    });

    App.UpgradeButtonView = Ember.View.extend({
        tagName: 'button',
        click: function () {

            if (!this.reveal) {

                this.reveal = $('.reveal-modal')
                    .foundation('reveal');

                var elem    = this.reveal;
                var iframe  = elem.find('iframe');
                var orginal = iframe.data('src');

                elem.on('closed', function () {
                    iframe.attr('src', '');
                });

                elem.on('open', function () {
                    iframe.attr('src', orginal);
                });

            }

            this.reveal.foundation('reveal', 'open');

        }
    });

    function bytesToWhatever ( bytes ) {

        if (bytes == 0) return '0 Bytes';

        var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
        var result = Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];

        return result;

    }

    Ember.Handlebars.helper('meter', function (object) {
        return new Handlebars.SafeString('<span class="meter" style="width: '+((object.used * 100) / object.total)+'%"></span>');
    });

    Ember.Handlebars.helper('kb-to-gb', function (object) {
        return new Handlebars.SafeString( object / 1048576 );
    });

    Ember.Handlebars.helper('kb-to-whatever', function ( object ) {
        var result = bytesToWhatever( object * 1024 );
        return new Handlebars.SafeString( result );
    });

}) (App, Ember, $);