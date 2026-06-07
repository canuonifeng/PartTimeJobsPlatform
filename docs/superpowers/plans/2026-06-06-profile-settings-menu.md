# Profile Settings Menu Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the settings menu entry on the C-side “我的” page.

**Architecture:** Update the existing static `menuGroups` array in `worker-uniapp/src/pages/profile/profile.vue`. No backend changes are required.

**Tech Stack:** Vue 3 setup syntax, uni-app, existing navigation helper.

---

### Task 1: Restore Settings Menu

**Files:**
- Modify: `worker-uniapp/src/pages/profile/profile.vue`

- [ ] Add a second menu group containing `{ title: '设置', url: '/pages/settings/settings', icon: '设', iconClass: 'icon-gray' }`.
- [ ] Keep existing `existingPageUrls` entry for `/pages/settings/settings` unchanged.
- [ ] Run `npm run build:mp-weixin` in `worker-uniapp`.
- [ ] Run `git diff --check`.
