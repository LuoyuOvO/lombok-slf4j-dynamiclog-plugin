# IDE Lombok 配置说明

## 问题说明

如果 IDE 中无法识别 Lombok 生成的 getter/setter 方法或 `log` 字段，需要安装并启用 Lombok 插件。

## IntelliJ IDEA 配置

### 1. 安装 Lombok 插件

1. 打开 IDEA
2. 进入 `File` -> `Settings` (Windows/Linux) 或 `IntelliJ IDEA` -> `Preferences` (Mac)
3. 选择 `Plugins`
4. 搜索 "Lombok"
5. 安装 "Lombok" 插件
6. 重启 IDEA

### 2. 启用注解处理器

1. 进入 `File` -> `Settings` -> `Build, Execution, Deployment` -> `Compiler` -> `Annotation Processors`
2. 勾选 `Enable annotation processing`
3. 点击 `Apply` 和 `OK`

### 3. 重新导入 Maven 项目

1. 右键点击 `pom.xml`
2. 选择 `Maven` -> `Reload Project`

## Eclipse 配置

### 1. 安装 Lombok

1. 下载 Lombok jar 文件：https://projectlombok.org/download
2. 运行：`java -jar lombok.jar`
3. 选择 Eclipse 安装目录
4. 点击 `Install/Update`
5. 重启 Eclipse

### 2. 启用注解处理器

1. 右键项目 -> `Properties`
2. 选择 `Java Compiler` -> `Annotation Processing`
3. 勾选 `Enable annotation processing`
4. 点击 `Apply`

## VS Code 配置

### 1. 安装扩展

1. 打开 VS Code
2. 安装 "Language Support for Java(TM) by Red Hat" 扩展
3. 安装 "Lombok Annotations Support for VS Code" 扩展

### 2. 配置 settings.json

```json
{
    "java.jdt.ls.lombokSupport.enabled": true
}
```

## 验证 Lombok 是否工作

编译后的类文件应该包含以下方法（以 `DynamicLogConfig` 为例）：

```bash
javap -cp target/classes com.luoyu.dynamiclog.config.DynamicLogConfig
```

应该能看到：
- `getEnabled()`, `setEnabled()`
- `getNacosServerAddr()`, `setNacosServerAddr()`
- 等等所有 getter/setter 方法

## Maven 编译验证

运行以下命令验证 Lombok 在编译时正常工作：

```bash
mvn clean compile
```

如果编译成功，说明 Lombok 注解处理器正常工作。

## 常见问题

### 1. IDE 中看不到生成的方法

- 确保安装了 Lombok 插件
- 确保启用了注解处理器
- 重新导入 Maven 项目
- 重启 IDE

### 2. 编译错误：找不到符号

- 检查 `pom.xml` 中是否有 Lombok 依赖
- 确保 Lombok 版本与 Java 版本兼容
- 运行 `mvn clean compile` 重新编译

### 3. @Slf4j 生成的 log 字段找不到

- 确保类上有 `@Slf4j` 注解
- 确保安装了 Lombok 插件
- 重新编译项目

## 当前项目 Lombok 配置

- Lombok 版本：1.18.28
- Java 版本：1.8
- Scope：provided（编译时使用，运行时不需要）

Lombok 在编译时会自动处理注解，生成相应的代码。
