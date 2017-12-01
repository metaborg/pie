# PIE: Pipelines for Interactive Environments

PIE is a DSL, API, and runtime for developing interactive software development pipelines.

## Components

The DSL is implemented in [Spoofax](http://spoofax.org/) and is located at [lang/spec](lang/spec), with several examples at [lang/example](lang/example).
The API and runtime is implemented in Kotlin, and is located at [runtime/core](runtime/core), with several builtin pipeline functions located at [runtime/builtin](runtime/builtin).
We use the filesystem and logging abstraction from [metaborg/util](https://github.com/metaborg/util).
