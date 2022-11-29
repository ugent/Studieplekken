# Profiling with Skywalking
**Requirements**
- Docker
- docker-compose

**Commands**
```shell
cd skywalking
docker-compose -f skywalking.yml -f skywalking.override.yml up --build
```

The backend will automatically connect to this using an agent. On port `:8000` you can find the skywalking onion.

