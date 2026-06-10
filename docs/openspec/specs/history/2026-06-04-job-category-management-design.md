# Job Category Management Design

## Goal

Make job categories centrally managed by the platform and consumed dynamically by C-side worker job search and enterprise job creation/editing.

## Current State

- The database already has `job_categories` with `id`, `name`, `parent_id`, and `sort_order`.
- Platform service and platform PC already have a basic job category module, but it deletes categories instead of disabling them.
- Enterprise service exposes `/api/job-categories` and enterprise miniapp already calls it for job forms/templates.
- Worker miniapp job list currently hardcodes category tabs.
- C service does not expose job category APIs.

## Data Model

Add soft-state category status to `job_categories`:

- `status`: `ACTIVE` or `DISABLED`, default `ACTIVE`.

Disabled categories remain in the table so historical jobs keep their `category_id` and can still display category names.

## Platform Admin Behavior

Platform admin manages all categories:

- Create category.
- Edit category name, parent category, sort order, and status.
- Disable category instead of hard delete.
- Re-enable disabled category.
- Category list shows status.

The existing delete operation will be replaced by disable behavior in the UI and service layer. A physical delete endpoint is not needed for normal product flow.

## Public Category Read Behavior

C-side and enterprise-side read only active categories for selection/filtering:

- Preserve parent-child category tree.
- Sort by `sort_order` ascending.
- Disabled categories are excluded from picker/filter options.

## C-side Worker App

Add a C service category API:

- `GET /api/job-categories`
- Returns active category tree using the same VO shape as enterprise category API.

Update `worker-uniapp/src/pages/jobs/jobList.vue`:

- Replace hardcoded category tabs with API data.
- Prepend local `全部` tab.
- Clicking a category still passes `categoryId` to existing job search.
- If category loading fails, show only `全部` instead of stale hardcoded categories.

## Enterprise App

Keep enterprise miniapp category source as `/api/job-categories`, but ensure it returns active categories only.

Update enterprise job create/edit forms if needed to flatten the active tree for picker display while retaining category IDs.

## Backend Service Alignment

Both C service and enterprise service should read from shared `job_categories` table.

Enterprise service changes:

- Add `status` field to category entity/cmd/vo/mapper.
- `findAll` used by enterprise public API returns active categories only.
- Add an admin/all variant only if needed by platform; platform service already owns admin management.

Platform service changes:

- Add `status` field to entity/cmd/vo/mapper.
- Admin `findAll` returns all statuses.
- Update category instead of physical delete for disable.

C service changes:

- Add category entity/mapper/service/controller/VO or reuse local equivalents matching C service conventions.
- Public `findActive` returns active category tree.

## Migration and Data Repair

- Add `status` column if missing:
  - `ALTER TABLE job_categories ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';`
- Existing categories become active automatically.

## Testing

Verify:

- `platform-service` compile.
- `enterprise-service` compile.
- `c-service` compile.
- `platform-pc` build.
- `enterprise-uniapp` build if scripts exist.
- `worker-uniapp` `npm run build:mp-weixin`.
- Manual SQL/API check that disabled categories are hidden from C/enterprise options but visible in platform admin.

## Self Review

- Scope is focused on job category management and consumers.
- Parent-child structure is preserved.
- Disabling is soft-state, not hard deletion.
- C and enterprise read active categories from backend, not hardcoded frontends.
