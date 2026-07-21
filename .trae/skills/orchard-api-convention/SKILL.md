---
name: "orchard-api-convention"
description: "Orchard项目API约定规范。所有接口使用POST请求，时间字段前端传时间戳、后端转标准格式存库、返回时转回时间戳。Invoke when creating new API endpoints, controllers, or handling datetime fields."
---

# Orchard API 约定规范

## 2. 时间字段处理约定

### 2.1 数据流向

```
前端 → 时间戳(Long) → 后端接收 → 转换为LocalDateTime → 存入数据库(标准格式)
数据库(标准格式) → 后端查询 → 转换为时间戳(Long) → 返回前端
```

### 2.2 DTO 定义（接收前端时间戳）

```java
@Data
public class UserDto {
    private String username;
    
    // 前端传入时间戳，后端转换为LocalDateTime
    @JsonDeserialize(using = TimestampToLocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    
    // 查询条件：时间范围筛选（传入时间戳）
    @JsonDeserialize(using = TimestampToLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    
    @JsonDeserialize(using = TimestampToLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
}
```

### 2.3 VO 定义（返回前端时间戳）

```java
@Data
public class UserVo {
    private Long id;
    private String username;
    
    // 返回给前端时转为时间戳
    @JsonSerialize(using = LocalDateTimeToTimestampSerializer.class)
    private LocalDateTime createTime;
}
```

### 2.4 时间转换工具类

```java
// 时间戳 → LocalDateTime
public class TimestampToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long timestamp = p.getLongValue();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}

// LocalDateTime → 时间戳
public class LocalDateTimeToTimestampSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        long timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        gen.writeNumber(timestamp);
    }
}
```

### 2.5 数据库查询示例

```java
// 根据时间范围查询（DTO中已是LocalDateTime）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(User::getCreateTime, dto.getStartTime())
       .le(User::getCreateTime, dto.getEndTime());
List<User> users = userMapper.selectList(wrapper);
```

## 3. 统一响应格式

所有接口返回统一的 `Result<T>` 格式：

```java
{
    "code": 200,
    "msg": "操作成功",
    "data": {},
    "success": true
}
```

## 4. 注意事项

1. **Controller 层**：所有方法使用 `@PostMapping`
2. **DTO 层**：时间字段使用 `@JsonDeserialize` 注解接收时间戳
3. **VO 层**：时间字段使用 `@JsonSerialize` 注解返回时间戳
4. **Entity 层**：时间字段使用 `LocalDateTime` 类型
5. **数据库**：时间字段使用 `DATETIME` 类型，存储标准格式
