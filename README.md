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
    val isNew = true//自己判断当前用户是否是新用户，如果一开始就接入了ABTest，可以写成true。
    ABTest.isDebug = true
    ABTest.init(this, true)
    ABTest.getInstance()
        .addTest(this,ABConfig().apply {//添加一项ABTest
            this.dataArray = arrayOf("0","1")
            this.firstVersionCode = 1
            this.isOnlyNew = true
            this.name = "NewUI2"
            this.nowVersionCode = 2 })
        .startTimeTack()//开启游戏时长统计功能
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

