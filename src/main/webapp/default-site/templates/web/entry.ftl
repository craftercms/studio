<!DOCTYPE html>
<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale = 1.0, user-scalable = no">

    <!--[if lt IE 9]>
    <script src="/static-assets/js/html5shiv.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="http://craftersoftware.com/static-assets/ico/apple-touch-icon-57-precomposed.png">
    <link rel="shortcut icon" href="http://craftersoftware.com/static-assets/ico/favicon.png">
    
    <title>Crafter Studio</title>

    <link rel="stylesheet" href="/studio/static-assets/styles/cloud-site-main.css">

    <script src="/studio/static-assets/libs/modernizr/modernizr.js"></script>
    <script src="/studio/static-assets/scripts/cloud-home.js"></script>

</head>
<body>

<form id="signInModal" action="/studio/api/1/services/api/1/user/login" class="reveal-modal small" data-reveal>
    <div class="row">
        <div class="large-12 columns text-center">
            <img src="/studio/static-assets/images/crafter_studio_360.png" style="width: 60%"/>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
                Email
                <input type="text" name="username" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
            <a data-reveal data-reveal-id="forgotPasswordModal" class="right" href="javascript:">Forgot your Password?</a>
                Password
                <input type="password" name="password" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
         <div class="feedback hide"></div>
            <button type="submit" class="button" id="signInButton">Sign in</button>
        </div>
    </div>
</form>

<form id="forgotPasswordModal" action="/studio/api/1/services/api/1/user/reset-password" class="reveal-modal small" data-reveal>
    <div class="row">
        <div class="large-12 columns">
            <h2>Recover Password</h2>
            <p class="lead">Type your email to recover your password.</p>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <label>
                Email
                <input type="text" name="username" placeholder="" />
            </label>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <div class="feedback hide"></div>
            <button type="submit" class="button" id="recoverPasswordButton">Submit</button>
        </div>
    </div>
    <a class="close-reveal-modal">&#215;</a>
</form>


<script>

    $(document).foundation();

    $('#signInModal, #forgotPasswordModal')
            .submit(function () {

                var $form   = $(this),
                        $inputs = $form.find('input'),
                        error   = false;

                $form.find('.feedback').addClass('hide');

                $inputs.each(function () {

                    var $elem   = $(this);
                    var $label  = $elem.parents('label:first');

                    if (this.value.trim() === '') {
                        error = true;
                        if (!$label.hasClass('error')) {
                            $elem.addClass('error');
                            $label.addClass('error');
                            $label.append('<small class="error">Invalid entry</small>')
                        }
                    } else {
                        $elem
                                .removeClass('error')
                                .parents('label:first').removeClass('error')
                                .find('small')
                                .remove();
                    }

                });

                if (error) {
                    $inputs.filter('.error:first').focus();
                } else {

                    $.ajax({
                        type: 'POST',
                        data: $form.serialize(),
                        url: $form.attr('action') + '.json'
                    })
                    .always(function (data) {
                        if (typeof data === 'object') {

                            var isRecover = $form.attr('id') === 'forgotPasswordModal';

                            $form.find('.feedback')
                                    .html('<div class="alert-box ' + data.type + '">' + data.message + ((isRecover && data.type === 'success') ? ' <a href=\"javascript:\" data-reveal data-reveal-id=\"signInModal\">Back to login &raquo;</a>' : '') + '</div>')
                                    .removeClass('hide');
 
                            if ("success" === data.type && !isRecover) {
                                window.location.href = '/studio/user-dashboard#/sites/all';
                            }

                        }
                    });

                }

                return false;

            })
            .on('closed', function () {

                $(this)
                        .find('.feedback').addClass('hide').end()
                        .find('input').each(function () {

                            var $elem = $(this);

                            $elem
                                    .val('')
                                    .removeClass('error')
                                    .parents('label:first').removeClass('error')
                                    .find('small')
                                    .remove();

                        });

            });

    $('#signInModal').foundation('reveal', 'open');

    $('#forgotPasswordModal .close-reveal-modal').on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        $('#signInModal').foundation('reveal', 'open');
    });

</script>

</body>
</html>