package adlive.infrastructure.aws.ec2

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model._
import adlive.infrastructure.aws.MyCredentials.credentials
import com.amazonaws.services.opsworks.model.StopInstanceRequest
import play.api.{Logger, Configuration, Play}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * AWS EC2 API
 * DOC: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html
 */
object Ec2Api {

  def conf: Configuration = Play.current.configuration

  val defaultAz: Seq[String] = conf.getStringSeq("aws.default.az").getOrElse(throw new RuntimeException("no such availability zone"))
  val endPoint: String = conf.getString("aws.default.endpoint.ec2").getOrElse(throw new RuntimeException("no such elb endpoint"))
  var client = new AmazonEC2Client(credentials).withEndpoint[AmazonEC2Client](endPoint)

  def create(
    imageId: String,
    instanceType: String,
    minCount: Int,
    maxCount: Int,
    keyName: String,
    securityGroup: String,
    userData: String,
    instanceProfileArn: String): List[Instance] = {
    val request: RunInstancesRequest =
      new RunInstancesRequest()
        .withImageId(imageId)
        .withInstanceType(instanceType)
        .withMinCount(minCount)
        .withMaxCount(maxCount)
        .withKeyName(keyName)
        .withSecurityGroupIds(securityGroup)
        .withUserData(userData)
        .withIamInstanceProfile(new IamInstanceProfileSpecification().withArn(instanceProfileArn))

    client.runInstances(request).getReservation.getInstances.toList
  }

  @tailrec
  def waitRunning(instanceId: String): Boolean = {
    Thread.sleep(3000)
    getStatus(instanceId) match {
      case Some(x) if checkStatus(x) == "OK" => true
      case Some(x) if checkStatus(x) == "PENDING" => waitRunning(instanceId)
      case Some(x) if checkStatus(x) == "FAILED" => throw new RuntimeException(s"ec2 instance launch failed. status[${x.toString}]")
      case None => waitRunning(instanceId) // たまに取れない模様
      case _ => throw new RuntimeException("ec2 instance launch failed. status[]")
    }
  }

  def checkStatus(status: InstanceStatus): String = {
    val instanceState = status.getInstanceState.getName
    val systemState = status.getSystemStatus.getStatus
    Logger.info(s"InstanceState:[$instanceState] SystemState[$systemState]")

    (instanceState, systemState) match {
      case ("running", "ok") => "OK"
      case ("running", "initializing") => "PENDING"
      case ("pending", _) => "PENDING"
      case _ => "FAILED"
    }
  }

  def getStatus(instanceId: String): Option[InstanceStatus] = {
    val request: DescribeInstanceStatusRequest =
      new DescribeInstanceStatusRequest()
        .withInstanceIds(instanceId)

    client.describeInstanceStatus(request).getInstanceStatuses.headOption
  }

  def delete(instanceId: String): Option[InstanceStateChange] = {
    val request: TerminateInstancesRequest =
      new TerminateInstancesRequest()
        .withInstanceIds(instanceId)

    client.terminateInstances(request).getTerminatingInstances.headOption
  }

}
