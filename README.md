# CNVproj

Sudoku@Cloud it is a Sudoku solver application that uses AWS Cloud to implement it.


README file with the text description of the architecture and your selections
of the system configurations (auto-scaling, load balancer, etc...).


# Architecture

The architecture of our implementation of the Sudoku@Cloud is composed by the following AWS Resources:
* 1 Instance (1 <= N <= 5, 1 is the default number of AWS Instances.);
  * 1 Image (AMI of the default AWS Instance);
  * 1 Volume
* 1 Load Balancer;
* 1 Auto Scaler;
* 1 Security Group;
* 1 Auto Scaling Group;
* 1 Key Pair


Also, the Sudoku@Cloud application has a Frontend developed by the professors and it is not the focus of this project.
The Frontend is a website where you can choose a sudoku to be solved, from a predefined set of sudokus, and then it sends an HTTP Request to the Webserver that is located in our Instance.



# System Configuration

## Instances

### Number of Instances

The number of instances is determined by the Load Balancer. More detailed specifications in the [System Configuration >Load Balancer](##Load-Balancer) section.


### Webserver

The WebServer can receive more than 1 request concurrently, since it has a thread pool that handles concurrent HTTP Requests. When the Instance gets a lot or too few HTTP Requests, i.e., the workload increases or decreases a certain amount of HTTP Requests, the Auto Scaler starts and stops Instances. This specifications/system configurations are more detailed in the System Configuration >Auto Scaler section.

The Webserver is automaticly started when the Instance is started. For that we did the following steps:

1) Connected to the instance using SSH;
2) Open the file /etc/rc.local;
3) Changed the Java Options, the Classpath and runned the Webserver;

The final changes were the following:
```
export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
export CLASSPATH=%CLASSPATH%:/home/ec2-user/cnv/instrumented:/home/ec2-user/cnv/project:/home/ec2-user/cnv/BIT:/home/ec2-user/cnv/BIT/samples:.

java pt.ulisboa.tecnico.cnv.server.WebServer
```


### Instrumented XXX

To generate the WebServer with instrumented code, we used the samples from the BIT tool, to create our own instrumented tool in the WebServer.



## Load Balancer

### Cost Function

The Load Balancer has a cost function that is used to send the HTTP Requests to the appropriate Instance. The cost function is already defined in the report but not currently implemented.


### Specifications XXX
criamos uma imagem onde so colocoamos o nome e o resto foi default.
created an image, all default except the name, "proj"

Load Balancer:
Load Balancer port <-> Instance Port
HTTP : 80            <-> HTTP : 8000
security group
STEP 5: no image
all default.

## Auto Scaler XXX

### 


## Security Group

The security Group is used in the Instances, the Load Balancer and the Auto Scaler.


## Auto Scaling Group

The Auto Scaling Group is used in the Auto Scaler.


## Key Pair XXX

The Key Pair is used in the to connect to the Instances, 



# Metrics

The metrics are generated by our WebServer that runs an instrumented code, as explain in the System Configuration >Instances >Instrumented section.
These metrics data being stored in a temporary file, tempBD.txt, located in the root folder of our instance. This is a temporary method
used by us, to save the metrics data. In the near future, we will use the DynamoDB Service given by the AWS, to store our metrics data.

 






