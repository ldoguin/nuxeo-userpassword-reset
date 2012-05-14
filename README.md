nuxeo-userpassword-reset
=============

This project adds a 'forgotten password' link on the default login page of Nuxeo. The user has to enter is email address, will reveive a mail with a link to a form asking for the new password.

Make sure your userDirectory is not read-only and don't forget to configure the following properties in nuxeo.conf if you want to send mail:
mail.smtp.host=
mail.smtp.port=
mail.smtp.auth=
mail.smtp.username=
mail.smtp.password=
mail.from=
