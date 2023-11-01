package com.myorg;

import software.amazon.awscdk.App;

public class HellokawsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        VPCHellokawsStack vpcStack = new VPCHellokawsStack(app, "VPCHellokaws");

        RDSStack rdsStack = new RDSStack(app, "RDSHellokaws", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        ClusterHellokawsStack clusterStack = new ClusterHellokawsStack(app, "ClusterHellokaws", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        Service01Stack service01Stack = new Service01Stack(app, "ServiceHellokaws", clusterStack.getCluster());
        service01Stack.addDependency(clusterStack);

        app.synth();
    }
}

//cdk deploy --parameters RDSHellokaws:DatabasePassword=hi8dfFDSA$#! RDSHellokaws 