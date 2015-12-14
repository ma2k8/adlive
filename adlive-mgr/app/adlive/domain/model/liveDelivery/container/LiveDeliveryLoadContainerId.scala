package adlive.domain.model.liveDelivery.container

import adlive.domain.support.{EmptyIdentifier, Identifier}

/**
 * Container識別子の基底トレイト
 */
trait LiveDeliveryContainerId extends Identifier[String]

object LiveDeliveryContainerId {
  def apply(value: String) = ExistLiveDeliveryContainerId(value)
}

/**
 * Container識別子
 * @param value 識別子の値
 */
case class ExistLiveDeliveryContainerId(value: String) extends LiveDeliveryContainerId

/**
 * 空の識別子
 */
object EmptyLiveDeliveryContainerId extends EmptyIdentifier with LiveDeliveryContainerId
