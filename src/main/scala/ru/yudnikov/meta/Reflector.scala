package ru.yudnikov.meta

import _root_.ru.yudnikov.engine.{Model, Reference}

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe => ru}
import _root_.ru.yudnikov.database.cassandra.Indexed

/**
  * Created by Don on 16.04.2017.
  */
trait Reflector {

  //private val mirror = ru.runtimeMirror(getClass.getClassLoader)

  private def symbolToString(symbol: Symbol): String = symbol.name.toString.trim

  def describe[T <: Model](aClass: Class[T]): List[FieldDescription] = {
    val tpe = ru.runtimeMirror(aClass.getClassLoader).classSymbol(aClass).toType
    val getters = tpe.members.collect {
      case m: MethodSymbol if !m.isPrivate & m.isGetter => symbolToString(m) -> m
    }.toMap
    val fields = tpe.members.collect {
      case ts: TermSymbol if ts.isVal => symbolToString(ts) -> ts
    }.toMap
    val intersect = getters.keys.toList.intersect(fields.keys.toList)
    val ordered = getPrimaryConstructor(aClass).paramLists.head.map(s => symbolToString(s)).intersect(intersect)
    ordered.map(name => {
      new FieldDescription(name, fields(name), aClass)
    })
  }

  def describe(model: Model): List[PropertyDescription] = {
    val modelMirror = ru.runtimeMirror(model.getClass.getClassLoader).reflect(model)
    val aClass = model.getClass
    val fds = describe(aClass)
    fds.map(fd => new PropertyDescription(fd.name, fd.termSymbol, modelMirror.reflectField(fd.termSymbol).get, aClass))
  }

  def describe[T <: Model](aClass: Class[T], propertyName: String, propertyValue: Any): Option[PropertyDescription] = {
    describe(aClass).filter(fd => fd.name == propertyName) match {
      case list: List[FieldDescription] if list.length == 1 =>
        Some(new PropertyDescription(list.head, propertyValue))
      case _ => None
    }
  }

  def getAnnotated[T <: Model](aClass: Class[T], annotationClass: Class[_]): List[FieldDescription] = {
    val fds = describe(aClass)
    val tpe = ru.runtimeMirror(aClass.getClassLoader).classSymbol(annotationClass).toType
    fds.filter(fd => fd.termSymbol.annotations.exists(a => a.tree.tpe.typeSymbol.asType.toType == tpe))
  }

  def getPrimaryConstructor(aClass: Class[_]): MethodSymbol = {
    val classSymbol = ru.runtimeMirror(aClass.getClassLoader).classSymbol(aClass)
    val tpe = classSymbol.toType
    tpe.decl(ru.termNames.CONSTRUCTOR).asTerm.alternatives.head.filter(_.asMethod.isPrimaryConstructor == true).asMethod
  }

  def getReferences(model: Model): List[Reference[Model]] = {
    val straightRefs = describe(model).filter(pd => pd.isReference).map(pd => pd.value.asInstanceOf[Reference[Model]])
    val embeddedRefs = for {
      pd <- describe(model).filter(pd => pd.isReferenceCollection)
      value <- pd.value.asInstanceOf[Iterable[Reference[Model]]]
    } yield value
    straightRefs.union(embeddedRefs)
  }

  def instantiate[T <: Model](aClass: Class[T], args: List[Any]): T = {
    val mirror = ru.runtimeMirror(aClass.getClassLoader)
    val classMirror = mirror.reflectClass(mirror.classSymbol(aClass))
    val constructorMirror = classMirror.reflectConstructor(getPrimaryConstructor(aClass))
     constructorMirror.apply(args: _*).asInstanceOf[T]
  }

}

object Reflector extends Reflector with ReflectorCache
