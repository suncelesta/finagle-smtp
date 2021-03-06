# finagle-smtp

[![Build status](https://img.shields.io/travis/finagle/finagle-smtp/master.svg)](http://travis-ci.org/finagle/finagle-smtp) [![Coverage status](https://img.shields.io/coveralls/finagle/finagle-smtp/master.svg)](https://coveralls.io/r/finagle/finagle-smtp?branch=master)

This is a minimum implementation of SMTP client for finagle according to 
[`RFC5321`][rfc]. Please see the [API documentation][docs] for information
that isn't covered in the introduction below.

Note: There is no API yet in this implementation for creating 
[`MIME`][mimewiki] messages, so the message should be plain US-ASCII text, or converted 
to such. There is currently no support for any other SMTP extensions, either. This 
functionality is to be added in future versions.

[rfc]: http://tools.ietf.org/search/rfc5321
[docs]: https://finagle.github.io/finagle-smtp/docs/
[mimewiki]: http://en.wikipedia.org/wiki/MIME

## Usage

### Sending an email

The object for instantiating a client capable of sending a simple email is `SmtpSimple`.
For services created with it the request type is `EmailMessage`, described in 
[`EmailMessage.scala`][EmailMessage].

You can create an email using `DefaultEmail` class described in [`DefaultEmail.scala`][DefaultEmail]:

```scala
    val email = DefaultEmail()
      .from_("from@from.com")
      .to_("first@to.com", "second@to.com")
      .subject_("test")
      .text("first line", "second line") //body is a sequence of lines
```

Applying the service on the email returns `Future.Done` in case of a successful operation.
In case of failure it returns the first encountered error wrapped in a `Future`.

[EmailMessage]: src/main/scala/com/twitter/finagle/smtp/EmailMessage.scala
[DefaultEmail]: src/main/scala/com/twitter/finagle/smtp/DefaultEmail.scala

#### Greeting and session

Upon the connection the client receives server greeting.
In the beginning of the session an EHLO request is sent automatically to identify the client.
The session state is reset before every subsequent try.

### Sending independent SMTP commands

The object for instantiating an SMTP client capable of sending any command defined in *RFC5321* is `Smtp`. 

For services created with it the request type is `Request`. Command classes are described in 
[`Request.scala`][Request]. 

Replies are differentiated by groups, which are described in [`ReplyGroups.scala`][ReplyGroups].
The concrete reply types are case classes described in [`SmtpReplies.scala`][SmtpReplies].

This allows flexible error handling:

```scala
val res = service(command) onFailure {
  // An error group
  case ex: SyntaxErrorReply => log.error("Syntax error: %s", ex.info)

  // A concrete reply
  case ProcessingError(info) => log,error("Error processing request: %s", info)

  // Default
  case _ => log.error("Error!")
}

// Or, another way:

res handle {
  ...
}
```

[Request]: src/main/scala/com/twitter/finagle/smtp/Request.scala
[ReplyGroups]: src/main/scala/com/twitter/finagle/smtp/reply/ReplyGroups.scala
[SmtpReplies]: src/main/scala/com/twitter/finagle/smtp/reply/SmtpReplies.scala

#### Greeting and session

Default SMTP client only connects to the server and receives its greeting, but does not return greeting,
as some commands may be executed without it. In case of malformed greeting the service is closed.
Upon service.close() a quit command is sent automatically, if not sent earlier.

License
-------

Licensed under the **[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)** (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
