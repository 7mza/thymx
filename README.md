# thymx

personal playground to test

* daisyui / thymleaf / htmx integration
* time sorted id / jpa integration
* strict hibernate conf
* hibernate L1/L2 caches
* strict csp with haproxy
* alpinejs csp

rbac db auth is just for demo

## build

[sdkman](https://sdkman.io)

```shell
sdk env install
```

[nvm](https://github.com/nvm-sh/nvm)

```shell
nvm i
npm i
npm run build
./gradlew clean ktlintFormat ktlintCheck build
```

## local certs for haproxy

```shell
cd ./keystore/
chmod +x *.sh
./gen_cert.sh
cd ..
```

## run

spring is configured with compose support, run with ide or

```shell
./gradlew jibDockerBuild
```

```shell
docker compose up --build
```

[http://localhost:8080/](http://localhost:8080/)

## hotswap jvm

https://github.com/HotswapProjects/HotswapAgent

```shell
-XX:+AllowEnhancedClassRedefinition -XX:HotswapAgent=fatjar
```

## graalvm

```shell
java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ \
  -jar -Dspring.profiles.active="default" \
  ./build/libs/*.jar
```
