## CAF_5018 - Invalid Expression Id sent to Boilerplate Worker ##

Verify that a task sent to boilerplate worker with an invalid expression id is returned as an INVALID_TASK

**Test Steps**

1. Set up system to perform Boilerplate and send a task message to the worker that contains an expression id that does not exist or is otherwise invalid
2. Examine the output

**Test Data**

Plain text files

**Expected Result**

The output message is returned with a status of INVALID_TASK

**JIRA Link** - [CAF-1703](https://jira.autonomy.com/browse/CAF-1703)




