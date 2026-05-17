# Postgres with SSL Connection

## Overview
This script checks if a PostgreSQL database is configured to accept SSL connections. It connects to the database using the provided connection parameters and verifies if SSL is enabled.
Please take a look for the [Postgresql-ssl](https://github.com/SiThuTun-mdy/postgresql-ssl) in docker container.
## Technologies Used
1. Spring Boot
2. PostgreSQL JDBC Driver

## Update Config file

### Check postgresql.conf and pg_hba.conf
```
$psql -h localhost -p 5432 -U postgres 

=> SHOW config_file;
=> SHOW hba_file;
```

### Update postgresql.conf
```
ssl = on
ssl_ca_file = '/var/lib/postgresql/data/certs/rootCA.crt'
ssl_cert_file = '/var/lib/postgresql/data/certs/localhost.crt'
#ssl_crl_file = ''
#ssl_crl_dir = ''
ssl_key_file = '/var/lib/postgresql/data/certs/localhost.key'
```
```
make sure the certs with permission 640 or less
chmod 600 path/to/the/file ...
```
### Update pg_hba.conf
```
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# IPv4 local connections:
hostssl    all             all             0.0.0.0/0            cert
```

## Generate SSL Certificate
1. CA Certificate:
   ```bash
   openssl req -new -x509 -days 3650 -keyout ca.key -out ca.crt
   ```
2. Server Certificate:
   ```bash
   openssl req -new -nodes -out server.csr -keyout server.key
   openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 3650
   ```
3. Client Certificate:
   ```bash
   openssl req -new -nodes -out client.csr -keyout client.key -subj "/CN=client"
   openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt -days 3650
   ```
4. Create PKCS#8 file for client:
   ```bash
   openssl pkcs8 -topk8 -inform PEM -outform DER -in certs/pg_client.key -out certs/pg_client.pk8 -nocrypt
   ```

## JDBC SSL properties used by this app

The runtime SSL connection in `src/main/java/com/psql/util/App.java` uses:

- `sslmode=verify-full`
- `ssl=true`
- `sslcert=<project>/client.crt`
- `sslkey=<project>/certs/pg_client.pk8`
- `sslrootcert=<project>/certs/rootCA.crt`

If you get `The server does not support SSL.`, PostgreSQL is still running without SSL enabled (or client is connecting to the wrong host/port).
   
## Run the project
```
mvn clean compile
mvn -q -DskipTests spring-boot:run
```
