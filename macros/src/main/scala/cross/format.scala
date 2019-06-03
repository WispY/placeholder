package cross

import scala.languageFeature.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.control.NonFatal

object format {

  /** Reads and writes the object A as format B */
  trait AbstractFormat[A, B] {
    /** Reads the object A from formatted form B */
    def read(path: Path, formatted: B): (A, B)

    /** Writes the object A info formatted form B */
    def append(path: Path, a: A, formatted: B): B

    /** Tells whether or now the format supports the given value */
    def isDefinedFor(a: Any): Boolean = false

    /** Creates a new format based on this one and type mapping */
    def map[C](constructor: A => C, destructor: C => A): AbstractFormat[C, B] = {
      val delegate = this
      new AbstractFormat[C, B] {
        override def read(path: Path, formatted: B): (C, B) = {
          val (c, tail) = delegate.read(path, formatted)
          val mapped = try {
            constructor.apply(c)
          } catch {
            case NonFatal(up) => throw new IllegalArgumentException(s"failed to read: ${path.stringify}", up)
          }
          mapped -> tail
        }

        override def append(path: Path, c: C, formatted: B): B = {
          delegate.append(path, destructor.apply(c), formatted)
        }
      }
    }
  }

  type AF[A, B] = AbstractFormat[A, B]

  /** Defines the target type of formatting */
  trait FormatType[A] {
    def unit: A
  }

  /** Defines a segment of the path to the value */
  sealed trait PathSegment {
    /** Returns the string representation of this segment */
    def stringify: String
  }

  type Path = List[PathSegment]

  /** Defines the array element path */
  case class ArrayPathSegment(index: Int) extends PathSegment {
    override def stringify: String = index.toString
  }

  /** Defines the object field path */
  case class FieldPathSegment(name: String) extends PathSegment {
    override def stringify: String = name
  }

  implicit class PathListOps(val path: Path) extends AnyVal {
    /** Returns path in dot notation */
    def stringify: String = path.map(s => s.stringify).mkString(".")
  }

  implicit class AnyFormatOps[A](val a: A) extends AnyVal {
    /** Formats the object into B */
    def format[B](path: Path = Nil)(implicit fmt: AbstractFormat[A, B], tpe: FormatType[B]): B = fmt.append(path, a, tpe.unit)
  }

  implicit val macros: macros = scala.language.experimental.macros

  def formatX[A: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree, parts: List[c.Tree]): c.Expr[AF[A, B]] = {
    import c.universe._
    val tpe = weakTypeOf[A]
    val fmt = weakTypeOf[B]
    val fields = tpe.decls.collectFirst { case primary: MethodSymbol if primary.isPrimaryConstructor => primary }.get.paramLists.head
    val read = {
      val (lines, names) = parts.zip(fields).zipWithIndex.map { case ((part, field), i) =>
        val value = TermName(s"value$i")
        val currentFormat = TermName(s"format$i")
        val nextFormat = TermName(s"format${i + 1}")
        val fieldName = field.name.decodedName.toString
        val line = (tail: c.Tree) => q"val ($value, $nextFormat) = $part.read(path :+ FieldPathSegment($fieldName), $currentFormat); $tail"
        line -> q"$value"
      }.unzip
      val lastBytes = TermName(s"format${parts.size}")
      val lastLine = (tail: c.Tree) => q"new $tpe(..$names) -> $lastBytes"
      mkblock(c)(lines :+ lastLine)
    }
    val append = {
      val lines = parts.zip(fields).zipWithIndex.map { case ((part, field), i) =>
        val currentFormat = TermName(s"format$i")
        val nextFormat = TermName(s"format${i + 1}")
        val accessor = TermName(field.fullName.split('.').last)
        val fieldName = field.name.decodedName.toString
        (tail: c.Tree) => q"val $nextFormat = $part.append(path :+ FieldPathSegment($fieldName), a.$accessor, $currentFormat); $tail"
      }
      val lastBytes = TermName(s"format${parts.size}")
      val lastLine = (tail: c.Tree) => q"$lastBytes"
      mkblock(c)(lines :+ lastLine)
    }
    c.Expr[AF[A, B]] {
      q"""
      new cross.format.AbstractFormat[$tpe, $fmt] {
        override def read(path: Path, format0: $fmt): ($tpe, $fmt) = { $read }
        override def append(path: Path, a: $tpe, format0: $fmt): $fmt = { $append }
        override def isDefinedFor(a: Any): Boolean = a match {
          case m: $tpe => true
          case other => false
        }
      }
      """
    }
  }

  def mkblock(c: blackbox.Context)(lines: List[c.Tree => c.Tree]): c.Tree = {
    import c.universe._
    lines.foldRight(q"") { case (head, tail) =>
      q"""
      ${head.apply(tail)}
      """
    }
  }

  // @formatter:off
  // GENERATED CODE
  def format0[A,B](constructor: () => A): AF[A,B] = macro macroFormat0[A,B]
  def format1[P1,A,B](constructor: (P1) => A)(implicit p1: AF[P1,B]): AF[A,B] = macro macroFormat1[A,B]
  def format2[P1,P2,A,B](constructor: (P1,P2) => A)(implicit p1: AF[P1,B],p2: AF[P2,B]): AF[A,B] = macro macroFormat2[A,B]
  def format3[P1,P2,P3,A,B](constructor: (P1,P2,P3) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B]): AF[A,B] = macro macroFormat3[A,B]
  def format4[P1,P2,P3,P4,A,B](constructor: (P1,P2,P3,P4) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B]): AF[A,B] = macro macroFormat4[A,B]
  def format5[P1,P2,P3,P4,P5,A,B](constructor: (P1,P2,P3,P4,P5) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B]): AF[A,B] = macro macroFormat5[A,B]
  def format6[P1,P2,P3,P4,P5,P6,A,B](constructor: (P1,P2,P3,P4,P5,P6) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B]): AF[A,B] = macro macroFormat6[A,B]
  def format7[P1,P2,P3,P4,P5,P6,P7,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B]): AF[A,B] = macro macroFormat7[A,B]
  def format8[P1,P2,P3,P4,P5,P6,P7,P8,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B]): AF[A,B] = macro macroFormat8[A,B]
  def format9[P1,P2,P3,P4,P5,P6,P7,P8,P9,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B]): AF[A,B] = macro macroFormat9[A,B]
  def format10[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B]): AF[A,B] = macro macroFormat10[A,B]
  def format11[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B]): AF[A,B] = macro macroFormat11[A,B]
  def format12[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B]): AF[A,B] = macro macroFormat12[A,B]
  def format13[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B]): AF[A,B] = macro macroFormat13[A,B]
  def format14[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B]): AF[A,B] = macro macroFormat14[A,B]
  def format15[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B]): AF[A,B] = macro macroFormat15[A,B]
  def format16[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B]): AF[A,B] = macro macroFormat16[A,B]
  def format17[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B]): AF[A,B] = macro macroFormat17[A,B]
  def format18[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B],p18: AF[P18,B]): AF[A,B] = macro macroFormat18[A,B]
  def format19[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B],p18: AF[P18,B],p19: AF[P19,B]): AF[A,B] = macro macroFormat19[A,B]
  def format20[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B],p18: AF[P18,B],p19: AF[P19,B],p20: AF[P20,B]): AF[A,B] = macro macroFormat20[A,B]
  def format21[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20,P21,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20,P21) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B],p18: AF[P18,B],p19: AF[P19,B],p20: AF[P20,B],p21: AF[P21,B]): AF[A,B] = macro macroFormat21[A,B]
  def format22[P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20,P21,P22,A,B](constructor: (P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,P16,P17,P18,P19,P20,P21,P22) => A)(implicit p1: AF[P1,B],p2: AF[P2,B],p3: AF[P3,B],p4: AF[P4,B],p5: AF[P5,B],p6: AF[P6,B],p7: AF[P7,B],p8: AF[P8,B],p9: AF[P9,B],p10: AF[P10,B],p11: AF[P11,B],p12: AF[P12,B],p13: AF[P13,B],p14: AF[P14,B],p15: AF[P15,B],p16: AF[P16,B],p17: AF[P17,B],p18: AF[P18,B],p19: AF[P19,B],p20: AF[P20,B],p21: AF[P21,B],p22: AF[P22,B]): AF[A,B] = macro macroFormat22[A,B]
  def macroFormat0[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,Nil)
  def macroFormat1[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: Nil)
  def macroFormat2[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: Nil)
  def macroFormat3[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: Nil)
  def macroFormat4[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: Nil)
  def macroFormat5[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: Nil)
  def macroFormat6[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: Nil)
  def macroFormat7[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: Nil)
  def macroFormat8[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: Nil)
  def macroFormat9[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: Nil)
  def macroFormat10[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: Nil)
  def macroFormat11[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: Nil)
  def macroFormat12[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: Nil)
  def macroFormat13[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: Nil)
  def macroFormat14[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: Nil)
  def macroFormat15[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: Nil)
  def macroFormat16[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: Nil)
  def macroFormat17[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: Nil)
  def macroFormat18[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree,p18: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: Nil)
  def macroFormat19[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree,p18: c.Tree,p19: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: Nil)
  def macroFormat20[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree,p18: c.Tree,p19: c.Tree,p20: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: Nil)
  def macroFormat21[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree,p18: c.Tree,p19: c.Tree,p20: c.Tree,p21: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: Nil)
  def macroFormat22[A: c.WeakTypeTag,B: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree,p2: c.Tree,p3: c.Tree,p4: c.Tree,p5: c.Tree,p6: c.Tree,p7: c.Tree,p8: c.Tree,p9: c.Tree,p10: c.Tree,p11: c.Tree,p12: c.Tree,p13: c.Tree,p14: c.Tree,p15: c.Tree,p16: c.Tree,p17: c.Tree,p18: c.Tree,p19: c.Tree,p20: c.Tree,p21: c.Tree,p22: c.Tree): c.Expr[AF[A,B]] = formatX(c)(constructor,p1 :: p2 :: p3 :: p4 :: p5 :: p6 :: p7 :: p8 :: p9 :: p10 :: p11 :: p12 :: p13 :: p14 :: p15 :: p16 :: p17 :: p18 :: p19 :: p20 :: p21 :: p22 :: Nil)
  // GENERATED CODE
  // @formatter:on 

}