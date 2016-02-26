import org.scalatest.PropSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
  * Created by ievgen on 22/02/16.
  */
class PathMatcherSuite extends PropSpec{

  val pathMatcher = PathMatcher()

  val unslash = Table(
    ("in", "out"),
    ("/", ""),
    ("/test", "test"),
    ("/*", "*"),
    ("/?","?"),
    ("/**","**"),
    ("**", "**"),
    ("///test","test"),
    ("///**", "**")
  )

  property("unslash") {
    forAll(unslash) {
      (in, out) => {
        val result = pathMatcher.unslash(in)
        assert(result === out)
      }
    }
  }

  val isValidPath = Table(
    "path",
    "/",
    "/path",
    "/path/path",
    "/path/path/",
    "//",
    "///"
  )

  property("valid path") {
    forAll(isValidPath) {
      path => {
        assert(pathMatcher.isValidPath(path) === true)
      }
    }
  }

  val isNotValidPath = Table(
    "path",
    null,
    "",
    "not-started-from-slash",
    "/test//",
    "///test//"
  )

  property("not valid path") {
    forAll(isNotValidPath) {
      path => {
        assert(pathMatcher.isValidPath(path) === false)
      }
    }
  }

  val isValidPattern = Table(
    "pattern",
    "/",
    "/pattern",
    "/*",
    "/?",
    "/**",
    "**",
    "**/?/*",
    "//",
    "///"
  )

  property("valid pattern") {
    forAll(isValidPattern) {
      pattern => {
        assert(pathMatcher.isValidPattern(pattern) === true)
      }
    }
  }

  val isNotValidPattern = Table(
    "pattern",
    null,
    "",
    "not-started-from-slash-or-double-star",
    "*-started-from-star",
    "?-started-from-star",
    "***",
    "**//",
    "///**//"
  )

  property("not valid") {
    forAll(isNotValidPattern) {
      pattern => {
        assert(pathMatcher.isValidPattern(pattern) === false)
      }
    }
  }

  val patternKeys = Table(
    ("in", "out"),
    ("", List()),
    ("test", List()),
    ("*", List("*")),
    ("?",List("?")),
    ("??",List("?", "?")),
    ("**", List("**")),
    ("?test",List("?")),
    ("te?st",List("?")),
    ("test?",List("?")),
    ("?te?st?",List("?", "?", "?")),
    ("???te???st???",List("?", "?", "?","?", "?", "?", "?", "?", "?")),
    ("*test",List("*")),
    ("te*st",List("*")),
    ("test*",List("*")),
    ("*te*st*",List("*", "*", "*")),
    ("**test",List("**")),
    ("te**st",List("**")),
    ("test**",List("**")),
    ("**te**st**",List("**", "**", "**")),
    ("*/?/**/test/*/??/**/test",List("*", "?", "**", "*", "?", "?", "**"))
  )

  property("getPatternKeys") {
    forAll(patternKeys) {
      (in, out) => {
        val result = pathMatcher.getPatternKeys(in)
        assert(result === out)
      }
    }
  }

  val regexpPattern = Table(
    ("in", "out"),
    ("", "^$"),
    ("test", "^test$"),
    ("*", "^([^/]*)$"),
    ("?","^(.?)$"),
    ("??","^(.?)(.?)$"),
    ("**", "^(.*)$"),
    ("*/?/**/test/*/??/**/test", "^([^/]*)/(.?)/(.*)/test/([^/]*)/(.?)(.?)/(.*)/test$")
  )

  property("getRegexpPattern") {
    forAll(regexpPattern) {
      (in, out) => {
        val result = pathMatcher.getRegexpPattern(in)
        assert(result === out)
      }
    }
  }

  val values = Table(
    ("isMatches", "path", "pattern", "patterns"),
    (true, "/" , "/", List()),                              //match root
    (true, "/test" , "/test", List()),                      //match
    (true, "/test" , "/*test", List(("*", ""))),            //match section with star in the beginning empty
    (true, "/any-test" , "/*test", List(("*", "any-"))),    //match section with star in the beginning not empty
    (true, "/test" , "/te*st", List(("*", ""))),            //match section with star in the middle empty
    (true, "/te-any-st" , "/te*st", List(("*", "-any-"))),  //match section with star in the middle not empty
    (true, "/test-any" , "/test*", List(("*", "-any"))),    //match section with star in the end not empty
    (true, "/test" , "/test*", List(("*", ""))),            //match section with star in the end empty
    (true, "/with/sections/test" , "**test",                //match doublestar in the beginning not empty
      List(("**", "with/sections/"))),
    (true, "/test" , "**test", List(("**", ""))),           //match doublestar in the beginning empty
    (true, "/test" , "/**test", List(("**", ""))),          //match slash doublestar in the beginning empty
    (true, "/test" , "/te**st", List(("**", ""))),          //match doublestar in the middle empty
    (true, "/te/with/sections/st" , "/te**st",              //match doublestar in the middle not empty
      List(("**", "/with/sections/"))),
    (true, "/test" , "/test**", List(("**", ""))),          //match doublestar in the end empty
    (true, "/test/with/sections" , "/test**",               //match doublestar in the end not empty
      List(("**", "/with/sections"))),
    (true, "/test" , "/?test", List(("?", ""))),            //match question in the beginning empty
    (true, "/atest" , "/?test", List(("?", "a"))),          //match question in the beginning not empty
    (true, "/test" , "/te?st", List(("?", ""))),            //match question in the middle empty
    (true, "/teast" , "/te?st", List(("?", "a"))),          //match question in the middle not empty
    (true, "/test" , "/test?", List(("?", ""))),            //match question in the end empty
    (true, "/testa" , "/test?", List(("?", "a"))),          //match question in the end not empty
    (true, "/thetest" , "/?h?t?s?",                         //match multi question
      List(
        ("?", "t"),
        ("?", "e"),
        ("?", "e"),
        ("?", "t")
      )),
    (true, "/thetest" , "/th???st",                         //match multi question in a row
      List(
        ("?", "e"),
        ("?", "t"),
        ("?", "e")
      )),
    (true,                                                  //match complex test
      "/this/is/very/complex/test",
      "/?hi*/*/**/??s??",
      List(
        ("?","t"),
        ("*","s"),
        ("*","is"),
        ("**","very/complex"),
        ("?","t"),
        ("?","e"),
        ("?","t"),
        ("?","")
      )),
    (false, "" , "/", List()),                              //no match empty path
    (false, "/" , "", List()),                              //no match empty pattern
    (false, "/test1" , "/test2", List()),                   //no match
    (false, "/test" , "/test1", List()),                    //no match simple extra path
    (false, "/test1" , "/test", List()),                    //no match simple extra pattern
    (false, "test" , "/test", List()),                      //no match path not started with slash
    (false, "/test" , "test", List()),                      //no match pattern not started with slash
    (false, "/test" , "*test", List()),                     //no match starts from star
    (false, "/test1" , "/*test", List()),                   //no match section with star in the beginning
    (false, "/test" , "/*test2", List()),                   //no match section with star in the beginning not match pattern
    (false, "/test/any/one" , "/test/*one", List()),        //no match section with star extra section
    (false, "/test1" , "**test", List()),                   //no match doublestar in the beginning extra path
    (false, "/test" , "**test2", List()),                   //no match doublestar in the beginning extra pattern
    (false, "/test" , "?test", List()),                     //no match starts from question
    (false, "/test1" , "/?test", List()),                   //no match question in the beginning
    (false, "/test" , "/?test2", List()),                   //no match question in the beginning not match pattern
    (false, "/test/any/one" , "/test/?one", List())         //no match question extra section

  )

  property("matches") {
    forAll(values) {
      (isMatches, path, pattern, patterns) => {
        val result: PathMatcher#MatcherResult = pathMatcher.matches(path, pattern)
        assert(result.isMatch === isMatches)
        assert(result.patterns === patterns)
      }
    }
  }

}
