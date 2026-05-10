### 1. install sbt (if no)

```shell
brew install sbt
```

### 2. automatically compile all the changes

```shell
sbt
  tyrian
  ~fastOptJS
```

### 3. serve web server

```shell
cd front
npm install
npm run start
```

### 4. open in the browser

```shell
open http://localhost:1234
```

https://github.com/djnzx/learning-scalajs-websockets
http://localhost:1234
npm install -g wscat
wscat -c ws://localhost:8081/ws