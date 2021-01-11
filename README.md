![](https://github.com/logicsquad/minifier-maven-plugin/workflows/build/badge.svg)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-2-Clause)

Minifier Maven Plugin
=====================

What is this?
-------------
This project provides a Maven plugin to do one thing: safely "minify"
web resources. The key word is "safely": the aim is _not_ to produce
maximal size reduction, nor introduce any morphological changes, but
to provide _reasonable_ file size reduction in most cases.

Using minifier-maven-plugin
---------------------------
You can use `minifier-maven-plugin` in your projects by including it in
the `build/plugins` section of your project's POM.

    <plugin>
      <groupId>net.logicsquad</groupId>
      <artifactId>minifier-maven-plugin</artifactId>
      <version>0.2</version>
      <executions>
        <execution>
          <id>default-minify</id>
          <configuration>
            <sourceDir>${basedir}/src/main/webserver-resources</sourceDir>
            <targetDir>${project.build.directory}/${project.build.finalName}.woa/Contents/WebServerResources</targetDir>
            <cssIncludes>
              <cssInclude>**/*.css</cssInclude>
            </cssIncludes>
            <cssExcludes>
              <cssExclude>**/*.min.css</cssExclude>
            </cssExcludes>
            <jsIncludes>
              <jsInclude>**/*.js</jsInclude>
            </jsIncludes>
            <jsExcludes>
              <jsExclude>**/*.min.js</jsExclude>
            </jsExcludes>
          </configuration>
          <goals>
            <goal>minify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

Mandatory properties are:

* `sourceDir`: the parent directory in which to scan for resources.
* `targetDir`: the corresponding directory in the built package.

The appropriate values for these properties will vary with the type of
project you are trying to build. (The example above shows appropriate
values for a WebObjects project.)

You can then supply include and exclude patterns to build up lists of
filenames of resources to minify. This is done separately for
Javascript and CSS, as shown above. The syntax for these patterns
follows the [standard Maven syntax used by other plugins](https://maven.apache.org/plugins/maven-resources-plugin/examples/include-exclude.html).

Contributing
------------
By all means, open issue tickets and pull requests if you have something
to contribute.

References
----------
Minification is handled by Logic Squad's [Minifier](https://github.com/logicsquad/minifier)
project. This project is inspired by [Minify Maven Plugin](https://github.com/samaxes/minify-maven-plugin),
though aims to be a simpler product. 
