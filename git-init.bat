@echo off
chcp 65001 > nul

echo ========================================
echo 个人记事本应用 - Git 初始化脚本
echo ========================================
echo.

REM 进入项目根目录
cd /d "%~dp0"

REM 初始化 Git 仓库
echo [1/4] 初始化 Git 仓库...
git init

REM 添加所有文件到暂存区
echo [2/4] 添加所有文件到暂存区...
git add .

REM 配置提交信息
echo [3/4] 创建初始提交...
git commit -m "feat: 初始化个人记事本应用

## Phase 1: 项目基础搭建
- 初始化 Spring Boot 后端项目
- 初始化 Vue 3 前端项目
- 创建 MySQL 数据库表结构
- 实现用户认证模块（注册、登录、JWT）
- 实现统一响应和异常处理
- 实现 Spring Security + JWT 认证

## Phase 2: 核心功能开发
- 实现笔记管理后端 API（CRUD、归档、恢复、删除）
- 实现分类管理（树形结构）
- 实现标签管理（多对多关联）
- 实现笔记列表页面（卡片展示、分页）
- 实现 Markdown 编辑器（编辑+实时预览）

## 技术栈
- 后端: Spring Boot 2.7 + MyBatis-Plus + JWT
- 前端: Vue 3 + Element Plus + Pinia
- 数据库: MySQL 8.0
- Markdown: markdown-it + highlight.js

## 功能特性
- 用户注册和登录
- 笔记 CRUD 操作
- 分类管理（树形结构）
- 标签管理（多对多关联）
- Markdown 富文本编辑
- 笔记归档和删除
- 软删除和回收站"

REM 显示提交结果
echo.
echo [4/4] 显示提交记录...
git log --oneline

echo.
echo ========================================
echo ✅ Git 仓库初始化完成！
echo ========================================
pause
