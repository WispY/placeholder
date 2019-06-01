 //1
package cross.subbinary //2
 //3
import cross.binary._ //4
import cross.subbinary.macrodef._ //5
 //6
import scala.languageFeature.experimental.macros //7
import scala.reflect.macros.blackbox //8
 //9
/** Generated. Macros that build binary formats for case classes of any size */ //10
object formats { //11
implicit val macros: macros = scala.language.experimental.macros //12
 //13
  def binaryFormat0[A](constructor: () => A): BF[A] = macro format0[A]

  def binaryFormat1[P1, A](constructor: (P1) => A)(implicit p1: BF[P1]): BF[A] = macro format1[A]

  def binaryFormat2[P1, P2, A](constructor: (P1, P2) => A)(implicit p1: BF[P1], p2: BF[P2]): BF[A] = macro format2[A]

  def binaryFormat3[P1, P2, P3, A](constructor: (P1, P2, P3) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3]): BF[A] = macro format3[A]

  def binaryFormat4[P1, P2, P3, P4, A](constructor: (P1, P2, P3, P4) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4]): BF[A] = macro format4[A]

  def binaryFormat5[P1, P2, P3, P4, P5, A](constructor: (P1, P2, P3, P4, P5) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5]): BF[A] = macro format5[A]

  def binaryFormat6[P1, P2, P3, P4, P5, P6, A](constructor: (P1, P2, P3, P4, P5, P6) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6]): BF[A] = macro format6[A]

  def binaryFormat7[P1, P2, P3, P4, P5, P6, P7, A](constructor: (P1, P2, P3, P4, P5, P6, P7) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7]): BF[A] = macro format7[A]

  def binaryFormat8[P1, P2, P3, P4, P5, P6, P7, P8, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8]): BF[A] = macro format8[A]

  def binaryFormat9[P1, P2, P3, P4, P5, P6, P7, P8, P9, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9]): BF[A] = macro format9[A]

  def binaryFormat10[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10]): BF[A] = macro format10[A]

  def binaryFormat11[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11]): BF[A] = macro format11[A]

  def binaryFormat12[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12]): BF[A] = macro format12[A]

  def binaryFormat13[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13]): BF[A] = macro format13[A]

  def binaryFormat14[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14]): BF[A] = macro format14[A]

  def binaryFormat15[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15]): BF[A] = macro format15[A]

  def binaryFormat16[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16]): BF[A] = macro format16[A]

  def binaryFormat17[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17]): BF[A] = macro format17[A]

  def binaryFormat18[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17], p18: BF[P18]): BF[A] = macro format18[A]

  def binaryFormat19[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17], p18: BF[P18], p19: BF[P19]): BF[A] = macro format19[A]

  def binaryFormat20[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17], p18: BF[P18], p19: BF[P19], p20: BF[P20]): BF[A] = macro format20[A]

  def binaryFormat21[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17], p18: BF[P18], p19: BF[P19], p20: BF[P20], p21: BF[P21]): BF[A] = macro format21[A]

  def binaryFormat22[P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22, A](constructor: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22) => A)(implicit p1: BF[P1], p2: BF[P2], p3: BF[P3], p4: BF[P4], p5: BF[P5], p6: BF[P6], p7: BF[P7], p8: BF[P8], p9: BF[P9], p10: BF[P10], p11: BF[P11], p12: BF[P12], p13: BF[P13], p14: BF[P14], p15: BF[P15], p16: BF[P16], p17: BF[P17], p18: BF[P18], p19: BF[P19], p20: BF[P20], p21: BF[P21], p22: BF[P22]): BF[A] = macro format22[A] //22
 //23
  def format0[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, Nil)

  def format1[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: Nil)

  def format2[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: Nil)

  def format3[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: Nil)

  def format4[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: Nil)

  def format5[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: Nil)

  def format6[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: Nil)

  def format7[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: Nil)

  def format8[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: Nil)

  def format9[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: Nil)

  def format10[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: Nil)

  def format11[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: Nil)

  def format12[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: Nil)

  def format13[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: Nil)

  def format14[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: Nil)

  def format15[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: Nil)

  def format16[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: Nil)

  def format17[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: Nil)

  def format18[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: Nil)

  def format19[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: Nil)

  def format20[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: Nil)

  def format21[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree, p21: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: Nil)

  def format22[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree, p3: c.Tree, p4: c.Tree, p5: c.Tree, p6: c.Tree, p7: c.Tree, p8: c.Tree, p9: c.Tree, p10: c.Tree, p11: c.Tree, p12: c.Tree, p13: c.Tree, p14: c.Tree, p15: c.Tree, p16: c.Tree, p17: c.Tree, p18: c.Tree, p19: c.Tree, p20: c.Tree, p21: c.Tree, p22: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: p22 :: Nil) //31
} //32
