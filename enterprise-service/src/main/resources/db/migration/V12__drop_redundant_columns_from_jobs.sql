ALTER TABLE jobs
  DROP COLUMN IF EXISTS company_name,
  DROP COLUMN IF EXISTS company_logo,
  DROP COLUMN IF EXISTS category_name;
