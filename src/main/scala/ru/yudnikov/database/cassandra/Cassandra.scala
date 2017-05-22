package ru.yudnikov.database.cassandra

import java.util.{Date, UUID}

import com.datastax.driver.core._
import org.joda.time.DateTime
import ru.yudnikov.Settings
import ru.yudnikov.engine.{Model, Reference}
import ru.yudnikov.meta.{Description, PropertyDescription, Reflector}

import scala.collection.JavaConversions

/**
  * Created by Don on 21.04.2017.
  */
object Cassandra {

  /* CREATE KEYSPACE todo WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }; */

  lazy private val cluster = Cluster.builder().addContactPoint(Settings.cassandraHost).withPort(Settings.cassandraPort).build()
  lazy private val session = cluster.connect(Settings.cassandraKeyspace)


  implicit def javaBooleanToScalaBoolean(b: java.lang.Boolean): Boolean = {
    b.booleanValue()
  }

  private val scalaToCassandra: Map[Class[_], (String, (Any) => String)] = Map(
    classOf[Int] -> ("int", (value: Any) => value.asInstanceOf[Int].toString),
    classOf[String] -> ("varchar", (value: Any) => s"'${value.asInstanceOf[String].toString}'"),
    Class.forName("scala.Boolean") -> ("boolean", (value: Any) => value.toString),
    classOf[Date] -> ("timestamp", (value: Any) => s"'${value.asInstanceOf[Date].getTime.toString}'"),
    classOf[DateTime] -> ("timestamp", (value: Any) => s"'${value.asInstanceOf[DateTime].toDate.getTime.toString}'"),
    classOf[UUID] -> ("uuid", (value: Any) => value.asInstanceOf[UUID].toString),
    classOf[Reference[_]] -> ("uuid", (value: Any) => value.asInstanceOf[Reference[Model]].id.toString)
  )

  private def castType(description: Description): Option[String] = {
    scalaToCassandra.get(if (description.isReference) classOf[UUID] else description.aClass) match {
      case None =>
        println(s"cannot castType for: $description")
        None
      case Some(tuple: (String, (Any) => String)) => Some(s"${tuple._1}")
    }
  }

  private def castValue(propertyDescription: PropertyDescription): Option[String] = {
    scalaToCassandra.get(propertyDescription.aClass) match {
      case None =>
        println(s"[Cassandra]: cannot get value for: $propertyDescription"); None
      case Some(tuple: (String, (Any) => String)) =>
        Some(tuple._2.apply(propertyDescription.value))
    }
  }

  def executeQuery(query: String): Option[ResultSet] = {
    println(s"[Cassandra]: executeQuery($query)")
    try Some(session.execute(query))
    catch {
      case _ => println(s"[Cassandra]: can't execute query..."); None
    }
  }

  def save[T <: Model](model: T): Unit = {
    println(s"[Cassandra]: save($model)")
    createTable(model.getClass)
    val pds = Reflector.describe(model)
    val straights = pds.filter(pd => !pd.isCollection)
    val casted = straights.map(pd => pd.name -> castValue(pd))
    val query = s"insert into ${model.getClass.getSimpleName} (${
      casted.map(tuple => tuple._1).mkString(", ")
    }) values (${
      casted.map(tuple => tuple._2.get).mkString(", ")
    });"
    executeQuery(query)
  }

  def list[T <: Model](aClass: Class[T]): List[T] = {
    val query = s"select * from ${aClass.getSimpleName};"
    val resultSet: ResultSet = executeQuery(query).get
    val iterator = JavaConversions.asScalaIterator(resultSet.iterator).toList
    iterator.map(row => fetchRow(aClass, row))
  }

  def list[T <: Model](propertyDescription: PropertyDescription): List[T] = {
    val casted = propertyDescription.name -> castValue(propertyDescription)
    val query = s"select * from ${propertyDescription.parentClass.getSimpleName} where ${casted._1} = ${casted._2.get};"
    executeQuery(query) match {
      case Some(resultSet: ResultSet) =>
        JavaConversions.asScalaIterator(resultSet.iterator).map(row =>
          fetchRow(propertyDescription.parentClass.asInstanceOf[Class[T]], row)
        ).toList
      case _ => List[T]()
    }
  }

  def load[T <: Model](propertyDescription: PropertyDescription): Option[T] = {
    val casted = propertyDescription.name -> castValue(propertyDescription)
    val query = s"select * from ${propertyDescription.parentClass.getSimpleName} where ${casted._1} = ${casted._2.get};"
    executeQuery(query) match {
      case Some(resultSet: ResultSet) =>
        JavaConversions.asScalaIterator(resultSet.iterator).toList match {
          case list: List[Row] if list.length == 1 =>
            Some(fetchRow(propertyDescription.parentClass.asInstanceOf[Class[T]], list.head))
          case _ => None
        }
      case _ => None
    }
  }

  private def fetchRow[T <: Model](aClass: Class[T], row: Row): T = {
    val args = Reflector.describe(aClass).map(fd =>
      if (!fd.isReference)
        fd.aClass match {
          case _ if fd.aClass == classOf[DateTime] =>
            new DateTime(row.get(fd.name, classOf[Date]))
          case _ if fd.aClass == Class.forName("scala.Boolean") =>
            row.get(fd.name, classOf[java.lang.Boolean])
          case _ =>
            row.get(fd.name, fd.aClass)
        }
      else
        new Reference(fd.refClass.get.asInstanceOf[Class[T]], row.get(fd.name, classOf[UUID]))
    )
    Reflector.instantiate(aClass, args)
  }

  def createTable[T <: Model](aClass: Class[T]): Unit = {
    val columns = {
      for {
        d <- Reflector.describe(aClass).filter(fd => !fd.isCollection)
        cassandraType = castType(d)
        if cassandraType.isDefined
      } yield s"${d.name} ${cassandraType.get} ${getSuffix(d.name)}".trim
    }.mkString(", ")
    executeQuery(s"create table if not exists ${aClass.getSimpleName} ($columns);")
    Reflector.getAnnotated(aClass, classOf[Indexed]).foreach(
      fd => executeQuery(
        s"create index if not exists ${aClass.getSimpleName}_${fd.name} on ${Settings.cassandraKeyspace}.${aClass.getSimpleName} (${fd.name});")
    )
  }

  def dropTable[T <: Model](aClass: Class[T]): Unit = {
    executeQuery(s"drop table if exists ${aClass.getSimpleName}")
    Reflector.getAnnotated(aClass, classOf[Indexed]).foreach(
      fd => executeQuery(
        s"drop index if exists ${Settings.cassandraKeyspace}.${aClass.getSimpleName}_${fd.name}")
    )
  }

  def getSuffix(fieldName: String): String = fieldName match {
    case "id" => "primary key"
    case _ => ""
  }

}
