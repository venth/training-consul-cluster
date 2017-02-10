# training-consul-cluster
Just a spike to learn how consul is working

## Consul on docker 

This part consists of:
* three consul servers which are clustered
* consul-aware-service along with consul agent
* consul-aware-service-consumer along with consul agent

To start consul cluster run: 

```gradle consulStart```

To stop consul cluster run: 

```gradle consulStop```

Consul http console is exposed on urls as follows:
* http://localhost:8500/ui
* http://localhost:7500/ui
* http://localhost:8500/ui

Consul service aware provides following services:
* http://localhost:8090/health - for health check purposes
* http://localhost:8090/home - returns always Hello World

Consul service aware consumer provides following services:
* http://localhost:8070/health - for health check purposes
* http://localhost:8070/called - calls Consul service aware home service, in case the service in unavailable return Service Unavailable

## Consul on AWS

Consul cluster on AWS consists of two parts:
* consul server cluster and
* consul agents deployed besides a specific service

### Consul Server Cluster on AWS

In directory ```./consul-server-cluster/aws``` there are two cloudformation templates:

* consul-permission.yaml - exports ConsulServerMemberInstanceProfile used 
    to localize consul server leader in the cluster
* consul-infrastructure.yaml - spins up consul server cluster infrastructure and 
    uses exported ConsulServerMemberInstanceProfile to lookup consul server leader
    
After creating consul server cluster ui is provided on the address as follows:

* ```http://pResourceNamePrefix.pHostedZoneName``` - where pResourceNamePrefix and pHostedZoneName are 
    cloud formation parameters values (e.g.: ```http://awesome-consul.awesome.zone```)

In order to create consul server cluster by aws cli use a bash file as follows:

```
#!/bin/bash

AWS_PROFILE=default

stack_name=awesome-consul
iam_stack_name=${stack_name}-iam
OWNER='your email'
SSH_KEY=your-ssh-key

VPC_ID=your-vpc-id
# comma separated list of your subnets
SUBNETS=subnet-00000000,subnet-00000001

# base AMI ID - needs to have yum
AMI=ami-f9619996

# your hosted zone in Route53
HOSTED_ZONE=awesome.zone.

# SSH access cidr
SSHAccessCIDR=10.0.0.0/8

# HTTP access cidr
HTTPAccessCIDR=10.0.0.0/8

if aws --profile ${AWS_PROFILE} cloudformation describe-stacks --stack-name ${iam_stack_name} &> /dev/null ; then
    echo "updates ${iam_stack_name}"
    aws --profile ${AWS_PROFILE} cloudformation update-stack \
          --stack-name=${iam_stack_name} \
          --template-body=file://$(pwd)/consul-permission.yaml \
          --capabilities CAPABILITY_NAMED_IAM CAPABILITY_IAM \
          --tags \
              Key=Owner,Value=${OWNER}
else
    echo "creates ${iam_stack_name}"
    aws --profile ${AWS_PROFILE} cloudformation create-stack \
          --stack-name=${iam_stack_name} \
          --template-body=file://$(pwd)/consul-permission.yaml \
          --capabilities CAPABILITY_NAMED_IAM CAPABILITY_IAM \
          --tags \
              Key=Owner,Value=${OWNER} && \

        aws --profile ${AWS_PROFILE} cloudformation wait stack-create-complete \
              --stack-name=${iam_stack_name} || \

        aws --profile ${AWS_PROFILE} cloudformation delete-stack \
              --stack-name=${iam_stack_name}
fi

if aws --profile ${AWS_PROFILE} cloudformation describe-stacks --stack-name ${stack_name} &> /dev/null ; then
    echo "updates ${stack_name}"
    aws --profile ${AWS_PROFILE} cloudformation update-stack \
          --stack-name=${stack_name} \
          --template-body=file://$(pwd)/consul-infrastructure.yaml \
          --parameters \
              ParameterKey=pResourceNamePrefix,ParameterValue=${stack_name} \
              ParameterKey=pKeyName,ParameterValue=${SSH_KEY} \
              ParameterKey=pDesiredCapacity,ParameterValue=3 \
              ParameterKey=pInstanceType,ParameterValue=t2.medium \
              ParameterKey=pVpcId,ParameterValue=${VPC_ID} \
              ParameterKey=pSubnetIds,ParameterValue=\"${SUBNETS}\" \
              ParameterKey=pClusterMemberAmiId,ParameterValue=${AMI} \
              ParameterKey=pHostedZoneName,ParameterValue=${HOSTED_ZONE} \
              ParameterKey=pEcsIAMStack,ParameterValue=${iam_stack_name} \
              ParameterKey=pSSHAccessCIDR,ParameterValue=${SSHAccessCIDR} \
              ParameterKey=pHTTPAccessCIDR,ParameterValue=${HTTPAccessCIDR} \
          --tags \
              Key=Owner,Value=${OWNER}
else
    echo "creates ${stack_name}"
    aws --profile ${AWS_PROFILE} cloudformation create-stack \
          --stack-name=${stack_name} \
          --template-body=file://$(pwd)/consul-infrastructure.yaml \
          --parameters \
              ParameterKey=pResourceNamePrefix,ParameterValue=${stack_name} \
              ParameterKey=pKeyName,ParameterValue=${SSH_KEY} \
              ParameterKey=pDesiredCapacity,ParameterValue=3 \
              ParameterKey=pInstanceType,ParameterValue=t2.medium \
              ParameterKey=pVpcId,ParameterValue=${VPC_ID} \
              ParameterKey=pSubnetIds,ParameterValue=\"${SUBNETS}\" \
              ParameterKey=pClusterMemberAmiId,ParameterValue=${AMI} \
              ParameterKey=pHostedZoneName,ParameterValue=${HOSTED_ZONE} \
              ParameterKey=pEcsIAMStack,ParameterValue=${iam_stack_name} \
              ParameterKey=pSSHAccessCIDR,ParameterValue=${SSHAccessCIDR} \
              ParameterKey=pHTTPAccessCIDR,ParameterValue=${HTTPAccessCIDR} \
          --tags \
          Key=Owner,Value=${OWNER} || \
    aws --profile ${AWS_PROFILE} cloudformation delete-stack \
          --stack-name=${stack_name}
fi
```
