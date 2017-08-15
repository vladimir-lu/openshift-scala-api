package is.solidninja
package openshift
package api
package v1

import java.util.regex.Pattern

import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import fs2.util.Attempt

import scala.util.matching.Regex

import is.solidninja.k8s.api.{v1 => k8sv1}

import JsonProtocol._

private[api] object TemplateExpander {

  private val PARAM_REGEX = """\$\{(\w+)\}""".r

  sealed trait ParameterExpansionError extends Exception

  case class ParameterNotFoundError(name: String)
      extends Exception(s"Could not find template parameter '$name' in parameter definitions")
      with ParameterExpansionError

  case class ExpansionError(parameter: Parameter, message: String)
      extends Exception(s"Error expanding parameter $parameter: $message")
      with ParameterExpansionError

  def expandTemplate(template: Template, parameters: Map[String, String]): Attempt[TemplateList] =
    template.objects
      .map(expandParametersOnObject(template, parameters))
      .sequenceU
      .map(TemplateList(_, metadata = None))

  private def expandParametersOnObject(template: Template, overrides: Map[String, String])(
      obj: Json): Attempt[EitherTopLevel] = {

    def expandKeysInField(k: String, v: Json): Either[ParameterExpansionError, (String, Json)] = v match {
      case _ if v.isString =>
        resolveParameter(v.asString.get, overrides, template.parameters)
          .map(s => (k, Json.fromString(s)))
      case _ if v.isObject || v.isArray => expandKeys(v).map((k, _))
      case _ => Right((k, v))
    }

    def expandInObject(o: JsonObject): Either[ParameterExpansionError, Json] =
      o.toList
        .map { case (k, v) => expandKeysInField(k, v) }
        .sequenceU
        .map(JsonObject.from(_))
        .map(Json.fromJsonObject)

    def expandInArray(a: Vector[Json]): Either[ParameterExpansionError, Json] =
      a.map(expandKeys)
        .sequenceU
        .map(Json.fromValues)

    def expandKeys(j: Json): Either[ParameterExpansionError, Json] =
      j.asObject
        .map(expandInObject)
        .orElse(j.asArray.map(expandInArray))
        .getOrElse(Right(j))

    // Need to merge the labels from the template
    val labelJson = Json
      .fromJsonObject(JsonObject.fromMap(Map("metadata" -> k8sv1.ObjectMeta(labels = template.labels).asJson)))
      .withoutNulls

    expandKeys(obj).map(o => labelJson.deepMerge(o)).flatMap(_.as[EitherTopLevel])
  }

  private[v1] def resolveParameter(in: String,
                                   overrides: Map[String, String],
                                   params: List[Parameter]): Either[ParameterExpansionError, String] = {

    def findParameter(name: String): Either[ParameterExpansionError, Parameter] =
      params.find(_.name == name).map(Right(_)).getOrElse(Left(ParameterNotFoundError(name)))

    def findReplacement(m: Regex.Match): Either[ParameterExpansionError, (String, String)] = {
      val (full, name) = (m.group(0), m.group(1))
      for {
        param <- findParameter(name)
        maybeValue = overrides
          .get(name)
          .orElse(param.value)
          .orElse(generateParameterValue(param))
          .orElse(if (param.required.forall(_ == false)) Some("") else None)
        v <- maybeValue
          .map(Right(_))
          .getOrElse(Left(ExpansionError(param, s"Unable to lookup or generate value for parameter '$name'")))
      } yield (full, v)
    }

    def performReplace(replacements: List[(String, String)]): String = replacements.foldLeft(in) {
      case (s, (prev, repl)) => s.replaceAll(Pattern.quote(prev), repl)
    }

    PARAM_REGEX
      .findAllIn(in)
      .matchData
      .toList
      .map(findReplacement)
      .sequenceU
      .map(performReplace)
  }

  private[v1] def generateParameterValue(parameter: Parameter): Option[String] = None // FIXME - support generation
}
