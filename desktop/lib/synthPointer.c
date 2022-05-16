#define _WIN32_WINNT_WIN10
#define NTDDI_VERSION NTDDI_WIN10_RS5
#include <stdio.h>
#include <windows.h>
//#include <atlwin.h>
#include "SynthPointer.h"

typedef enum {
  PEN_STATE_MASK = (POINTER_FLAG_INRANGE | POINTER_FLAG_INCONTACT | POINTER_FLAG_DOWN | POINTER_FLAG_UP | POINTER_FLAG_UPDATE),

  PEN_HOVER = (POINTER_FLAG_INRANGE | POINTER_FLAG_UPDATE),
  PEN_DOWN = (POINTER_FLAG_INRANGE | POINTER_FLAG_INCONTACT | POINTER_FLAG_DOWN),
  PEN_CONTACT = (POINTER_FLAG_INRANGE | POINTER_FLAG_INCONTACT | POINTER_FLAG_UPDATE),
  PEN_UP = (POINTER_FLAG_INRANGE | POINTER_FLAG_UP),
  PEN_ENDHOVER = (POINTER_FLAG_UPDATE),
} PEN_STATES;

POINTER_TYPE_INFO currPointerInf = {0};
POINTER_TYPE_INFO lastPointerInf = {0};
HSYNTHETICPOINTERDEVICE SyntheticPointer;

int screenWidth = 0, screenHeight = 0;

void getPointer(POINTER_TYPE_INFO *pInfo, PEN_STATES state, float px, float py, int offsetX, int offsetY) {
  POINTER_TYPE_INFO zInfo = {0};
  POINTER_PEN_INFO zPen = {0};
  POINTER_INFO zPinfo = {0};
  POINT p = { (px * screenWidth) + offsetX,  (py * screenHeight) + offsetY};

  zPinfo.pointerType = PT_PEN;
  //   zPinfo.pointerFlags = ContextualizeFlags(&currPointerInf.penInfo, &lastPointerInf.penInfo);
  zPinfo.pointerFlags = state;
  zPinfo.ptPixelLocation = p;
  if (px < 0 && py < 0)
    zPinfo.ptPixelLocation = lastPointerInf.penInfo.pointerInfo.ptPixelLocation;

  zPen.pointerInfo = zPinfo;

  zInfo.type = PT_PEN;
  zInfo.penInfo = zPen;

  *pInfo = zInfo;
}

void injectPointer() {
  if (!InjectSyntheticPointerInput(SyntheticPointer, &currPointerInf, 1)) {
    handleError();
  }
  lastPointerInf = currPointerInf;
}

JNIEXPORT void JNICALL Java_SynthPointer_setup(JNIEnv *env, jobject obj) {
  SyntheticPointer = CreateSyntheticPointerDevice(PT_PEN, 1, POINTER_FEEDBACK_DEFAULT);
}

JNIEXPORT void JNICALL Java_SynthPointer_setupScreen(JNIEnv *env, jobject obj, jint screenID) {
  screenWidth = GetSystemMetrics(SM_CXFULLSCREEN);
  screenHeight = GetSystemMetrics(SM_CYSCREEN);
  return;
}

JNIEXPORT void JNICALL Java_SynthPointer_penHoverExit(JNIEnv *env, jobject obj) {
  getPointer(&currPointerInf, PEN_ENDHOVER, -1, -1, 0, 0);
  injectPointer();
  return;
}

JNIEXPORT void JNICALL Java_SynthPointer_penDown(JNIEnv *env, jobject obj) {
  getPointer(&currPointerInf, PEN_DOWN, -1, -1, 0, 0);
  injectPointer();
  return;
}

JNIEXPORT void JNICALL Java_SynthPointer_penUp(JNIEnv *env, jobject obj) {
  getPointer(&currPointerInf, PEN_UP, -1, -1, 0, 0);
  injectPointer();
  return;
}

JNIEXPORT void JNICALL Java_SynthPointer_penHoverMove(JNIEnv *env, jobject obj, jfloat x, jfloat y, jint offsetX, jint offsetY, jboolean buttonPressed) {
   getPointer(&currPointerInf, PEN_HOVER, x, y, offsetX, offsetY);
   currPointerInf.penInfo.penFlags |= (buttonPressed) ? PEN_FLAG_BARREL : 0;
   injectPointer();
   return;
}

JNIEXPORT void JNICALL Java_SynthPointer_penContactMove(JNIEnv *env, jobject obj, jfloat x, jfloat y, jint offsetX, jint offsetY, jint pressure, jboolean buttonPressed) {
  getPointer(&currPointerInf, PEN_CONTACT, x, y, offsetX, offsetY);
  currPointerInf.penInfo.penFlags |= (buttonPressed) ? PEN_FLAG_BARREL : 0;
  currPointerInf.penInfo.penMask = PEN_MASK_PRESSURE;
  currPointerInf.penInfo.pressure = pressure;
  injectPointer();
}

void handleError() { //TODO: how to handle error in a JNI way?
  LPVOID lpMsgBuf;
  DWORD dw = GetLastError();
  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                (LPTSTR)&lpMsgBuf, 0, NULL);
  printf("Error %d: %s\n", GetLastError(), (char *)lpMsgBuf);
}