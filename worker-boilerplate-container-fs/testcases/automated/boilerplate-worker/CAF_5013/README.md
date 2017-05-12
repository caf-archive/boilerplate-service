## CAF_5013 - Signature removal from emails ##

Verify that signatures in an email thread can be identified and removed from the email content

**Test Steps**

1. Set up system to perform Boilerplate with Signature Detection parameters configured to remove signatures from the content
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings

**Expected Result**

The files are all processed, the signatures will be detected and the output will be the content of the emails with the signatures removed

**JIRA Link** - [CAF-842](https://jira.autonomy.com/browse/CAF-842)

