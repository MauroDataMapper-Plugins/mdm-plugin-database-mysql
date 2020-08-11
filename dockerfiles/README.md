# Setting up MySQL Server  Test Environment

## Build the MySQL Docker image

```bash
$ docker build --tag=mysql-mdm:latest .
```

## Start the MySQL Docker Instance

The following command will start up a default MySQL server instance.
--rm -d
```bash
docker run  \
 -e MYSQL_ROOT_PASSWORD=my-secret-pw \
 -p 3306:3306 \
 --name mysql-mdm \
 mysql-mdm:latest
```

## Connect to the instance

You can connect using `root` user, password `my-secret-pw` on port 3306.
This instance will have the same test databases installed as 
[mdm-plugin-database-postgres](https://github.com/MauroDataMapper-Plugins/mdm-plugin-database-postgresql)
