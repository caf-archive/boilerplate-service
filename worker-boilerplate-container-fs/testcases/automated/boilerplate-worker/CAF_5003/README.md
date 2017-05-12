## CAF_5003 Replace boilerplate text from a document ##

Process the specified documents that contain boilerplate text defined in the database

**Test Steps**

1. Set up system with defined boilerplates in the database
2. Perform boilerplate extract using replace mode
3. Examine output

**Test Data**

Variety of text documents some of which contain defined boilerplate text and some that do not

**Expected Result**

The files are all processed and the presence of boilerplate text is replaced with the defined string from only the files that contain the boilerplate text

**JIRA Link** - [CAF-390](https://jira.autonomy.com/browse/CAF-390)

