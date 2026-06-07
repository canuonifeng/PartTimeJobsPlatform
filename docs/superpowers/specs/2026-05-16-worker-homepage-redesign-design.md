# Worker Homepage Redesign Design

## Goal
Rework the C-end home page to make three things prominent:
1. The greeting header stays visible.
2. Today's shift and its check-in status are the primary focus.
3. The check-in action is available directly on the home page.

The home page must no longer surface the `找活` entry as a main shortcut. `找活` remains available from the bottom tab bar.

## Current State
- `worker-uniapp/src/pages/index/index.vue` currently shows a greeting banner, four stat cards, and a hot jobs section.
- `worker-uniapp/src/pages/schedule/schedule.vue` already loads weekly shifts from `getMyShifts`.
- `worker-uniapp/src/pages/attendance/clockIn.vue` already supports today's check-in and check-out actions.
- `worker-uniapp/src/pages.json` already has a bottom tab bar entry for `pages/jobs/jobList`.

## Scope
- Keep the greeting header.
- Replace the current home body with a shift-first layout.
- Show today's shift check-in state and the primary check-in action on the home page.
- Show future shifts on the home page.
- Remove the `找活` shortcut from the home page.
- Keep the bottom tab bar unchanged.

## Non-Goals
- No redesign of the job list page.
- No change to the bottom tab bar structure.
- No new backend endpoint unless the existing schedule/attendance data is insufficient.

## Proposed Layout

### 1. Greeting Header
- Keep the existing greeting card styling.
- Show greeting text, worker name, and current date.
- Add a compact summary line for today's shift count.

### 2. Primary Card: Today's Shift
- This is the first main card on the page.
- Show the first relevant shift for today.
- If multiple shifts exist today, show one primary shift and a count like `今天还有 2 个班次`.
- Show:
  - shift title
  - time range
  - location
  - current attendance state
- Show one primary action:
  - not checked in: `去签到`
  - checked in: `已签到`
  - checked out: `已签退`
- If there is no shift today, show an empty state with a link to the schedule page.

### 3. Secondary Card: Future Shifts
- Show upcoming shifts after today.
- Default to the next 3-5 items so the section stays compact.
- Each item should show:
  - date
  - time range
  - job title
  - location
  - status badge
- Provide a `查看全部排班` action that goes to the schedule page.

### 4. Quick Actions
- Keep only the actions that support work flow:
  - 我的排班
  - 打卡
  - 我的收入
  - 消息
- Do not render `找活` here.

## Data Flow
- On page mount, load today's shifts and upcoming shifts with the existing schedule API.
- Derive today's primary shift and future shifts on the client.
- For the primary shift card, use existing attendance status fields where available.
- For direct check-in, reuse the attendance API already used by `clockIn.vue`.

## Behavior Rules
- Home page should not depend on the jobs list API.
- If today's shifts fail to load, show a retry state inside the main card.
- If there is no authenticated worker, show a login prompt instead of shift cards.
- The home page should remain usable on small mobile screens without horizontal scrolling.

## Acceptance Criteria
- Greeting banner remains at the top.
- `找活` is no longer visible in the home shortcuts.
- Today's shift and check-in state are visible immediately on page load.
- The primary check-in action is available on the home page.
- Future shifts are visible below today's shift.
- Bottom tab bar still contains `找活`.

## Testing Notes
- Verify the home page renders with and without today's shifts.
- Verify the check-in action states are correct for:
  - not checked in
  - checked in
  - checked out
- Verify the home page does not show the old hot jobs block.
- Verify the bottom tab bar still routes to job list.
