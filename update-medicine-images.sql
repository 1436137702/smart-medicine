-- ============================================
-- 药品图片URL更新脚本
-- 说明：使用网络公开的药品图片URL
-- 使用方法：在MySQL中执行此脚本即可
-- ============================================

-- 阿莫西林胶囊
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/amoxilin.png' WHERE id = 1;

-- 999感冒灵颗粒
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/999.jpg' WHERE id = 2;

-- 开塞露
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/kaisailu.jpg' WHERE id = 3;

-- 三九胃泰颗粒
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/weitai.jpg' WHERE id = 4;

-- 999皮炎平
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/piyanping.jpg' WHERE id = 5;

-- 甲硝唑
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/jiaxiaocuo.jpg' WHERE id = 6;

-- 布洛芬缓释胶囊
UPDATE medicine SET img_path = 'https://smartmedicine-images.oss-cn-hangzhou.aliyuncs.com/smartmedicine-images/buluofen.jpg' WHERE id = 7;

-- 查看更新结果
SELECT id, medicine_name, img_path FROM medicine;
