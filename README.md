# ABTest-UM
**一款简单易用的ABTest工具-基于友盟打点**

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
    implementation 'com.TJHello:ABTest:0.9.6'
}
```

### Step3 配置Application

```kotlin
override fun onCreate() {
    super.onCreate()
    ABTest.init(this, mutableListOf(ABConfig().apply {
            this.dataArray = arrayOf("A","B","C","D")//ABCD方案,可以任意文本
            this.firstVersionCode = 0//第一次接入ABTest的版本号
            this.isOnlyNew = true//是否只测试新增的用户
            this.name = "Test"
            this.nowVersionCode = getVersionCode()//当前APP版本号
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

data | data_Test_A | data_Test_B | data_Test_B | data_Test_all |
:---:|:---:|:---:|:---:|:---:|
1 | 1 | 1 | 1 | 1 |
2 | 2 | 2 | 2 | 2 |
3 | 3 | 3 | 3 | 3 |

