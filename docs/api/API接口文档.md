# 个人记账系统 API 接口文档

> 文档版本：v1.0
> 更新日期：2026-05-05
> 适用范围：mint-pab 前后端分离管理系统

---

## 1. 文档信息

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 基础 URL | `http://localhost:8080`（开发环境） |
| 协议 | HTTP/1.1 |
| 数据格式 | JSON |
| 编码 | UTF-8 |

### 1.2 认证方式

系统采用 **Token 认证** 机制：

1. 用户调用登录接口获取 `accessToken`
2. 后续所有业务请求需在请求头中携带 `Authorization: Bearer {accessToken}`
3. Token 过期后需重新登录获取新 Token

---

## 2. 通用约定

### 2.1 请求头规范

| 请求头 | 必填 | 说明 |
|--------|------|------|
| `Content-Type` | 是 | 固定值 `application/json` |
| `Authorization` | 是（登录除外） | `Bearer {accessToken}` |

### 2.2 统一响应格式

所有接口统一返回 `Result<T>` 包装对象：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | Integer | 状态码，`200` 表示成功 |
| `message` | String | 提示信息 |
| `data` | T | 业务数据，失败时可能为 `null` |

### 2.3 分页请求格式

列表查询接口统一使用以下分页参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNum` | Integer | 否 | 当前页码，默认 `1` |
| `pageSize` | Integer | 否 | 每页条数，默认 `20`，最大 `100` |

### 2.4 分页响应格式

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [],
    "pageNum": 1,
    "pageSize": 20,
    "total": 100,
    "pages": 5
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | Array | 数据列表 |
| `pageNum` | Integer | 当前页码 |
| `pageSize` | Integer | 每页条数 |
| `total` | Long | 总记录数 |
| `pages` | Integer | 总页数 |

### 2.5 通用错误码表

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| `200` | 操作成功 | 200 |
| `400` | 请求参数错误 | 400 |
| `401` | 未授权，Token 无效或过期 | 401 |
| `403` | 禁止访问 | 403 |
| `404` | 资源不存在 | 404 |
| `500` | 服务器内部错误 | 500 |

---

## 3. 认证模块 API

### 3.1 用户登录

- **接口路径**：`POST /api/auth/login`
- **接口描述**：用户通过用户名和密码登录，验证成功后返回 Token
- **请求头**：`Content-Type: application/json`

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `username` | String | 是 | 用户名，长度3-20位，支持字母、数字、下划线 |
| `password` | String | 是 | 密码，长度6-20位 |

#### 请求示例

```json
{
  "username": "admin",
  "password": "123456"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expireTime": 3600
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 用户名或密码错误 |
| `400` | 用户名或密码不能为空 |

---

### 3.2 用户登出

- **接口路径**：`POST /api/auth/logout`
- **接口描述**：用户退出登录，服务端使当前 Token 失效
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数

无

#### 请求示例

```json
{}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 401,
  "message": "Token 已过期或无效",
  "data": null
}
```

---

### 3.3 Token 刷新

- **接口路径**：`POST /api/auth/refresh`
- **接口描述**：刷新 AccessToken，延长登录有效期
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数

无

#### 请求示例

```json
{}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expireTime": 3600
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null
}
```

---

## 4. 账户管理 API

### 4.1 账户列表

- **接口路径**：`GET /api/accounts`
- **接口描述**：查询账户列表，支持按状态筛选
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `status` | String | 否 | 状态筛选：`ACTIVE` 正常 / `DISABLED` 停用，不传返回全部 |

#### 请求示例

```
GET /api/accounts?status=ACTIVE
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": "acc_001",
      "name": "招商银行储蓄卡",
      "type": "BANK_SAVINGS",
      "typeName": "银行储蓄卡",
      "initialBalance": "5000.00",
      "currentBalance": "8750.50",
      "remark": "工资卡",
      "status": "ACTIVE",
      "createTime": "2026-01-15 10:30:00",
      "updateTime": "2026-05-05 14:20:00"
    }
  ]
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 账户ID |
| `name` | String | 账户名称 |
| `type` | String | 账户类型编码：`CASH` 现金、`BANK_SAVINGS` 银行储蓄卡、`CREDIT_CARD` 信用卡、`ALIPAY` 支付宝、`WECHAT` 微信 |
| `typeName` | String | 账户类型名称 |
| `initialBalance` | String | 初始余额，两位小数 |
| `currentBalance` | String | 当前余额，两位小数 |
| `remark` | String | 备注 |
| `status` | String | 状态：`ACTIVE` 正常 / `DISABLED` 停用 |
| `createTime` | String | 创建时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `updateTime` | String | 更新时间，格式 `yyyy-MM-dd HH:mm:ss` |

---

### 4.2 新增账户

- **接口路径**：`POST /api/accounts`
- **接口描述**：创建新账户
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `name` | String | 是 | 账户名称，长度2-30位，同一用户下不可重复 |
| `type` | String | 是 | 账户类型：`CASH` / `BANK_SAVINGS` / `CREDIT_CARD` / `ALIPAY` / `WECHAT` |
| `initialBalance` | String | 是 | 初始余额，精确到两位小数，可正可负 |
| `remark` | String | 否 | 备注，长度0-200位 |

#### 请求示例

```json
{
  "name": "微信零钱",
  "type": "WECHAT",
  "initialBalance": "200.00",
  "remark": "日常消费"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "acc_002",
    "name": "微信零钱",
    "type": "WECHAT",
    "typeName": "微信",
    "initialBalance": "200.00",
    "currentBalance": "200.00",
    "remark": "日常消费",
    "status": "ACTIVE",
    "createTime": "2026-05-05 15:00:00",
    "updateTime": "2026-05-05 15:00:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "账户名称已存在",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 账户名称已存在 |
| `400` | 账户类型不合法 |
| `400` | 初始余额格式错误 |

---

### 4.3 编辑账户

- **接口路径**：`PUT /api/accounts/{id}`
- **接口描述**：修改已有账户信息，仅允许修改名称和备注
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 账户ID |

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `name` | String | 是 | 账户名称，长度2-30位 |
| `remark` | String | 否 | 备注，长度0-200位 |

#### 请求示例

```json
{
  "name": "微信零钱-新",
  "remark": "修改后的备注"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "acc_002",
    "name": "微信零钱-新",
    "type": "WECHAT",
    "typeName": "微信",
    "initialBalance": "200.00",
    "currentBalance": "200.00",
    "remark": "修改后的备注",
    "status": "ACTIVE",
    "createTime": "2026-05-05 15:00:00",
    "updateTime": "2026-05-05 15:30:00"
  }
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 账户不存在 |
| `400` | 账户名称已存在 |

---

### 4.4 删除账户

- **接口路径**：`DELETE /api/accounts/{id}`
- **接口描述**：删除账户，有关联交易记录或系统预设账户不允许删除
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 账户ID |

#### 请求示例

```
DELETE /api/accounts/acc_002
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "该账户存在交易记录，不允许删除",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 账户不存在 |
| `400` | 该账户存在交易记录，不允许删除 |
| `400` | 系统预设账户不允许删除 |

---

### 4.5 停用/启用账户

- **接口路径**：`PUT /api/accounts/{id}/status`
- **接口描述**：修改账户状态，停用后不可用于新增交易
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 账户ID |

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `status` | String | 是 | 目标状态：`ACTIVE` 启用 / `DISABLED` 停用 |

#### 请求示例

```json
{
  "status": "DISABLED"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "acc_002",
    "name": "微信零钱-新",
    "status": "DISABLED",
    "updateTime": "2026-05-05 16:00:00"
  }
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 账户不存在 |
| `400` | 状态值不合法 |

---

## 5. 交易记账 API

### 5.1 新增交易

- **接口路径**：`POST /api/transactions`
- **接口描述**：记录一笔收入、支出或转账交易
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `type` | String | 是 | 交易类型：`INCOME` 收入 / `EXPENSE` 支出 / `TRANSFER` 转账 |
| `fromAccountId` | String | 条件必填 | 支出/转账时必填，转出账户ID |
| `toAccountId` | String | 条件必填 | 收入/转账时必填，转入账户ID |
| `amount` | String | 是 | 交易金额，精确到两位小数，必须大于0 |
| `categoryId` | String | 是 | 交易分类ID（转账时可为空） |
| `transactionTime` | String | 是 | 交易时间，格式 `yyyy-MM-dd HH:mm:ss`，默认当前时间 |
| `remark` | String | 否 | 备注，长度0-500位 |

#### 请求示例（收入）

```json
{
  "type": "INCOME",
  "toAccountId": "acc_001",
  "amount": "5000.00",
  "categoryId": "cat_income_001",
  "transactionTime": "2026-05-05 10:00:00",
  "remark": "5月工资"
}
```

#### 请求示例（支出）

```json
{
  "type": "EXPENSE",
  "fromAccountId": "acc_001",
  "amount": "35.50",
  "categoryId": "cat_food_001",
  "transactionTime": "2026-05-05 12:30:00",
  "remark": "午餐外卖"
}
```

#### 请求示例（转账）

```json
{
  "type": "TRANSFER",
  "fromAccountId": "acc_001",
  "toAccountId": "acc_003",
  "amount": "1000.00",
  "transactionTime": "2026-05-05 14:00:00",
  "remark": "转账到支付宝"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "记账成功",
  "data": {
    "id": "txn_001",
    "type": "EXPENSE",
    "typeName": "支出",
    "fromAccountId": "acc_001",
    "fromAccountName": "招商银行储蓄卡",
    "toAccountId": null,
    "toAccountName": null,
    "amount": "35.50",
    "categoryId": "cat_food_001",
    "categoryName": "餐饮 > 外卖",
    "transactionTime": "2026-05-05 12:30:00",
    "remark": "午餐外卖",
    "createTime": "2026-05-05 12:31:00",
    "updateTime": "2026-05-05 12:31:00"
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "该账户已停用，请选择其他账户",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 交易类型不合法 |
| `400` | 收入/支出金额必须大于0 |
| `400` | 转出账户和转入账户不能相同 |
| `400` | 该账户已停用，请选择其他账户 |
| `404` | 账户不存在 |
| `404` | 分类不存在 |

---

### 5.2 编辑交易

- **接口路径**：`PUT /api/transactions/{id}`
- **接口描述**：修改已记录的交易，不允许修改交易类型，修改后自动重新计算账户余额
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 交易ID |

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `fromAccountId` | String | 条件必填 | 支出/转账时必填 |
| `toAccountId` | String | 条件必填 | 收入/转账时必填 |
| `amount` | String | 是 | 交易金额，精确到两位小数，必须大于0 |
| `categoryId` | String | 是 | 交易分类ID |
| `transactionTime` | String | 是 | 交易时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `remark` | String | 否 | 备注 |

#### 请求示例

```json
{
  "fromAccountId": "acc_001",
  "amount": "45.00",
  "categoryId": "cat_food_001",
  "transactionTime": "2026-05-05 12:30:00",
  "remark": "午餐外卖-修改"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "txn_001",
    "type": "EXPENSE",
    "amount": "45.00",
    "remark": "午餐外卖-修改",
    "updateTime": "2026-05-05 16:30:00"
  }
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 交易不存在 |
| `400` | 不允许修改交易类型 |
| `400` | 该账户已停用 |

---

### 5.3 删除交易

- **接口路径**：`DELETE /api/transactions/{id}`
- **接口描述**：删除交易记录，删除后自动恢复账户原余额
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 交易ID |

#### 请求示例

```
DELETE /api/transactions/txn_001
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 交易不存在 |

---

### 5.4 交易详情

- **接口路径**：`GET /api/transactions/{id}`
- **接口描述**：查询单条交易记录详情
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 交易ID |

#### 请求示例

```
GET /api/transactions/txn_001
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": "txn_001",
    "type": "EXPENSE",
    "typeName": "支出",
    "fromAccountId": "acc_001",
    "fromAccountName": "招商银行储蓄卡",
    "toAccountId": null,
    "toAccountName": null,
    "amount": "35.50",
    "categoryId": "cat_food_001",
    "categoryName": "餐饮 > 外卖",
    "transactionTime": "2026-05-05 12:30:00",
    "remark": "午餐外卖",
    "createTime": "2026-05-05 12:31:00",
    "updateTime": "2026-05-05 12:31:00"
  }
}
```

---

## 6. 分类管理 API

### 6.1 分类列表

- **接口路径**：`GET /api/categories`
- **接口描述**：查询两级分类列表，支持按类型筛选
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `type` | String | 否 | 分类类型筛选：`INCOME` 收入 / `EXPENSE` 支出，不传返回全部 |

#### 请求示例

```
GET /api/categories?type=EXPENSE
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": "cat_food",
      "parentName": "餐饮",
      "name": "外卖",
      "type": "EXPENSE",
      "typeName": "支出",
      "isSystem": true,
      "sortOrder": 1
    },
    {
      "id": "cat_food_002",
      "parentName": "餐饮",
      "name": "堂食",
      "type": "EXPENSE",
      "typeName": "支出",
      "isSystem": true,
      "sortOrder": 2
    }
  ]
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 分类ID |
| `parentName` | String | 一级分类名称 |
| `name` | String | 二级分类名称 |
| `type` | String | 分类类型：`INCOME` / `EXPENSE` |
| `typeName` | String | 分类类型名称 |
| `isSystem` | Boolean | 是否系统预置：`true` 系统预置 / `false` 用户自定义 |
| `sortOrder` | Integer | 排序序号 |

---

### 6.2 新增分类

- **接口路径**：`POST /api/categories`
- **接口描述**：添加自定义分类
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `parentName` | String | 是 | 一级分类名称，长度2-20位 |
| `name` | String | 是 | 二级分类名称，长度2-20位 |
| `type` | String | 是 | 分类类型：`INCOME` 收入 / `EXPENSE` 支出 |
| `sortOrder` | Integer | 否 | 排序序号，默认0 |

#### 请求示例

```json
{
  "parentName": "宠物",
  "name": "猫粮",
  "type": "EXPENSE",
  "sortOrder": 10
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "cat_custom_001",
    "parentName": "宠物",
    "name": "猫粮",
    "type": "EXPENSE",
    "typeName": "支出",
    "isSystem": false,
    "sortOrder": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "该分类下已存在同名子分类",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 该分类下已存在同名子分类 |
| `400` | 分类类型不合法 |

---

### 6.3 编辑分类

- **接口路径**：`PUT /api/categories/{id}`
- **接口描述**：修改自定义分类名称，系统预置分类不允许修改
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 分类ID |

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `parentName` | String | 是 | 一级分类名称 |
| `name` | String | 是 | 二级分类名称 |
| `sortOrder` | Integer | 否 | 排序序号 |

#### 请求示例

```json
{
  "parentName": "宠物",
  "name": "宠物食品",
  "sortOrder": 10
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "cat_custom_001",
    "parentName": "宠物",
    "name": "宠物食品",
    "type": "EXPENSE",
    "isSystem": false,
    "sortOrder": 10
  }
}
```

#### 响应示例（失败）

```json
{
  "code": 403,
  "message": "系统预置分类不允许修改",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 分类不存在 |
| `403` | 系统预置分类不允许修改 |
| `400` | 该分类下已存在同名子分类 |

---

### 6.4 删除分类

- **接口路径**：`DELETE /api/categories/{id}`
- **接口描述**：删除自定义分类，有关联交易或系统预置分类不允许删除
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 分类ID |

#### 请求示例

```
DELETE /api/categories/cat_custom_001
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "该分类下存在交易记录，不允许删除",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 分类不存在 |
| `403` | 系统预置分类不允许删除 |
| `400` | 该分类下存在交易记录，不允许删除 |

---

## 7. 预算管理 API

### 7.1 查询指定月份预算

- **接口路径**：`GET /api/budgets`
- **接口描述**：查询指定月份的总预算和分类子预算列表
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `month` | String | 是 | 预算月份，格式 `yyyy-MM`，如 `2026-05` |

#### 请求示例

```
GET /api/budgets?month=2026-05
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "month": "2026-05",
    "totalBudget": {
      "id": "bud_total_001",
      "type": "TOTAL",
      "amount": "8000.00",
      "usedAmount": "5620.50",
      "remainingAmount": "2379.50",
      "usageRate": "70.26"
    },
    "categoryBudgets": [
      {
        "id": "bud_cat_001",
        "type": "CATEGORY",
        "categoryId": "cat_food",
        "categoryName": "餐饮",
        "amount": "2000.00",
        "usedAmount": "1850.00",
        "remainingAmount": "150.00",
        "usageRate": "92.50"
      },
      {
        "id": "bud_cat_002",
        "type": "CATEGORY",
        "categoryId": "cat_traffic",
        "categoryName": "交通",
        "amount": "500.00",
        "usedAmount": "320.50",
        "remainingAmount": "179.50",
        "usageRate": "64.10"
      }
    ]
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `month` | String | 预算月份 |
| `totalBudget` | Object | 总预算信息，未设置时为 `null` |
| `categoryBudgets` | Array | 分类预算列表 |
| `id` | String | 预算ID |
| `type` | String | 预算类型：`TOTAL` 总预算 / `CATEGORY` 分类预算 |
| `categoryId` | String | 关联一级分类ID（分类预算时） |
| `categoryName` | String | 关联分类名称 |
| `amount` | String | 预算金额 |
| `usedAmount` | String | 已用金额 |
| `remainingAmount` | String | 剩余金额 |
| `usageRate` | String | 使用比例（百分比，保留两位小数） |

---

### 7.2 设置预算

- **接口路径**：`POST /api/budgets`
- **接口描述**：设置月度总预算或分类子预算
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `month` | String | 是 | 预算月份，格式 `yyyy-MM` |
| `type` | String | 是 | 预算类型：`TOTAL` 总预算 / `CATEGORY` 分类预算 |
| `categoryId` | String | 条件必填 | 分类预算时必填，关联一级分类ID |
| `amount` | String | 是 | 预算金额，精确到两位小数，必须大于0 |

#### 请求示例（总预算）

```json
{
  "month": "2026-05",
  "type": "TOTAL",
  "amount": "8000.00"
}
```

#### 请求示例（分类预算）

```json
{
  "month": "2026-05",
  "type": "CATEGORY",
  "categoryId": "cat_food",
  "amount": "2000.00"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "设置成功",
  "data": {
    "id": "bud_total_001",
    "month": "2026-05",
    "type": "TOTAL",
    "amount": "8000.00",
    "usedAmount": "0.00",
    "remainingAmount": "8000.00",
    "usageRate": "0.00"
  }
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 预算金额必须大于0 |
| `400` | 该月份已存在同类型预算，将覆盖原有预算 |
| `404` | 分类不存在（分类预算时） |

---

### 7.3 修改预算

- **接口路径**：`PUT /api/budgets/{id}`
- **接口描述**：修改已有预算金额
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 预算ID |

#### 请求参数（Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `amount` | String | 是 | 预算金额，精确到两位小数，必须大于0 |

#### 请求示例

```json
{
  "amount": "9000.00"
}
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "修改成功",
  "data": {
    "id": "bud_total_001",
    "amount": "9000.00",
    "usedAmount": "5620.50",
    "remainingAmount": "3379.50",
    "usageRate": "62.45"
  }
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 预算不存在 |
| `400` | 预算金额必须大于0 |

---

### 7.4 删除预算

- **接口路径**：`DELETE /api/budgets/{id}`
- **接口描述**：删除指定预算
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Path）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | String | 是 | 预算ID |

#### 请求示例

```
DELETE /api/budgets/bud_total_001
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `404` | 预算不存在 |

---

### 7.5 预算执行情况查询

- **接口路径**：`GET /api/budgets/execution`
- **接口描述**：查询指定月份预算执行详情，包含预警状态
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `month` | String | 是 | 预算月份，格式 `yyyy-MM` |

#### 请求示例

```
GET /api/budgets/execution?month=2026-05
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "month": "2026-05",
    "totalBudget": {
      "id": "bud_total_001",
      "amount": "8000.00",
      "usedAmount": "5620.50",
      "remainingAmount": "2379.50",
      "usageRate": "70.26",
      "alertStatus": "NORMAL",
      "alertStatusName": "正常"
    },
    "categoryExecutions": [
      {
        "id": "bud_cat_001",
        "categoryId": "cat_food",
        "categoryName": "餐饮",
        "amount": "2000.00",
        "usedAmount": "1850.00",
        "remainingAmount": "150.00",
        "usageRate": "92.50",
        "alertStatus": "WARNING",
        "alertStatusName": "接近预算"
      },
      {
        "id": "bud_cat_002",
        "categoryId": "cat_traffic",
        "categoryName": "交通",
        "amount": "500.00",
        "usedAmount": "520.00",
        "remainingAmount": "-20.00",
        "usageRate": "104.00",
        "alertStatus": "OVER",
        "alertStatusName": "已超预算"
      }
    ]
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `alertStatus` | String | 预警状态：`NORMAL` 正常（<80%）/ `WARNING` 接近预算（>=80%且<100%）/ `OVER` 已超预算（>=100%） |
| `alertStatusName` | String | 预警状态名称 |

---

## 8. 流水查询 API

### 8.1 多条件组合查询

- **接口路径**：`GET /api/transactions`
- **接口描述**：按时间、类型、账户、分类、金额、关键词等多条件组合查询交易流水，支持分页
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `startDate` | String | 否 | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | String | 否 | 结束日期，格式 `yyyy-MM-dd` |
| `types` | String | 否 | 交易类型，多选用逗号分隔，如 `EXPENSE,TRANSFER` |
| `accountIds` | String | 否 | 账户ID，多选用逗号分隔 |
| `categoryIds` | String | 否 | 分类ID，多选用逗号分隔 |
| `minAmount` | String | 否 | 最小金额 |
| `maxAmount` | String | 否 | 最大金额 |
| `keyword` | String | 否 | 关键词，模糊匹配备注内容 |
| `pageNum` | Integer | 否 | 页码，默认 `1` |
| `pageSize` | Integer | 否 | 每页条数，默认 `20` |

#### 请求示例

```
GET /api/transactions?startDate=2026-05-01&endDate=2026-05-31&types=EXPENSE&accountIds=acc_001&pageNum=1&pageSize=20
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "id": "txn_001",
        "type": "EXPENSE",
        "typeName": "支出",
        "fromAccountId": "acc_001",
        "fromAccountName": "招商银行储蓄卡",
        "toAccountId": null,
        "toAccountName": null,
        "amount": "35.50",
        "categoryId": "cat_food_001",
        "categoryName": "餐饮 > 外卖",
        "transactionTime": "2026-05-05 12:30:00",
        "remark": "午餐外卖",
        "createTime": "2026-05-05 12:31:00",
        "updateTime": "2026-05-05 12:31:00"
      }
    ],
    "pageNum": 1,
    "pageSize": 20,
    "total": 50,
    "pages": 3
  }
}
```

---

### 8.2 导出流水

- **接口路径**：`GET /api/transactions/export`
- **接口描述**：将当前筛选条件下的交易记录导出为 Excel 或 PDF 文件
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `startDate` | String | 否 | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | String | 否 | 结束日期，格式 `yyyy-MM-dd` |
| `types` | String | 否 | 交易类型，多选用逗号分隔 |
| `accountIds` | String | 否 | 账户ID，多选用逗号分隔 |
| `categoryIds` | String | 否 | 分类ID，多选用逗号分隔 |
| `minAmount` | String | 否 | 最小金额 |
| `maxAmount` | String | 否 | 最大金额 |
| `keyword` | String | 否 | 关键词，模糊匹配备注内容 |
| `format` | String | 是 | 导出格式：`excel` / `pdf` |

#### 请求示例

```
GET /api/transactions/export?startDate=2026-05-01&endDate=2026-05-31&format=excel
```

#### 响应示例（成功）

文件流下载，响应头 `Content-Disposition: attachment; filename="transactions_20260505.xlsx"`

#### 响应示例（失败）

```json
{
  "code": 400,
  "message": "没有可导出的数据",
  "data": null
}
```

#### 业务错误码

| 错误码 | 说明 |
|--------|------|
| `400` | 导出格式不合法 |
| `400` | 没有可导出的数据 |

---

## 9. 财务报表 API

### 9.1 资产负债表

- **接口路径**：`GET /api/reports/balance-sheet`
- **接口描述**：展示某一时点资产分布情况，按账户类型分组汇总
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `date` | String | 否 | 截止日期，格式 `yyyy-MM-dd`，默认当天 |

#### 请求示例

```
GET /api/reports/balance-sheet?date=2026-05-05
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "date": "2026-05-05",
    "totalAssets": "45800.50",
    "groupSummary": [
      {
        "type": "BANK_SAVINGS",
        "typeName": "银行储蓄卡",
        "totalAmount": "30000.00",
        "percentage": "65.50",
        "accounts": [
          {
            "id": "acc_001",
            "name": "招商银行储蓄卡",
            "currentBalance": "8750.50"
          },
          {
            "id": "acc_004",
            "name": "建设银行储蓄卡",
            "currentBalance": "21249.50"
          }
        ]
      },
      {
        "type": "CASH",
        "typeName": "现金",
        "totalAmount": "500.00",
        "percentage": "1.09",
        "accounts": [
          {
            "id": "acc_005",
            "name": "钱包现金",
            "currentBalance": "500.00"
          }
        ]
      },
      {
        "type": "ALIPAY",
        "typeName": "支付宝",
        "totalAmount": "8000.00",
        "percentage": "17.47",
        "accounts": [
          {
            "id": "acc_003",
            "name": "支付宝余额",
            "currentBalance": "8000.00"
          }
        ]
      },
      {
        "type": "WECHAT",
        "typeName": "微信",
        "totalAmount": "200.00",
        "percentage": "0.44",
        "accounts": [
          {
            "id": "acc_002",
            "name": "微信零钱",
            "currentBalance": "200.00"
          }
        ]
      },
      {
        "type": "CREDIT_CARD",
        "typeName": "信用卡",
        "totalAmount": "-1850.00",
        "percentage": "-4.04",
        "accounts": [
          {
            "id": "acc_006",
            "name": "招商银行信用卡",
            "currentBalance": "-1850.00"
          }
        ]
      }
    ]
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `date` | String | 统计截止日期 |
| `totalAssets` | String | 总资产（各账户当前余额之和） |
| `groupSummary` | Array | 按账户类型分组汇总 |
| `type` | String | 账户类型编码 |
| `typeName` | String | 账户类型名称 |
| `totalAmount` | String | 该类型账户余额合计 |
| `percentage` | String | 该类型资产占比（%） |
| `accounts` | Array | 该类型下的账户明细 |

---

### 9.2 收支表

- **接口路径**：`GET /api/reports/income-expense`
- **接口描述**：展示指定月份的收入、支出及结余情况，按分类汇总
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `month` | String | 是 | 目标月份，格式 `yyyy-MM` |

#### 请求示例

```
GET /api/reports/income-expense?month=2026-05
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "month": "2026-05",
    "totalIncome": "8000.00",
    "totalExpense": "5620.50",
    "balance": "2379.50",
    "incomeDetails": [
      {
        "categoryId": "cat_income_001",
        "categoryName": "工资",
        "amount": "8000.00",
        "percentage": "100.00",
        "transactionCount": 1
      }
    ],
    "expenseDetails": [
      {
        "categoryId": "cat_food",
        "categoryName": "餐饮",
        "amount": "1850.00",
        "percentage": "32.91",
        "transactionCount": 15
      },
      {
        "categoryId": "cat_traffic",
        "categoryName": "交通",
        "amount": "320.50",
        "percentage": "5.70",
        "transactionCount": 8
      },
      {
        "categoryId": "cat_shopping",
        "categoryName": "购物",
        "amount": "1500.00",
        "percentage": "26.69",
        "transactionCount": 3
      },
      {
        "categoryId": "cat_other",
        "categoryName": "其他",
        "amount": "1950.00",
        "percentage": "34.70",
        "transactionCount": 5
      }
    ]
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `month` | String | 统计月份 |
| `totalIncome` | String | 总收入 |
| `totalExpense` | String | 总支出 |
| `balance` | String | 结余（总收入 - 总支出） |
| `incomeDetails` | Array | 收入分类明细 |
| `expenseDetails` | Array | 支出分类明细 |
| `transactionCount` | Integer | 该分类交易笔数 |

---

### 9.3 现金流量表

- **接口路径**：`GET /api/reports/cash-flow`
- **接口描述**：展示指定月份各账户资金流入、流出及净流量情况
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数（Query）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `month` | String | 是 | 目标月份，格式 `yyyy-MM` |

#### 请求示例

```
GET /api/reports/cash-flow?month=2026-05
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "month": "2026-05",
    "accounts": [
      {
        "accountId": "acc_001",
        "accountName": "招商银行储蓄卡",
        "accountType": "BANK_SAVINGS",
        "openingBalance": "5000.00",
        "totalInflow": "8000.00",
        "totalOutflow": "4250.00",
        "netFlow": "3750.00",
        "closingBalance": "8750.50"
      },
      {
        "accountId": "acc_003",
        "accountName": "支付宝余额",
        "accountType": "ALIPAY",
        "openingBalance": "7000.00",
        "totalInflow": "1000.00",
        "totalOutflow": "0.00",
        "netFlow": "1000.00",
        "closingBalance": "8000.00"
      }
    ],
    "incomeSourceAnalysis": [
      {
        "categoryName": "工资",
        "amount": "8000.00",
        "percentage": "100.00"
      }
    ],
    "expenseDestinationAnalysis": [
      {
        "categoryName": "餐饮",
        "amount": "1850.00",
        "percentage": "32.91"
      },
      {
        "categoryName": "购物",
        "amount": "1500.00",
        "percentage": "26.69"
      }
    ]
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `month` | String | 统计月份 |
| `accounts` | Array | 各账户现金流量明细 |
| `openingBalance` | String | 期初余额 |
| `totalInflow` | String | 本期流入 |
| `totalOutflow` | String | 本期流出 |
| `netFlow` | String | 净流量（流入 - 流出） |
| `closingBalance` | String | 期末余额 |
| `incomeSourceAnalysis` | Array | 流入来源分析（收入分类占比） |
| `expenseDestinationAnalysis` | Array | 流出去向分析（支出分类占比） |

---

## 10. 首页数据 API

### 10.1 首页财务摘要

- **接口路径**：`GET /api/dashboard/summary`
- **接口描述**：查询今日、本周、本月三个时间维度的财务摘要数据及预算执行状态
- **请求头**：`Content-Type: application/json`、`Authorization: Bearer {accessToken}`

#### 请求参数

无

#### 请求示例

```
GET /api/dashboard/summary
```

#### 响应示例（成功）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "today": {
      "income": "0.00",
      "expense": "128.50",
      "count": 3
    },
    "week": {
      "income": "0.00",
      "expense": "890.00",
      "count": 12,
      "dailyAverage": "127.14"
    },
    "month": {
      "income": "8000.00",
      "expense": "5620.50",
      "balance": "2379.50",
      "budgetExecutionRate": "70.26"
    },
    "budgetAlert": {
      "hasBudget": true,
      "month": "2026-05",
      "totalBudget": "8000.00",
      "usedAmount": "5620.50",
      "remainingAmount": "2379.50",
      "usageRate": "70.26",
      "alertStatus": "NORMAL",
      "alertStatusName": "正常"
    }
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `today` | Object | 今日摘要 |
| `today.income` | String | 今日收入 |
| `today.expense` | String | 今日支出 |
| `today.count` | Integer | 今日交易笔数 |
| `week` | Object | 本周摘要 |
| `week.income` | String | 本周收入 |
| `week.expense` | String | 本周支出 |
| `week.count` | Integer | 本周交易笔数 |
| `week.dailyAverage` | String | 日均支出 |
| `month` | Object | 本月摘要 |
| `month.income` | String | 本月收入 |
| `month.expense` | String | 本月支出 |
| `month.balance` | String | 本月结余 |
| `month.budgetExecutionRate` | String | 预算执行率（%），未设置预算时为 `null` |
| `budgetAlert` | Object | 预算执行提醒 |
| `budgetAlert.hasBudget` | Boolean | 是否已设置本月预算 |
| `budgetAlert.alertStatus` | String | 预警状态：`NORMAL` / `WARNING` / `OVER` |
| `budgetAlert.alertStatusName` | String | 预警状态名称 |

---

## 附录

### A. 枚举值对照表

#### 账户类型（AccountType）

| 编码 | 名称 |
|------|------|
| `CASH` | 现金 |
| `BANK_SAVINGS` | 银行储蓄卡 |
| `CREDIT_CARD` | 信用卡 |
| `ALIPAY` | 支付宝 |
| `WECHAT` | 微信 |

#### 交易类型（TransactionType）

| 编码 | 名称 |
|------|------|
| `INCOME` | 收入 |
| `EXPENSE` | 支出 |
| `TRANSFER` | 转账 |

#### 分类类型（CategoryType）

| 编码 | 名称 |
|------|------|
| `INCOME` | 收入 |
| `EXPENSE` | 支出 |

#### 账户状态（AccountStatus）

| 编码 | 名称 |
|------|------|
| `ACTIVE` | 正常 |
| `DISABLED` | 停用 |

#### 预算类型（BudgetType）

| 编码 | 名称 |
|------|------|
| `TOTAL` | 总预算 |
| `CATEGORY` | 分类预算 |

#### 预警状态（AlertStatus）

| 编码 | 名称 | 说明 |
|------|------|------|
| `NORMAL` | 正常 | 预算消耗 < 80% |
| `WARNING` | 接近预算 | 预算消耗 >= 80% 且 < 100% |
| `OVER` | 已超预算 | 预算消耗 >= 100% |
