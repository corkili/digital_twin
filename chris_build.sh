docker build --platform linux/amd64 -t dt_server:2.39 .
docker save dt_server:2.39 >> server2.39.tar