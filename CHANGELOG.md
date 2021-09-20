# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [Unreleased]


## [0.16.8]
### Changed
- Common to 0.9.5.
- `Pie` to only allow a single session to guarantee thread-safety.

### Added
- `tryNewSession` method to `Pie` that creates a new session if no session exist yet, or returns `Optional.empty()` if a session already exists. This is useful for actions that require a session but can be skipped, such as the reference resolution and hover tooltip editor services.


## [0.16.7]
### Changed
- Validation is now performed immediately after creating a resource dependency, instead of performing this after task execution. This catches a couple of hidden dependencies which were not caught before. For example, requiring a resource before requiring the task that provides it in the same task now causes a hidden dependency error.
- Dependencies are now immediately stored when created. This fixes several issues where a hidden dependency was detected even though at that exact moment, it was not a hidden dependency.


## [0.16.6]
### Changed
- Update resource dependency to 0.11.5.
- Update common dependency to 0.9.3.


[Unreleased]: https://github.com/metaborg/pie/compare/release-0.16.8...HEAD
[0.16.8]: https://github.com/metaborg/pie/compare/release-0.16.7...release-0.16.8
[0.16.7]: https://github.com/metaborg/pie/compare/release-0.16.6...release-0.16.7
[0.16.6]: https://github.com/metaborg/pie/compare/release-0.16.5...release-0.16.6
