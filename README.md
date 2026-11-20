## PlatformUtils

### Developer Usage

Maven: 
```xml
<repository>
  <id>cleanroommc</id>
  <name>CleanroomMC</name>
  <url>https://maven.cleanroommc.com</url>
</repository>
```

```xml
<dependency>
    <groupId>com.cleanroommc</groupId>
    <artifactId>platform-utils</artifactId>
    <version>3.0.0</version>
</dependency>
```

Gradle:

```groovy
repositories {
    maven {
        name 'CleanroomMC'
        url 'https://maven.cleanroommc.com'
    }
}

dependencies {
    implementation 'com.cleanroommc:platform-utils:3.0.0'
}
```
### Usage

```java
Platform platform = Platform.current();
platform.id();                  // "linux-arm64", "windows-x64", "macos-arm64", ...
platform.operatingSystem();     // OperatingSystem.LINUX
platform.architecture();        // Architecture.ARM64
platform.executable("java");    // "java.exe" on Windows, "java" elsewhere

Platform.parse("freebsd-x64");  // Platform ids round trip, "linux-x64-musl" keeps LibC.MUSL
OperatingSystem.parse("SunOS"); // Parse raw os.name values
Architecture.parse("aarch64");  // Parse raw os.arch values

platform.libC();                // LibC.MUSL on Alpine, LibC.GLIBC on most other Linux distributions
platform.glibcVersion();        // Optional of "2.39", read once through getconf
platform.id(platform.libC());   // "linux-x64-glibc", "linux-x64-musl", "windows-x64", ...
platform.osRelease();           // Optional of /etc/os-release, with id(), versionId(), isLike("debian"), ...

platform.translationLayer();    // TranslationLayer.ROSETTA for an x64 JVM on Apple silicon, WOW64, PRISM or NONE
platform.machineArchitecture(); // Architecture.ARM64 under Rosetta, while platform.architecture() is X64
platform.wsl();                 // WSL.WSL1 or WSL.WSL2 under Windows Subsystem for Linux, WSL.NONE elsewhere
platform.hasWslInterop();       // True when Windows executables can run, false inside containers on WSL2
```

`Architecture` describes the JVM, not the machine. An x64 JVM under Rosetta 2 reports `x64`, which is what native libraries have to match.

### Testing

`Platform` reads every system property, environment variable, file and command through its own inputs. Build one with fake inputs to test Windows or macOS handling on any CI runner:

```java
Platform windowsOnArm = Platform.from(
        Map.of("os.name", "Windows 11", "os.arch", "x86"),
        Map.of("PROCESSOR_ARCHITEW6432", "ARM64"));
windowsOnArm.translationLayer();    // TranslationLayer.WOW64

Platform alpine = Platform.builder()
        .property("os.name", "Linux")
        .property("os.arch", "amd64")
        .file("/proc/self/maps", "7f00-7f01 r-xp 00000000 08:01 1 /lib/ld-musl-x86_64.so.1")
        .file("/etc/os-release", "ID=alpine\nVERSION_ID=3.20.3")
        .build();
alpine.libC();                      // LibC.MUSL
```

Your own code can read through `platform.property(...)`, `platform.environment(...)`, `platform.readLines(...)` and `platform.run(...)` to stay testable the same way.
