Abstract: 

The purpose of this branch is to integrate the SQL capabilities of aws open distro fork made by yandDb 

Changes: 
    
    Upgrade jooby version to 1.6.8
    
    Upgrade Elasticsearch version to 7.4.2

Fixes:
 
    Remove BasicIdGenerator.getNext() needs no version lock
    Fix CSV-convertor to concider LOV (dictionary) when creating the json document   

Upgrades: 
    
    Add support for retry using https://reflectoring.io/retry-with-resilience4j/