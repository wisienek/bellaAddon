# Building

Prereqs: Java 17, Git, internet access for dependencies.

```bash
# from repo root
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
