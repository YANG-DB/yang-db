#!/bin/bash
#docker pull grafana
docker pull grafana/grafana
# docker Run grafana
docker run -it --name=grafana -p 3000:3000 grafana/grafana
