package li.barter.chat.backup;

/**
 * Class that backs up chat messages on the external storage
 *
 * Created by vinaysshenoy on 28/09/14.
 */
public class ExternalStorageChatBackupAgent implements ChatBackupAgent {

    private static final String TAG = "ExternalStorageChatBackupAgent";

    private static final String FILE_NAME_FORMAT = "barterli_chat_backup_%s.bak";

    @Override
    public boolean backupChats() {
        return false;
    }

    @Override
    public void restoreChats() {

    }
}
