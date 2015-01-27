#
# This ProGuard configuration file illustrates how to process Android
# applications.
# Usage:
#     java -jar proguard.jar @android.pro
#
# If you're using the Android SDK (version 2.3 or higher), the android tool
# already creates a file like this in your project, called proguard.cfg.
# It should contain the settings of this file, minus the input and output paths
# (-injars, -outjars, -libraryjars, -printmapping, and -printseeds).
# The generated Ant build file automatically sets these paths.

# Specify the input jars, output jars, and library jars.
# Note that ProGuard works with Java bytecode (.class),
# before the dex compiler converts it into Dalvik code (.dex).

#-injars  bin/classes
#-injars  libs
#-outjars bin/classes-processed.jar

#-libraryjars /usr/local/android-sdk/platforms/android-9/android.jar
#-libraryjars /usr/local/android-sdk/add-ons/google_apis-7_r01/libs/maps.jar
# ...

# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on.

#-printmapping bin/classes-processed.map
#-printmapping build/intermediates/classes-proguard/release/classes-processed.map

# You can print out the seeds that are matching the keep options below.

#-printseeds bin/classes-processed.seeds

# Preverification is irrelevant for the dex compiler and the Dalvik VM.

-dontpreverify

# Reduce the size of the output some more.

-repackageclasses ''
-allowaccessmodification

# Switch off some optimizations that trip older versions of the Dalvik VM.

-optimizations !code/simplification/arithmetic

# Keep a fixed source file attribute and all line number tables to get line
# numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# RemoteViews might need annotations.

-keepattributes *Annotation*

# Preserve all fundamental application classes.

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Preserve all View implementations, their special context constructors, and
# their setters.

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Preserve all classes that have special context constructors, and the
# constructors themselves.

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Preserve all classes that have special context constructors, and the
# constructors themselves.

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve the special fields of all Parcelable implementations.

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.

-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the required interface from the License Verification Library
# (but don't nag the developer if the library is not used at all).

-keep public interface com.android.vending.licensing.ILicensingService

-dontnote com.android.vending.licensing.ILicensingService

# The Android Compatibility library references some classes that may not be
# present in all versions of the API, but we know that's ok.

-dontwarn android.support.**

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your application doesn't use serialization.
# If your code contains serializable classes that have to be backward 
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Your application may contain more items that need to be preserved; 
# typically classes that are dynamically created using Class.forName:

# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface

# SLF4J & logback
-keep class org.slf4j.** { *; }
-keep class ch.qos.** { *; }
-dontwarn ch.qos.logback.core.net.*

# ignore lambda warnings
-dontwarn java.lang.invoke**

# Configuration for Guava
# Note, requires inclusion of jsr305.jar & javax.inject.jar
# see http://www.marvinlabs.com/2013/01/22/android-proguard-and-guavas-event-bus-component/
# or
# see https://code.google.com/p/guava-libraries/wiki/UsingProGuardWithGuava
-keep class javax.** { *; }
-libraryjars libs/jsr305-2.0.3.jar(!META-INF/MANIFEST.MF)
#-libraryjars libs/javax.inject-1.jar(!META-INF/MANIFEST.MF)
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-keep,allowoptimization class com.google.inject.** { *; }
-keep,allowoptimization class javax.inject.** { *; }
-keep,allowoptimization class javax.annotation.** { *; }
-keep,allowoptimization class com.google.inject.Binder
-keepclasseswithmembers public class * {
public static void main(java.lang.String[]);
}
-keepclassmembers,allowoptimization class com.google.common.* {
void finalizeReferent();
void startFinalizer(java.lang.Class,java.lang.Object);
}
-keepclassmembers class * {
@com.google.common.eventbus.Subscribe *;
}

# Android Annotations
# Ignore spring framework reference (not used)
-dontwarn org.springframework.**

# Joda time
-dontwarn org.joda.convert.**

# Dagger 2
-keep class com.sweetsoft.ias.** {
    @javax.inject.* *;
}

-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class javax.inject.** { *; }
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection
-keep class dagger.** { *; }

# Otto
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# DroidWorker
-keepclassmembers,allowobfuscation class * {
    @com.be.android.library.worker.annotations.* *;
}

# cupboard (keep models' field names)
-keep class com.simbirsoft.timemeter.db.model.** {*;}