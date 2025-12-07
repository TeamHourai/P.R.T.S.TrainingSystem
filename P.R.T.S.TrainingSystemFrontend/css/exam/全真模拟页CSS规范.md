# 考试模块 CSS 架构说明文档

## 概述

本考试模块的CSS采用模块化设计，将原本单一的 `exam.css` 文件分割为6个功能明确的模块文件，存放在 `/css/exam/` 目录下。这种设计提高了代码的可维护性、可读性和团队协作效率。

## 文件结构说明

### 1. **/css/exam/base.css** - 考试基础样式层
**作用**：定义考试模块专用的CSS变量和基础样式

**包含内容**：
- 考试专用CSS变量（扩展全局变量）
  - 考试状态色（已提交、当前题、已答、未答）
  - 计时器颜色（紧急、警告、正常）
- 考试全局重置和包装器
- 考试页面专用滚动条样式
- 核心动画定义（`fadeInUp`、`pulse`、`shake`）
- 动画工具类（`.exam-fade-in`、`.exam-pulse`、`.exam-shake`）

**适用场景**：
- 修改考试状态的颜色标识
- 调整计时器的颜色阈值
- 添加新的考试动画效果
- 更改考试页面的全局背景

**快速定位**：
```css
/* 修改考试状态颜色 */
--exam-answered: #4CAF50;

/* 修改计时器警告颜色 */
--time-warning: #FFA726;

/* 添加考试专用动画 */
@keyframes examAnimation { ... }
```

### 2. **/css/exam/layout.css** - 考试布局结构层
**作用**：定义考试页面的整体布局框架

**包含内容**：
- 考试头部布局（`.exam-header`、`.header-left`）
- 考试容器布局（`.exam-container`）
- 答题区域布局（`.question-area`、`.question-section`、`.section-header`）
- 答题卡区域布局（`.answer-sheet`、`.sheet-header`、`.sheet-content`）
- 移动端答题卡面板布局（`.mobile-sheet-panel`、`.panel-header`、`.panel-content`）
- 成绩页面布局（`.result-container`、`.result-card`、`.result-header`）

**适用场景**：
- 调整考试页面的整体布局结构
- 修改头部、答题区域、答题卡的相对位置
- 更改移动端答题卡面板的显示方式
- 调整成绩页面的布局结构

**快速定位**：
```css
/* 调整考试容器布局 */
.exam-container { display: flex; max-width: 1400px; }

/* 修改答题区域宽度 */
.question-area { max-width: 900px; }

/* 调整答题卡固定位置 */
.answer-sheet { position: sticky; top: 100px; }
```

### 3. **/css/exam/components.css** - 考试组件层
**作用**：定义考试模块专用的UI组件

**包含内容**：
- 计时器组件（`.timer`及其状态类：`.critical`、`.warning`、`.normal`）
- 时间信息组件（`.time-info`、`.time-elapsed`、`.time-remaining`）
- 按钮组件（`.submit-btn`、`.action-btn`及其变体）
- 答题卡题目项组件（`.question-number-item`及其状态类）
- 移动端答题卡切换按钮（`.mobile-sheet-toggle`、`.answered-count`）
- 关闭按钮组件（`.close-panel`、`.close-modal`）
- 模态框组件（`.modal-overlay`、`.modal-content`、`.modal-header`、`.modal-body`）

**适用场景**：
- 修改计时器的外观和状态样式
- 调整按钮的样式（颜色、大小、悬停效果）
- 更改答题卡题目项的颜色和状态
- 更新模态框的样式
- 调整移动端切换按钮的位置和样式

**快速定位**：
```css
/* 修改计时器样式 */
.timer { font-size: 1.2rem; font-weight: 600; }

/* 调整主要按钮 */
.action-btn.primary { background: var(--primary-color); }

/* 修改答题卡题目项状态 */
.question-number-item.current { border-color: var(--exam-current); }
```

### 4. **/css/exam/question.css** - 题目显示层
**作用**：处理题目显示与交互的详细样式

**包含内容**：
- 题目卡片样式（`.question-card`）
- 题目元数据（`.question-meta`、`.question-number`、`.question-difficulty`、`.question-points`）
- 题目文本（`.question-text`、代码块、预格式化文本）
- 题目图片（`.question-image`、`.image-caption`）
- 选项容器和选项样式（`.options-container`、`.option`及其状态类）
- 选项标签和文本（`.option-label`、`.option-text`）
- 提交区域（`.submit-section`）
- 答案分析（`.answer-analysis`、`.answer-analysis-content`、`.user-answer`、`.correct-answer`）

**适用场景**：
- 调整题目卡片的样式（边框、阴影、内边距）
- 修改题目文本的字体、大小和颜色
- 更改选项的选择状态样式（选中、正确、错误、用户选择）
- 调整答案分析的显示样式
- 修改题目图片的样式

**快速定位**：
```css
/* 调整题目卡片 */
.question-card { padding: 25px; margin-bottom: 20px; }

/* 修改选项悬停效果 */
.option:hover { transform: translateX(5px); }

/* 调整答案分析样式 */
.answer-analysis { border-left: 4px solid var(--primary-color); }
```

### 5. **/css/exam/result.css** - 成绩页面层
**作用**：处理考试成绩页面的详细样式

**包含内容**：
- 分数显示区域（`.score-display`、`.total-score`、`.score-label`、`.score-value`、`.score-grade`）
- 分数详情（`.score-details`、`.score-item`、`.section-name`、`.section-score`、`.section-percentage`）
- 统计图表容器（`.score-charts`、`.chart-container`、`.chart-title`）
- 题目回顾区域（`.question-review`、`.review-section`）
- 题目回顾项（`.question-review-items`、`.question-review-item`及其状态类）
- 详细回顾区域（`.detailed-review`、`.review-summary`、`.summary-item`）
- 结果操作按钮（`.result-actions`）
- 成绩分享（`.result-share`、`.share-buttons`、`.share-btn`）

**适用场景**：
- 修改分数显示的样式和布局
- 调整分数详情项的展示方式
- 更改题目回顾项的样式（正确、错误、未答）
- 修改分享按钮的样式
- 调整统计图表的容器样式

**快速定位**：
```css
/* 修改分数显示 */
.score-value { font-size: 3rem; font-weight: 700; }

/* 调整题目回顾项状态 */
.question-review-item.correct { background: rgba(67, 160, 71, 0.2); }

/* 修改分享按钮样式 */
.share-btn.wechat { background: #07C160; }
```

### 6. **/css/exam/responsive.css** - 考试响应式层
**作用**：处理所有考试页面的响应式适配

**包含内容**：
- 大屏设备（1024px以上）优化样式
- 中等屏幕（769px - 1024px）适配
- 平板设备（577px - 768px）适配
- 手机设备（480px - 576px）适配
- 小手机设备（小于480px）适配
- 横屏模式适配（高度小于600px且横向）
- 打印样式（隐藏不需要的元素，优化打印效果）

**适用场景**：
- 调整特定屏幕尺寸下的布局
- 优化移动端用户体验
- 修复特定设备上的显示问题
- 调整打印时的样式
- 添加新的断点响应

**快速定位**：
```css
/* 平板设备适配 */
@media (max-width: 768px) {
  .exam-header { flex-direction: column; }
}

/* 手机设备适配 */
@media (max-width: 576px) {
  .question-text { font-size: 1rem; }
}

/* 打印样式优化 */
@media print {
  .exam-header { display: none !important; }
}
```

## CSS文件加载顺序

在HTML文件中，必须按照以下顺序引入CSS文件以确保样式正确应用：

```html
<!-- 第1层：考试基础样式 -->
<link rel="stylesheet" href="/css/exam/base.css">

<!-- 第2层：考试布局样式 -->
<link rel="stylesheet" href="/css/exam/layout.css">

<!-- 第3层：考试组件样式 -->
<link rel="stylesheet" href="/css/exam/components.css">

<!-- 第4层：题目显示样式 -->
<link rel="stylesheet" href="/css/exam/question.css">

<!-- 第5层：成绩页面样式 -->
<link rel="stylesheet" href="/css/exam/result.css">

<!-- 第6层：响应式适配（必须最后加载） -->
<link rel="stylesheet" href="/css/exam/responsive.css">
```

## 加载顺序原则说明

1. **基础层优先**：`base.css` 定义考试专用变量和基础样式，必须在最前面
2. **结构层次之**：`layout.css` 定义整体布局框架
3. **组件层随后**：`components.css` 定义考试专用组件，依赖基础样式
4. **内容层分组**：`question.css` 处理题目显示，`result.css` 处理成绩页面
5. **响应式最后**：`responsive.css` 必须最后加载以覆盖前面的样式

## 快速查找指南

当需要修改样式时，请按以下流程定位：

### 1. 确定修改范围
- **考试全局设计** → `base.css`
- **页面布局结构** → `layout.css`
- **考试专用组件** → `components.css`
- **题目显示与交互** → `question.css`
- **成绩页面** → `result.css`
- **移动端适配** → `responsive.css`

### 2. 使用搜索关键词
| 修改内容 | 建议搜索关键词 | 主要文件 |
|---------|--------------|---------|
| 考试状态色 | `--exam-answered`, `--exam-current` | `base.css` |
| 计时器样式 | `.timer`, `.time-remaining` | `components.css` |
| 答题卡题目项 | `.question-number-item` | `components.css` |
| 题目卡片 | `.question-card`, `.question-meta` | `question.css` |
| 选项样式 | `.option`, `.option-label` | `question.css` |
| 分数显示 | `.score-display`, `.score-value` | `result.css` |
| 题目回顾 | `.question-review-item` | `result.css` |
| 移动端菜单 | `@media (max-width: 768px)` | `responsive.css` |

### 3. 常见修改示例

**示例1：修改考试计时器颜色**
1. 打开 `base.css`
2. 找到 `:root` 选择器
3. 修改 `--time-critical`、`--time-warning`、`--time-normal` 变量值

**示例2：调整答题卡题目项大小**
1. 打开 `components.css`
2. 搜索 `.question-number-item`
3. 修改 `width` 和 `height` 属性

**示例3：修改选项选中状态**
1. 打开 `question.css`
2. 搜索 `.option.selected`
3. 修改背景色或边框样式

**示例4：调整移动端布局**
1. 打开 `responsive.css`
2. 找到对应的媒体查询（如 `@media (max-width: 768px)`）
3. 修改对应组件的样式

**示例5：添加新的题目难度颜色**
1. 打开 `question.css`
2. 搜索 `.question-difficulty`
3. 添加新的难度类，如 `.question-difficulty.expert`

## 最佳实践

1. **优先修改变量**：需要调整设计参数时，优先修改CSS变量
2. **避免样式重复**：确保同一样式只在一个文件中定义
3. **遵循加载顺序**：不要随意调整CSS文件的加载顺序
4. **移动端优先**：在 `responsive.css` 中编写移动端样式时，使用覆盖而非重写
5. **注释清晰**：对复杂样式或特殊处理添加注释说明

## 维护建议

1. **新增考试组件**：通用考试组件添加到 `components.css`，特定组件添加到对应页面CSS文件
2. **新增题目类型**：在 `question.css` 中添加相应的样式
3. **新增成绩页面元素**：在 `result.css` 中添加样式
4. **新增响应式断点**：在 `responsive.css` 中添加新的媒体查询
5. **性能优化**：确保响应式样式只在需要的媒体查询中定义

通过这种模块化架构，我们可以更高效地维护和扩展考试模块的样式，每个团队成员都能快速定位和修改所需样式。