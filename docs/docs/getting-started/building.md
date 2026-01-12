# Building

Prereqs: Java 17, Git, internet access for dependencies.

## Java Configuration

Gradle uses `JAVA_HOME` environment variable or system path to find Java. Options:

**Option 1: Set JAVA_HOME (recommended)**
```powershell
# Windows (PowerShell) - add to your profile or set permanently
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```
```bash
# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

**Option 2: Local gradle properties (gitignored)**

Create `gradle-local.properties` in the repo root:
```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
```
Then reference it from your IDE or terminal. This file is gitignored so it won't affect CI.

## Build Commands

```bash
# from repo root (Windows: use gradlew.bat, Linux/macOS: ./gradlew)
./gradlew clean build shadowJar
```

Outputs:
- Shaded plugin JAR: `build/libs/belladdon-<version>.jar`
- JavaDoc: `build/docs/javadoc`

Useful tasks:
- `./gradlew clean` - remove build outputs
- `./gradlew javadoc` - generate API docs
- `./gradlew downloadDependencies` - warm dependency cache

Docs preview:
```bash
pip install mkdocs mkdocs-material
mkdocs serve
# open http://127.0.0.1:8000
```

CI notes:
- GitHub Actions runs on Linux; wrapper path is `./gradlew` (made executable in workflows).
