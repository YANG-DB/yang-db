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

- Get people who own a dragon, this dragon has fired upon other dragons with very hot fire (above 600 degrees) and are they know the dragon's (that was fired upon) owners:

```
match (p1:Person)-[own1:Own]-(d1:Dragon),
      (d1:Dragon)-[fire:Fire]-(d2:Dragon),  
      (d2:Dragon)<-[own2:Own]-(p2:Person),
      (p1:Person)-[know:Know]-(p2:Person) 
        where fire.temperature > 600
      return *
```   
