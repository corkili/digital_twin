docker builder prune -f
docker system prune -f
docker rmi -f dt_server:latest
docker build --no-cache -t dt_server:latest .