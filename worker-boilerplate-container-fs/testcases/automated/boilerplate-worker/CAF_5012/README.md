## CAF_5012 - KeyContent segregation for emails - primary and secondary content only ##

Verify that only the primary and secondary content and be returned as KeyContent from an email thread

**Test Steps**

1. Set up system to perform Boilerplate with KeyContent parameters configured to only return primary and secondary content of an email
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings

**Expected Result**

The files are all processed and the output will be only 2 content files for primary and secondary content. The content will be the expected section of the email conversation as per the email range configuration.

**JIRA Link** - [CAF-932](https://jira.autonomy.com/browse/CAF-932)

