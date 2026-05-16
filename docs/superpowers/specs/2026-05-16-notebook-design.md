# 个人记事本应用 - 技术设计文档

**项目名称**：Notebook  
**版本**：1.0.0  
**日期**：2026-05-16  
**架构类型**：单体架构（前后端分离）

---

## 1. 项目概述

### 1.1 项目目标
开发一个功能完整的个人云笔记应用，支持用户注册登录、笔记的创建编辑删除、分类管理、标签管理、Markdown富文本编辑、全文搜索、笔记归档等功能。

### 1.2 技术栈
- **后端**：Spring Boot 2.7.x + MyBatis-Plus + JWT
- **前端**：Vue 3 + Element Plus + Pinia + Vue Router
- **数据库**：MySQL 8.0
- **构建工具**：Maven（后端）+ Vite（前端）
- **Markdown编辑器**：markdown-it + highlight.js

### 1.3 项目结构
```
notebook-0.1/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/notebook/
│   │       │   ├── controller/    # 控制器层
│   │       │   ├── service/       # 业务逻辑层
│   │       │   ├── mapper/        # 数据访问层
│   │       │   ├── entity/        # 实体类
│   │       │   ├── dto/          # 数据传输对象
│   │       │   ├── config/       # 配置类
│   │       │   └── common/       # 通用工具类
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API 接口封装
│   │   ├── components/     # 公共组件
│   │   ├── views/         # 页面视图
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── router/        # 路由配置
│   │   ├── utils/         # 工具函数
│   │   └── assets/        # 静态资源
│   ├── public/
│   └── package.json
└── docs/                   # 文档目录
```

---

## 2. 数据库设计

### 2.1 ER图概述
```
用户 (user)
  ├── id (PK, BIGINT)
  ├── username (VARCHAR, UNIQUE)
  ├── password (VARCHAR, 加密存储)
  ├── email (VARCHAR, UNIQUE)
  ├── created_at (DATETIME)
  └── updated_at (DATETIME)
      │
      ├── 1:N
      │
笔记 (note)
  ├── id (PK, BIGINT)
  ├── user_id (FK → user.id)
  ├── title (VARCHAR)
  ├── content (TEXT, Markdown内容)
  ├── category_id (FK → category.id, 可为空)
  ├── is_archived (BOOLEAN, 默认false)
  ├── is_deleted (BOOLEAN, 软删除标记)
  ├── created_at (DATETIME)
  └── updated_at (DATETIME)
      │
      ├── N:M (通过note_tag表关联)
      │
标签 (tag)
  ├── id (PK, BIGINT)
  ├── user_id (FK → user.id)
  ├── name (VARCHAR)
  └── color (VARCHAR, 颜色代码)

笔记标签关联 (note_tag)
  ├── id (PK, BIGINT)
  ├── note_id (FK → note.id)
  └── tag_id (FK → tag.id)

分类 (category)
  ├── id (PK, BIGINT)
  ├── user_id (FK → user.id)
  ├── name (VARCHAR)
  ├── parent_id (BIGINT, 自关联，支持多级分类)
  └── sort_order (INT)
```

### 2.2 核心表结构

#### 2.2.1 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 2.2.2 笔记表 (note)
```sql
CREATE TABLE note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(255) NOT NULL COMMENT '笔记标题',
    content LONGTEXT COMMENT 'Markdown内容',
    summary VARCHAR(500) COMMENT '摘要（自动生成或手动）',
    category_id BIGINT COMMENT '分类ID',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '软删除标记',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_updated_at (updated_at),
    INDEX idx_is_archived (is_archived)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';
```

#### 2.2.3 分类表 (category)
```sql
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
```

#### 2.2.4 标签表 (tag)
```sql
CREATE TABLE tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tag (user_id, name),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

#### 2.2.5 笔记标签关联表 (note_tag)
```sql
CREATE TABLE note_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
    UNIQUE KEY uk_note_tag (note_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签关联表';
```

---

## 3. API 接口设计

### 3.1 认证模块 (Auth)

#### 3.1.1 用户注册
- **POST** `/api/auth/register`
- **Request Body**:
```json
{
  "username": "string (3-20字符)",
  "password": "string (6-20字符)",
  "email": "string (邮箱格式)"
}
```
- **Response**: 返回用户信息和JWT Token

#### 3.1.2 用户登录
- **POST** `/api/auth/login`
- **Request Body**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **Response**: 返回用户信息和JWT Token

#### 3.1.3 获取当前用户信息
- **GET** `/api/auth/userinfo`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回用户详细信息

#### 3.1.4 退出登录
- **POST** `/api/auth/logout`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

### 3.2 笔记模块 (Note)

#### 3.2.1 获取笔记列表
- **GET** `/api/notes`
- **Query Params**: 
  - `page` (默认1)
  - `size` (默认20)
  - `categoryId` (可选)
  - `tagId` (可选)
  - `isArchived` (可选, 默认false)
  - `keyword` (可选, 搜索标题和内容)
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回分页的笔记列表

#### 3.2.2 获取单个笔记详情
- **GET** `/api/notes/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回笔记详情（包含标签和分类信息）

#### 3.2.3 创建笔记
- **POST** `/api/notes`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "title": "string (必填)",
  "content": "string (Markdown内容)",
  "summary": "string (可选)",
  "categoryId": "number (可选)",
  "tagIds": "number[] (可选)"
}
```
- **Response**: 返回创建的笔记信息

#### 3.2.4 更新笔记
- **PUT** `/api/notes/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: 同创建笔记
- **Response**: 返回更新后的笔记信息

#### 3.2.5 删除笔记（软删除）
- **DELETE** `/api/notes/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

#### 3.2.6 归档笔记
- **PUT** `/api/notes/{id}/archive`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: `{ "isArchived": true/false }`
- **Response**: 返回成功消息

#### 3.2.7 移动到回收站
- **DELETE** `/api/notes/{id}/recycle`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

#### 3.2.8 恢复笔记
- **PUT** `/api/notes/{id}/restore`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

#### 3.2.9 永久删除笔记
- **DELETE** `/api/notes/{id}/permanent`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

### 3.3 分类模块 (Category)

#### 3.3.1 获取分类树
- **GET** `/api/categories`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回树形结构的分类列表

#### 3.3.2 创建分类
- **POST** `/api/categories`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "name": "string (必填)",
  "parentId": "number (可选)"
}
```
- **Response**: 返回创建的分类信息

#### 3.3.3 更新分类
- **PUT** `/api/categories/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: 同创建分类

#### 3.3.4 删除分类
- **DELETE** `/api/categories/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

### 3.4 标签模块 (Tag)

#### 3.4.1 获取所有标签
- **GET** `/api/tags`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回标签列表

#### 3.4.2 创建标签
- **POST** `/api/tags`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "name": "string (必填)",
  "color": "string (可选, 默认#1890ff)"
}
```
- **Response**: 返回创建的标签信息

#### 3.4.3 更新标签
- **PUT** `/api/tags/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: 同创建标签

#### 3.4.4 删除标签
- **DELETE** `/api/tags/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: 返回成功消息

---

## 4. 前端页面设计

### 4.1 页面结构

```
App
├── 登录页 (Login)
│   ├── 用户名/密码登录
│   └── 注册入口
├── 注册页 (Register)
│   └── 用户注册表单
└── 主布局 (MainLayout)
    ├── 顶部导航栏
    │   ├── Logo
    │   ├── 搜索框
    │   └── 用户菜单
    ├── 左侧边栏
    │   ├── 全部笔记
    │   ├── 分类列表（树形）
    │   ├── 标签云
    │   ├── 归档笔记
    │   └── 回收站
    └── 右侧内容区
        ├── 笔记列表视图 (NoteList)
        │   ├── 列表头部（排序、视图切换）
        │   └── 笔记卡片列表
        └── 笔记编辑视图 (NoteEditor)
            ├── 笔记信息区（标题、分类、标签）
            ├── Markdown编辑器
            │   ├── 编辑区
            │   └── 预览区（实时渲染）
            └── 工具栏（保存、归档、删除）
```

### 4.2 路由配置

```javascript
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/notes',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'notes',
        name: 'NoteList',
        component: NoteList
      },
      {
        path: 'notes/:id',
        name: 'NoteEditor',
        component: NoteEditor
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: CategoryManagement
      },
      {
        path: 'tags',
        name: 'TagManagement',
        component: TagManagement
      },
      {
        path: 'archive',
        name: 'Archive',
        component: Archive
      },
      {
        path: 'trash',
        name: 'Trash',
        component: Trash
      }
    ]
  }
]
```

### 4.3 状态管理 (Pinia Stores)

#### 4.3.1 authStore
```javascript
{
  state: {
    user: null,
    token: null,
    isLoggedIn: false
  },
  actions: {
    login(credentials),
    register(userInfo),
    logout(),
    fetchUserInfo()
  }
}
```

#### 4.3.2 noteStore
```javascript
{
  state: {
    notes: [],
    currentNote: null,
    pagination: {
      page: 1,
      size: 20,
      total: 0
    },
    filters: {
      categoryId: null,
      tagId: null,
      keyword: '',
      isArchived: false
    }
  },
  actions: {
    fetchNotes(),
    fetchNoteById(id),
    createNote(data),
    updateNote(id, data),
    deleteNote(id),
    archiveNote(id),
    moveToTrash(id),
    restoreNote(id),
    permanentDeleteNote(id)
  }
}
```

#### 4.3.3 categoryStore
```javascript
{
  state: {
    categories: [],
    treeData: []
  },
  actions: {
    fetchCategories(),
    createCategory(data),
    updateCategory(id, data),
    deleteCategory(id)
  }
}
```

#### 4.3.4 tagStore
```javascript
{
  state: {
    tags: []
  },
  actions: {
    fetchTags(),
    createTag(data),
    updateTag(id, data),
    deleteTag(id)
  }
}
```

---

## 5. 核心功能流程

### 5.1 用户认证流程
1. 用户输入用户名和密码
2. 前端发送登录请求到后端
3. 后端验证用户信息，生成JWT Token
4. 前端存储Token到localStorage
5. 后续请求通过Axios拦截器自动添加Authorization Header
6. 路由守卫检查Token有效性，未登录重定向到登录页

### 5.2 笔记创建流程
1. 用户点击"新建笔记"按钮
2. 跳转到编辑页面（新建模式）
3. 用户输入标题、内容，选择分类和标签
4. 实时Markdown预览
5. 点击保存，前端发送POST请求
6. 后端保存数据，返回成功响应
7. 前端更新笔记列表，显示成功提示

### 5.3 Markdown编辑流程
1. 编辑器左侧为Markdown源码编辑区
2. 编辑器右侧为实时渲染预览区
3. 使用markdown-it解析Markdown语法
4. 使用highlight.js实现代码高亮
5. 支持常用快捷键（Ctrl+S保存等）

### 5.4 笔记搜索流程
1. 用户在顶部搜索框输入关键词
2. 前端防抖处理（延迟300ms）
3. 发送搜索请求到后端
4. 后端在标题和内容中进行模糊匹配
5. 返回匹配的笔记列表
6. 前端高亮显示匹配的关键词

---

## 6. 安全设计

### 6.1 密码安全
- 使用BCryptPasswordEncoder进行密码加密
- 密码强度要求：6-20位
- 密码不在日志中输出

### 6.2 JWT安全
- Token有效期：7天
- Token中存储用户ID和用户名
- 后端验证Token有效性
- 前端Token存储在localStorage中

### 6.3 权限控制
- 所有API接口需要登录（除登录和注册外）
- 用户只能操作自己的笔记、分类、标签
- 使用Spring Security进行接口权限校验

### 6.4 SQL注入防护
- 使用MyBatis-Plus的SQL注入防护
- 参数化查询
- 不拼接SQL字符串

### 6.5 XSS防护
- 后端对输入内容进行HTML转义
- 前端使用文本插值而非v-html（除Markdown渲染外）

---

## 7. 错误处理设计

### 7.1 统一响应格式
```json
{
  "code": 200,          // 状态码
  "message": "success", // 消息
  "data": {}            // 数据
}
```

### 7.2 异常类型
- **200** - 成功
- **400** - 请求参数错误
- **401** - 未登录或Token失效
- **403** - 无权限访问
- **404** - 资源不存在
- **500** - 服务器内部错误

### 7.3 前端错误处理
- Axios拦截器统一处理响应错误
- 401错误自动跳转到登录页
- 使用Element Plus Message组件显示错误信息
- 网络错误显示友好的错误提示

---

## 8. 性能优化

### 8.1 后端优化
- 使用数据库索引优化查询
- 分页查询避免全表扫描
- 使用Redis缓存热门数据（可选）
- 懒加载笔记内容

### 8.2 前端优化
- Vue Router路由懒加载
- 组件按需加载
- 图片懒加载
- 防抖和节流处理高频操作
- 虚拟列表优化长列表渲染

### 8.3 数据库优化
- 为常用查询字段添加索引
- 定期清理回收站数据
- 使用LIMIT分页查询

---

## 9. 测试策略

### 9.1 单元测试
- Service层业务逻辑测试
- 工具类测试
- 使用JUnit 5 + Mockito

### 9.2 集成测试
- Controller层接口测试
- 数据库操作测试
- 使用Spring Boot Test

### 9.3 前端测试
- 组件单元测试（Vitest）
- 集成测试
- E2E测试（Playwright，可选）

---

## 10. 部署方案

### 10.1 开发环境
- 后端：Spring Boot DevTools热部署
- 前端：Vite热重载
- 数据库：本地MySQL

### 10.2 生产环境
- 后端：打包为JAR，使用java -jar运行
- 前端：打包为静态文件，部署到Nginx
- 数据库：云服务器MySQL

### 10.3 配置文件管理
- 开发环境：application-dev.yml
- 生产环境：application-prod.yml
- 使用Maven Profile管理不同环境

---

## 11. 项目开发计划

### Phase 1: 项目基础搭建
- 初始化Spring Boot项目
- 初始化Vue 3项目
- 配置数据库连接
- 实现用户注册和登录
- JWT认证机制

### Phase 2: 核心功能开发
- 笔记CRUD功能
- 分类管理功能
- 标签管理功能
- Markdown编辑器集成

### Phase 3: 高级功能
- 笔记搜索功能
- 归档和回收站
- 笔记排序和筛选

### Phase 4: 优化和测试
- 性能优化
- 错误处理完善
- 单元测试编写
- 代码审查

---

## 12. 风险评估与缓解

### 12.1 技术风险
- **风险**：Markdown渲染性能问题
  - **缓解**：使用轻量级markdown-it库，按需渲染

### 12.2 数据风险
- **风险**：数据丢失
  - **缓解**：定期备份数据库，使用事务保证数据一致性

### 12.3 安全风险
- **风险**：SQL注入、XSS攻击
  - **缓解**：使用ORM框架，输入验证和输出转义

---

## 13. 后续扩展建议

- 文件上传和附件管理
- 笔记分享功能
- 笔记版本历史
- 多端同步
- 数据导出（PDF、HTML）
- 协作编辑功能

---

**文档版本**：1.0  
**编写人**：AI Assistant  
**审核状态**：待审核
