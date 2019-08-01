# ether4j (Compact ethereum java client)

**License:** [MIT](https://opensource.org/licenses/MIT)

**Requires:** JDK 1.8 or higher

## Features:
* No magic.
* No code generation.
* No redundant dependencies.
* Solidity compiler supports.
* Compact library.
* Abi supports.
* Rlp supports.
* Key generation supports.
* Transaction sign supports.
* Keccak hash supports.
* Smart contracts compile, deploy, call and transaction call also supports.
* Test codes examples. 
* Geth docker examples.

## Dev server
```sh
sudo utils/geth_docker.sh pull
sudo utils/geth_docker.sh dev
```

## Make
```sh
mvn clean install
```

## Install
```sh
<dependency>
  <groupId>tech.xwood</groupId>
  <artifactId>ether4j</artifactId>
  <version>1.0.1</version>
</dependency>
```
