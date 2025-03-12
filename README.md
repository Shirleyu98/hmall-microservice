# Microservice Mall Service  

This project is an implementation of a microservices-based e-commerce application, following [this tutorial](https://www.bilibili.com/video/BV1S142197x7/?spm_id_from=333.1387.favlist.content.click&vd_source=4b6f260d8f4a2ff2fa4c78872b019102).  

### Overview  
This project transforms a monolithic e-commerce application into a microservices architecture, improving scalability and maintainability. It includes five key microservices:  
- **Item Service**  
- **User Service**  
- **Cart Service**  
- **Trade Service**  
- **Payment Service**  

The system is built using **Spring Boot** and **Spring Cloud** for service orchestration.  

⚠️ **Note:** This repository contains only the microservice code. The required infrastructure components (RabbitMQ, Nacos, Seata, MySQL, Elasticsearch, Kibana) must be set up separately.  

---

## Running the Microservices  

### 1️⃣ Run All Services  
To start all microservices, use:  
```sh
docker compose up -d --build
```

### 2️⃣ Run a Specific Service  
To start only a specific service (e.g., `user-service`), run:  
```sh
docker compose up -d --build user-service
```

---

## Setting Up the Infrastructure  

To set up the necessary infrastructure components, create a separate **Docker Compose** configuration file (`docker-compose-infra.yml`) with the following content:  

```yaml
services:
  mysql:
    image: mysql
    container_name: mysql
    restart: always
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=hmall

  elasticsearch:
    image: elasticsearch:7.12.1
    container_name: es
    restart: always
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    volumes:
      - es_data:/usr/share/elasticsearch/data

  kibana:
    image: kibana:7.12.1
    container_name: kibana
    restart: always
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200

  rabbitmq:
    image: rabbitmq:3.8-management
    container_name: mq
    restart: always
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  nacos:
    image: nacos/nacos-server:v2.1.0-slim
    container_name: nacos
    restart: always
    ports:
      - "8848:8848"
      - "9848-9849:9848-9849"
    environment:
      - MODE=standalone
    volumes:
      - nacos_data:/home/nacos/data

  seata:
    image: seataio/seata-server:1.5.2
    container_name: seata
    restart: always
    ports:
      - "8099:8099"
      - "7099:7099"
    depends_on:
      - nacos
    volumes:
      - seata_data:/seata-server/data
    environment:
      - SEATA_IP=seata
      - SEATA_PORT=8099

volumes:
  mysql_data:
  es_data:
  rabbitmq_data:
  nacos_data:
  seata_data:
```

### 3️⃣ Start the Infrastructure  
Run the following command to start the infrastructure components:  
```sh
docker compose -f docker-compose-infra.yml up -d
```

---

