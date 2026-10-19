DELETE FROM service_event; DELETE FROM purchase; DELETE FROM inventory; DELETE FROM vendor_goods; DELETE FROM buyer; DELETE FROM goods; DELETE FROM vendor;
INSERT INTO vendor VALUES(1,'晨曦设备'),(2,'远山电子'),(3,'青禾服务');
INSERT INTO goods VALUES(10,'开发工作站',6800),(20,'办公显示器',1800),(30,'网络设备',900);
INSERT INTO buyer VALUES(1,'东湖研究室','武汉'),(2,'西岭设计院','成都'),(3,'海湾培训中心','宁波');
INSERT INTO vendor_goods VALUES(1,10),(1,20),(2,10),(2,30),(3,20),(3,30);
INSERT INTO inventory VALUES(101,10,1,'EQ-101'),(102,10,2,'EQ-102'),(103,20,1,'EQ-103'),(104,30,2,'EQ-104'),(105,20,3,'EQ-105'),(106,30,3,'EQ-106');
INSERT INTO purchase VALUES(1,101,1,6500,'2026-08-01'),(2,102,2,6700,'2026-08-05'),(3,103,2,1700,'2026-08-06'),(4,104,3,850,'2026-08-07');
INSERT INTO service_event VALUES(1,1,1,'设备配置指导','2026-08-10'),(2,1,3,'环境诊断','2026-08-11'),(3,2,2,'现场升级','2026-08-12');
