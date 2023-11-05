#include <org_sqteam_Main.h>
#include <stdio.h>

JNIEXPORT void JNICALL Java_org_sqteam_Main_test
  (JNIEnv *, jobject){
  printf("hello world");
  }