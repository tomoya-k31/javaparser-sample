# javaparser-sample


```shell
Usage: java -jar javaparser-sample.jar [--java-ver=<javaVer>] [-o=<outputDir>]
                         [-j=<jarFiles>]... [--jar-txt=<jarTexts>]...
                         -f=<filePaths>... [-f=<filePaths>...]...
                         -s=<srcDirs>... [-s=<srcDirs>...]...
Perform code syntax analysis using JavaParser and output the results to the
specified file.
  -f, --file-path=<filePaths>...
                             Paths of the Java files to be extracted (multiple
                               paths can be specified)
  -j, --jar=<jarFiles>       Paths of the JAR files used by the project
                               (multiple paths can be specified)
      --jar-txt=<jarTexts>   Text files listing the JAR files used by the
                               project (multiple files can be specified)
      --java-ver=<javaVer>
  -o, --output-dir=<outputDir>
                             Output directory for the syntax resolution results
  -s, --src-dir=<srcDirs>... Paths of the src directories of the entire project
                               (multiple paths can be specified)
```

## Example

```shell
java -jar javaparser-sample.jar \
     -f=~/Workspace/code/src/main/java/sample/CacheHelper.java \
     -s=~/Workspace/code/src \
     --jar-txt=~/Workspace/code/build/report/resolved-jars.txt
```