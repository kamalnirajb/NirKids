# Project-level ProGuard rules for NirKids
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# Keep TraceValidator
-keep class com.nirkids.tracevalidator.** { *; }
-keep class com.nirkids.app.validator.** { *; }

# Keep data classes
-keep class com.nirkids.app.data.model.** { *; }
-keep class com.nirkids.app.domain.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class com.google.inject.** { *; }
