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

# used to detect a country flag by its name:
-keep class com.blongho.country_data.World {*;}
-keep interface com.blongho.country_data.World
# used to decect informaiton about a country by its name (continent, area, population)
-keep class com.blongho.country_data.Country {*;}
-keep interface com.blongho.country_data.Country

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keeppackagenames com.blongho.country_data
-keepclassmembers class com.blongho.country_data.* {
   public *;
}
-keep class com.blongho.country_data.R$*{
    *;
 }