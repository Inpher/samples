# Encrypted Query Module Command line Client

This a simple commandline client for an Encrypted elasticsearch.

## Build/Install
To build and install the client:
```shell
gradle installDist
```

## Setup
You need a schema, a key and an elastic settings file.
Examples of those can be found in `src/main/resources`.

### Key
To generate a key run the following
```shell
./build/install/search-cli/bin/search-cli keygen > key.bin
```

### Schema
The schema determines what field can be retrieve and searched.
It is a JSON document of the form:
```
{
    "field_name": [FIELD_TYPE_1, ... , FIELD_TYPE_K]
}
```
where `FIELD_TYPE_i` is either of the following:
- `"retrievable"` : this is a field that can be retrieved as a result when searching documents
- `"searchable"` : this is a field that can be searched

### Elastic settings
The elastic settings is a YAML file
You need to specify the cluster name and the list of initial nodes.
For the rest please look at the elasticsearch documentation.
```yaml
cluster.name: your_cluster_name
transport.client.initial_nodes: ["node1_hostname:port", "hode2_hostname:port"]
```

## How to use it
To connect to elasticsearch run
```
./build/install/search-cli/bin/search-cli connect -key <path_to_key> -schema <path_to_schema> -settings <path_to_elastic_settings>
```
If the connection could be successfully opened, you should be in an interactive
shell.

In the shell you can type `help` to get a description of all the available commands.
Type `quit` to leave the shell.

## Example

Using the following schema:

```json
{
  "movie" : ["retrievable"],
  "firstName" : ["searchable"],
  "lastName" : ["searchable"]
}
```

start the cli and insert type followed by ENTER:

```
> insert
Enter you document on multiple lines. To send the query you need to finished with an empty line:

```

you can now enter a document to index according to the schema. Such a document would look like this:

```json
{
  "movie" : "The Room",
  "firstName" : "Tommy",
  "lastName" : "Wiseau"
}
```

no search for the newly inserted document:

```
> select * where firstName:"Tommy"
1. {"movie":["The Room"]}
```