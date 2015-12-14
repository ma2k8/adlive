package adlive.domain.model.channel

import adlive.domain.support.{EmptyIdentifier, Identifier}

/**
 * Channel識別子の基底トレイト
 */
trait ChannelId extends Identifier[Int]

object ChannelId {
  def apply(value: Int) = ExistChannelId(value)
}

/**
 * Channel識別子
 * @param value 識別子の値
 */
case class ExistChannelId(value: Int) extends ChannelId

/**
 * 空の識別子
 */
object EmptyChannelId extends EmptyIdentifier with ChannelId
