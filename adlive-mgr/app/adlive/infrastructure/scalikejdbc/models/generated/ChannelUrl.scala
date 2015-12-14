package adlive.infrastructure.scalikejdbc.models.generated

import scalikejdbc._
import org.joda.time.{DateTime}

case class ChannelUrl(
  channelId: Int, 
  url: String, 
  createdAt: DateTime, 
  updatedAt: DateTime, 
  deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = ChannelUrl.autoSession): ChannelUrl = ChannelUrl.save(this)(session)

  def destroy()(implicit session: DBSession = ChannelUrl.autoSession): Unit = ChannelUrl.destroy(this)(session)

}
      

object ChannelUrl extends SQLSyntaxSupport[ChannelUrl] {

  override val tableName = "channel_url"

  override val columns = Seq("channel_id", "url", "created_at", "updated_at", "deleted_at")

  def apply(cu: SyntaxProvider[ChannelUrl])(rs: WrappedResultSet): ChannelUrl = apply(cu.resultName)(rs)
  def apply(cu: ResultName[ChannelUrl])(rs: WrappedResultSet): ChannelUrl = new ChannelUrl(
    channelId = rs.get(cu.channelId),
    url = rs.get(cu.url),
    createdAt = rs.get(cu.createdAt),
    updatedAt = rs.get(cu.updatedAt),
    deletedAt = rs.get(cu.deletedAt)
  )
      
  val cu = ChannelUrl.syntax("cu")

  override val autoSession = AutoSession

  def find(channelId: Int)(implicit session: DBSession = autoSession): Option[ChannelUrl] = {
    withSQL {
      select.from(ChannelUrl as cu).where.eq(cu.channelId, channelId)
    }.map(ChannelUrl(cu.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[ChannelUrl] = {
    withSQL(select.from(ChannelUrl as cu)).map(ChannelUrl(cu.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(ChannelUrl as cu)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ChannelUrl] = {
    withSQL { 
      select.from(ChannelUrl as cu).where.append(sqls"${where}")
    }.map(ChannelUrl(cu.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(ChannelUrl as cu).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    channelId: Int,
    url: String,
    createdAt: DateTime,
    updatedAt: DateTime,
    deletedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): ChannelUrl = {
    withSQL {
      insert.into(ChannelUrl).columns(
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

    ChannelUrl(
      channelId = channelId,
      url = url,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt)
  }

  def save(entity: ChannelUrl)(implicit session: DBSession = autoSession): ChannelUrl = {
    withSQL {
      update(ChannelUrl).set(
        column.channelId -> entity.channelId,
        column.url -> entity.url,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt,
        column.deletedAt -> entity.deletedAt
      ).where.eq(column.channelId, entity.channelId)
    }.update.apply()
    entity
  }
        
  def destroy(entity: ChannelUrl)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ChannelUrl).where.eq(column.channelId, entity.channelId) }.update.apply()
  }
        
}
