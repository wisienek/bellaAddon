# database.yml

Provide MySQL credentials:

```yaml
BaseURL: "jdbc:mysql://"
Host: "db-host"
Port: "3306"
Database: "database_name"
User: "db_user"
Password: "db_password"
```

Notes:
- Use dedicated DB user with limited privileges.
- Ensure `jdbc:mysql://<Host>:<Port>/<Database>` is reachable from the server.
- Connection string options are appended automatically (`useUnicode=yes&characterEncoding=UTF-8`).
- Restart the server after edits.
