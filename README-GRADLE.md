# Gradle Build Setup for BellaAddon

## Prerequisites

This project requires **Java 17** (required for Minecraft 1.20.1).

## Building the Plugin

### Option 1: Using Gradle Wrapper (Recommended - No Installation Needed)

The project includes a Gradle wrapper, so you don't need to install Gradle separately:

```bash
# Build the plugin
.\gradlew.bat build

# The JAR file will be in: build\libs\belladdon-1.2.9.jar
```

### Option 2: If You Have Java 25+ Issues

If you're getting "Unsupported class file major version 69" errors, you need to use Java 17:

1. **Install Java 17** (if not already installed):
   - Download from: https://adoptium.net/temurin/releases/?version=17
   - Or use: `winget install EclipseAdoptium.Temurin.17.JDK`

2. **Set JAVA_HOME** to point to Java 17:
   ```powershell
   # Find Java 17 installation
   $java17 = Get-ChildItem "C:\Program Files\Java" | Where-Object { $_.Name -like "*17*" } | Select-Object -First 1
   
   # Set JAVA_HOME (temporary for current session)
   $env:JAVA_HOME = $java17.FullName
   
   # Or set permanently:
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $java17.FullName, "User")
   ```

3. **Or configure Gradle to use Java 17**:
   Edit `gradle.properties` and set:
   ```
   org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
   ```
   (Replace with your actual Java 17 path)

## Build Commands

- `.\gradlew.bat build` - Build the plugin
- `.\gradlew.bat clean` - Clean build artifacts
- `.\gradlew.bat shadowJar` - Create the shaded JAR (includes dependencies)
- `.\gradlew.bat --version` - Check Gradle version

## Output

The built plugin JAR will be located at:
```
build\libs\belladdon-1.2.9.jar
```

This JAR can be placed directly in your Magma 1.20.1 server's `plugins` folder.

## Troubleshooting

### "Unsupported class file major version" Error
- You're using Java 25, but need Java 17
- Install Java 17 and configure Gradle to use it (see above)

### "Gradle wrapper not found"
- The wrapper should be included in the project
- If missing, run: `gradle wrapper` (if you have Gradle installed)

### Build Fails with Dependency Errors
- Make sure you have internet connection (Gradle downloads dependencies)
- Check that all repositories in `build.gradle` are accessible


