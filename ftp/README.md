### 编译 java 类文件，-classpath 为引入的外部 jar 包
```
javac -classpath /Users/admin/Documents/ftp/commons-net-3.10.0.jar FTPManager.java 
```

### 打成jar包，这里需要在 META-INF/MANIFEST.MF 这个文件中配置好入口类（Main-Class）和引入的类路径 Main-Class: FTPManager Class-Path: commons-net-3.10.0.jar
```
jar -cvfm ftp.jar META-INF/MANIFEST.MF FTPManager.class
```

### 执行 jar 包
```
java -jar ftp.jar
```
