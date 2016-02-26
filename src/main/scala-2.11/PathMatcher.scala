import java.util.regex.Pattern

/**
  * Created by Ievgen Medvediev on 19/02/16.
  */
class PathMatcher {

  case class MatcherResult(isMatch: Boolean, patterns: List[(String, String)] = List())

  def matches(path: String, pattern: String): MatcherResult = (path, pattern) match {
    case (p, t) if isValidPath(p) && isValidPattern(t) => {
      val (path, pattern) = (unslash(p), unslash(t))
      val patternKeys = getPatternKeys(pattern)
      val m = Pattern.compile(getRegexpPattern(pattern)).matcher(path)
      if (m.matches()) new MatcherResult(isMatch = true, (1 to m.groupCount()).toList.map(i => (patternKeys(i - 1), m.group(i))))
      else new MatcherResult(isMatch = false, List())
    }
    case _ => new MatcherResult(isMatch = false, List())
  }

  def getPatternKeys(pattern: String): List[String] = {
    def splitQuestion(list: List[String]): List[String] = list match {
      case Nil => Nil
      case x :: xs => (if (x.toList.forall(_ == '?')) List.fill(x.length)("?") else List(x)) ::: splitQuestion(xs)
    }
    splitQuestion(pattern.split("[^\\*^\\?]").toList.filter(_.nonEmpty))
  }

  def isValidPath(path: String): Boolean = {
    path != null && path.startsWith("/") && !unslash(path).contains("//")
  }

  def isValidPattern(pattern: String): Boolean = {
    pattern != null &&
      (pattern.startsWith("/") || pattern.startsWith("**")) &&
      !pattern.contains("***") &&
      !unslash(pattern).contains("//")
  }

  def unslash(s: String): String = s match {
    case x if x.startsWith("/") => unslash(x.substring(1))
    case _ => s
  }

  def getRegexpPattern(pattern: String): String = {
    s"^${pattern.replace("**", "||").replace("*", "([^/]*)").replace("||", "(.*)").replace("?", "(.?)")}$$"
  }
}

object PathMatcher {
  def apply(): PathMatcher = {
    new PathMatcher()
  }
}

