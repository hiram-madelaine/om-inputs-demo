module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        less: {
              css: {
                src: ['src/css/app.less'],
                dest: 'target/app-less.css',
              }
            },
         autoprefixer: {
             options: {
                    browsers: ['last 3 versions']
             },
              single_file: {
                   options: {
                     // Target-specific options go here.
                   },
                   src: 'target/app-less.css',
                   dest: 'resources/public/css/style.css'
                 }
         },
        concat: {
            css: {
                src: ['resources/public/css/bootstrap.min.css',
                        'resources/public/css/forms.css',
                        'resources/public/css/datepicker.css',
                        'resources/public/css/popupdatepicker.css',
                        'resources/public/css/codemirror/codemirror.css',
                        'resources/public/css/style.css'],
                dest: 'resources/public/css/release.css'
            }},
        cssmin: {
            css:{
                src: 'resources/public/css/release.css',
                dest: 'resources/public/css/release.min.css'
            }
        },
        watch: {
            files: ['src/css/app.less'],
            tasks: ['less', 'autoprefixer']
        }

    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-autoprefixer');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.registerTask('default', ['less', 'autoprefixer','concat','cssmin' ]);
};
