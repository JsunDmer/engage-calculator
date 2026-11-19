# kotlinx.serialization 通过反射读取 @Serializable 描述符
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.engage.core.** { *; }
-keep,includedescriptorclasses class com.engage.core.**$$serializer { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
