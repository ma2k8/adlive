package adlive.domain.model.liveDelivery.container

import adlive.domain.support.Entity
import adlive.infrastructure.aws.ec2.Ec2Api
import adlive.infrastructure.aws.ecs.EcsApi
import com.amazonaws.services.ecs.model._

import scalaz.Scalaz._
import adlive.domain.model.liveConverter.loadBalancer.LiveConvertLoadBalancer

case class LiveDeliveryContainer(
  id: LiveDeliveryContainerId,
  instanceCount: Int,
  runningTaskCount: Int,
  pendingTasksCount: Int,
  activeServicesCount: Int,
  status: String) extends Entity[LiveDeliveryContainerId] {
  override val identifier: LiveDeliveryContainerId = id

  lazy val services: List[Service] = EcsApi.findAllService(id.value)
  lazy val tasks: List[Task] = EcsApi.findAllTask(id.value)
}

object LiveDeliveryContainer {
  self =>

  // fixme: 名前は仮で入れています。
  lazy val taskDefinitionArn = "arn:aws:ecs:ap-northeast-1:696360264248:task-definition/nginx-hls:3"
  lazy val containerName = "nginx-hls"
  lazy val servicePrefix = "service-"
  lazy val ruleArn = "arn:aws:iam::696360264248:role/ecsServiceRole"
  lazy val desiredCount = 1
  lazy val defaultTaskCount = 2

  def apply(name: String, lb: LiveConvertLoadBalancer): LiveDeliveryContainer = {
    val id = LiveDeliveryContainerId(name)
    resolveById(id) match {
      case Some(x) => x
      case _ => self.create(name, lb)
    }
  }

  def resolveAll(): List[LiveDeliveryContainer] = {
    EcsApi.findAllCluster().map(self.clusterObjToEntity)
  }

  def resolveById(id: LiveDeliveryContainerId): Option[LiveDeliveryContainer] = {
    EcsApi.findCluster(id.value).map(self.clusterObjToEntity)
  }

  def deleteById(id: LiveDeliveryContainerId): Unit = {
    EcsApi.findAllTaskArn(id.value).map( x => EcsApi.stopTask(id.value, x))
    val arns = EcsApi.findAllInstanceArnByCluster(id.value)
    EcsApi.findAllInstanceIdByClusterAndArn(id.value, arns).map(Ec2Api.delete)
    arns.map(x=>EcsApi.deRegisterInstance(id.value, x))
    EcsApi.deleteService(servicePrefix + id.value, id.value)
    EcsApi.deleteCluster(id.value)
  }

  def create(name: String, lb: LiveConvertLoadBalancer) = {
    val clusterResult: CreateClusterResult = EcsApi.createCluster(name)

    EcsApi.createService(
      servicePrefix + name,
      name,
      lb.toHlsLoadBalancer(containerName).pure[List],
      desiredCount,
      taskDefinitionArn,
      ruleArn)

    EcsApi.runTask(
      name,
      taskDefinitionArn,
      defaultTaskCount
    )

    resolveById(LiveDeliveryContainerId(name)).get
  }

  def clusterObjToEntity(cluster: Cluster) = {
    LiveDeliveryContainer(
      id = LiveDeliveryContainerId(cluster.getClusterName),
      instanceCount = cluster.getRegisteredContainerInstancesCount,
      runningTaskCount = cluster.getRunningTasksCount,
      pendingTasksCount = cluster.getPendingTasksCount,
      activeServicesCount = cluster.getActiveServicesCount,
      status = cluster.getStatus
    )
  }

  def createResultToEntity(result: CreateClusterResult): LiveDeliveryContainer = {
    val cluster: Cluster = result.getCluster
    self.clusterObjToEntity(cluster)
  }

}