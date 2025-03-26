import java.util.Date;
public class Message {

    public Date timestamp;
    public String message;
    public Participant sender;

    public Message(Date timestamp, String message, Participant sender) {
        this.timestamp = timestamp;
        this.message = message;
        this.sender = sender;
    }
}