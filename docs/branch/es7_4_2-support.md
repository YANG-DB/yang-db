Abstract: 

The purpose of this branch is to integrate the E/S version 7.4.2 into yandDb 

Changes: 
    
    Upgrade jooby version to 1.6.8
    
    Upgrade Elasticsearch version to 7.4.2

Fixes:
 
    Remove BasicIdGenerator.getNext() needs no version lock
    Fix CSV-convertor to concider LOV (dictionary) when creating the json document
    
    Fix ES: 
        - remove "_all" mapping    
        - remove standard filter
        - fix ngram max-min diff

Upgrades: 
    
    Add support for retry using https://reflectoring.io/retry-with-resilience4j/