package sc.core.sc

trait Whistles[State] {
  private type History = List[State]

  trait Whistle {
    def blow(h: History): Boolean
    def |(w: Whistle) = (this, w) match {
      case (AnyWhistle(ws1), AnyWhistle(ws2)) => AnyWhistle(ws1 ++ ws2)
      case (AnyWhistle(ws1), w2) => AnyWhistle(ws1 :+ w2)
      case (w1, AnyWhistle(ws2)) => AnyWhistle(w1::ws2)
      case (w1, w2) => AnyWhistle(List(w1, w2))
    }
    def &(w: Whistle) = (this, w) match {
      case (AllWhistle(ws1), AllWhistle(ws2)) => AllWhistle(ws1 ++ ws2)
      case (AllWhistle(ws1), w2) => AllWhistle(ws1 :+ w2)
      case (w1, AllWhistle(ws2)) => AllWhistle(w1::ws2)
      case (w1, w2) => AllWhistle(List(w1, w2))
    }
  }

  def isInstance(s1: State, s2: State): Boolean

  // Return true if the configuration MIGHT diverge and should therefore
  // be tested using isSmaller.
  def mightDiverge(c: State): Boolean

  case object NoWhistle extends Whistle {
    def blow(h: History) = false
  }

  class StateWhistle(f: State => Boolean) extends Whistle {
    def blow(h: History) = h match {
      case Nil => false
      case s::_ => f(s)
    }
  }

  // Whistle blows if the configuation might diverge
  case object MightDivergeWhistle extends StateWhistle(mightDiverge _)

  case class DepthWhistle(maxDepth: Int) extends Whistle {
    def blow(h: History) = h.length > maxDepth
  }

  type Bag = Map[Any, Int]
  object Bag {
    def apply(): Bag = Map()
    def apply(xs: Any*): Bag = {
      xs.foldLeft(Bag()) {
        case (bag, v) =>
          bag.get(v) match {
            case Some(n) => bag + (v -> (1+n))
            case None => bag + (v -> 1)
          }
      }
    }
  }

  val tagThreshold = 10

  def tagOf(a: Any): Option[Any] = a match {
    case n: Int if n > tagThreshold => Some(tagThreshold)
    case n: Int if n < -tagThreshold => Some(-tagThreshold)
    case n: Int => Some(n)
    case n: Long if n > tagThreshold => Some(tagThreshold)
    case n: Long if n < -tagThreshold => Some(-tagThreshold)
    case n: Long => Some(n)
    case n: Float if n == n.toInt && n > tagThreshold => Some(tagThreshold)
    case n: Float if n == n.toInt && n < -tagThreshold => Some(-tagThreshold)
    case n: Double if n == n.toInt && n > tagThreshold => Some(tagThreshold)
    case n: Double if n == n.toInt && n < -tagThreshold => Some(-tagThreshold)
    case n: Float if n == n.toInt => Some(n.toInt)
    case n: Double if n == n.toInt => Some(n.toInt)
    case n: Float => Some(0.0)
    case n: Double => Some(0.0)
    case n: Char => Some(n)
    case n: String => Some("")
    case n: Boolean => Some(n)
    case n => Some(n.getClass.getName)
  }

  def tagbag(s: State) = {
    import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._

    def mergeBags(b1: Bag, b2: Bag) = {
      b2.foldLeft(b1) {
        case (bag, (v, m)) =>
          bag.get(v) match {
            case Some(n) => bag + (v -> (m+n))
            case None => bag + (v -> m)
          }
      }
    }

    lazy val toBag: PartialFunction[Any, Bag] = {
      case (n: Any) =>
        tagOf(n) match {
          case Some(v) => Bag(v)
          case None => Bag()
        }
    }

    everything(Bag())(mergeBags _)(toBag)(s)
  }

  def size(s: State): Int = {
    import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._
    count {
      case a => 1
    } (s)
  }

  case object TagbagWhistle extends Whistle {
    def isSmaller(s1: State, s2: State): Boolean = {
      import org.bitbucket.inkytonik.kiama.util.Comparison.same
      lazy val bag1 = tagbag(s1)
      lazy val bag2 = tagbag(s2)
      same(s1, s2) || (bag1.keySet == bag2.keySet && bag1.values.sum <= bag2.values.sum)
    }

    def blow(h: History) = {
      h match {
        case Nil => false
        case s::h =>
          h.exists {
            prev =>
              val v = isSmaller(prev, s)
              if (v) println(s"""
$prev < $s
""")
              v
          }
      }
    }
  }

  class FoldWhistle(ws: List[Whistle], z: Boolean, f: (Boolean, Boolean) => Boolean) extends Whistle {
    def blow(h: History) = {
      ws match {
        case Nil => z
        case w::ws =>
          println(s"Whistle $w says ${w.blow(h)}")
          ws.foldLeft(w.blow(h)) {
            case (result, w) =>
              println(s"Whistle $w says ${w.blow(h)}")
              f(result, w.blow(h))
          }
      }
    }
  }

  case class AllWhistle(ws: List[Whistle]) extends FoldWhistle(ws, true, _ && _)
  case class AnyWhistle(ws: List[Whistle]) extends FoldWhistle(ws, false, _ || _)
}
