ALTER TABLE schedule_shifts
    ADD UNIQUE KEY uk_schedule_shifts_application_schedule (application_id, job_id, worker_id, shift_date, start_time, end_time);
