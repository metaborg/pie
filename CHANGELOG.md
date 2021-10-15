# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [Unreleased]
### Added
- `Session#setSerializableCallback` for setting a callback that is serializable, so it can be stored in the `Store` to survive recreating of the `Pie` instance.

### Removed
- `Pie#isObserved`, use `Session#isObserved` instead.
- `Pie#isExplicitlyObserved`, use `Session#isExplicitlyObserved` instead.
- `Pie#setImplicitToExplicitlyObserved`, use `Session#setImplicitToExplicitlyObserved` instead.
- `Pie#setCallback`, use `Session#setCallback` instead.
- `Pie#removeCallback`, use `Session#removeCallback` instead.
- `Pie#dropCallbacks`, use `Session#dropCallbacks` instead.

### Deprecated
- `Pie#hasBeenExecuted`, use `Session#hasBeenExecuted` instead.
- `Pie#dropStore`, use `Session#dropStore` instead.


## [0.18.1] - 2021-10-12
### Fixed
- Incorrect task data after task execution fails (due to an exception) or is interrupted. The previous data of the task is now restored.


## [0.18.0] - 2021-10-11
### Added
- Tasks can get/set/clear an internal object with `getInternalObject`, `setInternalObject`, and `clearInternalObject` on `ExecContext`. Internal objects are stored and serialized along with the PIE store.
- Tasks can inspect their previous input, output, and dependencies with `getPreviousInput`, `getPreviousOutput`,`getPreviousTaskRequireDeps`, `getPreviousResourceRequireDeps`, and `getPreviousResourceProvideDeps` in `ExecContext`.

### Changed
- The void resource dependency methods in `ExecContext` now return a boolean indicating whether the resource is changed or whether the dependency is new compared to the last execution of the task.
- `resource` requirement to `0.12.0`.
- `common` requirement to `0.9.8`.


## [0.17.0] - 2021-10-01
### Changed
- `Serde` and implementations allow passing in `ClassLoader`s for all deserialization methods.
- `SerializingStore` can now serialize/deserialize from `BufferedOutputStream`/`BufferedInputStream` instead of just resources.
- `common` requirement to 0.9.7.

### Added
- `SerializingStoreBuilder` to ease the creation of `SerializingStore`s and to reduce the number of its constructors.
- In-memory buffer convenience to `SerializingStoreBuilder`.
- `MockExecContext` to test methods that require an `ExecContext`, which just executes tasks and ignores dependencies.


## [0.16.8] - 2021-09-20
### Changed
- Common to 0.9.5.
- `Pie` to only allow a single session to guarantee thread-safety.

### Added
- `tryNewSession` method to `Pie` that creates a new session if no session exist yet, or returns `Optional.empty()` if a session already exists. This is useful for actions that require a session but can be skipped, such as the reference resolution and hover tooltip editor services.


## [0.16.7] - 2021-09-10
### Changed
- Validation is now performed immediately after creating a resource dependency, instead of performing this after task execution. This catches a couple of hidden dependencies which were not caught before. For example, requiring a resource before requiring the task that provides it in the same task now causes a hidden dependency error.
- Dependencies are now immediately stored when created. This fixes several issues where a hidden dependency was detected even though at that exact moment, it was not a hidden dependency.


## [0.16.6] - 2021-09-01
### Changed
- Update resource dependency to 0.11.5.
- Update common dependency to 0.9.3.


[Unreleased]: https://github.com/metaborg/pie/compare/release-0.18.1...HEAD
[0.18.1]: https://github.com/metaborg/pie/compare/release-0.18.0...release-0.18.1
[0.18.0]: https://github.com/metaborg/pie/compare/release-0.17.0...release-0.18.0
[0.17.0]: https://github.com/metaborg/pie/compare/release-0.16.8...release-0.17.0
[0.16.8]: https://github.com/metaborg/pie/compare/release-0.16.7...release-0.16.8
[0.16.7]: https://github.com/metaborg/pie/compare/release-0.16.6...release-0.16.7
[0.16.6]: https://github.com/metaborg/pie/compare/release-0.16.5...release-0.16.6
