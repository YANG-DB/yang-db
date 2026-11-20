### Sample Queries 

We will present few sample queries to show different usage capabilities of Yang.db in regard to the dragons schema

#### Loading CSV
Once loading the csv different graph nodes and edges is complete we can start asking different questions about the information.

#### Queries - in cypher language
 
 - Get all nodes types:
    ```match (p) return *```

 - Get all nodes of types Person & Dragon (a label union such as `(p:Person | Dragon)` is not supported - use `WHERE`):
    ```match (p) where p:Person OR p:Dragon return *```
   

 - Get all nodes with Person label:
    ```match (p:Person) return *```


 - Get nodes with Person label and name :
    ```match (p:Person {name:'Braden Heaney'}) return *```


 - Get people who own a dragon:
  ```match (p:Person)-[own:Own]-(d:Dragon) return *```
   

 - Get people who own a dragon and are members of the Bolling Guild:
```
match (p1:Person)-[own:Own]-(d:Dragon),
      (p1:Person)-[member:MemberOf]-(g:Guild {name:Bolling}) return *
```   

- Get people who own a dragon that has fired upon other dragons with very hot fire (above 600 degrees), together with the owners of the dragons that were fired upon:

```
match (p1:Person)-[own1:Own]-(d1:Dragon),
      (d1:Dragon)-[fire:Fire]-(d2:Dragon),
      (d2:Dragon)<-[own2:Own]-(p2:Person)
        where fire.temperature > 600
      return *
```

> **Not supported: cyclic patterns.** A pattern that closes a loop back to a node that is already matched — for
> example adding `(p1:Person)-[know:Know]-(p2:Person)` above to ask whether the two owners know each other — is
> rejected with "Cyclic patterns are not supported". Queries are executed as trees; run the chain above and check the
> extra relationship in a second query instead.
