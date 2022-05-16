#include <jni.h>
#ifndef _Included_NativeSample
#define _Included_NativeSample
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT void JNICALL Java_NativeSample_setup(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_NativeSample_penHoverExit(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_NativeSample_penDown(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_NativeSample_penUp(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_NativeSample_penHoverMove(JNIEnv *, jobject, jfloat, jfloat, jint, jint jboolean);
#ifdef __cplusplus
}
#endif
#endif
void handleError();