package georgeh.test.axonframework.multimaster.rest;

import georgeh.test.axonframework.multimaster.domain.api.CounterCreateCommand;
import georgeh.test.axonframework.multimaster.domain.api.CounterIncreaseCommand;
import georgeh.test.axonframework.multimaster.query.Counter;
import georgeh.test.axonframework.multimaster.query.CounterRepository;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.axonframework.commandhandling.gateway.CommandGateway;

@Path("/counter")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CounterApi {

    @Inject
    private CommandGateway commandGateway;

    @Inject
    private CounterRepository repo;

    @PUT
    public Counter createNew() {
        String id = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CounterCreateCommand(id));
        return new Counter(id);
    }

    @PUT
    @Path("{id}")
    public Counter increase(@PathParam("id") final String id) {
        int newValue = commandGateway.sendAndWait(new CounterIncreaseCommand(id));
        Counter counter = repo.findOne(id);
        if (counter == null) {
            throw new NotFoundException();
        }
        counter.setValue(newValue); // because the value in the repo can be stale
        return counter;
    }

    @GET
    @Path("{id}")
    public Counter get(@PathParam("id") final String id) {
        return repo.findOne(id);
    }

    @GET
    public List<Counter> list() {
        return repo.findAll();
    }

}
