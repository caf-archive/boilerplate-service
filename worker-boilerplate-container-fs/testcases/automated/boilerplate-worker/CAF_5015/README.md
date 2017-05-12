## CAF_5015 - KeyContent segregation for emails - overlapping content ##

Verify that KeyContent can be returned into the multiple categories

**Test Steps**

1. Set up system to perform Boilerplate with KeyContent parameters configured to return primary content as the original email and first reply, secondary content as the first and second replies and tertiary content as second reply and all other replies of the text content of an email
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings

**Expected Result**

The files are all processed and the output will be three separate content files for primary, secondary and tertiary content. The content will be the expected section of the email conversation as per the email range configuration. The first reply will be contained within both primary and secondary content while the second reply will be in both the secondary and tertiary content.

**JIRA Link** - [CAF-932](https://jira.autonomy.com/browse/CAF-932)

