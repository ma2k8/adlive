package adlive.infrastructure.aws.ecs

import awscala.ec2.Instance
import com.amazonaws.services.ecs.AmazonECSClient
import com.amazonaws.services.ecs.model._
import com.amazonaws.services.ecs.model.Resource
import adlive.infrastructure.aws.MyCredentials.credentials
import play.api.{Logger, Configuration, Play}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

import scalaz.Scalaz._

/**
 * AWS ECS API
 * DOC: http://docs.aws.amazon.com/AmazonECS/latest/APIReference/Welcome.html
 * ※ 一旦describe系のメソッドは実装してません
 */
object EcsApi {

  def conf: Configuration = Play.current.configuration

  val az: Seq[String] = conf.getStringSeq("aws.default.az").getOrElse(throw new RuntimeException("no such availability zone"))
  val endPoint = conf.getString("aws.default.endpoint.ecs").getOrElse(throw new RuntimeException("no such ecs endpoint"))
  val client = new AmazonECSClient(credentials).withEndpoint[AmazonECSClient](endPoint)

  /** Container Cluster */

  /**
   * アカウントに紐づくECS Clusterを全て索引
   * @return
   */
  def findAllCluster(): List[Cluster] = {
    val arns: List[String] = client.listClusters().getClusterArns.toList
    val request = new DescribeClustersRequest().withClusters(arns: _*)
    client.describeClusters(request).getClusters.toList
  }

  /**
   * ECS Clusterを名前で索引
   * @param name ecs name
   * @return
   */
  def findCluster(name: String): Option[Cluster] = {
    val clusters = findAllCluster()
    clusters.find(_.getClusterName == name)
  }

  /**
   * クラスタにインスタンスが所属するまでを監視する
   */
  @tailrec
  def waitInstanceRegist(name: String): Unit = {
    findCluster(name) match {
      case Some(x) if x.getRegisteredContainerInstancesCount > 0 =>
        Logger.info("regist succuessful.")
      case Some(x) if x.getRegisteredContainerInstancesCount == 0 =>
        Logger.info("waiting regist.")
        Thread.sleep(1000)
        waitInstanceRegist(name)
      case _ =>  Logger.info("Ops.")
    }

  }

  /**
   * ECS Clusterを作成する
   * @param name ecs name
   * @return
   */
  def createCluster(name: String): CreateClusterResult = {
    val request: CreateClusterRequest =
      new CreateClusterRequest()
        .withClusterName(name)

    client.createCluster(request)
  }

  /**
   * ECS Clusterを削除する
   * @param name ecs name
   */
  def deleteCluster(name: String): DeleteClusterResult = {
    val request: DeleteClusterRequest =
      new DeleteClusterRequest()
        .withCluster(name) // name??

    client.deleteCluster(request)
  }

  /** Container Service */

  /**
   * クラスタに紐づくESC Serviceを索引
   * @param clusterName ecs cluster name
   * @return
   */
  def findAllService(clusterName: String): List[Service] = {
    val request: DescribeServicesRequest =
      new DescribeServicesRequest()
        .withCluster(clusterName)

    client.describeServices(request).getServices.toList
  }

  /**
   * クラスタに紐づくECS Serviceを名前で索引
   * @param clusterName ecs cluster name
   * @param name esc service name
   * @return
   */
  def findService(clusterName: String, name: String): Option[Service] = findServices(clusterName, name.pure[List]).headOption
  def findServices(clusterName: String, names: List[String]): List[Service] = {
    val request =
      new DescribeServicesRequest()
        .withCluster(clusterName)
        .withServices(names: _*)

    client.describeServices(request).getServices.toList
  }

  /**
   * ECS Clusterに属するServiceを作成する
   * @param serviceName service name
   * @param clusterName ecs cluster name
   * @param loadBalancers elb
   * @param desiredCount instance count
   * @param taskDefinitionArn task definition arn
   * @param ruleArn rule arn
   * @return
   */
  def createService(
    serviceName: String,
    clusterName: String,
    loadBalancers: List[LoadBalancer],
    desiredCount: Int,
    taskDefinitionArn: String,
    ruleArn: String): CreateServiceResult = {
    val request: CreateServiceRequest =
      new CreateServiceRequest()
        .withServiceName(serviceName)
        .withCluster(clusterName)
        .withLoadBalancers(loadBalancers: _*)
        .withDesiredCount(desiredCount)
        .withTaskDefinition(taskDefinitionArn)
        .withRole(ruleArn)

    client.createService(request)
  }

  def updateService(serviceName: String, clusterName: String, desiredCount: Int) = {
    val request =
      new UpdateServiceRequest()
        .withCluster(clusterName)
        .withService(serviceName)
        .withDesiredCount(desiredCount)

    client.updateService(request)
  }

  /**
   * ECS Clusterに属するServiceを削除する
   * @param serviceName service name
   * @param clusterName ecs cluster name
   * @return
   */
  def deleteService(serviceName: String, clusterName: String): DeleteServiceResult = {
    updateService(serviceName, clusterName, 0)
    val request: DeleteServiceRequest =
      new DeleteServiceRequest()
        .withCluster(clusterName)
        .withService(serviceName)

    client.deleteService(request)
  }

  /** Task */

  def findAllTaskArn(clusterName: String): List[String] = {
    val request =
      new ListTasksRequest()
        .withCluster(clusterName)

    client.listTasks(request).getTaskArns.toList
  }

  def findAllTask(clusterName: String): List[Task] = {
    val taskArns: List[String] = findAllTaskArn(clusterName)
    val request =
      new DescribeTasksRequest()
        .withCluster(clusterName)
        .withTasks(taskArns: _*)

    client.describeTasks(request).getTasks.toList
  }

  def findTask(clusterName: String, taskArn: String): Option[Task] = findTasks(clusterName, taskArn.pure[List]).headOption
  def findTasks(clusterName: String, taskArn: List[String]): List[Task] = {
    val request =
      new DescribeTasksRequest()
        .withCluster(clusterName)
        .withTasks(taskArn: _*)

    client.describeTasks(request).getTasks.toList
  }

  /**
   * タスクを起動する
   * @param clusterName ecs cluster name
   * @param taskDefinitionArn ecs task name
   * @param taskCount number of task
   * @return
   */
  def runTask(clusterName: String, taskDefinitionArn: String, taskCount: Int): RunTaskResult = {
    val request: RunTaskRequest =
      new RunTaskRequest()
        .withCluster(clusterName)
        .withTaskDefinition(taskDefinitionArn)
        .withCount(taskCount)
//        .withOverrides()
//        .withStartedBy()

    client.runTask(request)
  }

  /**
   * タスクを停止する
   * @param clusterName esc cluster name
   * @param taskArn task aws arn
   * @return
   */
  def stopTask(clusterName: String, taskArn: String): StopTaskResult = {
    val request =
      new StopTaskRequest()
        .withCluster(clusterName)
        .withTask(taskArn)

    client.stopTask(request)
  }

  /** Task Definition */

  def findAllTaskDefinitions(clusterName: String): List[String] = {
    val request: ListTaskDefinitionsRequest = new ListTaskDefinitionsRequest()

    client.listTaskDefinitions(request).getTaskDefinitionArns.toList
  }

  /**
   * Task定義を作成する
   * @param name task name
   * @param containerDefinition コンテナ情報
   * @param volume コンテナ間でのデータ共有用ボリューム
   */
  def createTaskDefinition(name: String, containerDefinition: ContainerDefinition, volume: Volume) = createTaskDefinitions(name, containerDefinition.pure[List], volume.pure[List])
  def createTaskDefinitions(name: String, containerDefinitions: List[ContainerDefinition], volumes: List[Volume]) = {
    val request: RegisterTaskDefinitionRequest =
      new RegisterTaskDefinitionRequest()
        .withFamily(name)
        .withContainerDefinitions(containerDefinitions: _*)
        .withVolumes(volumes: _*)

    client.registerTaskDefinition(request)
  }

  /**
   * Task定義を削除する
   * @param name task name
   */
  def deleteTaskDefinition(name: String) = {
    val request: DeregisterTaskDefinitionRequest =
      new DeregisterTaskDefinitionRequest()
        .withTaskDefinition(name)

    client.deregisterTaskDefinition(request)
  }

  /** Container Instance */

  /**
   * 対象のクラスタに属するEC2インスタンスを取得
   * @param clusterName ec2 container cluster name
   * @return
   */
  def findAllInstanceArnByCluster(clusterName: String): List[String] = {
    val request: ListContainerInstancesRequest =
      new ListContainerInstancesRequest()
        .withCluster(clusterName)

    val result = client.listContainerInstances(request)
    result.getContainerInstanceArns.asScala.toList
  }

  def findAllInstanceIdByClusterAndArn(clusterName: String, arns: List[String]) = {
    val request =
      new DescribeContainerInstancesRequest()
        .withCluster(clusterName)
        .withContainerInstances(arns: _*)

    client.describeContainerInstances(request).getContainerInstances.map(_.getEc2InstanceId)
  }

  def deRegisterInstance(clusterName: String, arn: String) = {
    val request =
      new DeregisterContainerInstanceRequest()
        .withCluster(clusterName)
        .withContainerInstance(arn)

    client.deregisterContainerInstance(request)
  }

//
//  def addInstance(cluster: Cluster, instance: Instance) = addInstances(cluster, List(instance))
//  def addInstances(cluster: Cluster, instances: List[Instance]) = {
//
//    val request: RegisterContainerInstanceRequest =
//      new RegisterContainerInstanceRequest()
//        .withCluster(clusterName)
//        .withInstanceIdentityDocument(id)
//        //        .withInstanceIdentityDocumentSignature("Q/McoXuViJxI2ZVzrlPDOAhT8xg1I2gTFUCz6Gtfhqg+m4Xo/bxe1afn3PS8QYpTvCvIrHCsb/Xg\npJ3ynGL+OwlYD4xLnkXYGSY2OhvpG1YXeIPY+84S9AQO35rEiWozL02ofUEldfUa3jTVeDErd9gn\nz7O+dT0esvKczidiSCQ=")
//        .withTotalResources(resources: _*)
//    //        .withContainerInstanceArn("arn:aws:ec2:ap-northeast-1:696360264248:instance/i-f5b32e07")
//    //        .withContainerInstanceArn("arn:aws:ecs:ap-northeast-1:696360264248:container-instance/d9ca7ebc-4fcc-4e80-934c-e696ed6ec6d2")
//
//    client.registerContainerInstance(request)
//  }

}
