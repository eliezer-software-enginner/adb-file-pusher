# AGENTS.md

This file contains guidelines and commands for agentic coding agents working in this repository.

## Build System Commands

This project uses Gradle with Kotlin DSL (`build.gradle.kts`).

### Core Commands
- `./gradlew clean` - Clean build artifacts
- `./gradlew compileJava` - Compile Java source code
- `./gradlew jar` - Build JAR file
- `./gradlew test` - Run all tests
- `./gradlew createInstaller` - Build Linux .deb installer
- `./gradlew copyDependencies` - Copy runtime dependencies to build/dependencies

### Development Commands
- `./gradlew build` - Full build including test
- `./gradlew run` - Run the application (if configured)
- `./gradlew publishToMavenLocal` - Publish to local Maven repository

### Testing
- `./gradlew test` - Run all tests using JUnit 5
- No single test command available - tests are run as a batch

## Project Structure

This is a JavaFX desktop application for ADB file pushing with hot reload capabilities.

### Key Directories
- `src/main/java/my_app/` - Main application code
- `src/main/java/my_app/hotreload/` - Hot reload system
- `scripts/linux/` - Linux installer scripts
- `build/dependencies/` - Runtime dependencies (copied via task)

### Main Components
- `Main.java` - Application entry point with JavaFX setup
- `UI.java` - Main UI component with ADB operations
- Hot reload system in `my_app.hotreload` package

## Code Style Guidelines

### Java Conventions
- Java 25 target (toolchain configured)
- Use modern Java features (virtual threads, records, etc.)
- Package naming: `my_app` for main code
- Import organization: standard Java conventions

### JavaFX Specific
- Use JavaFX 17 modules: `javafx.controls`, `javafx.graphics`
- Component-based UI using custom `megalodonte.components` library
- State management with `State<T>` class
- Reactive patterns with `.map()` transformations

### Dependencies
- Custom libraries from `megalodonte` namespace
- JUnit 5 + Mockito for testing
- Shadow plugin for fat JAR creation
- JavaFX plugin for module management

### Error Handling
- Use `Platform.runLater()` for UI thread operations
- Virtual threads for background operations (ADB commands)
- Proper exception handling in process execution
- Alert dialogs for user feedback

### Naming Conventions
- Classes: PascalCase (e.g., `UIReloaderImpl`)
- Methods: camelCase (e.g., `findDevices`)
- Variables: camelCase with descriptive names
- Constants: UPPER_SNAKE_CASE
- State variables: descriptive with type suffix (e.g., `folderDestination`)

### Hot Reload System
- Classes annotated with `@CoesionApp` participate in hot reload
- Exclusion set prevents reloading certain classes
- Custom classloader isolation for reloadable components
- File watching with debouncing to prevent duplicate reloads

### ADB Integration
- Use `ProcessBuilder` for ADB command execution
- Virtual threads for non-blocking operations
- Progress tracking with `State<Integer>`
- Proper stream handling for process output

### UI Patterns
- Component composition using custom library
- Props pattern for component configuration
- Conditional rendering with `Show.when()`
- Layout components: `Column`, `Row`, `SpacerVertical`

### Build Configuration
- Gradle Kotlin DSL for build scripts
- Module path detection for JavaFX compilation
- Fat JAR creation with Shadow plugin
- Linux installer generation via custom task

### Testing Guidelines
- Unit tests with JUnit 5
- Mockito for mocking dependencies
- Test resources in `src/test/resources/`
- No integration tests currently configured

## Development Notes

### Hot Reload Development
- Set `devMode = true` in `Main.java` to enable hot reload
- Hot reload monitors `src/main/java/my_app` directory
- Excludes core classes to prevent instability
- Requires proper module visibility configuration

### ADB Command Patterns
- All ADB commands run in virtual threads
- Use `BufferedReader` for process output
- Update UI state via `Platform.runLater()`
- Handle process completion and errors gracefully

### Component Library Usage
- Follow existing component patterns in `UI.java`
- Use props objects for configuration
- Leverage state management for reactive updates
- Maintain consistent spacing and padding

## Platform-Specific Notes

### Linux Development
- Installer creation uses `jpackage` via shell script
- Application installs to `/opt/adb-file-pusher/`
- Executable: `/opt/adb-file-pusher/bin/adb_file_pusher`
- Icon copying handled in installer script

### Module System
- Java Module System (JPMS) enabled
- Requires proper `opens` directives for reflection
- Module path detection for compilation
- Custom classloader for hot reload isolation