---
name: "orchard-api-convention"
description: "Orchard项目API约定规范。所有接口使用POST请求，时间字段全局自动转时间戳（JacksonConfig配置，无需字段注解），POST/PUT参数统一用@RequestBody，禁止路径参数和请求参数。Invoke when creating new API endpoints, controllers, or handling datetime fields."
---

# Orchard API 约定规范

## 1. HTTP 方法与参数规范

### 1.1 总原则

| HTTP 方法 | 参数接收方式 | 是否允许 @RequestParam / @PathVariable |
|-----------|--------------|-----------------------------------------|
| POST      | `@RequestBody Dto` / `@RequestPart` | ❌ 禁止用 @RequestParam / @PathVariable 传业务参数 |
| PUT       | `@RequestBody Dto` | ❌ 禁止 |
| GET       | 无 body，正常用 @RequestParam | ✅ 允许（查询类操作） |
| DELETE    | 无 body，可用 @PathVariable | ✅ 允许（删除操作可用路径ID） |

> **核心约束**：POST/PUT 一律用 `@RequestBody` 接收 JSON DTO；ID 等标识字段放 DTO body 里（`dto.getId()`），不放 URL 路径上。

### 1.2 文件上传（multipart/form-data）

文件上传使用 `multipart/form-data`，文件和业务参数分两部分传：

```java
@PostMapping("/upload")
public Result<FileUploadVo> uploadFile(
        @RequestPart("file") MultipartFile file,           // 二进制文件
        @RequestPart("data") FileUploadDto dto,             // JSON 业务参数
        HttpServletRequest request) {
    Long userId = getUserId(request, dto.getUserId());
    Long folderId = dto.getFolderId();
    ...
}
```

前端 FormData 写法：
```js
const formData = new FormData();
formData.append('file', file);
formData.append('data', new Blob([JSON.stringify({
  userId: 123,
  folderId: 456
})], { type: 'application/json' }));
```

> **关于 multipart 中的**简单字段（String/Long/Integer 等）**，直接用 `@RequestParam("xxx")`，不要用 `@RequestPart("xxx")`，否则 Spring 会尝试把 octet-stream 的 part 找 converter → 415 `Content-Type 'application/octet-stream' is not supported`。只有 MultipartFile 和 JSON 对象 part（`@RequestPart("data") Dto` 才用 `@RequestPart`。

### 1.3 批量文件上传

```java
@PostMapping("/uploadBatch")
public Result<List<FileUploadVo>> uploadFileBatch(
        @RequestPart("files") List<MultipartFile> files,
        @RequestPart("data") FileUploadDto dto,
        HttpServletRequest request) {
}
```

## 2. 时间字段处理约定

### 2.1 数据流向

```
前端 → 时间戳(Long) → 后端自动转换为 LocalDateTime → 存入数据库(DATETIME)
数据库(DATETIME) → 后端查询得到 LocalDateTime → 自动转为时间戳(Long) → 返回前端
```

> **全局自动转换**：由 `orchard-common` 模块的 `JacksonConfig` 统一注册 `JavaTimeModule` + 自定义序列化器/反序列化器，**所有 `LocalDateTime` / `LocalDate` / `LocalTime` 字段自动生效，无需在字段上加任何注解**。

### 2.2 序列化规则

| Java 类型 | 序列化输出（响应前端） | 反序列化支持（接收前端） |
|-----------|----------------------|--------------------------|
| `LocalDateTime` | 毫秒时间戳 `long`（13位） | ① 数字时间戳 ② `"yyyy-MM-dd HH:mm:ss"` 字符串 |
| `LocalDate` | 当天零点毫秒时间戳 `long` | ① 数字时间戳 ② `"yyyy-MM-dd"` 字符串 |
| `LocalTime` | 当天从零点开始的毫秒数 `long` | ① 数字毫秒数 ② `"HH:mm:ss"` 字符串 |

### 2.3 DTO/VO 定义（无需注解）

直接写 `LocalDateTime` 类型即可，**不需要**加 `@JsonSerialize` / `@JsonDeserialize`：

```java
@Data
public class UserDto {
    private String username;
    
    // 直接写 LocalDateTime，JacksonConfig 全局自动处理时间戳转换
    private LocalDateTime createTime;
    
    // 查询条件：时间范围筛选
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

```java
@Data
public class UserVo {
    private Long id;
    private String username;
    
    // 直接写 LocalDateTime，返回前端自动变成毫秒时间戳
    private LocalDateTime createTime;
}
```

### 2.4 全局配置位置

- **配置类**：`orchard-common` → `com.example.orchardcommon.config.JacksonConfig`
- **关键 Bean**：`Jackson2ObjectMapperBuilder`，使用 `modulesToInstall()` **追加**注册（不会覆盖 SpringBoot 默认模块）
- **包含模块**：
  - `JavaTimeModule`（自定义了 LocalDateTime/LocalDate/LocalTime 的序列化/反序列化）
  - `SimpleModule`（`Long` → `String`，解决雪花 ID JS 精度丢失）

### 2.5 数据库查询示例

```java
// DTO 中已是 LocalDateTime（Jackson 已自动把时间戳转好）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(User::getCreateTime, dto.getStartTime())
       .le(User::getCreateTime, dto.getEndTime());
List<User> users = userMapper.selectList(wrapper);
```

## 3. ID 字段处理约定

- **雪花 ID**：Service 层 `add()` 时用 `SnowflakeUtils.nextId(BizCodeEnum.XXX)` 生成
- **Long 类型的 ID 序列化**：JacksonConfig 全局把 `Long`/`long` 转成 `String` 输出，避免 JS 精度丢失，**无需字段注解**
- **更新接口**：ID 放在 DTO body 的 `id` 字段，Controller 从 `dto.getId()` 获取

## 4. DTO 命名约定

| 场景 | 命名 | 示例 |
|------|------|------|
| 新增/更新同一类实体 | `XxxDto`（含 id 字段，更新时用） | `UserDto`、`PlanDto` |
| 创建专用（字段与更新差异大） | `XxxCreateDto` | `AiWorkCreateDto` |
| 更新专用（字段与新增差异大） | `XxxUpdateDto` | `AiWorkUpdateDto` |
| 单 ID 操作 | `IdDto`（common 通用）或模块内 `XxxIdDto` | `AiWorkIdDto` |
| 取消类操作 | `CancelXxxDto` | `CancelSubscribeDto` |
| 文件操作参数 | `FileXxxDto` / `FolderXxxDto` | `FileUploadDto`、`FolderCreateDto` |
| 查询条件/分页 | `XxxQuery` / `XxxPageDto` | `AiWorkQuery`、`KnowledgeDocQueryDto` |

---

## 5. 列表查询 & 分页（项目强制规则）

### 5.1 总原则
> **凡是"多条记录的查询，一律走分页**。禁止返回无界的 `List<XxxVo>`，防止数据量上来后OOM+慢查询。

### 5.2 DTO 约束（强制）
- **所有**分页查询 DTO 必须 `extends BaseQuery`（`orchard-common → com.example.orchardcommon.entity.BaseQuery`）**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XXX查询参数")
public class AiWorkQuery extends BaseQuery {
    // 你的业务字段
    private String type;
}
```

`BaseQuery` 已封装：
| 字段 | 说明 | 默认/边界 |
|------|------|----------|
| `query` | 通用关键字搜索（按业务决定搜哪个字段 | - |
| `pageNum` | 页码 | 默认 1，最小 1 |
| `pageSize` | 每页条数 | 默认 20，最大 300 |

> **取分页参数时用 `query.getPageNum()` / `query.getPageSize()`（不是字段直接取值，getter 已做边界修正+默认值）**，不要手动判空。

### 5.3 返回结构（强制）
分页查询 Service 返回 `PageResult<T>`（`orchard-common → com.example.orchardcommon.result.PageResult`），Controller 返回 `Result<PageResult<XxxVo>>`：

```java
// Service
PageResult<AiWorkVo> page(XxxQuery query, Long userId);

// Controller
@PostMapping("/page")
public Result<PageResult<AiWorkVo>> page(@RequestBody XxxQuery query) {
    Long userId = getCurrentUserId();
    return Result.ok(xxxService.page(query, userId));
}
```

`PageResult` 结构：
```json
{
  "list": [ ...当前页数据...],
  "size": 20,
  "current": 1
}
```

### 5.4 Service 实现模板（MyBatis-Plus，强制对齐）

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.orchardcommon.result.PageResult;

@Override
public PageResult<XxxVo> page(XxxQuery query, Long userId) {
    LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Xxx::getUserId, userId)
           // 1. 业务条件过滤
           .eq(StringUtils.hasText(query.getType()), Xxx::getType, query.getType())
           // 2. 通用关键字模糊搜索（按业务选字段，通常是名称/标题）
           .like(StringUtils.hasText(query.getQuery()), Xxx::getFileName, query.getQuery())
           // 3. 统一按创建时间倒序
           .orderByDesc(Xxx::getCreateTime);

    // 4. BaseQuery getter 已做边界修正，直接传
    Page<Xxx> page = new Page<>(query.getPageNum(), query.getPageSize());
    Page<Xxx> result = xxxMapper.selectPage(page, wrapper);
    return PageResult.of(result, this::toVo);
}
```

> **命名约定：分页接口 Path 统一用 `/xxx/page`（不是 `/list`），语义清晰对齐返回的是分页结构。

### 5.5 哪些情况可以返回不分页的 `List<T>`（白名单：
1. 目录树（`/folder/tree`）、字典枚举等条数严格≤几百条且不会增长的数据结构
2. 下拉选项目录（但如果可能增长，仍建议分页或加 `limit`）

---

## 6. 统一响应格式

所有接口返回统一的 `Result<T>` 格式：

```java
{
    "code": 200,
    "msg": "操作成功",
    "data": {},
    "success": true
}
```

## 7. Service 层规范

- Service 实现类 `add()` 方法必须自动设置：`id`（雪花 ID）、`createTime`（`LocalDateTime.now()`）
- Service 实现类 `update()` 方法必须自动设置：`updateTime`（`LocalDateTime.now()`）
- Service 类对应 `BizCodeEnum`：`CompanyServiceImpl`(COMPANY)、`DepartmentServiceImpl`(DEPARTMENT)、`UserServiceImpl`(USER)、`PlanServiceImpl`(PLAN)、`UserSubscriptionServiceImpl`(USER_SUBSCRIPTION)

## 8. 注意事项

1. **Controller 层**：所有 POST/PUT 用 `@RequestBody`，禁止 `@RequestParam` / `@PathVariable` 传业务参数
   - 例外：multipart 里的简单字段（Long/String/Integer）用 `@RequestParam`，避免 `@RequestPart` 触发 octet-stream 415
2. **DTO/VO 层**：时间字段直接写 `LocalDateTime`，**不要**加 `@JsonSerialize` / `@JsonDeserialize`（全局自动处理）
3. **Entity 层**：时间字段使用 `LocalDateTime` 类型
4. **数据库**：时间字段使用 `DATETIME` 类型，存储标准格式
5. **文件上传**：`@RequestPart("file"/"files")` 传二进制；简单字段用 `@RequestParam`；复杂 JSON 参数用 `@RequestPart("data") DTO`
6. **GET 查询**：可正常使用 `@RequestParam` 传查询参数，不受 POST/PUT 约束
7. **列表查询**：一律走分页（见第 5 章），查询 DTO extends BaseQuery，接口 path 用 `/page`，返回 `Result<PageResult<XxxVo>>`
