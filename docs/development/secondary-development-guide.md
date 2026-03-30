# 二次开发说明

## 文档目标

本文档面向准备在 `asset-workspace-system` 基础上继续扩展业务的开发者，重点说明三个高频切入面：

- 权限控制链路如何分层
- 资源上传链路如何从入口走到存储
- 工作区协作模型如何组织成员、邀请与资源访问

本文档描述的是当前仓库已经落地的结构与约束，不等价于未来的理想架构。进行二次开发时，应先识别“当前实现事实”和“后续规划方向”之间的差异，再决定修改策略。

## 模块分层

当前仓库的核心职责可按以下方式理解：

- `backend/src/main/java/com/xxr/lingtuthinktank/controller`
  负责 HTTP 入口、参数校验、权限注解声明与响应封装。
- `backend/src/main/java/com/xxr/lingtuthinktank/service`
  负责业务规则、对象装配、批量操作与事务边界。
- `backend/src/main/java/com/xxr/lingtuthinktank/manager`
  负责更偏基础设施或流程模板的能力，例如上传模板、工作区权限管理、文件访问与邮件集成。
- `backend/src/main/java/com/xxr/lingtuthinktank/mapper`
  负责数据库访问。
- `frontend/src/router`
  负责页面路由和访问入口。
- `frontend/src/access`
  负责前端页面级访问控制。
- `frontend/src/pages/asset` 与 `frontend/src/pages/workspace`
  分别承载资源管理与工作区协作页面。

二次开发时，建议优先沿着现有边界扩展，而不是直接跨层拼接逻辑。这样可以把行为变更限制在较小范围内，降低回归风险。

## 权限模型

### 系统级权限

后端系统级权限入口由 `@AuthCheck` 与 AOP 拦截器组成：

- `backend/src/main/java/com/xxr/lingtuthinktank/annotation/AuthCheck.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/aop/AuthInterceptor.java`

`AuthInterceptor` 在进入控制器方法前读取当前登录用户，并执行两类判断：

- 被封禁用户直接拒绝访问
- 若控制器声明了 `mustRole`，则按系统角色做精确校验

当前系统角色定义见：

- `backend/src/main/java/com/xxr/lingtuthinktank/model/enums/user/UserRoleEnum.java`

角色集合为：

- `user`
- `admin`
- `ban`

### 前端页面级权限

前端页面访问控制由路由守卫和角色判定函数组成：

- `frontend/src/access/index.ts`
- `frontend/src/access/checkAccess.ts`

页面的 `meta.access` 决定进入该路由所需的最低权限。路由守卫会先尝试恢复本地登录态，再在跳转前完成页面级访问判定。这个层级只负责前端导航控制，不能替代后端的接口鉴权。

### 工作区级权限

工作区权限与资源权限的核心收口在：

- `backend/src/main/java/com/xxr/lingtuthinktank/annotation/SpaceAuthCheck.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/manager/workspace/SpaceAuthManager.java`

工作区类型定义见：

- `backend/src/main/java/com/xxr/lingtuthinktank/model/enums/workspace/SpaceTypeEnum.java`

当前包含两类空间：

- `PRIVATE`
- `TEAM`

工作区成员角色定义见：

- `backend/src/main/java/com/xxr/lingtuthinktank/model/enums/workspace/SpaceUserRoleEnum.java`

当前成员角色包括：

- `viewer`
- `editor`
- `admin`

`SpaceAuthManager` 的职责不是简单判断“是不是成员”，而是把多个业务语义统一到同一处：

- 空间查看权限
- 空间编辑权限
- 空间管理员权限
- 资源查看权限
- 资源编辑权限
- 资源删除权限
- 分享码校验

因此，二次开发中如果你要新增“工作区内可见但不可编辑”“带时效的分享访问”“资源草稿态隔离”等能力，优先修改或扩展 `SpaceAuthManager`，而不是在每个控制器里散落条件判断。

## 上传链路

### 入口层

资源上传入口主要集中在：

- `backend/src/main/java/com/xxr/lingtuthinktank/controller/asset/PictureController.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/controller/file/FileUploadController.java`

当前提供的入口形态包括：

- 单文件上传
- 多文件上传
- URL 上传
- 批量抓取预览与确认入库
- 文件上传但不入库，仅返回 URL

前端入口页面主要在：

- `frontend/src/pages/asset/AssetUploadPage.vue`
- `frontend/src/pages/asset/AssetBatchUploadPage.vue`
- `frontend/src/pages/user/UserMediaPage.vue`

### 模板流程

上传模板的核心抽象在：

- `backend/src/main/java/com/xxr/lingtuthinktank/manager/asset/upload/PictureUploadTemplate.java`

该模板当前的标准执行顺序是：

1. 校验输入源
2. 生成上传路径
3. 构造临时文件
4. 将输入源处理为本地临时文件
5. 写入存储并生成元数据
6. 清理临时文件

本地文件上传、URL 上传等差异点，分别落在各自子类中，例如：

- `backend/src/main/java/com/xxr/lingtuthinktank/manager/asset/upload/UrlPictureUpload.java`

### 当前存储实现现状

这里需要特别注意一个二次开发风险点。

配置层已经暴露了本地文件存储相关参数：

- `backend/src/main/resources/application.yml`
- `backend/.env.example`

并且仓库内也存在本地文件管理与本地访问控制器：

- `backend/src/main/java/com/xxr/lingtuthinktank/manager/file/LocalFileManager.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/controller/file/FileController.java`

但 `PictureUploadTemplate` 当前主流程仍直接调用 `saveToCloud(...)`，也就是上传主干仍以 `OssManager` 为中心。这意味着：

- 配置层已经具备“本地文件存储”语义
- 局部实现已经存在本地文件管理能力
- 资源上传主链路尚未完全收敛到统一的“存储策略抽象”

如果你计划把仓库继续向“默认本地存储完全可用”推进，建议先做一层统一存储策略接口，再让上传模板在本地与云存储之间切换，而不是在现有控制器里直接插入条件分支。

### 上传路径与资源元数据

上传链路目前会在返回结果中生成资源元数据，例如：

- 原始名称
- 格式
- 宽高与比例
- 体积
- 主色
- 缩略图地址

若要扩展更多资源类型，应优先考虑以下问题：

- 元数据提取是否需要根据 MIME 类型拆分
- 缩略图是否必须同步生成
- URL 上传是否允许跨站点抓取
- 是否要为工作区资源与公开资源使用不同路径前缀

## 工作区协作模型

### 数据与职责分布

工作区相关后端职责主要落在：

- `backend/src/main/java/com/xxr/lingtuthinktank/controller/workspace/SpaceController.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/controller/workspace/SpaceUserController.java`
- `backend/src/main/java/com/xxr/lingtuthinktank/service/workspace`
- `backend/src/main/java/com/xxr/lingtuthinktank/manager/workspace/SpaceAuthManager.java`

前端对应页面主要在：

- `frontend/src/pages/workspace/CreateWorkspacePage.vue`
- `frontend/src/pages/workspace/MyWorkspacePage.vue`
- `frontend/src/pages/workspace/WorkspaceDetailPage.vue`
- `frontend/src/pages/workspace/WorkspaceInvitePage.vue`

### 协作闭环

当前工作区协作链路包含以下基本闭环：

- 创建私有空间或团队空间
- 邀请成员加入团队空间
- 用户申请加入团队空间
- 维护成员角色
- 在工作区内上传、编辑、删除和打包下载资源
- 基于编辑日志、评论、点赞、收藏、通知与加入申请构建活动流

`SpaceUserController` 负责邀请、申请、成员增删改查和邀请响应。`WorkspaceInvitePage.vue` 负责前端的接受邀请、拒绝邀请和无待处理邀请时的申请加入入口。这个结构说明，工作区协作已经不是单纯的资源分组能力，而是带成员关系和通知状态的业务域。

### 二次开发建议

如果你要扩展工作区协作，请优先先决定你要改动的是哪一个层级：

- 访问策略层：修改 `SpaceAuthManager`
- 成员生命周期层：修改 `SpaceUserController` 与 `SpaceUserService`
- 协作表现层：修改 `WorkspaceDetailPage.vue` 与相关 API 调用
- 审计与活动流层：修改 `SpaceController` 中的聚合查询与活动封装

不要把“新增成员角色”“新增加入方式”“新增资源分享模式”视为单点修改。它们通常会同时影响枚举、权限管理器、控制器参数、通知文案和前端状态展示。

## 二次开发优先顺序建议

为了控制复杂度，推荐按以下顺序推进二次开发：

1. 明确权限边界，再新增页面或接口
2. 先稳定上传与存储策略，再扩展资源类型
3. 先稳定成员模型，再扩展协作流程
4. 先统一命名和文档，再进行大规模重构

如果你准备继续推进“完全本地可运行的开源模板”，建议把“存储策略统一”和“命名重构”作为优先级更高的两条主线。
