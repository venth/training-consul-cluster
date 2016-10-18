# training-consul-cluster
Just a spike to learn how consul is working

## Consul

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
* http://localhost:8090/home - returns always Hellow World

Consul service aware consumer provides following services:
* http://localhost:8090/health - for health check purposes
* http://localhost:8090/called - calls Consul service aware home service, in case the service in unavailable return Service Unavailable
