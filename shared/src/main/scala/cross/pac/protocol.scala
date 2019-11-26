package cross.pac

import cross.general.protocol._

object protocol {

  /** Common fields for paginated responses */
  trait Page {
    /** The requested or default entity offset */
    def offset: Int
    /** The requested or default page size limit */
    def limit: Int
    /** The total number of entities on the server */
    def total: Int
  }

  /** Simply wraps message list into an object */
  case class MessageList(messages: List[ChatMessage])

  /** Describes a message from the user
    *
    * @param id              the discord message id
    * @param text            the message text in discord format
    * @param author          the info about message author
    * @param createTimestamp the epoch millis when message was first sent to discord
    */
  case class ChatMessage(id: String, text: String, author: User, createTimestamp: Long)

  /** Simply wraps challenge list into an object */
  case class ArtChallengeList(challenges: List[ArtChallenge])

  /** Describes an art challenge topic held within some time span
    *
    * @param id             the internal id of the art challenge
    * @param name           the art challenge label or topic
    * @param video          Some(url) to the video containing art challenge review, None if video is not linked
    * @param startTimestamp the epoch millis when the art challenge was started
    * @param endTimestamp   Some(epoch millis) when the art challenge ended, None if art challenge is ongoing
    * @param submissions    the url to art challenge submissions
    */
  case class ArtChallenge(id: String,
                          name: String,
                          video: Option[String],
                          startTimestamp: Long,
                          endTimestamp: Option[Long],
                          submissions: String)

  /** Simply wraps submission list into an object */
  case class SubmissionList(submissions: List[Submission])

  /** Describes a single art challenge entry from the user
    *
    * Entry usually includes some comment or multiple comments from the user
    * These comments are put into text field, which may be empty if user did not leave any comments near the entry
    *
    * Entry must have at least one external resource (text entries, like poems, are not considered entries right now)
    * Entry may include multiple resources: multiple images if it's a comic sequence, or a before/after type of entry
    * User may have multiple entries per challenge if they are separated by somebody else messages or entries
    *
    * @param id              the internal id of the art challenge entry
    * @param author          the author of the entry
    * @param createTimestamp the epoch millis when the entry was submitted
    * @param text            the list of text messages around the entry resources, each message can still be multi-line
    * @param resources       the list of submitted resources with at least one always present
    */
  case class Submission(id: String, author: User, createTimestamp: Long, text: List[String], resources: List[SubmissionResource])

  /** Describes a single resource: image, video, recording, etc - that was submitted to an art challenge
    *
    * @param id        the internal id of the submitted resource
    * @param url       the originally submitted url
    * @param thumbnail Some(url) to generated thumbnail image, None if thumbnail is not generated yet, or failed to generate
    */
  case class SubmissionResource(id: String, url: String, thumbnail: Option[String])

}