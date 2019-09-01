# LazyBum - Decision tree learning using lazy propositionalization

LazyBum is a dynamic propositionalization system that simultaneously:
 * propositionalizes a relational dataset.
 * learns a decision tree on that dataset, which guides the propositionalization.
 
It is described in the paper:
> Schouterden Jonas, Davis Jesse, Blockeel Hendrik (2019). LazyBum: Decision tree learning using lazy propositionalization. Presented at the The 29th International Conference on Inductive Logic Programming, Plovdiv, Bulgaria, 03 Sep 2019-05 Sep 2019. 


Propositionalization consists of summarizing a relational dataset into a single attribute-value table. This table can next be used by any attribute-value learner. 
It assumes an instance-based setting. There is one **target table** in the dataset, of which each row identifies an example. Two main disadvantages of propositionalization are:
1. the loss of information resulting from reducing a database to a single table
2. the generation of features that are not interesting to a learner.

LazyBum is **dynamic** (or lazy) propositionalization approach. In contrast to static propositionalizers, it guides its feature construction by simultaneously learning a machine learning model.
LazyBum's feature construction is based on [OneBM](https://arxiv.org/abs/1706.00327). However, LazyBum interleaves its feature construction with the induction a decision tree. 
The decision tree learning process both uses and guides the feature construction.
As a result, LazyBum generates both:
* a propositional feature table, which can be reused by a propositional learner,
* a decision tree, which can be used on the relational dataset.

Currently, LazyBum assumes a classification setting. That is, one column of the target column represents a class to be predicted by the learned decision tree.

Apart from LazyBum, this repository also contains our own implementation of OneBM, as the original version was unavailable. Both LazyBum and OneBM use the same feature construction code.
However, note that this version of OneBM is more limited than the version described in its paper, in two ways:
1. this version assumes numerical and categorical features, while the original OneBM supports many more feature types,
2. this version uses *forward-only* traversal for creating join paths between tables, while the original version also supports *backwards traversal*.

## Setup

LazyBum is developed in Java version 11. It can be build using [Gradle](https://gradle.org/). [JOOQ](https://www.jooq.org/) is used to build and execute type safe SQL queries.
[JGraphT](https://jgrapht.org/) is used for representing and processing the relational database schema as a graph.
See the included *build.gradle* for the actual dependencies. 

## Expected data format
LazyBum expects its data to be in a relational database, interacting with it using SQL queries. For our experiments, we mostly used [PostgreSQL](https://www.postgresql.org/).
We also tried out MySQL, on which LazyBum ran as expected. Note though that this is still very much research code, so your mileage may vary.

LazyBum expects as a dataset a relational database of tables connected by foreign-key relationships. Make sure the primary key and foreign key constraints are actually defined.
There is one **target table** in the dataset; each of its rows represents an example. The target table has a primary key column (its *target_id* column) and a class column (its *target_column*). 
Note that during development, it was implicitly assumed that all primary keys consist of a single column. Multi-column keys are currently not supported.

LazyBum expects as input a [java .properties file](https://en.wikipedia.org/wiki/.properties) describing the dataset to be used. This is a key-value file containing containing fields as defined in the ProgramConfigurationOption enum. The following is an example:
```properties
# user configuration
DB_USER = database_username
DB_PASSWORD = database_password

# the url of the database to connect to
SQL_DIALECT = postgres
DB_URL = jdbc:postgresql://localhost/hepatitis_std_mod_target

# the schema to consider, the target table, its primary key and its target column.
CATALOG = hepatitis_std_mod_target
SCHEMA = hepatitis_std_mod_target
TARGET_TABLE = dispat
TARGET_ID = m_id
TARGET_COLUMN = type
```
More examples are can be found in the *data* directory.

## Running LazyBum or OneBM
An example of how to run OneBM can be found in [OneBMMain.java](src/main/java/onebm/OneBMMain.java).

Examples of how to run LazyBum can be found in:

*[LazyBumLearnSingleDecisionTreeMain.java](src/main/java/lazybum/main/LazyBumLearnSingleDecisionTreeMain.java) Learns a single LazyBum tree.
*[LazyBumTenFoldCVDecisionTreePredictionMain.java](rc/main/java/lazybum/main/LazyBumTenFoldCVDecisionTreePredictionMain.java) Performs ten-fold cross validation, using the folds defined in the [folds](folds) directory. It will generate an *output* directory containing its results.




