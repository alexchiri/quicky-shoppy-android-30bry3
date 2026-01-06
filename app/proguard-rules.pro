# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

# Keep Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class com.kroslabs.quickyshoppy.data.remote.** { *; }
-keep class com.kroslabs.quickyshoppy.domain.model.** { *; }
