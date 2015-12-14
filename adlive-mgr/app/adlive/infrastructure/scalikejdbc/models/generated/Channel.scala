package adlive.infrastructure.scalikejdbc.models.generated

import scalikejdbc._
import org.joda.time.{DateTime}

case class Channel(
  id: Int, 
  name: String, 
  createdAt: DateTime, 
  updatedAt: DateTime, 
  deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Channel.autoSession): Channel = Channel.save(this)(session)

  def destroy()(implicit session: DBSession = Channel.autoSession): Unit = Channel.destroy(this)(session)

}
      

object Channel extends SQLSyntaxSupport[Channel] {

  override val tableName = "channel"

  override val columns = Seq("id", "name", "created_at", "updated_at", "deleted_at")

  def apply(c: SyntaxProvider[Channel])(rs: WrappedResultSet): Channel = apply(c.resultName)(rs)
  def apply(c: ResultName[Channel])(rs: WrappedResultSet): Channel = new Channel(
    id = rs.get(c.id),
    name = rs.get(c.name),
    createdAt = rs.get(c.createdAt),
    updatedAt = rs.get(c.updatedAt),
    deletedAt = rs.get(c.deletedAt)
  )
      
  val c = Channel.syntax("c")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Channel] = {
    withSQL {
      select.from(Channel as c).where.eq(c.id, id)
    }.map(Channel(c.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Channel] = {
    withSQL(select.from(Channel as c)).map(Channel(c.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Channel as c)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Channel] = {
    withSQL { 
      select.from(Channel as c).where.append(sqls"${where}")
    }.map(Channel(c.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Channel as c).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    name: String,
    createdAt: DateTime,
    updatedAt: DateTime,
    deletedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): Channel = {
    val generatedKey = withSQL {
      insert.into(Channel).columns(
        column.name,
        column.createdAt,
        column.updatedAt,
        column.deletedAt
      ).values(
        name,
        createdAt,
        updatedAt,
        deletedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Channel(
      id = generatedKey.toInt, 
      name = name,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt)
  }

  def save(entity: Channel)(implicit session: DBSession = autoSession): Channel = {
    withSQL {
      update(Channel).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt,
        column.deletedAt -> entity.deletedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Channel)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Channel).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
