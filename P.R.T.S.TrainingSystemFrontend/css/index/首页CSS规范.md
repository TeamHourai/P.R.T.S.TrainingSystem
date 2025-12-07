# CSS 模块化架构说明文档

## 概述

本项目的CSS采用了模块化架构设计，将原本单一的 `style.css` 文件分割为8个功能明确的模块文件。这种设计提高了代码的可维护性、可读性和团队协作效率。

## 文件结构说明

### 1. **base.css** - 基础样式层
**作用**：定义全局设计系统基础，提供CSS变量和全局样式

**包含内容**：
- 全局CSS重置 (`*` 选择器)
- CSS自定义属性（设计变量）定义
  - 颜色系统（主色、辅色、功能色）
  - 背景和文字颜色
  - 效果变量（圆角、阴影、过渡）
- 基础元素样式（`body` 元素）
- 核心动画定义（`fadeIn`）
- 自定义滚动条样式

**适用场景**：
- 修改网站整体配色方案
- 调整全局圆角、阴影等设计参数
- 更改网站背景或字体
- 添加新的设计变量

**快速定位**：
```css
/* 修改主色调 */
--primary-color: #5E35B1;

/* 修改圆角大小 */
--border-radius: 12px;

/* 修改整体背景 */
body { background: ... }
```

### 2. **layout.css** - 布局结构层
**作用**：定义页面整体布局框架和导航结构

**包含内容**：
- 应用容器（`.app-container`）
- 侧边栏导航系统
  - 侧边栏容器（`.sidebar`）
  - 导航菜单项（`.nav-item`）
  - 侧边栏头部和底部
- 主内容区域（`.main-content`）
- 页面头部（`.page-header`, `.page-title`, `.page-subtitle`）

**适用场景**：
- 调整侧边栏宽度或位置
- 修改导航菜单样式
- 调整主内容区域的边距
- 更改页面标题样式

**快速定位**：
```css
/* 调整侧边栏宽度 */
.sidebar { width: 280px; }

/* 修改导航项样式 */
.nav-item { padding: 18px 25px; }

/* 调整主内容区域 */
.main-content { margin-left: 280px; }
```

### 3. **components.css** - 通用组件层
**作用**：定义可复用的UI组件和交互元素

**包含内容**：
- 按钮组件（`.back-btn`, `.nav-btn`, `.auth-btn` 等）
- 卡片组件（`.dashboard-card`, `.card-icon`, `.card-title` 等）
- 表单元素（`.auth-input`, `.jump-input`, `.search-input`）
- 模态框系统（`.modal-overlay`, `.modal-content`, `.modal-header`）
- 标签页组件（`.system-notice-tabs`, `.tab`）
- 颜色图例（`.color-legend`, `.legend-item`）
- 统计展示组件（`.stat-item`, `.stat-value`, `.stat-label`）
- 删除按钮（`.delete-category-btn`, `.delete-question-btn`）

**适用场景**：
- 修改按钮样式（颜色、大小、悬停效果）
- 调整卡片设计（边框、阴影、动画）
- 更新表单输入框样式
- 更改模态框的外观
- 调整标签页的交互效果

**快速定位**：
```css
/* 修改主要按钮样式 */
.nav-btn.primary { background: var(--primary-color); }

/* 调整卡片悬停效果 */
.dashboard-card:hover { transform: translateY(-8px); }

/* 修改输入框焦点状态 */
.auth-input:focus { border-color: var(--primary-color); }
```

### 4. **study-pages.css** - 学习功能页面层
**作用**：处理学习相关页面的特定样式

**包含内容**：
- 仪表盘网格布局（`.dashboard-grid`）
- 分类管理系统
  - 分类容器（`.categories-container`）
  - 分类卡片（`.category-card`, `.category-header`）
- 练习模式选择器（`.mode-selector`, `.mode-option`）
- 培训控制区域（`.training-controls`）
- 快速跳题功能（`.quickjump-container`, `.quickjump-card`）
- 错题管理界面（`.wrong-controls`, `.wrong-stats`）
- 题目网格系统（`.questions-grid`, `.training-grid`, `.question-item`）
- 系统公告功能（`.update-notice-btn`, `.update-container`）

**适用场景**：
- 调整分类页面的布局
- 修改练习模式选择器的样式
- 更新错题本的展示方式
- 更改题目网格的大小和间距
- 调整系统公告的显示效果

**快速定位**：
```css
/* 调整分类卡片样式 */
.category-card:hover { transform: translateY(-5px); }

/* 修改练习模式选项 */
.mode-option.active { background: var(--primary-color); }

/* 调整题目网格 */
.questions-grid { grid-template-columns: repeat(auto-fill, minmax(60px, 1fr)); }
```

### 5. **exam-pages.css** - 考试功能页面层
**作用**：处理考试和答题相关页面的样式

**包含内容**：
- 考试信息展示（`.exam-info`, `.info-item`）
- 题源标识（`.question-source`）
- 答题页面核心组件
  - 问题容器（`.question-container`）
  - 问题卡片（`.question-card`）
  - 选项系统（`.option`, `.option-label`, `.option-text`）
- 导航按钮组（`.navigation-buttons`）
- 答案分析区域（`.answer-analysis`）
- 考试卡片（`.exam-card`, `.exam-start-btn`）
- 考试统计（`.exam-stats`）
- 题目统计（`.question-stats`, `.stats-grid`）
- 用户认证界面（`.auth-switch`）

**适用场景**：
- 修改答题页面的布局
- 调整选项的选择状态样式
- 更改考试统计的展示方式
- 更新用户认证界面的样式
- 调整答案分析的显示效果

**快速定位**：
```css
/* 修改选项选中状态 */
.option.selected { border-color: var(--primary-color); }

/* 调整答题卡片样式 */
.question-card { padding: 30px; }

/* 修改考试统计布局 */
.exam-stats { display: flex; justify-content: center; }
```

### 6. **search-pages.css** - 搜索功能页面层
**作用**：处理搜索相关页面的样式

**包含内容**：
- 搜索页面容器（`.search-container`）
- 搜索输入区域（`.search-input-section`, `.search-input-container`）
- 搜索结果展示
  - 结果网格（`.results-grid`）
  - 结果卡片（`.result-card`）
  - 结果元数据（`.result-meta`, `.result-id`）
  - 关键词标签（`.keyword-tag`）
- 无结果提示（`.no-results-section`, `.search-suggestions`）
- 搜索提示区域（`.search-tips-section`, `.tips-grid`, `.tip-card`）
- 热门关键词（`.popular-keywords`, `.keyword-examples`）

**适用场景**：
- 调整搜索页面的布局
- 修改搜索输入框的样式
- 更新搜索结果卡片的设计
- 更改搜索提示的展示方式
- 调整热门关键词的样式

**快速定位**：
```css
/* 修改搜索输入框 */
.search-input { padding: 18px 60px 18px 25px; }

/* 调整结果卡片悬停效果 */
.result-card:hover { transform: translateY(-5px); }

/* 修改关键词标签样式 */
.keyword-tag { padding: 4px 10px; border-radius: 12px; }
```

### 7. **admin.css** - 管理界面层
**作用**：处理管理员特定界面的样式

**包含内容**：
- 管理网格布局（`.admin-grid`）
- 移动端菜单切换按钮（`.mobile-menu-toggle`）

**适用场景**：
- 调整管理页面的布局
- 修改移动端菜单按钮的样式

**快速定位**：
```css
/* 调整管理网格布局 */
.admin-grid { grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); }

/* 修改移动菜单按钮 */
.mobile-menu-toggle { background: var(--primary-color); }
```

### 8. **responsive.css** - 响应式适配层
**作用**：处理所有响应式布局和移动端适配

**包含内容**：
- 1024px及以下屏幕的媒体查询
- 768px及以下屏幕的媒体查询
- 移动端布局调整
- 触控设备优化

**适用场景**：
- 调整特定屏幕尺寸下的布局
- 优化移动端用户体验
- 修复特定设备上的显示问题
- 添加新的断点响应

**快速定位**：
```css
/* 平板设备适配 */
@media (max-width: 1024px) {
  .sidebar { transform: translateX(-100%); }
}

/* 手机设备适配 */
@media (max-width: 768px) {
  .dashboard-grid { grid-template-columns: 1fr; }
}
```

## CSS文件加载顺序

在HTML文件中，必须按照以下顺序引入CSS文件以确保样式正确应用：

```html
<!-- 第1层：设计基础 -->
<link rel="stylesheet" href="css/index/base.css">

<!-- 第2层：布局框架 -->
<link rel="stylesheet" href="css/index/layout.css">

<!-- 第3层：通用组件 -->
<link rel="stylesheet" href="css/index/components.css">

<!-- 第4层：学习功能页面 -->
<link rel="stylesheet" href="css/index/study-pages.css">

<!-- 第5层：考试功能页面 -->
<link rel="stylesheet" href="css/index/exam-pages.css">

<!-- 第6层：搜索功能页面 -->
<link rel="stylesheet" href="css/index/search-pages.css">

<!-- 第7层：管理界面（按需加载） -->
<link rel="stylesheet" href="css/index/admin.css">

<!-- 第8层：响应式适配（必须最后加载） -->
<link rel="stylesheet" href="css/index/responsive.css">
```

## 加载顺序原则说明

1. **基础层优先**：`base.css` 定义CSS变量，必须在最前面
2. **结构层次之**：`layout.css` 定义整体布局框架
3. **组件层随后**：`components.css` 定义通用组件，依赖基础样式
4. **页面层分组**：各功能页面样式按使用频率和依赖关系排序
5. **响应式最后**：`responsive.css` 必须最后加载以覆盖前面的样式

## 快速查找指南

当需要修改样式时，请按以下流程定位：

### 1. 确定修改范围
- **全局设计** → `base.css`
- **布局结构** → `layout.css`
- **通用组件** → `components.css`
- **特定页面** → 对应功能CSS文件
- **移动端适配** → `responsive.css`

### 2. 使用搜索关键词
| 修改内容 | 建议搜索关键词 | 主要文件 |
|---------|--------------|---------|
| 颜色主题 | `--primary-color`, `--bg-card` | `base.css` |
| 按钮样式 | `.nav-btn`, `.back-btn` | `components.css` |
| 卡片设计 | `.dashboard-card`, `.card-title` | `components.css` |
| 答题页面 | `.question-card`, `.option` | `exam-pages.css` |
| 搜索结果 | `.result-card`, `.search-input` | `search-pages.css` |
| 分类管理 | `.category-card`, `.categories-container` | `study-pages.css` |
| 移动菜单 | `@media (max-width: 768px)` | `responsive.css` |

### 3. 常见修改示例

**示例1：修改主色调**
1. 打开 `base.css`
2. 找到 `:root` 选择器
3. 修改 `--primary-color` 变量值

**示例2：调整按钮圆角**
1. 打开 `base.css`
2. 找到 `:root` 选择器
3. 修改 `--border-radius` 变量值

**示例3：修改答题选项选中状态**
1. 打开 `exam-pages.css`
2. 搜索 `.option.selected`
3. 修改背景色或边框样式

**示例4：调整移动端布局**
1. 打开 `responsive.css`
2. 找到对应的媒体查询（`768px` 或 `1024px`）
3. 修改对应组件的样式

## 最佳实践

1. **优先修改变量**：需要调整设计参数时，优先修改CSS变量
2. **避免样式重复**：确保同一样式只在一个文件中定义
3. **遵循加载顺序**：不要随意调整CSS文件的加载顺序
4. **移动端优先**：在 `responsive.css` 中编写移动端样式时，使用覆盖而非重写
5. **注释清晰**：对复杂样式或特殊处理添加注释说明

## 维护建议

1. **新增页面**：根据页面功能选择对应CSS文件添加样式
2. **新增组件**：通用组件添加到 `components.css`，特定组件添加到对应页面CSS文件
3. **删除功能**：删除对应页面的CSS文件中的相关样式
4. **性能优化**：按需加载CSS文件（如管理员样式）

通过这种模块化架构，我们可以更高效地维护和扩展项目样式，每个团队成员都能快速定位和修改所需样式。