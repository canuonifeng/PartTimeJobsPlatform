ALTER TABLE c_job
  ADD COLUMN headcount INT DEFAULT NULL COMMENT '招聘人数',
  ADD COLUMN deadline DATETIME DEFAULT NULL COMMENT '报名截止时间';

UPDATE c_job cj
JOIN jobs j ON cj.job_id = j.id
SET cj.headcount = j.headcount,
    cj.deadline = j.deadline;

UPDATE c_job cj
JOIN enterprises e ON cj.company_id = e.id
SET cj.company_name = e.company_name;
