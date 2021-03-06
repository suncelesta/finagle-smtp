package io.github.finagle.smtp.filter

import com.twitter.finagle.{Service, Filter}
import com.twitter.util.Future
import io.github.finagle.smtp._
import io.github.finagle.smtp.reply.Reply

/**
 * Sends [[io.github.finagle.smtp.EmailMessage]], transforming it to a sequence of SMTP commands.
 */
object MailFilter extends Filter[EmailMessage, Unit, Request, Reply]{
  /**
   * @return [[com.twitter.util.Future.Done]] if the message was sent successfully,
   *        or the first encountered error.
   */
   override def apply(msg: EmailMessage, send: Service[Request, Reply]): Future[Unit] = {
     val SendEmailRequest: Seq[Request] =
       Seq(Request.AddSender(msg.sender))   ++
       msg.to.map(Request.AddRecipient(_))  ++
       msg.cc.map(Request.AddRecipient(_))  ++
       msg.bcc.map(Request.AddRecipient(_)) ++
       Seq(Request.BeginData, Request.Data(msg.body))

     val reqs: Seq[Request] =
       Seq(Request.Reset) ++
       SendEmailRequest

     val freqs = for (req <- reqs) yield send(req)

     Future.join(freqs)

   }
 }
