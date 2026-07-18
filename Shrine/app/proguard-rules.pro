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

# ---------------------------------------------------------------------------
# plan_9 Phase C — R8 keep rules
# Most libraries (Hilt, Room, Coil, Compose, kotlinx-serialization runtime) ship their own
# consumer R8 rules. The app-specific surface R8 cannot infer is the type-safe Navigation-Compose
# graph: route destinations are @Serializable and resolved reflectively via their generated
# KSerializers, so keep them and their serializers intact.
# ---------------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,InnerClasses,Signature,EnclosingMethod

-keep class com.skystone1000.shrine.ui.navigation.** { *; }
-keepclassmembers class com.skystone1000.shrine.ui.navigation.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.skystone1000.shrine.ui.navigation.**$$serializer { *; }

# Keep @Serializable types' generated serializers across modules.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
