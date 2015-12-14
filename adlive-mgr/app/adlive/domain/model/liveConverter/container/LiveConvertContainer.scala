package adlive.domain.model.liveConverter.container

import adlive.domain.model.liveConverter.loadBalancer.LiveConvertLoadBalancer
import adlive.domain.support.Entity
import adlive.infrastructure.aws.ec2.Ec2Api
import adlive.infrastructure.aws.ecs.EcsApi
import com.amazonaws.services.ecs.model._
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding

import scalaz.Scalaz._

case class LiveConvertContainer(
  id: LiveConvertContainerId,
  instanceCount: Int,
  runningTaskCount: Int,
  pendingTasksCount: Int,
  activeServicesCount: Int,
  status: String) extends Entity[LiveConvertContainerId] {
  override val identifier: LiveConvertContainerId = id

  lazy val services: List[Service] = EcsApi.findAllService(id.value)
  lazy val tasks: List[Task] = EcsApi.findAllTask(id.value)
//  lazy val instances =
}

object LiveConvertContainer {
  self =>

  lazy val taskDefinitionArn = "arn:aws:ecs:ap-northeast-1:696360264248:task-definition/nginx-rtmp"
  lazy val containerName = "nginx-rtmp"
  lazy val servicePrefix = "service-"
  lazy val ruleArn = "arn:aws:iam::696360264248:role/ecsServiceRole"
  lazy val desiredCount = 1
  lazy val defaultTaskCount = 1

  def apply(name: String, lb: LiveConvertLoadBalancer): LiveConvertContainer = {
    val id = LiveConvertContainerId(name)
    resolveById(id) match {
      case Some(x) => x
      case _ => self.create(name, lb)
    }
  }

  def resolveAll(): List[LiveConvertContainer] = {
    EcsApi.findAllCluster().map(self.clusterObjToEntity)
  }

  def resolveById(id: LiveConvertContainerId): Option[LiveConvertContainer] = {
    EcsApi.findCluster(id.value).map(self.clusterObjToEntity)
  }

  def deleteById(id: LiveConvertContainerId): Unit = {
    EcsApi.findAllTaskArn(id.value).map( x => EcsApi.stopTask(id.value, x))
    val arns = EcsApi.findAllInstanceArnByCluster(id.value)
    EcsApi.findAllInstanceIdByClusterAndArn(id.value, arns).map(Ec2Api.delete)
    arns.map(x=>EcsApi.deRegisterInstance(id.value, x))
    EcsApi.deleteService(servicePrefix + id.value, id.value)
    EcsApi.deleteCluster(id.value)
  }

  def create(name: String, lb: LiveConvertLoadBalancer): LiveConvertContainer = {

    EcsApi.createService(
      servicePrefix + name + "rtmp",
      name,
      lb.toEcsLoadBalancer(containerName).pure[List],
      desiredCount,
      taskDefinitionArn,
      ruleArn)

    EcsApi.createService(
      servicePrefix + name + "http",
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

    resolveById(LiveConvertContainerId(name)).get
  }

  def clusterObjToEntity(cluster: Cluster) = {
    LiveConvertContainer(
      id = LiveConvertContainerId(cluster.getClusterName),
      instanceCount = cluster.getRegisteredContainerInstancesCount,
      runningTaskCount = cluster.getRunningTasksCount,
      pendingTasksCount = cluster.getPendingTasksCount,
      activeServicesCount = cluster.getActiveServicesCount,
      status = cluster.getStatus
    )
  }

  def createResultToEntity(result: CreateClusterResult): LiveConvertContainer = {
    val cluster: Cluster = result.getCluster
    self.clusterObjToEntity(cluster)
  }

}