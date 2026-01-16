# revived-duels
Our duel system. Monorepo including minecraft plugins & microservices.

## Why are we open source now?
We decided to take this big step because we want to give back to the community by sharing some of our features and functionality. We also welcome and encourage contributions.

## Our Infrastrucure
![infra](img/network-infra.png)

## Development Setup

To set up a proper development environment, you need the **Docker Compose plugin** installed and **Minecraft server Docker images** prepared.

### Step 1: Edit the Dockerfiles

**Dockerfile locations:**

* `duels/Dockerfile`
* `lobby/Dockerfile`
* `limbo/Dockerfile`
* `service/queue/Dockerfile`

**Example Dockerfile:**

```Dockerfile
FROM ghcr.io/revived-club/duels-server:main
COPY build/libs/duels-*-all.jar duel/plugins/
```

Replace
`ghcr.io/revived-club/duels-server:main`
with **your own Docker image**.

### Step 2: Docker Compose Configuration

Below is the Docker Compose configuration used for development:

```yml
version: '3.8'

services:
  mongodb:
    image: mongo:latest
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
      MONGO_INITDB_DATABASE: revived
    ports:
      - "27017:27017"
    networks:
      - revived-net

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
    networks:
      - revived-net

  lobby:
    build: ./lobby
    environment:
      MONGODB_HOST: mongodb
      MONGODB_USERNAME: admin
      MONGODB_PASSWORD: password
      MONGODB_DATABASE: revived
      REDIS_HOST: redis
      REDIS_PORT: 6379
      HOSTNAME: lobby
    depends_on:
      - mongodb
      - redis
    networks:
      - revived-net

  duels:
    build: ./duels
    environment:
      MONGODB_HOST: mongodb
      MONGODB_USERNAME: admin
      MONGODB_PASSWORD: password
      MONGODB_DATABASE: revived
      REDIS_HOST: redis
      REDIS_PORT: 6379
      HOSTNAME: duels
    depends_on:
      - mongodb
      - redis
    networks:
      - revived-net

  proxy:
    build: ./proxy
    environment:
      MONGODB_HOST: mongodb
      MONGODB_USERNAME: admin
      MONGODB_PASSWORD: password
      MONGODB_DATABASE: revived
      REDIS_HOST: redis
      REDIS_PORT: 6379
      HOSTNAME: proxy
    depends_on:
      - mongodb
      - redis
    ports:
      - "19132:19132"
    networks:
      - revived-net

  queue:
    build: ./service/queue
    environment:
      MONGODB_HOST: mongodb
      MONGODB_USERNAME: admin
      MONGODB_PASSWORD: password
      MONGODB_DATABASE: revived
      REDIS_HOST: redis
      REDIS_PORT: 6379
      HOSTNAME: proxy
    depends_on:
      - mongodb
      - redis
    ports:
      - "6767:6767"
    networks:
      - revived-net

networks:
  revived-net:
    driver: bridge
```

### Step 3: Start the Environment

Once everything is configured, simply run:

```bash
docker compose up
```

## Deployment

This monorepo is orginally built for [Kubernetes](https://kubernetes.io/). Our Kubernetes files can be found in [this repo](https://github.com/Revived-club/revived-kubernetes). 

### Step 1: Build your own Docker Images

Build your own docker images. Examples can be found in `.github/workflows`. 

### Step 2: Setup a Kubernetes Cluster

Setup a Kubernetes Cluster using [the documentation](https://kubernetes.io/docs/home/)

### Setup 3: Install Redis & MongoDB

To deploy our server, you need a MongoDB & Redis. You can find charts for these using [Helm](https://helm.sh/docs/topics/charts/) 

### Step 4: Apply yml files

Once you apply the yml files, you are all set!

## Contribution

Contributions are welcome! Please feel free to submit a **Pull Request**.

1. Fork the repository
2. Create your feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add some amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

## License
This project is licensed under the GNU Affero General Public License v3.0 - see the LICENSE file for details.
