# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [Unreleased]
### Fixed
- Fix inconsistent store state in very specific task cancellation scenario, which would result in `RuntimeException`s when deleting unobserved tasks.


## [0.19.8] - 2022-03-09
### Changed
- Moved graph code into a separate project `pie.graph`.
- - `common` requirement to `0.10.2`.

### Fixed
- Calling `Pie.close` causing the `Store` to be closed multiple times.
- Unnecessary `throws Exception` in `SerializingStoreInMemoryBuffer.close` method definition.
- Don't log observability changes if both top-down and bottom-up are disabled.


## [0.19.7] - 2021-12-13
### Changed
- Do not schedule deferred task if it is not affected.
- `MetricsTracer` to have a mode where it automatically resets itself when a new build starts.
- `LoggingTracer` to accept a `MetricsTracer` which it uses to log task execution durations.


## [0.19.6] - 2021-11-23
### Changed
- `common` requirement to `0.10.1`.


## [0.19.5] - 2021-11-11
### Changed
- `common` requirement to `0.10.0`.


## [0.19.4] - 2021-11-10
### Changed
- Make it possible to implement a different Java compiler for the `CompileJava` task with the `JavaCompiler` interface.
- `resource` requirement to `0.13.2`.


## [0.19.3] - 2021-11-04
### Changed
- `common` requirement to `0.9.9`.

### Added
- `requireMapping` overloads to `ExecContext` which require a task and then transform the output with a mapping function, which is also used as a function output stamper (`FuncEqualsOutputStamper`). These overloads make it convenient to depend on a subset of the output of a task.


## [0.19.2] - 2021-10-22
### Fixed
- `null` being set as the output of a new task that failed (due to an exception) or was interrupted, which was then subsequently returned as the output of that task.


## [0.19.1] - 2021-10-19
### Fixed
- Tasks data for new tasks (i.e., tasks that have not been executed before) not being reset on cancel or failure.
- Task data not being restored when task throws a `Throwable`.
- `UncheckedInterruptedException` and `CanceledException` not being handled as an interrupt/cancel.


## [0.19.0] - 2021-10-18
### Removed
- `Pie#isObserved`, use `Session#isObserved` instead.
- `Pie#isExplicitlyObserved`, use `Session#isExplicitlyObserved` instead.
- `Pie#setImplicitToExplicitlyObserved`, use `Session#setImplicitToExplicitlyObserved` instead.
- `Pie#setCallback`, use `Session#setCallback` instead.
- `Pie#removeCallback`, use `Session#removeCallback` instead.
- `Pie#dropCallbacks`, use `Session#dropCallbacks` instead.

### Changed
- `resource` requirement to `0.13.0`.

### Deprecated
- `Pie#hasBeenExecuted`, use `Session#hasBeenExecuted` instead.
- `Pie#dropStore`, use `Session#dropStore` instead.

### Added
- `Session#setSerializableCallback` for setting a callback that is serializable, so it can be stored in the `Store` to survive recreating of the `Pie` instance.


## [0.18.1] - 2021-10-12
### Fixed
- Incorrect task data after task execution fails (due to an exception) or is interrupted. The previous data of the task is now restored.


## [0.18.0] - 2021-10-11
### Changed
- The void resource dependency methods in `ExecContext` now return a boolean indicating whether the resource is changed or whether the dependency is new compared to the last execution of the task.
- `resource` requirement to `0.12.0`.
- `common` requirement to `0.9.8`.

### Added
- Tasks can get/set/clear an internal object with `getInternalObject`, `setInternalObject`, and `clearInternalObject` on `ExecContext`. Internal objects are stored and serialized along with the PIE store.
- Tasks can inspect their previous input, output, and dependencies with `getPreviousInput`, `getPreviousOutput`,`getPreviousTaskRequireDeps`, `getPreviousResourceRequireDeps`, and `getPreviousResourceProvideDeps` in `ExecContext`.


## [0.17.0] - 2021-10-01
### Changed
- `Serde` and implementations allow passing in `ClassLoader`s for all deserialization methods.
- `SerializingStore` to be instantiated with a `SerializingStoreBuilder` to reduce the number of its constructors, easing its creation.
- `SerializingStore` can now serialize/deserialize from `BufferedOutputStream`/`BufferedInputStream` instead of just resources.
- `common` requirement to `0.9.7`.

### Added
- In-memory buffer convenience to `SerializingStoreBuilder`.
- `MockExecContext` to test methods that require an `ExecContext`, which just executes tasks and ignores dependencies.


## [0.16.8] - 2021-09-20
### Changed
- `common` requirement to `0.9.5`.
- `Pie` to only allow a single session to guarantee thread-safety.

### Added
- `tryNewSession` method to `Pie` that creates a new session if no session exist yet, or returns `Optional.empty()` if a session already exists. This is useful for actions that require a session but can be skipped, such as the reference resolution and hover tooltip editor services.


## [0.16.7] - 2021-09-10
### Changed
- Validation is now performed immediately after creating a resource dependency, instead of performing this after task execution. This catches a couple of hidden dependencies which were not caught before. For example, requiring a resource before requiring the task that provides it in the same task now causes a hidden dependency error.
- Dependencies are now immediately stored when created. This fixes several issues where a hidden dependency was detected even though at that exact moment, it was not a hidden dependency.


## [0.16.6] - 2021-09-01
### Changed
- `resource` requirement to `0.11.5`.
- `common` requirement to `0.9.3`.


[Unreleased]: https://github.com/metaborg/pie/compare/release-0.19.8...HEAD
[0.19.8]: https://github.com/metaborg/pie/compare/release-0.19.7...release-0.19.8
[0.19.7]: https://github.com/metaborg/pie/compare/release-0.19.6...release-0.19.7
[0.19.6]: https://github.com/metaborg/pie/compare/release-0.19.5...release-0.19.6
[0.19.5]: https://github.com/metaborg/pie/compare/release-0.19.4...release-0.19.5
[0.19.4]: https://github.com/metaborg/pie/compare/release-0.19.3...release-0.19.4
[0.19.3]: https://github.com/metaborg/pie/compare/release-0.19.2...release-0.19.3
[0.19.2]: https://github.com/metaborg/pie/compare/release-0.19.1...release-0.19.2
[0.19.1]: https://github.com/metaborg/pie/compare/release-0.19.0...release-0.19.1
[0.19.0]: https://github.com/metaborg/pie/compare/release-0.18.1...release-0.19.0
[0.18.1]: https://github.com/metaborg/pie/compare/release-0.18.0...release-0.18.1
[0.18.0]: https://github.com/metaborg/pie/compare/release-0.17.0...release-0.18.0
[0.17.0]: https://github.com/metaborg/pie/compare/release-0.16.8...release-0.17.0
[0.16.8]: https://github.com/metaborg/pie/compare/release-0.16.7...release-0.16.8
[0.16.7]: https://github.com/metaborg/pie/compare/release-0.16.6...release-0.16.7
[0.16.6]: https://github.com/metaborg/pie/compare/release-0.16.5...release-0.16.6
