package adlive.domain.model.channel

import adlive.domain.model.liveConverter.container.{LiveConvertContainerId, LiveConvertContainer}
import adlive.domain.model.liveDelivery.container.{LiveDeliveryContainerId, LiveDeliveryContainer}
import adlive.domain.model.liveConverter.loadBalancer.{LiveConvertLoadBalancerId, LiveConvertLoadBalancer}
import adlive.domain.support.Entity
import adlive.infrastructure.aws.ec2.Ec2Api
import adlive.infrastructure.aws.ecs.EcsApi
import adlive.infrastructure.scalikejdbc.models.generated.{Channel => DataModel, ChannelUrl, StreamUrl}
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ecs.model.CreateClusterResult
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.joda.time.DateTime
import play.api.Logger

case class Channel(
  id: ChannelId,
  name: String,
  container: LiveConvertContainer,
  loadBarancer: LiveConvertLoadBalancer,
//  deliveryContainer: LiveDeliveryContainer,
  createdAt: DateTime,
  updatedAt: DateTime,
  deletedAt: Option[DateTime] = None) extends Entity[ChannelId] {
  override val identifier: ChannelId = id

}

object Channel {
  self =>

  lazy val rtmpPort = 1935
  lazy val hlsPort = 80
  def loadBalancerPrefix(id: ChannelId) = s"LB-${id.value}-"
  def containerPrefix(id: ChannelId) = s"C-${id.value}-"
//  def deliveryContainerPrefix(id: ChannelId) = s"C-${id.value}-DELIVERY-"

  // ec2 settings
  lazy val instanceNum = 2
  lazy val amiImageId = "ami-d2b93dd2"
  lazy val instanceType = "t2.medium"
  lazy val keyPair = "adhack-adlive"
  lazy val securityGroupId = "sg-3c4d0859"
  lazy val instanceProfileArn = "arn:aws:iam::696360264248:instance-profile/ecsInstanceRole"
  def userData(clusterName: String) = {
    val data = s"#!/bin/bash\necho ECS_CLUSTER=$clusterName >> /etc/ecs/ecs.config"
    BaseEncoding.base64().encode(data.getBytes(Charsets.UTF_8))
  }

  def create(name: String) = {
    val createdAt = DateTime.now
    val channel = DataModel.create(name, createdAt, createdAt)
    val channelId = ChannelId(channel.id)

    val ecsClusterName = self.containerPrefix(channelId) + name
    EcsApi.createCluster(ecsClusterName)

    // Instance作成
    val ec2: List[Instance] = Ec2Api.create(
      amiImageId,
      instanceType,
      instanceNum,
      instanceNum,
      keyPair,
      securityGroupId,
      userData(ecsClusterName),
      instanceProfileArn
    )

    Logger.info(s"${ec2.toString}")
//    ec2.par.foreach( x => EcsApi.waitInstanceRegist(ecsClusterName))
    ec2.par.foreach( x => Ec2Api.waitRunning(x.getInstanceId))

    val lb: LiveConvertLoadBalancer = LiveConvertLoadBalancer(self.loadBalancerPrefix(channelId) + name)
    val container: LiveConvertContainer = LiveConvertContainer.create(ecsClusterName, lb)
//    val deliveryContainer = LiveDeliveryContainer(self.deliveryContainerPrefix(channelId) + ecsClusterName, lb)

    ChannelUrl.create(channelId.value, s"http://${lb.url}/hls/live.m3u8", createdAt, createdAt)
    StreamUrl.create(channelId.value, s"rtmp://${lb.url}/hls", createdAt, createdAt)
    Channel(
      channelId,
      channel.name,
      container,
      lb,
//      deliveryContainer,
      channel.createdAt,
      channel.updatedAt)
  }

  def resolveAll: List[Channel] = {
    DataModel.findAll().map(self.recordToEntity)
  }

  def resolveById(id: ChannelId): Option[Channel] = {
    DataModel.find(id.value).map(self.recordToEntity)
  }

  def deleteById(id: ChannelId) = {
    val entity: Option[Channel] = resolveById(id)
    entity.foreach { x =>
      Logger.info(self.containerPrefix(id) + x.name)
      Logger.info(self.loadBalancerPrefix(id) + x.name)
      val containerId = LiveConvertContainerId(self.containerPrefix(id) + x.name)
      val lbId = LiveConvertLoadBalancerId(self.loadBalancerPrefix(id) + x.name)

      LiveConvertContainer.deleteById(containerId)
      LiveConvertLoadBalancer.deleteById(lbId)
      DataModel.find(id.value).foreach(_.destroy)
      StreamUrl.find(id.value).foreach(_.destroy)
      ChannelUrl.find(id.value).foreach(_.destroy)
    }

  }

  def recordToEntity(record: DataModel): Channel = {
    val channelId = ChannelId(record.id)
    val lbId = LiveConvertLoadBalancerId(self.loadBalancerPrefix(channelId) + record.name)
    val cId = LiveConvertContainerId(self.containerPrefix(channelId) + record.name)
//    val lcId = LiveDeliveryContainerId(self.deliveryContainerPrefix(channelId) + record.name)
    val lb: LiveConvertLoadBalancer = LiveConvertLoadBalancer.resolveById(lbId).getOrElse(throw new RuntimeException("aaa"))
    val container: LiveConvertContainer = LiveConvertContainer.resolveById(cId).getOrElse(throw new RuntimeException("bbb"))
//    val deliveryContainer = LiveDeliveryContainer.resolveById(lcId).getOrElse(throw new RuntimeException("ccc"))

    Channel(
      id = ChannelId(record.id),
      name = record.name,
      container = container,
      loadBarancer = lb,
//      deliveryContainer = deliveryContainer,
      createdAt = record.createdAt,
      updatedAt = record.updatedAt,
      deletedAt = record.deletedAt
    )
  }

  def entityToRecord(entity: Channel): DataModel = {
    DataModel(
      id = entity.id.value,
      name = entity.name,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
      deletedAt = entity.deletedAt
    )
  }

}