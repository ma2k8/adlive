package adlive.domain.model.liveConverter.container

import adlive.domain.support.{EmptyIdentifier, Identifier}

/**
 * Container識別子の基底トレイト
 */
trait LiveConvertContainerId extends Identifier[String]

object LiveConvertContainerId {
  def apply(value: String) = ExistLiveConvertContainerId(value)
}

/**
 * Container識別子
 * @param value 識別子の値
 */
case class ExistLiveConvertContainerId(value: String) extends LiveConvertContainerId

/**
 * 空の識別子
 */
object EmptyLiveConvertContainerId extends EmptyIdentifier with LiveConvertContainerId
