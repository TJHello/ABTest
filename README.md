# ABTest-UM
**一款简单易用的ABTest开源库-支持友盟，Firebase**

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
    //需要接入友盟或者Firebase打点，初始化以及接入需要自行处理。
    implementation 'com.TJHello:ABTest:0.9.22'
}
```

### Step3 配置Application

```kotlin
override fun onCreate() {
    super.onCreate()
    val isNew = true//自己判断当前用户是否是新用户
    ABTest.init(this,isNew,mutableListOf(ABConfig().apply {
            this.dataArray = arrayOf("A","B","C","D")//ABCD方案,打点的时候以这个来区分。
            this.firstVersionCode = 0//第一次接入ABTest的版本号
            this.isOnlyNew = true//是否只测试新增的用户
            this.name = "Test"//测试唯一编号，不能与历史使用过的重复
            this.nowVersionCode = 1//首次安装版本号大于等于这个值才是新用户。
            this.listenEventArray = arrayOf("data")//监听的事件,空的时候监听所有事件
        }))
}

```

### Step4 替换友盟打点

其余友盟初始方法不变，替换友盟onEvent方法

```kotlin
//MobclickAgent.onEvent(context, eventId, map)

ABTest(context).event(eventId,map)

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

data | data_Test_A | data_Test_B | data_Test_C | data_Test_D | data_Test_all |
:---:|:---:|:---:|:---:|:---:|:---:|
1 | 1 | 1 | 1 | 1 | 1 |
2 | 2 | 2 | 2 | 2 | 2 |
3 | 3 | 3 | 3 | 3 | 3 |

