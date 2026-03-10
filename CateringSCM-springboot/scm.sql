USE catering_scm;

-- ==========================================
-- 模块一：基础档案 (Base)
-- ERP思路：引入仓库概念
-- ==========================================

-- 1. 供应商表
DROP TABLE IF EXISTS `base_supplier`;
CREATE TABLE `base_supplier` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` varchar(100) NOT NULL COMMENT '供应商名称',
                                 `contact` varchar(50) COMMENT '联系人',
                                 `phone` varchar(20) COMMENT '电话',
                                 `status` tinyint(4) DEFAULT '1' COMMENT '状态',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 2. 物料表 (保持不变)
DROP TABLE IF EXISTS `base_material`;
CREATE TABLE `base_material` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` varchar(100) NOT NULL COMMENT '物料名称',
                                 `category` varchar(50) COMMENT '分类',
                                 `unit` varchar(20) COMMENT '单位',
                                 `price` decimal(10,2) COMMENT '参考价',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料表';

-- 3. 仓库表
--
DROP TABLE IF EXISTS `base_warehouse`;
CREATE TABLE `base_warehouse` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `name` varchar(50) NOT NULL COMMENT '仓库名称(如:主仓库,冷冻库)',
                                  `location` varchar(100) COMMENT '位置',
                                  `manager` varchar(50) COMMENT '负责人',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- ==========================================
-- 模块二：采购业务 (Business)
-- 增加审核状态、关联仓库
-- ==========================================

-- 4. 采购订单主表 (升级)
DROP TABLE IF EXISTS `scm_purchase_order`;
CREATE TABLE `scm_purchase_order` (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `order_no` varchar(50) NOT NULL COMMENT '单号',
                                      `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
                                      `warehouse_id` bigint(20) NOT NULL COMMENT '入库仓库ID',
                                      `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '总金额',
                                      `status` tinyint(4) DEFAULT '0' COMMENT '状态(0:待审核 1:审核通过 2:已入库 9:驳回)',
                                      `audit_time` datetime COMMENT '审核时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

-- 5. 采购订单明细 (升级)
DROP TABLE IF EXISTS `scm_purchase_detail`;
CREATE TABLE `scm_purchase_detail` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                       `order_id` bigint(20) NOT NULL COMMENT '订单ID',
                                       `material_id` bigint(20) NOT NULL COMMENT '物料ID',
                                       `plan_qty` int(11) NOT NULL COMMENT '计划采购数',
                                       `real_qty` int(11) DEFAULT '0' COMMENT '实际入库数(ERP允许实收与计划不同)',
                                       `price` decimal(10,2) COMMENT '采购单价',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细';

-- ==========================================
-- 模块三：库存中心 (Inventory)
-- ERP思路：实时库存 + 流水日志 (核心中的核心)
-- ==========================================

-- 6. 实时库存表 (快照)
-- 记录：哪个仓库、哪个物料、现在有多少
DROP TABLE IF EXISTS `scm_inventory`;
CREATE TABLE `scm_inventory` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `warehouse_id` bigint(20) NOT NULL COMMENT '仓库ID',
                                 `material_id` bigint(20) NOT NULL COMMENT '物料ID',
                                 `quantity` int(11) DEFAULT '0' COMMENT '当前库存',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_wh_mat` (`warehouse_id`, `material_id`) COMMENT '一个仓库一种物料只有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时库存表';

-- 7.库存流水日志表
-- 任何库存变动，都要记录下来。
DROP TABLE IF EXISTS `scm_stock_log`;
CREATE TABLE `scm_stock_log` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `order_no` varchar(50) COMMENT '关联单据号(采购单号/领料单号)',
                                 `type` tinyint(4) NOT NULL COMMENT '类型(1:采购入库 2:领料出库 3:盘点调整)',
                                 `warehouse_id` bigint(20) NOT NULL COMMENT '仓库ID',
                                 `material_id` bigint(20) NOT NULL COMMENT '物料ID',
                                 `change_qty` int(11) NOT NULL COMMENT '变动数量(+100 或 -50)',
                                 `before_qty` int(11) COMMENT '变动前数量',
                                 `after_qty` int(11) COMMENT '变动后数量',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水日志表';

-- 初始化一点数据
INSERT INTO `base_warehouse` (`name`) VALUES ('主仓库');
INSERT INTO `base_material` (`name`, `price`) VALUES ('土豆', 2.5);