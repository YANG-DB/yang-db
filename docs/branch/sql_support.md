Abstract: The purpose of this branch is to integrate the SQL capabilities of aws open distro fork made by yandDb 


Changes: 
    
    upgrade jooby version to 1.6.8
    
    upgrade Elasticsearch version to 7.4.2

Fix:
 
    Remove BasicIdGenerator.getNext() needs no version lock  

Upgrades: 
    
    add support for retry using https://reflectoring.io/retry-with-resilience4j/