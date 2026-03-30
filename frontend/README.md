# Frontend Workspace

`frontend/` 是 `asset-workspace-system` 的 Web 客户端工作区，基于 Vue 3、Vite、TypeScript、Pinia、Tailwind CSS 与 Ant Design Vue 构建。

这个模块负责素材展览页、搜索页、上传页、工作区协作页、个人中心与管理员后台界面的实现。

## 技术栈

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Tailwind CSS
- Ant Design Vue

## 目录重点

```text
frontend/
├── src/api/              OpenAPI 生成与接口封装
├── src/components/       页面组件与布局组件
├── src/layouts/          基础布局与后台布局
├── src/pages/            业务页面
├── src/stores/           Pinia 状态管理
├── src/assets/           品牌资源与全局样式
└── dist/                 生产构建产物
```

## 本地开发

安装依赖：

```bash
npm install
```

启动开发环境：

```bash
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

## 常用命令

类型检查：

```bash
npm run type-check
```

生产构建：

```bash
npm run build
```

代码格式化：

```bash
npm run format
```

接口代码生成：

```bash
npm run openapi
```

## 产物说明

- `npm run build` 会输出静态文件到 `frontend/dist`
- 如果后端以静态资源方式托管页面，可将 `dist` 同步到 `backend/html`
- 不建议直接手工修改 `backend/html` 中的构建产物，应以前端源码为准重新构建

## 相关文档

- 根文档：[../README.md](../README.md)
- 贡献说明：[../CONTRIBUTING.md](../CONTRIBUTING.md)
