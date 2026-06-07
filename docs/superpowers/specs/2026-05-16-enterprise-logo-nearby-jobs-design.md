# Enterprise Logo + Nearby Jobs

## Goal
Make the worker-side job list show enterprise info, enterprise logo, and distance from the user's current location, while keeping `enterprises` as the source of truth for the logo.

## Scope
- Add `company_logo` to `enterprises`.
- Propagate `company_logo` into `c_job` so the worker-side can render job cards without extra cross-schema lookups.
- Extend the C-side job list response with company info, logo, and distance.
- Auto-request location on the worker job list page and sort by proximity on page load.

## Design

### Data Model
- Add `company_logo VARCHAR(500)` to `enterprises`.
- Add `company_logo VARCHAR(500)` to `c_job`.
- Keep `company_name` on both tables as-is.
- Keep `enterprises.company_logo` as the source of truth.

### Platform Side
- `platform-service` enterprise create/update flows will accept and persist `companyLogo`.
- `platform-pc` enterprise management UI will add an input for logo URL.
- When an enterprise logo changes, the platform service will update the matching `c_job.company_logo` values for that `company_id` so existing job cards stay current.

### Job Sync
- `enterprise-service` will continue to materialize jobs into `c_job`.
- The sync path will also copy `company_logo` from the enterprise record into `c_job`.
- This keeps the worker-side read path simple and consistent with the existing denormalized `company_name` field.

### C-side API
- Extend `JobSummaryVO` with:
  - `companyName`
  - `companyLogo`
  - `distanceKm`
- Extend `/api/jobs` to accept optional `latitude` and `longitude`.
- If coordinates are provided, sort results by Haversine distance ascending.
- If coordinates are missing or permission is denied, preserve existing order.

### Worker UI
- `worker-uniapp` job list page will call `uni.getLocation()` on mount.
- The request will pass the coordinates to `/api/jobs`.
- Each job card will show:
  - enterprise logo
  - enterprise name
  - distance like `1.2km`
- If logo is missing, show a neutral placeholder.

## Error Handling
- If location permission is denied, the list still loads normally and stays unsorted by distance.
- If an enterprise has no logo, the UI shows a fallback avatar.
- If coordinates are missing on a job, it stays in the list but is sorted after jobs with valid coordinates.

## Testing
- Update platform-service tests for enterprise create/update with logo.
- Update enterprise-service tests for c_job sync including logo.
- Add C-service tests for distance sort and summary fields.
- Update worker-uniapp smoke checks to confirm the job card renders logo/name/distance.

## Notes
- Logo is treated as a URL string.
- Distance is displayed in kilometers on the client, based on backend-calculated meters.
