package utilities

/**
  * Created by weijiayi on 3/2/16.
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
}
