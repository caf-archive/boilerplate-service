#util-boilerplate-creation

##Description
This utility allows for the bulk creation of boilerplate expressions and tags.


1) Reads in an input file in json format that represents the expressions and tags to create.

2) Sends these to boilerplate-web-api (specified via environment variable) create method url.

3) Outputs id's and names of created expressions and tags.

##Usage

###Prerequisites

-   boilerplate-web-api to be running and accessible to it for the creation of the boilerplate expressions and tags.
- JSON input file containing the expressions/tags to create. 

###Environment Properties

Execute from the command line passing in the required environment properties.

####boilerplateapi.url
- The url where the boilerplate web api can be accessed by the utility.

####file.input
- The json input file containing the structure of the boilerplate expressions & tags to create and the project ID to create them under.


###Example
<pre>
java -Dboilerplateapi.url=http://localhost:23334/boilerplateapi -Dfile.input=test-input/creation-data.json -Dfile.output=test-output/creation-output.json -jar util-boilerplate-creation.jar
</pre>

##Input & Output
The input file should be in json format and laid out with a projectId to be used when creating boilerplate expressions and tags and then the expressions and tags themselves.

The structure of the expressions and tags is the same as defined for the web-api except for boilerplate expressions having a property "tempId". This ID is for use with tags and will be discarded once the expression is created and assigned a unique ID on creation. The utility will replace any expression ids that a tag refers to with the creation ID it was given.

###Example Input
<pre>
{
  "projectId": "123456",
  "expressions": [
    {
      "tempId": 1,
      "name": "Test 1",
      "description": "Test description 1",
      "expression": "HP Ltd"
    },
    {
      "tempId": 2,
      "name": "Test 2",
      "description": "Test description 2",
      "expression": "aaaaa"
    },
    {
      "tempId": 3,
      "name": "Test 3",
      "description": "Test description 3",
      "expression": "aaaaa"
    }
  ],
  "tags": [
    {
      "name": "Tag 1",
      "description": "This is tag 1",
      "defaultReplacementText": "<redacted>",
      "boilerplateExpressions": [2,1]
    },
      {
        "name": "Tag 2",
        "description": "This is tag 2",
        "defaultReplacementText": "N/A",
        "boilerplateExpressions": [1]
      }
  ]
}
</pre>

The output of the utility is a file containining json representations of the ID and name properties of created tags and expressions. 

###Example Output

<pre>
{
   "projectId": "123456",
   "expressions": [
      {
         "id": 30292,
         "name": "Test 1"
      },
      {
         "id": 30293,
         "name": "Test 2"
      },
      {
         "id": 30294,
         "name": "Test 3"
      }
   ],
   "tags": [
      {
         "id": 3238,
         "name": "Tag 1"
      },
      {
         "id": 3239,
         "name": "Tag 2"
      }
   ]
}
</pre>

Note that the Tags will have been created with the tempID defined on the input file replaced with the ID given to the expression when it was actually created.

e.g. After creation BoilerplateExpressions for "Tag 1" is not [2,1] but is [30293, 30292].