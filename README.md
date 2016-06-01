# FastQuery 数据持久层框架
FastQuery 基于Java语言.他的使命是:简化Java操作数据层.<br />
做为一个开发者, **仅仅只需要设计DAO接口即可**,其内部采用ASM动态生成实现,执行快. 因此,代码简洁而优雅.从而,大幅度提升开发效率.<br />
遵循非侵入式原则设计,松耦合,很容易与其它容器或框架集成.<br />
提供了一组简单的`Annotation`.消费者只用关心注解的含义.这就使得框架的核心便于重构,便于持续良性发展.<br />

## FastQuery 主要特性如下:
1. 设计优雅,配置简单,极易上手.
2. 采用ASM动态生成字节码,因此支持编译前预处理,可最大限度减少运行期的错误.显著提升程序的强壮性.
3. 支持安全查询,防止SQL注入.
4. 支持与主流数据库连接池框架集成,如集成c3p0,dbcp等等
5. 支持 `@Query` 查询,使用 `@Condition`,可实现动态 `where` 条件查询.
6. 支持查询结果集以JSON类型返回
7. 支持`AOP`,注入拦截器只需标识几个简单的注解,如: `@Before` , `@After`

## 运行环境要求
jdk1.8+

## 配置文件
### jdbc-config.xml
用来配置支持jdbc. **注意**:如果采用连接池,该配置文件可以不要.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jdbc-config>  
        <!-- 配置第一个数据源 -->
        <named-config name="xk_db">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="url">jdbc:mysql://192.168.1.1:3306/xk?user=xk&amp;password=abc123</property>
        </named-config>

        <!-- 配置第二个数据源 -->
        <named-config name="shtest_db">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  <!-- jdbc 驱动 -->
        <property name="databaseName">dbname</property>                <!-- 数据库的名称 -->
        <property name="user">username</property>                      <!-- 数据库用户名称 -->
        <property name="password">userpasswd</property>                <!-- 数据库用户的密码 --> 
        <property name="portNumber">3306</property>                    <!-- 端口 -->
        <property name="serverName">192.168.1.1</property>             <!-- 数据库主机地址 -->
    </named-config>
</jdbc-config>
```

### c3p0-config.xml
支持c3p0配置,详情配置请参照c3p0官网的说明.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>  
    <!--
    <default-config>  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://...</property>
        <property name="user">root</property>  
        <property name="password">123***</property>  
        <property name="initialPoolSize">10</property>  
        <property name="maxIdleTime">30</property>  
        <property name="maxPoolSize">20</property>  
        <property name="minPoolSize">5</property>  
        <property name="maxStatements">200</property>  
    </default-config> 
    -->  
     
    <named-config name="xk-c3p0">  
        <property name="driverClass">com.mysql.jdbc.Driver</property>  
        <property name="jdbcUrl">jdbc:mysql://192.168.1.1:3306/xk</property>  
        <property name="user">xk</property>  
        <property name="password">abc123</property>  
        <property name="acquireIncrement">50</property>  
        <property name="initialPoolSize">100</property>  
        <property name="minPoolSize">50</property>  
        <property name="maxPoolSize">1000</property>
        <property name="maxStatements">0</property>  
        <property name="maxStatementsPerConnection">5</property>     
    </named-config> 
</c3p0-config>
```

### fastquery.json
配置数据源的作用范围

```js
// @author xixifeng (fastquery@126.com)
// 配置必须遵循标准的json语法.
[
    // config目前支持的可选值有"jdbc","c3p0"
    {
        "config": "c3p0",            // 表示由c3p0负责提供数据源
        "dataSourceName": "xk-c3p0", // 数据源的名称
        "basePackages": [            // 该数据源的作用范围
            "org.fastquery.example.StudentDBService",
            "org.fastquery.example.UserInfoDBService"
            // 在这可以配置多个DB接口,以","号隔开
        ]
    },
    
    /*
     再配置一个数据源作用域
    */
    {
        "config" : "jdbc",             // 表示由jdbc驱动负责提供数据源
        "dataSourceName": "shtest_db", // 数据源的名称
        "basePackages": [              // 该数据源的作用范围
            "org.fastquery.example.DataAcquireDbService"
             // 在这可以配置多个DB接口,以","号隔开
        ]
    }
]
```

## 一个完整的入门例子
- 准备一个实体

```java
 public class Student
 {
      private String no;
      private String name;
      private String sex;
      private Integer age;
      private String dept;
      // getter /setter 省略...
 } 
```

- DAO接口

```java
 public interface StudentDBService extends QueryRepository {
    @Query("select no, name, sex from student")
    JSONArray findAll();
    @Query("select no as no,name,sex,age,dept from student")
    Student[] find();      
 }
```

- 使用DAO接口.

 **注意**:不用去实现StudentDBService接口.

```java
   // get porxy impl
   StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
   // call findAll
   JSONArray jsonArray = studentDBService.findAll();
   // call find
   Student[] students = studentDBService.find(); 
```

## 带条件查询

```java
// sql中的?1 表示当前方法的第一个参数
// sql中的?2 表示当前方法的第二个参数
//       ?N 表示当前方法的第N个参数
	
// 查询返回数组格式
@Query("select no as no,name,sex,age,dept from student s where s.sex=?2 and s.age > ?1")
Student[] find(Integer age,String sex);
 	
// 查询返回JSON格式
@Query("select no, name, sex from student s where s.sex=?1 and s.age > ?2")
JSONArray find(String sex,Integer age);
	
// 查询返回List Map
@Query("select no, name, sex from student s where s.sex=?1 and s.age > ?2")
List<Map<String, Object>> findBy(String sex,Integer age);
```

## 动态条件查询
```java
@Query("select no, name, sex from Student #{#where} order by age desc")
// 增加一些条件
@Condition(l="no",o=Operator.LIKE,r="?1")                   // ?1的值,如果是null, 该行条件将不参与运算
@Condition(c=COperator.AND,l="name",o=Operator.LIKE,r="?2") // 参数 ?2,如果接收到的值为null,该条件不参与运算
// 通过 ignoreNull=false 开启条件值即使是null也参与运算
// 下行?3接收到的值若为null,该条件也参与运算.
@Condition(c=COperator.AND,l="age",o=Operator.GT,r="?3",ignoreNull=false)
@Condition(c=COperator.OR,l="dept",o=Operator.IN,r="(?4,?5,?6)")           // dept in(?4,?5,?6)
@Condition(c=COperator.AND,l="name",o={Operator.NOT,Operator.LIKE},r="?7") // 等效于 name not like ?7
@Condition(c=COperator.OR,l="age",o=Operator.BETWEEN,r="?8 and ?9")        // 等效于 age between ?8 and ?9
Student[] findAllStudent(... args ...);
```

## count

统计查询行数
```java
@Query("select count(no) from student")
long count();
```

## exists

判断是否存在
```java
@Query("select no from student s where s.no=?1")
boolean exists(String no);
```

## 改操作
```java
@Query("update student s set s.age=?3,s.name=?2 where  s.no=?1")
@Modifying
int update(String no,String name,int age); // 返回修改之后所影响的行数

@Modifying
@Query("DELETE FROM `userinfo` WHERE id=?1")
boolean deleteUserinfoById(int id);
	
@Query("update student s set s.age=?2 where  s.no=?1")
@Modifying
int update(String no,int age);

// 以实体bean格式,返回当前保存的数据
@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
@Modifying(table="student",id="no")
// 注意: student的主键是字符串不会自增长,在此处需要用@Id标识
Student addStudent(@Id String no,String name,String sex,int age,String dept);
	
// 以Map格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Map<String, Object> addUserInfo(String name,Integer age);

// 以JSON格式,返回当前保存的数据
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
JSONObject saveUserInfo2(String name,Integer age);

// 返回当前保存的数据的主键信息.
@Modifying(id="id",table="userinfo")
@Query("insert into #{#table} (name,age) values (?1, ?2)")
Primarykey saveUserInfo(String name,Integer age);

```

## @Transactional

```java
// 将三条改操作纳入到一个事务中.
@Transactional
@Modifying
@Query("update `userinfo` set `name`=?1 where id=?3")
@Query("update `userinfo` set `age`=?2 where id=?3")
// 把主键id修改为1,目前主键id=1是存在的.这行会报错.那么前两行所做的操作全部失效.
@Query("update `userinfo` set `id`=1 where `id`=?3")
int updateBatch(String name,Integer age,Integer id);
// 注意: 
// 1).返回值如果是int类型,表示这个事务成功提交后所有改操作所影响的行数总和.
// 2).返回值如果是int[]类型,表示这个事务成功提交后,每个最小修改单元所影响行数的集合.
//    举例说明: 若有个事务T,它里面有3条改操作,分别叫U1,U2,U3. T成功提交后,U1,U2,U3所影响的数据行数分别为N1,N2,N3.
//    则: 返回值为: new int[]{N1,N2,N3}
```

## 处理异常

捕获和处理`Repository`实例在运行期抛出的异常.   
例如: 捕获`UserInfoDBService`中的`updateBatch`在运行期间可能抛出的异常.

```java
// 获取 Repository
UserInfoDBService udb = FQuery.getRepository(UserInfoDBService.class);

try {
	int effect = udb.updateBatch("小不点", 6, 2);
} catch (RepositoryException e) {
	// Handle exceptional condition
	// TODO ... ...
}
```

## @Before拦截器
- 准备一个BeforeFilter

```java
 /**
  * @author xixifeng (fastquery@126.com)
  */
 public class MyBeforeFilter1 extends BeforeFilter<Repository> {

 	@Override
 	public void doFilter(Repository repository, Method method, Object[] args) {
 	
 		// repository: 当前拦截到的实例
 		// method: 当前拦截到的方法
 		// args: 当前传递进来的参数
 		
 		// this.abortWith(returnVal); // 中断拦截器,并指定返回值
		
 	}
 }
```

- 注入Filter

```java
// 可以同时标识多个@Before
@Before(MyBeforeFilter1.class)
@Before(MyBeforeFilter2.class)
@Before(MyBeforeFilter3.class)
public interface StudentDBService extends QueryRepository {
   // some code ... ...
}
```

## @After拦截器
```java
/**
 * @author xixifeng (fastquery@126.com)
 */
public class MyAfterFilter extends AfterFilter<Repository> {

	@Override
	public Object doFilter(Repository repository, Method method, Object[] args, Object returnVal) {
		
		// repository: 当前拦截到的实例
		// method: 当前拦截到的method
		// args: 当前传递进来的参数
		// returnVal 即将返回的值
		
		// 在这里可以中途修改 returnVal
		
		return returnVal;
	}
}
```


```java
// 可以同时标识多个@After
@After(MyAfterFilter.class)
@After(MyAfterFilter2.class)
public interface StudentDBService extends QueryRepository {
	// some code ... ...
}
```

## 控制拦截器的作用域
若: 有一个拦截器叫`A<T>`,那么:这个拦截器的作用范围只能在`T`类或`T`的子类里.<br />
举例:
```java
// 这个拦截器的作用范围在 DataAcquireDbService里或在DataAcquireDbService子类里.
// 换言之: MyBeforeFilter3这个拦截器只能标注在DataAcquireDbService里或标注在DataAcquireDbService的子类里.
public class MyBeforeFilter3 extends BeforeFilter<DataAcquireDbService> { 
     // some code ... ...
}
```

### @SkipFilter
跳过当前接口绑定的所有非默认的Filter(系统默认的Filter不会跳过).<br />
举例:

```java
@SkipFilter
@Query("select no from `course` limit 1")
String findOneCourse();
```

### 注意:
- `@Before`和`@After`不仅可以标注在接口类上,也可以标注在方法上
- 标识在类的上方:表示其拦截的作用范围是整个类的方法
- 标识在方法上:表示其拦截的作用范围是当前方法
- 一个方法的拦截器总和=它的所属类的拦截器+自己的拦截器

## 源码

- http://git.oschina.net/xixifeng.com/fastquery
- https://github.com/xixifeng/fastquery
- https://code.aliyun.com/xixifeng/fastquery

## 联系作者
fastquery#126.com
欢迎批评指正.
