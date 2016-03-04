package utilities

/**
  * Some extra operations on IndexedSeq
  */
object CollectionOp {
  def modify[A](collection: IndexedSeq[A], index: Int)(x: A): IndexedSeq[A] = {
    collection.indices.map{ i =>
      if(i==index) x else collection(i)
    }
  }

  def modifyInsert[A](collection: IndexedSeq[A], index: Int)(xs: IndexedSeq[A]): IndexedSeq[A] = {
    val (l, r) = collection.splitAt(index)
    l ++ xs ++ r.tail
  }

  def transformSelected[A](collection: IndexedSeq[A], indices: Set[Int])(transform: A => A): IndexedSeq[A] = {
    collection.indices.map{ i =>
      if(indices.contains(i))
        transform(collection(i))
      else collection(i)
    }
  }

  def firstIndex[A](xs: IndexedSeq[A])(p: A => Boolean): Option[Int] = {
    xs.indices.foreach(i => if(p(xs(i))) return Some(i))
    None
  }
}
