if [ $# -eq 0 ]
  then
    echo "Please specify the pgdump file"
    exit 1
fi

cat $1 | docker-compose exec -T postgres psql -U postgres -d blokatugent
