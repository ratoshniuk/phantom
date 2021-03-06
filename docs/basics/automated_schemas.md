[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
======================================
<a id="automated-schema-generation">Automated schema generation</a>
===================================================================

Keyspace level generation and setting properties for keyspaces is provided separately, but CQL 3 Table schemas are automatically generated from the Scala code. To create a schema in Cassandra from a table definition:

```scala

import scala.concurrent.Await
import scala.concurrent.duration._
import com.outworkers.phantom.dsl._

//database.table.createAsync()
```

Of course, you don't have to block unless you want to. Phantom is capable of handling more advanced scenarios as well, including and not limited to things like creating secondary indexes and initialising them in due time and so on.


More advanced indexing scenarios
================================

Secondary indexes are a non scalable flavour of Cassandra indexing that allows us to query certain columns without needing to duplicate data. They do not scale very well at well, but they remain useful for tables where the predicted cardinality for such records is very small.

That aside, it's worth noting phantom is capable of auto-generating your CQL schema and initialising all your indexes automatically, and this functionality is exposed through the exact same `table.create.future()`.

```scala


import com.outworkers.phantom.dsl._

case class TestRow(
  key: String,
  list: List[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String],
  mapIntToInt: Map[Int, Int]
)

abstract class IndexedCollectionsTable extends Table[IndexedCollectionsTable, TestRow] {

  object key extends StringColumn with PartitionKey

  object list extends ListColumn[String]

  object setText extends SetColumn[String] with Index

  object mapTextToText extends MapColumn[String, String] with Index

  object setInt extends SetColumn[Int]

  object mapIntToText extends MapColumn[Int, String] with Index with Keys

  object mapIntToInt extends MapColumn[Int, Int] with Index with Entries
}
```

Automated initialisation of an entire database
=============================================

Using `Database` objects and injecting them into your controllers and other parts where they need to exist is not something that we have designed just for application layering purposes. It is indeed a very powerful feature that you can perfectly encapsulate the scope where a `session` exists using a `Database` object, but another very powerful feature is the ability to auto-generate and sync the schema for entire databases with a single method.

Let's have a look at this example taken from the dsl module inside phantom. We use this for testing purposes, and all phantom tests are written to run against this database, which is then injected into all the test suites using a provider trait, namely `DatabaseProvider`.

To initialise the entire database in a single call, you can use a single call to the `autocreate().future()` method. If you are using the Twitter API provided via the `phantom-finagle` module, you can also call `autocreate.execute()` and get a Twitter future back, provided you have imported the right implicits.

```scala
Await.result(database.autocreate.future(), 10.seconds)
```

```scala
class TestDatabase(override val connector: KeySpaceDef) extends DatabaseImpl(connector) {
  object articles extends ConcreteArticles with connector.Connector
  object articlesByAuthor extends ConcreteArticlesByAuthor with connector.Connector

  object basicTable extends ConcreteBasicTable with connector.Connector
  object enumTable extends ConcreteEnumTable with connector.Connector
  object namedEnumTable extends ConcreteNamedEnumTable with connector.Connector
  object clusteringTable extends ConcreteClusteringTable with connector.Connector
  object complexClusteringTable extends ConcreteComplexClusteringTable with connector.Connector
  object brokenClusteringTable extends ConcreteBrokenClusteringTable with connector.Connector
  // ..
}
```

Specifying custom table creation options during auto-generation
==================================================================

In some instances you may want to override the default query used to create a table, and that could be for the purpose of specifying more advanced options at query creation time.

The default query used by phantom during auto-generation is: `database.myTable.create.ifNotExists()`

Which will produce the following CQL equivalent: `CREATE TABLE IF NOT EXISTS mytable`, with no options specified.
