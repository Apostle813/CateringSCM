-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: catering_scm
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `scm_inventory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_inventory` (
  `id` int NOT NULL AUTO_INCREMENT,
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` int DEFAULT '0' COMMENT '当前库存',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次入库时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wh_mat` (`warehouse_id`,`material_id`) COMMENT '一个仓库一种物料只有一条记录'
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实时库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_inventory`
--

INSERT INTO `scm_inventory` VALUES (1,1,1,163,'2026-04-25 22:20:21','2026-03-11 13:16:06'),(2,1,2,550,'2026-03-11 13:16:06','2026-03-11 13:16:06'),(3,2,3,50,'2026-03-11 13:16:06','2026-03-11 13:16:06'),(4,1,8,20,'2026-04-02 20:00:33','2026-04-02 20:00:33'),(5,1,9,10,'2026-04-02 20:00:33','2026-04-02 20:00:33'),(6,1,10,30,'2026-04-02 20:00:33','2026-04-02 20:00:33'),(7,2,6,15,'2026-04-02 20:00:33','2026-04-02 20:00:33');

--
-- Table structure for table `scm_material`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_material` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '物料名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `spec` text COMMENT '描述',
  `price` decimal(10,2) DEFAULT NULL COMMENT '参考价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2048046908932190211 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_material`
--

INSERT INTO `scm_material` VALUES (1,'精选五花肉','肉禽类','kg','去皮,肥瘦相间',35.50,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(2,'有机大白菜','蔬菜类','kg','无农药残留',3.20,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(3,'进口三文鱼','海鲜类','kg','整条去内脏,空运',120.00,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(4,'黄心土豆','蔬菜类','kg','无发芽,个头均匀',2.50,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(5,'走地黄鸡','肉禽类','只','约1.5kg/只',45.00,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(6,'特级雪花牛肉','肉禽类','kg','原切,冷冻',180.00,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(7,'新鲜番茄','蔬菜类','kg','红透,硬挺',5.50,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(8,'五常大米','粮油类','袋','10kg/袋',88.00,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(9,'大豆色拉油','粮油类','桶','5L/桶',65.00,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(10,'纯生啤酒','酒水类','箱','24瓶/箱',120.00,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1);

--
-- Table structure for table `scm_purchase_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_purchase_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `plan_qty` int NOT NULL COMMENT '计划采购数',
  `real_qty` int DEFAULT '0' COMMENT '实际入库数(ERP允许实收与计划不同)',
  `price` decimal(10,2) DEFAULT NULL COMMENT '采购单价',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_purchase_detail`
--

INSERT INTO `scm_purchase_detail` VALUES (1,1,2,50,50,3.20),(2,2,8,20,20,88.00),(3,2,9,10,10,65.00),(4,3,10,30,30,120.00),(5,4,1,10,10,10.00),(6,5,1,10,10,10.00),(7,6,1,10,10,10.00),(8,7,2,1,1,3.20);

--
-- Table structure for table `scm_purchase_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(50) NOT NULL COMMENT '单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '入库仓库ID',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '总金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT '0' COMMENT '状态(0:待审核 1:审核通过 2:已入库 9:驳回)',
  `payment_status` tinyint DEFAULT '0' COMMENT '财务结算状态(0:未打款 1:已打款)',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_user` bigint DEFAULT NULL COMMENT '审核人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_user` mediumtext COMMENT '创建订单用户的id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_purchase_order`
--

INSERT INTO `scm_purchase_order` VALUES (1,'P202603110001',1,1,160.00,'周末客流高峰加急补货，请库管尽快审核！',2,0,'2026-04-16 22:18:57',1,'2026-03-11 13:16:06',0,'2'),(2,'P202603200001',4,1,2410.00,'月底粮油常规补充',2,0,'2026-03-20 14:00:00',3,'2026-03-20 09:00:00',0,'2'),(3,'P202604010001',5,1,3600.00,'五一假期前酒水紧急囤货',2,1,'2026-04-01 11:30:00',3,'2026-04-01 10:00:00',0,'2'),(4,'P1777216168605',1,1,100.00,'门店极速直采补货',1,0,NULL,NULL,'2026-04-26 23:09:28',0,'0'),(5,'P1777216175249',1,1,100.00,'门店极速直采补货',2,0,NULL,NULL,'2026-04-26 23:09:35',0,'0'),(6,'P1777216203325',1,1,100.00,'门店极速直采补货',2,1,NULL,NULL,'2026-04-26 23:10:03',0,'0'),(7,'P1777267625690',1,1,3.20,'门店极速直采补货',0,0,NULL,NULL,'2026-04-27 13:27:05',0,'0');

--
-- Table structure for table `scm_requisition_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_requisition_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `plan_qty` int NOT NULL,
  `real_qty` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_requisition_detail`
--

INSERT INTO `scm_requisition_detail` VALUES (1,2,3,4,0),(2,2,4,1,0),(3,2,5,1,0);

--
-- Table structure for table `scm_requisition_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_requisition_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(50) NOT NULL COMMENT '出库单号(如:OUT20260425001)',
  `store_id` bigint NOT NULL COMMENT '申请门店ID',
  `warehouse_id` bigint NOT NULL COMMENT '发货仓库ID',
  `status` tinyint DEFAULT '0' COMMENT '状态(0:待审核 1:已配送出库)',
  `payment_status` tinyint DEFAULT '0' COMMENT '财务结算状态(0:未结算 1:已结算)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店请购出库单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_requisition_order`
--

INSERT INTO `scm_requisition_order` VALUES (1,'OUT1777268161596',4,1,1,1,'2026-04-27 13:36:02'),(2,'OUT1777268744696',2,2,0,0,'2026-04-27 13:45:45');

--
-- Table structure for table `scm_stock_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_stock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reference_no` varchar(50) DEFAULT NULL COMMENT '关联单据号(采购单号/领料单号/盘点号等)',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID(谁点的入库/出库)',
  `type` tinyint NOT NULL COMMENT '类型(1:采购入库 2:领料出库 3:盘点调整)',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `change_qty` int NOT NULL COMMENT '变动数量(+100 或 -50)',
  `before_qty` int DEFAULT NULL COMMENT '变动前数量',
  `after_qty` int DEFAULT NULL COMMENT '变动后数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_stock_log`
--

INSERT INTO `scm_stock_log` VALUES (10,'OUT1776352025952',1,2,1,1,-1,150,149,'2026-04-16 23:07:06'),(11,'OUT1776352047949',1,2,1,1,-1,149,148,'2026-04-16 23:07:28'),(12,'OUT1776352049229',1,2,1,1,-1,148,147,'2026-04-16 23:07:29'),(13,'盘点调整：null',1,3,1,1,1,147,148,'2026-04-16 23:07:31'),(14,'盘点调整：null',1,3,1,1,2,148,150,'2026-04-16 23:07:33'),(15,'OUT1777126696747',1,2,1,1,-1,150,149,'2026-04-25 22:18:17'),(16,'盘点调整：',1,3,1,1,-148,149,1,'2026-04-25 22:18:43'),(17,'盘点调整：',1,3,1,1,149,1,150,'2026-04-25 22:20:11'),(18,'OUT1777126811864',1,2,1,1,-1,150,149,'2026-04-25 22:20:13'),(19,'OUT1777126815436',1,2,1,1,-1,149,148,'2026-04-25 22:20:16'),(20,'OUT1777126817572',1,2,1,1,-3,148,145,'2026-04-25 22:20:19'),(21,'OUT1777126820308',1,2,1,1,-2,145,143,'2026-04-25 22:20:21'),(22,'P1777216175249',1,1,1,1,10,143,153,'2026-04-26 23:09:37'),(24,'P1777216203325',1,1,1,1,10,153,163,'2026-04-27 13:53:32');

--
-- Table structure for table `scm_supplier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_supplier`
--

INSERT INTO `scm_supplier` VALUES (1,'绿源生态农业基地','农老板','13911112222',1,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(2,'极客肉联厂批发部','肉老板','13933334444',1,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(3,'大洋远洋海鲜贸易','海哥','13955556666',1,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(4,'金龙粮油批发代理','王总','13888889999',1,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1),(5,'城市酒水直营店','刘经理','13777778888',1,'2026-04-02 20:00:33',1,'2026-04-02 20:00:33',1);

--
-- Table structure for table `scm_sys_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称(如:超级管理员,采购员,库管员)',
  `role_code` varchar(50) NOT NULL COMMENT '角色权限编码(ADMIN, PURCHASER, WAREHOUSE)',
  `description` varchar(200) DEFAULT NULL COMMENT '角色职责描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_sys_role`
--

INSERT INTO `scm_sys_role` VALUES (1,'超级管理员','ADMIN','拥有系统最高权限'),(2,'采购员','PURCHASER','负责发起采购申请与比价'),(3,'库管员','WAREHOUSE','负责入库审核、发料和盘点');

--
-- Table structure for table `scm_sys_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号(唯一)',
  `password` varchar(100) NOT NULL COMMENT '登录密码(MD5加密)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名(如:张三)',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `role_id` bigint NOT NULL COMMENT '关联的角色ID',
  `status` tinyint DEFAULT '1' COMMENT '账号状态(1:启用 0:停用)',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标识(0正常,1删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) COMMENT '保证账号不重复'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统统一用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_sys_user`
--

INSERT INTO `scm_sys_user` VALUES (1,'admin','e10adc3949ba59abbe56e057f20f883e','系统总管','13800000001',1,1,0,'2026-03-11 13:16:05','2026-03-11 13:16:05'),(2,'buyer','e10adc3949ba59abbe56e057f20f883e','张三(采购)','13800000002',2,1,0,'2026-03-11 13:16:05','2026-03-11 13:16:05'),(3,'stocker','e10adc3949ba59abbe56e057f20f883e','李四(库管)','13800000003',3,1,0,'2026-03-11 13:16:05','2026-03-11 13:16:05');

--
-- Table structure for table `scm_warehouse`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scm_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '仓库名称(如:主仓库,冷冻库)',
  `location` varchar(100) DEFAULT NULL COMMENT '位置',
  `manager` varchar(50) DEFAULT NULL COMMENT '负责人',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scm_warehouse`
--

INSERT INTO `scm_warehouse` VALUES (1,'主仓库(常温)','园区A栋1楼','李四',0,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(2,'冷冻库(-18℃)','园区A栋地下室','王五',0,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL),(3,'生鲜保鲜库','园区B栋1楼','赵六',0,'2026-03-11 13:16:05',NULL,'2026-03-11 13:16:05',NULL);
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-02 21:15:47
