#Docker pull prometheus DB
docker pull prom/prometheus
# Docker run prometheus
docker run -it --network=host -v src/main/config/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus