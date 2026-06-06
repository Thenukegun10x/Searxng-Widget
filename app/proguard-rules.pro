# Keep Retrofit service interfaces
-keep,allowobfuscation interface com.searxng.widget.data.api.SearxngApi

# Keep Gson serialized classes
-keepclassmembers class com.searxng.widget.data.model.** { *; }
