import actors.CustomerRepositoryActor
import com.google.inject.AbstractModule
import config.Names
import play.libs.akka.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure () = {

    bindActor [CustomerRepositoryActor] ( classOf [ CustomerRepositoryActor ], Names.CUSTOMER_REPOSITORY_ACTOR )
  }
}