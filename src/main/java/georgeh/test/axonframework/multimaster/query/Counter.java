package georgeh.test.axonframework.multimaster.query;

import georgeh.test.axonframework.multimaster.mongo.DocumentNameParts;
import org.jgroups.util.UUID;
import org.springframework.data.annotation.Id;

/**
 * @author Georgeh
 *
 */
@DocumentNameParts(documentType = Counter.class, handlerType=CounterEventHandler.class)
public class Counter {

    @Id
    private String id;

    private long value;
    
    private String something;

    public Counter() {
        super();
        something = UUID.randomUUID().toString();
    }

    public Counter(final String id) {
        super();
        this.id = id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(final String something) {
        this.something = something;
    }
        
}
