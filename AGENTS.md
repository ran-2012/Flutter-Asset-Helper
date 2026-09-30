# Flutter Asset Helper 开发约定

## 项目概况

Flutter Asset Helper 是 IntelliJ Platform 插件，为 Flutter/Dart 项目提供资源文件补全、预览、跳转和引用生成能力。

- 插件 ID：`dev.ran.plugins.flutterex`
- 构建系统：Gradle Kotlin DSL
- 源码语言：Kotlin、Java
- 目标平台：IntelliJ IDEA 2025.3
- 兼容范围：build `253` 至 `261.*`
- 必需运行时：JDK 21
- 外部插件依赖：Dart、Flutter

版本和兼容范围以 `gradle.properties` 为准。依赖版本以 `gradle/libs.versions.toml` 和 `build.gradle.kts` 为准。

## 目录

- `src/main/kotlin/dev/ran/plugins/flutterex/`：补全、跳转、监听器、服务和工具窗口。
- `src/main/kotlin/com/shenyong/flutter/psi/`：资源 PSI 工具。
- `src/main/java/com/shenyong/flutter/`：资源引用生成、检查和 PSI 扩展。
- `src/main/resources/META-INF/plugin.xml`：插件 ID、依赖、扩展点、服务和 Action 注册。
- `src/test/kotlin/`：IntelliJ Platform 测试。
- `src/test/testData/`：测试输入与期望文件。
- `release/`：本地发布任务生成的可安装 ZIP；不要手工维护构建产物。

## 修改规则

- 保持 Kotlin 和 Java 现有包边界，不做与任务无关的迁移或重命名。
- 修改扩展点、服务、监听器或 Action 时，同步检查 `plugin.xml`。
- 修改插件依赖或 IntelliJ Platform 版本时，同时检查 `pluginSinceBuild`、`pluginUntilBuild`、Dart 和 Flutter 插件版本。
- 不要仅放宽 `pluginUntilBuild` 后宣称运行时兼容；至少完成构建和相关测试，跨平台版本升级时运行对应 IDE 沙箱。
- 保留 Java 源文件的 UTF-8 编译配置。
- 不要恢复已移除的 IntelliJ Platform Gradle Plugin 1.x DSL，例如 `create(platformType, version)` 或 `instrumentationTools()`。
- 不要提交 `.intellijPlatform/`、`build/` 或其他生成目录。

## 测试

在 Windows PowerShell 中使用 JDK 21：

```powershell
$env:JAVA_HOME='C:\Users\ran\.gradle\jdks\jetbrains_s_r_o_-21-amd64-windows.2'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

执行定向测试：

```powershell
.\gradlew.bat test --tests "dev.ran.plugins.flutterex.MyPluginTest.testRename" --console plain
```

执行完整验证：

```powershell
.\gradlew.bat build --console plain
```

修改测试数据路径时，注意 IntelliJ 2025.3 的 VFS 根访问限制。访问仓库内测试数据前应通过绑定 `testRootDisposable` 的 `VfsRootAccess.allowRootAccess` 放行规范化绝对路径。

## 沙箱

启动开发 IDE：

```powershell
.\gradlew.bat runIde --console plain
```

确认日志中存在 `Loaded custom plugins`，并列出 Dart、Flutter 和 Flutter Asset Helper。宿主 IDE 的 Ultimate 模块、WSL、Grazie 或 Kubernetes 告警不能直接归因于本插件；按插件 ID、包名和 `Plugin to blame` 判断来源。

## 发布

插件版本由 `gradle.properties` 中的 `pluginVersion` 控制。

生成并复制本地发布包：

```powershell
.\gradlew.bat publishPluginLocal --console plain
```

`publishPluginLocal` 必须：

- 依赖 `buildPlugin`，生成 IntelliJ 可安装 ZIP，而不是普通 JAR。
- 删除 `release/` 中旧的 `.jar` 和 `.zip`。
- 将当前版本的 `build/distributions/*.zip` 复制到 `release/`。
- 兼容 Gradle configuration cache，不在任务执行阶段访问 `project`。

发布前检查 ZIP 内主插件 JAR 的 `META-INF/plugin.xml`，确认 `version`、`since-build` 和 `until-build` 与 `gradle.properties` 一致。

## 完成标准

- 先运行覆盖改动行为的最小测试。
- 再运行受影响范围的编译、测试或完整 `build`。
- 发布任务变更必须实际运行 `publishPluginLocal`，并确认 `release/` 只保留当前 ZIP。
- 不修复与当前任务无关的宿主 IDE 告警或弃用警告，但应在结果中说明未处理的风险。