# Book Inventory using Vaadin and Spring AI

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
mvnw spring-boot:run

 OR 

.\mvnw spring-boot:run
```

To build the application in production mode, run:

```bash
./mvnw -Pproduction package
```

Start the application from the JAR:

```bash
java -jar book-inventory-withspringai-1.0-SNAPSHOT.jar
```

Format code:

```bash
./mvnw spotless:apply
```

## Vaadin Getting Started Guide

The [Getting Started](https://vaadin.com/docs/latest/getting-started) guide will quickly familiarize you with your new Book Inventory implementation. You'll learn how to set up your development environment, understand the project structure, and find resources to help you add muscles to your skeleton — transforming it into a fully-featured application.

## Dataset

[Goodreads Book Datasets With User Rating 2M](https://www.kaggle.com/datasets/bahramjannesarr/goodreads-book-datasets-10m/)