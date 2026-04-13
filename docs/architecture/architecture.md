# Architecture

We are assuming that the users of the system are going through an api gateway. 
This api gateway is using OAuth2 for authentication and authorization.

This folder contains diagrams as code using [Structurizr](https://structurizr.com/). 

## The service architecture diagrams

To view/generate the diagrams use the following command from the project root directory:

```bash
docker run -it --rm \
    -p 8090:8080 \
    -v ./docs/architecture:/usr/local/structurizr \
    structurizr/structurizr local
```

OR

```bash
docker compose up structurizr
```

The diagrams will be available at `http://localhost:8090` (or your configured port) and the source code for the diagrams is in the `docs/architecture` directory.

