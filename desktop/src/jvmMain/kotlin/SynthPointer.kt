object SynthPointer {
  init {
    System.load(System.getProperty("user.dir") + "/desktop/lib/libSynthPointer.so")
//        System.loadLibrary("lib/SynthPointer")
    setup()
    setupScreen(0)
  }

  private external fun setup()
  private external fun setupScreen(screenID: Int)

  external fun penHoverExit()
  external fun penDown()
  external fun penUp()
  external fun penHoverMove(x: Float, y: Float, offsetX: Int, offsetY: Int, buttonPressed: Boolean)
  external fun penContactMove(x: Float, y: Float, offsetX: Int, offsetY: Int, pressure: Int, buttonPressed: Boolean)

}
