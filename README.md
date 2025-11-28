# RumbleRedis

## Start Redis
Confirm docker is installed and running
Navigate to redis directory in terminal
```
docker compose up -d
```
This will run redis locally and enable connections for both python & java clients

## Projects:

### Python
Setup:
Navigate to python-client
```
pipenv install
```
Run pub / sub
```
pipenv run python main.py
```

### Java
In vscode open folder for java-client as root
Right click on App.java & select 'Run Java'

