

var gulp = require('gulp');
var gulpTypings = require("gulp-typings");

var ts = require('gulp-typescript');
var tsProject = ts.createProject("tsconfig.json");


gulp.task('default', ['typings', 'tsc']);

gulp.task('tsc', function(){
	return gulp.src('./yo/app/**/**.ts').pipe(tsProject()).js.pipe(gulp.dest('./yo/app'));
});

gulp.task('typings', function (callback) {
  return gulp.src("./typings.json").pipe(gulpTypings());
});