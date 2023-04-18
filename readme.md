# HIVE

## 介绍

1) 数据仓库
2) 包含解释器，编译器，优化器
3) 运行时，元数据存储在关系型数据库中

## 架构

### CLI、JDBC、webUI

### Thrift Server

### MetaStore

### Dirver

## 命令

1) desc formatted table_name;
   查询数据所有元数据信息。
2) create table table_name (id int, name string, likes array<string>, address map<string, string>)
   row format delimited
   fields terminated by ','
   collection items terminated by '-'
   map keys terminated by ':';  
   数据格式：```1,小明1,lol-book-movie,beijing:xisanqi-shanghai:pudong```
3) load data local inpath './data/table' into table table_name; 本地文件路径
4) load data inpath '/data/table' into table table_name; hdfs 文件路径
   这两种insert 快的原因就是文件的拷贝， 等于 hdfs dfs -put
5) 默认分隔符 ^A = \001 ^B= \002 ^C = \003
6) external 外部表注意事项：
    1) 必须和 location 一起使用， 指定数据存储路径（是文件夹而不是文件名）
    2) 内部表删除时 元数据和数据都会被删除， 外部表只会删除元数据而数据会保留下来。
    3) 应用场景：可以先创建外部表后添加数据， 也可以先创建数据后添加外部表
7) partitioned by 分区 分区字段不用在括号中创建，
   create table table_name (id int, name string, likes array<string>, address map<string, string>)
   partitioned by (gender string)
   row format delimited
   fields terminated by ','
   collection items terminated by '-'
   map keys terminated by ':';
8) alter table table_name add partition(col_name=col_value); 添加分区列时必须要添加所有的分区列
9) alter table table_name drop partition(col_name=col_value); 删除分区列如果不指定多个，会把多个分区的相同子分区全部删除
10) msck repair table table_name; 可以修复先创建数据在创建分区表查询不到数据的问题。
11) from table_name
    insert into table_tempA
    select id, name
    insert into table_tempB
    select id;
    前提是需要先创建好 table_tempA 和 table_tempB 再执行命令就可以把数据导入到table_tempA/B 中
12) ROW FORMAT SERDE 正则字段内容
    CREATE TABLE table_name (
    host STRING,
    identity STRING,
    t_user STRING,
    time STRING,
    request STRING,
    referer STRING,
    agent STRING)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.RegexSerDe'
    WITH SERDEPROPERTIES (
    "input.regex" = "([^ ]*) ([^ ]*) ([^ ]*) \\[(.*)\\] \"(.*)\" (-|[0-9]*) (-|[0- 9]*)"
    )
13) source *.sql; 直接执行sql 文件
14) insert into table_temp partition(gender,age) select id, name, gender, age from table_name; 设置动态分区添加数据
15) select explode(likes), explode(address) from table_name; 这种是无法执行的
    需要使用 lateral view 进行执行即：
    select count(col1), count(col2) from table_name
    lateral view explode(likes) tmp as col1
    lateral view explode(address) tmp as col2, col3
16) create view view_name as select * from table_name where id >2;

## HiveServer2

### 介绍

1) 启动命令: hiveserver2
2) webUI: http://localhost:10002/
3) 执行Client命令：beeline
4) beeline> !connect jdbc:hive2://localhost:10000/default root 12345678
5) 如果报错：User: root is not allowed to impersonate root 需要修改 core-site.xml配置
    1)
    ```
    <property>
         <name>hadoop.proxyuser.root.groups</name>	
         <value>*</value>
     </property>
     <property>
         <name>hadoop.proxyuser.root.hosts</name>	
         <value>*</value>
     </property> 
    ```
    2)
    ``` hdfs dfsadmin -fs hdfs://node01:8020 -refreshSuperUserGroupsConfiguration ```
6) beeline 只能执行查询操作，无法执行增删改。

## UDF、UDAF、UDTF

### 临时函数

1) 将文件打包到服务器
2) 进入hive客户端，添加jar包：hive>add jar target/hive-learning-1.0-SNAPSHOT.jar;
3) 创建临时函数：hive> CREATE TEMPORARY FUNCTION myudf AS 'org.example.MyUDF';
4) 查询HQL语句：hive> SELECT myudf(name) from table_name;
5) 销毁临时函数：hive> drop temporary function myudf

### 持久函数

1) 将jar包上传到hdfs集群中: hdfs dfs -put target/hive-learning-1.0-SNAPSHOT.jar /data/udf
2) 创建函数：hive> CREATE FUNCTION myudf AS 'org.example.MyUDF' using jar "hdfs://localhost:
   8020/data/udf/hive-learning-1.0-SNAPSHOT.jar"
3) 查询HQL语句：hive> SELECT myudf(name) from table_name;
4) 删除函数：hive> DROP FUNCTION myudf;

## HIVE 参数和操作

### 参数变量设置方式

1) hive --hiveconf hive.cli.print.header=true 这种是当前会话添加表头
2) hive> set hive.cli.print.header=true; 这种是当前hive会话添加表头
3) 创建 ～/.hiverc 文件并在其中写入 set hive.cli.print.header=true 这种就是永久文件

### 运行方式

1) cli
2) 脚本运行方式：hive --service cli help 查询运行方式
    1) hive -D abc = 1 这个是是设置参数，select * from table_name where id = ${abc};
    2) hive -e "select * from table_name"  直接运行sql
    3) 创建sql 文件 然后直接运行 hive -f sql 文件
    4) hive -S 静默模式
    5) hive -i *.sql 执行sql 文件后停留在hive 会话中

## 分区与分桶

### 分区

1) 在hdfs 中形成多级目录

### 分桶 bucketed table

1) 使用场景： 数据抽样
2) CREATE TABLE table_name (id INT,name STRING,age INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY',';
3) load data local inpath './data/bucketed' into table table_name;
4) CREATE TABLE table_bucket (id INT,name STRING,age INT)
   CLUSTERED BY(age) INTO 4 BUCKETS
   ROW FORMAT DELIMITED FIELDS TERMINATED BY',';
5) insert into table table_bucket select id,name,age from table_name;
6) select id,name,age from table_bucket tablesample(bucket 2 outof 4 on age); 即 x =2 y = 4；
   从第2个桶开始抽取数据， 取数据量为： 桶的总数/4 注意：一般y 设置为桶总数的倍数或因子。

## 权限管理

### 角色
1) admin 和 public
2) 查看角色 show current roles;
3) 创建角色 create role test;
4) 查看用户角色关系 show role grant role test;
5) 绑定角色和用户关系 grant admin to role test;
6) 如果想要test 也有创建角色功能需要写成 grant admin to role test with admin option;
7) 回收权限 revoke 


## HIVE 调优
1) 把 hiveSQL 当作 mapReduce 去进行优化
2) Explain 显示执行计划： explain extended select count(1) from table_name
3) 抓取策略  hive> set hive.fetch.task.conversion = more; (more: 判断是否执行mapreduce 任务； none：全部为mapreduce执行)
4) 并行度 hive> set hive.exec.parallel=true;
5) 严格模式 hive>set hive.mapred.mode=strict;
6) 排序方式
   1) order by 尽量不要用,容易导致数据倾斜，全量数据排序，必须和limit 搭配使用。 
   2) sort by 保证单个reduce 中有序
   3) distribute by 保证 reduce 间有序， 所以 distribute by + sort by 即全有序
   4) cluster by  相当于 distribute by + sort by 但是不能通过 asc 或者desc 指定顺序
   5) 所以一般使用 distribute by col sort by asc/desc 的方式
7) join
   1) 大表和小表的join 可以使用 map join 的方式 把小表放入到内存中，再执行sql
   ```SELECT /*+ MAPJOIN(smallTable) */ smallTable.key, bigTable.value FROM smallTable JOIN bigTable ON smallTable.key=bigTable.key```
    2) 大表和大表： 空key过滤 或 空key转换
8) map side 预聚合 (就是map 端的 combine)， 可以通过 hive> set hive.map.aggr=true; 开启map端聚合
    1) hive.groupby.mapaggr.checkinterval; map端group by执行聚合时处理的多少行数据（默认：100000）
   2) hive.map.aggr.hash.min.reduction; 进行聚合的最小比例（预先对100000条数据做聚合，若聚合之后的数据量/100000的值大于该配置0.5，则不会聚合）
   3) hive.map.aggr.hash.percentmemory; map端聚合使用的内存的最大值
   4) hive.groupby.skewindata; 是否对GroupBy产生的数据倾斜做优化，默认为false
9) 合并小文件
   1) 是否合并map输出文件：hive.merge.mapfiles=true; 默认开启
   2) 是否合并reduce输出文件：hive.merge.mapredfiles=true; 默认开启
   3) 合并文件的大小：hive.merge.size.per.task=256*1000*1000
10) count(distinct) 一般使用 group by 先去重 然后再聚合的方式
11) JVM 重用
    1) 小文件过多
    2) task个数过多 可以设置 set mapred.job.reuse.jvm.num.tasks=n 来控制task 数量，防止闲置浪费的情况
12) 压缩一般使用 snappy 进行压缩
13) 文件存储格式
    1) 常用文件存储格式：textFile、sequenceFile、ORC、parquet
    2) 行存储：textFile、sequenceFile
    3) 列存储：ORC、parquet

### hive 将 sql 查询转化成为 mapreduce 作业的过程
1) 输入sql 
2) parser：将SQL 妆化成为抽象语法树 
3) Semantic Analyzer：将抽象语法树转化成查询块 
4) Logical Plan Generator：将查询模块转化成逻辑查询计划 
5) Logical optimize： 重写逻辑查询计划 
6) Physical Plan Generator：将逻辑查询计划转成物理计划
7) Physical Optimizer： 选择最佳的优化查询策略
8) 输出结果

## SQL解析工具
### calcite
### ANTLR