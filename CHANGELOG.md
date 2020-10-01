# Change Log
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/) and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [0.3.0] - 2017-02-15
### Added
- `lein-shade`: added `install-shaded-jar` task to install artifacts to local repositories.

### Fixed
- `shade`: handle loader classes (`*__init.class`) and function classes (`*$fn__1234.class`) in the top-level namespace.

## [0.2.0] - 2017-02-14
### Fixed
- `shade`: avoid matching partial package names.

## 0.1.0 - 2017-02-14
### Added
- `lein-shade`: a Leiningen plugin for relocating namespaces within uberjars.
- `shade`: a library to perform the relocations.

[Unreleased]: https://github.com/haines/shade/compare/0.3.0...HEAD
[0.3.0]: https://github.com/haines/shade/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/haines/shade/compare/0.1.0...0.2.0
