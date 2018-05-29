# spring-dynamic-endpoint

Add new endpoints programmatically at runtime using Groovy scripts. The feature is also exposed through JMX.


### How to

Start application 

```
mvn spring-boot:run
```

Add a GET endpoint

```bash
curl -X POST 'http://localhost:8080/endpoint?url=/foo&method=GET' -H 'Content-Type: text/plain' -d \
'
if (params.containsKey("name")) {
	return "Hey " + params.getFirst("name") + " from params!";
} else {
	return "You lazy!";
}
'
```

Test the endpoint

```bash
curl 'http://localhost:8080/foo?name=Sam'
```

```bash
curl 'http://localhost:8080/foo'
```

Add a POST endpoint (or any other HTTP method with a body).

> Note that using JSON, any body is parsed as a Map object.

```bash
curl -X POST 'http://localhost:8080/endpoint?url=/foo&method=POST' -H 'Content-Type: text/plain' -d \
'
if (!body.isEmpty()) {
	return "Hey " + body.getOrDefault("name", "stranger") + " from body!";
} else {
	return "You lazy!";
}
'
```

Test the endpoint

```bash
curl -X POST 'http://localhost:8080/foo' -H 'Content-Type: application/json' -d '{"name": "Sam"}'
```

```bash
curl -X POST 'http://localhost:8080/foo' -H 'Content-Type: application/json' -d '{}'
```


Look at all endpoints

```bash
curl -X GET --url 'http://localhost:8080/endpoint'
```


### JMX

If our JMX beans are part of the Spring Context, there is no need of manually registration.
Spring automatically detects JMX beans by looking at possible implementation of "MBean" interfaces. 

In case we want to manually register a JMX bean, do as follow.

```java
ExampleMBean bean = new MyExample();
final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
final ObjectName objectName = new ObjectName("com.acme.foo:type=Example,name=example"); // use a decent namespace!
server.registerMBean(bean, objectName);
```
