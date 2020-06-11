# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.squareup.okhttp.**

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**


-keep class org.jetbrains.kotlin.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-keepclassmembers class ** {
  @org.jetbrains.annotations.ReadOnly public *;
}
-keepattributes *Annotation*

-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }

# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn okio.**

#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes *Annotation*,Signature, Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
#endRetrofit

-keepclassmembers public class com.cypressworks.kotlinreflectionproguard.** {
    public *;
}
-ignorewarnings
