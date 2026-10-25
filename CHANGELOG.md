# Changelog

The format here is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5] - 2026-10-25
### Added
- Added a `skip` parameter, also set by the user property
  `minifier.skip`, which switches minification off. #18
- Added an `encoding` parameter, also set by the user property
  `minifier.encoding`, for resources that aren't UTF-8. As in browsers,
  a resource that starts with a byte order mark is read and written in
  the encoding that gives, so a UTF-16 resource with one needs no
  setting. #20

### Changed
- Updated Minifier dependency to 2.0, which also brings the CSS fixes
  in Minifier 1.6 and the JavaScript fixes in 1.7. Minified JavaScript
  no longer starts with a line break, and minified CSS no longer ends
  with one. #19
- Comments that start with `/*!`, which mark a licence or copyright
  notice, are now kept in both CSS and JavaScript, and all others are
  removed, including the `/**` comments that CSS minification used to
  keep. #19
- A resource that can't be minified now fails the build with a message
  that says what went wrong and where, such as
  `Unterminated comment at line 4, column 1`. #19
- The `minify` goal is now marked as thread-safe, so a parallel build
  no longer warns about it. #21

### Fixed
- A resource that fails to minify is now left as it was, where
  minifying in place used to cut it short or empty it. #19
- A write error on a CSS resource now fails the build, rather than
  leaving a truncated file behind. #19
- Resources that aren't valid UTF-8 are no longer silently corrupted:
  one that isn't valid in its encoding now fails the build, saying
  where. #20

## [1.4] - 2026-09-24
### Added
- Added integration tests for minifying in place, relative paths in a
  multi-module build, a missing `sourceDir` and an empty file. #10, #11,
  #12, #13

### Changed
- Updated Minifier dependency to 1.5, which fixes a range of CSS
  minification bugs. #9
- Updated plexus-utils to 4.0.3 (GHSA-6fmv-xxpf-w3cw), and AssertJ to
  3.27.7 for tests. #8
- Tidied up the POM, removing unused site, reporting and PMD
  configuration. #8
- The integration test's POM now takes the plugin version from the
  project. #14
- Updated the test tooling, maven-plugin-plugin and the GitHub Actions
  workflows to current versions. #15, #16, #17

### Fixed
- Minifying in place, with `targetDir` the same as `sourceDir`, no
  longer empties the files. #10
- Relative `sourceDir` and `targetDir` are now resolved against the
  project's base directory rather than the working directory, which
  broke multi-module builds. #11
- A missing `sourceDir` now fails with a clear message rather than an
  internal error. #12
- The size log no longer shows a bogus percentage for empty files. #13

## [1.3] - 2026-08-09
### Changed
- Updated Minifier dependency to 1.4. #5
- MojoFailureException now logs the resource name for minification
  failure. #6

## [1.2] - 2023-12-14
### Changed
- Made some adjustments to POM to remove some Maven warnings. #2
- Updated Minifier to 1.2. #4

## [1.1] - 2023-07-07
### Changed
- Updated some dependency versions, including Minifier to 1.1.

## [1.0] - 2021-12-29
### Changed
- Updated some dependency versions.

## [0.2] - 2021-01-10
### Added
- Added a basic integration test (really just to prove the concept).
- Parent directories will now be created above output files if required.

### Changed
- Bumped [Minifier](https://github.com/logicsquad/minifier) dependency to `0.2`.

## [0.1] - 2021-01-10
Initial release.
