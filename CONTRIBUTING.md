# Contributing

Thanks for your interest in `asset-workspace-system`.

## Before You Start

- Make sure your change fits the project定位：数字素材管理、工作区协作、管理员后台、本地开箱即用。
- Do not re-introduce removed private modules or domain-specific legacy features unless discussed first.
- Keep new defaults beginner-friendly for graduation-project users.
- 在公开讨论、代码评审和协作过程中，默认遵守 [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)。

## Local Development

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Pull Request Guidance

- Keep PRs focused.
- Update docs when changing setup, config, or exposed behavior.
- Prefer local file storage compatibility over cloud-only assumptions.
- 涉及漏洞、权限绕过、凭据泄露或敏感链路时，不要在公开 PR 或 issue 中直接披露利用细节，请先阅读 [SECURITY.md](./SECURITY.md)。
- Ensure these checks pass before submitting:

```bash
cd backend && mvn -q -DskipTests compile
cd frontend && npm run build
```

## Code Style

- Follow existing project style in each module.
- Prefer clear naming over clever abstractions.
- Keep student-facing setup simple and explicit.
