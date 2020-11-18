docker-attach-db:
	docker-compose exec db psql blokat -d blokatugent
