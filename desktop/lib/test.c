#define _WIN32_WINNT_WIN10
#define NTDDI_WIN10_RS5
#include <stdio.h>
#include <windows.h>

int main() {
	  HSYNTHETICPOINTERDEVICE SyntheticPointer = CreateSyntheticPointerDevice(PT_PEN, 1, POINTER_FEEDBACK_DEFAULT);
	    return 0;
}
