docker-attach-db:
	docker-compose exec db psql blokat -d blokatugent
logs-backend:
	less +F /var/lib/docker/volumes/blokat_backend_logs/_data/blok-at-logs.log
logs-db:
	docker-compose logs -f db
