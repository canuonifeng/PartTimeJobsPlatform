UPDATE jobs SET deadline = CONCAT(DATE(deadline), ' 23:59:59') WHERE deadline IS NOT NULL;
UPDATE c_job SET deadline = CONCAT(DATE(deadline), ' 23:59:59') WHERE deadline IS NOT NULL;
