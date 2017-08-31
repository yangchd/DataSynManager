/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50715
Source Host           : localhost:3306
Source Database       : datasyn

Target Server Type    : MYSQL
Target Server Version : 50715
File Encoding         : 65001

Date: 2017-08-31 17:57:43
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `syn_datasource`
-- ----------------------------
DROP TABLE IF EXISTS `syn_datasource`;
CREATE TABLE `syn_datasource` (
  `pk_datasource` varchar(36) NOT NULL,
  `driver` varchar(50) DEFAULT NULL,
  `url` varchar(200) DEFAULT NULL,
  `basename` varchar(20) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`pk_datasource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of syn_datasource
-- ----------------------------
INSERT INTO `syn_datasource` VALUES ('129f9fb0-6a30-4d1a-8aee-6ddc53b30440', 'com.mysql.jdbc.Driver', 'jdbc:mysql://192.168.8.117:3306/datasyn', 'datasyn', 'ycd', '111111');
INSERT INTO `syn_datasource` VALUES ('4258c025-5b78-4d21-bcbc-ad670eeb5736', 'oracle.jdbc.driver.OracleDriver', 'jdbc:oracle:thin:@10.4.102.22:1521:orcl', 'orcl', 'ipatest', '1');
INSERT INTO `syn_datasource` VALUES ('6878aee1-87bf-4550-a95a-e38b0c3a65e9', 'com.mysql.jdbc.Driver', 'jdbc:mysql://10.4.102.51:3306/datasyn', 'datasyn', 'ycd', '111111');
INSERT INTO `syn_datasource` VALUES ('75f6e549-139a-4df9-9d46-43df88d6be6b', 'com.mysql.jdbc.Driver', 'jdbc:mysql://10.4.102.22:3306/madb', 'madb', 'ma', 'ma');
INSERT INTO `syn_datasource` VALUES ('d92f47e1-245b-4062-8139-57851f537a25', 'com.mysql.jdbc.Driver', 'jdbc:mysql://192.168.8.117:3306/madb', 'madb', 'ycd', '111111');

-- ----------------------------
-- Table structure for `syn_datasyn`
-- ----------------------------
DROP TABLE IF EXISTS `syn_datasyn`;
CREATE TABLE `syn_datasyn` (
  `pk_sync` varchar(36) NOT NULL,
  `pk_datafrom` varchar(36) DEFAULT NULL,
  `pk_datato` varchar(36) DEFAULT NULL,
  `pk_syntable` varchar(36) DEFAULT NULL,
  `flag` varchar(10) DEFAULT '0',
  `lasttime` varchar(19) DEFAULT NULL,
  `timecost` varchar(20) DEFAULT NULL,
  `datasynmsg` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`pk_sync`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of syn_datasyn
-- ----------------------------
INSERT INTO `syn_datasyn` VALUES ('a9b86252-1d03-4d4b-83eb-186ded856f32', 'd92f47e1-245b-4062-8139-57851f537a25', '129f9fb0-6a30-4d1a-8aee-6ddc53b30440', '109d853b-29ab-4d43-9b2f-f92f28f81b3c', 'true', '2017-08-31 14:12:55', '50ms', '同步成功！');

-- ----------------------------
-- Table structure for `syn_table`
-- ----------------------------
DROP TABLE IF EXISTS `syn_table`;
CREATE TABLE `syn_table` (
  `pk_table` varchar(36) NOT NULL,
  `tablename` varchar(50) DEFAULT NULL,
  `tablekey` varchar(50) DEFAULT NULL,
  `fromtables` varchar(200) DEFAULT NULL,
  `tablesrelation` varchar(500) DEFAULT NULL,
  `allcolumn` varchar(500) DEFAULT NULL,
  `relation` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`pk_table`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of syn_table
-- ----------------------------
INSERT INTO `syn_table` VALUES ('109d853b-29ab-4d43-9b2f-f92f28f81b3c', 'jrpt_user_dept', 'user_code,pk_dept', 'jrpt_user_dept_copy', '', 'user_code,cuserid,pk_dept,dept_code,emp_order,extend1,extend2,dr', '{\"user_code\":\"jrpt_user_dept_copy.user_code\",\"cuserid\":\"jrpt_user_dept_copy.cuserid\",\"pk_dept\":\"jrpt_user_dept_copy.pk_dept\",\"dept_code\":\"jrpt_user_dept_copy.dept_code\",\"emp_order\":\"\",\"extend1\":\"\",\"extend2\":\"\",\"dr\":\"\"}');
