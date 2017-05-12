## CAF_5016 - Signature removal from emails in non-English language ##

Verify that signatures in an email thread  in non-English language can be identified and removed from the email content

**Test Steps**

1. Set up system to perform Boilerplate with Signature Detection parameters configured to remove signatures from the content
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings in non-English language

**Expected Result**

The files are all processed, the signatures will be detected and the output will be the content of the emails with the signatures removed

**JIRA Link** - [CAF-842](https://jira.autonomy.com/browse/CAF-842)

**Actual Result**

Foreign language sign offs from email are not picked up e.g. `Cordialement` and `Mit freundlichen Grüßen` 



