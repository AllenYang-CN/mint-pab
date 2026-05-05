# AGENTS.md - Monit 项目技术栈规范

## 项目概述

mint-pab 是一个基于 Java 和 Vue 的个人记账系统，采用前后端分离架构。本文档详细规定了项目的技术栈选择、版本要求、使用规范及最佳实践。

---

## 一、后端技术栈

### 1.1 构建工具：Maven

**版本要求：**
- Maven 3.6+ 
- JDK 8

**使用规范：**
- 使用 `pom.xml` 进行依赖管理
- 统一在 `<properties>` 中定义版本号
- 使用 `<dependencyManagement>` 管理依赖版本
- 模块化项目使用 parent POM 继承

**配置示例：**
```xml
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

**最佳实践：**
- 使用阿里云 Maven 镜像加速依赖下载
- 定期执行 `mvn dependency:tree` 检查依赖冲突
- 使用 `mvn clean install -DskipTests` 进行快速构建

---

### 1.2 编程语言：Java 8

**版本要求：**
- Java 1.8.0_201+ (推荐使用 LTS 版本)

**使用规范：**
- 使用 Lambda 表达式和 Stream API 简化集合操作
- 合理使用 Optional 避免空指针异常
- 使用 CompletableFuture 进行异步编程
- 遵循阿里巴巴 Java 开发手册规范

**禁止使用：**
- Java 9+ 的特性（如模块化、var 关键字等）
- 已废弃的 API（如 Date，推荐使用 LocalDate）

**最佳实践：**
```java
// 推荐：使用 Stream API
List<String> names = users.stream()
    .map(User::getName)
    .filter(StringUtils::isNotBlank)
    .collect(Collectors.toList());

// 推荐：使用 Optional
Optional<User> user = userRepository.findById(id);
user.ifPresent(u -> processUser(u));
```

---

### 1.3 框架：Spring Boot

**版本要求：**
- Spring Boot 2.7.x（最新稳定版本）
- Spring Framework 5.3.x

**使用规范：**
- 使用 `@SpringBootApplication` 作为主启动类
- 配置文件使用 `application.properties`（而非 yml）
- 按环境拆分配置文件：`application-dev.properties`、`application-prod.properties`
- 使用 `@ConfigurationProperties` 进行类型安全的配置绑定

**项目结构规范：**
```
src/main/java/
├── controller/      # 控制器层
├── service/         # 业务逻辑层
├── repository/      # 数据访问层
├── entity/          # 实体类
├── dto/             # 数据传输对象
├── config/          # 配置类
├── exception/       # 异常处理
└── util/            # 工具类
```

**最佳实践：**
- 使用全局异常处理 `@RestControllerAdvice`
- 接口返回统一封装 `Result<T>` 对象
- 合理使用 `@Transactional` 管理事务
- 启用 Actuator 进行健康检查

---

### 1.4 数据库：MySQL

**版本要求：**
- MySQL 8.0+

**依赖配置：**
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

**使用规范：**
- 使用 HikariCP 作为默认连接池
- 表名和字段名使用下划线命名法
- 必须包含 `id`、`create_time`、`update_time` 字段
- 使用逻辑删除（`is_deleted`）而非物理删除

**配置示例：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/monit?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

**最佳实践：**
- 所有查询必须使用索引
- 避免 N+1 查询问题
- 使用 MyBatis-Plus 或 JPA 简化数据访问
- 定期执行慢查询优化

---

### 1.6 缓存：Redis

**版本要求：**
- Redis 6.2+
- Spring Data Redis（与 Spring Boot 版本匹配）

**依赖配置：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**使用规范：**
- 使用 `RedisTemplate<String, Object>` 进行操作
- 配置 Jackson 序列化方式
- 缓存键统一前缀：`monit:{module}:{key}`
- 设置合理的过期时间，避免内存泄漏

**配置示例：**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

**最佳实践：**
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 使用 Jackson 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
```

---

### 1.7 简化代码：Lombok

**版本要求：**
- Lombok 1.18.30+

**依赖配置：**
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

**使用规范：**
- Entity 类使用 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`
- DTO 类使用 `@Data`
- 日志使用 `@Slf4j`
- 避免在 JPA Entity 中使用 `@Data`（使用 `@Getter`、`@Setter` 替代）

**最佳实践：**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
}

@Slf4j
@Service
public class UserService {
    public void process() {
        log.info("Processing user...");
    }
}
```

---

### 1.8 工具库：Hutool

**版本要求：**
- Hutool 5.8.x

**依赖配置：**
```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.25</version>
</dependency>
```

**使用规范：**
- 优先使用 Hutool 工具类替代手写工具方法
- 常用模块：`DateUtil`、`StrUtil`、`HttpUtil`、`FileUtil`
- 避免重复造轮子

**最佳实践：**
```java
// 日期处理
String dateStr = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

// 字符串处理
boolean blank = StrUtil.isBlank(str);
```

---

### 1.9 工具库：Guava

**版本要求：**
- Guava 32.1.x

**依赖配置：**
```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>
```

**使用规范：**
- 使用 `Cache` 实现本地缓存
- 使用 `Lists`、`Maps`、`Sets` 简化集合创建
- 使用 `Preconditions` 进行参数校验
- 使用 `RateLimiter` 进行限流

**最佳实践：**
```java
// 本地缓存
Cache<String, User> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build();

// 集合工具
List<String> list = Lists.newArrayList("a", "b", "c");

// 参数校验
Preconditions.checkNotNull(param, "参数不能为空");

// 限流
RateLimiter limiter = RateLimiter.create(10.0); // 每秒10个请求
limiter.acquire();
```

---

### 1.10 JSON 处理：Fastjson

**版本要求：**
- Fastjson 2.0.x（推荐使用 fastjson2）

**依赖配置：**
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.43</version>
</dependency>
```

**使用规范：**
- 统一使用 fastjson2 进行 JSON 序列化与反序列化
- 使用 `JSON.toJSONString()` 进行对象转 JSON 字符串
- 使用 `JSON.parseObject()` 进行 JSON 字符串转对象
- 使用 `JSON.parseArray()` 进行 JSON 字符串转集合
- 注意处理 null 值和日期格式化

**最佳实践：**
```java
// 对象转 JSON
User user = new User(1L, "test", "test@example.com");
String json = JSON.toJSONString(user);

// JSON 转对象
String jsonStr = "{\"id\":1,\"username\":\"test\",\"email\":\"test@example.com\"}";
User user = JSON.parseObject(jsonStr, User.class);

// JSON 转集合
String jsonArray = "[{\"id\":1,\"username\":\"test1\"},{\"id\":2,\"username\":\"test2\"}]";
List<User> users = JSON.parseArray(jsonArray, User.class);

// 格式化输出（带缩进）
String prettyJson = JSON.toJSONString(user, true);

// 日期格式化
JSON.toJSONStringWithDateFormat(user, "yyyy-MM-dd HH:mm:ss");

// 忽略 null 字段
JSON.toJSONString(user, SerializerFeature.WriteMapNullValue);
```

**注意事项：**
- 生产环境建议启用安全模式，防止 JSON 反序列化漏洞
- 大字段 JSON 解析注意性能优化
- 与 Spring MVC 集成时，可配置 HttpMessageConverter 使用 fastjson

---

## 二、前端技术栈

### 2.1 框架：Vue

**版本要求：**
- Vue 2.6.x 或 Vue 2.7.x
- Vue CLI 4.x+ 或 Vite 4.x+

**使用规范：**
- 使用 Vue Router 进行路由管理
- 使用 Vuex 进行状态管理
- 组件采用单文件组件（.vue）
- 遵循 Vue 官方风格指南

**项目结构规范：**
```
src/
├── api/           # API 接口
├── assets/        # 静态资源
├── components/    # 公共组件
├── views/         # 页面组件
├── router/        # 路由配置
├── store/         # Vuex 状态管理
├── utils/         # 工具函数
└── styles/        # 全局样式
```

**最佳实践：**
```vue
<template>
  <div class="user-list">
    <el-table :data="users" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'UserList',
  data() {
    return {
      users: [],
      loading: false
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    async fetchUsers() {
      this.loading = true
      try {
        const { data } = await this.$api.getUsers()
        this.users = data
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
```

---

### 2.2 UI 组件库：Element UI

**版本要求：**
- Element UI 2.15.x（Vue 2）
- 如使用 Vue 3，请使用 Element Plus 2.x

**依赖配置：**
```bash
npm install element-ui@2.15.14 -S
# 或
yarn add element-ui@2.15.14
```

**使用规范：**
- 全局引入或按需引入（推荐按需引入减少体积）
- 统一使用 Element UI 的主题色
- 表单验证使用 Element UI 的表单验证规则
- 消息提示统一使用 `this.$message`

**配置示例：**
```javascript
import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.use(ElementUI, {
  size: 'small' // 全局组件尺寸
})
```

**最佳实践：**
```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  data() {
    return {
      form: { username: '' },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            await this.$api.createUser(this.form)
            this.$message.success('创建成功')
          } catch (error) {
            this.$message.error('创建失败')
          }
        }
      })
    }
  }
}
</script>
```

---

## 三、技术集成关系

### 3.1 后端架构集成

```
┌─────────────────────────────────────────┐
│           Spring Boot Application        │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │    Controller 层                  │   │
│  │  (接收请求、参数校验、返回响应)    │   │
│  └────────────┬─────────────────────┘   │
│               │                          │
│  ┌────────────▼─────────────────────┐   │
│  │    Service 层                     │   │
│  │  (业务逻辑、事务管理)              │   │
│  │  使用: Lombok + Hutool + Guava   │   │
│  └────────────┬─────────────────────┘   │
│               │                          │
│  ┌────────────▼─────────────────────┐   │
│  │    Repository 层                  │   │
│  │  (数据访问、ORM 映射)              │   │
│  └────────────┬─────────────────────┘   │
│               │                          │
│     ┌─────────▼─────────┐               │
│     │   MySQL Database  │               │
│     └───────────────────┘               │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │    Redis Cache                    │   │
│  │  (缓存、会话管理)                  │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### 3.2 前后端交互

```
┌──────────────┐         HTTP/REST API         ┌──────────────┐
│   Vue +      │  ◄─────────────────────────►  │  Spring Boot │
│  Element UI  │      (JSON 数据交换)           │   Backend    │
└──────────────┘                                └──────────────┘
```


---

## 四、开发规范与约束

### 4.1 代码规范
- 后端遵循阿里巴巴 Java 开发手册
- 前端遵循 Vue 官方风格指南
- 使用统一的代码格式化工具（如 Prettier、Spotless）

### 4.2 版本管理
- 所有依赖版本在 `pom.xml` 的 `<properties>` 中统一定义
- 禁止在代码中硬编码版本号
- 定期更新依赖版本，修复安全漏洞

### 4.3 安全规范
- 敏感信息（密码、密钥）使用环境变量或配置中心管理
- SQL 查询使用参数化查询，防止 SQL 注入
- 启用 CORS 配置，限制跨域访问
- 使用 Spring Security 或 JWT 进行认证授权

### 4.4 性能规范
- Redis 缓存设置合理的过期时间
- 数据库查询必须使用索引
- 接口响应时间控制在 500ms 以内
- 使用连接池管理数据库和 Redis 连接

### 4.5 日志规范
- 使用 SLF4J + Logback 进行日志记录
- 日志级别：ERROR（错误）、WARN（警告）、INFO（重要信息）、DEBUG（调试）
- 生产环境禁止输出 DEBUG 日志
- 关键业务操作必须记录日志

---

## 五、环境配置

### 5.1 开发环境
```yaml
# application-dev.yml
spring:
  profiles: dev
  datasource:
    url: jdbc:mysql://localhost:3306/monit_dev
  redis:
    host: localhost
    port: 6379
```

### 5.2 生产环境
```yaml
# application-prod.yml
spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://prod-server:3306/monit_prod
  redis:
    host: prod-redis-server
    port: 6379
    password: ${REDIS_PASSWORD}
```

---

## 六、快速开始

### 6.1 后端启动
```bash
# 克隆项目
git clone <repository-url>
cd monit

# 编译项目
mvn clean install -DskipTests

# 启动应用
mvn spring-boot:run
# 或
java -jar target/monit.jar
```

### 6.2 前端启动
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
# 或
yarn install

# 启动开发服务器
npm run serve
# 或
yarn serve
```

---

## 七、常见问题与解决方案

### 7.1 依赖冲突
- 使用 `mvn dependency:tree` 查看依赖树
- 使用 `<exclusions>` 排除冲突依赖
- 使用 `<dependencyManagement>` 统一管理版本

### 7.2 Redis 连接失败
- 检查 Redis 服务是否启动
- 检查防火墙配置
- 验证密码和端口配置

### 7.3 MySQL 连接超时
- 增加 HikariCP 的 `connection-timeout`
- 检查数据库连接数限制
- 优化慢查询

---

## 八、附录

### 8.1 技术栈版本总览

| 技术 | 版本 | 用途 |
|------|------|------|
| Maven | 3.6+ | 构建工具 |
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.x | 应用框架 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 6.2+ | 缓存数据库 |
| Lombok | 1.18.30+ | 代码简化 |
| Hutool | 5.8.x | Java 工具库 |
| Guava | 32.1.x | Google 工具库 |
| Fastjson | 2.0.x | JSON 处理 |
| Vue | 2.6.x/2.7.x | 前端框架 |
| Element UI | 2.15.x | UI 组件库 |

### 8.2 参考文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 官方文档](https://v2.cn.vuejs.org/)
- [Element UI 官方文档](https://element.eleme.cn/)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)

---

**文档版本：** v1.0  
**最后更新：** 2026-05-02  
**维护者：** 开发团队
