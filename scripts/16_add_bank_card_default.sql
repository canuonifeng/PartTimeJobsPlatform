-- 添加银行卡默认标识字段
ALTER TABLE worker_bank_cards ADD COLUMN is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认卡' AFTER bank_branch;

-- 为每个工人的第一张卡设置为默认卡
UPDATE worker_bank_cards wbc1
INNER JOIN (
    SELECT worker_id, MIN(id) as min_id
    FROM worker_bank_cards
    GROUP BY worker_id
) wbc2 ON wbc1.worker_id = wbc2.worker_id AND wbc1.id = wbc2.min_id
SET wbc1.is_default = 1;
