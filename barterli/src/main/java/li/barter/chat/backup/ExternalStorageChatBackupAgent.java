package li.barter.chat.backup;

import android.database.Cursor;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import li.barter.data.DBInterface;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.utils.Logger;

/**
 * Class that backs up chat messages on the external storage
 * <p/>
 * Created by vinaysshenoy on 28/09/14.
 */
public class ExternalStorageChatBackupAgent implements ChatBackupAgent {

    private static final String TAG = "ExternalStorageChatBackupAgent";

    private static final String FILE_NAME_FORMAT = "barterli_chat_backup_%s.bak";

    /**
     * Class that takes care of serializing and deserializing chats
     */
    private ChatSerializer mSerializer;

    public ExternalStorageChatBackupAgent() {
        mSerializer = new ChatSerializer();
    }

    @Override
    public boolean backupChats() {

        final Cursor chatsCursor = DBInterface.query(false, TableChats.NAME, null, null, null, null, null, null, null);
        final Cursor messagesCursor = DBInterface.query(false, TableChatMessages.NAME, null, null, null, null, null, null, null);

        //TODO: Vinay - Change to File Output Stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
        try {
            mSerializer.beginWrite(chatsCursor, messagesCursor, outputStream);
            final String chatJson = outputStream.toString(HTTP.UTF_8);
            Logger.d(TAG, "Chat Json %s", chatJson);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                outputStream.close();
            } catch (IOException e) {
                //Not much we can do here
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public void restoreChats() {

    }
}
