#include "helloj_HelloLib.h"
#include <iostream>

JNIEXPORT jstring JNICALL Java_helloj_HelloLib_hi (JNIEnv *env, jobject obj) {
  jstring result = env->NewStringUTF("Hello from JNI!");
  return result;
}
