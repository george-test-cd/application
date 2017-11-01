package georgeh.test.axonframework.multimaster.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;

@Path("/stats")
public class StatsApi {

    @Inject
    private DistributedCommandBus distributedCommandBus;

    @Path("/health")
    @GET
    public Response healthCheck() {
        return distributedCommandBus.getLoadFactor() > 0 ? Response.ok().build() : Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

}
