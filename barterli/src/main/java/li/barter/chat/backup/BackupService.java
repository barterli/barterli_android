package li.barter.chat.backup;

import android.app.IntentService;
import android.content.Intent;

/**
 * Service that takes care of backing up the chat messages
 * <p/>
 * Created by vinaysshenoy on 28/09/14.
 */
public class BackupService extends IntentService {

    private static final String TAG = "BackupService";

    /**
     * Action to backup chats
     */
    public static final String ACTION_BACKUP_CHATS = "li.barter.ACTION_BACKUP_CHATS";

    /**
     * Action to restore chats
     */
    public static final String ACTION_RESTORE_CHATS = "li.barter.ACTION_RESTORE_CHATS";

    /**
     * Agent that takes care of backing up and restoring chats
     * */
    private ChatBackupAgent mBackupAgent;

    public BackupService(String name) {
        super(name);
        mBackupAgent = new ExternalStorageChatBackupAgent();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String action = intent.getAction();

        if (ACTION_BACKUP_CHATS.equals(action)) {
            backupChatMessages();
        } else if (ACTION_RESTORE_CHATS.equals(action)) {


        }

    }

    private void backupChatMessages() {
        mBackupAgent.backupChats();
    }
}
