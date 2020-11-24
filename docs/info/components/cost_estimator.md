
##Cost Estimator
![](https://static.priceandcost.com/wp-content/uploads/2018/03/Your_Guide_to_Project_Management_Cost_Estimation_blog.jpg)

Cost estimator is the process of evaluating a “cost” of an execution plan according to a prior made statistical calculation for the distribution of values in the dataset.

Each execution step (element in the query) of any graph element (entity, relation, filter) will be estimated based on the physical storage statistical attributes of the data.

####Redundancy
Redundancy is the process of pushing node attributes to adjacent relations – making them redundant in the data.
The purpose of this process will increase the volume of data stored on one hand, on the other hand it will decrease 

the amount of data needed to fetch in each query, and will enable more accurate execution plan.
In a redundant model we will store on each relation both id’s of the relating sides, the relating types and any of the sides properties we may think may be of redundancy value.

####Collecting statistical data process
![](https://i0.wp.com/www.skepticalraptor.com/blog/wp-content/uploads/2017/07/anti-vaccine-statistics.png?zoom=2&resize=2000%2C1200&ssl=1)

The process of collecting statistical information about elements of the graph – is tightly connected to the physical storage format of the underlying DB.

In a non-native graph DB that its storage mechanism is based on a document store.
The graph elements – both node & edges are stored in a document. we need to calculate the histogram (distribution) for every property in the graph.

In a document store like elasticsearch there is an additional level of abstraction for the document location and partitioning:

* Indices – indicate the “table” (index) that holds all the “records” of a given type

* Shard – The table’s (index) “partition” factor that partitions the data according to some column (mostly time based)

* Mappings – mappings is Elasticsearch’s schema index structure, we use it to label the graph elements with a distinct type (each type can have named properties).

###Types of statistical estimators:

Entity
----

Histogram with buckets (bucket per schema type)
* Each bucket contains:
    * Cardinality of entity types
    * Document count for entity types

Properties – (both entity & relation)
----
Histogram with buckets (bucket per range)

* Each bucket contains:
*  *    Field cardinality 
*  *    Graph elements cardinality

####Graph element properties Example 
Let’s assume we have a Person entity type with name property, we will create a histogram with 26 buckets – one per each starting letter.

Given a bucket for the letter ‘J’ – it will contain the unique name (starting with J) in the data (Jack, Jhon) and the unique Entities with that name.

We can observe that the entity cardinality (unique people with names starting with ‘j’) for that bucked would be much higher than of the value cardinality (unique names starting with ‘j’) since many person entities can share the same name.

Estimating the number of People named ‘James’ would take us to the next formula:
‘J’-Bucket cardinality: [100, 350000] –
100 stands for unique names starting with J and 350,000 stands for unique people with names starting with J.
Estimate will be 350,000 / 100 = 35000.
Let’s assume we would like to estimate number of people named ‘JMorgam’ – 

If we assume equal distribution of cardinality inside the bucket, we may take the assumption that since the second letter is M - which is about in the middle of the alphabet, we can get better estimation by assuming ½ of the values are from the letter ‘m’ onwards ...
Estimate will be (350,000 / 100) /2 = 17500.
 

####Bucket Ranges – depends on field type
Bucket may have the following ranges (according to the field type)

**Numeric** – given bucket number and min-max range – buckets range according to data

**String**  - alphabetic range buckets (equal width / depth)

**Manual**  - manual number & size buckets (user defined)

**Term**    - buckets stands for a string term according to number of terms in the data

**Dynamic** – given bucket number, auto calculate min-max (numeric only) according to data 

####Relation – 
Let’s review relation histogram; The histogram is defined to measures count & cardinality of the following tuple 

(A,B are the relations side respectively) :

```javascript
{A.id, A type, B.id, B type, Rel.id, direction, values}.
```

Each relation type (owns, lives, knows...) will have this histogram, the number of buckets will be the cartesian product of the tuple elements (assuming non-valid combinations such as ‘car-[owns]->person’ is not allowed)

Each of these buckets will contain:

* Count of relations id’s
* Count of SideA unique entities
* Cardinality of SideA

Let’s assume we have 10 entity types, then the number of buckets will be:
 Side A - 10 (possible types) X Side B - 20 (possible types) X2 (directions) = 400.

Example:
-----
**Owns** – 3 dimensional histogram for ‘own type relation.

```javascript
{
  "type": "owns",
  "fields": [
    {
      "field": "entityA.type",
      "histogram": {
        "histogramType": "manual",
        "dataType": "term",
        "buckets": […]
      }]
    },
    {	
      "field": "entityB.type",
      "histogram": {
        "histogramType": "manual",
        "dataType": "term",
        "buckets": […]
    },
    {	
      "field": "dir",
      "histogram": {
        "histogramType": "manual",
        "dataType": "term",
        "buckets": […]
       }
    }
  ]
}
```
##Filter selectivity – 
A filter condition is a predicate expression specified in the predicate clause (property constraint) of a V1 query.

A predicate can be a compound logical expression with logical AND, OR, NOT operators combining multiple single conditions.

A single condition usually has comparison operators such as =, <, <=, >, >= or <=>2. 

For logical AND expression, its filter selectivity is the selectivity of left condition multiplied by the selectivity of the right condition:
>selectivity(a AND b) = selectivity(a) * selectivity(b).

Available Filter Conditions:
---

```javascript
•	empty
•	notEmpty
•	eq
•	ne
•	gt
•	ge
•	lt
•	le
•	inset
•	notInSet
•	inRange
•	notInRange
•	contains
•	notContains
•	startsWith
•	notStartsWith
•	endsWith
•	notEndsWith
•	match
•	notMatch
•	fuzzyEq
•	fuzzyNe
```

##Collection Statistics Framework

For every property we collect histogram data, each histogram has a list of buckets holding monotonic range of content, the location of its content (index, shard) type, documents count & cardinality.

###Example fields to histogram mapping
The next example has 3 types of histograms for 3 fields:

* **Age** – numeric histogram
* **Name** – String histogram (equal width bucket size)
* **Address** – manual buckets histogram

```javascript
"types": [{
  "type": "dragon", #(entity type)
  "fields": [{
    "field": "age", #(field name)
    "histogram": {
      "histogramType": "numeric",
      "min": 10,
      "max": 100,
      "numOfBins": 10
    } },
    {
      "field": "name",
      "histogram":
      {
        "histogramType": "string",
        "prefixSize": 3,
        "interval":10,
        "firstCharCode":"97",
        "numOfChars":26
      } },
    {
      "field": "address",
      "histogram":      {
        "histogramType": "manual",
        "dataType":"string",
        "buckets":[
          { "start":"abc",
            "end": "dzz"
          },
          { "start":"efg",
            "end": "hij"
          },
          { "start":"klm",
            "end": "xyz"
          }]
	}
    }]
```

>String histogram:

```javascript
numOfBuckets = Math.ceil(Math.pow(numChars, prefixLen) / interval
```

##Join
In relational algebra join has 4 type semantics:
* Inner join
* Left outer join
* Right outer join
* Full outer join

In the graph semantics, traversing from node a -> b is performing inner join between a type elements and b type elements, fetching only the (a,b) records that have relationship.

Left & right outer joins (Right outer join) in graph semantics are equivalent to optional graph operators.
Optional operator a --Optional-->b will fetch all type of a’s and if exists relation to b’s it will also bring them.

The direction of the join is according to the direction of the relation. 

###Join cost estimation

For estimating the join cost we currently assume using in memory hash-join; lets assume we need to join two groups A with size n and B with size m.
The cost of doing in-memory hash join is the I/O cost of fetching the groups from the data-set to the driver,

> cost = O(m) + O(n). 
The count estimation of the elements after the join is the smallest size group 
> docCount = min(docCount(m),docCount(n))  

This is an upper bound estimation; the actual count may be lower.

###Cost Estimator is Pattern Based
For an existing plan, adding a step to the plan has a price, this price is added to the current total price of the plan.

Cost is estimated for the following steps:

```javascript
EntityPattern – EntityPatternCostEstimator cost estimator
```
Estimates single entity step cost – entity docCount (with filter if exists)

```javascript
EntityRelationEntityPattern – EntityRelationEntityPatternCostEstimator cost estimator
```
Estimates (entity)-->[relation]-->(entity) step cost – see formula below

```javascript
GoToEntityRelationEntityPattern – GoToEntityRelationEntityPatternCostEstimator cost estimator
```

Estimates cost of the entity relation entity pattern with additional goTo step

Example
----
We will use the next schema:
###Entities:
**Person** (1M distinct values)
* id
* Name (1K distinct values)
* * String Histogram with about 2000 buckets (equal width) 

* Age (100 distinct values)
* * Dynamic Histogram with 100 buckets
* * Gaussian age distribution (mean = 25)

* Gender
* * Term Histogram with 2 buckets, 50% in each bucket

**City** (500 distinct values)
* Name
* * Term Histogram with about 500 buckets (number of distinct models)
* Country (50 distinct values)
* * Term Histogram with about 50 buckets (number of distinct models)

**Car** (100 distinct values)
* Brand
* * Term Histogram with 100 buckets (number of distinct models)

**Company** (1000 distinct values)
* Name

###Relations:
Owns (Person->car)
* 50% of the people own car (geometric distribution with mean 1) 

Time of purchase - (10K distinct values)
* * Dynamic Histogram with 100 buckets

Selectivity: (average number of edges)
* * Number of Persons / Number of distinct edges

Collision Factor:
* * Number of Edges / Number of distinct Cars	

Redundant Properties 
Holds count & cardinality of the edges within the context of the measured redundant property.

####**Side A**
* * Person Name – multidimension histogram [name, direction]
* * * String Histogram with about 2000 buckets (equal width) 
* * Person age – multidimension histogram [age, direction]
* * * Dynamic Histogram with 100 buckets

####**Side B**
* * City Name – multidimension histogram [name, direction]
* * * Term Histogram with about 500 buckets (number of distinct models)
* * City Country – multidimension histogram [country, direction]
* * * Term Histogram with about 50 buckets (number of distinct models)

####Located-In (Person->City, Company->City)

**Selectivity**: (average number of edges)

* [Person, City]: Number of Persons / Number of (Unique Person) Located-In edges
* [Company, City]: Number of Company / Number of (Unique Company) Located-In edges

**Collision** Factor:

* Number of Persons / Number of Cities
* Number of Companies / Number of Cities

####Redundant Properties 
**[Person,City]**

*Side A*

* Person Name – multidimension histogram [name, direction]
* * String Histogram with about 2000 buckets (equal width) 
* * 26*26*26 / 10 = 26^prefixSize / interval
* Person age – multidimension histogram [age, direction]
* * Dynamic Histogram with 100 buckets

*Side B*

* City Name – multidimension histogram [name, direction]
* * Term Histogram with about 500 buckets (number of distinct models)
* City Country – multidimension histogram [country, direction]
* * Term Histogram with about 50 buckets (number of distinct models)

**[Company,City]**

**Side A**
* Company Name – multidimension histogram [name, direction]

**Side B**

* City Name – multidimension histogram [name, direction]
* * Term Histogram with about 500 buckets (number of distinct models)
* City Country – multidimension histogram [country, direction]
* * Term Histogram with about 50 buckets (number of distinct models)

**Friend** (Person->Person)

* todo

**Works** (Person->Company)

* todo

##Query
```javascript
Person[1]:Quant1[2]:{3|4|6}:Person.name[3]{start with “dan”}:Owns[4]:Car[5]:located[6]:City[7]: City.name[7]{equals “N.Y”}
```

This query will be estimated on each step the extender adds, lets start the process by applying the initial extender & evaluating cost according to type’s count & cardinality

Phase 1: (Initial Extender)
>EType[1]: EProp[3] - cost : (person cardinality 1M)  - **Plan 1**

Estimating EProp[3] using property histogram, the condition “starts with:‘dan’” is mapped to a bucket with value cardinality of 50K.

>EType [5]: - cost : (car cardinality 100) - **Plan 2**
>EType [7]: - cost : (city cardinality 5k) - **Plan 3**

Phase 2: (Step Extender)
>EType[1]:EProp[3]-->Rel[4]-->EType[5]	- Plan (1)-4

Estimating Rel[4]  :
**Side A**

Bucket wise - redundant filter:
* Edge estimation according to the edge name filter is 30K.
* Unique persons according to the edge name filter is 20K
* Selectivity is the average number of “owns” edge per person:  30K/20K = 1.5

**Side B**

Bucket wise:
* Unique cars according to the edge name filter is 10
* Edge collision factor will be 30K/10 = 3K

Step count = number of graph elements to be scanned: nodes + side B edges 

* Side A estimation (using edge redundant filter)
* Edges count = min(SideA edges estimation, SideB edges estimation) 
* Side B estimation (using edge collision estimator)

>Step count = 20K+10

The new plan cost is:
>new_count = old_count*step_propagation_factor + step_count

Step propagation factor is the new knowledge we have on the estimated number of graph elements – this estimation should be propagated back to the former estimators to fix their counts using the new knowledge we discovered.

####Step propagation:
* Node back propagation => no filter exists on side B therefore no back propagation 


