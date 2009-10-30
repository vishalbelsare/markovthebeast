package org.riedelcastro.thebeast.env


import collection.mutable.HashMap
import doubles.{DoubleFunApp}

/**
 * @author Sebastian Riedel
 */

trait Beliefs[V, T <: Term[V]] {
  def belief(term: T): Belief[V]

  def normalize: Beliefs[V,T]

  def terms: Collection[T]
}

class MutableBeliefs[V, T <: Term[V]] extends Beliefs[V,T] {
  private val beliefs = new HashMap[T, Belief[V]]

  def setBelief(term: T, belief: Belief[V]) = {
    beliefs(term) = belief
  }

  def increaseBelief(term: T, value: V, delta: Double) = {
    var belief = beliefs.getOrElseUpdate(term,
      new MutableBelief[V](term.values)).asInstanceOf[MutableBelief[V]]
    belief.increaseBelief(value, delta)
  }

  def belief(term: T): Belief[V] = beliefs(term)

  def terms = beliefs.keySet

  def normalize = {
    val result = new MutableBeliefs[V,T]
    result.beliefs ++= beliefs.map(entry => (entry._1, entry._2.normalize))
    result
  }

  override def toString = beliefs.toString
}

sealed trait Belief[T] {
  def values: Values[T]

  def belief(value: T): Double

  def normalize: Belief[T]
 
  def norm: Double = values.foldLeft(0.0) {(n, v) => {val b = belief(v); n + b * b}}

  def /(that: Belief[T]) = applyPointWiseIgnoreIgnorance(that, (b1, b2) => b1 / b2)

  def *(that: Belief[T]) = applyPointWiseIgnoreIgnorance(that, (b1, b2) => b1 * b2)

  def +(that: Belief[T]) = applyPointWise(that, (b1, b2) => b1 + b2)

  def -(that: Belief[T]) = applyPointWise(that, (b1, b2) => b1 - b2)


  private def applyPointWiseIgnoreIgnorance(that: Belief[T], op: (Double, Double) => Double) = {
     that match {
       case Ignorance(_) => this
       case _ => {
         val result = new MutableBelief(values)
         for (v <- values) result.setBelief(v, op(belief(v), that.belief(v)))
         result
       }
     }
   }

   private def applyPointWise(that: Belief[T], op: (Double, Double) => Double) = {
     val result = new MutableBelief(values)
     for (v <- values) result.setBelief(v, op(belief(v), that.belief(v)))
     result
   }



}

case class BeliefTerm[T](belief: Belief[T], override val arg: Term[T])
        extends DoubleFunApp(Constant((t: T) => belief.belief(t)), arg) {
  override def toString = "Belief(" + arg + ")=" + belief
}

case class Ignorance[T](val values: Values[T]) extends Belief[T] {
  def belief(value: T) = 1.0

  override def *(that: Belief[T]) = that

  def normalize = this
}

class MutableBelief[T](val values: Values[T]) extends Belief[T] {
  private val _belief = new HashMap[T, Double]

  def belief(value: T) = _belief(value)

  def setBelief(value: T, belief: Double) = _belief(value) = belief

  def increaseBelief(value: T, belief: Double) = _belief(value) = _belief.getOrElse(value, 0.0) + belief

  def total = _belief.values.foldLeft(0.0) {(r, b) => r + b}  

  def normalize = {
    val norm = total
    val result = new MutableBelief(values)
    for (pair <- _belief) {
      result._belief += pair._1 -> pair._2 / norm
    }
    result
  }

  override def toString = _belief.toString
}
