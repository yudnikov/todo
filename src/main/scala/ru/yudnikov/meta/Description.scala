package ru.yudnikov.meta

import ru.yudnikov.engine.Model

/**
  * Created by Don on 14.04.2017.
  */
trait Description {

  val name: String
  val aClass: Class[_]
  val parentClass: Class[_ <: Model]
  val isReference: Boolean
  val isCollection: Boolean
  val isReferenceCollection: Boolean

  assume(isReference != isCollection | !isReference & !isCollection)
  assume(isReference & isReferenceCollection | !isReferenceCollection)

}
