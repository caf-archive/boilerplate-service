## CAF_5010 - KeyContent segregation for emails - last reply is primary content ##

Verify that the last reply in an email thread can be returned as primary KeyContent

**Test Steps**

1. Set up system to perform Boilerplate with KeyContent parameters configured to return primary content as the most recent email, secondary content as the previous 2 replies and tertiary content as the original email and all other replies of the text content of an email
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings

**Expected Result**

The files are all processed and the output will be three separate content files for primary, secondary and tertiary content. The content will be the expected section of the email conversation as per the email range configuration.

**JIRA Link** - [CAF-932](https://jira.autonomy.com/browse/CAF-932)

