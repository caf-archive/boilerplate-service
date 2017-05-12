## CAF_5017 - Signature detection in emails in non-English language ##

Verify that signatures in an email thread in non-English language can be identified

**Test Steps**

1. Set up system to perform Boilerplate with Signature Detection parameters configured to detect signatures in the content
2. Examine the output

**Test Data**

Plain text files containing the content of email conversation strings in non-English language

**Expected Result**

The files are all processed, the signatures will be detected and the output will be the content of the emails with the signatures still present

**JIRA Link** - [CAF-842](https://jira.autonomy.com/browse/CAF-842)

**Actual Result**

Foreign language sign offs from email are not picked up e.g. `Cordialement` and `Mit freundlichen Grüßen` 



