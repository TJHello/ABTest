# ABTest

**一款简单易用的ABTest开源库**

原理：通过给事件标签，添加A,B后缀来统计ABTest结果。

1、兼容友盟、Firebase，可同时的打点。

2、支持在线配置ABTest。

3、支持统计留存。

4、支持统计使用时长。

## 使用步骤

### Step1 接入自动集成插件到build.gradle(project)

```
buildscript {
     repositories {
        ...
         maven { url 'https://raw.githubusercontent.com/TJHello/publicLib/master'}
     }
}

allprojects {
     repositories {
        ...
        maven { url 'https://raw.githubusercontent.com/TJHello/publicLib/master'}
     }
}

```

### Step2 配置build.gradle(app)


```
dependencies {
    //需要另外接入友盟或者Firebase打点
    implementation 'com.TJHello:ABTest:1.0.17'
}
```

### Step3 配置Application


```kotlin
override fun onCreate() {
    super.onCreate()
    val isNew = true//自己判断当前用户是否是新用户，如果一开始就接入了ABTest，可以写成true。
    ABTest.isDebug = true
    //本地测试
    ABTest.init(this, true)
        .addTest(ABConfig().apply {//添加一项ABTest
            this.name = "ABTestDemo"//测试名称
            this.ver = 0//AB测试版本，默认0，如果同一个测试不同版本，则修改这个数值
            this.abVer = 10001//当前测试对应的应用版本号
            this.data = mutableListOf("1","2")//支持任意多维度的数据
            this.listenEvent = mutableListOf("ABTestDemoEvent")//需要纳入AB的事件,留空则监听所有事件
            this.mergeEvent = false//合并事件(测试中)
            this.mergeTag = true//合并标签eg:data_A=value1,data_B=value2  ->  data = value1_A,data = value2_B
            this.onlyNew = true//仅将新增用户纳入测试结果
        })
    //通过Firebase-RemoteConfig在线配置ABTest（需要另外接入该库）
    ABTest.init(this, true)
    RemoteConfig.init(){
        if(it){
            ABTest.addTestByRemoteConfig()
        }    
    }
    //通过自己的服务器在线配置ABTest
    ABTest.init(this, true)
    ABTest.addTestByJsonConfig(xx)
    ABTest.addTestByInfoConfig()
}

```

### Step4 替换友盟打点

其余友盟初始方法不变，替换友盟onEvent方法

```kotlin
//MobclickAgent.onEvent(context, eventId, map)

ABTest.getInstance().event(eventId,map)

```

### 日志查看 tag : ABTestLog

### 打点效果

**未接入ABTest前->**

data |
:---:|
1 |
2 |
3 |



**接入ABTest后->**

data | data_Test_A | data_Test_B | data_Test_C | data_Test_D |
:---:|:---:|:---:|:---:|:---:|
1 | 1 | 1 | 1 | 1 |
2 | 2 | 2 | 2 | 2 |
3 | 3 | 3 | 3 | 3 |

