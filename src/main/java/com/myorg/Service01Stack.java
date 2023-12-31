package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerLookupOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.List;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Role taskExecutionRole = Role.Builder.create(this, "TaskExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        taskExecutionRole
        .attachInlinePolicy(Policy.Builder.create(this, "TaskExecutionPolicy")
                        .policyName("TaskExecutionPolicy")
                        .statements(List.of(
                                PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .actions(List.of(
                                                "ecr:*",
                                                "logs:CreateLogStream",
                                                "logs:PutLogEvents"))
                                        .resources(List.of("*"))
                                        .build()))
                        .build());

        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService
                .Builder.create(this, "ALB01")
                .serviceName("ServiceHellokaws")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .runtimePlatform(
                        RuntimePlatform.builder()
                                .cpuArchitecture(CpuArchitecture.X86_64)
                                .operatingSystemFamily(OperatingSystemFamily.LINUX)
                                .build()
                )
                .desiredCount(1)
                .listenerPort(80)
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("Hellokaws")
                                .image(ContainerImage.fromRegistry("912656312251.dkr.ecr.us-east-1.amazonaws.com/helloaws:latest"))
                                .containerPort(80)
                                .executionRole(taskExecutionRole)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                                .logGroup(LogGroup.Builder.create(this, "ServiceHellokawsLogGroup")
                                                        .logGroupName("ServiceHellokaws")
                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                        .build())
                                                .streamPrefix("ServiceHellokaws")
                                        .build()))
                        .build())
                .publicLoadBalancer(true)
                .build();

        

        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("80")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

    }
}
