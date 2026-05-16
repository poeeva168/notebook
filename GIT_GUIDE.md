# Git 使用指南

本项目使用 Git 进行版本控制。以下是常用的 Git 操作指南。

## 初始化仓库（首次使用）

### Windows 用户
双击运行 `git-init.bat` 文件即可自动完成：
- 初始化 Git 仓库
- 添加所有文件到暂存区
- 创建初始提交

### Mac/Linux 用户
在终端中运行：
```bash
cd notebook-0.1
chmod +x git-init.sh
./git-init.sh
```

## 日常开发

### 1. 查看当前状态
```bash
git status
```

### 2. 查看修改内容
```bash
git diff
```

### 3. 添加文件到暂存区
```bash
# 添加所有修改
git add .

# 添加特定文件
git add src/App.vue

# 添加特定类型的文件
git add *.js
```

### 4. 创建提交
```bash
git commit -m "feat: 添加新功能"
```

### 5. 查看提交历史
```bash
git log

# 简洁模式
git log --oneline

# 图形化显示
git log --graph
```

### 6. 推送代码到远程仓库
```bash
# 首次推送
git remote add origin https://your-repo-url.git
git push -u origin main

# 后续推送
git push
```

### 7. 拉取最新代码
```bash
git pull
```

## 分支管理

### 创建新分支
```bash
git checkout -b feature/new-feature
```

### 切换分支
```bash
git checkout main
git checkout feature/new-feature
```

### 合并分支
```bash
git checkout main
git merge feature/new-feature
```

### 删除分支
```bash
git branch -d feature/new-feature
```

## 常用命令速查

| 命令 | 说明 |
|------|------|
| `git init` | 初始化仓库 |
| `git clone url` | 克隆仓库 |
| `git status` | 查看状态 |
| `git add .` | 添加所有文件 |
| `git commit -m "message"` | 提交 |
| `git push` | 推送到远程 |
| `git pull` | 拉取远程 |
| `git branch` | 查看分支 |
| `git checkout -b` | 创建并切换分支 |
| `git merge` | 合并分支 |
| `git log` | 查看提交历史 |
| `git reset --hard` | 重置到某个提交 |

## Git 提交规范

本项目使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 提交类型
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构（不是新功能也不是修复）
- `perf`: 性能优化
- `test`: 测试相关
- `build`: 构建系统或依赖更新
- `ci`: CI 配置更新
- `chore`: 其他杂项

### 示例
```bash
git commit -m "feat: 添加笔记搜索功能"
git commit -m "fix: 修复登录页面样式问题"
git commit -m "docs: 更新 README"
git commit -m "refactor: 重构笔记服务层"
```

## 撤销操作

### 撤销工作区的修改
```bash
git checkout -- filename
```

### 撤销暂存区的文件
```bash
git reset HEAD filename
```

### 回退到某个提交
```bash
git reset --hard commit_hash
```

## 注意事项

1. **不要提交敏感信息**
   - 不要提交 `.env` 文件
   - 不要提交密码、API Key 等
   - 这些信息应该放在 `.gitignore` 中

2. **提交前先拉取最新代码**
   ```bash
   git pull origin main
   ```

3. **创建有意义的提交信息**
   - 说明做了什么，而不是怎么做
   - 使用现在时态
   - 保持简短（< 72 字符）

4. **频繁提交**
   - 每个功能完成就提交
   - 不要等到写了很多代码才提交
   - 提交信息要清晰描述改动

## 获取帮助

- Git 官方文档：https://git-scm.com/doc
- Git 教程：https://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000
