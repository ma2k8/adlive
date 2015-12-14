package adlive.infrastructure.scalikejdbc.models.generated

import scalikejdbc._
import org.joda.time.{DateTime}

case class StreamUrl(
  channelId: Int, 
  url: String, 
  createdAt: DateTime, 
  updatedAt: DateTime, 
  deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = StreamUrl.autoSession): StreamUrl = StreamUrl.save(this)(session)

  def destroy()(implicit session: DBSession = StreamUrl.autoSession): Unit = StreamUrl.destroy(this)(session)

}
      

object StreamUrl extends SQLSyntaxSupport[StreamUrl] {

  override val tableName = "stream_url"

  override val columns = Seq("channel_id", "url", "created_at", "updated_at", "deleted_at")

  def apply(su: SyntaxProvider[StreamUrl])(rs: WrappedResultSet): StreamUrl = apply(su.resultName)(rs)
  def apply(su: ResultName[StreamUrl])(rs: WrappedResultSet): StreamUrl = new StreamUrl(
    channelId = rs.get(su.channelId),
    url = rs.get(su.url),
    createdAt = rs.get(su.createdAt),
    updatedAt = rs.get(su.updatedAt),
    deletedAt = rs.get(su.deletedAt)
  )
      
  val su = StreamUrl.syntax("su")

  override val autoSession = AutoSession

  def find(channelId: Int)(implicit session: DBSession = autoSession): Option[StreamUrl] = {
    withSQL {
      select.from(StreamUrl as su).where.eq(su.channelId, channelId)
    }.map(StreamUrl(su.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[StreamUrl] = {
    withSQL(select.from(StreamUrl as su)).map(StreamUrl(su.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(StreamUrl as su)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[StreamUrl] = {
    withSQL { 
      select.from(StreamUrl as su).where.append(sqls"${where}")
    }.map(StreamUrl(su.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(StreamUrl as su).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    channelId: Int,
    url: String,
    createdAt: DateTime,
    updatedAt: DateTime,
    deletedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): StreamUrl = {
    withSQL {
      insert.into(StreamUrl).columns(
        column.channelId,
        column.url,
        column.createdAt,
        column.updatedAt,
        column.deletedAt
      ).values(
        channelId,
        url,
        createdAt,
        updatedAt,
        deletedAt
      )
    }.update.apply()

    StreamUrl(
      channelId = channelId,
      url = url,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt)
  }

  def save(entity: StreamUrl)(implicit session: DBSession = autoSession): StreamUrl = {
    withSQL {
      update(StreamUrl).set(
        column.channelId -> entity.channelId,
        column.url -> entity.url,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt,
        column.deletedAt -> entity.deletedAt
      ).where.eq(column.channelId, entity.channelId)
    }.update.apply()
    entity
  }
        
  def destroy(entity: StreamUrl)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(StreamUrl).where.eq(column.channelId, entity.channelId) }.update.apply()
  }
        
}
