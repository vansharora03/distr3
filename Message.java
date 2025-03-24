import java.util.Date;
public class Message {

    public Date timestamp;
    public String message;

    public Message(Date timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }
}