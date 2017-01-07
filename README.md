# akka-remote-cluster

### Get code and assembly

`git clone https://github.com/sheh/akka-remote-cluster.git`

`cd akka-remote-cluster && sbt assembly` 

(sbt 0.13.13)

### Run cluster:

`./cluster`

### Run cli: 

`./cli`

### Cli commands:

#### Run 5 nodes:

```
cluster>> add 5
NQBRvV
RwGBON
o746Ux
22oawD
47fqoh
```

#### Get performance msg/sec:
```
cluster>> perf
47fqoh 40
22oawD 40
NQBRvV 40
o746Ux 40
RwGBON 40
total perf 200.0
```

#### Delete one node:
```
cluster>> del 1
47fqoh
cluster>> ls
22oawD
NQBRvV
o746Ux
RwGBON
4 nodes 
```

#### Change interval between messages
```
cluster>> int
100
cluster>> int 200
22oawD 200
NQBRvV 200
o746Ux 200
RwGBON 200
cluster>> int
200
```
