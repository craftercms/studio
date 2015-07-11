/**
 * editor tools
 */
CStudioAuthoring.MediumPanel = CStudioAuthoring.MediumPanel || {

    channels: [],

    $container: null,

    initialized: false,

    /**
     * initialize module
     */
    initialize: function (config) {
        if (this.initialized === false) {
            this.initialized = true;
        }
    },

    render: function (containerEl, config) {

        var me = this,
            $select,
            $container = $(containerEl),
            channels = ((config.config.channels.length)
                ? config.config.channels.channels[0]
                : [config.config.channels.channel])[0];

        $container
            .addClass('studio-view')
            .append([
                '<div class="form-group">',
                '<label class="display-block">Dimensions (px)</label> ',
                '<input class="form-control channel-width" data-axis="x" placeholder="auto">',
                ' &times; ',
                '<input class="form-control channel-height" data-axis="y" placeholder="auto"> ',
                '<a class="flip"><i class="glyphicon glyphicon-refresh" title="Flip dimensions"></i></a>',
                '</div>',
                '<div>',
                '<label>Presets</label> ',
                '<select class="form-control"></select>',
                '</div>'
            ].join(''));

        $select = $container.find('select');

        $select[0].options[0] = new Option(
            'Custom', 'custom', false, false);

        for (var i = 0, label; i < channels.length; i++) {
            label = channels[i].title;
            $select[0].options[i + 1] = new Option(
                label, channels[i].value);
        }

        $select[0].options[1].selected = true;

        $select.change(function () {
            var preset = $(this).val();
            me.presetSelected(preset);

        });

        var timeout;
        $container.find('input').keyup(function (e) {
            clearTimeout(timeout);
            timeout = setTimeout(function () {

                var $el = $(e.currentTarget),
                    value = $el.val(),
                    number = parseInt(value);

                if (value === '' || value === 'auto') {
                    $el.val('');
                    $el.data('rollback', '');
                    me.update();
                } else if (isNaN(number)) {
                    $el.val($el.data('rollback') || '');
                } else {
                    $el.val(number);
                    $el.data('rollback', number);
                    me.update();
                }

            }, 200);
        });

        $container.find('a.flip').click(function () {
            var $inputs = me.$container.find('input'),
                $width = $inputs.filter('[data-axis="x"]'),
                $height = $inputs.filter('[data-axis="y"]'),
                width = $width.val(),
                height = $height.val();
            $width.val(height);
            $height.val(width);
            me.update();
        });

        this.channels = channels;
        this.$container = $container;

    },

    update: function () {

        var $body = $('body'),
            $engine = $('#engineWindow'),
            $inputs = this.$container.find('input'),
            width = $inputs.filter('[data-axis="x"]').val() || 'auto',
            height = $inputs.filter('[data-axis="y"]').val() || 'auto',
            orientation;

        $body.removeClass('studio-device-preview-portrait studio-device-preview-landscape');

        if (width !== 'auto') {
            width = parseInt(width);
        }

        if (height !== 'auto') {
            height = parseInt(height);
        }

        if (width !== 'auto' && height !== 'auto') {

            orientation = (width < height) ? 'portrait' : 'landscape';
            $body.addClass('studio-device-preview-' + orientation);

            // Add up the border widths to the iframe dimenssions
            // to get more accurate preview dimenssions
            if (orientation === 'portrait') {
                height += 100;
                width += 20;
            } else /*if (orientation === 'landscape')*/ {
                height += 20;
                width += 100;
            }

        }

        $engine.width(
            (width === 'auto' || width === '')
                ? ''  : parseInt(width));

        $engine.height(
            (height === 'auto' || height === '')
                ? ''  : parseInt(height));

    },

    presetSelected: function (value) {
        var $window = $(window);
        switch (value) {
            case 'custom':
                this.$container.find('input')
                    .filter('[data-axis="y"]').val($window.height() - 150).end()
                    .filter('[data-axis="x"]').val(parseInt($window.width() * .8)).select();
                break;
            default:
            {

                var channel;

                this.channels.forEach(function (item) {
                    if (item.value === value) channel = item;
                });

                CStudioAuthoringContext.channel = value;

                this.$container.find('input')
                    .filter('[data-axis="x"]').val(channel.width).end()
                    .filter('[data-axis="y"]').val(channel.height);

            }
        }
        this.update();
    }

};

CStudioAuthoring.Module.moduleLoaded("medium-panel", CStudioAuthoring.MediumPanel);