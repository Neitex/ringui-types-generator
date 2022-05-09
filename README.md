# Ring UI Types converter (generator)

Converter of Ring UI's TypeScript definitions to Kotlin definitions.
Heavily inspired by [types-kotlin](https://github.com/karakum-team/types-kotlin), but my using different approach - 
doing surface parsing of TypeScript definition and generating new Kotlin definitions instead of replacing types in original file and writing changed version to output.

_Maybe, that's why my output has so many compile errors (4228 at the moment of writing) :)_

## Running

1. Download Ring UI 5.0.0-beta.23 from NPM (https://registry.npmjs.org/@jetbrains/ring-ui/-/ring-ui-5.0.0-beta.23.tgz) and extract it somewhere.
2. Replace base paths in Main.kt to yours
3. `./gradlew run' will generate Kotlin definitions into Output dir.
4. You may try to compile output using Kotlin/JS. Example of Ring UI `build.gradle.kts` may be obtained from https://github.com/JetBrains/kotlin-wrappers/tree/master/kotlin-ring-ui.

## Contributing

Contributions are highly accepted :)

## License

Licensed under MIT License.
