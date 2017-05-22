package ru.yudnikov.meta

import ru.yudnikov.engine.{Model, Reference}

import scala.reflect.runtime.universe._

/**
  * Created by Don on 16.04.2017.
  */
trait ReflectorCache extends Reflector {

  var classDescriptions: Map[Class[_ <: Model], List[FieldDescription]] = Map()
  var modelDescriptions: Map[Model, List[PropertyDescription]] = Map()
  var primaryConstructors: Map[Class[_], MethodSymbol] = Map()
  var references: Map[Model, List[Reference[Model]]] = Map()

  override def describe[T <: Model](aClass: Class[T]): List[FieldDescription] = {
    classDescriptions.get(aClass) match {
      case None =>
        classDescriptions = classDescriptions + (aClass -> super.describe(aClass))
        describe[T](aClass)
      case Some(x) =>
        x
    }
  }

  override def describe(model: Model): List[PropertyDescription] = {
    modelDescriptions.get(model) match {
      case None =>
        modelDescriptions = modelDescriptions + (model -> super.describe(model))
        describe(model)
      case Some(x) =>
        x
    }
  }

  override def getPrimaryConstructor(aClass: Class[_]): MethodSymbol = {
    primaryConstructors.get(aClass) match {
      case None =>
        primaryConstructors = primaryConstructors + (aClass -> super.getPrimaryConstructor(aClass))
        getPrimaryConstructor(aClass)
      case Some(x) =>
        x
    }
  }

  override def getReferences(model: Model): List[Reference[Model]] = {
    references.get(model) match {
      case None =>
        references = references + (model -> super.getReferences(model))
        getReferences(model)
      case Some(x) =>
        x
    }
  }

}
