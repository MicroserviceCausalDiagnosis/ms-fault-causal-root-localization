# Working diary

## 第一周 搭建微服务demo

### **第一阶段：准备工作（使用 Spring Initializr 创建项目）**

我们将创建两个独立的 Spring Boot 项目。

#### **1. 创建 `user-service` 项目**

1. **访问 Spring Initializr：** 打开浏览器，前往 [https://start.spring.io](https://start.spring.io/)。

<img src="C:\Users\Noah arh\AppData\Roaming\Typora\typora-user-images\image-20250919195040639.png" alt="image-20250919195040639" style="zoom:25%;" />

1. **配置项目信息：**
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 选择最新的稳定版（如 3.2.5）
   - **Group:** `com.example` (你可以改为自己的域名反写)
   - **Artifact:** `user-service`
   - **Packaging:** Jar
   - **Java:** 17 或 21
2. **添加依赖：**
   - 在 "Dependencies" 部分，点击 "ADD DEPENDENCIES"，搜索并添加：
     - **Spring Web**: 提供 RESTful API 支持。
3. **生成项目：** 点击右下角的 "GENERATE" 按钮，下载生成的压缩包。
4. **解压并导入 IDE：** 将压缩包解压到你的工作目录，然后用 IntelliJ IDEA 或 Eclipse 打开该项目。

#### **2. 创建 `order-service` 项目**

重复上述步骤，但在第2步中做出以下改变：

- **Artifact:** `order-service`
- **添加依赖：**
  - **Spring Web**
  - **OpenFeign**: 这是实现服务调用的核心依赖。

![image-20250919195141699](C:\Users\Noah arh\AppData\Roaming\Typora\typora-user-images\image-20250919195141699.png)

将两个压缩包解压并用IDEA打开：

![image-20250919195625279](C:\Users\Noah arh\AppData\Roaming\Typora\typora-user-images\image-20250919195625279.png)

如果不想使用Spring Initializr创建springboot项目也可以直接在IDEA中创建：

<img src="C:\Users\Noah arh\AppData\Roaming\Typora\typora-user-images\image-20250919195905126.png" alt="image-20250919195905126" style="zoom:33%;" />

### **第二阶段：编写业务代码**

#### **1. 开发 `user-service` (提供服务方)**

这个服务提供一个根据用户ID查询用户信息的简单接口。

1. **创建 UserController：**
   在 `user-service/src/main/java/com/example/userservice/` 目录下，创建 `UserController.java` 文件。

   java

   ```java
   package com.example.userservice;
   
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;
   import org.springframework.web.bind.annotation.RestController;
   
   @RestController
   public class UserController {
   
       // 简单模拟，根据ID返回用户信息
       @GetMapping("/users/{id}")
       public String getUserById(@PathVariable String id) {
           // 在实际应用中，这里会从数据库查询
           return "User " + id + " (From user-service)";
       }
   }
   ```

   

2. **配置应用端口：**
   为了避免两个服务端口冲突，我们需要为 `user-service` 指定一个端口。
   打开 `user-service/src/main/resources/application.properties` 文件，添加一行：

   properties

   ```
   server.port=8081
   ```

   

3. **启动测试：**
   运行 `UserServiceApplication.java` 中的 `main` 方法启动服务。
   打开浏览器访问 `http://localhost:8081/users/123`，你应该能看到 `User 123 (From user-service)`。这说明 `user-service` 配置成功。

#### **2. 开发 `order-service` (服务调用方)**

这个服务将通过 OpenFeign 调用 `user-service` 的接口。

1. **启用 Feign 客户端：**
   在 `order-service` 的主启动类 `OrderServiceApplication.java` 上添加 `@EnableFeignClients` 注解。

   java

   ```java
   package com.example.orderservice;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.openfeign.EnableFeignClients;
   
   @SpringBootApplication
   @EnableFeignClients // 添加这个注解
   public class OrderServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(OrderServiceApplication.class, args);
       }
   }
   ```

   

2. **声明 Feign 客户端接口：**
   在 `order-service` 中创建一个新的接口 `UserClient.java`。这个接口定义了如何调用 `user-service` 的 API。

   java

   ```java
   package com.example.orderservice.client;
   
   import org.springframework.cloud.openfeign.FeignClient;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;
   
   @FeignClient(name = "user-service", url = "localhost:8081") 
   // name: 服务标识，可自定义
   // url: 明确指定user-service的地址
   public interface UserClient {
   
       // 这个定义必须和user-service的Controller接口完全一致
       @GetMapping("/users/{id}")
       String getUserById(@PathVariable String id);
   }
   ```

   

3. **创建 OrderController：**
   在 `order-service` 中创建 `OrderController.java`，并注入上面定义的 `UserClient` 进行调用。

   java

   ```java
   package com.example.orderservice;
   
   import com.example.orderservice.client.UserClient;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;
   import org.springframework.web.bind.annotation.RestController;
   
   @RestController
   public class OrderController {
   
       @Autowired
       private UserClient userClient; // 注入Feign客户端
   
       @GetMapping("/orders/{orderId}/user")
       public String getOrderWithUser(@PathVariable String orderId) {
           // 模拟通过订单ID查询用户ID，这里简化处理，直接用orderId作为userId
           String userInfo = userClient.getUserById(orderId); 
           // 调用user-service的接口，就像调用本地方法一样简单
   
           return "Order ID: " + orderId + " | User Info: [" + userInfo + "]";
       }
   }
   ```

   

4. **配置应用端口：**
   打开 `order-service/src/main/resources/application.properties` 文件，为其指定另一个端口。

   properties

   ```
   server.port=8082
   ```

   

------

### **第三阶段：测试 Feign 调用**

1. **启动服务：**

   - 首先，确保 `user-service` 正在运行（端口 8081）。
   - 然后，启动 `order-service`（端口 8082）。

2. **测试调用：**
   打开浏览器或使用 Postman，访问 `order-service` 的接口：

   text

   ```
   http://localhost:8082/orders/456/user
   ```

   

   你应该会立刻看到返回结果：

   text

   ```
   Order ID: 456 | User Info: [User 456 (From user-service)]
   ```

   

### **恭喜！**

你已成功完成：

1. 使用 Spring Initializr 创建了两个独立的微服务项目。
2. 在 `user-service` 中暴露了一个 REST API。
3. 在 `order-service` 中通过 **OpenFeign** 声明式地调用了 `user-service` 的接口。
4. 测试验证了服务间的通信是完全成功的。

**下一步：**
现在，你可以按照之前的计划，为这两个服务配置 **SkyWalking Agent**，所有的调用链路都将被自动捕获和追踪，为你的智能分析工具提供数据来源。

### 注意：

过程中可能会出现 **Maven 依赖管理** 的问题，例如Cannot resolve symbol 'springframework'   、Cannot resolve symbol 'SpringBootApplication'   、Cannot resolve symbol 'EnableFeignClients'   、Cannot resolve symbol 'SpringApplication'。

#### 只需要**强制让 Maven 重新下载依赖（首选方案）**

这是最可能解决问题的一步。IDEA 的 Maven 导入有时会卡住或失败。

1. 打开屏幕右侧的 **Maven 工具栏**。
   - 如果没看到，请通过菜单栏 `View -> Tool Windows -> Maven` 打开。
2. 点击那个**刷新图标**（**Reload All Maven Projects**）。
3. 等待底部进度条完成，IDEA 会重新下载所有依赖并重建索引。这需要一些时间，请耐心等待。

### 第四阶段 集成skyWalking Agent

### 一、给 Windows 的 Spring Boot Demo 集成 Agent（两种方式）

注意：每个微服务都是独立的 “被观测单元”，因此 **`user-server` 和 `order-server` 都需要单独集成 SkyWalking Agent**，才能实现分布式系统的全链路追踪。

根据你启动 Spring Boot Demo 的习惯（IDEA 启动 / 命令行启动），选择对应的集成方式，核心都是通过**JVM 参数**将 Agent “挂载” 到 Demo 进程上，并指定 OAP 的地址。

#### 方式 1：IDEA 中启动 Demo（开发调试常用）

如果你习惯在 IDEA 中运行 Spring Boot Demo，直接在 IDEA 的 “运行配置” 中添加 JVM 参数即可：

1. 打开 IDEA 的 “Run/Debug Configurations”（右上角运行按钮旁的下拉框）。

2. 找到你的 Spring Boot Demo 的配置（通常是 “Spring Boot” 类型），切换到 “VM options” 选项卡。（在 IntelliJ IDEA 的 **Run/Debug Configurations** 界面中，默认可能不会直接显示 `VM options` 输入框，需要通过 **“Modify options”** 按钮来显示它）

3. 粘贴以下 JVM 参数（关键参数已标注说明）：

   ```plaintext
   -javaagent:D:\skywalking-agent\agent\skywalking-agent.jar
   -Dskywalking.agent.service_name=springboot-demo  # 你的Demo名称（在SkyWalking UI中识别用，自定义）
   -Dskywalking.collector.backend_service=192.168.1.100:11800  # 替换为你Linux的IP！11800是OAP默认端口
   ```

   

   - 注意：`-javaagent` 后面的路径必须是你 Windows 上 Agent 解压后的 `skywalking-agent.jar` 实际路径（别写错反斜杠 `\`）。
   - 替换 `192.168.1.100` 为你 Linux 的真实 IP（可以通过 Linux 命令 `ifconfig` 或 `ip addr` 查看）。

4. 点击 “Apply” 保存，然后正常点击 IDEA 的 “运行” 按钮启动 Demo。此时 Agent 会随 Demo 进程一起启动，自动采集数据。

#### 方式 2：命令行启动 Demo（模拟生产环境）

如果你的 Demo 已经打包成 JAR 包（比如 `springboot-demo.jar`），想在 Windows 命令行中启动，直接在 `java -jar` 命令前加 JVM 参数即可：

1. 打开 Windows 的 “命令提示符（CMD）” 或 “PowerShell”。

2. 切换到 Demo 的 JAR 包所在目录，执行以下命令：

   ```cmd
   java -javaagent:D:\skywalking-agent\agent\skywalking-agent.jar ^
   -Dskywalking.agent.service_name=springboot-demo ^
   -Dskywalking.collector.backend_service=192.168.1.100:11800 ^
   -jar springboot-demo.jar
   ```

   

   - 注意：Windows 命令行中换行用 `^`（CMD）或 ```（PowerShell），确保参数完整；路径同样要匹配你的 Agent 实际位置。

### 三、关键验证：确保 Agent 能访问 Linux 的 OAP（网络互通）

这是跨系统部署的核心前提，如果 Agent 连不上 OAP，数据无法上报，UI 中就看不到链路。需要做两步验证：

1. **Linux 端确保 OAP 端口开放**OAP 默认用 11800 端口（gRPC 协议，Agent 上报数据用），需要确保 Linux 的防火墙没有拦截这个端口。可以在 Linux 上执行以下命令开放端口（以 CentOS 为例）：

   ```bash
   # 开放11800端口
   sudo firewall-cmd --zone=public --add-port=11800/tcp --permanent
   # 重启防火墙生效
   sudo firewall-cmd --reload
   ```

   

   （如果是 Ubuntu，用 `ufw` 命令：`sudo ufw allow 11800/tcp`）

2. **Windows 端测试网络连通性**在 Windows 的 CMD 中执行 `ping 192.168.1.100`（替换为你的 Linux IP），确保能 ping 通；再用 `telnet 192.168.1.100 11800` 或 `nc -zv 192.168.1.100 11800`（需安装 nc 工具）测试 11800 端口是否能连接。

   - 如果 ping 不通：检查 Linux 和 Windows 是否在同一局域网（比如同一 WiFi / 网线），或 Linux 是否禁用了 ICMP（ping）。
   - 如果端口连不上：重新检查 Linux 防火墙配置，或确认 OAP 容器是否正常启动（`docker ps` 看 oap 容器状态是否为 `Up`）。

### 四、验证结果：在 Linux 的 SkyWalking UI 中看数据

1. 启动 Windows 上的 Spring Boot Demo（IDEA 或命令行）。
2. 访问 Linux 上的 SkyWalking UI（地址是 `http://LinuxIP:8080`，8080 是 UI 默认端口）。
3. 在 UI 左侧菜单中，进入 “拓扑图” 或 “追踪” 页面：
   - 拓扑图中会显示你配置的 `springboot-demo` 服务节点。
   - 访问 Demo 的接口（比如 `http://localhost:8080/hello`）后，在 “追踪” 页面能看到完整的链路调用记录。

### 总结

- ✅ 核心结论：Spring Boot Demo 在 Windows → Agent 就集成在 Windows，无需碰 Linux。
- ✅ 关键配置：JVM 参数中的 `backend_service` 必须填 Linux 的 IP（让 Agent 知道往哪发数据）。
- ✅ 网络前提：确保 Windows 能 ping 通 Linux，且 Linux 开放 11800 端口。
