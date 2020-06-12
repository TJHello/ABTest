#ABTest
-keep class com.tjhello.ab.test.**{*;}

-keep class com.umeng.** { *; }
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.firebase.analytics.**{*;}