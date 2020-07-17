/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

-- Records of t_ds_user，user : admin , password : dolphinscheduler123
INSERT INTO t_ds_user ( ID, USER_NAME, USER_PASSWORD, USER_TYPE, EMAIL, PHONE, TENANT_ID, CREATE_TIME, UPDATE_TIME )
VALUES ( 1, 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', '754221241@qq.com', 'xx', '0', to_date('2018-03-27 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'), to_date('2018-03-27 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'));

-- Records of t_ds_alertgroup，dolphinscheduler warning group
INSERT INTO t_ds_alertgroup(ID,group_name,group_type,description,create_time,update_time)  VALUES (1,'dolphinscheduler warning group', '0', 'dolphinscheduler warning group',to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'), to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'));
INSERT INTO t_ds_relation_user_alertgroup(id,alertgroup_id,user_id,create_time,update_time) VALUES (1, '1', '1', to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'), to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'));

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_queue(id,queue_name,queue,create_time,update_time) VALUES (1,'default', 'default',to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'), to_date('2020-04-29 15:48:50' , 'yyyy-mm-dd hh24:mi:ss'));

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_version(ID,version) VALUES (1,'1.3.0');