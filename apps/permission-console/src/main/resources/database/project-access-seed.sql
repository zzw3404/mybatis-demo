INSERT INTO acl_user(id,username,password,real_name,status) VALUES(1,'admin','demo123','宋知远',1),(2,'reader','demo123','周若宁',1),(3,'editor','demo123','何景行',1);
INSERT INTO acl_role(id,role_name,description,status) VALUES(1,'管理角色','管理全部业务',1),(2,'浏览角色','浏览和新增',1),(3,'维护角色','浏览与修改',1);
INSERT INTO acl_resource(id,parent_id,resource_name,code,kind,sort_order) VALUES(1,0,'管理入口','system',1,1),(2,1,'人员入口','system:user',1,2),(3,1,'角色入口','system:role',1,3),(4,1,'资源入口','system:permission',1,4),(5,2,'查看','system:user:list',3,5),(6,2,'新建','system:user:add',3,6),(7,2,'修改','system:user:edit',3,7),(8,2,'移除','system:user:delete',3,8);
INSERT INTO acl_member(left_id,right_id) VALUES(1,1),(1,2),(1,3),(2,2),(3,3);
INSERT INTO acl_grant(left_id,right_id) VALUES(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(2,5),(2,6),(3,5),(3,7);
