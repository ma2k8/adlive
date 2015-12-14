package adlive.infrastructure.aws.elb

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient
import com.amazonaws.services.elasticloadbalancing.model._
import adlive.infrastructure.aws.MyCredentials.credentials
import play.api.{Configuration, Play}

import scala.collection.JavaConverters._

import scalaz.Scalaz._

/**
 * AWS ELB API
 * DOC: http://docs.aws.amazon.com/ElasticLoadBalancing/latest/APIReference/Welcome.html
 * ※ 一旦describe系のメソッドは実装してません
 */
object ElbApi {

  def conf: Configuration = Play.current.configuration

//  val defaultSecurityGroupId = "sg-f3dea696"
//  val defaultSubnetIds = List("subnet-7b80000c", "subnet-981bc0c1")
//  val defaultScheme = "internet-facing"
  val defaultAz: Seq[String] = conf.getStringSeq("aws.default.az").getOrElse(throw new RuntimeException("no such availability zone"))
  val endPoint: String = conf.getString("aws.default.endpoint.elb").getOrElse(throw new RuntimeException("no such elb endpoint"))
  var client: AmazonElasticLoadBalancingClient = new AmazonElasticLoadBalancingClient(credentials).withEndpoint[AmazonElasticLoadBalancingClient](endPoint)

  /**
   * アカウントに紐づくELBを全て索引
   * @return
   */
  def findAll(): List[LoadBalancerDescription] = {
    val result: DescribeLoadBalancersResult = client.describeLoadBalancers
    result.getLoadBalancerDescriptions.asScala.toList
  }

  /**
   * ELBを名前指定で取得
   * @param name elb name
   * @return
   */
  def find(name: String): Option[LoadBalancerDescription] = {
    val elbList = findAll()
    elbList.find(_.getLoadBalancerName == name)
  }

  /**
   * ELBを作成する
   * @param name elb name
   * @param listener port listeners
   * @return
   */
  def createElb(
    name: String,
    listener: Listener,
    securityGroupId: String,
    schema: String): CreateLoadBalancerResult = createElb(name, listener.pure[List], securityGroupId.pure[List], schema)

  def createElb(
    name: String,
    listeners: List[Listener],
    securityGroupIds: List[String],
    schema: String,
    az: Seq[String] = defaultAz): CreateLoadBalancerResult = {

    val request: CreateLoadBalancerRequest =
      new CreateLoadBalancerRequest()
        .withLoadBalancerName(name)
        .withAvailabilityZones(az: _*)
        .withSecurityGroups(securityGroupIds: _*)
        .withScheme(schema)
        .withListeners(listeners: _*)

    client.createLoadBalancer(request)
  }

  /**
   * ELBを削除する
   * @param name elb name
   */
  def deleteElb(name: String): Unit = {
    val request: DeleteLoadBalancerRequest =
      new DeleteLoadBalancerRequest()
        .withLoadBalancerName(name)

    client.deleteLoadBalancer(request)
  }

  /**
   * ELBへlistenerを追加する
   * @param name elb name
   * @param listener port listener
   */
  def addListener(name: String, listener: Listener): Unit = addListeners(name, listener.pure[List])
  def addListeners(name: String, listeners: List[Listener]): Unit = {
    val request =
      new CreateLoadBalancerListenersRequest()
        .withLoadBalancerName(name)
        .withListeners(listeners: _*)

    client.createLoadBalancerListeners(request)
  }

  /**
   * ELBからlistenerを削除する
   * @param name elb name
   * @param elbPort elb port number
   */
  def deleteListener(name: String, elbPort: Integer) = deleteListeners(name, elbPort.pure[List])
  def deleteListener(name: String, listener: Listener) = deleteListeners(name, listener.getLoadBalancerPort.pure[List])
  def deleteListeners(name: String, elbPorts: List[Integer]) = {
    val request =
      new DeleteLoadBalancerListenersRequest()
        .withLoadBalancerName(name)
        .withLoadBalancerPorts(elbPorts: _*)

    client.deleteLoadBalancerListeners(request)
  }

  /**
   * ELBへec2インスタンスを登録する
   * @param name elb name
   * @param instance ec2 instance
   */
  def registerInstance(name: String, instance: Instance): RegisterInstancesWithLoadBalancerResult = registerInstances(name, instance.pure[List])
  def registerInstance(name: String, instanceId: String): RegisterInstancesWithLoadBalancerResult = registerInstances(name, new Instance(instanceId).pure[List])
  def registerInstances(name: String, instances: List[Instance]): RegisterInstancesWithLoadBalancerResult = {
    val request =
      new RegisterInstancesWithLoadBalancerRequest()
        .withLoadBalancerName(name)
        .withInstances(instances: _*)

    client.registerInstancesWithLoadBalancer(request)
  }

  /**
   * ELBからec2インスタンスを外す
   * @param name elb name
   * @param instance ec2 instance
   * @return
   */
  def deRegisterInstance(name: String, instance: Instance) = deRegisterInstances(name, instance.pure[List])
  def deRegisterInstances(name: String, instances: List[Instance]) = {
    val request =
      new DeregisterInstancesFromLoadBalancerRequest()
        .withLoadBalancerName(name)
        .withInstances(instances: _*)

    client.deregisterInstancesFromLoadBalancer(request)
  }

}

