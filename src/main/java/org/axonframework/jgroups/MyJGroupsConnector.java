package org.axonframework.jgroups;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.Member;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.commandhandling.distributed.ServiceRegistryException;
import org.axonframework.common.Registration;
import org.axonframework.jgroups.commandhandling.JGroupsConnector;
import org.axonframework.jgroups.commandhandling.JoinMessage;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.serialization.Serializer;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyJGroupsConnector extends JGroupsConnector {
    private Logger LOG = LoggerFactory.getLogger(MyJGroupsConnector.class);
    private Predicate<? super CommandMessage<?>> commandFilter;
    private final JChannel channel;

    public MyJGroupsConnector(final CommandBus localSegment, final JChannel channel, final String clusterName, final Serializer serializer, final RoutingStrategy routingStrategy) {
        super(localSegment, channel, clusterName, serializer, routingStrategy);
        this.channel = channel;
    }

    public MyJGroupsConnector(final CommandBus localSegment, final JChannel channel, final String clusterName, final Serializer serializer) {
        super(localSegment, channel, clusterName, serializer);
        this.channel = channel;
    }

    @Override
    public Registration subscribe(final String commandName, final MessageHandler<? super CommandMessage<?>> handler) {
        LOG.info("Subscribing to command {} using handler {}", commandName, handler);
        return super.subscribe(commandName, handler);
    }

    @Override
    public void updateMembership(final int loadFactor, final Predicate<? super CommandMessage<?>> commandFilter) {
        this.commandFilter = commandFilter;
        super.updateMembership(loadFactor, commandFilter);
    }

    @Override
    public void receive(final Message msg) {
        if (msg.getObject() instanceof RequestMembershipUpdateMessage) {
            broadCastMembership();
            return;
        }
        if (msg.getObject() instanceof JoinMessage) {
            JoinMessage m = (JoinMessage) msg.getObject();
            LOG.info("Processing join message with filter {} from {}", m.messageFilter(), msg.src());
        }
        super.receive(msg);
    }

    @Override
    public Optional<Member> findDestination(final CommandMessage<?> message) {
        Optional<Member> result = super.findDestination(message);
        if (!result.isPresent()) {
            StringBuilder sb = new StringBuilder();
            sb.append("No node known to accept ").append(message.getCommandName()).append('\n');
            sb.append("Filter: ").append(commandFilter).append('\n');
            sb.append(super.getConsistentHash().getMembers().stream().map(ReflectionToStringBuilder::toString).collect(Collectors.joining(", ", "Members: ", "\n")));

            LOG.info(sb.toString());
            broadCastMembershipRequest();
        }
        return result;
    }

    protected void broadCastMembershipRequest() throws ServiceRegistryException {
        try {
            if (channel.isConnected()) {
                Message joinMessage = new Message(null, new RequestMembershipUpdateMessage());
                joinMessage.setFlag(Message.Flag.OOB);
                channel.send(joinMessage);
            }
        } catch (Exception e) {
            throw new ServiceRegistryException("Could not broadcast membership request to the cluster", e);
        }
    }

}
