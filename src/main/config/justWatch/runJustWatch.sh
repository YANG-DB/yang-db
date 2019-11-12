# export elasticdata with https://github.com/justwatchcom/elasticsearch_exporter
#Docker pull just watch E/S matrix collector
docker pull justwatch/elasticsearch_exporter:1.1.0
#Docker run collector
docker run -it --network=host justwatch/elasticsearch_exporter:1.1.0