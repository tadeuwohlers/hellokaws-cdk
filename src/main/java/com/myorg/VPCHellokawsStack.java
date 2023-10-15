package com.myorg;

import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class VPCHellokawsStack extends Stack {

    private Vpc vpc;

    public VPCHellokawsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VPCHellokawsStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "VPCHellokaws")
                .maxAzs(2)
                .natGateways(0)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
