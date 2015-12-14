package adlive.infrastructure.scalikejdbc.models.generated

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import org.joda.time._
import scalikejdbc._

class ChannelUrlSpec extends Specification {

  "ChannelUrl" should {

    val cu = ChannelUrl.syntax("cu")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ChannelUrl.find(123)
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ChannelUrl.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ChannelUrl.countAll()
      count should be_>(0L)
    }
    "find by where clauses" in new AutoRollback {
      val results = ChannelUrl.findAllBy(sqls.eq(cu.channelId, 123))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ChannelUrl.countBy(sqls.eq(cu.channelId, 123))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ChannelUrl.create(channelId = 123, createdAt = DateTime.now, updatedAt = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ChannelUrl.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ChannelUrl.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ChannelUrl.findAll().head
      ChannelUrl.destroy(entity)
      val shouldBeNone = ChannelUrl.find(123)
      shouldBeNone.isDefined should beFalse
    }
  }

}
        