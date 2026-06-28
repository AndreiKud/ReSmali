Integration-test resource layout:

- `test-app/app-debug.apk` - APK installed by `AdbFixture`.
- `smali-project/` - smali sources opened as IDE project by Starter.

Integration tests use `IntegrationTestConfig` defaults from
`src/integrationTest/kotlin/dev/resmali/integration/config/IntegrationTestConfig.kt`.

Override values in code when needed, for example:

```kotlin
val config = IntegrationTestConfig(
    ideProductCode = "IC",
    ideBuildType = IdeBuildType.RELEASE,
    jdwpLocalPort = 8710,
    breakpointLine = 145,
)
```
