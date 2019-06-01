 //1
package cross.subconfig //2
 //3
import cross.config._ //4
import cross.subconfig.macrodef._ //5
 //6
import scala.languageFeature.experimental.macros //7
import scala.reflect.macros.blackbox //8
 //9
/** Generated. Macros that build config formats for case classes of any size */ //10
object formats { //11
implicit val macros: macros = scala.language.experimental.macros //12
 //13
  def configFormat0[A](constructor: () => A): CF[A] = macro format0[A]

  def configFormat1[P1, A](constructor: (P1) => A)(implicit p1: CF[P1]): CF[A] = macro format1[A]

  def configFormat2[P1, P2, A](constructor: (P1, P2) => A)(implicit p1: CF[P1], p2: CF[P2]): CF[A] = macro format2[A]

  def configFormat3[P1, P2, P3, A](constructor: (P1, P2, P3) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3]): CF[A] = macro format3[A]

  def configFormat4[P1, P2, P3, P4, A](constructor: (P1, P2, P3, P4) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4]): CF[A] = macro format4[A]

  def configFormat5[P1, P2, P3, P4, P5, A](constructor: (P1, P2, P3, P4, P5) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5]): CF[A] = macro format5[A]

  def configFormat6[P1, P2, P3, P4, P5, P6, A](constructor: (P1, P2, P3, P4, P5, P6) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6]): CF[A] = macro format6[A]

  def configFormat7[P1, P2, P3, P4, P5, P6, P7, A](constructor: (P1, P2, P3, P4, P5, P6, P7) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7]): CF[A] = macro format7[A]

  def configFormat8[P1, P2, P3, P4, P5, P6, P7, P8, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8]): CF[A] = macro format8[A]

  def configFormat9[P1, P2, P3, P4, P5, P6, P7, P8, P9, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9]): CF[A] = macro format9[A]

  def configFormat10[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10]): CF[A] = macro format10[A]

  def configFormat11[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11]): CF[A] = macro format11[A]

  def configFormat12[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12]): CF[A] = macro format12[A]

  def configFormat13[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13]): CF[A] = macro format13[A]

  def configFormat14[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14]): CF[A] = macro format14[A]

  def configFormat15[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15]): CF[A] = macro format15[A]

  def configFormat16[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16]): CF[A] = macro format16[A]

  def configFormat17[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17]): CF[A] = macro format17[A]

  def configFormat18[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17], p18: CF[P18]): CF[A] = macro format18[A]

  def configFormat19[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17], p18: CF[P18], p19: CF[P19]): CF[A] = macro format19[A]

  def configFormat20[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17], p18: CF[P18], p19: CF[P19], p20: CF[P20]): CF[A] = macro format20[A]

  def configFormat21[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17], p18: CF[P18], p19: CF[P19], p20: CF[P20], p21: CF[P21]): CF[A] = macro format21[A]

  def configFormat22[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22) => A)(implicit p1: CF[P1], p2: CF[P2], p3: CF[P3], p4: CF[P4], p5: CF[P5], p6: CF[P6], p7: CF[P7], p8: CF[P8], p9: CF[P9], p10: CF[P10], p11: CF[P11], p12: CF[P12], p13: CF[P13], p14: CF[P14], p15: CF[P15], p16: CF[P16], p17: CF[P17], p18: CF[P18], p19: CF[P19], p20: CF[P20], p21: CF[P21], p22: CF[P22]): CF[A] = macro format22[A] //22
 //23
  def format0[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, Nil)

  def format1[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: Nil)

  def format2[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: Nil)

  def format3[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: Nil)

  def format4[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: Nil)

  def format5[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: Nil)

  def format6[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: Nil)

  def format7[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: Nil)

  def format8[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: Nil)

  def format9[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: Nil)

  def format10[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: Nil)

  def format11[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: Nil)

  def format12[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: Nil)

  def format13[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: Nil)

  def format14[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: Nil)

  def format15[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: Nil)

  def format16[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: Nil)

  def format17[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: Nil)

  def format18[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: Nil)

  def format19[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: Nil)

  def format20[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: Nil)

  def format21[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree, p21: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: Nil)

  def format22[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree, p21: c.Tree, p22: c.Tree): c.Expr[CF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: p22 :: Nil) //31
} //32
