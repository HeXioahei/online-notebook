# 在线笔记本系统 (Online Notebook)

## 项目简介

本项目是一个基于 **Spring Boot 3.2 + Vue.js 3.3** 的**多级目录在线笔记本系统**，支持用户注册登录、创建无限层级的目录树结构、编写 Markdown 格式笔记并实时预览渲染等功能。

### ✨ 核心功能特性

✅ **用户管理系统**
- 用户注册与登录（表单验证、路由守卫）
- 用户信息展示

✅ **多级目录管理** ⭐核心亮点
- 支持无限层级目录树（递归组件实现）
- 目录的增删改查操作
- 目录展开/折叠交互
- 同级目录可重名
- 等距缩进，视觉层次清晰

✅ **笔记管理系统**
- Markdown 格式编辑器
- 实时渲染预览（支持GFM标准）
- 笔记的新建、编辑、删除
- 图片自适应显示
- 新标签页打开详情

✅ **高级搜索功能**
- 全局关键词搜索
- 搜索结果弹窗展示
- 显示完整路径信息
- 按修改时间排序

✅ **用户体验优化**
- 响应式设计，适配多种设备
- 居中确认弹窗替代原生 alert
- 卡片式布局展示
- 平滑动画过渡效果

---

## 技术选型

### 后端技术栈

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|---------|
| Java | 17 | 编程语言 | LTS版本，性能优秀 |
| Spring Boot | 3.2.0 | 应用框架 | 快速开发，生态完善 |
| Spring Data JPA | 3.x | ORM框架 | 简化数据库操作 |
| MySQL | 8.0 | 关系型数据库 | 成熟稳定 |
| Maven | 3.x | 项目构建 | 依赖管理自动化 |

### 前端技术栈

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|---------|
| Vue.js | 3.3 | 前端框架 | Composition API，响应式强 |
| Vue Router | 4.x | 路由管理 | 单页应用路由控制 |
| Axios | 1.x | HTTP客户端 | Promise-based，易用 |
| Vite | 5.0 | 构建工具 | 快速热更新 |
| Marked | 12.x | Markdown解析 | 轻量级，效果好 |

### 关键技术选择说明

1. **Vue 3 Composition API**：逻辑复用更灵活，代码组织更清晰
2. **递归组件 DirectoryTree.vue**：优雅解决无限层级目录树的渲染问题
3. **Spring Data JPA**：自动生成 SQL，减少样板代码
4. **Marked 库**：支持 GFM 标准，渲染效果好

---

## 访问与运行方式

### 方式一：本地运行（推荐）

#### 前置环境要求

```bash
必需软件版本：
- JDK 17 或更高版本
- Node.js 16+ 或更高版本
- MySQL 8.0+
- Maven 3.6+
```

#### 步骤 1：配置数据库

```bash
# 使用备份文件一键创建数据库
mysql -u root -p < database/backup_with_create_db.sql
```

#### 步骤 2：配置后端连接

编辑文件 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notebook_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&useUnicode=true
    username: root          # 修改为你的数据库用户名
    password:              # 修改为你的数据库密码（如无密码则留空）
```

#### 步骤 3：启动后端服务

```bash
cd backend

# 编译项目（首次运行）
mvn clean install -DskipTests

# 启动 Spring Boot 应用
mvn spring-boot:run
```

后端成功启动后，访问：http://localhost:8081

> ⚠️ **注意**：如果端口被占用，请检查 `application.yml` 中的端口配置。

#### 步骤 4：启动前端服务

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端成功启动后，访问：http://localhost:5173

#### 步骤 5：使用系统

1. 打开浏览器访问 http://localhost:5173
2. 点击"注册"按钮创建新账号，或使用测试账号登录
3. 开始使用笔记本功能！

### 测试账号信息

| 用户名 | 密码 | 说明 | 包含数据 |
|--------|------|------|---------|
| testuser | 123456 | 测试账号 | 4门课程，32条笔记 |
| hjj | 123456 | 主账号 | 4门课程，32条笔记 |

**推荐使用 hjj 账号进行演示**

---

## 数据库设计说明

### 数据库概览

- **数据库名称**：`notebook_db`
- **字符集**：utf8mb4（支持完整 Unicode，包括 emoji）
- **排序规则**：utf8mb4_unicode_ci

### 核心数据表

#### 1. users 表（用户表）
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. categories 表（目录表）⭐关键设计
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,           -- 支持多级目录（自引用外键）
    sort_order INT DEFAULT 0,   -- 排序顺序
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE
);
```

**关键设计点**：
- `parent_id` 字段实现无限层级目录结构
- `ON DELETE CASCADE` 确保删除父目录时自动清理子目录
- `sort_order` 支持自定义排序

#### 3. notes 表（笔记表）
```sql
CREATE TABLE notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,            -- 支持 Markdown 长文本
    user_id BIGINT NOT NULL,
    category_id BIGINT,          -- 所属目录
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
```

---

## 项目文件结构

```
online_notebook/ 
│ ├── backend/ # Spring Boot 后端项目 
│ ├── src/main/java/com/example/notebook/ 
│ │ ├── config/ 
│ │ │ └── WebConfig.java # CORS跨域配置 
│ │ ├── controller/ 
│ │ │ ├── UserController.java # 用户接口 
│ │ │ ├── CategoryController.java # 目录接口 
│ │ │ └── NoteController.java # 笔记接口 
│ │ ├── dto/ # 数据传输对象 
│ │ ├── entity/ # JPA实体类 
│ │ ├── exception/ # 全局异常处理 
│ │ ├── repository/ # 数据访问层 
│ │ ├── service/ # 业务逻辑层（含递归删除算法） 
│ │ └── NotebookApplication.java # 启动类 
│ ├── src/main/resources/ 
│ │ └── application.yml # 应用配置文件 
│ └── pom.xml # Maven依赖配置 
│ ├── frontend/ # Vue.js 前端项目 
│ ├── src/ 
│ │ ├── components/ 
│ │ │ └── DirectoryTree.vue # ⭐ 递归目录树组件 
│ │ ├── router/index.js # 路由配置 
│ │ ├── utils/api.js # Axios封装 
│ │ ├── views/ 
│ │ │ ├── Login.vue # 登录页 
│ │ │ ├── Register.vue # 注册页 
│ │ │ ├── NoteList.vue # ⭐ 笔记列表主页 
│ │ │ └── NoteDetail.vue # 笔记详情页 
│ │ ├── App.vue, main.js, style.css 
│ ├── index.html, package.json, vite.config.js 
│ ├── database/ # 数据库脚本 
│ ├── init.sql # 初始化表结构 
│ ├── update_multilevel_categories.sql # 多级目录字段更新 
│ ├── insert_data_for_hjj.sql # 示例数据（hjj用户）⭐推荐导入 
│ ├── insert_sample_data.sql # 示例数据（testuser） 
│ └── clear_all_data.sql # 清空数据脚本 
│ ├── README.md # 本文档 
└── 项目说明文档.md # 项目说明文档（考核提交用）
```


---

## 核心功能实现详解

### 1️⃣ 多级目录树（DirectoryTree.vue）⭐核心技术亮点

**技术挑战**：如何渲染无限层级的目录树？

**解决方案**：使用 Vue 3 的**递归组件**机制

**关键技术点**：
- 组件自身引用自身名称实现递归
- 控制缩进距离（每级8px）
- 使用 `v-if` 条件渲染避免无限循环
- 所有事件通过 `$emit()` 冒泡给父组件处理

**代码示例**：
```vue
<template>
  <div v-for="item in items" :key="item.id">
    <!-- 渲染当前项 -->
    <div :style="{ paddingLeft: 8 + 'px' }">
      {{ item.name }}
    </div>
    
    <!-- 递归渲染子项 -->
    <DirectoryTree
      v-if="expandedDirectories.includes(item.id) && hasChildren(item)"
      :items="getChildrenItems(item)"
      :level="level + 1"
      @toggle="$emit('toggle', $event)"
    />
  </div>
</template>
```

---

### 2️⃣ 递归删除算法（CategoryService.java）⭐核心技术亮点

**技术挑战**：删除目录时需要同时删除所有子目录和相关笔记

**解决方案**：深度优先搜索（DFS）递归算法 + 事务保护

**算法特点**：
- 采用后序遍历（先子节点后父节点），避免外键约束冲突
- 整个过程在 `@Transactional` 事务中执行，保证原子性
- 时间复杂度：O(n)，n 为总节点数

**代码示例**：
```java
@Transactional
private void recursiveDeleteCategory(Long categoryId) {
    // 1. 先查找并递归删除所有子目录
    List<Category> childCategories = categoryRepository.findByParentId(categoryId);
    for (Category child : childCategories) {
        recursiveDeleteCategory(child.getId()); // 递归调用
    }
    
    // 2. 删除当前目录下的所有笔记
    noteRepository.deleteByCategoryId(categoryId);
    
    // 3. 最后删除当前目录本身
    categoryRepository.deleteById(categoryId);
}
```

---

### 3️⃣ Markdown 实时渲染

使用 Marked 库解析 Markdown，配合自定义 CSS 实现图片自适应和代码高亮。

---

### 4️⃣ 全局搜索功能

前端发送关键词到后端，后端在 `title` 和 `content` 字段中进行模糊匹配，返回结果按更新时间降序排列，在弹窗中展示结果卡片并显示路径信息。

---

## API 接口文档

### 用户接口
- POST `/api/users/register` - 用户注册
- POST `/api/users/login` - 用户登录
- GET `/api/users/{id}` - 获取用户信息

### 目录接口
- GET `/api/categories/user/{userId}` - 获取用户所有目录
- POST `/api/categories` - 创建目录
- PUT `/api/categories/{id}` - 更新目录
- DELETE `/api/categories/{id}` - 删除目录（含子目录和笔记）

### 笔记接口
- GET `/api/notes/user/{userId}` - 获取用户所有笔记
- GET `/api/notes/category/{categoryId}` - 获取目录下的笔记
- GET `/api/notes/search` - 全局搜索笔记
- POST `/api/notes` - 创建笔记
- PUT `/api/notes/{id}` - 更新笔记
- DELETE `/api/notes/{id}` - 删除笔记

---

## 扩展功能建议（未来改进方向）

- [ ] 协作编辑（WebSocket实时同步）
- [ ] 版本历史记录
- [ ] 标签系统
- [ ] 导出为PDF/Word
- [ ] 离线支持（Service Worker）
- [ ] 暗色主题切换
- [ ] JWT Token认证增强安全性

---

## 许可证

MIT License

Copyright (c) 2026 HeXioahei
