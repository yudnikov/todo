package ru.yudnikov.meta

import ru.yudnikov.engine.{Model, Reference}

import scala.reflect.runtime.universe._

/**
  * Created by Don on 15.04.2017.
  */
class FieldDescription(
                        val name: String,
                        val termSymbol: TermSymbol,
                        val parentClass: Class[_ <: Model]
                      ) extends Description {

  val aType: Type = termSymbol.typeSignature
  val aClass: Class[_] = Class.forName(aType.typeSymbol.asClass.fullName)
  private val typeArgs = aType.typeArgs
  val isReference: Boolean = classOf[Reference[Model]].isAssignableFrom(aClass)
  val refClass: Option[Class[_]] = if (isReference) Some(Class.forName(typeArgs.head.typeSymbol.fullName)) else None
  val isCollection: Boolean = classOf[Iterable[_]].isAssignableFrom(aClass)
  val isReferenceCollection: Boolean =
    if (typeArgs.length == 1)
      classOf[Reference[Model]].isAssignableFrom(Class.forName(typeArgs.head.typeSymbol.fullName))
    else
      false

  override def toString: String = {
    s"\nFieldDescription(name = $name, aClass = ${aClass.getName}, isReference = $isReference, " +
      s"isCollection = $isCollection, isReferenceCollection = $isReferenceCollection)"
  }

}
