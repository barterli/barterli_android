package li.barter.chat.backup;

/**
 * Interface that defines how a chat backup agent works
 * <p/>
 * Created by vinaysshenoy on 28/09/14.
 */
public interface ChatBackupAgent {

    /**
     * Method that will be called to backup the chats. This will be called on a separate thread.
     * <p/>
     * This will be called at regular intervals to backup the chat messages.
     *
     * @return {@code true} if the chat backup was successful, {@code false} otherwise
     */
    public boolean backupChats();

    /**
     * Method that will be called to restore the chats. This will be called on a separate thread.
     * <p/>
     * This will be called only once, at the very first login of a user to restore the chat messages
     */
    public void restoreChats();
}
