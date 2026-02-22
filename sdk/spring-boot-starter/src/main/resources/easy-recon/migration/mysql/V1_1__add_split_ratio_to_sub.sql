-- Add split_ratio to easy_recon_order_sub
ALTER TABLE `easy_recon_order_sub` ADD COLUMN `split_ratio` INT NULL COMMENT '分账比例 (基点)' AFTER `fee_fen`;
