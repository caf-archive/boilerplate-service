# boilerplate-creation-container
A docker image containing the boilerplate-creation testing utility used to set up integration tests.

## Running this container

### Docker Command

To use this contain with just docker, run this command:

<pre><code>
docker run -it &lt;IMAGE_ID&gt; java -Dboilerplateapi.url=http://boilerplate-api:8080/boilerplateapi -Dfile.input=/util-data/test-input/creation-data.json -Dfile.output=/util-data/test-output/creation-output.json -jar util-boilerplate-creation.jar
</code></pre>


### Docker Maven Plugin

To use this container within a pom.xml, here is the configuration:

Note the use of the volume to supply the input data.

<code>

    <!-- Start input data volume -->
    <image>
        <alias>util-data</alias>
        <name>${project.artifactId}-test-data:${project.version}</name>
        <build>
            <assembly>
                <basedir>/util-data</basedir>
                <inline>
                    <fileSets>
                        <fileSet>
                            <directory>util-data/test-input</directory>
                            <outputDirectory>/test-input</outputDirectory>
                            <includes>
                                <include>*</include>
                            </includes>
                        </fileSet>
                    </fileSets>
                </inline>
            </assembly>
        </build>
    </image>
    <!-- Start input data volume -->
    
    <!-- Begin Boilerplate Creation image -->
    <image>
        <alias>boilerplate-creation-container</alias>
        <name>${corporateDockerReleaseRegistry}/caf/boilerplate-creator:1.12.0</name>
        <run>
            <links>
                <link>boilerplate-api</link>
            </links>
            <log>
                <enabled>true</enabled>
            </log>
            <cmd>java -Dboilerplateapi.url=http://boilerplate-api:8080/boilerplateapi -Dfile.input=/util-data/test-input/creation-data.json -Dfile.output=/util-data/test-output/creation-output.json -jar util-boilerplate-creation.jar
            </cmd>
            <volumes>
                <from>
                    <image>util-data</image>
                </from>
            </volumes>
            <wait>
                <log>Creation completed successfully</log>
                <time>20000</time>
                <shutdown>500</shutdown>
            </wait>
        </run>
    </image>
    <!-- End Boilerplate Creation image -->
</code>