# java-cid

[![](https://img.shields.io/badge/made%20by-Protocol%20Labs-blue.svg?style=flat-square)](http://ipn.io)
[![](https://img.shields.io/badge/project-ipld-blue.svg?style=flat-square)](http://github.com/ipld/ipld)
[![](https://img.shields.io/badge/freenode-%23ipfs-blue.svg?style=flat-square)](http://webchat.freenode.net/?channels=%23ipfs)

> A Java implementation of Content Id

This is the [cid](https://github.com/ipld/cid) implementation in Java.

## Usage

```
Cid cid = Cid.decode("zdpuAyvkgEDQm9TenwGkd5eNaosSxjgEYd8QatfPetgB1CdEZ");
```
## Dependency
You can use this project by building the JAR file as specified below, or by using [JitPack](https://jitpack.io/#ipld/java-cid/) (also supporting Gradle, SBT, etc).

for Maven, you can add the follwing sections to your POM.XML:
```
  <repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>com.github.ipld</groupId>
      <artifactId>java-cid</artifactId>
      <version>v1.0.0</version>
    </dependency>
  </dependencies>
```

## Testing

### Ant
`ant test`

### Maven
`mvn test`

## Building

### Ant
`ant dist` will build a JAR file in the `./dist` suitable for manual inclusion in a project. Dependent libraries are included in `./dist/lib`.

### Maven
`mvn package` will build a JAR file with Maven dependency information.

## Releasing
The version number is specified in `build.xml` and `pom.xml` and must be changed in both places in order to be accurately reflected in the JAR file manifest. A git tag must be added in the format "vx.x.x" for JitPack to work.

## Maintainers

Captain: [@ianopolous](https://github.com/ianopolous).

## Contribute

Contributions welcome. Please check out [the issues](https://github.com/ipld/java-cid/issues).

Check out our [contributing document](https://github.com/multiformats/multiformats/blob/master/contributing.md) for more information on how we work, and about contributing in general. Please be aware that all interactions related to multiformats are subject to the IPFS [Code of Conduct](https://github.com/ipfs/community/blob/master/code-of-conduct.md).

Small note: If editing the Readme, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

## License

[MIT](LICENSE) © Ian Preston
