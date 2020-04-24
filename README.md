# CNVproj

Sudoku@Cloud it is a Sudoku solver application that uses AWS Cloud to implement it.


# Architecture

The architecture of our implementation of the Sudoku@Cloud is composed by the following AWS Resources:
* 1 Instance (1 <= N <= 5, 1 is the default number of AWS Instances.);
  * 1 Image (AMI of the default AWS Instance);
  * 1 Volume
* 1 Load Balancer;
* 1 Auto Scaling Group (Auto-Scaler);
* 1 Security Group;
* 1 Key Pair


Also, the Sudoku@Cloud application has a Frontend developed by the professors and it is not the focus of this project.
The Frontend is a website where you can choose a sudoku to be solved, from a predefined set of sudokus, and then it sends an HTTP Request to the Webserver that is located in our Instance.


Note: All our AWS Resources are in the region "us-east-1a".


# System Configuration

## Instances

### Number of Instances

The number of instances is determined by the Load Balancer. More detailed specifications in the [System Configuration >Load Balancer](##Load-Balancer) section.


### Webserver

The WebServer can receive more than 1 request concurrently, since it has a thread pool that handles concurrent HTTP Requests. When the Instance gets a lot or too few HTTP Requests, i.e., the workload increases or decreases a certain amount of HTTP Requests, the Auto Scaler starts and stops Instances. This specifications/system configurations are more detailed in the [System Configuration >Auto Scaler]() section.

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
We also used the command `systemctl status rc-local.service` to help us debbug a few problems that happen during this proccess.


## Load Balancer

### Cost Function

The Load Balancer has a cost function that is used to send the HTTP Requests to the appropriate Instance. The cost function is already defined in the report but not currently implemented.


### Specifications

| Port Configuration | Idle Timeout |
| ---------------- | ---------------- |
| 80 (HTTP) <-> 8000 (HTTP) | 60 seconds |


## Auto-Scaler

### Specification

| Desired Capacity | Minimum Capacity |	Maximum Capacity | Health Check Timer | Default Cooldown |
| ---------------- | ---------------- | ---------------- | ------------------ | ---------------- |
| 1                | 1                | 5                | 100 seconds        | 300 seconds      |

The Desired and Minimum Capacity is the default because we do not want to waste credits unnecessarily.
The maximum capacity can be adjusted depending on the predicted income of HTTP Requests in the future. We chose 5 for testing porposes.
We increased the Health Check Timer from 60 seconds (default) to 100 seconds because we do not want to overload the Health Checker.
To prevent the Auto Scaler to start and stop Instances very frequently, we opted to make Default Cooldown be 300 seconds.


#### Decrease Group Size

| Policy Type  | Policy (CPU Utilization) |	Action |
| ------------ | ----------------         | ---------------- |
| Step Scaling | <20% for 60 seconds      | Remove 1 Instance                |


#### Increase Group Size 

| Policy Type  | Policy (CPU Utilization) |	Action |
| ------------ | ----------------         | ---------------- |
| Step Scaling | >80% for 60 seconds      | Add 1 Instance                |


## Security Group

The security Group is used in the Instances, the Load Balancer and the Auto Scaler.


### Specifications

| Type	     | Protocol |	Port range | Source    | Description - optional |
| ---------- | -------- | ---------- | --------- | ---------------------- |
| HTTP	     | TCP	    | 80	       | 0.0.0.0/0 | -                      |
| Custom TCP | TCP      |	8000	     | 0.0.0.0/0 | -                      |
| SSH	       | TCP	    | 22	       | 0.0.0.0/0 | -                      |


## Key Pair

The Key Pair is used to connect to the Instances.
It is a default key pair created in AWS.



# Data Base

The metrics are generated by our WebServer that runs an instrumented code carefully design by us.
The metrics data is being stored in a temporary file, tempBD.txt, located in the root folder of our Instance. This is a temporary method used by us, to save the metrics data. In the near future, we will use the DynamoDB Service given by the AWS, to store our metrics data.

