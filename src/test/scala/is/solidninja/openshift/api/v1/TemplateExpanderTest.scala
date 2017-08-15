package is.solidninja.openshift.api.v1

import org.scalatest.{FreeSpec, Matchers}

class TemplateExpanderTest extends FreeSpec with Matchers {

  "Expanding with no parameters" - {
    "should result in an identical string" in {
      TemplateExpander.resolveParameter("Test parameter.", overrides = Map.empty, params = Nil) should equal(
        Right("Test parameter."))
    }
  }

  "Expanding with a single parameter" - {

    "with no value should pick an empty default value" in {
      TemplateExpander.resolveParameter(s"Param $${X}", overrides = Map.empty, params = Parameter("X") :: Nil) should equal(
        Right("Param ")
      )
    }

    "with a default value should replace that parameter" in {
      val param = Parameter(
        name = "NAME",
        value = Some("Bob")
      )

      TemplateExpander.resolveParameter(s"Hello $${NAME}!", overrides = Map.empty, params = param :: Nil) should equal(
        Right("Hello Bob!")
      )
    }

    "with a default value and override should pick the override" in {
      val param = Parameter(
        name = "NAME",
        value = Some("Bob")
      )

      TemplateExpander.resolveParameter(s"Hello $${NAME}!", overrides = Map("NAME" -> "Alice"), params = param :: Nil) should equal(
        Right("Hello Alice!")
      )
    }

    "multiple times should replace all occurrences" in {
      TemplateExpander.resolveParameter(in = s"Hello $${NAME}! Are you $${NAME}?",
                                        overrides = Map("NAME" -> "Eve"),
                                        params = Parameter("NAME") :: Nil) should equal(
        Right("Hello Eve! Are you Eve?")
      )
    }
  }

  "Expanding with multiple parameters" - {

    "should pick both default and override values" in {
      val params = List(
        Parameter("A"),
        Parameter("B", value = Some("B")),
        Parameter("C")
      )

      TemplateExpander.resolveParameter(in = s"$${A} $${B} $${C}",
                                        overrides = Map("A" -> "A", "C" -> "C"),
                                        params = params) should equal(Right("A B C"))
    }
  }

}
