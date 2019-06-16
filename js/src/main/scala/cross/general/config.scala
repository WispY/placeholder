package cross.general

object config {

  /** General configuration for all projects
    *
    * @param server the protocol and host part for server uris
    * @param client the protocol and host part for client uris
    */
  case class GeneralConfig(server: String,
                           client: String)

  val DefaultConfig = GeneralConfig(
    server = "http://127.0.0.1:8081",
    client = "http://127.0.0.1:8080"
  )

  val Config: GeneralConfig = DefaultConfig

}