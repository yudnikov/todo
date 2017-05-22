package ru.yudnikov.meta

import ru.yudnikov.engine.Model

import scala.reflect.runtime.universe._

/**
  * Created by Don on 15.04.2017.
  */
class PropertyDescription(
                           name: String,
                           termSymbol: TermSymbol,
                           val value: Any,
                           parentClass: Class[_ <: Model]
                         ) extends FieldDescription(name, termSymbol, parentClass) {

  def this(fieldDescription: FieldDescription, value: Any) {
    this(fieldDescription.name, fieldDescription.termSymbol, value, fieldDescription.parentClass)
  }

  override def toString: String = {
    s"\nPropertyDescription(name = $name, value = $value, aClass = ${aClass.getName}, isReference = $isReference, " +
      s"isCollection = $isCollection, isReferenceCollection = $isReferenceCollection)"
  }

}
