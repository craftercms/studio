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

/* jshint node:true */
'use strict';

var gulp = require('gulp');
var $ = require('gulp-load-plugins')();

var pathToDist = '../main/webapp/default-site';

gulp.task('styles', function () {
    return gulp.src('app/styles/main.scss')
        .pipe($.plumber())
        .pipe($.rubySass({
            style: 'expanded',
            precision: 10
        }))
        .pipe($.autoprefixer({browsers: ['last 1 version']}))
        .pipe(gulp.dest('.tmp/styles'));
});

gulp.task('jshint', function () {
    return gulp.src('app/scripts/**/*.js')
        .pipe($.jshint())
        .pipe($.jshint.reporter('jshint-stylish'))
        .pipe($.jshint.reporter('fail'));
});

gulp.task('html', ['styles'], function () {

    var lazypipe = require('lazypipe');

    var source = gulp.src(['app/*.html', 'app/*.ftl']);
    var assets = $.useref.assets({searchPath: '{.tmp,app}'});
    var cssChannel = lazypipe()
        .pipe($.csso)
        .pipe($.replace, 'libs/bootstrap-sass-official/assets/fonts/bootstrap', 'fonts');

    return source
        .pipe(assets)
        .pipe($.if('*.js', $.uglify()))
        .pipe($.if('*.css', cssChannel()))
        .pipe(assets.restore())
        .pipe($.useref())
        .pipe($.rename(function (path) {
            if (path.extname === '.html') {
                path.extname = '.ftl';
            }
        }))
        .pipe($.replace('<!-- ', '<#-- '))
        .pipe($.replace('../../static-assets', '/studio/static-assets'))
        .pipe($.replace('"images/', '"/studio/static-assets/images/'))
        .pipe($.replace('\'images/', '\'/studio/static-assets/images/'))
        .pipe(gulp.dest(pathToDist + '/templates/web/'));

});

gulp.task('images', function () {
    return gulp.src('app/images/**/*')
        .pipe($.cache($.imagemin({
            progressive: true,
            interlaced: true
        })))
        .pipe(gulp.dest(pathToDist + '/static-assets/images'));
});

gulp.task('fonts', function () {
    return gulp.src(require('main-bower-files')().concat('app/fonts/**/*'))
        .pipe($.filter('**/*.{eot,svg,ttf,woff}'))
        .pipe($.flatten())
        .pipe(gulp.dest(pathToDist + '/static-assets/fonts'));
});

gulp.task('extras', function () {
    return gulp.src([
        'app/*.*',
        '!app/*.html',
        '!app/*.ftl'/*,
        'node_modules/apache-server-configs/dist/.htaccess'*/
    ], {
        dot: true
    }).pipe(gulp.dest(pathToDist));
});

// gulp.task('clean', require('del').bind(null, ['.tmp', pathToDist]));

gulp.task('connect', ['styles'], function () {
    var serveStatic = require('serve-static');
    var serveIndex = require('serve-index');
    var app = require('connect')()
        .use(require('connect-livereload')({port: 35729}))
        .use(serveStatic('.tmp'))
        .use(serveStatic('app'))
        // paths to libs should be relative to the current file
        // e.g. in app/index.html you should use ../libs
        .use('app/libs', serveStatic('libs'))
        .use(serveIndex('app'));

    require('http').createServer(app)
        .listen(9000)
        .on('listening', function () {
            console.log('Started connect web server on http://localhost:9000');
        });
});

gulp.task('serve', ['connect', 'watch'], function () {
    require('opn')('http://localhost:9000');
});

// inject bower components
gulp.task('wiredep', function () {
    var wiredep = require('wiredep').stream;

    gulp.src('app/styles/*.scss')
        .pipe(wiredep())
        .pipe(gulp.dest('app/styles'));

    gulp.src('app/*.html')
        .pipe(wiredep({exclude: ['bootstrap-sass-official']}))
        .pipe(gulp.dest('app'));
});

gulp.task('watch', ['connect'], function () {
    $.livereload.listen();

    // watch for changes
    gulp.watch([
        'app/*.html',
        '.tmp/styles/**/*.css',
        'app/scripts/**/*.js',
        'app/images/**/*'
    ]).on('change', $.livereload.changed);

    gulp.watch('app/styles/**/*.scss', ['styles']);
    gulp.watch('bower.json', ['wiredep']);
});

gulp.task('build', ['jshint', 'html', 'images', 'fonts', 'extras'], function () {
    return gulp.src(pathToDist + '/**/*')
        .pipe($.size({title: 'build', gzip: true}));
});

gulp.task('default', function () {
    gulp.start('build');
});
