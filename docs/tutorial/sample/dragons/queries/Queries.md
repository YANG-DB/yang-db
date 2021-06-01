### Sample Queries 

We will present few sample queries to show different usage capabilities of Yang.db in regard to the dragons schema

#### Loading CSV
Once loading the csv different graph nodes and edges is complete we can start asking different questions about the information.

#### Queries - in cypher language
 
 - Get all nodes types:
    ```match (p) return *```

 - Get all nodes of types Person & Dragon:
    ```match (p:Person | Dragon) return *```
   

 - Get all nodes with Person label:
    ```match (p:Person) return *```


 - Get nodes with Person label and name :
    ```match (p:Person {name:Bonaparte}) return *```


 - Get people who own a dragon:
  ```match (p:Person)-[own:Own]-(d:Dragon) return *```
   

 - Get people who own a dragon and are members of the merchant Guild:
```
match (p1:Person)-[own:Own]-(d:Dragon),
      (p1:Person)-[member:memberOf]-(g:Guild {name:merchant }) return *
```   

- Get people who own a dragon, this dragon has fired upon other dragons with blue fire and are they know the dragon's (that was fired upon) owners:

```
match (p1:Person)-[own1:Own]-(d1:Dragon),
      (d1:Dragon)-[fire:Fire]-(d2:Dragon),  
      (d2:Dragon)<-[own2:Own]-(p2:Person),
      (p1:Person)-[know:Know]-(p2:Preson) 
        where fire.color=blue
      return *
```   
