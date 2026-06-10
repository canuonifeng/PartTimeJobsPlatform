# C-Side UI & Salary Display Fixes

## Overview

Fix 4 issues in the worker uniapp: tab bar fixation, "立即报名" button fixation, salary display by rules, and tab bar icons.

## Changes

### 1. Tab Bar Fixed at Bottom + Icons

- `pages.json` already has native uni-app tabBar config with all 4 pages and icon paths
- Native tabBar is inherently fixed at bottom, no CSS changes needed
- Icon files exist at `src/static/*.png` (48x48, valid PNG) — confirm display
- No changes required for this item

### 2. "立即报名" Button Fixed at Bottom

- `jobDetail.vue` `.bottom-bar` already has `position: fixed; bottom: 0; left: 0; right: 0` with safe-area padding
- Verified: already implemented
- No changes required

### 3. Salary Display by Rules

**Backend** (`c-service`):

- **JobMapper.xml**: Add `LEFT JOIN job_rates jr ON j.id = jr.job_id` to `jobSelectWithJoins` SQL fragment; add `<collection property="rates" ofType="com.parttime.cservice.pojo.vo.JobRateInfoVO">` result mapping to map id/type/amount
- **JobServiceImpl.toSummary()**: Compute minRate/maxRate from `job.getRates()` instead of single `rateAmount`; populate rateTypes from all rates
- **JobServiceImpl.toDetail()**: Populate `detail.setRates()` from `job.getRates()` (all rates) instead of creating single rate from `rateType`/`rateAmount`
- **JobSummaryVO.java**: Add `List<JobRateInfoVO> rates` field (reuse `JobRateInfoVO`)
- Remove `searchJobs()` filter that uses `job.getRateAmount()` — replace with rate-agnostic filter or compute from all rates

**Frontend** (`worker-uniapp`):

- **jobList.vue**: Replace `{{ job.minRate }}-{{ job.maxRate }}元/{{ rateUnit }}` with multi-rate display iterating `job.rates`, formatted as `25元/小时 + 200元/天`

### 4. Tab Bar Icons

- Icons at `src/static/` are valid 48x48 PNGs for all 4 tabs (home, jobs, message, profile) with active/inactive variants
- Already configured in `pages.json` — confirm rendering

## Files Modified

| File | Change |
|------|--------|
| `c-service/.../JobMapper.xml` | LEFT JOIN job_rates + collection mapping |
| `c-service/.../JobServiceImpl.java` | toSummary/toDetail use rates list, fix filter |
| `c-service/.../JobSummaryVO.java` | Add `List<JobRateInfoVO> rates` field |
| `worker-uniapp/.../jobList.vue` | Multi-rate salary display |
