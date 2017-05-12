# Boilerplate Testcase Generation using Template file

Some of the Boilerplate testcases are designed with different input parameters e.g. expression ids, keycontent configuration. In order to generate Boilerplate testcases with these input parameters you need to use a template file. The testcases which use a template file will have a `*.yaml` file within their testcase directory.

In order to run testcase generation which utilises the template file you need to add the `-Dtask.template=filename.yaml` variable to the command when running the worker testing application.



