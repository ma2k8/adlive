package adlive.infrastructure.scalikejdbc.models.generated

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import org.joda.time._
import scalikejdbc._

class StreamUrlSpec extends Specification {

  "StreamUrl" should {

    val su = StreamUrl.syntax("su")

    "find by primary keys" in new AutoRollback {
      val maybeFound = StreamUrl.find()
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = StreamUrl.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = StreamUrl.countAll()
      count should be_>(0L)
    }
    "find by where clauses" in new AutoRollback {
      val results = StreamUrl.findAllBy()
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = StreamUrl.countBy()
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = StreamUrl.create(channelId = 123, createdAt = DateTime.now, updatedAt = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = StreamUrl.findAll().head
      // TODO modify something
      val modified = entity
      val updated = StreamUrl.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = StreamUrl.findAll().head
      StreamUrl.destroy(entity)
      val shouldBeNone = StreamUrl.find()
      shouldBeNone.isDefined should beFalse
    }
  }

}
        