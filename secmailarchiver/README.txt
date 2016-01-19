secmailarchive version 0.0.1
inpher-api version 0.6 (www.inpher.io)

Description:
This application allow you to securely store an email archive (Microsoft .PST) in 
Amazon s3 without losing the ability to search for specific emails based on keywords. 
A searchable encryption scheme is used to search the archive on s3 without 
downloading the encrypted archive.

Usage:

java jar secmailarchive.jar <username> <password> <options...>

<username>    Inpher Username
<password>    Inpher Password

<options>

    register                 Registers a new Inpher user using <username> <password>
    upload <file>            Encrypts indexes and uploads .pst <file>
    search <kw 1> <kw 2> ... Searches and downloads top 10 emails containing all 
                             provided keywords
